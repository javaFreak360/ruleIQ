package com.highcourt.ruleIQ.api.service;

import com.highcourt.ruleIQ.entities.RuleDefinition;

import java.util.List;

public interface IRuleDefinitionService {
    RuleDefinition saveRule(RuleDefinition ruleDefinition);
    RuleDefinition getRuleById(Long id);
    List<RuleDefinition> getAllRules();
    RuleDefinition updateRule(RuleDefinition ruleDefinition);
    void deleteRule(Long id);
    List<RuleDefinition> getRulesByDataSource(String dataSource);
}
