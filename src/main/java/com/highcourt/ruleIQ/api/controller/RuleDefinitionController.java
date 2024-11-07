package com.highcourt.ruleIQ.api.controller;

import com.highcourt.ruleIQ.api.service.IRuleDefinitionService;
import com.highcourt.ruleIQ.entities.RuleDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rule")
public class RuleDefinitionController {
    @Autowired
    private IRuleDefinitionService ruleDefinitionService;

    // Create a new rule
    @PostMapping
    public RuleDefinition createRule(@RequestBody RuleDefinition ruleDefinition) {
        return ruleDefinitionService.saveRule(ruleDefinition);
    }

    // Get all rules
    @GetMapping
    public List<RuleDefinition> getAllRules() {
        return ruleDefinitionService.getAllRules();
    }

    // Get a rule by ID
    @GetMapping("/{id}")
    public RuleDefinition getRuleById(@PathVariable Long id) {
        return ruleDefinitionService.getRuleById(id);
    }

    // Update a rule
    @PutMapping("/{id}")
    public RuleDefinition updateRule(@PathVariable Long id, @RequestBody RuleDefinition ruleDefinition) {
        ruleDefinition.setId(id);
        return ruleDefinitionService.updateRule(ruleDefinition);
    }

    // Delete a rule
    @DeleteMapping("/{id}")
    public void deleteRule(@PathVariable Long id) {
        ruleDefinitionService.deleteRule(id);
    }

    @GetMapping("/entity-source/{source}")
    public List<RuleDefinition> getRulesByEntitySource(@PathVariable("source") String entitySource) {
        return ruleDefinitionService.getRulesByDataSource(entitySource);
    }
}
