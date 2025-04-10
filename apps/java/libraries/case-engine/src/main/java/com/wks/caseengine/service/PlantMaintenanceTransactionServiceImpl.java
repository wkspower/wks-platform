package com.wks.caseengine.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantMaintenanceTransactionRepository;

@Service
public class PlantMaintenanceTransactionServiceImpl implements PlantMaintenanceTransactionService {

	@Autowired
	private PlantMaintenanceTransactionRepository plantMaintenanceTransactionRepository;

	@Override
	public AOPMessageVM getAll() {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			List<PlantMaintenanceTransaction> list = plantMaintenanceTransactionRepository.findAll();
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(list);
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid type", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

}
