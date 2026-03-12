package com.wks.caseengine.crude.dto;

public interface NormBasisProjection {
    
    String getId();

    String getName();
    String getDisplayName();
    String getUOM();
    String getAttributeValue();
    String getConfig();
    String getRemarks();
    String getType();
    String getNormParameterType();
    String getDisplayOrder();
    Integer getIsEditable();
}
