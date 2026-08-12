package ru.yandex.practicum.commerce.store.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.data.domain.Sort;

import java.io.IOException;

@JsonComponent
public class SortJsonSerializer extends JsonSerializer<Sort> {

    @Override
    public void serialize(
            Sort value,
            JsonGenerator generator,
            SerializerProvider serializers
    ) throws IOException {
        generator.writeStartArray();

        for (Sort.Order order : value) {
            generator.writeStartObject();
            generator.writeStringField("direction", order.getDirection().name());
            generator.writeStringField("property", order.getProperty());
            generator.writeEndObject();
        }

        generator.writeEndArray();
    }
}
