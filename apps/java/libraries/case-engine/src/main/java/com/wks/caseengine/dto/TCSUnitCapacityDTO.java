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
public class TCSUnitCapacityDTO {
    private String id;          
    private String particulates;
    private String uom;
    private Double kbpsd;
    private String remark;
    private Date insertedDateTime;
}
