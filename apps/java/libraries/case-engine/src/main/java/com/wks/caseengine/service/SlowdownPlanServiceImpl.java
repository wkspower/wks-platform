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
	public List<ShutDownPlanDTO> findSlowdownDetailsByPlantIdAndType(UUID plantId, String maintenanceTypeName,
			String year) {

		List<Object[]> listOfSite = null;
		try {
			listOfSite = slowdownPlanRepository.findSlowdownPlanDetailsByPlantIdAndType(maintenanceTypeName,
					plantId.toString(), year);
		} catch (Exception e) {
			e.printStackTrace();
		}

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
		// TODO Auto-generated method stub
		return dtoList;
	}

	@Override
	public List<ShutDownPlanDTO> saveShutdownData(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
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
				System.out.println("plantMaintenanceTransaction.getMaintForMonth()"+plantMaintenanceTransaction.getMaintForMonth());
				
				System.out.println("shutDownPlanDTO.getCreatedOn()" + shutDownPlanDTO.getCreatedOn());

				plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
				plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
				//plantMaintenanceTransaction.setName("Default Name");
				plantMaintenanceTransaction.setVersion("V1");
				plantMaintenanceTransaction.setUser("system");
				if (shutDownPlanDTO.getProductId() != null) {
					plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
				}
				plantMaintenanceTransaction.setAuditYear(shutDownPlanDTO.getAudityear());
				if (shutDownPlanDTO.getCreatedOn() == null) {
					plantMaintenanceTransaction.setCreatedOn(new Date());
				} else {
					plantMaintenanceTransaction.setCreatedOn(shutDownPlanDTO.getCreatedOn());
					plantMaintenanceTransaction.setName(shutDownPlanDTO.getPlantMaintenanceTransactionName());
				}
				slowdownPlanRepository.save(plantMaintenanceTransaction);
			} else {
				
				Optional<PlantMaintenanceTransaction> plantMaintenance = slowdownPlanRepository
						.findById(UUID.fromString(shutDownPlanDTO.getId()));
				PlantMaintenanceTransaction plantMaintenanceTransaction = new PlantMaintenanceTransaction();
				plantMaintenanceTransaction.setId(UUID.fromString(shutDownPlanDTO.getId()));
				System.out.println("At start name is:"+plantMaintenanceTransaction.getName());
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
				plantMaintenanceTransaction.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth() + 1);
				System.out.println("plantMaintenanceTransaction.getMaintForMonth()"+plantMaintenanceTransaction.getMaintForMonth());
				plantMaintenanceTransaction.setUser("system");
				//plantMaintenanceTransaction.setName("Default Name");
				plantMaintenanceTransaction.setVersion("V1");
				System.out.println("shutDownPlanDTO.getCreatedOn()" + shutDownPlanDTO.getCreatedOn());
				if (shutDownPlanDTO.getCreatedOn() == null) {
					plantMaintenanceTransaction.setCreatedOn(new Date());
				} else {
					plantMaintenanceTransaction.setCreatedOn(shutDownPlanDTO.getCreatedOn());
					//plantMaintenanceTransaction.setName(shutDownPlanDTO.getPlantMaintenanceTransactionName());
				}
				plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);
				plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());

				plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());

				if (shutDownPlanDTO.getProductId() != null) {
					plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
				}

				plantMaintenanceTransaction.setAuditYear(shutDownPlanDTO.getAudityear());
				System.out.println("At end name is:"+plantMaintenanceTransaction.getName());
				// Save new record
				slowdownPlanRepository.save(plantMaintenanceTransaction);

			}
		}
		return shutDownPlanDTOList;

	}

	@Override
	public List<ShutDownPlanDTO> editShutdownData(UUID plantMaintenanceTransactionId,
			List<ShutDownPlanDTO> shutDownPlanDTOList) {
		for (ShutDownPlanDTO shutDownPlanDTO : shutDownPlanDTOList) {
			Optional<PlantMaintenanceTransaction> plantMaintenance = slowdownPlanRepository
					.findById(plantMaintenanceTransactionId);
			PlantMaintenanceTransaction plantMaintenanceTransaction = plantMaintenance.get();
			plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
			plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
			plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
			plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());

			if (shutDownPlanDTO.getMaintStartDateTime() != null) {
				plantMaintenanceTransaction.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth() + 1);
			}
			System.out.println("plantMaintenanceTransaction.getMaintForMonth()"+plantMaintenanceTransaction.getMaintForMonth());
			plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
			plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
			// Save entity
			slowdownPlanRepository.save(plantMaintenanceTransaction);
		}
		// TODO Auto-generated method stub
		return shutDownPlanDTOList;
	}

}
