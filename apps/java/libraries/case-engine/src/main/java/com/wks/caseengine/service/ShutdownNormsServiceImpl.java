package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Connection;
import jakarta.persistence.Query;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.ShutdownNormsValueDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.GradeShutdownNormsValue;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.ShutdownNormsValue;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.GradeShutdownNormsValueRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.ShutdownNormsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@Service
public class ShutdownNormsServiceImpl implements ShutdownNormsService {

	@Autowired
	private ShutdownNormsRepository shutdownNormsRepository;

	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	PlantsRepository plantsRepository;
	@Autowired
	SiteRepository siteRepository;
	@Autowired
	VerticalsRepository verticalRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private NormParametersRepository normParametersRepository;
	
	@Autowired
	private GradeShutdownNormsValueRepository gradeShutdownNormsValueRepository;
	
	private DataSource dataSource;

	// Inject or set your DataSource (e.g., via constructor or setter)
	public ShutdownNormsServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	


	@Override
	public AOPMessageVM getShutdownNormsData(String year, String plantId,String gradeId) {
		try {
			List<Object[]> objList = null;
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			// Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			if (vertical.getName().equalsIgnoreCase("MEG")) {
				objList = getShutdownNormsMEG(year, plant.getId(), "vwScrnShutdownNorms");
			}else {
				String viewName="vwScrn"+vertical.getName()+"ShutdownNorms";
				objList = getShutdownNorms(year, plant.getId(), viewName,UUID.fromString(gradeId));
			} 
			// List<Object[]> objList = shutdownNormsRepository.findByYearAndPlantFkId(year,
			// UUID.fromString(plantId));
			System.out.println("obj.size(): " + objList.size());
			List<ShutdownNormsValueDTO> shutdownNormsValueDTOList = new ArrayList<>();
			for (Object[] row : objList) {
				ShutdownNormsValueDTO shutdownNormsValueDTO = new ShutdownNormsValueDTO();
				shutdownNormsValueDTO.setId(row[0] != null ? row[0].toString() : null);
				shutdownNormsValueDTO.setSiteFkId(row[1] != null ? row[1].toString() : null);
				shutdownNormsValueDTO.setPlantFkId(row[2] != null ? row[2].toString() : null);
				shutdownNormsValueDTO.setVerticalFkId(row[3] != null ? row[3].toString() : null);
				shutdownNormsValueDTO.setMaterialFkId(row[4] != null ? row[4].toString() : null);
				shutdownNormsValueDTO.setApril(row[5] != null ? Double.parseDouble(row[5].toString()) : null);
				shutdownNormsValueDTO.setMay(row[6] != null ? Double.parseDouble(row[6].toString()) : null);
				shutdownNormsValueDTO.setJune(row[7] != null ? Double.parseDouble(row[7].toString()) : null);
				shutdownNormsValueDTO.setJuly(row[8] != null ? Double.parseDouble(row[8].toString()) : null);
				shutdownNormsValueDTO.setAugust(row[9] != null ? Double.parseDouble(row[9].toString()) : null);
				shutdownNormsValueDTO.setSeptember(row[10] != null ? Double.parseDouble(row[10].toString()) : null);
				shutdownNormsValueDTO.setOctober(row[11] != null ? Double.parseDouble(row[11].toString()) : null);
				shutdownNormsValueDTO.setNovember(row[12] != null ? Double.parseDouble(row[12].toString()) : null);
				shutdownNormsValueDTO.setDecember(row[13] != null ? Double.parseDouble(row[13].toString()) : null);
				shutdownNormsValueDTO.setJanuary(row[14] != null ? Double.parseDouble(row[14].toString()) : null);
				shutdownNormsValueDTO.setFebruary(row[15] != null ? Double.parseDouble(row[15].toString()) : null);
				shutdownNormsValueDTO.setMarch(row[16] != null ? Double.parseDouble(row[16].toString()) : null);

				shutdownNormsValueDTO.setFinancialYear(row[17] != null ? row[17].toString() : null);
				shutdownNormsValueDTO.setRemarks(row[18] != null ? row[18].toString() : " ");
				shutdownNormsValueDTO.setCreatedOn(row[19] != null ? (Date) row[19] : null);
				shutdownNormsValueDTO.setModifiedOn(row[20] != null ? (Date) row[20] : null);
				shutdownNormsValueDTO.setMcuVersion(row[21] != null ? row[21].toString() : null);
				shutdownNormsValueDTO.setUpdatedBy(row[22] != null ? row[22].toString() : null);
				shutdownNormsValueDTO.setNormParameterTypeId(row[23] != null ? row[23].toString() : null);
				shutdownNormsValueDTO.setNormParameterTypeName(row[24] != null ? row[24].toString() : null);
				shutdownNormsValueDTO.setNormParameterTypeDisplayName(row[25] != null ? row[25].toString() : null);

				shutdownNormsValueDTO.setUOM(row[28] != null ? row[28].toString() : null);
				shutdownNormsValueDTO.setIsEditable(row[29] != null ? Boolean.valueOf(row[29].toString()) : null);
				shutdownNormsValueDTO.setProductName(row[30] != null ? row[30].toString() : null);
				shutdownNormsValueDTOList.add(shutdownNormsValueDTO);
			}
			Map<String, Object> map = new HashMap<>();

			List<AopCalculation> aopCalculation = aopCalculationRepository
					.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "shutdown-norms");
			map.put("mcuNormsValueDTOList", shutdownNormsValueDTOList);
			map.put("aopCalculation", aopCalculation);
			
