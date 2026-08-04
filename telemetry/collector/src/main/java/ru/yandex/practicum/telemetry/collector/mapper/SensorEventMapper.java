package ru.yandex.practicum.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.collector.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.LightSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.TemperatureSensorEvent;

@Component
public class SensorEventMapper {

    public SensorEventAvro toAvro(SensorEvent event) {
        Object payload;
        if (event instanceof ClimateSensorEvent climate) {
            payload = new ClimateSensorAvro(climate.getTemperatureC(), climate.getHumidity(), climate.getCo2Level());
        } else if (event instanceof LightSensorEvent light) {
            payload = new LightSensorAvro(light.getLinkQuality(), light.getLuminosity());
        } else if (event instanceof MotionSensorEvent motion) {
            payload = new MotionSensorAvro(motion.getLinkQuality(), motion.isMotion(), motion.getVoltage());
        } else if (event instanceof SwitchSensorEvent switchEvent) {
            payload = new SwitchSensorAvro(switchEvent.isState());
        } else if (event instanceof TemperatureSensorEvent temperature) {
            payload = new TemperatureSensorAvro(temperature.getTemperatureC(), temperature.getTemperatureF());
        } else {
            throw new IllegalArgumentException("Unsupported sensor event type: " + event.getClass().getName());
        }

        return new SensorEventAvro(event.getId(), event.getHubId(), event.getTimestamp(), payload);
    }
}
