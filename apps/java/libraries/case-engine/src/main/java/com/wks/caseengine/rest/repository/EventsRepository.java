package com.wks.caseengine.rest.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.rest.entity.Events;

@Repository
public interface EventsRepository extends JpaRepository<Events, Long> {

	@Query(value="SELECT * FROM Events WHERE event_pk_id = :eventId",nativeQuery = true)
	Events findByEventId(@Param(value = "eventId") UUID eventId);

}
