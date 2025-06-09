package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;
import com.wks.caseengine.entity.AOPMCCalculatedData;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AOPMCCalculatedDataRepository;
import com.wks.caseengine.repository.AopCalculationRepository;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Connection;

@Service
public class AOPMCCalculatedDataServiceImpl implements AOPMCCalculatedDataService {

	@Autowired
	private AOPMCCalculatedDataRepository aOPMCCalculatedDataRepository;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;

	private DataSource dataSource;

	@Autowired
	private AopCalculationRepository aopCalculationRepository;

	@Autowired
	private ScreenMappingRepository screenMappingRepository;

	// Inject or set your DataSource (e.g., via constructor or setter)
	public AOPMCCalculatedDataServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public AOPMessageVM getAOPMCCalculatedData(String plantId, String year) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			List<Object[]> obj = aOPMCCalculatedDataRepository.getDataMCUValuesAllData(year, plantId);
			List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList = new ArrayList<>();

			for (Object[] row : obj) {
				AOPMCCalculatedDataDTO aOPMCCalculatedDataDTO = new AOPMCCalculatedDataDTO();
				aOPMCCalculatedDataDTO.setId(row[0] != null ? row[0].toString() : null);
				aOPMCCalculatedDataDTO.setSiteFKId(row[1] != null ? row[1].toString() : null);
				aOPMCCalculatedDataDTO.setPlantFKId(row[2] != null ? row[2].toString() : null);
				aOPMCCalculatedDataDTO.setMaterialFKId(row[3] != null ? row[3].toString() : null);
				aOPMCCalculatedDataDTO.setApril(row[4] != null ? Double.parseDouble(row[4].toString()) : null);
				aOPMCCalculatedDataDTO.setMay(row[5] != null ? Double.parseDouble(row[5].toString()) : null);
				aOPMCCalculatedDataDTO.setJune(row[6] != null ? Double.parseDouble(row[6].toString()) : null);
				aOPMCCalculatedDataDTO.setJuly(row[7] != null ? Double.parseDouble(row[7].toString()) : null);
				aOPMCCalculatedDataDTO.setAugust(row[8] != null ? Double.parseDouble(row[8].toString()) : null);
				aOPMCCalculatedDataDTO.setSeptember(row[9] != null ? Double.parseDouble(row[9].toString()) : null);
				aOPMCCalculatedDataDTO.setOctober(row[10] != null ? Double.parseDouble(row[10].toString()) : null);
				aOPMCCalculatedDataDTO.setNovember(row[11] != null ? Double.parseDouble(row[11].toString()) : null);
				aOPMCCalculatedDataDTO.setDecember(row[12] != null ? Double.parseDouble(row[12].toString()) : null);
				aOPMCCalculatedDataDTO.setJanuary(row[13] != null ? Double.parseDouble(row[13].toString()) : null);
				aOPMCCalculatedDataDTO.setFebruary(row[14] != null ? Double.parseDouble(row[14].toString()) : null);
				aOPMCCalculatedDataDTO.setMarch(row[15] != null ? Double.parseDouble(row[15].toString()) : null);
				aOPMCCalculatedDataDTO.setFinancialYear(row[16] != null ? row[16].toString() : null);
				aOPMCCalculatedDataDTO.setRemarks(row[17] != null ? row[17].toString() : " ");
				aOPMCCalculatedDataDTO.setVerticalFKId(row[22] != null ? row[22].toString() : null);
				aOPMCCalculatedDataDTOList.add(aOPMCCalculatedDataDTO);
			}
			Map<String, Object> map = new HashMap<>();

