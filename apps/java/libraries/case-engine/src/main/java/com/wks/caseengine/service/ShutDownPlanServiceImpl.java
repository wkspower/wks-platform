package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.repository.PlantMaintenanceTransactionRepository;
import com.wks.caseengine.repository.ShutDownPlanRepository;

@Service
public class ShutDownPlanServiceImpl implements ShutDownPlanService{
	
	@Autowired
	private ShutDownPlanRepository shutDownPlanRepository;
	
	@Autowired
	private PlantMaintenanceTransactionRepository plantMaintenanceTransactionRepository;

	@Override
	public List<Object[]> findMaintenanceDetailsByPlantIdAndType(UUID plantId,String maintenanceTypeName) {
		return	shutDownPlanRepository.findMaintenanceDetailsByPlantIdAndType(plantId,maintenanceTypeName);
	}

	@Override
	public UUID findPlantMaintenanceId(String productName) {
		return shutDownPlanRepository.findPlantMaintenanceId(productName);
	}

	@Override
	public void saveShutdownData(PlantMaintenanceTransaction plantMaintenanceTransaction) {
		plantMaintenanceTransactionRepository.save(plantMaintenanceTransaction);
	}

}
