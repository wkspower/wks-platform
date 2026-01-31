package com.wks.caseengine.tcs.dto.camundadto;

import java.util.Date;

public interface PlantSubmissionAuditTrailProjection {
    
    //Plant_Id, PlantName, Site_Id, Vertical_Id, SubmittedBy, SubmissionDate,
  //  SubmissionRemark, VerifiedDate, VerifiedBy, VerifiedRemark, Tab, Status, Type
    String getPlant_Id();
    String getPlantName();
    String getSite_Id();
    String getVertical_Id();
    String getSubmittedBy();
    Date getSubmissionDate();
    String getSubmissionRemark();
    Date getVerifiedDate();
    String getVerifiedBy();
    String getVerifiedRemark();
   // String getTab();
    String getStatus();
    String getType();
}
