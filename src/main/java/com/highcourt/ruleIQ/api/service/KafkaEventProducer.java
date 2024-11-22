package com.highcourt.ruleIQ.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class KafkaEventProducer implements IEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final Set<String> topicNames= new HashSet<>();

    @Value("${kafka.topic.prefix}")
    String topicPrefix;

    private KafkaAdmin kafkaAdmin;

    @Autowired
    KafkaListenerEndpointRegistry registry;

    @Autowired
    public KafkaEventProducer(KafkaTemplate<String, Object> kafkaTemplate, KafkaAdmin kafkaAdmin){
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaAdmin = kafkaAdmin;
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            topicNames.addAll(adminClient.listTopics().names().get(10, TimeUnit.SECONDS));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean sendEvent(JsonNode event, Map<String, String> params) {
        if(event == null)
            return false;
        if(params == null || params.keySet().size() == 0)
            return false;
        var topic = topicPrefix.concat(params.getOrDefault("topicName",""));
        createTopicIfNotExists(topic);
        String key = null;
        if(params.containsKey("uid")){
            key = params.get("entityName") + ":"+  params.get("uid");
        }
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, event);
        var result = this.kafkaTemplate.send(record);
        return result != null;
    }

    /**
     *
     * @param topic
     */
    private void createTopicIfNotExists(String topic){
        if(topicNames.contains(topic))
            return;
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            Set<String> topics = adminClient.listTopics().names().get(10, TimeUnit.SECONDS);
            if(topicNames.size() != topics.size()){
                topicNames.addAll(topics);
            }
            if(!topicNames.contains(topic)){
                topicNames.add(topic);
                NewTopic newTopic = new NewTopic(topic, 1, Short.valueOf("1"));
                adminClient.createTopics(List.of(newTopic));
                registry.getListenerContainers().forEach(MessageListenerContainer::stop);
                registry.getListenerContainers().forEach(container -> {
                    while (container.isRunning()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                });
                registry.getListenerContainers().forEach(container -> {
                    if (!container.isRunning()) {
                        System.out.println("Starting listener: " + container.getContainerProperties().getGroupId());
                        container.start();
                    }
                });
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
