package com.highcourt.ruleIQ.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.highcourt.ruleIQ.api.pojo.FileProducerRequest;
import com.highcourt.ruleIQ.api.service.IEventProducer;
import com.highcourt.ruleIQ.api.service.IFileService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@RestController
@RequestMapping("rule/evaluate")
public class DataSourceController {


    IEventProducer eventProducer;

    IFileService sftpProducer;

    ObjectMapper mapper;

    private static final String TOPIC_NAME = "topicName";

    ExecutorService executor = Executors.newFixedThreadPool(10);

    public DataSourceController(@Qualifier("sftpProducer") IFileService sftpProducer, ObjectMapper mapper, IEventProducer eventProducer){
        this.sftpProducer = sftpProducer;
        this.mapper = mapper;
        this.eventProducer = eventProducer;

    }

    @PostMapping("/{entity}")
    public ResponseEntity evaluate(@RequestBody JsonNode jsonArray, @PathVariable String entity) {
        Map<String, String> params = new WeakHashMap<>();
        params.put(TOPIC_NAME, entity);
        if (jsonArray.isArray()) {
            for (JsonNode jsonNode : jsonArray) {
                //var uid =
                boolean result = eventProducer.sendEvent(jsonNode, params);
                var retry = 0;
                while (!result) {
                    ++retry;
                    if (retry > 3)
                        break;
                }
            }
        } else {
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/files")
    public ResponseEntity evaluateFile(@RequestBody List<FileProducerRequest> requests) {
        Map<String, String> params = new WeakHashMap<>();
        requests.stream()
                .forEach(request -> CompletableFuture.runAsync(() -> {
                    consumeFile.accept(request);
                }));

        return ResponseEntity.ok().build();
    }

    /**
     *
     */
    private Consumer<FileProducerRequest> consumeFile = (request -> {
       if(request != null){
           IFileService fileProducer =  switch (request.getProducerType()){
               default -> sftpProducer;
           };
           var localFilePath = request.getLocalFilePath() != null ? request.getLocalFilePath() : request.getRemoteFilePath();
           fileProducer.download(request.getRemoteFilePath(), localFilePath);
           List<JsonNode> fileDataList = readFile(new File(localFilePath));
           fileDataList.forEach(data -> this.eventProducer.sendEvent(data, Map.of(TOPIC_NAME, request.getEntity())));
       }
    });

    /**
     * Read Json object from File
     * @param file
     * @return
     */
    private List<JsonNode> readFile(File file){
        List<JsonNode> objects = new LinkedList<>();
        JsonNode rootNode = null;
        try {
            rootNode = mapper.readTree(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(rootNode.isArray()){
            rootNode.elements().forEachRemaining(node -> objects.add(node));
        }else{
            objects.add(rootNode);
        }
        return objects;
    }

    /**
     * Read Json object from File
     * @param file
     * @return
     */
    private List<JsonNode> readFileXml(File file){
        List<JsonNode> objects = new LinkedList<>();
        JsonNode rootNode = null;
        try {
            rootNode = mapper.readTree(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(rootNode.isArray()){
            rootNode.elements().forEachRemaining(node -> objects.add(node));
        }else{
            objects.add(rootNode);
        }
        return objects;
    }
}
