package com.highcourt.ruleIQ.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ActionConverter implements AttributeConverter<Action, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(Action action) {
        if (action == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(action);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting Details to JSON", e);
        }
    }

    @Override
    public Action convertToEntityAttribute(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Action.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON to Details", e);
        }    }
}
