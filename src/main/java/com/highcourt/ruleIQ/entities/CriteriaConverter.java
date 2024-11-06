package com.highcourt.ruleIQ.entities;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter(autoApply = false)
public class CriteriaConverter implements AttributeConverter<List<Filter>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(List<Filter> criteria) {
        if (criteria == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(criteria);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting Details to JSON", e);
        }
    }

    @Override
    public List<Filter> convertToEntityAttribute(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(s, new TypeReference<List<Filter>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON to Details", e);
        }    }
}
