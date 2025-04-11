package com.wks.caseengine.service;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import com.wks.caseengine.dto.MaintenanceDetailsDTO;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.repository.MaintenanceCalculatedDataRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

@Service
public class MaintenanceCalculatedServiceImpl implements MaintenanceCalculatedService{
	
	@Autowired
	private MaintenanceCalculatedDataRepository maintenanceCalculatedDataRepository;

	@Autowired
	PlantsRepository plantsRepository;
	@Autowired
	SiteRepository siteRepository;
	@Autowired
	VerticalsRepository verticalRepository;
	@PersistenceContext
    private EntityManager entityManager;
	@Override
	public List<MaintenanceDetailsDTO> getMaintenanceCalculatedData(String plantId,  String year) {
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		String storedProcedure=vertical.getName()+"_HMD_GETMaintenance";
//		List<MaintenanceCalculatedData> maintenanceCalculatedDataList= 	maintenanceCalculatedDataRepository.findAllByPlantFKIdAndAopYear(UUID.fromString(plantId),year);
		List<Object[]> list = executeDynamicStoredProcedure(storedProcedure,plantId,
				site.getId().toString(), vertical.getId().toString(), year);
		List<MaintenanceDetailsDTO> maintenanceDetailsDTOList=new ArrayList<>();
		for(Object[] row : list) {
			MaintenanceDetailsDTO dto = new MaintenanceDetailsDTO();
			dto.setName(row[2] != null ? row[2].toString() : null);
			dto.setJan(row[3] != null ? Float.valueOf(row[3].toString()) : null);
	        dto.setFeb(row[4] != null ? Float.valueOf(row[4].toString()) : null);
	        dto.setMar(row[5] != null ? Float.valueOf(row[5].toString()) : null);
	        dto.setApril(row[6] != null ? Float.valueOf(row[6].toString()) : null);
	        dto.setMay(row[7] != null ? Float.valueOf(row[7].toString()) : null);
	        dto.setJune(row[8] != null ? Float.valueOf(row[8].toString()) : null);
	        dto.setJuly(row[9] != null ? Float.valueOf(row[9].toString()) : null);
	        dto.setAug(row[10] != null ? Float.valueOf(row[10].toString()) : null);
	        dto.setSep(row[11] != null ? Float.valueOf(row[11].toString()) : null);
	        dto.setOct(row[12] != null ? Float.valueOf(row[12].toString()) : null);
	        dto.setNov(row[13] != null ? Float.valueOf(row[13].toString()) : null);
	        dto.setDec(row[14] != null ? Float.valueOf(row[14].toString()) : null);
	        maintenanceDetailsDTOList.add(dto);
		}
		
		//List<MaintenanceCalculatedDataDTO> maintenanceCalculatedDataDTOList = list.stream()
			//    .map(MaintenanceCalculatedDataDTO::new)
			  //  .collect(Collectors.toList());
//		List<MaintenanceCalculatedDataDTO> maintenanceCalculatedDataDTOList= new ArrayList<>();
//		for(MaintenanceCalculatedData maintenanceCalculatedData:maintenanceCalculatedDataList) {
//			MaintenanceCalculatedDataDTO maintenanceCalculatedDataDTO= new MaintenanceCalculatedDataDTO();
//			maintenanceCalculatedDataDTO.setAopYear(maintenanceCalculatedData.getAopYear());
//			maintenanceCalculatedDataDTO.setAvgSlowdownLoadPVT(maintenanceCalculatedData.getAvgSlowdownLoadPVT());
//			maintenanceCalculatedDataDTO.setEffectiveOperatingHrs(maintenanceCalculatedData.getEffectiveOperatingHrs());
//			maintenanceCalculatedDataDTO.setId(maintenanceCalculatedData.getId().toString());
//			maintenanceCalculatedDataDTO.setMonthNo(maintenanceCalculatedData.getMonthNo());
//			maintenanceCalculatedDataDTO.setNonShoutdownHrs(maintenanceCalculatedData.getNonShoutdownHrs());
//			maintenanceCalculatedDataDTO.setPlantFKId(maintenanceCalculatedData.getPlantFKId().toString());
//			maintenanceCalculatedDataDTO.setRunningHoursInMonth(maintenanceCalculatedData.getRunningHoursInMonth());
//			maintenanceCalculatedDataDTO.setShoutdownHrs(maintenanceCalculatedData.getShoutdownHrs());
//			maintenanceCalculatedDataDTO.setSlowdownLoadReduction(maintenanceCalculatedData.getSlowdownLoadReduction());
//			maintenanceCalculatedDataDTOList.add(maintenanceCalculatedDataDTO);
//		}
		// TODO Auto-generated method stub
		return maintenanceDetailsDTOList;
	}
	
	@Transactional
    public List<Object[]> executeDynamicStoredProcedure(String procedureName, String plantId, String siteId, String verticalId, String aopYear) {
        String sql = "EXEC " + procedureName + " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @aopYear = :aopYear";
        Query query = entityManager.createNativeQuery(sql);
        
        query.setParameter("plantId", plantId);
        query.setParameter("siteId", siteId);
        query.setParameter("verticalId", verticalId);
        query.setParameter("aopYear", aopYear);

        return query.getResultList();
    }
}
