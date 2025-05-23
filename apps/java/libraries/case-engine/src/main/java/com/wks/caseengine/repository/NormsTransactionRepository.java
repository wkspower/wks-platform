package com.wks.caseengine.repository;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.NormsTransactions;

@Repository
public interface NormsTransactionRepository extends JpaRepository<NormsTransactions,UUID>{
	
	 @Query(
		        value = """
		            SELECT
		                AOPMonth,
		                NormParameter_FK_Id,
		                MIN(AttributeValue) AS value
		            FROM
		                NormsTransactions
		            WHERE
		                Plant_FK_Id = :plantFkId
		                AND AOPYear = :aopYear
		            GROUP BY
		                AOPMonth,
		                NormParameter_FK_Id
		            """,
		        nativeQuery = true
		    )
		    List<Object[]> findDistinctTransactionsByMonthAndParameter(
		        @Param("plantFkId") UUID plantFkId,
		        @Param("aopYear") String aopYear
		    );

}
