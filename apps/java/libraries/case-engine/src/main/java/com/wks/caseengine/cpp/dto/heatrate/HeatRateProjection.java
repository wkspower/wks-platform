package com.wks.caseengine.cpp.dto.heatrate;

import java.util.UUID;

public interface HeatRateProjection {
    
    UUID   getId();
    String getEquipType();
    String getCPPUtility();
    Double getGTLoad();
    Double getHeatRate();
    Double getFreeSteamFactor();
    String getRemarks();
    Double getPreviousYearHeatRate();
    Double getFinalHeatRate();
    Double getOemHeatRate();
    String getSelectedHeatRate();
}


