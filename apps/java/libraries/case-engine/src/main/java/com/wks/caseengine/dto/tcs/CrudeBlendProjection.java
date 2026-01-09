package com.wks.caseengine.dto.tcs;

import java.util.UUID;

public interface CrudeBlendProjection {
    
    UUID   getId();
    String getProperty();
    String getStream();
    String getUnit();
    Double getMinValue();
    Double getMaxValue();
    Integer getCriticality();
    String  getRemarks();
    String getType();
}
