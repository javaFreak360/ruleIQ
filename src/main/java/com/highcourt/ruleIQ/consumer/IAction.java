package com.highcourt.ruleIQ.consumer;

import com.fasterxml.jackson.databind.JsonNode;

public interface IAction {
    void perform(JsonNode data);
}
