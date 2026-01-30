package com.wks.caseengine.cpp.repository;

import java.util.UUID;

public interface ImportPowerHoursProjection {
    UUID getSourceId();
    String getSourceName();
    String getMaterialCode();
    String getSAPMaterialCode();
    String getUtilityName();
    String getPlantName();
    Double getApr();
    Double getMay();
    Double getJun();
    Double getJul();
    Double getAug();
    Double getSep();
    Double getOct();
    Double getNov();
    Double getDec();
    Double getJan();
    Double getFeb();
    Double getMar();
    String getRemarks();
}
