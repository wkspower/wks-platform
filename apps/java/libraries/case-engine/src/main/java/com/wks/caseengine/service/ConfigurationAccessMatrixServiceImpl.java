package com.wks.caseengine.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.ConfigurationAccessMatrixRepository;

@Service
public class ConfigurationAccessMatrixServiceImpl implements ConfigurationAccessMatrixService {

	@Autowired
	private ConfigurationAccessMatrixRepository configurationAccessMatrixRepository;

	@Override
	public AOPMessageVM getConfigurationAccessMatrix(String plantId, String siteId, String verticalId,String type) {
		try {
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			UUID plant = UUID.fromString(plantId);
			UUID site = UUID.fromString(siteId);
			UUID vertical = UUID.fromString(verticalId);

			String configurationTabsStr = configurationAccessMatrixRepository
			        .findConfigurationTabsByVerticalSitePlant(vertical, site, plant,type)
			        .orElse("[]"); // Default to empty JSON array string if not found
			// TODO Auto-generated method stub
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(configurationTabsStr);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

}
