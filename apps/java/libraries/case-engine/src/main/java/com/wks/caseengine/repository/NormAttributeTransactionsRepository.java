package com.wks.caseengine.repository;
import com.wks.caseengine.entity.NormAttributeTransactions;
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
	       "WHERE nat.month = :month AND nat.normParameterFKId = :normParameterFKId AND nat.auditYear = :auditYear")
	int updateNormAttributeTransactions(@Param("attributeValue") String attributeValue,  
	                                    @Param("month") Integer month,  
	                                    @Param("normParameterFKId") UUID normParameterFKId,  
	                                    @Param("auditYear") Integer auditYear);
	
	@Modifying
    @Transactional
    @Query("UPDATE NormAttributeTransactions nat " +
           "SET nat.attributeValue = :attributeValue " +
           "WHERE nat.month = :month " +
           "AND nat.auditYear = :auditYear " +
           "AND nat.normParameterFKId = :normParameterFKId " +
           "AND nat.catalystAttributeFKId = :catalystAttributeFKId")
    void updateCatalystData(@Param("attributeValue") String attributeValue, 
                            @Param("month") Integer month, 
                            @Param("auditYear") Integer auditYear, 
                            @Param("normParameterFKId") UUID normParameterFKId, 
                            @Param("catalystAttributeFKId") UUID catalystAttributeFKId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM NormAttributeTransactions nat " +
	       "WHERE nat.attributeValue = :attributeValue " +
	       "AND nat.month = :month " +  // Added missing AND
	       "AND nat.auditYear = :auditYear " +
	       "AND nat.normParameterFKId = :normParameterFKId " +
	       "AND nat.catalystAttributeFKId = :catalystAttributeFKId")
	void deleteCatalystData(@Param("attributeValue") String attributeValue, 
	                        @Param("month") Integer month, 
	                        @Param("auditYear") Integer auditYear, 
	                        @Param("normParameterFKId") UUID normParameterFKId, 
	                        @Param("catalystAttributeFKId") UUID catalystAttributeFKId);

}
