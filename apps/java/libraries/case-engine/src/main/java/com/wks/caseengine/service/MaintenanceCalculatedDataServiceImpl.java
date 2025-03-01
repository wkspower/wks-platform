package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.wks.caseengine.dto.MaintenanceCalculatedDataDTO;
import com.wks.caseengine.entity.MaintenanceCalculatedData;
import com.wks.caseengine.repository.MaintenanceCalculatedDataRepository;

@Service
public class MaintenanceCalculatedDataServiceImpl implements MaintenanceCalculatedDataService{
	
	@Autowired
	private MaintenanceCalculatedDataRepository maintenanceCalculatedDataRepository;

	@Override
	public List<MaintenanceCalculatedDataDTO> getMaintenanceCalculatedData(String plantId,  String year) {
		List<MaintenanceCalculatedData> maintenanceCalculatedDataList= 	maintenanceCalculatedDataRepository.findAllByPlantIdAndYear(UUID.fromString(plantId),year);
		List<MaintenanceCalculatedDataDTO> maintenanceCalculatedDataDTOList= new ArrayList<>();
		for(MaintenanceCalculatedData maintenanceCalculatedData:maintenanceCalculatedDataList) {
			MaintenanceCalculatedDataDTO maintenanceCalculatedDataDTO= new MaintenanceCalculatedDataDTO();
			maintenanceCalculatedDataDTO.setAopYear(maintenanceCalculatedData.getAopYear());
			maintenanceCalculatedDataDTO.setAvgSlowdownLoadPVT(maintenanceCalculatedData.getAvgSlowdownLoadPVT());
			maintenanceCalculatedDataDTO.setEffectiveOperatingHrs(maintenanceCalculatedData.getEffectiveOperatingHrs());
			maintenanceCalculatedDataDTO.setId(maintenanceCalculatedData.getId().toString());
			maintenanceCalculatedDataDTO.setMonthNo(maintenanceCalculatedData.getMonthNo());
			maintenanceCalculatedDataDTO.setNonShoutdownHrs(maintenanceCalculatedData.getNonShoutdownHrs());
			maintenanceCalculatedDataDTO.setPlantFKId(maintenanceCalculatedData.getPlantFKId().toString());
			maintenanceCalculatedDataDTO.setRunningHoursInMonth(maintenanceCalculatedData.getRunningHoursInMonth());
			maintenanceCalculatedDataDTO.setShoutdownHrs(maintenanceCalculatedData.getShoutdownHrs());
			maintenanceCalculatedDataDTO.setSlowdownLoadReduction(maintenanceCalculatedData.getSlowdownLoadReduction());
			maintenanceCalculatedDataDTOList.add(maintenanceCalculatedDataDTO);
		}
		// TODO Auto-generated method stub
		return maintenanceCalculatedDataDTOList;
	}
}
