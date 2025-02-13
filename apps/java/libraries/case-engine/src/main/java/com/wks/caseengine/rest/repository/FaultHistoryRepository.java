package com.wks.caseengine.rest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wks.caseengine.rest.entity.FaultHistory;

public interface FaultHistoryRepository extends JpaRepository<FaultHistory, Long> {

	@Query(value="SELECT * FROM FaultHistory WHERE event_enrichment_pk_id IN (:eventIdsString);",nativeQuery = true)
	List<FaultHistory> getAllFaultHistoryFromEventIds(@Param(value = "eventIdsString") List<Long> eventIdsString);

}
