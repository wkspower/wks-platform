package com.wks.caseengine.dto;

public interface FixedConsumptionProjection {

    String getPlantName();
    String getPlantCode();
    String getCostCenterName();
    String getCostCenterCode();
    String getUtilityName();
    String getUtilitySAP();
    String getUtilityPlantName();
    String getUtilityPlantCode();
    String getUom();
    String getNormParameterId();

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
}

