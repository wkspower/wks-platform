package com.wks.caseengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MonthWiseProductionPlanDTO {
    private String id;
    private String remark;
    private Double opHrsActual;
}
