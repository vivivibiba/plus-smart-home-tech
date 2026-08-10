package ru.yandex.practicum.telemetry.analyzer.processor;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AnalyzerStarter implements ApplicationRunner {
    private final HubEventProcessor hubEventProcessor;
    private final SnapshotProcessor snapshotProcessor;
    private Thread hubEventsThread;
    private Thread snapshotsThread;

    public AnalyzerStarter(HubEventProcessor hubEventProcessor, SnapshotProcessor snapshotProcessor) {
        this.hubEventProcessor = hubEventProcessor;
        this.snapshotProcessor = snapshotProcessor;
    }

    @Override
    public void run(ApplicationArguments args) {
        hubEventsThread = new Thread(hubEventProcessor, "HubEventProcessorThread");
        snapshotsThread = new Thread(snapshotProcessor, "SnapshotProcessorThread");
        hubEventsThread.start();
        snapshotsThread.start();
    }

    @PreDestroy
    public void stop() {
        hubEventProcessor.stop();
        snapshotProcessor.stop();
        join(hubEventsThread);
        join(snapshotsThread);
    }

    private void join(Thread thread) {
        if (thread == null) {
            return;
        }
        try {
            thread.join(5000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
