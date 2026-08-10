package ru.yandex.practicum.telemetry.aggregator.service;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Component
public class AggregationStarter {
    private static final Logger log = LoggerFactory.getLogger(AggregationStarter.class);

    private final Consumer<String, SensorEventAvro> consumer;
    private final Producer<String, SpecificRecordBase> producer;
    private final SnapshotAggregator snapshotAggregator;
    private final String sensorEventsTopic;
    private final String snapshotsTopic;
    private final Duration pollTimeout;

    private volatile boolean running = true;

    public AggregationStarter(Consumer<String, SensorEventAvro> consumer,
                              Producer<String, SpecificRecordBase> producer,
                              SnapshotAggregator snapshotAggregator,
                              @Value("${aggregator.kafka.sensor-events-topic}") String sensorEventsTopic,
                              @Value("${aggregator.kafka.snapshots-topic}") String snapshotsTopic,
                              @Value("${aggregator.kafka.consumer.poll-timeout-ms:1000}") long pollTimeoutMs) {
        this.consumer = consumer;
        this.producer = producer;
        this.snapshotAggregator = snapshotAggregator;
        this.sensorEventsTopic = sensorEventsTopic;
        this.snapshotsTopic = snapshotsTopic;
        this.pollTimeout = Duration.ofMillis(pollTimeoutMs);
    }

    public void start() {
        consumer.subscribe(Collections.singletonList(sensorEventsTopic));
        log.info("Aggregator subscribed to topic {}", sensorEventsTopic);

        try {
            while (running) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(pollTimeout);
                if (records.isEmpty()) {
                    continue;
                }

                Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    process(record.value());
                    offsets.put(
                            new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1)
                    );
                }

                producer.flush();
                consumer.commitSync(offsets);
            }
        } catch (WakeupException exception) {
            if (running) {
                throw exception;
            }
        } catch (Exception exception) {
            log.error("Error while processing sensor events", exception);
        } finally {
            closeClients();
        }
    }

    public void stop() {
        running = false;
        consumer.wakeup();
    }

    private void process(SensorEventAvro event) {
        Optional<SensorsSnapshotAvro> updatedSnapshot = snapshotAggregator.updateState(event);
        if (updatedSnapshot.isEmpty()) {
            return;
        }

        SensorsSnapshotAvro snapshot = updatedSnapshot.orElseThrow();
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                snapshotsTopic,
                null,
                snapshot.getTimestamp().toEpochMilli(),
                snapshot.getHubId(),
                snapshot
        );

        try {
            var metadata = producer.send(record).get();
            if (log.isDebugEnabled()) {
                log.debug("Snapshot for hub {} sent to {}-{} at offset {}",
                        snapshot.getHubId(), metadata.topic(), metadata.partition(), metadata.offset());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while sending snapshot for hub "
                    + snapshot.getHubId(), exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Failed to send snapshot for hub "
                    + snapshot.getHubId(), exception.getCause());
        }
    }

    private void closeClients() {
        try {
            producer.flush();
        } finally {
            log.info("Closing Kafka consumer");
            consumer.close();
            log.info("Closing Kafka producer");
            producer.close();
        }
    }
}
