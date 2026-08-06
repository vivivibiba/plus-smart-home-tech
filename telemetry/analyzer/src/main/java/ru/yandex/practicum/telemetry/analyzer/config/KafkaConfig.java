package ru.yandex.practicum.telemetry.analyzer.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.deserializer.HubEventDeserializer;
import ru.yandex.practicum.kafka.deserializer.SensorsSnapshotDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Properties;

@Configuration
public class KafkaConfig {

    @Bean(name = "hubEventConsumer", destroyMethod = "")
    public Consumer<String, HubEventAvro> hubEventConsumer(
            @Value("${analyzer.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${analyzer.kafka.hub-consumer.group-id}") String groupId,
            @Value("${analyzer.kafka.hub-consumer.auto-offset-reset:earliest}") String autoOffsetReset,
            @Value("${analyzer.kafka.hub-consumer.max-poll-records:50}") int maxPollRecords) {
        Properties properties = baseConsumerProperties(bootstrapServers, groupId, autoOffsetReset, maxPollRecords);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializer.class);
        return new KafkaConsumer<>(properties);
    }

    @Bean(name = "snapshotConsumer", destroyMethod = "")
    public Consumer<String, SensorsSnapshotAvro> snapshotConsumer(
            @Value("${analyzer.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${analyzer.kafka.snapshot-consumer.group-id}") String groupId,
            @Value("${analyzer.kafka.snapshot-consumer.auto-offset-reset:earliest}") String autoOffsetReset,
            @Value("${analyzer.kafka.snapshot-consumer.max-poll-records:1}") int maxPollRecords) {
        Properties properties = baseConsumerProperties(bootstrapServers, groupId, autoOffsetReset, maxPollRecords);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorsSnapshotDeserializer.class);
        return new KafkaConsumer<>(properties);
    }

    private Properties baseConsumerProperties(String bootstrapServers,
                                              String groupId,
                                              String autoOffsetReset,
                                              int maxPollRecords) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        return properties;
    }
}
