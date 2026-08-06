package ru.yandex.practicum.telemetry.analyzer.processor;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.service.SnapshotService;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@Component
public class SnapshotProcessor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(SnapshotProcessor.class);

    private final Consumer<String, SensorsSnapshotAvro> consumer;
    private final SnapshotService service;
    private final String topic;
    private final Duration pollTimeout;
    private volatile boolean running = true;

    public SnapshotProcessor(@Qualifier("snapshotConsumer") Consumer<String, SensorsSnapshotAvro> consumer,
                             SnapshotService service,
                             @Value("${analyzer.kafka.snapshots-topic}") String topic,
                             @Value("${analyzer.kafka.snapshot-consumer.poll-timeout-ms:1000}") long pollTimeoutMs) {
        this.consumer = consumer;
        this.service = service;
        this.topic = topic;
        this.pollTimeout = Duration.ofMillis(pollTimeoutMs);
    }

    @Override
    public void run() {
        consumer.subscribe(Collections.singletonList(topic));
        log.info("Snapshot processor subscribed to {}", topic);
        try {
            while (running) {
                for (ConsumerRecord<String, SensorsSnapshotAvro> record : consumer.poll(pollTimeout)) {
                    if (!process(record)) {
                        break;
                    }
                }
            }
        } catch (WakeupException exception) {
            if (running) {
                throw exception;
            }
        } finally {
            consumer.close();
        }
    }

    private boolean process(ConsumerRecord<String, SensorsSnapshotAvro> record) {
        TopicPartition partition = new TopicPartition(record.topic(), record.partition());
        try {
            service.handle(record.value());
            consumer.commitSync(Map.of(partition, new OffsetAndMetadata(record.offset() + 1)));
            return true;
        } catch (RuntimeException exception) {
            log.error("Failed to process snapshot at {}-{} offset {}. The record will be retried",
                    record.topic(), record.partition(), record.offset(), exception);
            consumer.seek(partition, record.offset());
            pauseBeforeRetry();
            return false;
        }
    }

    private void pauseBeforeRetry() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            running = false;
        }
    }

    public void stop() {
        running = false;
        consumer.wakeup();
    }
}
