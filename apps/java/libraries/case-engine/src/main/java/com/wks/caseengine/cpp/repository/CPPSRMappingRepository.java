package com.wks.caseengine.cpp.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.DummyEntity;

import cpp.entity.CPPSRMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CPPSRMappingRepository extends JpaRepository<CPPSRMapping, UUID> {

    List<CPPSRMapping> findByAopYearAndPlantFkId(
            String aopYear,
            UUID plantFkId
    );
}
