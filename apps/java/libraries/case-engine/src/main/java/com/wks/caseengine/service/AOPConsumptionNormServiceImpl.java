package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.CalculatedConsumptionNormsDTO;
import com.wks.caseengine.dto.AOPConsumptionNormDTO;
import com.wks.caseengine.entity.AOPConsumptionNorm;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AOPConsumptionNormRepository;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Connection;
import jakarta.persistence.Query;


@Service
public class AOPConsumptionNormServiceImpl implements AOPConsumptionNormService {

	@Autowired
	private AOPConsumptionNormRepository aOPConsumptionNormRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;
	
	private DataSource dataSource;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	// Inject or set your DataSource (e.g., via constructor or setter)
		public AOPConsumptionNormServiceImpl(DataSource dataSource) {
			this.dataSource = dataSource;
		}

	@Override
	public AOPMessageVM getAOPConsumptionNorm(String plantId, String year,String gradeId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

		try {
			List<Object[]> resultList = getAOPConsumptionNormDataFromView(year, UUID.fromString(plantId),gradeId);
			List<AOPConsumptionNormDTO> aOPConsumptionNormDTOList = new ArrayList<>();

			for (Object[] row : resultList) {
				AOPConsumptionNormDTO dto = new AOPConsumptionNormDTO();

				dto.setId(row[0] != null ? row[0].toString() : null);
				dto.setSiteFkId(row[1] != null ? row[1].toString() : null);
				dto.setVerticalFkId(row[2] != null ? row[2].toString() : null);
				dto.setAopCaseId(row[3] != null ? row[3].toString() : null);
				dto.setAopStatus(row[4] != null ? row[4].toString() : null);
				dto.setAopRemarks(row[5] != null ? row[5].toString() : null);
				if(vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")) {
					dto.setGradeId(gradeId);
					dto.setMaterialFkId(row[7] != null ? row[7].toString() : null);
					dto.setJan(row[8] != null ? Double.valueOf(row[8].toString()) : null);
					dto.setFeb(row[9] != null ? Double.valueOf(row[9].toString()) : null);
					dto.setMarch(row[10] != null ? Double.valueOf(row[10].toString()) : null);
					dto.setApril(row[11] != null ? Double.valueOf(row[11].toString()) : null);
					dto.setMay(row[12] != null ? Double.valueOf(row[12].toString()) : null);
					dto.setJune(row[13] != null ? Double.valueOf(row[13].toString()) : null);
					dto.setJuly(row[14] != null ? Double.valueOf(row[14].toString()) : null);
					dto.setAug(row[15] != null ? Double.valueOf(row[15].toString()) : null);
					dto.setSep(row[16] != null ? Double.valueOf(row[16].toString()) : null);
					dto.setOct(row[17] != null ? Double.valueOf(row[17].toString()) : null);
					dto.setNov(row[18] != null ? Double.valueOf(row[18].toString()) : null);
					dto.setDec(row[19] != null ? Double.valueOf(row[19].toString()) : null);
					dto.setAopYear(row[20] != null ? row[20].toString() : null);
					dto.setPlantFkId(row[21] != null ? row[21].toString() : null);
					dto.setNormParameterTypeDisplayName(row[22] != null ? row[22].toString() : null);
					dto.setUOM(row[23] != null ? row[23].toString() : null);
					dto.setIsEditable(row[24] != null ? Boolean.valueOf(row[24].toString()) : null);
					dto.setProductName(row[25] != null ? row[25].toString() : null);
				}else {
					dto.setMaterialFkId(row[6] != null ? row[6].toString() : null);
					dto.setJan(row[7] != null ? Double.valueOf(row[7].toString()) : null);
					dto.setFeb(row[8] != null ? Double.valueOf(row[8].toString()) : null);
					dto.setMarch(row[9] != null ? Double.valueOf(row[9].toString()) : null);
					dto.setApril(row[10] != null ? Double.valueOf(row[10].toString()) : null);
					dto.setMay(row[11] != null ? Double.valueOf(row[11].toString()) : null);
					dto.setJune(row[12] != null ? Double.valueOf(row[12].toString()) : null);
					dto.setJuly(row[13] != null ? Double.valueOf(row[13].toString()) : null);
					dto.setAug(row[14] != null ? Double.valueOf(row[14].toString()) : null);
					dto.setSep(row[15] != null ? Double.valueOf(row[15].toString()) : null);
					dto.setOct(row[16] != null ? Double.valueOf(row[16].toString()) : null);
					dto.setNov(row[17] != null ? Double.valueOf(row[17].toString()) : null);
					dto.setDec(row[18] != null ? Double.valueOf(row[18].toString()) : null);
					dto.setAopYear(row[19] != null ? row[19].toString() : null);
					dto.setPlantFkId(row[20] != null ? row[20].toString() : null);
					dto.setNormParameterTypeDisplayName(row[21] != null ? row[21].toString() : null);
					dto.setUOM(row[22] != null ? row[22].toString() : null);
					dto.setIsEditable(row[23] != null ? Boolean.valueOf(row[23].toString()) : null);
					dto.setProductName(row[24] != null ? row[24].toString() : null);
				}
				
				aOPConsumptionNormDTOList.add(dto);
			}
			Map<String, Object> map = new HashMap<>(); 
			
			List<AopCalculation> aopCalculation=aopCalculationRepository.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId),year,"consumption-aop");
			map.put("aopConsumptionNormDTOList", aOPConsumptionNormDTOList);
			map.put("aopCalculation", aopCalculation);
			aopMessageVM.setCode(200);
			aopMessageVM.setData(map);
			aopMessageVM.setMessage("Data fetched successfully");
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public List<AOPConsumptionNormDTO> saveAOPConsumptionNorm(List<AOPConsumptionNormDTO> aOPConsumptionNormDTOList) {

		try {
			for (AOPConsumptionNormDTO aOPConsumptionNormDTO : aOPConsumptionNormDTOList) {

				AOPConsumptionNorm aOPConsumptionNorm = new AOPConsumptionNorm();

				// AOPConsumptionNorm aOPConsumptionNorm = new AOPConsumptionNorm();

				if (aOPConsumptionNormDTO.getId() != null && !aOPConsumptionNormDTO.getId().isEmpty()) {
					aOPConsumptionNorm.setId(UUID.fromString(aOPConsumptionNormDTO.getId()));
					// aOPConsumptionNorm.setModifiedOn(new Date());
				} else {
					UUID plantId = null;
					UUID siteId = null;
					UUID verticalId = null;
					UUID materialId = null;

					if (aOPConsumptionNormDTO.getSiteFkId() != null) {
						siteId = UUID.fromString(aOPConsumptionNormDTO.getSiteFkId());
					}
					if (aOPConsumptionNormDTO.getPlantFkId() != null) {
						plantId = UUID.fromString(aOPConsumptionNormDTO.getPlantFkId());
					}
					if (aOPConsumptionNormDTO.getVerticalFkId() != null) {
						verticalId = UUID.fromString(aOPConsumptionNormDTO.getVerticalFkId());
					}
					if (aOPConsumptionNormDTO.getMaterialFkId() != null) {
						materialId = UUID.fromString(aOPConsumptionNormDTO.getMaterialFkId());
					}

					UUID Id = aOPConsumptionNormRepository.findIdByFilters(plantId, siteId, verticalId, materialId,
							aOPConsumptionNormDTO.getAopYear());
					if (Id != null) {
						aOPConsumptionNorm.setId(Id);
					}

					// aOPConsumptionNorm.setCreatedOn(new Date());
				}
				aOPConsumptionNorm.setAopCaseId(aOPConsumptionNormDTO.getAopCaseId());
				aOPConsumptionNorm.setAopCaseId(aOPConsumptionNormDTO.getAopCaseId());
				aOPConsumptionNorm.setAopRemarks(aOPConsumptionNormDTO.getAopRemarks());
				aOPConsumptionNorm.setAopStatus(aOPConsumptionNormDTO.getAopStatus());
				aOPConsumptionNorm.setAopYear(aOPConsumptionNormDTO.getAopYear());
				aOPConsumptionNorm.setJan(aOPConsumptionNormDTO.getJan());
				aOPConsumptionNorm.setFeb(aOPConsumptionNormDTO.getFeb());
				aOPConsumptionNorm.setMarch(aOPConsumptionNormDTO.getMarch());
				aOPConsumptionNorm.setApril(aOPConsumptionNormDTO.getApril());
				aOPConsumptionNorm.setMay(aOPConsumptionNormDTO.getMay());
				aOPConsumptionNorm.setJune(aOPConsumptionNormDTO.getJune());
				aOPConsumptionNorm.setJuly(aOPConsumptionNormDTO.getJuly());
				aOPConsumptionNorm.setAug(aOPConsumptionNormDTO.getAug());
				aOPConsumptionNorm.setSep(aOPConsumptionNormDTO.getSep());
				aOPConsumptionNorm.setOct(aOPConsumptionNormDTO.getOct());
				aOPConsumptionNorm.setNov(aOPConsumptionNormDTO.getNov());
				aOPConsumptionNorm.setDec(aOPConsumptionNormDTO.getDec());
				if (aOPConsumptionNormDTO.getId() != null) {
					aOPConsumptionNorm.setId(UUID.fromString(aOPConsumptionNormDTO.getId()));
				}
				if (aOPConsumptionNormDTO.getSiteFkId() != null) {
					aOPConsumptionNorm.setSiteFkId(UUID.fromString(aOPConsumptionNormDTO.getSiteFkId()));
				}
				if (aOPConsumptionNormDTO.getVerticalFkId() != null) {
					aOPConsumptionNorm.setVerticalFkId(UUID.fromString(aOPConsumptionNormDTO.getVerticalFkId()));
				}
				if (aOPConsumptionNormDTO.getMaterialFkId() != null) {
					aOPConsumptionNorm.setMaterialFkId(UUID.fromString(aOPConsumptionNormDTO.getMaterialFkId()));
				}
				if (aOPConsumptionNormDTO.getPlantFkId() != null) {
					aOPConsumptionNorm.setPlantFkId(UUID.fromString(aOPConsumptionNormDTO.getPlantFkId()));
				}

				aOPConsumptionNormRepository.save(aOPConsumptionNorm);
			}
			// TODO Auto-generated method stub
			return aOPConsumptionNormDTOList;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	@Override
	public AOPMessageVM calculateExpressionConsumptionNorms(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_CalculateConsumptionAOPValues";
			System.out.println(storedProcedure);
			Integer result=  executeDynamicUpdateProcedure(storedProcedure, plantId, site.getId().toString(),
					vertical.getId().toString(), year);
			aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId),year,"consumption-aop");
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("consumption-aop");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("SP Executed successfully");
	        aopMessageVM.setData(result);
	        return aopMessageVM;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return aopMessageVM;
	}

