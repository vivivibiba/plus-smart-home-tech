package ru.yandex.practicum.telemetry.aggregator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Builds and updates one current sensor snapshot per hub. */
@Component
public class SnapshotAggregator {
    private static final Logger log = LoggerFactory.getLogger(SnapshotAggregator.class);

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        if (event == null) {
            log.warn("Skipping invalid sensor event: event is null");
            return Optional.empty();
        }

        if (event.getHubId() == null
                || event.getId() == null
                || event.getTimestamp() == null
                || event.getPayload() == null) {
            log.warn("Skipping invalid sensor event: hubId={}, id={}, timestamp={}, payload={}",
                    event.getHubId(), event.getId(), event.getTimestamp(), event.getPayload());
            return Optional.empty();
        }

        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(
                event.getHubId(),
                hubId -> new SensorsSnapshotAvro(hubId, event.getTimestamp(), new HashMap<>())
        );

        SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());
        if (oldState != null
                && (oldState.getTimestamp().isAfter(event.getTimestamp())
                || Objects.equals(oldState.getData(), event.getPayload()))) {
            return Optional.empty();
        }

        SensorStateAvro newState = new SensorStateAvro(event.getTimestamp(), event.getPayload());
        snapshot.getSensorsState().put(event.getId(), newState);
        snapshot.setTimestamp(event.getTimestamp());
        return Optional.of(snapshot);
    }
}
