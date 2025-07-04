package com.wks.caseengine.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.wks.caseengine.dto.DecokePlanningIBRDTO;
import com.wks.caseengine.dto.DecokeRunLengthDTO;
import com.wks.caseengine.dto.DecokingActivitiesDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.DecokeRunLength;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.DecokeRunLengthRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class DecokingActivitiesServiceImpl implements DecokingActivitiesService {

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;

	@Autowired
	private ScreenMappingRepository screenMappingRepository;

	@Autowired
	private AopCalculationRepository aopCalculationRepository;

	@Autowired
	private DecokeRunLengthRepository decokeRunLengthRepository;

	private DataSource dataSource;

	// Inject or set your DataSource (e.g., via constructor or setter)
	public DecokingActivitiesServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public AOPMessageVM getDecokingActivitiesData(String year, String plantId, String reportType) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> decokingActivitiesList = new ArrayList<>();
		List<DecokeRunLengthDTO> runLengthDTOs = new ArrayList<>();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			String procedureName = null;
			List<Object[]> results = null;
			if (reportType.equalsIgnoreCase("RunningDuration")) {
				procedureName = "vwScrn" + vertical.getName() + "_" + site.getName() + "_DecokingPlanning";
				results = getData(plantId, procedureName);
			} else if (reportType.equalsIgnoreCase("ibr")) {
				procedureName = "vwScrn" + vertical.getName() + "_" + site.getName() + "_DecokePlanningDates";
				results = getIBRData(plantId, procedureName);
			} else if (reportType.equalsIgnoreCase("RunLength")) {
				procedureName = "vwScrn" + vertical.getName() + "_" + site.getName() + "_Decoke_RunLength";
				results = getRunLengthData(plantId, year, procedureName);
			}

			for (Object[] row : results) {
				Map<String, Object> map = new HashMap<>(); // Create a new map for each row
				if (reportType.equalsIgnoreCase("RunningDuration")) {
					map.put("normParameterId", row[0]);
					map.put("name", row[1]);
					map.put("displayName", row[2]);
					map.put("isEditable", row[13]);
					map.put("isMonthAdd", row[16]);
					Object raw = row[0];
					UUID id = UUID.fromString(raw.toString());
					Optional<NormAttributeTransactions> normAttributeTransactionsopt = normAttributeTransactionsRepository
							.findByNormParameterFKId(id);
					if (normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions = normAttributeTransactionsopt.get();
						map.put("attributeValue", normAttributeTransactions.getAttributeValue());
						map.put("remarks", normAttributeTransactions.getRemarks());
						map.put("id", normAttributeTransactions.getId());
						map.put("month", getMonth(normAttributeTransactions.getAopMonth()));
					} else {
						map.put("remarks", "");
						map.put("id", "");
					}
				} else if (reportType.equalsIgnoreCase("ibr")) {
					map.put("furnace", row[0] != null ? row[0] : "");
					map.put("plantId", row[1] != null ? row[1] : "");
					map.put("ibrSDId", row[2] != null ? row[2] : "");
					map.put("ibrEDId", row[3] != null ? row[3] : "");
					map.put("taSDId", row[4] != null ? row[4] : "");
					map.put("taEDId", row[5] != null ? row[5] : "");
					map.put("sdSDId", row[6] != null ? row[6] : "");
					map.put("sdEDId", row[7] != null ? row[7] : "");
					map.put("ibrSD", row[8] != null ? row[8] : "");
					map.put("ibrED", row[9] != null ? row[9] : "");
					map.put("taSD", row[10] != null ? row[10] : "");
					map.put("taED", row[11] != null ? row[11] : "");
					map.put("sdSD", row[12] != null ? row[12] : "");
					map.put("sdED", row[13] != null ? row[13] : "");
					map.put("remarks", "");
				} else if (reportType.equalsIgnoreCase("activity")) {
					map.put("furnace", row[0]);
					map.put("startDateIBR", row[1]);
					map.put("endDateIBR", row[2]);
					map.put("startDateSD", row[3]);
					map.put("endDateSD", row[4]);
					map.put("startDateTA", row[5]);
					map.put("endDateTA", row[6]);
					map.put("remarks", row[7]);
				} else if (reportType.equalsIgnoreCase("RunLength")) {
					DecokeRunLengthDTO dto = new DecokeRunLengthDTO();
					dto.setId(row[0] != null ? row[0].toString() : "");
					dto.setDate(row[1] != null ? row[1].toString() : "");
					dto.setMonth(row[2] != null ? row[2].toString() : "");
					dto.setHTenActual(row[3] != null ? row[3].toString() : "");
					dto.setTenProposed(row[4] != null ? row[4].toString() : "");
					dto.setElevenProposed(row[6] != null ? row[6].toString() : "");
					dto.setHElevenActual(row[5] != null ? row[5].toString() : "");
					dto.setTwelveProposed(row[8] != null ? row[8].toString() : "");
					dto.setHTwelveActual(row[7] != null ? row[7].toString() : "");
					dto.setThirteenProposed(row[10] != null ? row[10].toString() : "");
					dto.setHThirteenActual(row[9] != null ? row[9].toString() : "");
					dto.setFourteenProposed(row[12] != null ? row[12].toString() : "");
					dto.setHFourteenActual(row[11] != null ? row[11].toString() : "");
					dto.setDemo(row[13] != null ? row[13].toString() : "");
					dto.setAopYear(year);
					dto.setPlantFkId(row[15] != null ? row[15].toString() : "");
					dto.setRemarks(row[16] != null ? row[16].toString() : "");
					runLengthDTOs.add(dto);
				}
				decokingActivitiesList.add(map); // Add the map to the list here
			}
			Map<String, Object> aopCalculationMap = new HashMap<>();
			if (reportType.equalsIgnoreCase("RunLength")) {
				List<AopCalculation> aopCalculation = aopCalculationRepository
						.findByPlantIdAndAopYearAndCalculationScreen(
								UUID.fromString(plantId), year, "Furnace-run-length");

				aopCalculationMap.put("aopCalculation", aopCalculation);
				aopCalculationMap.put("decokingActivitiesList", runLengthDTOs);
				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("Data fetched successfully");
				aopMessageVM.setData(aopCalculationMap);
				return aopMessageVM;
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(decokingActivitiesList);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> getData(String plantId, String aopYear, String reportType, String procedureName) {
		try {

			String sql = "EXEC " + procedureName
					+ " @PlantFKId = :plantId, @AuditYear = :aopYear, @ConfigTypeName = :reportType";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);
			query.setParameter("reportType", reportType);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> getData(String plantId, String viewName) {
		try {

			// 2. Construct SQL with dynamic view name
			String sql = "SELECT * FROM " + viewName +
					" WHERE Plant_FK_Id = :plantId";

			// 3. Create and parameterize the native query
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);

			// 4. Execute
			return query.getResultList();

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid argument: " + e.getMessage(), e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data from view " + viewName, ex);
		}
	}

	public List<Object[]> getRunLengthData(String plantId, String aopYear, String viewName) {
		try {

			// 2. Construct SQL with dynamic view name
			String sql = "SELECT * FROM " + viewName +
					" WHERE Plant_FK_Id = :plantId AND AOPYear = :aopYear";

			// 3. Create and parameterize the native query
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);

			// 4. Execute
			return query.getResultList();

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid argument: " + e.getMessage(), e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data from view " + viewName, ex);
		}
	}

	public List<Object[]> getIBRData(String plantId, String viewName) {
		try {

			// 2. Construct SQL with dynamic view name
			String sql = "SELECT * FROM " + viewName +
					" WHERE PlantId = :plantId";

			// 3. Create and parameterize the native query
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);

			// 4. Execute
			return query.getResultList();

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid argument: " + e.getMessage(), e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data from view " + viewName, ex);
		}
	}

	public static String getMonth(Integer month) {
		if (month == null) {
			return "Invalid month";
		}
		switch (month) {
			case 1:
				return "January";
			case 2:
				return "February";
			case 3:
				return "March";
			case 4:
				return "April";
			case 5:
				return "May";
			case 6:
				return "June";
			case 7:
				return "July";
			case 8:
				return "August";
			case 9:
				return "September";
			case 10:
				return "October";
			case 11:
				return "November";
			case 12:
				return "December";
			default:
				return "Invalid month";
		}
	}

	@Override
	public AOPMessageVM updateDecokingActivitiesData(String year, String plantId, String reportType,
			List<DecokingActivitiesDTO> decokingActivitiesDTOList) {
		List<NormAttributeTransactions> normAttributeTransactionsList = new ArrayList<>();
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			for (DecokingActivitiesDTO decokingActivitiesDTO : decokingActivitiesDTOList) {
				if (decokingActivitiesDTO.getId() != null) {
					Optional<NormAttributeTransactions> normAttributeTransactionsopt = normAttributeTransactionsRepository
							.findById(UUID.fromString(decokingActivitiesDTO.getId()));
					if (normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions = normAttributeTransactionsopt.get();
						normAttributeTransactions.setAttributeValue(decokingActivitiesDTO.getDays());
						if (decokingActivitiesDTO.getAopMonth() != null) {
							normAttributeTransactions.setAopMonth(decokingActivitiesDTO.getAopMonth());
						} else {
							normAttributeTransactions.setAopMonth(0);
						}

						normAttributeTransactions.setRemarks(decokingActivitiesDTO.getRemarks());
						normAttributeTransactionsList
								.add(normAttributeTransactionsRepository.save(normAttributeTransactions));
					}
				} else {
					NormAttributeTransactions normAttributeTransactions = new NormAttributeTransactions();
					normAttributeTransactions.setAttributeValue(decokingActivitiesDTO.getDays());
					if (decokingActivitiesDTO.getAopMonth() != null) {
						normAttributeTransactions.setAopMonth(decokingActivitiesDTO.getAopMonth());
					} else {
						normAttributeTransactions.setAopMonth(0);
					}
					normAttributeTransactions.setRemarks(decokingActivitiesDTO.getRemarks());
					normAttributeTransactions.setAuditYear(year);
					normAttributeTransactions.setCreatedOn(new Date());
					normAttributeTransactions.setAttributeValueVersion("V1");
					normAttributeTransactions
							.setNormParameterFKId(UUID.fromString(decokingActivitiesDTO.getNormParameterId()));
					normAttributeTransactions.setUserName("System");
					normAttributeTransactionsList
							.add(normAttributeTransactionsRepository.save(normAttributeTransactions));
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException("Failed to update data");
		}

		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("ibr");
		for (ScreenMapping screenMapping : screenMappingList) {
			AopCalculation aopCalculation = new AopCalculation();
			aopCalculation.setAopYear(year);
			aopCalculation.setIsChanged(true);
			aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
			aopCalculation.setPlantId(UUID.fromString(plantId));
			aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
			aopCalculationRepository.save(aopCalculation);
		}

		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Data Updated successfully");
		aopMessageVM.setData(normAttributeTransactionsList);
		return aopMessageVM;
	}

	@Override
	public AOPMessageVM updateDecokingActivitiesIBRData(String year, String plantId, String reportType,
			List<DecokePlanningIBRDTO> decokePlanningIBRDTOList) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			for (DecokePlanningIBRDTO decokePlanningIBRDTO : decokePlanningIBRDTOList) {
				if (decokePlanningIBRDTO.getIbrEDId() != null && decokePlanningIBRDTO.getIbrEDId() != "") {
					Optional<NormAttributeTransactions> normAttributeTransactionsopt = normAttributeTransactionsRepository
							.findByNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getIbrEDId()));
					if (normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions = normAttributeTransactionsopt.get();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getIbrED());
						normAttributeTransactions.setModifiedOn(new Date());
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					} else {
						NormAttributeTransactions normAttributeTransactions = new NormAttributeTransactions();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getIbrED());
						normAttributeTransactions.setAttributeValueVersion("V1");
						normAttributeTransactions.setAuditYear(year);
						normAttributeTransactions.setCreatedOn(new Date());
						normAttributeTransactions
								.setNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getIbrEDId()));
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactions.setUserName("System");
						normAttributeTransactions.setAopMonth(0);
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}
				}
				if (decokePlanningIBRDTO.getIbrSDId() != null && decokePlanningIBRDTO.getIbrSDId() != "") {
					Optional<NormAttributeTransactions> normAttributeTransactionsopt = normAttributeTransactionsRepository
							.findByNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getIbrSDId()));
					if (normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions = normAttributeTransactionsopt.get();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getIbrSD());
						normAttributeTransactions.setModifiedOn(new Date());
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					} else {
						NormAttributeTransactions normAttributeTransactions = new NormAttributeTransactions();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getIbrSD());
						normAttributeTransactions.setAttributeValueVersion("V1");
						normAttributeTransactions.setAuditYear(year);
						normAttributeTransactions.setCreatedOn(new Date());
						normAttributeTransactions
								.setNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getIbrSDId()));
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactions.setUserName("System");
						normAttributeTransactions.setAopMonth(0);
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}
				}
				if (decokePlanningIBRDTO.getTaSDId() != null && decokePlanningIBRDTO.getTaSDId() != "") {
					Optional<NormAttributeTransactions> normAttributeTransactionsopt = normAttributeTransactionsRepository
							.findByNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getTaSDId()));
					if (normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions = normAttributeTransactionsopt.get();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getTaSD());
						normAttributeTransactions.setModifiedOn(new Date());
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					} else {
						NormAttributeTransactions normAttributeTransactions = new NormAttributeTransactions();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getTaSD());
						normAttributeTransactions.setAttributeValueVersion("V1");
						normAttributeTransactions.setAuditYear(year);
						normAttributeTransactions.setCreatedOn(new Date());
						normAttributeTransactions
								.setNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getTaSDId()));
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactions.setUserName("System");
						normAttributeTransactions.setAopMonth(0);
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}
				}
				if (decokePlanningIBRDTO.getTaEDId() != null && decokePlanningIBRDTO.getTaEDId() != "") {
					Optional<NormAttributeTransactions> normAttributeTransactionsopt = normAttributeTransactionsRepository
							.findByNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getTaEDId()));
					if (normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions = normAttributeTransactionsopt.get();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getTaED());
						normAttributeTransactions.setModifiedOn(new Date());
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					} else {
						NormAttributeTransactions normAttributeTransactions = new NormAttributeTransactions();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getTaED());
						normAttributeTransactions.setAttributeValueVersion("V1");
						normAttributeTransactions.setAuditYear(year);
						normAttributeTransactions.setCreatedOn(new Date());
						normAttributeTransactions
								.setNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getTaEDId()));
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactions.setUserName("System");
						normAttributeTransactions.setAopMonth(0);
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}
				}
				if (decokePlanningIBRDTO.getSdSDId() != null && decokePlanningIBRDTO.getSdSDId() != "") {
					Optional<NormAttributeTransactions> normAttributeTransactionsopt = normAttributeTransactionsRepository
							.findByNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getSdSDId()));
					if (normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions = normAttributeTransactionsopt.get();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getSdSD());
						normAttributeTransactions.setModifiedOn(new Date());
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					} else {
						NormAttributeTransactions normAttributeTransactions = new NormAttributeTransactions();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getSdSD());
						normAttributeTransactions.setAttributeValueVersion("V1");
						normAttributeTransactions.setAuditYear(year);
						normAttributeTransactions.setCreatedOn(new Date());
						normAttributeTransactions
								.setNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getSdSDId()));
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactions.setUserName("System");
						normAttributeTransactions.setAopMonth(0);
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}
				}
				if (decokePlanningIBRDTO.getSdEDId() != null && decokePlanningIBRDTO.getSdEDId() != "") {
					Optional<NormAttributeTransactions> normAttributeTransactionsopt = normAttributeTransactionsRepository
							.findByNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getSdEDId()));
					if (normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions = normAttributeTransactionsopt.get();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getSdED());
						normAttributeTransactions.setModifiedOn(new Date());
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					} else {
						NormAttributeTransactions normAttributeTransactions = new NormAttributeTransactions();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getSdED());
						normAttributeTransactions.setAttributeValueVersion("V1");
						normAttributeTransactions.setAuditYear(year);
						normAttributeTransactions.setCreatedOn(new Date());
						normAttributeTransactions
								.setNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getSdEDId()));
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactions.setUserName("System");
						normAttributeTransactions.setAopMonth(0);
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}
				}

			}
		} catch (Exception ex) {
			throw new RuntimeException("Failed to update data");
		}
		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("sd-ta-activity");
		for (ScreenMapping screenMapping : screenMappingList) {
			AopCalculation aopCalculation = new AopCalculation();
			aopCalculation.setAopYear(year);
			aopCalculation.setIsChanged(true);
			aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
			aopCalculation.setPlantId(UUID.fromString(plantId));
			aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
			aopCalculationRepository.save(aopCalculation);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Data Updated successfully");
		aopMessageVM.setData(decokePlanningIBRDTOList);
		return aopMessageVM;
	}

	public byte[] createExcel(String year, String plantId, String reportType, boolean isAfterSave,
			List<DecokeRunLengthDTO> decokingActivitiesList) {
		try {
			System.out.println("Started the createExcel");

			if (!isAfterSave) {
				AOPMessageVM dataVM = getDecokingActivitiesData(year, plantId, reportType);
				Map<String, Object> aopCalculationMap = (Map<String, Object>) dataVM.getData();
				decokingActivitiesList = (List<DecokeRunLengthDTO>) aopCalculationMap.get("decokingActivitiesList");
			}

			Workbook workbook = new XSSFWorkbook();
			CellStyle borderStyle = createBorderedStyle(workbook);
			CellStyle boldStyle = createBoldStyle(workbook);
			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;
			// List<List<Object>> rows = new ArrayList<>();

			List<List<Object>> rows = new ArrayList<>();
			// Data rows

			for (DecokeRunLengthDTO dto : decokingActivitiesList) {
				List<Object> list = new ArrayList<>();
				list.add(dto.getId());
				list.add(dto.getMonth());
				list.add(dto.getDate());
				list.add(dto.getHTenActual());
				list.add(dto.getTenProposed());
				list.add(dto.getHElevenActual());
				list.add(dto.getElevenProposed());
				list.add(dto.getHTwelveActual());
				list.add(dto.getTwelveProposed());
				list.add(dto.getHThirteenActual());
				list.add(dto.getThirteenProposed());
				list.add(dto.getHFourteenActual());
				list.add(dto.getFourteenProposed());
				list.add(dto.getDemo());
				// map.get("aopYear");
				// map.get("plantId");
				// map.get("remark");
				// list.add(map.get("remark"));
				if (isAfterSave) {
					list.add(dto.getSaveStatus());
					list.add(dto.getErrDescription());
				}
				rows.add(list);
			}

			List<String> innerHeaders = new ArrayList<>();
			innerHeaders.add("Id");
			innerHeaders.add("Month");
			innerHeaders.add("Date");
			innerHeaders.add("H10 - Actual run length");
			innerHeaders.add("H10 - Proposed AOP");
			innerHeaders.add("H11 - Actual run length");
			innerHeaders.add("H11 - Proposed AOP");
			innerHeaders.add("H12 - Actual run length");
			innerHeaders.add("H12 - Proposed AOP");
			innerHeaders.add("H13 - Actual run length");
			innerHeaders.add("H13 - Proposed AOP");
			innerHeaders.add("H14 - Actual run length");
			innerHeaders.add("H14 - Proposed AOP");
			innerHeaders.add("DEMO");
			if (isAfterSave) {
				innerHeaders.add("Status");
				innerHeaders.add("Error Description");
			}
			CellStyle lockedStyle = workbook.createCellStyle();
			lockedStyle.setLocked(true);
			lockedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			lockedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			List<List<String>> headers = new ArrayList<>();
			headers.add(innerHeaders);

			for (List<String> headerRowData : headers) {
				Row headerRow = sheet.createRow(currentRow++);
				for (int col = 0; col < headerRowData.size(); col++) {
					Cell cell = headerRow.createCell(col);
					cell.setCellValue(headerRowData.get(col));
					cell.setCellStyle(createBoldBorderedStyle(workbook));
				}
			}
			for (List<Object> rowData : rows) {
				Row row = sheet.createRow(currentRow++);
				for (int col = 0; col < rowData.size(); col++) {
					Cell cell = row.createCell(col);
					Object value = rowData.get(col);

					if (value instanceof Number) {
						cell.setCellValue(((Number) value).doubleValue()); // Handles Integer, Double, etc.
					} else if (value instanceof Boolean) {
						cell.setCellValue((Boolean) value);
					} else if (value != null) {
						cell.setCellValue(value.toString());
					} else {
						cell.setCellValue("");
					}
					if (col == 3 || col == 5 || col == 7 || col == 9 || col == 11) {
						cell.setCellStyle(lockedStyle);
					}

				}
			}
			sheet.setColumnHidden(0, true);

			try {// (FileOutputStream fileOut = new FileOutputStream("output/generated.xlsx")) {

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				workbook.write(outputStream);
				workbook.close();
				return outputStream.toByteArray();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Ended the createExcel");
		return null;

	}

	private static String formatMonthYear(int month, int year) {
		LocalDate date = LocalDate.of(year, month, 1);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH);
		return date.format(formatter);
	}

	public static List<String> getAcademicYearMonths(String year) {
		List<String> months = new ArrayList<>();
		int startYear = Integer.parseInt(year.substring(0, 4));
		int nextYear = startYear + 1;

		// Apr to Dec of startYear
		for (int month = 4; month <= 12; month++) {
			String label = formatMonthYear(month, startYear);
			months.add(label);
		}

		// Jan to Mar of nextYear
		for (int month = 1; month <= 3; month++) {
			String label = formatMonthYear(month, nextYear);
			months.add(label);
		}

		return months;
	}

	private CellStyle createBorderedStyle(Workbook wb) {
		CellStyle style = wb.createCellStyle();
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		return style;
	}

	private CellStyle createBoldStyle(Workbook wb) {
		Font font = wb.createFont();
		font.setBold(true);
		CellStyle style = wb.createCellStyle();
		style.setFont(font);
		return style;
	}

	private CellStyle createBoldBorderedStyle(Workbook workbook) {
		CellStyle style = createBorderedStyle(workbook);
		Font font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);
		return style;
	}

	@Override
	public AOPMessageVM importExcel(String year, UUID plantFKId, String reportType, MultipartFile file) {
		// TODO Auto-generated method stub
		if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
			throw new IllegalArgumentException("Invalid or empty Excel file.");
		}

		try {

			System.out.println("started Read run length in importExcel");
			List<DecokeRunLengthDTO> data = readExcel(file.getInputStream(), plantFKId, year);
			System.out.println("Ended Read run length in importExcel");
			System.out.println("Started Save run length in importExcel");
			AOPMessageVM vm = updateDecokingActivitiesRunLengthData(year, plantFKId.toString(), reportType, data);
			List<DecokeRunLengthDTO> failedRecords = (List<DecokeRunLengthDTO>) vm.getData();
			System.out.println("Ended Save run length in importExcel");
			AOPMessageVM aopMessageVM = new AOPMessageVM();

			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = createExcel(year, plantFKId.toString(), reportType, true, failedRecords);
				String base64File = Base64.getEncoder().encodeToString(fileByteArray);
				aopMessageVM.setData(base64File);
				aopMessageVM.setCode(400);
				aopMessageVM.setMessage("Partial data has been saved");
			} else {
				// aopMessageVM.setData();
				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("All data has been saved");
			}

			return aopMessageVM;
			// return ResponseEntity.ok(data);
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<DecokeRunLengthDTO> readExcel(InputStream inputStream, UUID plantFKId, String year) {
		List<DecokeRunLengthDTO> runLengthList = new ArrayList<>();

		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			int firstRow = sheet.getFirstRowNum();
			int lastRow = sheet.getLastRowNum();

			System.out.println("firstRow" + firstRow);
			System.out.println("lastRow" + lastRow);

			if (rowIterator.hasNext())
				rowIterator.next(); // Skip header

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				DecokeRunLengthDTO dto = new DecokeRunLengthDTO();

				try {

					dto.setId(getStringCellValue(row.getCell(0), dto));

					dto.setMonth(getStringCellValue(row.getCell(1), dto));
					dto.setDate(getStringCellValue(row.getCell(2), dto));
					// dto.setDate( null);

					dto.setHTenActual(getStringCellValue(row.getCell(3), dto));
					dto.setTenProposed(getStringCellValue(row.getCell(4), dto));
					dto.setHElevenActual(getStringCellValue(row.getCell(5), dto));
					dto.setElevenProposed(getStringCellValue(row.getCell(6), dto));
					dto.setHTwelveActual(getStringCellValue(row.getCell(7), dto));
					dto.setTwelveProposed(getStringCellValue(row.getCell(8), dto));

					dto.setHThirteenActual(getStringCellValue(row.getCell(9), dto));
					dto.setThirteenProposed(getStringCellValue(row.getCell(10), dto));
					dto.setHFourteenActual(getStringCellValue(row.getCell(11), dto));
					dto.setFourteenProposed(getStringCellValue(row.getCell(12), dto));

					dto.setDemo(getStringCellValue(row.getCell(13), dto));

				} catch (Exception e) {
					e.printStackTrace();
					dto.setErrDescription(e.getMessage());
					dto.setSaveStatus("Failed");
				}

				runLengthList.add(dto);
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to read Data", e);
		}

		return runLengthList;
	}

	private static String getStringCellValue(Cell cell, DecokeRunLengthDTO dto) {
		try {
			if (cell == null)
				return null;
			cell.setCellType(CellType.STRING);
			return cell.getStringCellValue().trim();
		} catch (Exception e) {
			dto.setSaveStatus("Failed");
			dto.setErrDescription("Please enter correct values");
			e.printStackTrace();
		}
		return null;

	}

	private static Double getNumericCellValue(Cell cell, DecokeRunLengthDTO dto) {
		if (cell == null)
			return null;
		if (cell.getCellType() == CellType.NUMERIC) {
			return cell.getNumericCellValue();
		} else if (cell.getCellType() == CellType.STRING) {
			try {
				return Double.parseDouble(cell.getStringCellValue().trim());
			} catch (NumberFormatException e) {
				dto.setSaveStatus("Failed");
				dto.setErrDescription("Please enter numeric values");
			}
		}
		return null;
	}

	@Override
	public AOPMessageVM updateDecokingActivitiesRunLengthData(String year, String plantId, String reportType,
			List<DecokeRunLengthDTO> decokeRunLengthDTOList) {
		List<DecokeRunLengthDTO> failedList = new ArrayList<>();
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			for (DecokeRunLengthDTO decokeRunLengthDTO : decokeRunLengthDTOList) {
				if (decokeRunLengthDTO.getSaveStatus() != null
						&& decokeRunLengthDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(decokeRunLengthDTO);
					continue;
				}

				Optional<DecokeRunLength> decokeRunLengthopt = decokeRunLengthRepository
						.findById(UUID.fromString(decokeRunLengthDTO.getId()));
				if (decokeRunLengthopt.isPresent()) {
					DecokeRunLength decokeRunLength = decokeRunLengthopt.get();
					decokeRunLength.setH10Proposed(decokeRunLengthDTO.getTenProposed());
					decokeRunLength.setH11Proposed(decokeRunLengthDTO.getElevenProposed());
					decokeRunLength.setH12Proposed(decokeRunLengthDTO.getTwelveProposed());
					decokeRunLength.setH13Proposed(decokeRunLengthDTO.getThirteenProposed());
					decokeRunLength.setH14Proposed(decokeRunLengthDTO.getFourteenProposed());
					decokeRunLength.setDemo(decokeRunLengthDTO.getDemo());
					decokeRunLengthRepository.save(decokeRunLength);
				} else {
					decokeRunLengthDTO.setSaveStatus("Failed");
					decokeRunLengthDTO.setErrDescription("Norm Paramter not found");
					failedList.add(decokeRunLengthDTO);
					continue;

				}
			}
		} catch (Exception ex) {
			throw new RuntimeException("Failed to update data");
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Data Updated successfully");
		aopMessageVM.setData(failedList);
		return aopMessageVM;
	}

	@Override
	public AOPMessageVM calculateDecokingActivities(String plantId, String aopYear) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

			String storedProcedure = vertical.getName() + "_" + site.getName() + "_DecokingPlanning";

			String callSql = "{call " + storedProcedure + "(?, ?)}";

			try (Connection connection = dataSource.getConnection();
					CallableStatement stmt = connection.prepareCall(callSql)) {

				// Set parameters in the correct order
				stmt.setString(1, plantId); // @finYear
				stmt.setString(2, aopYear); // @plantId

				// Execute the stored procedure
				int rowsAffected = stmt.executeUpdate();

				// Optional: commit if auto-commit is off
				if (!connection.getAutoCommit()) {
					connection.commit();
				}

				aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId),
						aopYear, "Furnace-run-length");

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
