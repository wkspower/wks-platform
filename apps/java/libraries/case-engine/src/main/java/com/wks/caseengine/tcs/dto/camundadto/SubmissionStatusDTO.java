package com.wks.caseengine.tcs.dto.camundadto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionStatusDTO {
    
 private boolean cdu_1_submitted;

 private boolean crude_1_submitted;

 private boolean hpib_submitted;
}
