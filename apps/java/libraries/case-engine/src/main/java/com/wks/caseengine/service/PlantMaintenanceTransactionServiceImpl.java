package com.wks.caseengine.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.repository.PlantMaintenanceTransactionRepository;
@Service
public class PlantMaintenanceTransactionServiceImpl implements PlantMaintenanceTransactionService{
	
	@Autowired
	private PlantMaintenanceTransactionRepository plantMaintenanceTransactionRepository;

	@Override
	public List<PlantMaintenanceTransaction> getAll() {		
		return plantMaintenanceTransactionRepository.findAll();
	}

}
