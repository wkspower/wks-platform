package com.wks.caseengine.tcs.repository.tcsworkflow;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.entity.DummyEntity;
import com.wks.caseengine.tcs.dto.camundadto.PlantSubmissionAuditTrailProjection;

@Repository
@Transactional
public interface TCSAuditTrailRepository extends JpaRepository<DummyEntity, Long> {
    
    @Modifying
    @Transactional
    @Query(
        value = """
            INSERT INTO TCS_Submission_History
            (Plant_Id, PlantName, Site_Id, Vertical_Id, SubmittedBy, SubmissionDate,
             SubmissionRemark, VerifiedDate, VerifiedBy, VerifiedRemark, Status, Type)
            VALUES
            (:plantId, :plantName, :siteId, :verticalId, :submittedBy, :submissionDateTime,
             :submissionRemark, :verifiedDateTime, :verifiedBy, :verifiedRemark,
             :status, :type)
            """,
        nativeQuery = true
    )
    void savePlantSubmissionAuditTrail(
            @Param("plantId") UUID plantId,
            @Param("plantName") String plantName,
            @Param("siteId") UUID siteId,
            @Param("verticalId") UUID verticalId,
            @Param("submittedBy") String submittedBy,
            @Param("submissionDateTime") Date submissionDateTime,
            @Param("submissionRemark") String submissionRemark,
            @Param("verifiedDateTime") Date verifiedDateTime,
            @Param("verifiedBy") String verifiedBy,
            @Param("verifiedRemark") String verifiedRemark,
            @Param("status") String status,
            @Param("type") String type
           
    );
    

    // get the existing audit trail for given plant, site and vertical
    @Query(value = "SELECT Plant_Id, PlantName, Site_Id, Vertical_Id, SubmittedBy, SubmissionDate, SubmissionRemark, VerifiedDate, VerifiedBy, VerifiedRemark, Status, Type FROM TCS_Submission_History WHERE Plant_Id = :plantId AND Site_Id = :siteId AND Vertical_Id = :verticalId AND Type = :type", nativeQuery = true)
    List<PlantSubmissionAuditTrailProjection> getPlantSubmissionAuditTrail(@Param("plantId") UUID plantId, @Param("siteId") UUID siteId, @Param("verticalId") UUID verticalId, @Param("type") String type);


    @Query(value = "SELECT PlantName, Site_Id, Vertical_Id, SubmittedBy, SubmissionDate, SubmissionRemark, VerifiedDate, VerifiedBy, VerifiedRemark, Status, Type FROM TCS_Submission_History WHERE Site_Id = :siteId AND Vertical_Id = :verticalId AND Type = :type", nativeQuery = true)
    List<PlantSubmissionAuditTrailProjection> getEbsSubmissionAuditTrail( @Param("siteId") UUID siteId, @Param("verticalId") UUID verticalId, @Param("type") String type);



    @Query(value = """
        SELECT
            Id,
            Plant_Id,
            PlantName,
            Site_Id,
            Vertical_Id,
            SubmittedBy,
            SubmissionDate,
            SubmissionRemark,
            VerifiedDate,
            VerifiedBy,
            VerifiedRemark,
            Status,
            Type
        FROM (
            SELECT *,
                   ROW_NUMBER() OVER (
                       PARTITION BY PlantName
                       ORDER BY SubmissionDate DESC
                   ) AS rn
            FROM TCS_Submission_History
            WHERE Site_Id = :siteId
              AND Vertical_Id = :verticalId
              AND Type = :type
              AND VerifiedDate IS NULL
              AND PlantName IS NOT NULL
              
        ) t
        WHERE rn = 1
    """, nativeQuery = true)
    List<PlantSubmissionAuditTrailProjection> getLatestPlantWiseSubmissionAuditTrail(
            @Param("siteId") UUID siteId,
            @Param("verticalId") UUID verticalId,
            @Param("type") String type
            
    );



