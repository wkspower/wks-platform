package com.wks.caseengine.dto;

import lombok.*;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlowdownHistoryConfigDTO {

    private UUID id;
    private String description;
    private Date maintStartDateTime;
    private Date maintEndDateTime;
    private Integer durationInMins;
    private Integer maintForMonth;
    private String auditYear;
    private Double rate;
    private String remarks;
    private Date updatedOn;
    private String updatedBy;
    private UUID plantFkId;

}
