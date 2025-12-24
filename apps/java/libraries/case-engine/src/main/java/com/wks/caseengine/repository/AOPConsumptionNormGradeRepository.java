package com.wks.caseengine.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.AOPConsumptionNormGrade;

@Repository
public interface AOPConsumptionNormGradeRepository extends JpaRepository<AOPConsumptionNormGrade,UUID>{
}
