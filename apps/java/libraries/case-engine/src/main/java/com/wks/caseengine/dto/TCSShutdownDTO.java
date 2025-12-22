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
public class TCSShutdownDTO {

    private String id;
    private String particulates;
    private Integer sdTotalDurationInDays;
    private String tentativeMonth;
    private String purposeOfShutdown;
    private Date insertedDateTime;
}
