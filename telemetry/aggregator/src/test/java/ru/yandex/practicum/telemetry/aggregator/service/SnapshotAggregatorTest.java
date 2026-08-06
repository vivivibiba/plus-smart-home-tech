package ru.yandex.practicum.telemetry.aggregator.service;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SnapshotAggregatorTest {
    private final SnapshotAggregator aggregator = new SnapshotAggregator();

    @Test
    void shouldCreateSnapshotForFirstEvent() {
        Instant timestamp = Instant.parse("2026-08-06T00:00:00Z");
        SensorEventAvro event = event(timestamp, new LightSensorAvro(90, 150));

        Optional<SensorsSnapshotAvro> result = aggregator.updateState(event);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getHubId()).isEqualTo("hub-1");
        assertThat(result.orElseThrow().getTimestamp()).isEqualTo(timestamp);
        assertThat(result.orElseThrow().getSensorsState()).containsKey("sensor-1");
    }

    @Test
    void shouldIgnoreDuplicateData() {
        Instant firstTimestamp = Instant.parse("2026-08-06T00:00:00Z");
        aggregator.updateState(event(firstTimestamp, new LightSensorAvro(90, 150)));

        Optional<SensorsSnapshotAvro> result = aggregator.updateState(
                event(firstTimestamp.plusSeconds(5), new LightSensorAvro(90, 150))
        );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldIgnoreOlderEvent() {
        Instant firstTimestamp = Instant.parse("2026-08-06T00:00:10Z");
        aggregator.updateState(event(firstTimestamp, new LightSensorAvro(90, 150)));

        Optional<SensorsSnapshotAvro> result = aggregator.updateState(
                event(firstTimestamp.minusSeconds(1), new LightSensorAvro(80, 100))
        );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldUpdateChangedData() {
        Instant firstTimestamp = Instant.parse("2026-08-06T00:00:00Z");
        Instant secondTimestamp = firstTimestamp.plusSeconds(5);
        aggregator.updateState(event(firstTimestamp, new LightSensorAvro(90, 150)));

        Optional<SensorsSnapshotAvro> result = aggregator.updateState(
                event(secondTimestamp, new LightSensorAvro(80, 100))
        );

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getTimestamp()).isEqualTo(secondTimestamp);
        assertThat(result.orElseThrow().getSensorsState().get("sensor-1").getData())
                .isEqualTo(new LightSensorAvro(80, 100));
    }

    private SensorEventAvro event(Instant timestamp, LightSensorAvro payload) {
        return new SensorEventAvro("sensor-1", "hub-1", timestamp, payload);
    }
}
