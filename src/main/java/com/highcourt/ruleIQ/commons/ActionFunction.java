package com.highcourt.ruleIQ.commons;

import com.fasterxml.jackson.databind.JsonNode;
import com.highcourt.ruleIQ.entities.Action;
import com.highcourt.ruleIQ.entities.ActionTypeEnum;

import java.util.List;

@FunctionalInterface
public interface ActionFunction {
    void apply(ActionTypeEnum actionType, JsonNode data);
}
