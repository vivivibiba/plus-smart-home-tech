package ru.yandex.practicum.telemetry.analyzer.service;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;

import java.util.Map;

@Component
public class ScenarioEvaluator {

    public boolean matches(Scenario scenario, SensorsSnapshotAvro snapshot) {
        if (scenario.getConditions().isEmpty()) {
            return false;
        }
        return scenario.getConditions().entrySet().stream()
                .allMatch(entry -> matches(entry, snapshot));
    }

    private boolean matches(Map.Entry<String, Condition> entry, SensorsSnapshotAvro snapshot) {
        SensorStateAvro state = snapshot.getSensorsState().get(entry.getKey());
        if (state == null || state.getData() == null) {
            return false;
        }

        Integer actualValue = extractValue(entry.getValue(), state.getData());
        if (actualValue == null) {
            return false;
        }

        int expectedValue = entry.getValue().getValue();
        return switch (entry.getValue().getOperation()) {
            case EQUALS -> actualValue == expectedValue;
            case GREATER_THAN -> actualValue > expectedValue;
            case LOWER_THAN -> actualValue < expectedValue;
        };
    }

    private Integer extractValue(Condition condition, Object data) {
        return switch (condition.getType()) {
            case MOTION -> data instanceof MotionSensorAvro motion ? booleanToInt(motion.getMotion()) : null;
            case LUMINOSITY -> data instanceof LightSensorAvro light ? light.getLuminosity() : null;
            case SWITCH -> data instanceof SwitchSensorAvro switchSensor ? booleanToInt(switchSensor.getState()) : null;
            case TEMPERATURE -> temperature(data);
            case CO2LEVEL -> data instanceof ClimateSensorAvro climate ? climate.getCo2Level() : null;
            case HUMIDITY -> data instanceof ClimateSensorAvro climate ? climate.getHumidity() : null;
        };
    }

    private Integer temperature(Object data) {
        if (data instanceof ClimateSensorAvro climate) {
            return climate.getTemperatureC();
        }
        if (data instanceof TemperatureSensorAvro temperature) {
            return temperature.getTemperatureC();
        }
        return null;
    }

    private int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }
}
