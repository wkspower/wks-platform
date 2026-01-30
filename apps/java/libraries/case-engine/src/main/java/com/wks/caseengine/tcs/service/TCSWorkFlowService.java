package com.wks.caseengine.tcs.service;

import java.util.List;

import com.wks.caseengine.tcs.dto.camundadto.PlantSubmissionAuditTrailDTO;

public interface TCSWorkFlowService {
    
    void completePlantSubmissionTask(String plantName, String siteId, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear);

    void ebsApproval(String plantName, String siteId, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear);

    void ebsApproveReject(String plantName, String siteId, boolean approvalStatus, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear);

    void startProcess(String verticalId, String siteId, String finacialYear);

    void CTSApproval(String plantName, String siteId, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear);

    List<PlantSubmissionAuditTrailDTO> getSubmissionAuditTrail(String plantName, String siteId, String verticalId, String type);

    List<PlantSubmissionAuditTrailDTO> getEBSSubmissionAuditTrail(String plantName, String siteId, String verticalId, String type);

    List<PlantSubmissionAuditTrailDTO> getLatestPlantSubmissionAuditTrail(String siteId, String verticalId, String type);

    List<PlantSubmissionAuditTrailDTO> getPlantSubmissionAuditTrailByTab(String plantId, String siteId, String verticalId, String type, String tab);

}
