package com.highcourt.ruleIQ.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.highcourt.ruleIQ.api.service.IEventProducer;
import com.highcourt.ruleIQ.api.service.IRuleDefinitionService;
import com.highcourt.ruleIQ.entities.Action;
import com.highcourt.ruleIQ.entities.RuleDefinition;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

@Component
public class RuleManagerConsumer {
    private static final Logger logger = LoggerFactory.getLogger(RuleManagerConsumer.class);
    @Autowired
    private IRuleDefinitionService ruleDefinitionService;
    @Value("${kafka.topic.prefix}")
    private String topicPrefix;
    @Value("${kafka.action.topic.prefix}")
    private String actionTopicPrefix;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FilterEvaluator evaluator;
    @Autowired
    IEventProducer eventProducer;

    @KafkaListener(topicPattern = "${kafka.rule.manager.topic.pattern}", groupId = "${spring.kafka.consumer.group-id}", concurrency = "${kafka.listener.concurrency}")
    public void listen(ConsumerRecord<String, JsonNode> record) {
        logger.info("Received message from topic: {}", record.topic());
        var entitySource = record.topic().replace(topicPrefix, "");
        List<RuleDefinition> ruleDefinitions = ruleDefinitionService.getRulesByDataSource(entitySource);
        try {
            JsonNode jsonNode = record.value();
            if (jsonNode.isArray()) {
                for (JsonNode item : jsonNode) {
                    applyRules(item, ruleDefinitions);
                }
            } else if (jsonNode.isObject()) {
                applyRules(jsonNode, ruleDefinitions);
            } else {
                logger.error("Received JSON is neither an array nor an object: {}", record.value());
            }
        } catch (Exception e) {
            logger.error("Failed to parse JSON", e);
        }
    }

    private void applyRules(JsonNode item, List<RuleDefinition> definitions) {
        definitions.forEach(rule -> {
            var operator = rule.getCriteriaOperator() != null ? rule.getCriteriaOperator() : "AND";
            boolean result = switch (operator){
                case "OR" -> rule.getCriteria().stream().anyMatch(filter -> evaluator.applyFilter(filter, getNestedValue(item, filter.key())));
                default -> rule.getCriteria().stream().allMatch(filter -> evaluator.applyFilter(filter, getNestedValue(item, filter.key())));
            };
            if(result) {
                executeAction(rule, item);
            }

        });
    }


    private void executeAction(RuleDefinition rule, JsonNode data) {
        if (Objects.nonNull(rule)) {
            Action action = rule.getAction();
            logger.info("Executing action {} in rule id {}", action, rule.getId());
            if (action != null) {
                Map<String, String> params = new WeakHashMap<>();
                params.put("topicName", actionTopicPrefix.concat(action.type().name()).concat(".").concat(rule.getDataSource()));
                switch (action.type()) {
                    case FORWARD -> params.put("topicName", action.args().get("target").asText());
                    case FILE -> {
                        params.put("fileName", action.args().get("fileName").asText());
                        params.put("uploadType", action.args().get("type").asText());
                    }
                }
                eventProducer.sendEvent(data, params);
            } else {
                //
            }
        }
    }

    /**
     * Retrieves the nested value in the JsonNode based on the dot-separated path. E.g.,
     * "status.code" will retrieve data["status"]["code"].
     */
    private JsonNode getNestedValue(JsonNode node, String path) {
        String[] keys = path.split("\\.");
        JsonNode currentNode = node;
        for (String key : keys) {
            if (currentNode == null || currentNode.isMissingNode()) {
                return null; // Return null if any intermediate node is missing
            }
            currentNode = currentNode.path(key);
        }
        return currentNode;
    }
}
