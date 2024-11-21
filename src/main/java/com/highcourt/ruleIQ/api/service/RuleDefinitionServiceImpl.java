package com.highcourt.ruleIQ.api.service;

import com.highcourt.ruleIQ.api.dao.RuleDefinitionRepository;
import com.highcourt.ruleIQ.entities.RuleDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
public class RuleDefinitionServiceImpl implements IRuleDefinitionService {
    @Autowired
    private RuleDefinitionRepository ruleDefinitionRepository;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public RuleDefinition saveRule(RuleDefinition ruleDefinition) {
        var rule = ruleDefinitionRepository.save(ruleDefinition);
        String dataSource = rule.getDataSource();
        List<RuleDefinition> cachedRules = cacheManager.getCache("rulesByDataSource").get(dataSource, List.class);
        if (cachedRules != null) {
            cachedRules.add(rule);
            cacheManager.getCache("rulesByDataSource").put(dataSource, cachedRules);
        }
        return rule;
    }

    @Cacheable(value = "ruleDefinitions", key = "#id")
    @Override
    public RuleDefinition getRuleById(Long id) {
        return ruleDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found with id " + id));
    }

    @Override
    public List<RuleDefinition> getAllRules() {
        return ruleDefinitionRepository.findAll();
    }

    @Caching(evict = {
            @CacheEvict(value = "rules", key = "#ruleDefinition.id"),
            @CacheEvict(value = "rulesByDataSource", key = "#ruleDefinition.dataSource")
    },
            put = {
                    @CachePut(value = "rules", key = "#ruleDefinition.id"),
                    @CachePut(value = "rulesByDataSource", key = "#ruleDefinition.dataSource")
            })
    @Override
    public RuleDefinition updateRule(RuleDefinition ruleDefinition) {
        var ruleDef = ruleDefinitionRepository.findById(ruleDefinition.getId());
        if(ruleDef == null)
            throw new UnsupportedOperationException();
        // Optionally add checks to ensure the rule exists
        return ruleDefinitionRepository.save(ruleDefinition);
    }

    @Caching(evict = {
            @CacheEvict(value = "rules", key = "#id"),
            @CacheEvict(value = "rulesByDataSource", allEntries = true)
    })
    @Override
    public void deleteRule(Long id) {
        ruleDefinitionRepository.deleteById(id);
    }

    @Cacheable(value = "rulesByDataSource", key = "#dataSource")
    @Override
    public List<RuleDefinition> getRulesByDataSource(String dataSource) {
        return ruleDefinitionRepository.findByDataSource(dataSource);
    }


}