	public int executeDynamicUpdateProcedure(String procedureName, String plantId, String siteId, String verticalId,
			String finYear) {
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
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	@Transactional
	public List<CalculatedConsumptionNormsDTO> getCalculatedConsumptionNorms(String year, String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).orElseThrow();

			List<CalculatedConsumptionNormsDTO> listDTO = new ArrayList<>();
			String storedProcedure = vertical.getName() + "_"+site.getName()+"_CalculateConsumptionAOPValues";
			System.out.println("Executing SP: " + storedProcedure);

			List<Object[]> results = getCalculatedConsumptionNormsSP(storedProcedure, year, plant.getId().toString(),
					site.getId().toString(), vertical.getId().toString());

			for (Object[] row : results) {
				CalculatedConsumptionNormsDTO dto = new CalculatedConsumptionNormsDTO();
				// dto.setId(row[0] != null ? UUID.fromString(row[0].toString()) : null);
				dto.setSiteFkId(row[0] != null ? UUID.fromString(row[0].toString()) : null);
				dto.setVerticalFkId(row[1] != null ? UUID.fromString(row[1].toString()) : null);
				dto.setAopCaseId(row[2] != null ? row[2].toString() : "N/A");
				dto.setAopStatus(row[3] != null ? row[3].toString() : "N/A");
				dto.setAopRemarks(row[4] != null ? row[4].toString() : "");
				dto.setMaterialFkId(row[5] != null ? UUID.fromString(row[5].toString()) : null);

				dto.setJan(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0f);
				dto.setFeb(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0f);
				dto.setMarch(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0f);
				dto.setApril(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0f);
				dto.setMay(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0f);
				dto.setJune(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0f);
				dto.setJuly(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0f);
				dto.setAug(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0f);
				dto.setSep(row[16] != null ? Double.parseDouble(row[16].toString()) : 0.0f);
				dto.setOct(row[17] != null ? Double.parseDouble(row[17].toString()) : 0.0f);
				dto.setNov(row[18] != null ? Double.parseDouble(row[18].toString()) : 0.0f);
				dto.setDec(row[19] != null ? Double.parseDouble(row[19].toString()) : 0.0f);

				dto.setAopYear(row[20] != null ? row[20].toString() : "N/A");
				dto.setPlantFkId(row[21] != null ? UUID.fromString(row[21].toString()) : null);
				dto.setNormParameterTypeDisplayName(row[7] != null ? row[7].toString() : "N/A");
				dto.setUOM(row[6] != null ? row[6].toString() : "");

				listDTO.add(dto);
			}

			// return
			// results.stream().map(this::mapToAopDataDTO).collect(Collectors.toList());
			return listDTO;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional
	public List<Object[]> getCalculatedConsumptionNormsSP(String procedureName, String finYear, String plantId,
			String siteId, String verticalId) {
		try {
			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @finYear = :finYear";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("finYear", finYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional
	public List<Object[]> getAOPConsumptionNormDataFromView(String aopYear, UUID plantFkId,String gradeId) {
		try {
			Plants plant = plantsRepository.findById(plantFkId).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String sql=null;
			String viewName = "vwScrn" + vertical.getName() + "AOPConsumptionNorms";
			if(vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")) {
				 sql = "SELECT * FROM " + viewName + " WHERE Plant_FK_Id = :plantFkId AND AOPYear = :aopYear AND Grade_FK_Id = :gradeId";
			}else {
				 sql = "SELECT * FROM " + viewName + " WHERE Plant_FK_Id = :plantFkId AND AOPYear = :aopYear";
			}

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantFkId", plantFkId);
			query.setParameter("aopYear", aopYear);
			if(vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")) {
				query.setParameter("gradeId", gradeId);
			}
			return query.getResultList(); // Later you can map this to a
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getConsumptionAOPGrades(String financialYear, String plantId) {
		List<Map<String, Object>> gradeList = new ArrayList<>();
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

			String viewName = "vwScrn" + vertical.getName() + "ConsumptionAOPGrade";
			// Validate or sanitize viewName before using it directly in the query to
			// prevent SQL injection
			String sql = "SELECT * FROM " + viewName
					+ " WHERE AOPYear = :financialYear AND Plant_FK_Id = :plantId";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("financialYear", financialYear);
			query.setParameter("plantId", plantId);

			List<Object[]> obj = query.getResultList(); // You can cast this to a DTO later

			for (Object[] result : obj) {
				Map<String, Object> map = new HashMap<>();
				map.put("gradeId", result[0].toString());
				map.put("displayName", result[1].toString());
				map.put("name", result[2].toString());
				map.put("plantId", result[3].toString());
				map.put("aopYear", result[4].toString());
				gradeList.add(map);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(gradeList);
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

}
