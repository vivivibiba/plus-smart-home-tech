package ru.yandex.practicum.telemetry.analyzer.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.telemetry.analyzer.model.Action;
import ru.yandex.practicum.telemetry.analyzer.model.ActionType;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.model.ConditionOperation;
import ru.yandex.practicum.telemetry.analyzer.model.ConditionType;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.model.Sensor;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class HubEventService {
    private static final Logger log = LoggerFactory.getLogger(HubEventService.class);

    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;

    public HubEventService(SensorRepository sensorRepository, ScenarioRepository scenarioRepository) {
        this.sensorRepository = sensorRepository;
        this.scenarioRepository = scenarioRepository;
    }

    @Transactional
    public void handle(HubEventAvro event) {
        Objects.requireNonNull(event, "event");
        Object payload = Objects.requireNonNull(event.getPayload(), "event.payload");
        String hubId = Objects.requireNonNull(event.getHubId(), "event.hubId");

        if (payload instanceof DeviceAddedEventAvro addedEvent) {
            addDevice(hubId, addedEvent);
        } else if (payload instanceof DeviceRemovedEventAvro removedEvent) {
            removeDevice(hubId, removedEvent);
        } else if (payload instanceof ScenarioAddedEventAvro addedEvent) {
            addScenario(hubId, addedEvent);
        } else if (payload instanceof ScenarioRemovedEventAvro removedEvent) {
            removeScenario(hubId, removedEvent);
        } else {
            throw new IllegalArgumentException("Unsupported hub event payload: " + payload.getClass().getName());
        }
    }

    private void addDevice(String hubId, DeviceAddedEventAvro event) {
        sensorRepository.findById(event.getId()).ifPresentOrElse(existing -> {
            if (!existing.getHubId().equals(hubId)) {
                throw new IllegalStateException("Sensor " + event.getId() + " is already registered in hub "
                        + existing.getHubId());
            }
            log.debug("Sensor {} is already registered in hub {}", event.getId(), hubId);
        }, () -> sensorRepository.save(new Sensor(event.getId(), hubId)));
    }

    private void removeDevice(String hubId, DeviceRemovedEventAvro event) {
        sensorRepository.findByIdAndHubId(event.getId(), hubId).ifPresent(sensor -> {
            for (Scenario scenario : scenarioRepository.findByHubId(hubId)) {
                boolean changed = scenario.getConditions().remove(event.getId()) != null;
                changed |= scenario.getActions().remove(event.getId()) != null;

                if (changed) {
                    if (scenario.getConditions().isEmpty() || scenario.getActions().isEmpty()) {
                        scenarioRepository.delete(scenario);
                    } else {
                        scenarioRepository.save(scenario);
                    }
                }
            }
            scenarioRepository.flush();
            sensorRepository.delete(sensor);
        });
    }

    private void addScenario(String hubId, ScenarioAddedEventAvro event) {
        Set<String> sensorIds = new LinkedHashSet<>();
        event.getConditions().forEach(condition -> sensorIds.add(condition.getSensorId()));
        event.getActions().forEach(action -> sensorIds.add(action.getSensorId()));

        int registeredSensors = sensorRepository.findAllByIdInAndHubId(sensorIds, hubId).size();
        if (registeredSensors != sensorIds.size()) {
            throw new IllegalStateException("Scenario " + event.getName()
                    + " references sensors that are not registered in hub " + hubId);
        }

        scenarioRepository.findByHubIdAndName(hubId, event.getName()).ifPresent(existing -> {
            scenarioRepository.delete(existing);
            scenarioRepository.flush();
        });

        Map<String, Condition> conditions = new LinkedHashMap<>();
        for (ScenarioConditionAvro condition : event.getConditions()) {
            conditions.put(condition.getSensorId(), new Condition(
                    ConditionType.valueOf(condition.getType().name()),
                    ConditionOperation.valueOf(condition.getOperation().name()),
                    conditionValue(condition.getValue())
            ));
        }

        Map<String, Action> actions = new LinkedHashMap<>();
        event.getActions().forEach(action -> actions.put(action.getSensorId(), new Action(
                ActionType.valueOf(action.getType().name()),
                action.getValue()
        )));

        scenarioRepository.save(new Scenario(hubId, event.getName(), conditions, actions));
    }

    private void removeScenario(String hubId, ScenarioRemovedEventAvro event) {
        scenarioRepository.findByHubIdAndName(hubId, event.getName())
                .ifPresent(scenarioRepository::delete);
    }

    private int conditionValue(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue ? 1 : 0;
        }
        if (value instanceof Integer integerValue) {
            return integerValue;
        }
        throw new IllegalArgumentException("Unsupported scenario condition value: " + value);
    }
}
