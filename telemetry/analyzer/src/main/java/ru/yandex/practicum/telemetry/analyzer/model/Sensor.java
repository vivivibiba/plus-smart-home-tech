package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "sensors")
public class Sensor {
    @Id
    private String id;

    @Column(name = "hub_id", nullable = false)
    private String hubId;

    protected Sensor() {
    }

    public Sensor(String id, String hubId) {
        this.id = Objects.requireNonNull(id, "id");
        this.hubId = Objects.requireNonNull(hubId, "hubId");
    }

    public String getId() {
        return id;
    }

    public String getHubId() {
        return hubId;
    }

    public void setHubId(String hubId) {
        this.hubId = Objects.requireNonNull(hubId, "hubId");
    }
}
