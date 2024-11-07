package com.highcourt.ruleIQ.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaEventProducer implements IEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.prefix}")
    String topicPrefix;

    @Autowired
    public KafkaEventProducer(KafkaTemplate<String, Object> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public boolean sendEvent(JsonNode event, Map<String, String> params) {
        if(event == null)
            return false;
        if(params == null || params.keySet().size() == 0)
            return false;
        var topic = topicPrefix + params.get("entityName");
        String key = null;
        if(params.containsKey("uid")){
            key = params.get("entityName") + ":"+  params.get("uid");
        }
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, event);
        var result = this.kafkaTemplate.send(record);
        return result != null;
    }
}
