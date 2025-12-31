package com.wks.caseengine.dto;

import java.util.UUID;

public interface PowerGenerationNormParametersProjection {

    String getName();
    Integer getNormType_FK_Id();
    String getSAPMaterialCode();
    UUID getAssetId();
    
}
