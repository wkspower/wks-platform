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
	public List<ShutDownPlanDTO> findSlowdownDetailsByPlantIdAndType(UUID plantId, String maintenanceTypeName,String year) {
		
		List<Object[]> listOfSite=null;
		try {
			listOfSite =slowdownPlanRepository.findSlowdownPlanDetailsByPlantIdAndType(maintenanceTypeName,plantId.toString(),year);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		List<ShutDownPlanDTO> dtoList = new ArrayList<>();
		
		for (Object[] result : listOfSite) {
			ShutDownPlanDTO dto = new ShutDownPlanDTO();
			dto.setDiscription((String) result[0]);
			dto.setMaintStartDateTime(result[1]!=null? (Date) result[1] :null);
			dto.setMaintEndDateTime(result[2]!=null ?(Date) result[2] :null);
			dto.setDurationInMins(result[3] != null ? ((Integer) result[3]) : null); 
			double durationInHrs = ((Integer) result[3]) / 60.0;
			dto.setDurationInHrs(durationInHrs);
			dto.setRate(result[4] != null ? ((Number) result[4]).doubleValue() : null); // Extract Rate
			dto.setRemark(result[5] != null ? result[5].toString() : null); // Extract Remarks
			dto.setProduct(result[6] != null ? result[6].toString() : null);
			dto.setProductId(result[8] != null ? UUID.fromString(result[8].toString()) : null);
	
			dtoList.add(dto);
		}
		// TODO Auto-generated method stub
		return dtoList;
	}



	@Override
	public List<ShutDownPlanDTO> saveShutdownData(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		UUID plantMaintenanceId=shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId,"Slowdown");
		for(ShutDownPlanDTO shutDownPlanDTO:shutDownPlanDTOList) {

			if (shutDownPlanDTO.getId() == null || shutDownPlanDTO.getId().isEmpty()) {
			PlantMaintenanceTransaction plantMaintenanceTransaction=new PlantMaintenanceTransaction();
			plantMaintenanceTransaction.setId(UUID.randomUUID());
			plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
			if(shutDownPlanDTO.getDurationInMins()!=null){
				plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins() * 60);
			}else{
				plantMaintenanceTransaction.setDurationInHrs(0d);
			}
			
			plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
			plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
			plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);
			if(shutDownPlanDTO.getMaintStartDateTime()!=null){
				plantMaintenanceTransaction.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth()+1);
			}
			
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
		else{

			Optional<PlantMaintenanceTransaction> plantMaintenance=slowdownPlanRepository.findById(UUID.fromString(shutDownPlanDTO.getId()));
			PlantMaintenanceTransaction plantMaintenanceTransaction = new PlantMaintenanceTransaction();
			plantMaintenanceTransaction.setId(UUID.randomUUID());
			
			// Set mandatory fields with default values if missing
			plantMaintenanceTransaction.setDiscription(
				shutDownPlanDTO.getDiscription() != null ? shutDownPlanDTO.getDiscription() : "Default Description"
			);

			if(shutDownPlanDTO.getDurationInMins()!=null){
				plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins() * 60);
			}else{
				plantMaintenanceTransaction.setDurationInHrs(0d);
			}
			//plantMaintenanceTransaction.setDurationInMins(
			//	shutDownPlanDTO.getDurationInMins() != null ? shutDownPlanDTO.getDurationInMins().intValue() : 0
			//);
			plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
			plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
			plantMaintenanceTransaction.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth()+1);
			plantMaintenanceTransaction.setUser("system");
			plantMaintenanceTransaction.setName("Default Name");
			plantMaintenanceTransaction.setVersion("V1");
			plantMaintenanceTransaction.setCreatedOn(new Date());
			plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);

			plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());

			if (shutDownPlanDTO.getProductId() != null) {
				plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
			}

			plantMaintenanceTransaction.setAuditYear(shutDownPlanDTO.getAudityear());

			// Save new record
			slowdownPlanRepository.save(plantMaintenanceTransaction);

		}
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
		  
		  if(shutDownPlanDTO.getMaintStartDateTime()!=null){
			plantMaintenanceTransaction.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth()+1);
		  }
		  plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
		  plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
      // Save entity
		  slowdownPlanRepository.save(plantMaintenanceTransaction);
		}
		// TODO Auto-generated method stub
		return shutDownPlanDTOList;
	}

}
