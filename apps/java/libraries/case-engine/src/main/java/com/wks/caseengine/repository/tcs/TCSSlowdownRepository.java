package com.wks.caseengine.repository.tcs;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
//import java.util.List;
//import java.util.Optional;

import com.wks.caseengine.entity.tcs.TCSSlowdown;

@Repository
public interface TCSSlowdownRepository extends JpaRepository<TCSSlowdown, UUID> {

    // List<TCSSlowdown> findByParticulates(String particulates);

    // List<TCSSlowdown> findByTentativeMonth(String tentativeMonth);

    // List<TCSSlowdown> findByTentativeDurationInDays(Integer tentativeDurationInDays);

    // Optional<TCSSlowdown> findByParticulatesAndTentativeMonth(String particulates, String tentativeMonth);
}
