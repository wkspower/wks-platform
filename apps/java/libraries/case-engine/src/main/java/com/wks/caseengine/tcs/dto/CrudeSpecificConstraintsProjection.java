package com.wks.caseengine.tcs.dto;

import java.util.UUID;

public interface CrudeSpecificConstraintsProjection {
    
    UUID   getId();
    String getCrude();
    Double getMaxBlendLimit();
    String getReasons();
}


