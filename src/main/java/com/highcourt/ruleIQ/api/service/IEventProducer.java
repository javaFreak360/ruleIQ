package com.highcourt.ruleIQ.api.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public interface IEventProducer {
    boolean sendEvent(JsonNode event, Map<String, String> params);
}
