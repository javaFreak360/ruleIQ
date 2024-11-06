package com.highcourt.ruleIQ.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.highcourt.ruleIQ.api.service.IRuleDefinitionService;
import com.highcourt.ruleIQ.entities.RuleDefinition;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class RuleManagerConsumer {
    private static final Logger logger = LoggerFactory.getLogger(RuleManagerConsumer.class);
    @Autowired
    private IRuleDefinitionService ruleDefinitionService;
    @Value("${kafka.topic.pattern}")
    private String topicPattern;
    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topicPattern = "${kafka.topic.pattern}", groupId = "${spring.kafka.consumer.group-id}", concurrency = "${kafka.listener.concurrency}")
    public void listen(ConsumerRecord<String, String> record) {
        logger.info("Received message from topic: {}", record.topic());
        logger.debug("Key: {}", record.key());
        logger.debug("Value: {}", record.value());
        var entitySource = record.topic().replace(topicPattern, "");
        List<RuleDefinition> ruleDefinitions = ruleDefinitionService.getRulesByDataSource(entitySource);
        try {
            JsonNode jsonNode = objectMapper.readTree(record.value());
            if (jsonNode.isArray()) {
                for(JsonNode item : jsonNode) {
                    applyRules(item, ruleDefinitions, this::evaluateRule);
                }
            } else if (jsonNode.isObject()) {
                applyRules(jsonNode, ruleDefinitions, this::evaluateRule);
            } else {
                logger.error("Received JSON is neither an array nor an object: {}", record.value());
            }
        }
        catch (IOException e) {
            logger.error("Failed to parse JSON", e);
        }
    }

    private void applyRules(JsonNode data, List<RuleDefinition> ruleDefinitions, RuleEvaluator evaluator) {
        ruleDefinitions.stream().filter(rule -> {
            try {
                return evaluator.evaluate(data, rule);
            }
            catch (Exception e) {
                logger.error("Failed to evaluate rule: {}", rule.getId(), e);
                return false;
            }
        }).forEach(o -> logger.info(o.toString()));
    }

    private boolean evaluateRule(JsonNode data, RuleDefinition rule) {
        return rule.getCriteria().stream().allMatch(filter -> {
            JsonNode actualValueNode = getNestedValue(data, filter.key());
            if (actualValueNode == null || actualValueNode.isMissingNode()) {
                return false;
            }
            String actualValue = actualValueNode.asText();
            return switch (filter.operator()) {
                case EQ -> filter.values().size() == 1 && actualValue.equals(filter.values().get(0));
                case IN -> filter.values().contains(actualValue);
                case NEQ -> filter.values().size() == 1 && !actualValue.equals(filter.values().get(0));
                case NIN -> !filter.values().contains(actualValue);
                default -> {
                    logger.warn("Unsupported operator: {}", filter.operator());
                    yield false;
                }
            };
        });
    }

    /**
     * Retrieves the nested value in the JsonNode based on the dot-separated path.
     * E.g., "status.code" will retrieve data["status"]["code"].
     */
    private JsonNode getNestedValue(JsonNode node, String path) {
        String[] keys = path.split("\\.");
        JsonNode currentNode = node;
        for(String key : keys) {
            if (currentNode == null || currentNode.isMissingNode()) {
                return null; // Return null if any intermediate node is missing
            }
            currentNode = currentNode.path(key);
        }
        return currentNode;
    }
}
