package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "conditions")
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionOperation operation;

    @Column(nullable = false)
    private Integer value;

    protected Condition() {
    }

    public Condition(ConditionType type, ConditionOperation operation, Integer value) {
        this.type = Objects.requireNonNull(type, "type");
        this.operation = Objects.requireNonNull(operation, "operation");
        this.value = Objects.requireNonNull(value, "value");
    }

    public Long getId() {
        return id;
    }

    public ConditionType getType() {
        return type;
    }

    public ConditionOperation getOperation() {
        return operation;
    }

    public Integer getValue() {
        return value;
    }
}
