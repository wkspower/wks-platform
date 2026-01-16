package com.wks.caseengine.tcs.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class CrudeSpecificConstraintsDTO {
    

    private UUID   id;
    private String crude;
    private Double maxBlendLimit;
    private String reasons;
}