    @Query(value = """
    SELECT TOP 1 
        Id, Plant_Id, PlantName, Site_Id, Vertical_Id,
           SubmittedBy, SubmissionDate, SubmissionRemark,
           VerifiedDate, VerifiedBy, VerifiedRemark,
           Status, Type
    FROM TCS_Submission_History
    WHERE Plant_Id = :plantId
      AND Site_Id = :siteId
      AND Vertical_Id = :verticalId
      AND Type = :type
      AND VerifiedDate IS NULL
    ORDER BY SubmissionDate DESC
    """, nativeQuery = true)
PlantSubmissionAuditTrailProjection getLatestPlantSubmissionAuditTrail(
        @Param("plantId") UUID plantId,
        @Param("siteId") UUID siteId,
        @Param("verticalId") UUID verticalId,
        @Param("type") String type);


        @Query(value = """
            SELECT TOP 1 
                  Id, Site_Id, Vertical_Id,
                   SubmittedBy, SubmissionDate, SubmissionRemark,
                   VerifiedDate, VerifiedBy, VerifiedRemark,
                   Status, Type
            FROM TCS_Submission_History
            WHERE 
             Site_Id = :siteId
              AND Vertical_Id = :verticalId
              AND Type = :type
              AND VerifiedDate IS NULL
            ORDER BY SubmissionDate DESC
            """, nativeQuery = true)
        PlantSubmissionAuditTrailProjection getLatestEbsSubmissionAuditTrail(
                @Param("siteId") UUID siteId,
                @Param("verticalId") UUID verticalId,
                @Param("type") String type);
  
// ebs approval history
    @Query(
        value = "SELECT Plant_Id, PlantName, Site_Id, Vertical_Id, SubmittedBy, SubmissionDate, SubmissionRemark, " +
                "VerifiedDate, VerifiedBy, VerifiedRemark, Status, Type " +
                "FROM TCS_Submission_History " +
                "WHERE Plant_Id = :plantId " +
                "AND Site_Id = :siteId " +
                "AND Vertical_Id = :verticalId " +
                "AND Type = :type " +
                "AND VerifiedDate IS NOT NULL",
        nativeQuery = true
      )
    List<PlantSubmissionAuditTrailProjection> getPlantSubmissionAuditTrailByVerfiedDate(
            @Param("plantId") UUID plantId,
            @Param("siteId") UUID siteId,
            @Param("verticalId") UUID verticalId,
            @Param("type") String type
           
    );

    @Query(
        value = "SELECT Plant_Id, PlantName, Site_Id, Vertical_Id, SubmittedBy, SubmissionDate, SubmissionRemark, " +
                "VerifiedDate, VerifiedBy, VerifiedRemark, Status, Type " +
                "FROM TCS_Submission_History " +
                "WHERE Site_Id = :siteId " +
                "AND Vertical_Id = :verticalId " +
                "AND Type = :type " +
                "AND VerifiedDate IS NOT NULL",
        nativeQuery = true
      )
    List<PlantSubmissionAuditTrailProjection> getEbsSubmissionAuditTrailByVerfiedDate(
            @Param("siteId") UUID siteId,
            @Param("verticalId") UUID verticalId,
            @Param("type") String type
           
    );


    @Modifying
    @Transactional
    @Query(value = "UPDATE TCS_Submission_History SET Status = :status WHERE Plant_Id = :plantId AND Site_Id = :siteId AND Vertical_Id = :verticalId AND Type = :type", nativeQuery = true)
    void updateSubmissionStatus(
            @Param("plantId") UUID plantId,
            @Param("siteId") UUID siteId,
            @Param("verticalId") UUID verticalId,
            @Param("type") String type,
            @Param("status") String status
            
    );

    @Modifying
    @Transactional
    @Query(value = "UPDATE TCS_Submission_History SET Status = :status WHERE Id = :id", nativeQuery = true)
    void updateSubmissionStatusById(
            @Param("id") UUID id,
            @Param("status") String status
            
    );
    


}
