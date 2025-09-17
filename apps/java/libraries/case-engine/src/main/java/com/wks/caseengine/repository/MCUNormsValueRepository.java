package com.wks.caseengine.repository;


import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.MCUNormsValue;

@Repository
public interface MCUNormsValueRepository extends JpaRepository<MCUNormsValue,UUID>{


	@Query(value = """
				SELECT 
      [Material_FK_Id]
      ,[ModeType_January]
      ,[ModeType_February]
      ,[ModeType_March]
      ,[ModeType_April]
      ,[ModeType_May]
      ,[ModeType_June]
      ,[ModeType_July]
      ,[ModeType_August]
      ,[ModeType_September]
      ,[ModeType_October]
      ,[ModeType_November]
      ,[ModeType_December]
  FROM [dbo].[CRACKER_NormsMonthwiseModeType]
  where Plant_FK_Id = :plantFKId and AOPYear = :year and ModeOfOperation = :mode
						""", nativeQuery = true)
	List<Object[]> getNormsMonthWiseModeTypeData(@Param("year") String year, @Param("plantFKId") String plantFKId, @Param("mode") String mode);
	
	}
