package com.wks.caseengine.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.STGExtractionLookup;

@Repository
public interface STGExtractionLookupRepository extends JpaRepository<STGExtractionLookup, UUID> {

    // Find all records ordered by LoadMW
    List<STGExtractionLookup> findAllByOrderByLoadMWAsc();

    // Find exact match by LoadMW
    Optional<STGExtractionLookup> findByLoadMW(BigDecimal loadMW);

    // Find the closest lower LoadMW for interpolation
    @Query("SELECT s FROM STGExtractionLookup s WHERE s.loadMW <= :loadMW ORDER BY s.loadMW DESC LIMIT 1")
    Optional<STGExtractionLookup> findClosestLowerLoad(@Param("loadMW") BigDecimal loadMW);

    // Find the closest higher LoadMW for interpolation
    @Query("SELECT s FROM STGExtractionLookup s WHERE s.loadMW >= :loadMW ORDER BY s.loadMW ASC LIMIT 1")
    Optional<STGExtractionLookup> findClosestHigherLoad(@Param("loadMW") BigDecimal loadMW);

}
