package com.wks.caseengine.cpp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.cpp.entity.CPPSRMapping;

@Repository
public interface CPPSRMappingRepository extends JpaRepository<CPPSRMapping, UUID> {

    List<CPPSRMapping> findByAopYearAndPlantFkId(
            String aopYear,
            UUID plantFkId
    );
}
