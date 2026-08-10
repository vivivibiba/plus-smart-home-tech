package ru.yandex.practicum.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

import java.time.Instant;

@Component
public class SensorEventMapper {

    public SensorEventAvro toAvro(SensorEventProto event) {
        Object payload = switch (event.getPayloadCase()) {
            case CLIMATE_SENSOR -> new ClimateSensorAvro(
                    event.getClimateSensor().getTemperatureC(),
                    event.getClimateSensor().getHumidity(),
                    event.getClimateSensor().getCo2Level()
            );
            case LIGHT_SENSOR -> new LightSensorAvro(
                    event.getLightSensor().getLinkQuality(),
                    event.getLightSensor().getLuminosity()
            );
            case MOTION_SENSOR -> new MotionSensorAvro(
                    event.getMotionSensor().getLinkQuality(),
                    event.getMotionSensor().getMotion(),
                    event.getMotionSensor().getVoltage()
            );
            case SWITCH_SENSOR -> new SwitchSensorAvro(event.getSwitchSensor().getState());
            case TEMPERATURE_SENSOR -> new TemperatureSensorAvro(
                    event.getTemperatureSensor().getTemperatureC(),
                    event.getTemperatureSensor().getTemperatureF()
            );
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Sensor event payload is not set");
        };

        Instant timestamp = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        return new SensorEventAvro(event.getId(), event.getHubId(), timestamp, payload);
    }
}
