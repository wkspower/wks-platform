package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.MonthWiseDataDTO;
import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.entity.PlantMaintenance;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.repository.PlantMaintenanceRepository;
import com.wks.caseengine.repository.PlantMaintenanceTransactionRepository;
import com.wks.caseengine.repository.ShutDownPlanRepository;


@Service
public class ShutDownPlanServiceImpl implements ShutDownPlanService{
	
	@Autowired
	private ShutDownPlanRepository shutDownPlanRepository;
	
	@Lazy  // Add this annotation here
	@Autowired
	private SlowdownPlanService slowdownPlanService;
	
	@Autowired
	private PlantMaintenanceRepository plantMaintenanceRepository;
	
	@Autowired
	private PlantMaintenanceTransactionRepository plantMaintenanceTransactionRepository;

	@Override
	public List<ShutDownPlanDTO> findMaintenanceDetailsByPlantIdAndType(UUID plantId,String maintenanceTypeName, String year) {
		List<ShutDownPlanDTO> dtoList = new ArrayList<>();
		List<Object[]> listOfSite=	shutDownPlanRepository.findMaintenanceDetailsByPlantIdAndType(maintenanceTypeName, plantId.toString(), year);
		for (Object[] result : listOfSite) {
            ShutDownPlanDTO dto = new ShutDownPlanDTO();
            dto.setDiscription((String) result[0]);
            dto.setMaintStartDateTime((Date) result[1]);
            dto.setMaintEndDateTime((Date) result[2]);
            dto.setDurationInMins(result[3] != null ? ((Integer) result[3]) : null); 
			if(result[3]!=null){
				double durationInHrs = ((Integer) result[3]) / 60.0;
				dto.setDurationInHrs(durationInHrs);
			}
            dto.setProduct((String) result[6]);
            //FOR ID : pmt.Id
            dto.setId(result[5] != null ? result[5].toString() : null); 
			if((String) result[7]!=null){
				dto.setRemark((String) result[7]);
			}else{
				dto.setRemark(null);
			}
			dto.setDisplayOrder(result[8] != null ? ((Integer) result[8]) : null); 
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
	public List<ShutDownPlanDTO> saveShutdownPlantData(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		
		UUID plantMaintenanceId = findIdByPlantIdAndMaintenanceTypeName(plantId, "Shutdown");
		if(plantMaintenanceId==null) {
			UUID maintenanceTypesId =plantMaintenanceTransactionRepository.findIdByName("Shutdown");
			PlantMaintenance plantMaintenance=new PlantMaintenance();
			plantMaintenance.setMaintenanceText("Shutdown");
			plantMaintenance.setIsDefault(true);
			plantMaintenance.setPlantFkId(plantId);
			plantMaintenance.setMaintenanceTypeFkId(maintenanceTypesId);
			plantMaintenanceRepository.save(plantMaintenance);
			plantMaintenanceId = findIdByPlantIdAndMaintenanceTypeName(plantId, "Shutdown");
		}
	
		for (ShutDownPlanDTO shutDownPlanDTO : shutDownPlanDTOList) {
			if (shutDownPlanDTO.getId() == null || shutDownPlanDTO.getId().isEmpty()) {
				// Creating a new record
				PlantMaintenanceTransaction plantMaintenanceTransaction = new PlantMaintenanceTransaction();
				plantMaintenanceTransaction.setId(UUID.randomUUID());
				
				// Set mandatory fields with default values if missing
				plantMaintenanceTransaction.setDiscription(
					shutDownPlanDTO.getDiscription() != null ? shutDownPlanDTO.getDiscription() : "Default Description"
				);

				if (shutDownPlanDTO.getDurationInHrs() != null) {
				    plantMaintenanceTransaction.setDurationInMins((int) (shutDownPlanDTO.getDurationInHrs() * 60));
				} else {
				    plantMaintenanceTransaction.setDurationInMins(0);
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
				plantMaintenanceTransactionRepository.save(plantMaintenanceTransaction);
	
				// for (int i = 0; i < 2; i++) {

				// 	List<ShutDownPlanDTO> list = new ArrayList<>();
				// 	shutDownPlanDTO.setDiscription(shutDownPlanDTO.getDiscription());
				// 	slowdownPlanService.saveShutdownData(plantId, shutDownPlanDTOList);
				// }

				String description = shutDownPlanDTO.getDiscription();
				// if(shutDownPlanDTO.getVerticalName().equalsIgnoreCase("MEG")) {
					List<ShutDownPlanDTO> list = new ArrayList<>();
					shutDownPlanDTO.setDurationInMins(0);
					shutDownPlanDTO.setDiscription(description+" Ramp Up");
					shutDownPlanDTO.setProductId(UUID.fromString("A061E050-0281-421F-81C1-B136CE2ED3F3"));
					list.add(shutDownPlanDTO);
				    slowdownPlanService.saveShutdownData(plantId, list);

					List<ShutDownPlanDTO> list2 = new ArrayList<>();
					shutDownPlanDTO.setDiscription(description+" Ramp Down");
					shutDownPlanDTO.setProductId(UUID.fromString("A061E050-0281-421F-81C1-B136CE2ED3F3"));
					shutDownPlanDTO.setDurationInMins(0);
					list2.add(shutDownPlanDTO);
				    slowdownPlanService.saveShutdownData(plantId,list2);
				    
				    List<ShutDownPlanDTO> list3 = new ArrayList<>();
					shutDownPlanDTO.setDiscription(description+" Ramp Down");
					shutDownPlanDTO.setProductId(UUID.fromString("00DC05B1-9607-470E-A159-62497E0123E2"));
					shutDownPlanDTO.setDurationInMins(0);
					list3.add(shutDownPlanDTO);
				    slowdownPlanService.saveShutdownData(plantId,list3);
				    
				    List<ShutDownPlanDTO> list4 = new ArrayList<>();
					shutDownPlanDTO.setDiscription(description+" Ramp Up");
					shutDownPlanDTO.setProductId(UUID.fromString("00DC05B1-9607-470E-A159-62497E0123E2"));
					shutDownPlanDTO.setDurationInMins(0);
					list4.add(shutDownPlanDTO);
				    slowdownPlanService.saveShutdownData(plantId,list4);

				// }
			} else {
				// Updating an existing record

				try {
					Optional<PlantMaintenanceTransaction> plantMaintenance = 
						shutDownPlanRepository.findById(UUID.fromString(shutDownPlanDTO.getId()));
	
					if (plantMaintenance.isPresent()) {
						PlantMaintenanceTransaction plantMaintenanceTransaction = plantMaintenance.get();
						plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
						plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
						// if(shutDownPlanDTO.getDurationInMins()!=null){
						// 	plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins() * 60);
						// }else{
						// 	plantMaintenanceTransaction.setDurationInHrs(0d);
						// }
						
				if (shutDownPlanDTO.getDurationInHrs() != null) {
				    plantMaintenanceTransaction.setDurationInMins((int) (shutDownPlanDTO.getDurationInHrs() * 60));
				} else {
				    plantMaintenanceTransaction.setDurationInMins(0);
				}
						//plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins());
						plantMaintenanceTransaction.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth()+1);
						plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
						plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
						plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
	
						// Save updated record
						plantMaintenanceTransactionRepository.save(plantMaintenanceTransaction);
					} else {
						throw new RuntimeException("Record not found for ID: " + shutDownPlanDTO.getId());
					}
				} catch (IllegalArgumentException e) {
					throw new RuntimeException("Invalid ID format: " + shutDownPlanDTO.getId(), e);
				}
			}
		}
		return shutDownPlanDTOList;
	}
	
	@Override
	public List<ShutDownPlanDTO> editShutdownData(UUID plantMaintenanceTransactionId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		  for(ShutDownPlanDTO shutDownPlanDTO: shutDownPlanDTOList) {
			  Optional<PlantMaintenanceTransaction> plantMaintenance=	shutDownPlanRepository.findById(plantMaintenanceTransactionId);
			  PlantMaintenanceTransaction plantMaintenanceTransaction= plantMaintenance.get();
			  plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
			  plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins());
			  plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
			  plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
			  plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());  
			  plantMaintenanceTransactionRepository.save(plantMaintenanceTransaction);
		  }
		// TODO Auto-generated method stub
		return shutDownPlanDTOList;
	}

	@Override
	public PlantMaintenanceTransaction editShutDownPlanData(UUID plantMaintenanceTransactionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MonthWiseDataDTO> getMonthlyShutdownHours(String auditYear, UUID plantId) {
		List<MonthWiseDataDTO> monthWiseDataDTOList=new ArrayList<>();
		List<Object[]> results= shutDownPlanRepository.getMonthlyShutdownHours(auditYear,plantId);
		for (Object[] obj : results) {
			MonthWiseDataDTO monthWiseDataDTO=new MonthWiseDataDTO();
			monthWiseDataDTO.setMonthYear(obj[0].toString());
			monthWiseDataDTO.setProduct(obj[1].toString());
			monthWiseDataDTO.setTotalHours(Double.parseDouble(obj[2].toString()));
			monthWiseDataDTOList.add(monthWiseDataDTO);
		}
		return monthWiseDataDTOList;
	}

}
