package ru.yandex.practicum.telemetry.analyzer.service;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.model.Action;
import ru.yandex.practicum.telemetry.analyzer.model.ActionType;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.model.ConditionOperation;
import ru.yandex.practicum.telemetry.analyzer.model.ConditionType;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScenarioEvaluatorTest {
    private final ScenarioEvaluator evaluator = new ScenarioEvaluator();

    @Test
    void shouldMatchScenarioWhenEveryConditionIsTrue() {
        Instant timestamp = Instant.parse("2026-08-06T00:00:00Z");
        Map<String, SensorStateAvro> states = new HashMap<>();
        states.put("motion", new SensorStateAvro(timestamp, new MotionSensorAvro(50, true, 80)));
        states.put("light", new SensorStateAvro(timestamp, new LightSensorAvro(60, 300)));
        states.put("climate", new SensorStateAvro(timestamp, new ClimateSensorAvro(12, 40, 700)));

        Scenario scenario = new Scenario("hub-1", "night light", Map.of(
                "motion", new Condition(ConditionType.MOTION, ConditionOperation.EQUALS, 1),
                "light", new Condition(ConditionType.LUMINOSITY, ConditionOperation.LOWER_THAN, 500),
                "climate", new Condition(ConditionType.TEMPERATURE, ConditionOperation.LOWER_THAN, 15)
        ), Map.of("switch", new Action(ActionType.ACTIVATE, null)));

        SensorsSnapshotAvro snapshot = new SensorsSnapshotAvro("hub-1", timestamp, states);

        assertThat(evaluator.matches(scenario, snapshot)).isTrue();
    }

    @Test
    void shouldNotMatchWhenOneConditionIsFalse() {
        Instant timestamp = Instant.parse("2026-08-06T00:00:00Z");
        SensorsSnapshotAvro snapshot = new SensorsSnapshotAvro("hub-1", timestamp, Map.of(
                "light", new SensorStateAvro(timestamp, new LightSensorAvro(60, 900))
        ));
        Scenario scenario = new Scenario("hub-1", "light", Map.of(
                "light", new Condition(ConditionType.LUMINOSITY, ConditionOperation.LOWER_THAN, 500)
        ), Map.of("switch", new Action(ActionType.ACTIVATE, null)));

        assertThat(evaluator.matches(scenario, snapshot)).isFalse();
    }

    @Test
    void shouldNotMatchWhenSensorStateIsMissing() {
        Instant timestamp = Instant.parse("2026-08-06T00:00:00Z");
        SensorsSnapshotAvro snapshot = new SensorsSnapshotAvro("hub-1", timestamp, new HashMap<>());
        Scenario scenario = new Scenario("hub-1", "missing", Map.of(
                "sensor", new Condition(ConditionType.SWITCH, ConditionOperation.EQUALS, 1)
        ), Map.of("switch", new Action(ActionType.ACTIVATE, null)));

        assertThat(evaluator.matches(scenario, snapshot)).isFalse();
    }
}
