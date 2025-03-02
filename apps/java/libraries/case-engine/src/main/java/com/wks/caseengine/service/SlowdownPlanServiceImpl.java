package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.dto.SlowDownPlanDTO;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.repository.SlowdownPlanRepository;

@Service
public class SlowdownPlanServiceImpl implements SlowdownPlanService{
	
	@Autowired
	private SlowdownPlanRepository slowdownPlanRepository;
	
	@Autowired
	private ShutDownPlanService shutDownPlanService;

	@Override
	public List<SlowDownPlanDTO> findSlowdownDetailsByPlantIdAndType(UUID plantId, String maintenanceTypeName) {
		
		List<Object[]> listOfSite=null;
		try {
			listOfSite =slowdownPlanRepository.findSlowdownPlanDetailsByPlantIdAndType(maintenanceTypeName);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		List<SlowDownPlanDTO> dtoList = new ArrayList<>();
		
		for (Object[] result : listOfSite) {
			SlowDownPlanDTO dto = new SlowDownPlanDTO();
			dto.setDiscription((String) result[0]);
			dto.setMaintStartDateTime((Date) result[1]);
			dto.setMaintEndDateTime((Date) result[2]);
			dto.setDurationInMins(result[3] != null ? ((Number) result[3]).longValue() : 0L);
			dto.setRate(result[4] != null ? ((Number) result[4]).doubleValue() : null); // Extract Rate
			dto.setRemarks(result[5] != null ? result[5].toString() : null); // Extract Remarks
			dto.setProduct(result[8] != null ? result[8].toString() : null);
			dto.setMaintenanceId(result[7] != null ? UUID.fromString(result[7].toString()) : null);
	
			dtoList.add(dto);
		}
		// TODO Auto-generated method stub
		return dtoList;
	}



	@Override
	public List<ShutDownPlanDTO> saveShutdownData(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		UUID plantMaintenanceId=shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId,"Slowdown");
		for(ShutDownPlanDTO shutDownPlanDTO:shutDownPlanDTOList) {
			PlantMaintenanceTransaction plantMaintenanceTransaction=new PlantMaintenanceTransaction();
			plantMaintenanceTransaction.setId(UUID.randomUUID());
			plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
			plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins().intValue());
			plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
			plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
			plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);
			plantMaintenanceTransaction.setCreatedOn(new Date());
			plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
			plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
			plantMaintenanceTransaction.setName("Default Name"); 
	        plantMaintenanceTransaction.setVersion("V1");
			plantMaintenanceTransaction.setUser("system"); 
	        if(shutDownPlanDTO.getProductId()!=null) {
	        	plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
	        }
	        	plantMaintenanceTransaction.setAuditYear(shutDownPlanDTO.getAudityear());
			slowdownPlanRepository.save(plantMaintenanceTransaction);
		}
		return shutDownPlanDTOList;
		
	}



	@Override
	public List<ShutDownPlanDTO> editShutdownData(UUID plantMaintenanceTransactionId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		for(ShutDownPlanDTO shutDownPlanDTO:shutDownPlanDTOList) {
		Optional<PlantMaintenanceTransaction> plantMaintenance=slowdownPlanRepository.findById(plantMaintenanceTransactionId);
		PlantMaintenanceTransaction plantMaintenanceTransaction=plantMaintenance.get();
		plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
		  plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
		  plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
		  plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
		  plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
		  plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
      // Save entity
		  slowdownPlanRepository.save(plantMaintenanceTransaction);
		}
		// TODO Auto-generated method stub
		return shutDownPlanDTOList;
	}

}
