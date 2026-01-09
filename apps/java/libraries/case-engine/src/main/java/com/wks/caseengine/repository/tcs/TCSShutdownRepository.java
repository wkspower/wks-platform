package com.wks.caseengine.repository.tcs;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
//import java.util.List;
//import java.util.Optional;

import com.wks.caseengine.entity.tcs.TCSShutdown;

@Repository
public interface TCSShutdownRepository extends JpaRepository<TCSShutdown, UUID> {

    // List<TCSShutdown> findByParticulars(String particulars);

    // List<TCSShutdown> findByTentativeMonth(String tentativeMonth);

    // List<TCSShutdown> findBySdTotalDurationInDays(Integer sdTotalDurationInDays);

    // Optional<TCSShutdown> findByParticularsAndTentativeMonth(String particulars, String tentativeMonth);
}
