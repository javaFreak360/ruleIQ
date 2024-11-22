package com.highcourt.ruleIQ.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.highcourt.ruleIQ.commons.FilterFunction;
import com.highcourt.ruleIQ.commons.FilterFunctions;
import com.highcourt.ruleIQ.entities.Filter;
import com.highcourt.ruleIQ.entities.FilterOperatorEnum;
import com.highcourt.ruleIQ.entities.FilterType;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@Component
public class FilterEvaluator {
    public boolean applyFilter(Filter filter, Object fieldValue) {
        FilterType type = filter.type() != null ? FilterType.valueOf(filter.type().name()) : FilterType.STRING;
        FilterOperatorEnum operator = filter.operator();
        switch (type) {
            case NUMBER:
                return applyNumberFilter(operator, extractNumberValue(fieldValue), filter.value());
            case DATE:
                return applyDateFilter(operator, extractDateValue(fieldValue), filter.value());
            default:
                return applyStringFilter(operator, extractStringValue(fieldValue), filter.value());
        }
    }

    private boolean applyStringFilter(FilterOperatorEnum operator, String fieldValue, List<String> filterValues) {
        FilterFunction<String> function = FilterFunctions.STRING_OPERATORS.get(operator);
        if (function == null) {
            throw new UnsupportedOperationException("Operator " + operator + " not supported for String type");
        }
        return function.apply(fieldValue, filterValues);
    }

    private boolean applyNumberFilter(FilterOperatorEnum operator, Number fieldValue, List<String> filterValues) {
        FilterFunction<Number> function = FilterFunctions.NUMBER_OPERATORS.get(operator);
        if (function == null) {
            throw new UnsupportedOperationException("Operator " + operator + " not supported for Number type");
        }
        return function.apply(fieldValue, filterValues);
    }

    private boolean applyDateFilter(FilterOperatorEnum operator, Date fieldValue, List<String> filterValues) {
        FilterFunction<Date> function = FilterFunctions.DATE_OPERATORS.get(operator);
        if (function == null) {
            throw new UnsupportedOperationException("Operator " + operator + " not supported for Date type");
        }
        return function.apply(fieldValue, filterValues);
    }

    private Number extractNumberValue(Object fieldValue) {
        if (fieldValue instanceof JsonNode node) {
            if (node.isNumber()) {
                return node.numberValue();
            } else {
                throw new IllegalArgumentException("Expected a numeric value, but got: " + node);
            }
        } else if (fieldValue instanceof Number number) {
            return number;
        } else {
            throw new IllegalArgumentException("Cannot extract number from fieldValue of type: " + fieldValue.getClass());
        }
    }

    private Date extractDateValue(Object fieldValue) {
        if (fieldValue instanceof JsonNode node) {
            if (node.isTextual()) {
                String dateStr = node.asText();
                return parseDate(dateStr);
            } else {
                throw new IllegalArgumentException("Expected a date string, but got: " + node);
            }
        } else if (fieldValue instanceof Date date) {
            return date;
        } else if (fieldValue instanceof String dateStr) {
            return parseDate(dateStr);
        } else {
            throw new IllegalArgumentException("Cannot extract date from fieldValue of type: " + fieldValue.getClass());
        }
    }

    private String extractStringValue(Object fieldValue) {
        if (fieldValue instanceof JsonNode node) {
            return node.asText();
        } else if (fieldValue != null) {
            return fieldValue.toString();
        } else {
            return null;
        }
    }

    private Date parseDate(String dateStr) {
        try {
            return new SimpleDateFormat("MM/dd/yyyy").parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr);
        }
    }
}
