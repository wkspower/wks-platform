package com.wks.caseengine.dto.cpp.heatrate;

import java.util.UUID;

public interface HeatRateProjection {
    
    UUID   getId();
    String getEquipType();
    String getCPPUtility();
    Double getGTLoad();
    Double getHeatRate();
    Double getFreeSteamFactor();
    String getRemarks();
}
