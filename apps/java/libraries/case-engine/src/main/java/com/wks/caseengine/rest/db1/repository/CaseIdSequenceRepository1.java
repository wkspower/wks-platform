package com.wks.caseengine.rest.db1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.wks.caseengine.rest.db1.entity.CaseIdSequences1;

public interface CaseIdSequenceRepository1 extends JpaRepository<CaseIdSequences1, Long> {

	@Query(value="SELECT * FROM case_id_sequence1",nativeQuery = true)
	CaseIdSequences1 findLastElement();

}
