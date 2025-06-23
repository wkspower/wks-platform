package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AOPDTO;
import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;
import com.wks.caseengine.entity.AOP;
import com.wks.caseengine.entity.AOPMCCalculatedData;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AOPRepository;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.rest.entity.Vertical;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Connection;
import jakarta.persistence.Query;

@Service
public class AOPServiceImpl implements AOPService {

	@Autowired
	private AOPRepository aopRepository;
	@Autowired
	private PlantsRepository plantsRepository;
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private VerticalsRepository verticalRepository;

	@Autowired
	private NormParametersService normParametersService;

	@PersistenceContext
	private EntityManager entityManager;

	private DataSource dataSource;

	@Autowired
	private AopCalculationRepository aopCalculationRepository;

	@Autowired
	private ScreenMappingRepository screenMappingRepository;

	// Inject or set your DataSource (e.g., via constructor or setter)
	public AOPServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public List<AOPDTO> getAOP() {
		try {
			List<AOP> listAOP = aopRepository.findAll();
			List<AOPDTO> aOPList = new ArrayList<>();

			for (AOP aOP : listAOP) {
				AOPDTO aOPDTO = new AOPDTO();
				aOPDTO.setId(aOP.getId().toString());
				aOPDTO.setAopCaseId(aOP.getAopCaseId());
				aOPDTO.setAopRemarks(aOP.getAopRemarks());
				aOPDTO.setAopStatus(aOP.getAopStatus());
				aOPDTO.setAopType(aOP.getAopType());
				aOPDTO.setAopYear(aOP.getAopYear());
				aOPDTO.setApril(aOP.getApril());
				aOPDTO.setAug(aOP.getAug());
				aOPDTO.setAvgTPH(aOP.getAvgTPH());
				aOPDTO.setDec(aOP.getDec());
				aOPDTO.setFeb(aOP.getFeb());
				aOPDTO.setJan(aOP.getJan());
				aOPDTO.setJuly(aOP.getJuly());
				aOPDTO.setJune(aOP.getJune());
				aOPDTO.setMarch(aOP.getMarch());
				aOPDTO.setMay(aOP.getMay());
				// aOPDTO.setNormItem(aOP.getNormItem());
				aOPDTO.setNov(aOP.getNov());
				aOPDTO.setOct(aOP.getOct());
				aOPDTO.setPlantFKId(aOP.getPlantFkId().toString());
				aOPDTO.setSep(aOP.getSep());
				aOPList.add(aOPDTO);
			}

			// TODO Auto-generated method stub
			return aOPList;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getAOPData(String plantId, String year, String type) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<AOPDTO> aOPDTOList = new ArrayList<>();
		try {
			List<Object[]> obj = aopRepository.findByAOPYearAndPlantFkId(year, UUID.fromString(plantId), type);

			for (Object[] row : obj) {
				AOPDTO aopDTO = new AOPDTO();

				aopDTO.setId(row[0] != null ? row[0].toString() : null);
				aopDTO.setNormParameterName(row[1] != null ? row[1].toString() : null);
				aopDTO.setNormParameterDisplayName(row[2] != null ? row[2].toString() : null);
				aopDTO.setNormParameterTypeId(row[3] != null ? row[3].toString() : null);
				aopDTO.setMaterialFKId(row[4] != null ? row[4].toString() : null);
				aopDTO.setDisplayName(row[5] != null ? row[5].toString() : null);

				// Directly parsing Double values

				aopDTO.setApril(row[6] != null ? Double.valueOf(row[6].toString()) : null);
				aopDTO.setMay(row[7] != null ? Double.valueOf(row[7].toString()) : null);
				aopDTO.setJune(row[8] != null ? Double.valueOf(row[8].toString()) : null);
				aopDTO.setJuly(row[9] != null ? Double.valueOf(row[9].toString()) : null);
				aopDTO.setAug(row[10] != null ? Double.valueOf(row[10].toString()) : null);
				aopDTO.setSep(row[11] != null ? Double.valueOf(row[11].toString()) : null);
				aopDTO.setOct(row[12] != null ? Double.valueOf(row[12].toString()) : null);
				aopDTO.setNov(row[13] != null ? Double.valueOf(row[13].toString()) : null);
				aopDTO.setDec(row[14] != null ? Double.valueOf(row[14].toString()) : null);
				aopDTO.setJan(row[15] != null ? Double.valueOf(row[15].toString()) : null);
				aopDTO.setFeb(row[16] != null ? Double.valueOf(row[16].toString()) : null);
				aopDTO.setMarch(row[17] != null ? Double.valueOf(row[17].toString()) : null);
				aopDTO.setAvgTPH(row[18] != null ? Double.valueOf(row[18].toString()) : null);
				aopDTO.setRemark(row[19] != null ? row[19].toString() : null);
				aopDTO.setDisplayOrder(row[20] != null ? Integer.valueOf(row[20].toString()) : null);
				aopDTO.setIsEditable(row[21] != null ? Boolean.valueOf(row[21].toString()) : null);
				aopDTO.setIsVisible(row[22] != null ? Boolean.valueOf(row[22].toString()) : null);

				aOPDTOList.add(aopDTO);
			}
			Map<String, Object> map = new HashMap<>();

			List<AopCalculation> aopCalculation = aopCalculationRepository
					.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "production-aop");
			map.put("aopDTOList", aOPDTOList);
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

	@Override
	public List<AOPDTO> updateAOP(List<AOPDTO> aOPDTOList) {
		try {
			for (AOPDTO aOPDTO : aOPDTOList) {

				AOP aOP = null;
				Plants plant = null;
				Sites site = null;
				Verticals vertical = null;
				if (aOPDTO.getSiteFKId() == null || aOPDTO.getVerticalFKId() == null) {
					plant = plantsRepository.findById(UUID.fromString(aOPDTO.getPlantFKId())).get();
					site = siteRepository.findById(plant.getSiteFkId()).get();
					vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
				}

				if (aOPDTO.getId() == null) {
					UUID Site = null;
					UUID Vertical = null;
					UUID Material = null;
					UUID Plant = null;
					if (aOPDTO.getSiteFKId() != null) {
						Site = UUID.fromString(aOPDTO.getSiteFKId());
					} else {
						Site = site.getId();
					}
					if (aOPDTO.getVerticalFKId() != null) {
						Vertical = UUID.fromString(aOPDTO.getVerticalFKId());
					} else {
						Vertical = vertical.getId();
					}
					if (aOPDTO.getMaterialFKId() != null) {
						Material = UUID.fromString(aOPDTO.getMaterialFKId());
					}
					if (aOPDTO.getPlantFKId() != null) {
						Plant = UUID.fromString(aOPDTO.getPlantFKId());
					}
					Optional<UUID> Id = aopRepository.findAopIdByFilters(Site, Vertical, Material, Plant,
							aOPDTO.getAopYear());
					aOP = new AOP();
					if (Id != null && !Id.isEmpty()) {
						aOP.setId(Id.get());
					}

					String caseId = aOPDTO.getAopYear() + "-AOP-" + "-V1";
					aOP.setAopStatus("draft");
					aOP.setAopType("production");
					aOP.setAopCaseId(caseId);
				} else if (aOPDTO.getId().contains("#")) {
					aOP = new AOP();
					aOP.setId(null);
					String caseId = aOPDTO.getAopYear() + "-AOP-" + "-V1";
					aOP.setAopStatus("draft");
					aOP.setAopType("production");
					aOP.setAopCaseId(caseId);
				} else {
					aOP = aopRepository.findById(UUID.fromString(aOPDTO.getId())).get();
					aOP.setAopCaseId(aOPDTO.getAopCaseId());
					aOP.setAopStatus(aOPDTO.getAopStatus());
					aOP.setAopType(aOPDTO.getAopType());
				}
				aOP.setAopRemarks(aOPDTO.getAopRemarks());
				aOP.setAopType(aOPDTO.getAopType());
				aOP.setAopYear(aOPDTO.getAopYear());
				aOP.setApril(aOPDTO.getApril());
				aOP.setAug(aOPDTO.getAug());
				aOP.setAvgTPH(aOPDTO.getAvgTPH());
				aOP.setDec(aOPDTO.getDec());
				aOP.setFeb(aOPDTO.getFeb());
				aOP.setJan(aOPDTO.getJan());
				aOP.setJuly(aOPDTO.getJuly());
				aOP.setJune(aOPDTO.getJune());
				aOP.setMarch(aOPDTO.getMarch());
				aOP.setMay(aOPDTO.getMay());
				// aOP.setNormItem(aOPDTO.getNormItem());
				aOP.setNov(aOPDTO.getNov());
				aOP.setOct(aOPDTO.getOct());

				if (aOPDTO.getSiteFKId() != null) {
					aOP.setSiteFkId(UUID.fromString(aOPDTO.getSiteFKId()));
				} else {
					aOP.setSiteFkId(site.getId());
				}
				if (aOPDTO.getVerticalFKId() != null) {
					aOP.setVerticalFkId(UUID.fromString(aOPDTO.getVerticalFKId()));
				} else {
					aOP.setVerticalFkId(vertical.getId());
				}
				if (aOPDTO.getMaterialFKId() != null) {
					aOP.setMaterialFKId(UUID.fromString(aOPDTO.getMaterialFKId()));
				}
				if (aOPDTO.getPlantFKId() != null) {
					aOP.setPlantFkId(UUID.fromString(aOPDTO.getPlantFKId()));
				}

				aOP.setSep(aOPDTO.getSep());
				aOP.setAopYear(aOPDTO.getAopYear());
				aopRepository.save(aOP);
			}
			return aOPDTOList;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	@Override
	@Transactional
	public AOPMessageVM calculateData(String plantId, String year) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		String verticalName = plantsRepository.findVerticalNameByPlantId(plant.getId());
		// if(verticalName.equalsIgnoreCase("MEG")) {
		Integer result = executeDynamicMaintenanceCalculationMEG(verticalName, plant.getId().toString(),
				site.getId().toString(), vertical.getId().toString(), year);
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Data fetched successfully");
		aopMessageVM.setData(result);
		// }

		// else if(verticalName.equalsIgnoreCase("PE")) {
		// List<AOPDTO> result= calculateDataForPE(plant.getId().toString(),year);
		// aopMessageVM.setCode(200);
		// aopMessageVM.setMessage("Data fetched successfully");
		// aopMessageVM.setData(result);
		// }
		aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year,
				"production-aop");
		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("production-aop");
		for (ScreenMapping screenMapping : screenMappingList) {
			AopCalculation aopCalculation = new AopCalculation();
			aopCalculation.setAopYear(year);
			aopCalculation.setIsChanged(true);
			aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
			aopCalculation.setPlantId(UUID.fromString(plantId));
			aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
			aopCalculationRepository.save(aopCalculation);
		}
		return aopMessageVM;
	}

	@Override
	public List<AOPDTO> calculateDataForPE(String plantId, String year) {
		List<AOPDTO> dtoList = new ArrayList<>();
		try {
			List<Object[]> maintainsData = aopRepository.CheckIfMaintainanceDataExists(plantId, year);

			// if(maintainsData!=null && maintainsData.size()>0){
			if (1 == 1) {
				Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
				Sites site = siteRepository.findById(plant.getSiteFkId()).get();
				Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
				String verticalName = plantsRepository.findVerticalNameByPlantId(plant.getId());

				List<Object[]> list = executeDynamicMaintenanceCalculationPE(
						verticalName,
						plant.getId().toString(),
						site.getId().toString(),
						vertical.getId().toString(),
						year);

				System.out.println("list" + list);
				System.out.println("listTo String" + list.toString());

				List<AOP> objList = aopRepository.findAllByAopYearAndPlantFkId(year, UUID.fromString(plantId));
				System.out.println("objList" + objList);
				System.out.println("objList String" + objList.toString());

				for (Object[] obj : list) {
					System.out.println("obj" + obj.toString());
					System.out.println("obj[0].toString()" + obj[0].toString());
					System.out.println("obj[0]" + obj[0]);

					AOPDTO aopDto = new AOPDTO();
					aopDto.setAopCaseId("");
					aopDto.setAopRemarks("");
					aopDto.setId("");
					aopDto.setPlantFKId(plantId);
					aopDto.setAopStatus("Draft");
					aopDto.setAopYear(year);
					aopDto.setMaterialFKId(obj[0] != null ? obj[0].toString() : null);
					aopDto.setSiteFKId(site.getId().toString());

					aopDto.setJan(obj[3] != null ? Double.parseDouble(obj[3].toString()) : null);
					aopDto.setFeb(obj[4] != null ? Double.parseDouble(obj[4].toString()) : null);
					aopDto.setMarch(obj[5] != null ? Double.parseDouble(obj[5].toString()) : null);
					aopDto.setApril(obj[6] != null ? Double.parseDouble(obj[6].toString()) : null);
					aopDto.setMay(obj[7] != null ? Double.parseDouble(obj[7].toString()) : null);
					aopDto.setJune(obj[8] != null ? Double.parseDouble(obj[8].toString()) : null);
					aopDto.setJuly(obj[9] != null ? Double.parseDouble(obj[9].toString()) : null);
					aopDto.setAug(obj[10] != null ? Double.parseDouble(obj[10].toString()) : null);
					aopDto.setSep(obj[11] != null ? Double.parseDouble(obj[11].toString()) : null);
					aopDto.setOct(obj[12] != null ? Double.parseDouble(obj[12].toString()) : null);
					aopDto.setNov(obj[13] != null ? Double.parseDouble(obj[13].toString()) : null);
					aopDto.setDec(obj[14] != null ? Double.parseDouble(obj[14].toString()) : null);

					dtoList.add(aopDto);
				}
			}

			return dtoList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public Integer executeDynamicMaintenanceCalculationMEG(String verticalName, String plantId, String siteId,
			String verticalId, String aopYear) {
		try {

			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			String procedureName = verticalName + "_" + site.getName() + "_MaintenanceCalculation";

			String callSql = "{call " + procedureName + "(?, ?, ?, ?)}";

			try (Connection connection = dataSource.getConnection();
					CallableStatement stmt = connection.prepareCall(callSql)) {

				// Set parameters in the correct order
				stmt.setString(1, plantId); // @finYear
				stmt.setString(2, siteId.toString()); // @plantId
				stmt.setString(3, verticalId.toString()); // @verticalId
				stmt.setString(4, aopYear); // @siteId

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
	public List<Object[]> executeDynamicMaintenanceCalculationPE(String verticalName, String plantId, String siteId,
			String verticalId, String aopYear) {
		try {

			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			String procedureName = verticalName + "_" + site.getName() + "_MaintenanceCalculation";

			// Create a native query to execute the stored procedure
			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @aopYear = :aopYear";

			Query query = entityManager.createNativeQuery(sql);

			// Set parameters
			query.setParameter("plantId", plantId);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public List<Map<String, String>> getAOPYears() {
		try {
			List<Object[]> results = aopRepository.getAOPYears();
			List<Map<String, String>> aopYears = new ArrayList<>();

			for (Object[] row : results) {
				Map<String, String> map = new HashMap<>();
				map.put("AOPYear", row[0] != null ? row[0].toString() : null);
				map.put("AOPDisplayYear", row[1] != null ? row[1].toString() : null);
				map.put("currentYear", row[2] != null ? row[2].toString() : null);
				aopYears.add(map);
			}

			return aopYears;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

}
