package com.wks.caseengine.tcs.service;

import java.util.List;

import com.wks.caseengine.tcs.dto.camundadto.PlantSubmissionAuditTrailDTO;

public interface TCSWorkFlowService {
    
    void completePlantSubmissionTask(String plantName, String siteId, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear);

    void ebsApproval(String siteId, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear);

    void ebsApproveReject(String plantName, String siteId, boolean approvalStatus, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear);

    void startProcess(String verticalId, String siteId, String finacialYear);

    void ctsApproval(String siteId, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear);

    void clusterHeadApproval(String siteId, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear);

    List<PlantSubmissionAuditTrailDTO> getPlantSubmissionAuditTrail(String plantName, String siteId, String verticalId, String type, String finacialYear);

    List<PlantSubmissionAuditTrailDTO> getEBSSubmissionAuditTrail(String siteId, String verticalId, String type, String finacialYear);

    List<PlantSubmissionAuditTrailDTO> getLatestPlantWiseSubmissionAuditTrail(String siteId, String verticalId, String type, String finacialYear);

    List<PlantSubmissionAuditTrailDTO> getPlantSubmissionAuditTrailByVerfiedDate(String plantId, String siteId, String verticalId, String type, String finacialYear);

    List<PlantSubmissionAuditTrailDTO> getEbsSubmissionAuditTrailByVerfiedDate(String siteId, String verticalId, String type, String finacialYear);

    PlantSubmissionAuditTrailDTO getLatestEBSSubmissionAuditTrail(String siteId, String verticalId, String type, String finacialYear);

    void ctsApproveReject(String siteId, boolean approvalStatus, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear);

    void clusterHeadApproveReject(String siteId, boolean approvalStatus, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear);

}
