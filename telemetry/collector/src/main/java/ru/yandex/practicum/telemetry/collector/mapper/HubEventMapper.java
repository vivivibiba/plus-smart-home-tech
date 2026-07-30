package ru.yandex.practicum.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAction;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceRemovedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioCondition;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioRemovedEvent;

import java.util.List;

@Component
public class HubEventMapper {

    public HubEventAvro toAvro(HubEvent event) {
        Object payload;
        if (event instanceof DeviceAddedEvent added) {
            payload = new DeviceAddedEventAvro(added.getId(), DeviceTypeAvro.valueOf(added.getDeviceType().name()));
        } else if (event instanceof DeviceRemovedEvent removed) {
            payload = new DeviceRemovedEventAvro(removed.getId());
        } else if (event instanceof ScenarioAddedEvent added) {
            List<ScenarioConditionAvro> conditions = added.getConditions().stream()
                    .map(this::toAvro)
                    .toList();
            List<DeviceActionAvro> actions = added.getActions().stream()
                    .map(this::toAvro)
                    .toList();
            payload = new ScenarioAddedEventAvro(added.getName(), conditions, actions);
        } else if (event instanceof ScenarioRemovedEvent removed) {
            payload = new ScenarioRemovedEventAvro(removed.getName());
        } else {
            throw new IllegalArgumentException("Unsupported hub event type: " + event.getClass().getName());
        }

        return new HubEventAvro(event.getHubId(), event.getTimestamp(), payload);
    }

    private ScenarioConditionAvro toAvro(ScenarioCondition condition) {
        Object value = condition.getValue();
        if (value instanceof Number number) {
            value = number.intValue();
        } else if (value != null && !(value instanceof Boolean)) {
            throw new IllegalArgumentException("Scenario condition value must be null, integer or boolean");
        }
        return new ScenarioConditionAvro(
                condition.getSensorId(),
                ConditionTypeAvro.valueOf(condition.getType().name()),
                ConditionOperationAvro.valueOf(condition.getOperation().name()),
                value
        );
    }

    private DeviceActionAvro toAvro(DeviceAction action) {
        return new DeviceActionAvro(
                action.getSensorId(),
                ActionTypeAvro.valueOf(action.getType().name()),
                action.getValue()
        );
    }
}
