package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.repository.SlowdownPlanRepository;

@Service
public class SlowdownPlanServiceImpl implements SlowdownPlanService{
	
	@Autowired
	private SlowdownPlanRepository slowdownPlanRepository;

	@Override
	public List<Object[]> findSlowdownPlanDetailsByPlantIdAndType(UUID plantId, String maintenanceTypeName) {
		return slowdownPlanRepository.findSlowdownPlanDetailsByPlantIdAndType(plantId,maintenanceTypeName);
	}

}
