package com.highcourt.ruleIQ.commons;

import com.highcourt.ruleIQ.entities.FilterOperatorEnum;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

public class FilterFunctions {
    private static final String DATE_FORMAT = "MM/dd/yyyy";

    public static final Map<FilterOperatorEnum, FilterFunction<String>> STRING_OPERATORS = createStringOperators();

    public static final Map<FilterOperatorEnum, FilterFunction<Number>> NUMBER_OPERATORS = createNumberOperators();

    public static final Map<FilterOperatorEnum, FilterFunction<Date>> DATE_OPERATORS = createDateOperators();

    private static Date parseDate(String dateStr) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format (expected " + DATE_FORMAT + "): " + dateStr);
        }
    }

    private static Double parseNumber(String numberStr) {
        try {
            return Double.parseDouble(numberStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + numberStr);
        }
    }

    private static Map<FilterOperatorEnum, FilterFunction<String>> createStringOperators() {
        Map<FilterOperatorEnum, FilterFunction<String>> map = new EnumMap<>(FilterOperatorEnum.class);
        map.put(FilterOperatorEnum.EQ, (fieldValue, values) -> fieldValue.equals(values.get(0)));
        map.put(FilterOperatorEnum.NEQ, (fieldValue, values) -> !fieldValue.equals(values.get(0)));
        map.put(FilterOperatorEnum.IN, (fieldValue, values) -> values.contains(fieldValue));
        map.put(FilterOperatorEnum.NIN, (fieldValue, values) -> !values.contains(fieldValue));
        return map;
    }

    private static Map<FilterOperatorEnum, FilterFunction<Number>> createNumberOperators() {
        Map<FilterOperatorEnum, FilterFunction<Number>> map = new EnumMap<>(FilterOperatorEnum.class);
        map.put(FilterOperatorEnum.EQ, (fieldValue, values) -> fieldValue.doubleValue() == parseNumber(values.get(0)));
        map.put(FilterOperatorEnum.NEQ, (fieldValue, values) -> fieldValue.doubleValue() != parseNumber(values.get(0)));
        map.put(FilterOperatorEnum.GT, (fieldValue, values) -> fieldValue.doubleValue() > parseNumber(values.get(0)));
        map.put(FilterOperatorEnum.LT, (fieldValue, values) -> fieldValue.doubleValue() < parseNumber(values.get(0)));
        map.put(FilterOperatorEnum.GTE, (fieldValue, values) -> fieldValue.doubleValue() >= parseNumber(values.get(0)));
        map.put(FilterOperatorEnum.LTE, (fieldValue, values) -> fieldValue.doubleValue() <= parseNumber(values.get(0)));
        map.put(FilterOperatorEnum.IN, (fieldValue, values) -> values.stream()
                .map(FilterFunctions::parseNumber)
                .anyMatch(v -> fieldValue.doubleValue() == v));
        map.put(FilterOperatorEnum.NIN, (fieldValue, values) -> values.stream()
                .map(FilterFunctions::parseNumber)
                .noneMatch(v -> fieldValue.doubleValue() == v));

        return map;
    }

    private static Map<FilterOperatorEnum, FilterFunction<Date>> createDateOperators() {
        Map<FilterOperatorEnum, FilterFunction<Date>> map = new EnumMap<>(FilterOperatorEnum.class);
        map.put(FilterOperatorEnum.EQ, (fieldValue, values) -> fieldValue.equals(parseDate(values.get(0))));
        map.put(FilterOperatorEnum.NEQ, (fieldValue, values) -> !fieldValue.equals(parseDate(values.get(0))));
        map.put(FilterOperatorEnum.GT, (fieldValue, values) -> fieldValue.after(parseDate(values.get(0))));
        map.put(FilterOperatorEnum.LT, (fieldValue, values) -> fieldValue.before(parseDate(values.get(0))));
        map.put(FilterOperatorEnum.GTE, (fieldValue, values) -> !fieldValue.before(parseDate(values.get(0))));
        map.put(FilterOperatorEnum.LTE, (fieldValue, values) -> !fieldValue.after(parseDate(values.get(0))));
        map.put(FilterOperatorEnum.IN, (fieldValue, values) -> values.stream()
                .map(FilterFunctions::parseDate)
                .anyMatch(fieldValue::equals));
        map.put(FilterOperatorEnum.NIN, (fieldValue, values) -> values.stream()
                .map(FilterFunctions::parseDate)
                .noneMatch(fieldValue::equals));
        return map;
    }
}
