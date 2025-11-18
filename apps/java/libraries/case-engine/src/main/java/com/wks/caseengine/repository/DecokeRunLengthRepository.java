package com.wks.caseengine.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.DecokeRunLength;

@Repository
public interface DecokeRunLengthRepository extends JpaRepository<DecokeRunLength, UUID>{
	
	@Modifying
    @Transactional
    @Query(
      value = "DELETE FROM DecokeRunLength WHERE Plant_FK_Id = :plantFkId AND AOPYear = :aopYear",
      nativeQuery = true
    )
    int deleteByPlantFkIdAndAopYear(
      @Param("plantFkId") UUID plantFkId,
      @Param("aopYear") String aopYear
    );
	
	@Query(
	        value = "SELECT * FROM DecokeRunLength " +
	                "WHERE Plant_FK_Id = :plantId " +
	                "AND AOPYear = :aopYear " +
	                "AND [Date] = :date",
	        nativeQuery = true
	    )
	    Optional<DecokeRunLength> findUniqueByPlantAOPYearAndDate(
	        @Param("plantId") UUID plantId,
	        @Param("aopYear") String aopYear, // Assuming AOPYear is a String/Varchar like '2025-26'
	        @Param("date") LocalDate date    // Assuming Date is stored as a date type
	    );

}
