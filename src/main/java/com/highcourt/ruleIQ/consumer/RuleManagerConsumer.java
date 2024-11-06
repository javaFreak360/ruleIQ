package com.highcourt.ruleIQ.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RuleManagerConsumer {
    private static final Logger logger = LoggerFactory.getLogger(RuleManagerConsumer.class);

    private int concurrency;
    @KafkaListener(
            topicPattern = "${kafka.topic.pattern}",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "${kafka.listener.concurrency}"
    )
    public void listen(ConsumerRecord<String, String> record) {
        logger.info("Received message from topic: {}", record.topic());
        logger.debug("Key: {}", record.key());
        logger.debug("Value: {}", record.value());
        record.topic().replace("${kafka.topic.pattern}", "");
        // Processing logic
    }

}
