package com.wks.caseengine.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;


import java.sql.*;
import java.util.*;
import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.PlantMaintenance;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.PlantMaintenanceRepository;
import com.wks.caseengine.repository.PlantMaintenanceTransactionRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SlowdownPlanRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

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
					System.out.println("plantMaintenanceTransaction.getMaintForMonth()"
							+ plantMaintenanceTransaction.getMaintForMonth());

					System.out.println("shutDownPlanDTO.getCreatedOn()" + shutDownPlanDTO.getCreatedOn());

					plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
					plantMaintenanceTransaction.setRateEO(shutDownPlanDTO.getRateEO());
					plantMaintenanceTransaction.setRateEOE(shutDownPlanDTO.getRateEOE());
					plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
					// plantMaintenanceTransaction.setName("Default Name");
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

					PlantMaintenanceTransaction plantMaintenanceTransaction = slowdownPlanRepository
							.findById(UUID.fromString(shutDownPlanDTO.getId())).get();
					System.out.println("At start name is:" + plantMaintenanceTransaction.getName());
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
					System.out.println("plantMaintenanceTransaction.getMaintForMonth()"
							+ plantMaintenanceTransaction.getMaintForMonth());
					plantMaintenanceTransaction.setUser("system");
					// plantMaintenanceTransaction.setName("Default Name");
					plantMaintenanceTransaction.setVersion("V1");
					System.out.println("shutDownPlanDTO.getCreatedOn()" + shutDownPlanDTO.getCreatedOn());

					plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);
					plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
					plantMaintenanceTransaction.setRateEO(shutDownPlanDTO.getRateEO());
					plantMaintenanceTransaction.setRateEOE(shutDownPlanDTO.getRateEOE());
					plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());

					if (shutDownPlanDTO.getProductId() != null) {
						plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
					}

					plantMaintenanceTransaction.setAuditYear(shutDownPlanDTO.getAudityear());
					System.out.println("At end name is:" + plantMaintenanceTransaction.getName());
					// Save new record
					slowdownPlanRepository.save(plantMaintenanceTransaction);

				}
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
				UUID maintenanceId=plantMaintenanceTransactionRepository.findIdByNormIdAndDiscription(normAttributeTransactionsDTO.getDescription(),normAttributeTransactionsDTO.getNormParameterFKId());
				normAttributeTransactionsDTO.setMaintenanceId(maintenanceId);
				NormAttributeTransactions  normAttributeTransactions= normAttributeTransactionsRepository.findByMaintenanceIdAndNormParameterFKIdAndAuditYear(normAttributeTransactionsDTO.getMaintenanceId(),normAttributeTransactionsDTO.getNormParameterFKId(),year);
				if(normAttributeTransactions!=null) {
					normAttributeTransactions.setAttributeValue(normAttributeTransactionsDTO.getAttributeValue());
					normAttributeTransactionsList.add(normAttributeTransactionsRepository.save(normAttributeTransactions));
				}else {
					normAttributeTransactions = new NormAttributeTransactions();
					Optional<PlantMaintenanceTransaction> PlantMaintenanceTransactionopt=plantMaintenanceTransactionRepository.findById(maintenanceId);
					if(PlantMaintenanceTransactionopt.isPresent()) {
						normAttributeTransactions.setAopMonth(PlantMaintenanceTransactionopt.get().getMaintForMonth());
					}	
					normAttributeTransactions.setAttributeValue(normAttributeTransactionsDTO.getAttributeValue());
					normAttributeTransactions.setAttributeValueVersion("v1");
					normAttributeTransactions.setAuditYear(year);
					normAttributeTransactions.setCreatedOn(new Date());
					normAttributeTransactions.setMaintenanceId(normAttributeTransactionsDTO.getMaintenanceId());
					normAttributeTransactions.setNormParameterFKId(normAttributeTransactionsDTO.getNormParameterFKId());
					normAttributeTransactions.setUserName("System");
					normAttributeTransactionsList.add(normAttributeTransactionsRepository.save(normAttributeTransactions));
				}
			}
		}catch (Exception ex) {
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
	        try {
	            // Get the data
	            List<Object[]> rows = getData(plantId, year);

	            // Get column names
	            List<String> columnNames = getColumnNames("GetPlantNormConfigurations_Static", plantId, year);

	            // Prepare the list of maps
	            List<Map<String, Object>> resultList = new ArrayList<>();

	            for (Object[] row : rows) {
	                Map<String, Object> rowMap = new LinkedHashMap<>();
	                for (int i = 0; i < columnNames.size(); i++) {
	                    rowMap.put(columnNames.get(i), row[i]);
	                }
	                resultList.add(rowMap);
	            }
	            aopMessageVM.setCode(200);
	    		aopMessageVM.setData(resultList);
	    		aopMessageVM.setMessage("Data updated successfully");
	    		return aopMessageVM;
	            
	        } catch (Exception ex) {
	            throw new RuntimeException("Failed to fetch data", ex);
	        }
	    }
	
	public List<Object[]> getData(String plantId, String aopYear) {
		try {
			String procedureName = "GetPlantNormConfigurations_Static";
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
		Map<String, String> map = new HashMap<>();
		String result=null;
		try {
			List<String> data = getColumnNames("GetPlantNormConfigurations_Static", plantId.toString(), auditYear);

			map.put("field", "particulars");
			map.put("title", "Particulars");
			listOfMaps.add(map);

			// Iterate over data
			for (String row : data) {
			    map = new HashMap<>();
			    map.put("field", row);
			    map.put("title", row);
			    listOfMaps.add(map);
			}

		   
		}catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid data format", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Data fetched successfully");
		aopMessageVM.setData(listOfMaps);
		return aopMessageVM;
	}
}
