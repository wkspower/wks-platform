package com.wks.caseengine.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
import com.wks.caseengine.dto.SlowdownNormsValueDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.SlowdownConsumption;
import com.wks.caseengine.entity.SlowdownNormsValue;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantMaintenanceTransactionRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.SlowdownConsumptionRepository;
import com.wks.caseengine.repository.SlowdownNormsRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@Service
public class SlowdownNormsServiceImpl implements SlowdownNormsService {

	@Autowired
	private SlowdownNormsRepository slowdownNormsRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private PlantMaintenanceTransactionRepository plantMaintenanceTransactionRepository;
	
	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;
	
	@Autowired
	private SlowdownConsumptionRepository slowdownConsumptionRepository;
	
	private DataSource dataSource;
	
	@Autowired
	private NormParametersRepository normParametersRepository;

	public SlowdownNormsServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}


	

	@Override
	@Transactional
	public AOPMessageVM getSlowdownNormsData(String year, String plantId,String gradeId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			UUID grade=null;
			List<Object[]> objList = null;
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();

			if (vertical.getName().equalsIgnoreCase("MEG") || vertical.getName().equalsIgnoreCase("ELASTOMER")) {
				String storedProcedure = vertical.getName() + "_" + site.getName() + "_SlowdownNormCalculation";

				int spResult = getSlowdownNormsSPData(
						storedProcedure,
						year,
						plant.getId().toString(),
						site.getId().toString(),
						vertical.getId().toString());

				objList = getSlowdownNorms(year, plant.getId(), "vwScrnSlowdownNorms");
			} else {
				String viewName = "vwScrn" + vertical.getName() + "SlowdownNorms";
				
				if(gradeId!=null) {
					 grade=UUID.fromString(gradeId);
				}
				objList = getSlowdownNormsWithGrades(year, plant.getId(), viewName,grade);
			}

			List<SlowdownNormsValueDTO> slowdownNormsValueDTOList = new ArrayList<>();
			for (Object[] row : objList) {
				SlowdownNormsValueDTO slowdownNormsValueDTO = new SlowdownNormsValueDTO();
				slowdownNormsValueDTO.setId(row[0] != null ? row[0].toString() : null);
				slowdownNormsValueDTO.setSiteFkId(row[1] != null ? row[1].toString() : null);
				slowdownNormsValueDTO.setPlantFkId(row[2] != null ? row[2].toString() : null);
				slowdownNormsValueDTO.setVerticalFkId(row[3] != null ? row[3].toString() : null);
				slowdownNormsValueDTO.setMaterialFkId(row[4] != null ? row[4].toString() : null);
				slowdownNormsValueDTO.setApril(row[5] != null ? Double.parseDouble(row[5].toString()) : null);
				slowdownNormsValueDTO.setMay(row[6] != null ? Double.parseDouble(row[6].toString()) : null);
				slowdownNormsValueDTO.setJune(row[7] != null ? Double.parseDouble(row[7].toString()) : null);
				slowdownNormsValueDTO.setJuly(row[8] != null ? Double.parseDouble(row[8].toString()) : null);
				slowdownNormsValueDTO.setAugust(row[9] != null ? Double.parseDouble(row[9].toString()) : null);
				slowdownNormsValueDTO.setSeptember(row[10] != null ? Double.parseDouble(row[10].toString()) : null);
				slowdownNormsValueDTO.setOctober(row[11] != null ? Double.parseDouble(row[11].toString()) : null);
				slowdownNormsValueDTO.setNovember(row[12] != null ? Double.parseDouble(row[12].toString()) : null);
				slowdownNormsValueDTO.setDecember(row[13] != null ? Double.parseDouble(row[13].toString()) : null);
				slowdownNormsValueDTO.setJanuary(row[14] != null ? Double.parseDouble(row[14].toString()) : null);
				slowdownNormsValueDTO.setFebruary(row[15] != null ? Double.parseDouble(row[15].toString()) : null);
				slowdownNormsValueDTO.setMarch(row[16] != null ? Double.parseDouble(row[16].toString()) : null);
				slowdownNormsValueDTO.setFinancialYear(row[17] != null ? row[17].toString() : null);
				slowdownNormsValueDTO.setRemarks(row[18] != null ? row[18].toString() : " ");
				slowdownNormsValueDTO.setCreatedOn(row[19] != null ? (Date) row[19] : null);
				slowdownNormsValueDTO.setModifiedOn(row[20] != null ? (Date) row[20] : null);
				slowdownNormsValueDTO.setMcuVersion(row[21] != null ? row[21].toString() : null);
				slowdownNormsValueDTO.setUpdatedBy(row[22] != null ? row[22].toString() : null);
				slowdownNormsValueDTO.setNormParameterTypeId(row[23] != null ? row[23].toString() : null);
				slowdownNormsValueDTO.setNormParameterTypeName(row[24] != null ? row[24].toString() : null);
				slowdownNormsValueDTO.setNormParameterTypeDisplayName(row[25] != null ? row[25].toString() : null);
				slowdownNormsValueDTO.setUOM(row[28] != null ? row[28].toString() : null);
				slowdownNormsValueDTO.setIsEditable(row[29] != null ? Boolean.valueOf(row[29].toString()) : null);
				slowdownNormsValueDTO.setProductName(row[30] != null ? row[30].toString() : null);
				slowdownNormsValueDTOList.add(slowdownNormsValueDTO);
			}
			Map<String, Object> map = new HashMap<>();

			List<AopCalculation> aopCalculation = aopCalculationRepository
					.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "slowdown-norms");
			map.put("slowdownNormsValueDTO", slowdownNormsValueDTOList);
			map.put("aopCalculation", aopCalculation);
			aopMessageVM.setCode(200);
			aopMessageVM.setData(map);
			aopMessageVM.setMessage("Data fetched successfully");
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional
	public int getSlowdownNormsSPData(String procedureName, String finYear, String plantId,
			String siteId, String verticalId) {
		try {
			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @finYear = :finYear";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("finYear", finYear);

			return query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();

		}
		return 0;
	}

	@Override
	public List<SlowdownNormsValueDTO> saveSlowdownNormsData(List<SlowdownNormsValueDTO> slowdownNormsValueDTOList) {
		String year=null;
		UUID plantId=null;
		
		try {
			for (SlowdownNormsValueDTO slowdownNormsValueDTO : slowdownNormsValueDTOList) {
				year=slowdownNormsValueDTO.getFinancialYear();
				plantId=UUID.fromString(slowdownNormsValueDTO.getPlantFkId());
				SlowdownNormsValue slowdownNormsValue = new SlowdownNormsValue();
				if (slowdownNormsValueDTO.getId() != null && !slowdownNormsValueDTO.getId().isEmpty()) {
					slowdownNormsValue.setId(UUID.fromString(slowdownNormsValueDTO.getId()));
					slowdownNormsValue.setModifiedOn(new Date());
				} else {
					UUID siteId = null;
					UUID verticalId = null;
					UUID materialId = null;
					if (slowdownNormsValueDTO.getSiteFkId() != null) {
						siteId = UUID.fromString(slowdownNormsValueDTO.getSiteFkId());
					}
					if (slowdownNormsValueDTO.getPlantFkId() != null) {
						plantId = UUID.fromString(slowdownNormsValueDTO.getPlantFkId());
					}
					if (slowdownNormsValueDTO.getVerticalFkId() != null) {
						verticalId = UUID.fromString(slowdownNormsValueDTO.getVerticalFkId());
					}
					if (slowdownNormsValueDTO.getMaterialFkId() != null) {
						materialId = UUID.fromString(slowdownNormsValueDTO.getMaterialFkId());
					}
					UUID Id = slowdownNormsRepository.findIdByFilters(plantId, siteId, verticalId, materialId,
							slowdownNormsValueDTO.getFinancialYear());
					if (Id != null) {
						slowdownNormsValue.setId(Id);
					}

					slowdownNormsValue.setCreatedOn(new Date());
				}
				slowdownNormsValue.setApril(Optional.ofNullable(slowdownNormsValueDTO.getApril()).orElse(0.0));
				slowdownNormsValue.setMay(Optional.ofNullable(slowdownNormsValueDTO.getMay()).orElse(0.0));
				slowdownNormsValue.setJune(Optional.ofNullable(slowdownNormsValueDTO.getJune()).orElse(0.0));
				slowdownNormsValue.setJuly(Optional.ofNullable(slowdownNormsValueDTO.getJuly()).orElse(0.0));
				slowdownNormsValue.setAugust(Optional.ofNullable(slowdownNormsValueDTO.getAugust()).orElse(0.0));
				slowdownNormsValue.setSeptember(Optional.ofNullable(slowdownNormsValueDTO.getSeptember()).orElse(0.0));
				slowdownNormsValue.setOctober(Optional.ofNullable(slowdownNormsValueDTO.getOctober()).orElse(0.0));
				slowdownNormsValue.setNovember(Optional.ofNullable(slowdownNormsValueDTO.getNovember()).orElse(0.0));
				slowdownNormsValue.setDecember(Optional.ofNullable(slowdownNormsValueDTO.getDecember()).orElse(0.0));
				slowdownNormsValue.setJanuary(Optional.ofNullable(slowdownNormsValueDTO.getJanuary()).orElse(0.0));
				slowdownNormsValue.setFebruary(Optional.ofNullable(slowdownNormsValueDTO.getFebruary()).orElse(0.0));
				slowdownNormsValue.setMarch(Optional.ofNullable(slowdownNormsValueDTO.getMarch()).orElse(0.0));
				if (slowdownNormsValueDTO.getSiteFkId() != null) {
					slowdownNormsValue.setSiteFkId(UUID.fromString(slowdownNormsValueDTO.getSiteFkId()));
				}
				if (slowdownNormsValueDTO.getPlantFkId() != null) {
					slowdownNormsValue.setPlantFkId(UUID.fromString(slowdownNormsValueDTO.getPlantFkId()));
				}
				if (slowdownNormsValueDTO.getVerticalFkId() != null) {
					slowdownNormsValue.setVerticalFkId(UUID.fromString(slowdownNormsValueDTO.getVerticalFkId()));
				}
				if (slowdownNormsValueDTO.getMaterialFkId() != null) {
					slowdownNormsValue.setMaterialFkId(UUID.fromString(slowdownNormsValueDTO.getMaterialFkId()));
				}
				if (slowdownNormsValueDTO.getNormParameterTypeId() != null) {
					slowdownNormsValue
							.setNormParameterTypeFkId(UUID.fromString(slowdownNormsValueDTO.getNormParameterTypeId()));
				}

				slowdownNormsValue.setFinancialYear(slowdownNormsValueDTO.getFinancialYear());
				slowdownNormsValue.setRemarks(slowdownNormsValueDTO.getRemarks());
				slowdownNormsValue.setMcuVersion("V1");
				slowdownNormsValue.setUpdatedBy(Utility.getUserName());
				System.out.println(slowdownNormsValue.getApril());
				System.out.println("Data Saved Succussfully");
				slowdownNormsRepository.save(slowdownNormsValue);
			}
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("slowdown-norms");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(plantId);
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			
			return slowdownNormsValueDTOList;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to update data", ex);
		}
	}

	@Override
	@Transactional
	public List<SlowdownNormsValueDTO> getSlowdownNormsSPData(String year, String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_CalculateConsumptionAOPValues";
			List<Object[]> list = getCalculatedSlowdownNormsSP(storedProcedure, year, plant.getId().toString(),
					site.getId().toString(), vertical.getId().toString());
			List<SlowdownNormsValueDTO> slowdownNormsValueDTOList = new ArrayList<>();
			for (Object[] row : list) {
				SlowdownNormsValueDTO slowdownNormsValueDTO = new SlowdownNormsValueDTO();
				slowdownNormsValueDTO.setNormParameterTypeDisplayName(row[0] != null ? row[0].toString() : null);
				slowdownNormsValueDTO.setUOM(row[1] != null ? row[1].toString() : null);
				slowdownNormsValueDTO.setSiteFkId(row[2] != null ? row[2].toString() : null);
				slowdownNormsValueDTO.setVerticalFkId(row[3] != null ? row[3].toString() : null);
				slowdownNormsValueDTO.setAOPCaseId(row[4] != null ? row[4].toString() : null);
				slowdownNormsValueDTO.setAOPStatus(row[5] != null ? row[5].toString() : null);
				slowdownNormsValueDTO.setRemarks(row[6] != null ? row[6].toString() : "");
				slowdownNormsValueDTO.setMaterialFkId(row[7] != null ? row[7].toString() : null);
				slowdownNormsValueDTO.setJanuary(row[8] != null ? Double.parseDouble(row[8].toString()) : null);
				slowdownNormsValueDTO.setFebruary(row[9] != null ? Double.parseDouble(row[9].toString()) : null);
				slowdownNormsValueDTO.setMarch(row[10] != null ? Double.parseDouble(row[10].toString()) : null);
				slowdownNormsValueDTO.setApril(row[11] != null ? Double.parseDouble(row[11].toString()) : null);
				slowdownNormsValueDTO.setMay(row[12] != null ? Double.parseDouble(row[12].toString()) : null);
				slowdownNormsValueDTO.setJune(row[13] != null ? Double.parseDouble(row[13].toString()) : null);
				slowdownNormsValueDTO.setJuly(row[14] != null ? Double.parseDouble(row[14].toString()) : null);
				slowdownNormsValueDTO.setAugust(row[15] != null ? Double.parseDouble(row[15].toString()) : null);
				slowdownNormsValueDTO.setSeptember(row[16] != null ? Double.parseDouble(row[16].toString()) : null);
				slowdownNormsValueDTO.setOctober(row[17] != null ? Double.parseDouble(row[17].toString()) : null);
				slowdownNormsValueDTO.setNovember(row[18] != null ? Double.parseDouble(row[18].toString()) : null);
				slowdownNormsValueDTO.setDecember(row[19] != null ? Double.parseDouble(row[19].toString()) : null);
				slowdownNormsValueDTO.setFinancialYear(row[20] != null ? row[20].toString() : null);
				slowdownNormsValueDTO.setPlantFkId(row[21] != null ? row[21].toString() : null);
				slowdownNormsValueDTOList.add(slowdownNormsValueDTO);
			}

			return slowdownNormsValueDTOList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional
	public List<Object[]> getCalculatedSlowdownNormsSP(String procedureName, String finYear, String plantId,
			String siteId, String verticalId) {
		try {
			// Create a native query to execute the stored procedure
			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @finYear = :finYear";

			Query query = entityManager.createNativeQuery(sql);

			// Set parameters
			query.setParameter("plantId", plantId);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("finYear", finYear);

			return query.getResultList(); // Fetch results instead of executing an update
		} catch (Exception e) {
			e.printStackTrace(); // Log detailed exception for debugging
			return Collections.emptyList(); // Return an empty list instead of 0
		}
	}

	public List<Object[]> getSlowdownNorms(String year, UUID plantId, String viewName) {
		try {
			String sql = "SELECT TOP (1000) [Id], [Site_FK_Id], [Plant_FK_Id], [Vertical_FK_Id], "
					+ "[Material_FK_Id], [April], [May], [June], [July], [August], [September], "
					+ "[October], [November], [December], [January], [February], [March], "
					+ "[FinancialYear], [Remarks], [CreatedOn], [ModifiedOn], [MCUVersion], "
					+ "[UpdatedBy], [NormParameterTypeId], [NormParameterTypeName], "
					+ "[NormParameterTypeDisplayName], [NormTypeDisplayOrder], [MaterialDisplayOrder], [UOM],[isEditable],[DisplayName] "
					+ "FROM " + viewName + " "
					+ "WHERE Plant_FK_Id = :plantId AND (FinancialYear = :year OR FinancialYear IS NULL) "
					+ "ORDER BY NormTypeDisplayOrder";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("year", year);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public List<Object[]> getSlowdownNormsWithGrades(String year, UUID plantId, String viewName,UUID gradeId) {
		try {
			String sql = "SELECT TOP (1000) [Id], [Site_FK_Id], [Plant_FK_Id], [Vertical_FK_Id], "
					+ "[Material_FK_Id], [April], [May], [June], [July], [August], [September], "
					+ "[October], [November], [December], [January], [February], [March], "
					+ "[FinancialYear], [Remarks], [CreatedOn], [ModifiedOn], [MCUVersion], "
					+ "[UpdatedBy], [NormParameterTypeId], [NormParameterTypeName], "
					+ "[NormParameterTypeDisplayName], [NormTypeDisplayOrder], [MaterialDisplayOrder], [UOM],[isEditable],[DisplayName] "
					+ "FROM " + viewName + " "
					+ "WHERE Plant_FK_Id = :plantId AND (FinancialYear = :year OR FinancialYear IS NULL) AND (:gradeId IS NULL OR Grade_FK_Id = :gradeId) "
					+ "ORDER BY NormTypeDisplayOrder";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("gradeId", gradeId);
			query.setParameter("year", year);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}


	@Override
	@Transactional
	public List getSlowdownMonths(UUID plantId, String maintenanceName,String year,String gradeId) {
		String verticalName = plantsRepository.findVerticalNameByPlantId((plantId));
		
		try {
			if(verticalName.equalsIgnoreCase("PE") || verticalName.equalsIgnoreCase("PP")) {
				UUID grade=null;
				if(gradeId!=null) {
					 grade=UUID.fromString(gradeId);
				}
				return	slowdownNormsRepository.getSlowdownMonthsWithGrades(plantId,maintenanceName,year,grade);
			}else {
				return	slowdownNormsRepository.getSlowdownMonths(plantId,maintenanceName,year);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	@Transactional
	public AOPMessageVM getCalculateSlowdownNorms(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		String storedProcedure = vertical.getName() + "_" + site.getName() + "_SlowdownConsumptionCalculation";
		System.out.println("storedProcedure" + storedProcedure);
		int result = executeDynamicUpdateProcedure(storedProcedure, plantId, site.getId().toString(),
				vertical.getId().toString(), year);
		aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year,
				"slowdown-norms-configuration");
		
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("SP Executed successfully");
		aopMessageVM.setData(result);
		return aopMessageVM;
	}
	
	public int executeDynamicUpdateProcedure(String procedureName, String plantId, String siteId, String verticalId,
			String finYear) {
		String callSql = "{call " + procedureName + "(?, ?, ?, ?)}";

		try (Connection connection = dataSource.getConnection();
				CallableStatement stmt = connection.prepareCall(callSql)) {

			// Set parameters
			stmt.setString(1, plantId);
			stmt.setString(2, siteId);
			stmt.setString(3, verticalId);
			stmt.setString(4, finYear);

			// Execute the stored procedure
			int rowsAffected = stmt.executeUpdate();

			
			if (!connection.getAutoCommit()) {
				connection.commit();
			}

			return rowsAffected;

		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	@Override
	public AOPMessageVM getSlowdownNormsDynamicColumns(String auditYear, UUID plantId) {
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
			String procedureName = vertical.getName()+"_GetSlowdownConsumption";
	        List<String> data = getColumnNames(procedureName, plantId.toString(), auditYear);

	        // 3. Process each dynamic column
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
    public AOPMessageVM getSlowdownNormsConfigurationData(String plantId, String year) {
	 AOPMessageVM aopMessageVM = new AOPMessageVM();
	 Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		String procedureName = vertical.getName()+"_GetSlowdownConsumption";
        try {
            // Get the data
            List<Object[]> rows = getData(plantId, year,procedureName);

            
            
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
            Map<String, Object> map = new HashMap<>();

			List<AopCalculation> aopCalculation = aopCalculationRepository
					.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "slowdown-norms-configuration");
			map.put("resultList", resultList);
			map.put("aopCalculation", aopCalculation);
            aopMessageVM.setCode(200);
    		aopMessageVM.setData(map);
    		aopMessageVM.setMessage("Data updated successfully");
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

	@Override
	public AOPMessageVM saveSlowdownNormsConfigurationData(String plantId, String year,
			List<NormAttributeTransactionsDTO> normAttributeTransactionsDTOList) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		
		List<SlowdownConsumption> slowdownConsumptionList = new ArrayList<>();
		try {
			for(NormAttributeTransactionsDTO normAttributeTransactionsDTO:normAttributeTransactionsDTOList) {
				String rawDesc = normAttributeTransactionsDTO.getDescription();
				int month=extractMonthNumber(rawDesc);
				String cleanDesc = stripTrailingSuffix(rawDesc);
				UUID maintenanceId=plantMaintenanceTransactionRepository.findTransactionIdByDynamicParams("Slowdown",year,UUID.fromString(plantId),cleanDesc);
				if(maintenanceId==null) {
					throw new RuntimeException("No Maintenance Id found with "+normAttributeTransactionsDTO.getDescription());
				}
				SlowdownConsumption  slowdownConsumption= slowdownConsumptionRepository.findByParameterFKIdAndAuditYear(UUID.fromString(plantId),normAttributeTransactionsDTO.getNormParameterFKId(),year,maintenanceId,month);
				if(slowdownConsumption!=null) {
					slowdownConsumption.setParameterValue(Double.parseDouble(normAttributeTransactionsDTO.getAttributeValue()));
					slowdownConsumption.setUpdatedOn(new Date());
					slowdownConsumption.setUpdatedBy(Utility.getUserName());
					slowdownConsumptionList.add(slowdownConsumptionRepository.save(slowdownConsumption));
				}else {
					slowdownConsumption = new SlowdownConsumption();
					slowdownConsumption.setParameterValue(Double.parseDouble(normAttributeTransactionsDTO.getAttributeValue()));
					slowdownConsumption.setAopYear(year);
					slowdownConsumption.setCreatedOn(new Date());
					slowdownConsumption.setPlantMaintenanceFkId(maintenanceId);
					slowdownConsumption.setAopMonth(month);
					slowdownConsumption.setNormParameterFkId(normAttributeTransactionsDTO.getNormParameterFKId());
					slowdownConsumption.setCreatedBy(Utility.getUserName());
					slowdownConsumption.setPlantFkId(UUID.fromString(plantId));
					slowdownConsumptionList.add(slowdownConsumptionRepository.save(slowdownConsumption));
				}
			}
		}catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to save/update data", ex);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setData(slowdownConsumptionList);
		aopMessageVM.setMessage("Data updated successfully");
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
	
	@Override
	public AOPMessageVM getUniqueGrades(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			// Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String viewName="vwScrn"+vertical.getName()+"SlowdownNorms";
			List<String> grades=fetchUniqueGradeFkIds(viewName,UUID.fromString(plantId),year);
			List<Map<String, String>> listOfMaps = new ArrayList<>();

			for (String grade : grades) {
			    String productName = normParametersRepository.findNormParameterIdByGrade(UUID.fromString(grade));
			    Map<String, String> singleEntryMap = new HashMap<>();
			    singleEntryMap.put("gradeId", grade);
			    singleEntryMap.put("displayName", productName);
			    listOfMaps.add(singleEntryMap);
			}
			
			aopMessageVM.setCode(200);
			aopMessageVM.setData(listOfMaps);
			aopMessageVM.setMessage("Data fetched successfully");
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		return aopMessageVM;
	}
	
	 public List<String> fetchUniqueGradeFkIds(String viewName, UUID plantFkId, String financialYear) {
	        // Build SQL with safe view injection (ensure viewName is validated)
	        String sql = "SELECT DISTINCT Grade_Fk_Id FROM " + viewName +
	                     " WHERE Plant_Fk_Id = :plantFkId AND FinancialYear = :financialYear";

	        Query query = entityManager.createNativeQuery(sql);
	        query.setParameter("plantFkId", plantFkId);
	        query.setParameter("financialYear", financialYear);

	        @SuppressWarnings("unchecked")
	        List<String> results = query.getResultList();
	        return results;
	    }


}
