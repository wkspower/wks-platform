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
import com.wks.caseengine.entity.PlantMaintenance;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantMaintenanceRepository;
import com.wks.caseengine.repository.PlantMaintenanceTransactionRepository;
import com.wks.caseengine.repository.SlowdownPlanRepository;

@Service
public class SlowdownPlanServiceImpl implements SlowdownPlanService {

	@Autowired
	private SlowdownPlanRepository slowdownPlanRepository;

	@Autowired
	private ShutDownPlanService shutDownPlanService;

	@Autowired
	private PlantMaintenanceRepository plantMaintenanceRepository;

	@Autowired
	private PlantMaintenanceTransactionRepository plantMaintenanceTransactionRepository;

	@Override
	public AOPMessageVM getPlans(UUID plantId, String type, String year) {
		AOPMessageVM response = new AOPMessageVM();
		List<Object[]> listOfSite = null;
		try {
			listOfSite = slowdownPlanRepository.getPlans(type, plantId.toString(), year);

			List<ShutDownPlanDTO> dtoList = new ArrayList<>();

			for (Object[] result : listOfSite) {
				ShutDownPlanDTO dto = new ShutDownPlanDTO();
				dto.setDiscription((String) result[0]);
				dto.setMaintStartDateTime((Date) result[1]);
				dto.setMaintEndDateTime((Date) result[2]);
				dto.setDurationInMins(result[3] != null ? ((Integer) result[3]) : null);
				if (result[3] != null) {
					int totalMinutes = (Integer) result[3];
					int hours = totalMinutes / 60;
					int minutes = totalMinutes % 60;
					double durationInHrs = hours + (minutes / 100.0);
					dto.setDurationInHrs(durationInHrs);
				}
				dto.setProduct((String) result[6]);
				// FOR ID : pmt.Id
				dto.setId(result[5] != null ? result[5].toString() : null);
				if ((String) result[7] != null) {
					dto.setRemark((String) result[7]);
				} else {
					dto.setRemark(null);
				}
				dto.setDisplayOrder(result[8] != null ? ((Integer) result[8]) : null);
				dto.setRate(result[9] != null ? ((Number) result[9]).doubleValue() : null); // Extract Rate

				dtoList.add(dto);
			}
			response.setCode(200);
			response.setMessage("Shutdown plans fetched successfully.");
			response.setData(dtoList);
		} catch (Exception e) {
			response.setCode(500);
			response.setMessage("Failed to get shutdown plans: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}

	@Override
	public AOPMessageVM savePlans(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		AOPMessageVM response = new AOPMessageVM();
		try {
			UUID plantMaintenanceId = shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId, "Slowdown");
			if (plantMaintenanceId == null) {
				UUID maintenanceTypesId = plantMaintenanceTransactionRepository.findIdByName("Slowdown");
				PlantMaintenance plantMaintenance = new PlantMaintenance();
				plantMaintenance.setMaintenanceText("Slowdown");
				plantMaintenance.setIsDefault(true);
				plantMaintenance.setPlantFkId(plantId);
				plantMaintenance.setMaintenanceTypeFkId(maintenanceTypesId);
				plantMaintenanceRepository.save(plantMaintenance);
				plantMaintenanceId = shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId, "Slowdown");
			}
			for (ShutDownPlanDTO shutDownPlanDTO : shutDownPlanDTOList) {

				if (shutDownPlanDTO.getId() == null || shutDownPlanDTO.getId().isEmpty()) {
					PlantMaintenanceTransaction plantMaintenanceTransaction = new PlantMaintenanceTransaction();
					plantMaintenanceTransaction.setId(UUID.randomUUID());
					plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
					if (shutDownPlanDTO.getDurationInHrs() != null) {
						plantMaintenanceTransaction
								.setDurationInMins((int) (Math.floor(shutDownPlanDTO.getDurationInHrs()) * 60)
										+ (int) Math.round((shutDownPlanDTO.getDurationInHrs()
												- Math.floor(shutDownPlanDTO.getDurationInHrs())) * 100));
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
					slowdownPlanRepository.save(plantMaintenanceTransaction);
				} else {

					Optional<PlantMaintenanceTransaction> plantMaintenance = slowdownPlanRepository
							.findById(UUID.fromString(shutDownPlanDTO.getId()));
					PlantMaintenanceTransaction plantMaintenanceTransaction = new PlantMaintenanceTransaction();
					plantMaintenanceTransaction.setId(UUID.fromString(shutDownPlanDTO.getId()));

					// Set mandatory fields with default values if missing
					plantMaintenanceTransaction.setDiscription(
							shutDownPlanDTO.getDiscription() != null ? shutDownPlanDTO.getDiscription()
									: "Default Description");

					if (shutDownPlanDTO.getDurationInHrs() != null) {
						plantMaintenanceTransaction
								.setDurationInMins((int) (Math.floor(shutDownPlanDTO.getDurationInHrs()) * 60)
										+ (int) Math.round((shutDownPlanDTO.getDurationInHrs()
												- Math.floor(shutDownPlanDTO.getDurationInHrs())) * 100));

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
					plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());

					plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());

					if (shutDownPlanDTO.getProductId() != null) {
						plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
					}

					plantMaintenanceTransaction.setAuditYear(shutDownPlanDTO.getAudityear());

					// Save new record
					slowdownPlanRepository.save(plantMaintenanceTransaction);

				}
				response.setCode(200);
				response.setMessage("Shutdown plans saved successfully.");
				response.setData(shutDownPlanDTOList);
			}
		} catch (Exception e) {
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
				Optional<PlantMaintenanceTransaction> optionalTransaction = slowdownPlanRepository
						.findById(transactionId);
				if (optionalTransaction.isPresent()) {
					PlantMaintenanceTransaction plantMaintenanceTransaction = optionalTransaction.get();
					plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
					plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
					plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
					plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());

					if (shutDownPlanDTO.getMaintStartDateTime() != null) {
						plantMaintenanceTransaction
								.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth() + 1);
					}
					plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
					plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
					// Save entity
					slowdownPlanRepository.save(plantMaintenanceTransaction);
				} else {
					response.setCode(404);
					response.setMessage("Transaction ID not found: " + transactionId);
					response.setData(null);
					return response;
				}
			}
			// TODO Auto-generated method stub
			response.setCode(200);
			response.setMessage("Shutdown plans updated successfully.");
			response.setData(shutDownPlanDTOList);
		} catch (Exception e) {
			e.printStackTrace();
			response.setCode(500);
			response.setMessage("Failed to update shutdown plans: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}
}
