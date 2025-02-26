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
	public List<ShutDownPlanDTO> findTurnaroundPlanDataByPlantIdAndType(UUID plantId, String maintenanceTypeName) {
		List<Object[]> listOfSite=null;
		List<ShutDownPlanDTO> dtoList = new ArrayList<>();
		 listOfSite=turnaroundPlanRepository.findTurnaroundPlanDetailsByPlantIdAndType(maintenanceTypeName);
		  for (Object[] result : listOfSite) { 
			  ShutDownPlanDTO dto = new  ShutDownPlanDTO();
			  dto.setDiscription((String) result[0]); 
			  dto.setMaintStartDateTime((Date)result[1]);
			  dto.setMaintEndDateTime((Date) result[2]);
			  // dto.setDurationInMins((Integer) result[3]); // Duration in minutes
			  dto.setDurationInMins(result[3] != null ? ((Integer) result[3]) : 0); 
			  double durationInHrs = ((Integer) result[3]) / 60.0;
			  dto.setDurationInHrs(durationInHrs);
			  dto.setRemark((String)result[4]);
			  dto.setProduct((String) result[6]);
			  long diffInMillis = dto.getMaintEndDateTime().getTime() - dto.getMaintStartDateTime().getTime();
			  double diffInDays = diffInMillis / (1000.0 * 60 * 60 * 24);
			  dto.setDurationInDays(diffInDays);
			  dtoList.add(dto); 
		}
		// TODO Auto-generated method stub
		return dtoList;
	}



	@Override
	public ShutDownPlanDTO saveTurnaroundPlanData(UUID plantId, ShutDownPlanDTO shutDownPlanDTO) {
		UUID plantMaintenanceId=shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId,"TA_Plan");
		PlantMaintenanceTransaction plantMaintenanceTransaction=new PlantMaintenanceTransaction();
		plantMaintenanceTransaction.setId(UUID.randomUUID());
		plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
		plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins().intValue());
		plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
		plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
		plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);
		plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());




		plantMaintenanceTransaction.setUser("system"); 
        plantMaintenanceTransaction.setName("Default Name");
        plantMaintenanceTransaction.setVersion("V1");
        plantMaintenanceTransaction.setCreatedOn(new Date());



		// plantMaintenanceTransaction.setName("Default Name"); 
        // plantMaintenanceTransaction.setVersion("V1");
		// plantMaintenanceTransaction.setUser("system"); 
		// plantMaintenanceTransaction.setCreatedOn(new Date());


		

        if(shutDownPlanDTO.getProductId()!=null) {
        	plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
        }
        if(shutDownPlanDTO.getAudityear()==null) {
        	plantMaintenanceTransaction.setAuditYear(2025);
        }else {
        	plantMaintenanceTransaction.setAuditYear(shutDownPlanDTO.getAudityear());
        }
		


		turnaroundPlanRepository.save(plantMaintenanceTransaction);
		// TODO Auto-generated method stub
		return shutDownPlanDTO;
	}



	@Override
	public ShutDownPlanDTO editTurnaroundPlanData(UUID plantMaintenanceTransactionId, ShutDownPlanDTO shutDownPlanDTO) {
		Optional<PlantMaintenanceTransaction> plantMaintenance=turnaroundPlanRepository.findById(plantMaintenanceTransactionId);
		PlantMaintenanceTransaction plantMaintenanceTransaction=plantMaintenance.get();
		  plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
		  plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins().intValue());
		  plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
		  plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
		  plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
		  plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
		  turnaroundPlanRepository.save(plantMaintenanceTransaction);
		// TODO Auto-generated method stub
		return shutDownPlanDTO;
	}

}
