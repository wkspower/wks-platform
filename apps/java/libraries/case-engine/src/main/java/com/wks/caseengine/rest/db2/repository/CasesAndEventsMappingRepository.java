package com.wks.caseengine.rest.db2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.rest.db2.entity.CasesAndEventsMapping;

@Repository
public interface CasesAndEventsMappingRepository extends JpaRepository<CasesAndEventsMapping, Long>{

}