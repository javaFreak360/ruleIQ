package com.highcourt.ruleIQ.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.highcourt.ruleIQ.entities.RuleDefinition;

@FunctionalInterface
public interface RuleEvaluator {
    boolean evaluate(JsonNode data, RuleDefinition rule);
}
