package com.wks.caseengine.naphthasplitter.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataFilterDTO {
    private UUID normParameterId;
    private String displayName;
    private Double targetValue;
    private Double range;
    private String remarks;
    private String aopYear;
    private UUID plantId;
}