			List<AopCalculation> aopCalculation = aopCalculationRepository.findByPlantIdAndAopYearAndCalculationScreen(
					UUID.fromString(plantId), year, "production-volume-data");
			map.put("aopMCCalculatedDataDTOList", aOPMCCalculatedDataDTOList);
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
	public List<AOPMCCalculatedDataDTO> editAOPMCCalculatedData(
			List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList) {
		try {
			String finYear = "";
			UUID plantId = null;

			for (AOPMCCalculatedDataDTO aOPMCCalculatedDataDTO : aOPMCCalculatedDataDTOList) {
				AOPMCCalculatedData aOPMCCalculatedData = new AOPMCCalculatedData();
				if (aOPMCCalculatedDataDTO.getId() == null || aOPMCCalculatedDataDTO.getId().contains("#")) {
					aOPMCCalculatedData.setId(null);
				} else {
					aOPMCCalculatedData.setId(UUID.fromString(aOPMCCalculatedDataDTO.getId()));
				}
				aOPMCCalculatedData.setPlantFKId(UUID.fromString(aOPMCCalculatedDataDTO.getPlantFKId()));

				plantId = UUID.fromString(aOPMCCalculatedDataDTO.getPlantFKId());

				aOPMCCalculatedData.setSiteFKId(UUID.fromString(aOPMCCalculatedDataDTO.getSiteFKId()));
				aOPMCCalculatedData.setVerticalFKId(UUID.fromString(aOPMCCalculatedDataDTO.getVerticalFKId()));
				aOPMCCalculatedData.setMaterialFKId(UUID.fromString(aOPMCCalculatedDataDTO.getMaterialFKId()));
				aOPMCCalculatedData.setJanuary(aOPMCCalculatedDataDTO.getJanuary());
				aOPMCCalculatedData.setFebruary(aOPMCCalculatedDataDTO.getFebruary());
				aOPMCCalculatedData.setMarch(aOPMCCalculatedDataDTO.getMarch());
				aOPMCCalculatedData.setApril(aOPMCCalculatedDataDTO.getApril());
				aOPMCCalculatedData.setMay(aOPMCCalculatedDataDTO.getMay());
				aOPMCCalculatedData.setJune(aOPMCCalculatedDataDTO.getJune());
				aOPMCCalculatedData.setJuly(aOPMCCalculatedDataDTO.getJuly());
				aOPMCCalculatedData.setAugust(aOPMCCalculatedDataDTO.getAugust());
				aOPMCCalculatedData.setSeptember(aOPMCCalculatedDataDTO.getSeptember());
				aOPMCCalculatedData.setOctober(aOPMCCalculatedDataDTO.getOctober());
				aOPMCCalculatedData.setNovember(aOPMCCalculatedDataDTO.getNovember());
				aOPMCCalculatedData.setDecember(aOPMCCalculatedDataDTO.getDecember());
				aOPMCCalculatedData.setJanuary(aOPMCCalculatedDataDTO.getJanuary());

				aOPMCCalculatedData.setFinancialYear(aOPMCCalculatedDataDTO.getFinancialYear());
				finYear = aOPMCCalculatedDataDTO.getFinancialYear();
				aOPMCCalculatedData.setRemarks(aOPMCCalculatedDataDTO.getRemarks());

				aOPMCCalculatedDataRepository.save(aOPMCCalculatedData);
			}

			List<ScreenMapping> screenMappingList = screenMappingRepository
					.findByDependentScreen("production-volume-data");
			for (ScreenMapping screenMapping : screenMappingList) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(finYear);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId((plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}

			// TODO Auto-generated method stub
			return aOPMCCalculatedDataDTOList;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to edit data", ex);
		}
	}

	@Override
	public AOPMessageVM getAOPMCCalculatedDataSP(String plantId, String finYear) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

			UUID siteId = site.getId();
			UUID verticalId = vertical.getId();
			String storedProcedure = vertical.getName() + "_LoadMCValues";

			String callSql = "{call " + storedProcedure + "(?, ?, ?, ?)}";

			try (Connection connection = dataSource.getConnection();
					CallableStatement stmt = connection.prepareCall(callSql)) {

				// Set parameters in the correct order
				stmt.setString(1, finYear); // @finYear
				stmt.setString(2, plantId); // @plantId
				stmt.setString(3, verticalId.toString()); // @verticalId
				stmt.setString(4, siteId.toString()); // @siteId

				// Execute the stored procedure
				int rowsAffected = stmt.executeUpdate();

				// Optional: commit if auto-commit is off
				if (!connection.getAutoCommit()) {
					connection.commit();
				}

				aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId),
						finYear, "production-volume-data");

				List<ScreenMapping> screenMappingList = screenMappingRepository
						.findByDependentScreen("production-volume-data");
				for (ScreenMapping screenMapping : screenMappingList) {
					if (!screenMapping.getCalculationScreen().equalsIgnoreCase(screenMapping.getDependentScreen())) {

						AopCalculation aopCalculation = new AopCalculation();
						aopCalculation.setAopYear(finYear);
						aopCalculation.setIsChanged(true);
						aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
						aopCalculation.setPlantId(UUID.fromString(plantId));
						aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
						aopCalculationRepository.save(aopCalculation);
					}
				}

				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("SP Executed successfully");
				aopMessageVM.setData(rowsAffected);
				return aopMessageVM;

			} catch (SQLException e) {
				e.printStackTrace();
				return aopMessageVM;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return aopMessageVM;
		}
	}

}
