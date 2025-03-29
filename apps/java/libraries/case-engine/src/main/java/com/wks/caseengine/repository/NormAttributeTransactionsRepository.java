package com.wks.caseengine.repository;
import com.wks.caseengine.entity.NormAttributeTransactions;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface NormAttributeTransactionsRepository extends JpaRepository<NormAttributeTransactions, UUID>{
	
	@Modifying
	@Query("UPDATE NormAttributeTransactions nat SET nat.attributeValue = :attributeValue " +
	       "WHERE nat.aopMonth = :month AND nat.normParameterFKId = :normParameterFKId AND nat.auditYear = :auditYear")
	int updateNormAttributeTransactions(@Param("attributeValue") String attributeValue,  
	                                    @Param("month") Integer month,  
	                                    @Param("normParameterFKId") UUID normParameterFKId,  
	                                    @Param("auditYear") String auditYear);
	
	@Modifying
	@Transactional
	@Query("UPDATE NormAttributeTransactions nat " +
	       "SET nat.attributeValue = :attributeValue, " +  // Updating attributeValue
	       "    nat.remarks = :remarks " +  // Updating remarks
	       "WHERE nat.aopMonth = :month " +
	       "AND nat.auditYear = :auditYear " +
	       "AND nat.normParameterFKId = :normParameterFKId ")
	void updateCatalystData(@Param("attributeValue") String attributeValue, 
	                        @Param("remarks") String remarks,  // Added remarks parameter
	                        @Param("month") Integer month, 
	                        @Param("auditYear") String auditYear, 
	                        @Param("normParameterFKId") UUID normParameterFKId);

	
	@Modifying
	@Transactional
	@Query("DELETE FROM NormAttributeTransactions nat " +
	       "WHERE nat.attributeValue = :attributeValue " +
	       "AND nat.aopMonth = :month " +  // Added missing AND
	       "AND nat.auditYear = :auditYear " +
	       "AND nat.normParameterFKId = :normParameterFKId ")
	void deleteCatalystData(@Param("attributeValue") String attributeValue, 
	                        @Param("month") Integer month, 
	                        @Param("auditYear") String auditYear, 
	                        @Param("normParameterFKId") UUID normParameterFKId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM NormAttributeTransactions nat " +
	       "WHERE nat.attributeValue = :attributeValue " +
	       "AND nat.aopMonth = :month " +  // Added missing AND
	       "AND nat.auditYear = :auditYear " +
	       "AND nat.normParameterFKId = :normParameterFKId")
	void deleteBusinessDemandData(@Param("attributeValue") String attributeValue, 
	                        @Param("month") Integer month, 
	                        @Param("auditYear") String auditYear, 
	                        @Param("normParameterFKId") UUID normParameterFKId);


	@Query(value = """
					SELECT
			 NP.Id AS NormParameter_FK_Id,
			 MAX(CASE WHEN NAT.AOPMonth = '1' THEN NAT.AttributeValue ELSE NULL END) AS Jan,
			 MAX(CASE WHEN NAT.AOPMonth = '2' THEN NAT.AttributeValue ELSE NULL END) AS Feb,
			 MAX(CASE WHEN NAT.AOPMonth = '3' THEN NAT.AttributeValue ELSE NULL END) AS Mar,
			 MAX(CASE WHEN NAT.AOPMonth = '4' THEN NAT.AttributeValue ELSE NULL END) AS Apr,
			 MAX(CASE WHEN NAT.AOPMonth = '5' THEN NAT.AttributeValue ELSE NULL END) AS May,
			 MAX(CASE WHEN NAT.AOPMonth = '6' THEN NAT.AttributeValue ELSE NULL END) AS Jun,
			 MAX(CASE WHEN NAT.AOPMonth = '7' THEN NAT.AttributeValue ELSE NULL END) AS Jul,
			 MAX(CASE WHEN NAT.AOPMonth = '8' THEN NAT.AttributeValue ELSE NULL END) AS Aug,
			 MAX(CASE WHEN NAT.AOPMonth = '9' THEN NAT.AttributeValue ELSE NULL END) AS Sep,
			 MAX(CASE WHEN NAT.AOPMonth = '10' THEN NAT.AttributeValue ELSE NULL END) AS Oct,
			 MAX(CASE WHEN NAT.AOPMonth = '11' THEN NAT.AttributeValue ELSE NULL END) AS Nov,
			 MAX(CASE WHEN NAT.AOPMonth = '12' THEN NAT.AttributeValue ELSE NULL END) AS Dec,
			 MAX(NAT.Remarks) AS Remarks ,
			 MAX(NAT.Id) AS NormAttributeTransaction_Id,
			 MAX(NAT.AuditYear) AS AuditYear,
			 MAX(NP.UOM) AS UOM
			FROM NormParameters NP
			JOIN NormParameterType NPT ON NP.NormParameterType_FK_Id = NPT.Id
			LEFT JOIN NormAttributeTransactions NAT
			 ON NAT.NormParameter_FK_Id = NP.Id
			 AND NAT.AuditYear = :year
			WHERE NPT.Name = 'Configuration'  AND NP.Plant_FK_Id = :plantFKId
			GROUP BY NP.Id
			ORDER BY NP.Id;
			 """, nativeQuery = true)
	List<Object[]> findByYearAndPlantFkId(@Param("year") String year, @Param("plantFKId") UUID plantFKId);

	@Query(value = """
				SELECT * FROM NormAttributeTransactions d WHERE d.NormParameter_FK_Id = :normParameterFKId  AND d.AOPMonth = :month AND d.AuditYear = :auditYear
			""", nativeQuery = true)
	Optional<NormAttributeTransactions> findByNormParameterFKIdAndAOPMonthAndAuditYear(@Param("normParameterFKId") UUID normParameterFKId,
			@Param("month")	Integer month,@Param("auditYear") String auditYear);

}
