package com.wks.caseengine.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import java.util.*;
import java.util.regex.*;
import java.sql.*;
import java.time.Month;
import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.PlantMaintenance;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.PlantMaintenanceRepository;
import com.wks.caseengine.repository.PlantMaintenanceTransactionRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SlowdownPlanRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
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
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private PlantsRepository plantsRepository;
	
	@Autowired
	private VerticalsRepository verticalRepository;


	@Override
	public List<ShutDownPlanDTO> findSlowdownDetailsByPlantIdAndType(UUID plantId, String maintenanceTypeName,
			String year) {
		try {

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
				dto.setProductName(result[10] != null ? result[10].toString() : null);
				if(dto.getProductName()!=null && dto.getProductName().equalsIgnoreCase("EO")) {
					dto.setType("ramp-up");
				}
				if(dto.getProductName()!=null && dto.getProductName().equalsIgnoreCase("EOE")) {
					dto.setType("ramp-down");
				}
				dto.setRateEO(result[11] != null ? ((Number) result[11]).doubleValue() : null);
				dto.setRateEOE(result[12] != null ? ((Number) result[12]).doubleValue() : null);
				dtoList.add(dto);
			}
			// TODO Auto-generated method stub
			return dtoList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid data format", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public List<ShutDownPlanDTO> saveShutdownData(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		String year=null;
			
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
				year=shutDownPlanDTO.getAudityear();
				PlantMaintenanceTransaction plantMaintenanceTransaction =null;
				if (shutDownPlanDTO.getId() == null || shutDownPlanDTO.getId().isEmpty()) {
					plantMaintenanceTransaction = new PlantMaintenanceTransaction();
					plantMaintenanceTransaction.setId(UUID.randomUUID());
					
				} else {

					 plantMaintenanceTransaction = slowdownPlanRepository
							.findById(UUID.fromString(shutDownPlanDTO.getId())).get();
					
				}
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

					plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
					plantMaintenanceTransaction.setRateEO(shutDownPlanDTO.getRateEO());
					plantMaintenanceTransaction.setRateEOE(shutDownPlanDTO.getRateEOE());
					plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
					// plantMaintenanceTransaction.setName("Default Name");
					plantMaintenanceTransaction.setVersion("V1");
					plantMaintenanceTransaction.setUser(Utility.getUserName());
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
			}
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("slowdown-plan");
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
	public List<ShutDownPlanDTO> saveRampUpData(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		String year=null;
			
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
				year=shutDownPlanDTO.getAudityear();
				PlantMaintenanceTransaction plantMaintenanceTransaction =null;
				if (shutDownPlanDTO.getId() == null || shutDownPlanDTO.getId().isEmpty()) {
					plantMaintenanceTransaction = new PlantMaintenanceTransaction();
					plantMaintenanceTransaction.setId(UUID.randomUUID());
					
				} else {

					 plantMaintenanceTransaction = slowdownPlanRepository
							.findById(UUID.fromString(shutDownPlanDTO.getId())).get();
					
				}
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
					plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintEndDateTime());
					plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);
					if (shutDownPlanDTO.getMaintStartDateTime() != null) {
						plantMaintenanceTransaction
								.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth() + 1);
					}

					plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
					plantMaintenanceTransaction.setRateEO(shutDownPlanDTO.getRateEO());
					plantMaintenanceTransaction.setRateEOE(shutDownPlanDTO.getRateEOE());
					plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
					// plantMaintenanceTransaction.setName("Default Name");
					plantMaintenanceTransaction.setVersion("V1");
					plantMaintenanceTransaction.setUser(Utility.getUserName());
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
			}
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("slowdown-plan");
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
	public List<ShutDownPlanDTO> saveRampDownData(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		String year=null;
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
				year=shutDownPlanDTO.getAudityear();
				PlantMaintenanceTransaction plantMaintenanceTransaction =null;
				if (shutDownPlanDTO.getId() == null || shutDownPlanDTO.getId().isEmpty()) {
					plantMaintenanceTransaction = new PlantMaintenanceTransaction();
					plantMaintenanceTransaction.setId(UUID.randomUUID());
					
				} else {

					 plantMaintenanceTransaction = slowdownPlanRepository
							.findById(UUID.fromString(shutDownPlanDTO.getId())).get();
					
				}
				plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
					if (shutDownPlanDTO.getDurationInHrs() != null) {
						plantMaintenanceTransaction
								.setDurationInMins((int) (Math.floor(shutDownPlanDTO.getDurationInHrs()) * 60)
										+ (int) Math.round((shutDownPlanDTO.getDurationInHrs()
												- Math.floor(shutDownPlanDTO.getDurationInHrs())) * 100));
					} else {
						plantMaintenanceTransaction.setDurationInMins(0);
					}

					plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintStartDateTime());
					plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
					plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);
					if (shutDownPlanDTO.getMaintStartDateTime() != null) {
						plantMaintenanceTransaction
								.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth() + 1);
					}

					plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
					plantMaintenanceTransaction.setRateEO(shutDownPlanDTO.getRateEO());
					plantMaintenanceTransaction.setRateEOE(shutDownPlanDTO.getRateEOE());
					plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
					// plantMaintenanceTransaction.setName("Default Name");
					plantMaintenanceTransaction.setVersion("V1");
					plantMaintenanceTransaction.setUser(Utility.getUserName());
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
			}
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("slowdown-plan");
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
			System.out.println(
					"plantMaintenanceTransaction.getMaintForMonth()" + plantMaintenanceTransaction.getMaintForMonth());
			plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
			plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
			// Save entity
			slowdownPlanRepository.save(plantMaintenanceTransaction);
		}
		// TODO Auto-generated method stub
		return shutDownPlanDTOList;
	}

	@Override
	public AOPMessageVM saveSlowdownConfigurationData(String plantId, String year,
			List<NormAttributeTransactionsDTO> normAttributeTransactionsDTOList) {
		
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<NormAttributeTransactions> normAttributeTransactionsList = new ArrayList<>();
		try {
			for(NormAttributeTransactionsDTO normAttributeTransactionsDTO:normAttributeTransactionsDTOList) {
				String rawDesc = normAttributeTransactionsDTO.getDescription();
				int month=extractMonthNumber(rawDesc);
				String cleanDesc = stripTrailingSuffix(rawDesc);
				UUID maintenanceId=plantMaintenanceTransactionRepository.findTransactionIdByDynamicParams("Slowdown",year,UUID.fromString(plantId),cleanDesc);
				if(maintenanceId==null) {
					throw new RuntimeException("No Maintenance Id found with "+normAttributeTransactionsDTO.getDescription());
				}
				
				//UUID maintenanceId=plantMaintenanceTransactionRepository.findIdByNormIdAndDiscription(normAttributeTransactionsDTO.getDescription(),normAttributeTransactionsDTO.getNormParameterFKId());
				normAttributeTransactionsDTO.setMaintenanceId(maintenanceId);
				List<NormAttributeTransactions> existingList = normAttributeTransactionsRepository
					    .findByMaintenanceIdAndNormParameterFKIdAndAuditYear(
					        normAttributeTransactionsDTO.getMaintenanceId(),
					        normAttributeTransactionsDTO.getNormParameterFKId(),
					        year,
					        month
					    );

					if (existingList != null && !existingList.isEmpty()) {
					    for (NormAttributeTransactions existing : existingList) {
					        if (!Objects.equals(existing.getAttributeValue(), normAttributeTransactionsDTO.getAttributeValue())) {
					            existing.setAttributeValue(normAttributeTransactionsDTO.getAttributeValue());
					            existing.setModifiedOn(new Date());
					            normAttributeTransactionsList.add(normAttributeTransactionsRepository.save(existing));
					        }
					    }
					} else {
					    NormAttributeTransactions nat = new NormAttributeTransactions();
					    plantMaintenanceTransactionRepository.findById(maintenanceId).ifPresent(pmt -> {
					        nat.setAopMonth(pmt.getMaintForMonth());
					    });

					    nat.setAttributeValue(normAttributeTransactionsDTO.getAttributeValue());
					    nat.setAttributeValueVersion("v1");
					    nat.setAuditYear(year);
					    nat.setCreatedOn(new Date());
					    nat.setMaintenanceId(maintenanceId);
					    nat.setNormParameterFKId(normAttributeTransactionsDTO.getNormParameterFKId());
					    nat.setUserName(Utility.getUserName());
					    normAttributeTransactionsList.add(normAttributeTransactionsRepository.save(nat));
					}
			}
		}catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to save/update data", ex);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setData(normAttributeTransactionsList);
		aopMessageVM.setMessage("Data updated successfully");
		return aopMessageVM;
	}

	 	@Override
	    public AOPMessageVM getSlowdownConfigurationData(String plantId, String year) {
		 AOPMessageVM aopMessageVM = new AOPMessageVM();
		 List<Map<String, Object>> monthIdList = new ArrayList<>();
		 Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String procedureName = vertical.getName()+"_GetSlowdownNormConfiguration";
	        try {
	            // Get the data
	            List<Object[]> rows = getData(plantId, year,procedureName);

	            // Get column names
	            
	            List<String> columnNames = getColumnNames(procedureName, plantId, year);

	            // Prepare the list of maps
	            List<Map<String, Object>> resultList = new ArrayList<>();

	            for (Object[] row : rows) {
	                Map<String, Object> rowMap = new LinkedHashMap<>();
	                for (int i = 0; i < columnNames.size(); i++) {
	                    rowMap.put(columnNames.get(i), row[i]);
	                }
	                resultList.add(rowMap);
	            }
	            for (Map<String, Object> row : resultList) {
	                String normId = (String) row.get("NormParameter_FK_Id");
	                for (String key : row.keySet()) {
	                    int idx = key.lastIndexOf('_');
	                    if (idx > 0 && !key.equalsIgnoreCase("NormParameter_FK_Id")) {
	                        String month = key.substring(idx + 1);
	                        int monthNumber=extractMonthNumber(key);
	                        String cleanDesc = stripTrailingSuffix(key);
	                        UUID maintenanceId=plantMaintenanceTransactionRepository.findTransactionIdByDynamicParams("Slowdown",year,UUID.fromString(plantId),cleanDesc);
	                        List<NormAttributeTransactions> normAttributeTransactionsList=  normAttributeTransactionsRepository.findByMaintenanceIdAndNormParameterFKIdAndAuditYear(maintenanceId,UUID.fromString(normId),year,monthNumber);
	                        for(NormAttributeTransactions normAttributeTransactions: normAttributeTransactionsList) {
	                        	if(normAttributeTransactions!=null) {
		                        	Map<String, Object> m = new HashMap<>();
			                        m.put("NormParameter_FK_Id", normId);
			                        m.put("month", key);
			                        monthIdList.add(m);
		                        }
	                        }
	                        
	                        
	                    }
	                }
	            }
	            Map<String, Object> data = new HashMap<>();
	            data.put("data", resultList);
	            data.put("changedData", monthIdList);
	            aopMessageVM.setCode(200);
	    		aopMessageVM.setData(data);
	    		aopMessageVM.setMessage("Data fetched successfully");
	    		return aopMessageVM;
	            
	        } catch (Exception ex) {
	            throw new RuntimeException("Failed to fetch data", ex);
	        }
	    }
	
	public List<Object[]> getData(String plantId, String aopYear,String procedureName) {
		
		try {
			
			String sql = "EXEC " + procedureName +
					" @plantId = :plantId, @aopYear = :aopYear";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public List<String> getColumnNames(String procedureName, String plantId, String aopYear) {
	    return entityManager.unwrap(Session.class).doReturningWork(connection -> {
	        List<String> columnNames = new ArrayList<>();

	        String sql = "EXEC " + procedureName + " @plantId = ?, @aopYear = ?";
	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setString(1, plantId);
	            ps.setString(2, aopYear);

	            try (ResultSet rs = ps.executeQuery()) {
	                ResultSetMetaData rsMetaData = rs.getMetaData();
	                for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
	                    columnNames.add(rsMetaData.getColumnLabel(i));
	                }
	            }
	        }
	        return columnNames;
	    });
	}
	
	@Override
	public AOPMessageVM getShutdownDynamicColumns(String auditYear, UUID plantId) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    List<Map<String, String>> listOfMaps = new ArrayList<>();

	    
	    {
	        Map<String, String> map = new HashMap<>();
	        map.put("field", "particulars");
	        map.put("title", "Particulars");
	        listOfMaps.add(map);
	    }

	    
	    List<String> months = Arrays.asList(
	        "January", "February", "March", "April", "May", "June",
	        "July", "August", "September", "October", "November", "December"
	    );
	    String monthPattern = String.join("|", months);
	    Pattern monthSuffixPattern = Pattern.compile("_(?i)(" + monthPattern + ")$");

	    try {
	    	Plants plant = plantsRepository.findById(plantId).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String procedureName = vertical.getName()+"_GetSlowdownNormConfiguration";
	        List<String> data = getColumnNames(procedureName, plantId.toString(), auditYear);

	       
	        for (String row : data) {
	            Map<String, String> map = new HashMap<>();
	            map.put("field", row);

	            String title = row;
	            Matcher m = monthSuffixPattern.matcher(row);
	            if (m.find()) {
	                title = row.replaceFirst("_(?=[^_]+$)", " (") + ")";
	            }
	            map.put("title", title);

	            listOfMaps.add(map);
	        }

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid data format", e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data", ex);
	    }

	    aopMessageVM.setCode(200);
	    aopMessageVM.setMessage("Data fetched successfully");
	    aopMessageVM.setData(listOfMaps);
	    return aopMessageVM;
	}
	
	private String stripTrailingSuffix(String description) {
	    return description.replaceAll("_[^_]*$", "");
	}
	
	public static int extractMonthNumber(String description) {
        
        int u = description.lastIndexOf('_');
        if (u < 0 || u == description.length() - 1) {
            throw new IllegalArgumentException("No month suffix found.");
        }
        String monthName = description.substring(u + 1);
        try {
            
            Month m = Month.valueOf(monthName.toUpperCase());
            return m.getValue(); 
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unknown month: " + monthName, ex);
        }
    }
}
