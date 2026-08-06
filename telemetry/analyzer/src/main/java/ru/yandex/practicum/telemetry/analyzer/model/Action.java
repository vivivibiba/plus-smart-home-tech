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
@Table(name = "actions")
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType type;

    private Integer value;

    protected Action() {
    }

    public Action(ActionType type, Integer value) {
        this.type = Objects.requireNonNull(type, "type");
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public ActionType getType() {
        return type;
    }

    public Integer getValue() {
        return value;
    }
}
