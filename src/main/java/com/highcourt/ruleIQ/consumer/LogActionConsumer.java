package com.highcourt.ruleIQ.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class LogActionConsumer implements IAction {
    private static final Logger logger = LoggerFactory.getLogger(LogActionConsumer.class);

    @KafkaListener(topicPattern = "${kafka.action.log.topic.pattern}", groupId = "${spring.kafka.consumer.group-id}", concurrency = "${kafka.listener.concurrency}")
    public void listen(ConsumerRecord<String, JsonNode> record) {
        //logger.debug("Log consumer received record with key {} value {} from topic {}",record.key(), record.value(), record.topic());
        perform(record.value());
    }

    @Override
    public void perform(JsonNode data) {
        logger.info("Log consumer received record with data : {}", data );
    }
}
