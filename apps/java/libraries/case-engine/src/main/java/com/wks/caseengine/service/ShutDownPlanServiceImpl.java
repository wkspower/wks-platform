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
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantMaintenanceRepository;
import com.wks.caseengine.repository.PlantMaintenanceTransactionRepository;
import com.wks.caseengine.repository.ShutDownPlanRepository;

@Service
public class ShutDownPlanServiceImpl implements ShutDownPlanService {

	@Autowired
	private ShutDownPlanRepository shutDownPlanRepository;

	@Lazy // Add this annotation here
	@Autowired
	private SlowdownPlanService slowdownPlanService;

	@Autowired
	private PlantMaintenanceRepository plantMaintenanceRepository;

	@Autowired
	private PlantMaintenanceTransactionRepository plantMaintenanceTransactionRepository;

	@Autowired
	private PlantsService plantsService;

	@Override
	public List<ShutDownPlanDTO> findMaintenanceDetailsByPlantIdAndType(UUID plantId, String maintenanceTypeName,
			String year) {
		List<ShutDownPlanDTO> dtoList = new ArrayList<>();
		List<Object[]> listOfSite = shutDownPlanRepository.findMaintenanceDetailsByPlantIdAndType(maintenanceTypeName,
				plantId.toString(), year);
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
	public UUID findIdByPlantIdAndMaintenanceTypeName(UUID plantId, String maintenanceTypeName) {
		return shutDownPlanRepository.findIdByPlantIdAndMaintenanceTypeName(plantId, maintenanceTypeName);
	}

	@Override
	public void deletePlanData(UUID plantMaintenanceTransactionId) {
		Optional<PlantMaintenanceTransaction> plantMaintenanceTransaction = plantMaintenanceTransactionRepository
				.findById(plantMaintenanceTransactionId);
		plantMaintenanceTransactionRepository.delete(plantMaintenanceTransaction.get());
	}

	@Override
	public AOPMessageVM saveShutdownPlantData(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<PlantMaintenanceTransaction> PlantMaintenanceTransactionList = new ArrayList<>();
		try {
			UUID plantMaintenanceId = findIdByPlantIdAndMaintenanceTypeName(plantId, "Shutdown");
			if (plantMaintenanceId == null) {
				UUID maintenanceTypesId = plantMaintenanceTransactionRepository.findIdByName("Shutdown");
				PlantMaintenance plantMaintenance = new PlantMaintenance();
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
					plantMaintenanceTransaction
							.setDiscription(shutDownPlanDTO.getDiscription() != null ? shutDownPlanDTO.getDiscription()
									: "Default Description");

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
					plantMaintenanceTransactionRepository.save(plantMaintenanceTransaction);
					PlantMaintenanceTransactionList.add(plantMaintenanceTransaction);
					String verticalName = plantsService.findVerticalNameByPlantId(plantId);
					System.out.println("verticalName" + verticalName);
					String description = shutDownPlanDTO.getDiscription();
					if (verticalName.equalsIgnoreCase("MEG")) {
						shutDownPlanDTO.setMaintEndDateTime(shutDownPlanDTO.getMaintStartDateTime());
						List<ShutDownPlanDTO> list = new ArrayList<>();
						shutDownPlanDTO.setDurationInHrs(0.00);
						shutDownPlanDTO.setDurationInMins(0);
						shutDownPlanDTO.setDiscription(description + " Ramp Up");
						shutDownPlanDTO.setProductId(
								plantMaintenanceTransactionRepository.findIdByNameAndPlantFkId("EO", plantId));
						list.add(shutDownPlanDTO);
						slowdownPlanService.saveShutdownData(plantId, list);

						List<ShutDownPlanDTO> list2 = new ArrayList<>();
						shutDownPlanDTO.setDiscription(description + " Ramp Down");
						shutDownPlanDTO.setProductId(
								plantMaintenanceTransactionRepository.findIdByNameAndPlantFkId("EO", plantId));
						shutDownPlanDTO.setDurationInHrs(0.00);
						shutDownPlanDTO.setDurationInMins(0);
						list2.add(shutDownPlanDTO);
						slowdownPlanService.saveShutdownData(plantId, list2);

						List<ShutDownPlanDTO> list3 = new ArrayList<>();
						shutDownPlanDTO.setDiscription(description + " Ramp Down");
						shutDownPlanDTO.setProductId(
								plantMaintenanceTransactionRepository.findIdByNameAndPlantFkId("EOE", plantId));
						shutDownPlanDTO.setDurationInHrs(0.00);
						shutDownPlanDTO.setDurationInMins(0);
						list3.add(shutDownPlanDTO);
						slowdownPlanService.saveShutdownData(plantId, list3);

						List<ShutDownPlanDTO> list4 = new ArrayList<>();
						shutDownPlanDTO.setDiscription(description + " Ramp Up");
						shutDownPlanDTO.setProductId(
								plantMaintenanceTransactionRepository.findIdByNameAndPlantFkId("EOE", plantId));
						shutDownPlanDTO.setDurationInHrs(0.00);
						shutDownPlanDTO.setDurationInMins(0);
						list4.add(shutDownPlanDTO);
						slowdownPlanService.saveShutdownData(plantId, list4);

					}
				} else {
					// Updating an existing record

					try {
						Optional<PlantMaintenanceTransaction> plantMaintenance = shutDownPlanRepository
								.findById(UUID.fromString(shutDownPlanDTO.getId()));

						if (plantMaintenance.isPresent()) {
							PlantMaintenanceTransaction plantMaintenanceTransaction = plantMaintenance.get();
							plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
							plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());

							if (shutDownPlanDTO.getDurationInHrs() != null) {
								plantMaintenanceTransaction
										.setDurationInMins((int) (Math.floor(shutDownPlanDTO.getDurationInHrs()) * 60)
												+ (int) Math.round((shutDownPlanDTO.getDurationInHrs()
														- Math.floor(shutDownPlanDTO.getDurationInHrs())) * 100));

							} else {
								plantMaintenanceTransaction.setDurationInMins(0);
							}
							// plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins());
							plantMaintenanceTransaction
									.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth() + 1);
							plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
							plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
							plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());

							// Save updated record
							plantMaintenanceTransactionRepository.save(plantMaintenanceTransaction);
							PlantMaintenanceTransactionList.add(plantMaintenanceTransaction);
						} else {
							throw new RuntimeException("Record not found for ID: " + shutDownPlanDTO.getId());
						}
					} catch (IllegalArgumentException e) {
						throw new RuntimeException("Invalid ID format: " + shutDownPlanDTO.getId(), e);
					}
				}
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data saved successfully");
			aopMessageVM.setData(PlantMaintenanceTransactionList);
			return aopMessageVM;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	@Override
	public List<ShutDownPlanDTO> editShutdownData(UUID plantMaintenanceTransactionId,
			List<ShutDownPlanDTO> shutDownPlanDTOList) {
		for (ShutDownPlanDTO shutDownPlanDTO : shutDownPlanDTOList) {
			Optional<PlantMaintenanceTransaction> plantMaintenance = shutDownPlanRepository
					.findById(plantMaintenanceTransactionId);
			PlantMaintenanceTransaction plantMaintenanceTransaction = plantMaintenance.get();
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
		List<MonthWiseDataDTO> monthWiseDataDTOList = new ArrayList<>();
		List<Object[]> results = shutDownPlanRepository.getMonthlyShutdownHours(auditYear, plantId);
		for (Object[] obj : results) {
			MonthWiseDataDTO monthWiseDataDTO = new MonthWiseDataDTO();
			monthWiseDataDTO.setMonthYear(obj[0].toString());
			monthWiseDataDTO.setProduct(obj[1].toString());
			monthWiseDataDTO.setTotalHours(Double.parseDouble(obj[2].toString()));
			monthWiseDataDTOList.add(monthWiseDataDTO);
		}
		return monthWiseDataDTOList;
	}

}
