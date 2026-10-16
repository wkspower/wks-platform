package com.wks.caseengine.cpp.dto;

import java.math.BigDecimal;
import java.util.UUID;

public interface CPPImportPowerProjection {

    UUID getId();
    String getProcurementPlant();
    String getPlantName();
    String getUtilityCode();
    String getUtilityName();
    String getMaterialCode();
    String getMaterialName();
    String getUtility();
    String getMaterial();
    String getMaterialDisplayName();
    String getUom();

    BigDecimal getApr();
    BigDecimal getMay();
    BigDecimal getJun();
    BigDecimal getJul();
    BigDecimal getAug();
    BigDecimal getSep();
    BigDecimal getOct();
    BigDecimal getNov();
    BigDecimal getDec();
    BigDecimal getJan();
    BigDecimal getFeb();
    BigDecimal getMar();

    String getAopYear();
    UUID getSiteFkId();
    UUID getVerticalFkId();
    String getRemarks();
    String getCreatedDate();
    String getUpdatedDate();
    UUID getImportPlantFkId();
    UUID getCppPlantFkId();
    UUID getNormParameterFkId();
    UUID getUtilityNormParameterFkId();
}
