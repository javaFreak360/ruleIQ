package com.highcourt.ruleIQ.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

@Service
public class FileActionConsumer implements IAction{

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(FileActionConsumer.class);

    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    public void runConsumerBatch() {
        var container = kafkaListenerEndpointRegistry.getListenerContainer("scheduled_file_action_consumer");
        if (container != null) {
            logger.info("Starting batch file consumer...");
            container.start();
        }
    }

    @KafkaListener(id = "scheduled_file_action_consumer", topicPattern = "${kafka.action.file.topic.pattern}", groupId = "${spring.kafka.action.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeBatch(List<ConsumerRecord<String, JsonNode>> records) {
        logger.info("Received batch of size: {}", records.size());
        var OUTPUT_CSV_FILE = records.get(0).topic()+new Date()+".csv";
        try (CSVWriter writer = new CSVWriter(new FileWriter(OUTPUT_CSV_FILE, true))) {
            boolean isHeaderWritten = new File(OUTPUT_CSV_FILE).length() > 0;
            for (ConsumerRecord<String, JsonNode> record : records) {
                // Parse the JSON from the record's value
                JsonNode jsonNode = record.value();
                // Write the header row (if not already written)
                if (!isHeaderWritten) {
                    String[] header = extractHeader(jsonNode);
                    writer.writeNext(header);
                    isHeaderWritten = true;
                }
                String[] row = extractRow(jsonNode);
                writer.writeNext(row);
            }

            logger.info("Batch written to CSV file: {}", OUTPUT_CSV_FILE);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Error writing batch to CSV file: {}", e.getMessage());
        }
    }

    private String[] extractRow(JsonNode jsonNode) {
        // Extract values in the order of the header fields
        Iterator<String> fieldNames = jsonNode.fieldNames();
        String[] row = new String[jsonNode.size()];
        int index = 0;

        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            row[index++] = jsonNode.get(fieldName).asText();
        }

        return row;
    }

    private String[] fieldNamesToArray(Iterator<String> fieldNames) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(fieldNames, Spliterator.ORDERED), false)
                .toArray(String[]::new);
    }
    @Override
    public void perform(JsonNode data) {}

    private String[] extractHeader(JsonNode jsonNode) {
        Iterator<String> fieldNames = jsonNode.fieldNames();
        return fieldNamesToArray(fieldNames);
    }
}
