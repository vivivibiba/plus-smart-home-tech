package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "scenarios", uniqueConstraints = @UniqueConstraint(columnNames = {"hub_id", "name"}))
public class Scenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hub_id", nullable = false)
    private String hubId;

    @Column(nullable = false)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
            name = "scenario_conditions",
            joinColumns = @JoinColumn(name = "scenario_id"),
            inverseJoinColumns = @JoinColumn(name = "condition_id")
    )
    @MapKeyColumn(name = "sensor_id", nullable = false)
    private Map<String, Condition> conditions = new LinkedHashMap<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
            name = "scenario_actions",
            joinColumns = @JoinColumn(name = "scenario_id"),
            inverseJoinColumns = @JoinColumn(name = "action_id")
    )
    @MapKeyColumn(name = "sensor_id", nullable = false)
    private Map<String, Action> actions = new LinkedHashMap<>();

    protected Scenario() {
    }

    public Scenario(String hubId, String name, Map<String, Condition> conditions, Map<String, Action> actions) {
        this.hubId = Objects.requireNonNull(hubId, "hubId");
        this.name = Objects.requireNonNull(name, "name");
        this.conditions.putAll(Objects.requireNonNull(conditions, "conditions"));
        this.actions.putAll(Objects.requireNonNull(actions, "actions"));
    }

    public Long getId() {
        return id;
    }

    public String getHubId() {
        return hubId;
    }

    public String getName() {
        return name;
    }

    public Map<String, Condition> getConditions() {
        return conditions;
    }

    public Map<String, Action> getActions() {
        return actions;
    }
}
