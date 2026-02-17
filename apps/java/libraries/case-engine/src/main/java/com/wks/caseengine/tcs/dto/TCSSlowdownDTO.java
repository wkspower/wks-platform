package com.wks.caseengine.tcs.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TCSSlowdownDTO {

    private String id;
    private String particulates;
    private Integer durationInDays;
    private Double throughputDuringSlowdown;
    private String throughputUOM;
    private Date startDate;
    private Date endDate;
    private String purpose;
    private Date insertedDateTime;
    
    // Fields for import/export status
    private String saveStatus;
    private String errDescription;
}


