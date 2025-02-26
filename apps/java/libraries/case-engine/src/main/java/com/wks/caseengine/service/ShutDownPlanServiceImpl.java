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
import com.wks.caseengine.repository.PlantMaintenanceTransactionRepository;
import com.wks.caseengine.repository.ShutDownPlanRepository;


@Service
public class ShutDownPlanServiceImpl implements ShutDownPlanService{
	
	@Autowired
	private ShutDownPlanRepository shutDownPlanRepository;
	
	@Autowired
	private PlantMaintenanceTransactionRepository plantMaintenanceTransactionRepository;

	@Override
	public List<ShutDownPlanDTO> findMaintenanceDetailsByPlantIdAndType(UUID plantId,String maintenanceTypeName) {
		List<ShutDownPlanDTO> dtoList = new ArrayList<>();
		List<Object[]> listOfSite=	shutDownPlanRepository.findMaintenanceDetailsByPlantIdAndType(maintenanceTypeName);
		for (Object[] result : listOfSite) {
            ShutDownPlanDTO dto = new ShutDownPlanDTO();
            dto.setDiscription((String) result[0]);
            dto.setMaintStartDateTime((Date) result[1]);
            dto.setMaintEndDateTime((Date) result[2]);
            dto.setDurationInMins(result[3] != null ? ((Integer) result[3]) : 0); 
            dto.setProduct((String) result[6]);
            //FOR ID : pmt.Id
            dto.setMaintenanceId(result[5] != null ? UUID.fromString(result[5].toString()) : null); 
            dtoList.add(dto);
        }
		return dtoList;
	}

	@Override
	public UUID findPlantMaintenanceId(String productName) {
		return shutDownPlanRepository.findPlantMaintenanceId(productName);
	}

	@Override
	public void saveShutdownData(PlantMaintenanceTransaction plantMaintenanceTransaction) {
		plantMaintenanceTransactionRepository.save(plantMaintenanceTransaction);
	}
	
	@Override
	public UUID findIdByPlantIdAndMaintenanceTypeName(UUID plantId,String maintenanceTypeName) {
		return shutDownPlanRepository.findIdByPlantIdAndMaintenanceTypeName(plantId, maintenanceTypeName);
	}

	

	@Override
	public void deletePlanData(UUID plantMaintenanceTransactionId) {
		Optional<PlantMaintenanceTransaction> plantMaintenanceTransaction=plantMaintenanceTransactionRepository.findById(plantMaintenanceTransactionId);
		plantMaintenanceTransactionRepository.delete(plantMaintenanceTransaction.get());	
	}

	@Override
	public ShutDownPlanDTO saveShutdownPlantData(UUID plantId, ShutDownPlanDTO shutDownPlanDTO) {
		UUID plantMaintenanceId=findIdByPlantIdAndMaintenanceTypeName(plantId,"Shutdown");
        PlantMaintenanceTransaction plantMaintenanceTransaction = new PlantMaintenanceTransaction();
        plantMaintenanceTransaction.setId(UUID.randomUUID());
        // Set mandatory fields with default values if missing
        plantMaintenanceTransaction.setDiscription(
            shutDownPlanDTO.getDiscription() != null ? shutDownPlanDTO.getDiscription() : "Default Description"
        );
    
        plantMaintenanceTransaction.setDurationInMins(
            shutDownPlanDTO.getDurationInMins() != null ? shutDownPlanDTO.getDurationInMins().intValue() : 0
        );

        
        plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
        plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
        // Ensure required fields exist


		plantMaintenanceTransaction.setUser("system"); 
        plantMaintenanceTransaction.setName("Default Name");
        plantMaintenanceTransaction.setVersion("V1");
        plantMaintenanceTransaction.setCreatedOn(new Date());



		// plantMaintenanceTransaction.setName("Default Name"); 
        // plantMaintenanceTransaction.setVersion("V1");
		// plantMaintenanceTransaction.setUser("system"); 
		// plantMaintenanceTransaction.setCreatedOn(new Date());



        plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);
        if(shutDownPlanDTO.getProductId()!=null) {
        	plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
        }
        if(shutDownPlanDTO.getAudityear()==null) {
        	plantMaintenanceTransaction.setAuditYear(2025);
        }else {
        	plantMaintenanceTransaction.setAuditYear(shutDownPlanDTO.getAudityear());
        }
        
        plantMaintenanceTransactionRepository.save(plantMaintenanceTransaction);
		// TODO Auto-generated method stub
		return shutDownPlanDTO;
	}

	@Override
	public ShutDownPlanDTO editShutdownData(UUID plantMaintenanceTransactionId, ShutDownPlanDTO shutDownPlanDTO) {
		Optional<PlantMaintenanceTransaction> plantMaintenance=	shutDownPlanRepository.findById(plantMaintenanceTransactionId);
		  PlantMaintenanceTransaction plantMaintenanceTransaction= plantMaintenance.get();
		  plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
		  plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins());
		  plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
		  plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
		  plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());  
		  plantMaintenanceTransactionRepository.save(plantMaintenanceTransaction);
		// TODO Auto-generated method stub
		return shutDownPlanDTO;
	}

	@Override
	public PlantMaintenanceTransaction editShutDownPlanData(UUID plantMaintenanceTransactionId) {
		// TODO Auto-generated method stub
		return null;
	}

}
