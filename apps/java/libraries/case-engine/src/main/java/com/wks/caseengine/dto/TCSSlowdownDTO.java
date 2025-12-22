package com.wks.caseengine.dto;

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
    private Integer tentativeDurationInDays;
    private String throughputDuringSlowdown;
    private String tentativeMonth;
    private String purposeOfSlowdown;
    private Date insertedDateTime;
}
