package com.wks.caseengine.dto.tcs;

import java.util.UUID;

import lombok.Data;

@Data
public class CrudeBlendDTO {
    
    private UUID   id;
    private String property;
    private String stream;
    private String unit;
    private Double minValue;
    private Double maxValue;
    private Integer criticality;
    private String  remarks;
    private String type;
}
