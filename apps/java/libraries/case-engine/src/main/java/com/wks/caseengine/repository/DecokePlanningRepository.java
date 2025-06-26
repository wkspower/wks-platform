package com.wks.caseengine.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wks.caseengine.entity.DecokePlanning;
import org.springframework.stereotype.Repository;

@Repository
public interface DecokePlanningRepository extends JpaRepository<DecokePlanning, UUID>{

}
