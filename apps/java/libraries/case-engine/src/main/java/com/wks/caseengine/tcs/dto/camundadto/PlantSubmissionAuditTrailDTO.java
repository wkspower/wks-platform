package com.wks.caseengine.tcs.dto.camundadto;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlantSubmissionAuditTrailDTO {

    private UUID plantId;
    private String plantName;
    private UUID siteId;
    private UUID verticalId;
    private String submittedBy;
    private Date submissionDateTime;
    private String submissionRemark;

    private Date verifiedDateTime;
    private String verifiedBy;
    private String verifiedRemark;

  //  private String tab;
    private String status;
    private String type;
}
