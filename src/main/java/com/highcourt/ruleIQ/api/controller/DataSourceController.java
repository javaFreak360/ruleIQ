package com.highcourt.ruleIQ.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.highcourt.ruleIQ.api.service.IEventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.WeakHashMap;

@RestController
@RequestMapping("rule/evaluate")
public class DataSourceController {

    @Autowired
    IEventProducer eventProducer;

    @PostMapping("/{entity}")
    public ResponseEntity evaluate(@RequestBody JsonNode jsonArray, @PathVariable String entity) {
        Map<String, String> params = new WeakHashMap<>();
        params.put("entityName", entity);
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
}
