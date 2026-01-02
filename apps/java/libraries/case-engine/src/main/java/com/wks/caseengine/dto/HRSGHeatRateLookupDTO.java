package com.wks.caseengine.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HRSGHeatRateLookupDTO {

    private UUID id;
    private String equipmentName;
    private String cppUtility;
    private BigDecimal hrsgLoad;
    private BigDecimal heatRate;
    private String remarks;

}
