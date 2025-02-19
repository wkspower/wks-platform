package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.repository.TurnaroundPlanRepository;

@Service
public class TurnaroundPlanServiceImpl implements TurnaroundPlanService{
	
	@Autowired 
	private TurnaroundPlanRepository turnaroundPlanRepository;

	@Override
	public List<Object[]> findTurnaroundPlanDetailsByPlantIdAndType(UUID plantId, String maintenanceTypeName) {		
		return turnaroundPlanRepository.findTurnaroundPlanDetailsByPlantIdAndType(plantId,maintenanceTypeName);
	}

}
