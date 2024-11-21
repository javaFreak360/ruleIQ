package com.highcourt.ruleIQ.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Action(ActionTypeEnum type, @JsonProperty("args") JsonNode args) implements Serializable {
}