			AOPMessageVM aopMessageVM = new AOPMessageVM();
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
	
	@Override
	public AOPMessageVM saveShutDownNorms(String plantId,List<ShutdownNormsValueDTO> shutdownNormsValueDTOList) {
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		
		Map<String,Object> map=null;
		// Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		if(vertical.getName().equalsIgnoreCase("PP") || vertical.getName().equalsIgnoreCase("PE")) {
			 map=	savePPShutdownNormsData(shutdownNormsValueDTOList);
		}else {
			 map= saveShutdownNormsData(shutdownNormsValueDTOList);
		}
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(map);
		aopMessageVM.setMessage("data updated successfully");
		return aopMessageVM;
	}

	
	public Map<String,Object> saveShutdownNormsData(List<ShutdownNormsValueDTO> shutdownNormsValueDTOList) {
		String year=null;
		UUID plantId=null;
		List<ShutdownNormsValue> shutdownNormsValueList = new ArrayList<>();
		try {
			for (ShutdownNormsValueDTO shutdownNormsValueDTO : shutdownNormsValueDTOList) {
				year=shutdownNormsValueDTO.getFinancialYear();
				plantId=UUID.fromString(shutdownNormsValueDTO.getPlantFkId());
				ShutdownNormsValue shutdownNormsValue = new ShutdownNormsValue();
				if (shutdownNormsValueDTO.getId() != null && !shutdownNormsValueDTO.getId().isEmpty()) {
					shutdownNormsValue.setId(UUID.fromString(shutdownNormsValueDTO.getId()));
					shutdownNormsValue.setModifiedOn(new Date());
				} else {
					UUID siteId = null;
					UUID verticalId = null;
					UUID materialId = null;
					if (shutdownNormsValueDTO.getSiteFkId() != null) {
						siteId = UUID.fromString(shutdownNormsValueDTO.getSiteFkId());
					}
					if (shutdownNormsValueDTO.getPlantFkId() != null) {
						plantId = UUID.fromString(shutdownNormsValueDTO.getPlantFkId());
					}
					if (shutdownNormsValueDTO.getVerticalFkId() != null) {
						verticalId = UUID.fromString(shutdownNormsValueDTO.getVerticalFkId());
					}
					if (shutdownNormsValueDTO.getMaterialFkId() != null) {
						materialId = UUID.fromString(shutdownNormsValueDTO.getMaterialFkId());
					}
					UUID Id = shutdownNormsRepository.findIdByFilters(plantId, siteId, verticalId, materialId,
							shutdownNormsValueDTO.getFinancialYear());
					if (Id != null) {
						shutdownNormsValue.setId(Id);
					}

					shutdownNormsValue.setCreatedOn(new Date());
				}
				shutdownNormsValue.setApril(Optional.ofNullable(shutdownNormsValueDTO.getApril()).orElse(0.0));
				shutdownNormsValue.setMay(Optional.ofNullable(shutdownNormsValueDTO.getMay()).orElse(0.0));
				shutdownNormsValue.setJune(Optional.ofNullable(shutdownNormsValueDTO.getJune()).orElse(0.0));
				shutdownNormsValue.setJuly(Optional.ofNullable(shutdownNormsValueDTO.getJuly()).orElse(0.0));
				shutdownNormsValue.setAugust(Optional.ofNullable(shutdownNormsValueDTO.getAugust()).orElse(0.0));
				shutdownNormsValue.setSeptember(Optional.ofNullable(shutdownNormsValueDTO.getSeptember()).orElse(0.0));
				shutdownNormsValue.setOctober(Optional.ofNullable(shutdownNormsValueDTO.getOctober()).orElse(0.0));
				shutdownNormsValue.setNovember(Optional.ofNullable(shutdownNormsValueDTO.getNovember()).orElse(0.0));
				shutdownNormsValue.setDecember(Optional.ofNullable(shutdownNormsValueDTO.getDecember()).orElse(0.0));
				shutdownNormsValue.setJanuary(Optional.ofNullable(shutdownNormsValueDTO.getJanuary()).orElse(0.0));
				shutdownNormsValue.setFebruary(Optional.ofNullable(shutdownNormsValueDTO.getFebruary()).orElse(0.0));
				shutdownNormsValue.setMarch(Optional.ofNullable(shutdownNormsValueDTO.getMarch()).orElse(0.0));
				if (shutdownNormsValueDTO.getSiteFkId() != null) {
					shutdownNormsValue.setSiteFkId(UUID.fromString(shutdownNormsValueDTO.getSiteFkId()));
				}
				if (shutdownNormsValueDTO.getPlantFkId() != null) {
					shutdownNormsValue.setPlantFkId(UUID.fromString(shutdownNormsValueDTO.getPlantFkId()));
				}
				if (shutdownNormsValueDTO.getVerticalFkId() != null) {
					shutdownNormsValue.setVerticalFkId(UUID.fromString(shutdownNormsValueDTO.getVerticalFkId()));
				}
				if (shutdownNormsValueDTO.getMaterialFkId() != null) {
					shutdownNormsValue.setMaterialFkId(UUID.fromString(shutdownNormsValueDTO.getMaterialFkId()));
				}
				if (shutdownNormsValueDTO.getNormParameterTypeId() != null) {
					shutdownNormsValue
							.setNormParameterTypeFkId(UUID.fromString(shutdownNormsValueDTO.getNormParameterTypeId()));
				}

				shutdownNormsValue.setFinancialYear(shutdownNormsValueDTO.getFinancialYear());
				shutdownNormsValue.setRemarks(shutdownNormsValueDTO.getRemarks());
				shutdownNormsValue.setMcuVersion("V1");
				shutdownNormsValue.setUpdatedBy(Utility.getUserName());

				System.out.println("Data Saved Succussfully");
				shutdownNormsValueList.add(shutdownNormsRepository.save(shutdownNormsValue));
			}
			
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("shutdown-norms");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(plantId);
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			Map<String,Object> map=new HashMap<>();
			map.put("data", shutdownNormsValueList);
			// TODO Auto-generated method stub
			return map;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}
	}
	
	
	public Map<String,Object> savePPShutdownNormsData(List<ShutdownNormsValueDTO> shutdownNormsValueDTOList) {
		String year=null;
		UUID plantId=null;
		List<GradeShutdownNormsValue> gradeShutdownNormsValueList=new ArrayList<>();
		
		try {
			for (ShutdownNormsValueDTO shutdownNormsValueDTO : shutdownNormsValueDTOList) {
				year=shutdownNormsValueDTO.getFinancialYear();
				plantId=UUID.fromString(shutdownNormsValueDTO.getPlantFkId());
				GradeShutdownNormsValue gradeShutdownNormsValue = new GradeShutdownNormsValue();
				if (shutdownNormsValueDTO.getId() != null && !shutdownNormsValueDTO.getId().isEmpty()) {
					gradeShutdownNormsValue.setId(UUID.fromString(shutdownNormsValueDTO.getId()));
					gradeShutdownNormsValue.setModifiedOn(new Date());
				} else {
					UUID siteId = null;
					UUID verticalId = null;
					UUID materialId = null;
					if (shutdownNormsValueDTO.getSiteFkId() != null) {
						siteId = UUID.fromString(shutdownNormsValueDTO.getSiteFkId());
					}
					if (shutdownNormsValueDTO.getPlantFkId() != null) {
						plantId = UUID.fromString(shutdownNormsValueDTO.getPlantFkId());
					}
					if (shutdownNormsValueDTO.getVerticalFkId() != null) {
						verticalId = UUID.fromString(shutdownNormsValueDTO.getVerticalFkId());
					}
					if (shutdownNormsValueDTO.getMaterialFkId() != null) {
						materialId = UUID.fromString(shutdownNormsValueDTO.getMaterialFkId());
					}
					UUID Id = gradeShutdownNormsValueRepository.findIdByFilters(plantId, siteId, verticalId, materialId,
							shutdownNormsValueDTO.getFinancialYear());
					if (Id != null) {
						gradeShutdownNormsValue.setId(Id);
					}

					gradeShutdownNormsValue.setCreatedOn(new Date());
				}
				gradeShutdownNormsValue.setApril(Optional.ofNullable(shutdownNormsValueDTO.getApril()).orElse(0.0));
				gradeShutdownNormsValue.setMay(Optional.ofNullable(shutdownNormsValueDTO.getMay()).orElse(0.0));
				gradeShutdownNormsValue.setJune(Optional.ofNullable(shutdownNormsValueDTO.getJune()).orElse(0.0));
				gradeShutdownNormsValue.setJuly(Optional.ofNullable(shutdownNormsValueDTO.getJuly()).orElse(0.0));
				gradeShutdownNormsValue.setAugust(Optional.ofNullable(shutdownNormsValueDTO.getAugust()).orElse(0.0));
				gradeShutdownNormsValue.setSeptember(Optional.ofNullable(shutdownNormsValueDTO.getSeptember()).orElse(0.0));
				gradeShutdownNormsValue.setOctober(Optional.ofNullable(shutdownNormsValueDTO.getOctober()).orElse(0.0));
				gradeShutdownNormsValue.setNovember(Optional.ofNullable(shutdownNormsValueDTO.getNovember()).orElse(0.0));
				gradeShutdownNormsValue.setDecember(Optional.ofNullable(shutdownNormsValueDTO.getDecember()).orElse(0.0));
				gradeShutdownNormsValue.setJanuary(Optional.ofNullable(shutdownNormsValueDTO.getJanuary()).orElse(0.0));
				gradeShutdownNormsValue.setFebruary(Optional.ofNullable(shutdownNormsValueDTO.getFebruary()).orElse(0.0));
				gradeShutdownNormsValue.setMarch(Optional.ofNullable(shutdownNormsValueDTO.getMarch()).orElse(0.0));
				if (shutdownNormsValueDTO.getSiteFkId() != null) {
					gradeShutdownNormsValue.setSiteFkId(UUID.fromString(shutdownNormsValueDTO.getSiteFkId()));
				}
				if (shutdownNormsValueDTO.getPlantFkId() != null) {
					gradeShutdownNormsValue.setPlantFkId(UUID.fromString(shutdownNormsValueDTO.getPlantFkId()));
				}
				if (shutdownNormsValueDTO.getVerticalFkId() != null) {
					gradeShutdownNormsValue.setVerticalFkId(UUID.fromString(shutdownNormsValueDTO.getVerticalFkId()));
				}
				if (shutdownNormsValueDTO.getMaterialFkId() != null) {
					gradeShutdownNormsValue.setMaterialFkId(UUID.fromString(shutdownNormsValueDTO.getMaterialFkId()));
				}
				if (shutdownNormsValueDTO.getNormParameterTypeId() != null) {
					gradeShutdownNormsValue
							.setNormParameterTypeFkId(UUID.fromString(shutdownNormsValueDTO.getNormParameterTypeId()));
				}

				gradeShutdownNormsValue.setFinancialYear(shutdownNormsValueDTO.getFinancialYear());
				gradeShutdownNormsValue.setRemarks(shutdownNormsValueDTO.getRemarks());
				gradeShutdownNormsValue.setMcuVersion("V1");
				gradeShutdownNormsValue.setUpdatedBy(Utility.getUserName());
				if(shutdownNormsValueDTO.getGradeFkId()!=null) {
					gradeShutdownNormsValue.setGradeFkId(UUID.fromString(shutdownNormsValueDTO.getGradeFkId()));
				}
				System.out.println("Data Saved Succussfully");
				gradeShutdownNormsValueList.add(gradeShutdownNormsValueRepository.save(gradeShutdownNormsValue));
			}
			
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("shutdown-norms");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(plantId);
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			Map<String,Object> map=new HashMap<>();
			map.put("data", gradeShutdownNormsValueList);
			// TODO Auto-generated method stub
			return map;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}
	}


	@Override
	@Transactional
	public AOPMessageVM getShutdownNormsSPData(String year, String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_"+site.getName()+"_CalculateShutdownNorms";
			Integer rowsAffected = getCalculatedShutdownNormsSP(storedProcedure, year, plant.getId().toString(),
					site.getId().toString(), vertical.getId().toString());
			
			
			aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year,
					"shutdown-norms");
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(rowsAffected);
			aopMessageVM.setMessage("SP executed successfully");

			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional
	public Integer getCalculatedShutdownNormsSP(String procedureName, String finYear, String plantId,
			String siteId, String verticalId) {
		try {
						

			String callSql = "{call " + procedureName + "(?, ?, ?, ?)}";

			try (Connection connection = dataSource.getConnection();
					CallableStatement stmt = connection.prepareCall(callSql)) {

				// Set parameters in the correct order
				stmt.setString(1, plantId); // @finYear
				stmt.setString(2, siteId.toString()); // @plantId
				stmt.setString(3, verticalId.toString()); // @verticalId
				stmt.setString(4, finYear); // @siteId

				// Execute the stored procedure
				int rowsAffected = stmt.executeUpdate();

				// Optional: commit if auto-commit is off
				if (!connection.getAutoCommit()) {
					connection.commit();
				}

				return rowsAffected;

			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}


		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> getShutdownNorms(String year, UUID plantId, String viewName,UUID gradeId) {
		try {
			String sql = "SELECT TOP (1000) [Id], [Site_FK_Id], [Plant_FK_Id], [Vertical_FK_Id], "
					+ "[Material_FK_Id], [April], [May], [June], [July], [August], [September], "
					+ "[October], [November], [December], [January], [February], [March], "
					+ "[FinancialYear], [Remarks], [CreatedOn], [ModifiedOn], [MCUVersion], "
					+ "[UpdatedBy], [NormParameterTypeId], [NormParameterTypeName], "
					+ "[NormParameterTypeDisplayName], [NormTypeDisplayOrder], [MaterialDisplayOrder], [UOM],[isEditable],[DisplayName] "
					+ "FROM " + viewName + " "
					+ "WHERE Plant_FK_Id = :plantId AND Grade_FK_Id = :gradeId AND (FinancialYear = :year OR FinancialYear IS NULL) "
					+ "ORDER BY NormTypeDisplayOrder";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("year", year);
			query.setParameter("gradeId", gradeId);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> getShutdownNormsMEG(String year, UUID plantId, String viewName) {
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

	@Override
	public AOPMessageVM getUniqueGrades(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			// Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String viewName="vwScrn"+vertical.getName()+"ShutdownNorms";
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
