package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.MonthWiseDataDTO;
import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.PlantMaintenance;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantMaintenanceRepository;
import com.wks.caseengine.repository.PlantMaintenanceTransactionRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.ShutDownPlanRepository;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.temporal.ChronoUnit;

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

	@Autowired
	private NormParametersRepository normParametersRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;

	
	
	@Override
	public List<ShutDownPlanDTO> findMaintenanceDetailsByPlantIdAndType(UUID plantId, String maintenanceTypeName,
			String year) {
		List<ShutDownPlanDTO> dtoList = new ArrayList<>();
		try {
			List<Object[]> listOfSite = shutDownPlanRepository
					.findMaintenanceDetailsByPlantIdAndType(maintenanceTypeName, plantId.toString(), year);
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
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
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

	@Transactional
	@Override
	public void deletePlanData(UUID plantMaintenanceTransactionId, UUID plantId) {
	    try {
	        Optional<PlantMaintenanceTransaction> plantMaintenanceTransactionOpt =
	                plantMaintenanceTransactionRepository.findById(plantMaintenanceTransactionId);

	        if (plantMaintenanceTransactionOpt.isEmpty()) {
	            throw new RuntimeException("PlantMaintenanceTransaction not found for ID: " + plantMaintenanceTransactionId);
	        }

	        PlantMaintenanceTransaction plantMaintenanceTransaction = plantMaintenanceTransactionOpt.get();

	        List<NormAttributeTransactions> normAttributeTransactionsList =
	                normAttributeTransactionsRepository.findByMaintenanceId(plantMaintenanceTransactionId);

	        if (normAttributeTransactionsList != null && !normAttributeTransactionsList.isEmpty()) {
	            for (NormAttributeTransactions normAttributeTransaction : normAttributeTransactionsList) {
	                if (normAttributeTransaction != null) {
	                    normAttributeTransactionsRepository.delete(normAttributeTransaction);
	                }
	            }
	        }

	        plantMaintenanceTransactionRepository.delete(plantMaintenanceTransaction);

	        List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("slowdown-plan");
	        if (screenMappingList != null && !screenMappingList.isEmpty()) {
	            for (ScreenMapping screenMapping : screenMappingList) {
	                if (screenMapping != null && screenMapping.getCalculationScreen() != null) {
	                    AopCalculation aopCalculation = new AopCalculation();
	                    aopCalculation.setAopYear(plantMaintenanceTransaction.getAuditYear());
	                    aopCalculation.setIsChanged(true);
	                    aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
	                    aopCalculation.setPlantId(plantId);
	                    aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
	                    aopCalculationRepository.save(aopCalculation);
	                }
	            }
	        }

	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new RuntimeException("Failed to delete data", ex);
	    }
	}

	@Transactional
	@Override
	public void deleteShutPlanData(UUID plantMaintenanceTransactionId, UUID plantId) {
	    try {
	        Optional<PlantMaintenanceTransaction> plantMaintenanceTransactionOpt =
	                plantMaintenanceTransactionRepository.findById(plantMaintenanceTransactionId);
	        // Delete dependent NormAttributeTransactions first
	        List<NormAttributeTransactions> normAttributeTransactionsList =
	                normAttributeTransactionsRepository.findByMaintenanceId(plantMaintenanceTransactionId);

	        if (normAttributeTransactionsList != null && !normAttributeTransactionsList.isEmpty()) {
	            for (NormAttributeTransactions normAttr : normAttributeTransactionsList) {
	                if (normAttr != null) {
	                    normAttributeTransactionsRepository.delete(normAttr);
	                }
	            }
	            normAttributeTransactionsRepository.flush(); // Ensure delete is committed before parent delete
	        }

	        if (plantMaintenanceTransactionOpt.isEmpty()) {
	            throw new RuntimeException("PlantMaintenanceTransaction not found for ID: " + plantMaintenanceTransactionId);
	        }

	        PlantMaintenanceTransaction plantMaintenanceTransaction = plantMaintenanceTransactionOpt.get();
	        String year = plantMaintenanceTransaction.getAuditYear();

	        String verticalName = plantsService.findVerticalNameByPlantId(plantId);

	        if ("MEG".equalsIgnoreCase(verticalName)) {
	            UUID normparameterId1 = normParametersRepository.findNormParameterIdByNameAndPlant("EO", plantId);
	            if (normparameterId1 != null) {
	            	
	            	List<UUID> ids= plantMaintenanceTransactionRepository.findRampActivityIdsByNormAndName(
	                        normparameterId1, plantMaintenanceTransaction.getId().toString());
	            	for(UUID id:ids) {
	            		// Delete dependent NormAttributeTransactions first
	        	        List<NormAttributeTransactions> normAttributeTransactionsLists =
	        	                normAttributeTransactionsRepository.findByMaintenanceId(id);

	        	        if (normAttributeTransactionsLists != null && !normAttributeTransactionsLists.isEmpty()) {
	        	            for (NormAttributeTransactions normAttr : normAttributeTransactionsLists) {
	        	                if (normAttr != null) {
	        	                    normAttributeTransactionsRepository.delete(normAttr);
	        	                }
	        	            }
	        	            normAttributeTransactionsRepository.flush(); // Ensure delete is committed before parent delete
	        	        }

	            	}
	                plantMaintenanceTransactionRepository.deleteRampActivitiesByNormAndDate(
	                        normparameterId1, plantMaintenanceTransaction.getId().toString());
	            }

	            UUID normparameterId2 = normParametersRepository.findNormParameterIdByNameAndPlant("EOE", plantId);
	            if (normparameterId2 != null) {
	            	List<UUID> ids= plantMaintenanceTransactionRepository.findRampActivityIdsByNormAndName(
	            			normparameterId2, plantMaintenanceTransaction.getId().toString());
	            	for(UUID id:ids) {
	            		// Delete dependent NormAttributeTransactions first
	        	        List<NormAttributeTransactions> normAttributeTransactionsLists =
	        	                normAttributeTransactionsRepository.findByMaintenanceId(id);

	        	        if (normAttributeTransactionsLists != null && !normAttributeTransactionsLists.isEmpty()) {
	        	            for (NormAttributeTransactions normAttr : normAttributeTransactionsLists) {
	        	                if (normAttr != null) {
	        	                    normAttributeTransactionsRepository.delete(normAttr);
	        	                }
	        	            }
	        	            normAttributeTransactionsRepository.flush(); // Ensure delete is committed before parent delete
	        	        }

	            	}

	                plantMaintenanceTransactionRepository.deleteRampActivitiesByNormAndDate(
	                        normparameterId2, plantMaintenanceTransaction.getId().toString());
	            }
	        }

	        // Now delete the parent entity
	        plantMaintenanceTransactionRepository.delete(plantMaintenanceTransaction);

	        // Add AOP Calculation
	        List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("shutdown-plan");
	        if (screenMappingList != null && !screenMappingList.isEmpty()) {
	            for (ScreenMapping screenMapping : screenMappingList) {
	                if (screenMapping.getCalculationScreen() != null) {
	                    AopCalculation aopCalculation = new AopCalculation();
	                    aopCalculation.setAopYear(year);
	                    aopCalculation.setIsChanged(true);
	                    aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
	                    aopCalculation.setPlantId(plantId);
	                    aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
	                    aopCalculationRepository.save(aopCalculation);
	                }
	            }
	        }
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	        throw new RuntimeException("Failed to delete data", ex);
	    }
	}

	@Override
	public List<ShutDownPlanDTO> saveShutdownPlantData(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		String year=null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String userId = authentication.getName();	
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
				year=shutDownPlanDTO.getAudityear();
				if (shutDownPlanDTO.getId() == null || shutDownPlanDTO.getId().isEmpty()) {
					// Creating a new record
					PlantMaintenanceTransaction plantMaintenanceTransaction = new PlantMaintenanceTransaction();
					plantMaintenanceTransaction.setId(UUID.randomUUID());

					// Set mandatory fields with default values if missing
					plantMaintenanceTransaction
							.setDiscription(shutDownPlanDTO.getDiscription() != null ? shutDownPlanDTO.getDiscription()
									: "Default Description");

					if (shutDownPlanDTO.getDurationInHrs() != null) {
						// plantMaintenanceTransaction.setDurationInMins((int)
						// (shutDownPlanDTO.getDurationInHrs() * 60));
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
					plantMaintenanceTransaction.setUser(userId);
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

					String verticalName = plantsService.findVerticalNameByPlantId(plantId);
					System.out.println("verticalName" + verticalName);
					String description = shutDownPlanDTO.getDiscription();
					if (verticalName.equalsIgnoreCase("MEG")) {
						shutDownPlanDTO.setCreatedOn(plantMaintenanceTransaction.getCreatedOn());
						shutDownPlanDTO.setMaintEndDateTime(shutDownPlanDTO.getMaintStartDateTime());
						shutDownPlanDTO
								.setPlantMaintenanceTransactionName(plantMaintenanceTransaction.getId().toString());
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
								plantMaintenanceTransactionRepository.findIdByNameAndPlantFkId("EOE", plantId));
						shutDownPlanDTO.setDurationInHrs(0.00);
						shutDownPlanDTO.setDurationInMins(0);
						list2.add(shutDownPlanDTO);
						slowdownPlanService.saveShutdownData(plantId, list2);
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
								// plantMaintenanceTransaction.setDurationInMins((int)
								// (shutDownPlanDTO.getDurationInHrs() * 60));
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
						} else {
							throw new RuntimeException("Record not found for ID: " + shutDownPlanDTO.getId());
						}
					} catch (IllegalArgumentException e) {
						throw new RuntimeException("Invalid ID format: " + shutDownPlanDTO.getId(), e);
					}
				}
			}
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("shutdown-plan");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(plantId);
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			return shutDownPlanDTOList;
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
		try {
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
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid data format", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

		
	

}
