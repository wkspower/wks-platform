package com.wks.caseengine.service;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
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
public class MaintenanceCalculatedDataServiceImpl implements MaintenanceCalculatedDataService{
	
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
		List<MaintenanceDetailsDTO> list = executeDynamicStoredProcedure(storedProcedure,plantId,
				site.getId().toString(), vertical.getId().toString(), year);
		
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
		return list;
	}
	
	@Transactional
    public List<MaintenanceDetailsDTO> executeDynamicStoredProcedure(String procedureName, String plantId, String siteId, String verticalId, String aopYear) {
        String sql = "EXEC " + procedureName + " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @aopYear = :aopYear";
        Query query = entityManager.createNativeQuery(sql);
        
        query.setParameter("plantId", plantId);
        query.setParameter("siteId", siteId);
        query.setParameter("verticalId", verticalId);
        query.setParameter("aopYear", aopYear);

        return query.getResultList();
    }
}
