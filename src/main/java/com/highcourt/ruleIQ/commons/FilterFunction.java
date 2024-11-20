package com.highcourt.ruleIQ.commons;

import java.util.List;

@FunctionalInterface
public interface FilterFunction<T>{
    boolean apply(T fieldValue, List<String> filterValues);
}
