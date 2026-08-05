package ru.yandex.practicum.telemetry.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.service.EventCollectorService;

@GrpcService
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final EventCollectorService collectorService;

    public EventController(EventCollectorService collectorService) {
        this.collectorService = collectorService;
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        collect(() -> collectorService.collect(request), responseObserver);
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        collect(() -> collectorService.collect(request), responseObserver);
    }

    private void collect(Runnable action, StreamObserver<Empty> responseObserver) {
        try {
            action.run();
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception exception) {
            responseObserver.onError(Status.fromThrowable(exception).asRuntimeException());
        }
    }
}
