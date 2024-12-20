package com.highcourt.ruleIQ.entities;

import java.io.Serializable;
import java.util.List;

public record Filter(String key, List<String> value, FilterOperatorEnum operator, FilterType type) implements Serializable {
}
