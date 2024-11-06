package com.highcourt.ruleIQ.entities;

import java.io.Serializable;
import java.util.List;

public record Filter(String key, List<String> values, FilterOperationEnum operator) implements Serializable {
}
