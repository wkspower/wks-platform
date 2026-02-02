package com.wks.caseengine.tcs.service;

import java.util.List;

import com.wks.caseengine.tcs.dto.camundadto.PlantSubmissionAuditTrailDTO;

public interface TCSWorkFlowService {
    
    void completePlantSubmissionTask(String plantName, String siteId, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear);

    void ebsApproval(String siteId, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear);

    void ebsApproveReject(String plantName, String siteId, boolean approvalStatus, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear);

    void startProcess(String verticalId, String siteId, String finacialYear);

    void CTSApproval(String siteId, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear);

    List<PlantSubmissionAuditTrailDTO> getPlantSubmissionAuditTrail(String plantName, String siteId, String verticalId, String type);

    List<PlantSubmissionAuditTrailDTO> getEBSSubmissionAuditTrail(String siteId, String verticalId, String type);

    List<PlantSubmissionAuditTrailDTO> getLatestPlantWiseSubmissionAuditTrail(String siteId, String verticalId, String type);

    List<PlantSubmissionAuditTrailDTO> getPlantSubmissionAuditTrailByVerfiedDate(String plantId, String siteId, String verticalId, String type);

    List<PlantSubmissionAuditTrailDTO> getEbsSubmissionAuditTrailByVerfiedDate(String siteId, String verticalId, String type);

    PlantSubmissionAuditTrailDTO getLatestEBSSubmissionAuditTrail(String siteId, String verticalId, String type);

    void CtsApproveReject(String siteId, boolean approvalStatus, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear);

    void clusterHeadApproveReject(String siteId, boolean approvalStatus, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear);

}
