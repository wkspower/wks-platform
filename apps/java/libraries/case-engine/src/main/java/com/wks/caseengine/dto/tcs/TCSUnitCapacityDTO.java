package com.wks.caseengine.dto.tcs;

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
 //   private Double value;
 private Double summer;
 private Double winter;
    private String remark;
    private Date insertedDateTime;
}
