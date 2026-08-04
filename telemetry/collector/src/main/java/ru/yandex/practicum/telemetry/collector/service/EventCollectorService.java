package ru.yandex.practicum.telemetry.collector.service;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.mapper.HubEventMapper;
import ru.yandex.practicum.telemetry.collector.mapper.SensorEventMapper;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;

@Service
public class EventCollectorService {
    private static final Logger log = LoggerFactory.getLogger(EventCollectorService.class);

    private final Producer<String, SpecificRecordBase> producer;
    private final SensorEventMapper sensorEventMapper;
    private final HubEventMapper hubEventMapper;
    private final String sensorEventsTopic;
    private final String hubEventsTopic;

    public EventCollectorService(Producer<String, SpecificRecordBase> producer,
                                 SensorEventMapper sensorEventMapper,
                                 HubEventMapper hubEventMapper,
                                 @Value("${collector.kafka.sensor-events-topic}") String sensorEventsTopic,
                                 @Value("${collector.kafka.hub-events-topic}") String hubEventsTopic) {
        this.producer = producer;
        this.sensorEventMapper = sensorEventMapper;
        this.hubEventMapper = hubEventMapper;
        this.sensorEventsTopic = sensorEventsTopic;
        this.hubEventsTopic = hubEventsTopic;
    }

    public void collect(SensorEvent event) {
        SensorEventAvro avroEvent = sensorEventMapper.toAvro(event);
        send(sensorEventsTopic, event.getHubId(), event.getTimestamp().toEpochMilli(), avroEvent);
    }

    public void collect(HubEvent event) {
        HubEventAvro avroEvent = hubEventMapper.toAvro(event);
        send(hubEventsTopic, event.getHubId(), event.getTimestamp().toEpochMilli(), avroEvent);
    }

    private void send(String topic, String key, long timestamp, SpecificRecordBase event) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                topic,
                null,
                timestamp,
                key,
                event
        );

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to send telemetry event to topic {}", topic, exception);
            } else if (log.isDebugEnabled()) {
                log.debug("Sent telemetry event to {}-{} at offset {}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }
}
