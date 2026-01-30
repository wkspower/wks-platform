package com.wks.caseengine.tcs.dto.camundadto;

import java.util.Date;

public interface PlantSubmissionAuditTrailProjection {
    
    String getPlantId();
    String getPlantName();
    String getSiteId();
    String getVerticalId();
    String getSubmittedBy();
    Date getSubmissionDateTime();
    String getSubmissionRemark();
    Date getVerifiedDateTime();
    String getVerifiedBy();
    String getVerifiedRemark();
    String getTab();
    String getStatus();
    String getType();
}
