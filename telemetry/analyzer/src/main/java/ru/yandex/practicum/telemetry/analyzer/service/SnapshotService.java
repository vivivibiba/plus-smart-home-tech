package ru.yandex.practicum.telemetry.analyzer.service;

import com.google.protobuf.Timestamp;
import jakarta.transaction.Transactional;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.model.Action;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;

import java.time.Instant;
import java.util.Map;

@Service
public class SnapshotService {
    private static final Logger log = LoggerFactory.getLogger(SnapshotService.class);

    private final ScenarioRepository scenarioRepository;
    private final ScenarioEvaluator scenarioEvaluator;
    private final HubRouterControllerBlockingStub hubRouterClient;

    public SnapshotService(ScenarioRepository scenarioRepository,
                           ScenarioEvaluator scenarioEvaluator,
                           @GrpcClient("hub-router") HubRouterControllerBlockingStub hubRouterClient) {
        this.scenarioRepository = scenarioRepository;
        this.scenarioEvaluator = scenarioEvaluator;
        this.hubRouterClient = hubRouterClient;
    }

    @Transactional
    public void handle(SensorsSnapshotAvro snapshot) {
        for (Scenario scenario : scenarioRepository.findByHubId(snapshot.getHubId())) {
            if (!scenarioEvaluator.matches(scenario, snapshot)) {
                continue;
            }

            for (Map.Entry<String, Action> entry : scenario.getActions().entrySet()) {
                DeviceActionRequest request = DeviceActionRequest.newBuilder()
                        .setHubId(snapshot.getHubId())
                        .setScenarioName(scenario.getName())
                        .setAction(toProto(entry.getKey(), entry.getValue()))
                        .setTimestamp(toTimestamp(snapshot.getTimestamp()))
                        .build();

                hubRouterClient.handleDeviceAction(request);
                log.info("Sent action {} for sensor {} in scenario {} of hub {}",
                        entry.getValue().getType(), entry.getKey(), scenario.getName(), snapshot.getHubId());
            }
        }
    }

    private DeviceActionProto toProto(String sensorId, Action action) {
        DeviceActionProto.Builder builder = DeviceActionProto.newBuilder()
                .setSensorId(sensorId)
                .setType(ActionTypeProto.valueOf(action.getType().name()));
        if (action.getValue() != null) {
            builder.setValue(action.getValue());
        }
        return builder.build();
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
