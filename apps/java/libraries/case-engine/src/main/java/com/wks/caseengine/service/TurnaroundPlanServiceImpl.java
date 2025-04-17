package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.entity.PlantMaintenance;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantMaintenanceRepository;
import com.wks.caseengine.repository.PlantMaintenanceTransactionRepository;
import com.wks.caseengine.repository.TurnaroundPlanRepository;

@Service
public class TurnaroundPlanServiceImpl implements TurnaroundPlanService {

	@Autowired
	private TurnaroundPlanRepository turnaroundPlanRepository;

	@Autowired
	private ShutDownPlanService shutDownPlanService;

	@Autowired
	private PlantMaintenanceRepository plantMaintenanceRepository;

	@Autowired
	private PlantMaintenanceTransactionRepository plantMaintenanceTransactionRepository;

	@Override
	public AOPMessageVM getPlans(UUID plantId, String type, String year) {
		AOPMessageVM response = new AOPMessageVM();
		try {
			List<Object[]> listOfSite = turnaroundPlanRepository.getPlans(type, plantId.toString(), year);
			List<ShutDownPlanDTO> dtoList = new ArrayList<>();
			for (Object[] result : listOfSite) {
				ShutDownPlanDTO dto = new ShutDownPlanDTO();
				dto.setDiscription((String) result[0]);
				dto.setMaintStartDateTime((Date) result[1]);
				dto.setMaintEndDateTime((Date) result[2]);
				dto.setDurationInMins(result[3] != null ? ((Integer) result[3]) : null);
				if (result[3] != null) {
					double durationInHrs = ((Integer) result[3]) / 60.0;
					dto.setDurationInHrs(durationInHrs);
				}
				dto.setProduct((String) result[6]);
				dto.setId(result[5] != null ? result[5].toString() : null);
				dto.setRemark(result[7] != null ? (String) result[7] : null);
				dto.setDisplayOrder(result[8] != null ? ((Integer) result[8]) : null);
				dtoList.add(dto);
			}
			response.setCode(200);
			response.setMessage("Shutdown plans fetched successfully.");
			response.setData(dtoList);
		} catch (Exception e) {
			System.err.println("Error occurred while getting plans: " + e.getMessage());
			e.printStackTrace();
			response.setCode(500);
			response.setMessage("Failed to get plans: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}

	@Override
	public AOPMessageVM savePlans(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		AOPMessageVM response = new AOPMessageVM();
		try {
			UUID plantMaintenanceId = shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId, "TA_Plan");
			if (plantMaintenanceId == null) {
				UUID maintenanceTypesId = plantMaintenanceTransactionRepository.findIdByName("TA_Plan");
				PlantMaintenance plantMaintenance = new PlantMaintenance();
				plantMaintenance.setMaintenanceText("TA_Plan");
				plantMaintenance.setIsDefault(true);
				plantMaintenance.setPlantFkId(plantId);
				plantMaintenance.setMaintenanceTypeFkId(maintenanceTypesId);
				plantMaintenanceRepository.save(plantMaintenance);
				plantMaintenanceId = shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId, "TA_Plan");
			}
			for (ShutDownPlanDTO shutDownPlanDTO : shutDownPlanDTOList) {

				if (shutDownPlanDTO.getId() == null || shutDownPlanDTO.getId().isEmpty()) {
					PlantMaintenanceTransaction plantMaintenanceTransaction = new PlantMaintenanceTransaction();
					plantMaintenanceTransaction.setId(UUID.randomUUID());
					plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
					if (shutDownPlanDTO.getDurationInHrs() != null) {
						plantMaintenanceTransaction.setDurationInMins((int) (shutDownPlanDTO.getDurationInHrs() * 60));
					} else {
						plantMaintenanceTransaction.setDurationInMins(0);
					}
					plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
					plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
					plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);
					if (shutDownPlanDTO.getMaintStartDateTime() != null) {
						plantMaintenanceTransaction
								.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth() + 1);
					}

					plantMaintenanceTransaction.setCreatedOn(new Date());
					plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
					plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
					plantMaintenanceTransaction.setName("Default Name");
					plantMaintenanceTransaction.setVersion("V1");
					plantMaintenanceTransaction.setUser("system");
					if (shutDownPlanDTO.getProductId() != null) {
						plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
					}
					plantMaintenanceTransaction.setAuditYear(shutDownPlanDTO.getAudityear());
					turnaroundPlanRepository.save(plantMaintenanceTransaction);

				}

				else {

					Optional<PlantMaintenanceTransaction> plantMaintenance = turnaroundPlanRepository
							.findById(UUID.fromString(shutDownPlanDTO.getId()));
					PlantMaintenanceTransaction plantMaintenanceTransaction = new PlantMaintenanceTransaction();
					plantMaintenanceTransaction.setId(UUID.randomUUID());

					// Set mandatory fields with default values if missing
					plantMaintenanceTransaction.setDiscription(
							shutDownPlanDTO.getDiscription() != null ? shutDownPlanDTO.getDiscription()
									: "Default Description");

					if (shutDownPlanDTO.getDurationInHrs() != null) {
						plantMaintenanceTransaction.setDurationInMins((int) (shutDownPlanDTO.getDurationInHrs() * 60));
					} else {
						plantMaintenanceTransaction.setDurationInMins(0);
					}
					// plantMaintenanceTransaction.setDurationInMins(
					// shutDownPlanDTO.getDurationInMins() != null ?
					// shutDownPlanDTO.getDurationInMins().intValue() : 0
					// );
					plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
					plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
					plantMaintenanceTransaction
							.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth() + 1);
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
					turnaroundPlanRepository.save(plantMaintenanceTransaction);

				}
				response.setCode(200);
				response.setMessage("Shutdown plans saved successfully.");
				response.setData(shutDownPlanDTOList);
			}
			// TODO Auto-generated method stub
		} catch (Exception e) {
			System.err.println("Error occurred while saving plans: " + e.getMessage());
			e.printStackTrace();
			response.setCode(500);
			response.setMessage("Failed to save shutdown plans: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}

	@Override
	public AOPMessageVM updatePlans(UUID transactionId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		AOPMessageVM response = new AOPMessageVM();
		try {
			for (ShutDownPlanDTO shutDownPlanDTO : shutDownPlanDTOList) {
				Optional<PlantMaintenanceTransaction> plantMaintenance = turnaroundPlanRepository
						.findById(transactionId);
				PlantMaintenanceTransaction plantMaintenanceTransaction = plantMaintenance.get();
				plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
				plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins().intValue());
				plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
				plantMaintenanceTransaction.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth() + 1);
				plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
				plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
				plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
				turnaroundPlanRepository.save(plantMaintenanceTransaction);
			}
			response.setCode(200);
			response.setMessage("Shutdown plan(s) updated successfully.");
			response.setData(shutDownPlanDTOList);
		} catch (Exception e) {
			System.err.println("Error occurred while updating plans: " + e.getMessage());
			e.printStackTrace();
			response.setCode(500);
			response.setMessage("Failed to update shutdown plan(s): " + e.getMessage());
			response.setData(null);
		}
		return response;
	}
}
