package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

public interface TurnaroundPlanService {
	
	public List<Object[]> findTurnaroundPlanDetailsByPlantIdAndType(UUID plantId,String maintenanceTypeName);

}
