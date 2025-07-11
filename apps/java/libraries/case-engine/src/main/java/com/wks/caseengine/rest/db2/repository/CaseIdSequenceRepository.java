package com.wks.caseengine.rest.db2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.wks.caseengine.rest.db2.entity.CaseIdSequences;

public interface CaseIdSequenceRepository //extends JpaRepository<CaseIdSequences, Long> 
{

	@Query(value="SELECT * FROM case_id_sequence",nativeQuery = true)
	CaseIdSequences findLastElement();

}
