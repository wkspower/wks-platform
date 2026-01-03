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
public class STGExtractionLookupDTO {

    private UUID id;
    private BigDecimal loadMW;
    private BigDecimal svhInletTPH;
    private BigDecimal smBleedFlowTPH;
    private BigDecimal slExtFlowTPH;
    private BigDecimal condensingLoadM3Hr;
    private BigDecimal heatRateKcalKWH;
    private String remarks;
}
