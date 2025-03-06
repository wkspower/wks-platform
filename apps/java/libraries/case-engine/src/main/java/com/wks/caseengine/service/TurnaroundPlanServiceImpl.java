package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.repository.TurnaroundPlanRepository;

@Service
public class TurnaroundPlanServiceImpl implements TurnaroundPlanService{
	
	@Autowired 
	private TurnaroundPlanRepository turnaroundPlanRepository;
	
	@Autowired
	private ShutDownPlanService shutDownPlanService;

	

	@Override
	public List<ShutDownPlanDTO> findTurnaroundPlanDataByPlantIdAndType(UUID plantId, String maintenanceTypeName,String year) {
		List<Object[]> listOfSite=null;
		List<ShutDownPlanDTO> dtoList = new ArrayList<>();
		 listOfSite=turnaroundPlanRepository.findTurnaroundPlanDetailsByPlantIdAndType(maintenanceTypeName,plantId.toString(), year);
		  for (Object[] result : listOfSite) { 
			  ShutDownPlanDTO dto = new  ShutDownPlanDTO();
			  dto.setDiscription((String) result[0]); 
			  dto.setMaintStartDateTime((Date)result[1]);
			  dto.setMaintEndDateTime((Date) result[2]);
			  // dto.setDurationInMins((Integer) result[3]); // Duration in minutes
			  dto.setDurationInMins(result[3] != null ? ((Integer) result[3]) : null); 
			  double durationInHrs = ((Integer) result[3]) / 60.0;
			  dto.setDurationInHrs(durationInHrs);
			  dto.setRemark((String)result[4]);
			  dto.setProduct((String) result[6]);
			  dto.setId((String) result[7]);
			  long diffInMillis = dto.getMaintEndDateTime().getTime() - dto.getMaintStartDateTime().getTime();
			  double diffInDays = diffInMillis / (1000.0 * 60 * 60 * 24);
			 // dto.setDurationInDays(diffInDays);
			  dtoList.add(dto); 
		}
		// TODO Auto-generated method stub
		return dtoList;
	}



	@Override
	public List<ShutDownPlanDTO> saveTurnaroundPlanData(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		UUID plantMaintenanceId=shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId,"TA_Plan");
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
			turnaroundPlanRepository.save(plantMaintenanceTransaction);

		}

		else{

			Optional<PlantMaintenanceTransaction> plantMaintenance=turnaroundPlanRepository.findById(UUID.fromString(shutDownPlanDTO.getId()));
			PlantMaintenanceTransaction plantMaintenanceTransaction=plantMaintenance.get();
			  plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
			  plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins().intValue());
			  plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
			  plantMaintenanceTransaction.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth()+1);
			  plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
			  plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
			  plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
			  turnaroundPlanRepository.save(plantMaintenanceTransaction);

		}

		}
		// TODO Auto-generated method stub
		return shutDownPlanDTOList;
	}



	@Override
	public List<ShutDownPlanDTO> editTurnaroundPlanData(UUID plantMaintenanceTransactionId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		for(ShutDownPlanDTO shutDownPlanDTO:shutDownPlanDTOList) {
			Optional<PlantMaintenanceTransaction> plantMaintenance=turnaroundPlanRepository.findById(plantMaintenanceTransactionId);
			PlantMaintenanceTransaction plantMaintenanceTransaction=plantMaintenance.get();
			  plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
			  plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins().intValue());
			  plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
			  plantMaintenanceTransaction.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth()+1);
			  plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
			  plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
			  plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
			  turnaroundPlanRepository.save(plantMaintenanceTransaction);
		}
		// TODO Auto-generated method stub
		return shutDownPlanDTOList;
	}

}
