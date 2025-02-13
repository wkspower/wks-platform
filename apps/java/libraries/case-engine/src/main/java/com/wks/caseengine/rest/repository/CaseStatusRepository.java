package com.wks.caseengine.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.rest.entity.CaseStatus;

@Repository
public interface CaseStatusRepository extends JpaRepository<CaseStatus, Long> {

}