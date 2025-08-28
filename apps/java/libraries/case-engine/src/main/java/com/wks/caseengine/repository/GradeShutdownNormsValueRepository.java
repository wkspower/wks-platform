package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.GradeShutdownNormsValue;
import com.wks.caseengine.entity.ShutdownNormsValue;

@Repository
public interface GradeShutdownNormsValueRepository extends JpaRepository<GradeShutdownNormsValue,UUID>{
	
}
