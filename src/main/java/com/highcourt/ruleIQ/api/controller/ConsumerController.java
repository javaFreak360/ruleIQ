package com.highcourt.ruleIQ.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.highcourt.ruleIQ.api.pojo.ConsumerFileRequest;
import com.highcourt.ruleIQ.consumer.LogActionConsumer;
import com.opencsv.CSVWriter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("consume/")
public class ConsumerController {


    private final ConsumerFactory<String, JsonNode> consumerFactory;
    private ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(ConsumerController.class);
    @Autowired
    public ConsumerController(ConsumerFactory<String, JsonNode> consumerFactory, ObjectMapper objectMapper) {
        this.consumerFactory = consumerFactory;
        this.objectMapper=objectMapper;
    }

    @PostMapping("/file")
    public ResponseEntity evaluate(@RequestBody ConsumerFileRequest fileRequest) {
        var topicName = fileRequest.getTopicName();
        var fileName = fileRequest.getFileName();

        if (topicName != null && fileName != null) {

            consumeAllRecords(topicName, fileName);
        }
        return ResponseEntity.accepted().build();
    }

    public void consumeAllRecords(String topic, String outputCsvFile) {
        try (KafkaConsumer<String, JsonNode> consumer = (KafkaConsumer<String, JsonNode>) consumerFactory.createConsumer()) {
            logger.info("Polling from topic {}", topic);
            // Subscribe to the topic
            consumer.subscribe(Collections.singletonList(topic));

            // Poll to get assigned partitions
            consumer.poll(Duration.ofMillis(100));
            Set<TopicPartition> partitions = consumer.assignment();

            // Fetch offsets for all partitions
            Map<TopicPartition, Long> beginningOffsets = consumer.beginningOffsets(partitions);
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);

            // Seek to the beginning of each partition
            for (TopicPartition partition : partitions) {
                consumer.seek(partition, beginningOffsets.get(partition));
            }

            // Data structures for headers and rows
            List<String[]> recordsList = new ArrayList<>();
            String[] csvHeaders = null;

            // Process records until all are consumed
            boolean continueConsuming = true;
            while (continueConsuming) {
                var records = consumer.poll(Duration.ofMillis(1000));
                if (records.isEmpty()) {
                    continueConsuming = false; // Stop if no more records
                }

                for (ConsumerRecord<String, JsonNode> record : records) {
                    JsonNode jsonNode = record.value();
                    // Dynamically determine headers from the first record
                    if (csvHeaders == null) {
                        csvHeaders = extractHeaders(jsonNode);
                    }

                    // Extract values for this record
                    String[] csvRow = extractValues(jsonNode, csvHeaders);
                    recordsList.add(csvRow);
                }

                // Check if all partitions have reached their end offsets
                continueConsuming = records.partitions().stream()
                        .anyMatch(partition -> consumer.position(partition) < (endOffsets.get(partition) != null ? endOffsets.get(partition) : 0));
            }

            // Write the collected records to a CSV file
            writeToCsv(outputCsvFile, csvHeaders, recordsList);

            logger.info("All records up to the current point have been consumed and written to " + outputCsvFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] extractHeaders(JsonNode jsonNode) {
        Iterator<String> fieldNames = jsonNode.fieldNames();
        List<String> headers = new ArrayList<>();
        while (fieldNames.hasNext()) {
            headers.add(fieldNames.next());
        }
        return headers.toArray(new String[0]);
    }

    private String[] extractValues(JsonNode jsonNode, String[] headers) {
        String[] values = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            values[i] = jsonNode.has(headers[i]) ? jsonNode.get(headers[i]).asText() : ""; // Default to empty if missing
        }
        return values;
    }

    private void writeToCsv(String outputCsvFile, String[] headers, List<String[]> recordsList) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(outputCsvFile))) {
            // Write headers
            writer.writeNext(headers);

            // Write all rows
            writer.writeAll(recordsList);
        }
    }
}
