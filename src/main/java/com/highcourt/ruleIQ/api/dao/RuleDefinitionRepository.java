package com.highcourt.ruleIQ.api.dao;

import com.highcourt.ruleIQ.entities.RuleDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleDefinitionRepository extends JpaRepository<RuleDefinition, Long> {

    List<RuleDefinition> findByDataSource(String dataSource);
}
