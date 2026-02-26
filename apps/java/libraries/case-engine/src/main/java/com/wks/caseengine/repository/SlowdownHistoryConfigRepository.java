package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.wks.caseengine.entity.SlowdownHistoryConfig;

@Repository
public interface SlowdownHistoryConfigRepository extends JpaRepository<SlowdownHistoryConfig, UUID> {

    @Query(value = "SELECT * FROM Elastomer_SlowdownHistoryConfig WHERE AuditYear = :auditYear AND Plant_FK_Id = :plantId", nativeQuery = true)
    List<SlowdownHistoryConfig> findByAuditYear(@Param("auditYear") String auditYear,
                                                @Param("plantId") UUID plantId);
}
