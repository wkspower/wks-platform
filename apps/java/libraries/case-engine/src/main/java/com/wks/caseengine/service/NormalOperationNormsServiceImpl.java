package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Collections;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import com.wks.caseengine.dto.MCUNormsValueDTO;

import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.MCUNormsValue;

import com.wks.caseengine.entity.NormsTransactions;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.NormalOperationNormsRepository;
import com.wks.caseengine.repository.NormsTransactionRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.Connection;
import javax.sql.DataSource;

@Service
public class NormalOperationNormsServiceImpl implements NormalOperationNormsService {

	@Autowired
	private NormalOperationNormsRepository normalOperationNormsRepository;

	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	PlantsRepository plantsRepository;
	@Autowired
	SiteRepository siteRepository;
	@Autowired
	VerticalsRepository verticalRepository;
	@Autowired
	private NormsTransactionRepository normsTransactionRepository;

	@Autowired
	private ScreenMappingRepository screenMappingRepository;

	@Autowired
	private AopCalculationRepository aopCalculationRepository;

	@Autowired
	private NormParametersRepository normParametersRepository;

	private DataSource dataSource;

	// Inject or set your DataSource (e.g., via constructor or setter)
	public NormalOperationNormsServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public AOPMessageVM getNormalOperationNormsData(String year, String plantId, String gradeId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			List<Object[]> obj = getNormalOperationNormsDataFromView(year, UUID.fromString(plantId), gradeId);
			List<MCUNormsValueDTO> mCUNormsValueDTOList = new ArrayList<>();

			for (Object[] row : obj) {
				MCUNormsValueDTO mCUNormsValueDTO = new MCUNormsValueDTO();
				mCUNormsValueDTO.setId(row[0].toString());
				mCUNormsValueDTO.setSiteFkId(row[1].toString());
				mCUNormsValueDTO.setPlantFkId(row[2].toString());
				mCUNormsValueDTO.setVerticalFkId(row[3].toString());
				Verticals vertical = verticalRepository.findById(UUID.fromString(row[3].toString())).get();
				if (vertical.getName().equalsIgnoreCase("PE")) {
					mCUNormsValueDTO.setGradeId(row[4].toString());
					mCUNormsValueDTO.setMaterialFkId(row[5].toString());
					mCUNormsValueDTO.setApril(row[6] != null ? Double.parseDouble(row[6].toString()) : null);
					mCUNormsValueDTO.setMay(row[7] != null ? Double.parseDouble(row[7].toString()) : null);
					mCUNormsValueDTO.setJune(row[8] != null ? Double.parseDouble(row[8].toString()) : null);
					mCUNormsValueDTO.setJuly(row[9] != null ? Double.parseDouble(row[9].toString()) : null);
					mCUNormsValueDTO.setAugust(row[10] != null ? Double.parseDouble(row[10].toString()) : null);
					mCUNormsValueDTO.setSeptember(row[11] != null ? Double.parseDouble(row[11].toString()) : null);
					mCUNormsValueDTO.setOctober(row[12] != null ? Double.parseDouble(row[12].toString()) : null);
					mCUNormsValueDTO.setNovember(row[13] != null ? Double.parseDouble(row[13].toString()) : null);
					mCUNormsValueDTO.setDecember(row[14] != null ? Double.parseDouble(row[14].toString()) : null);
					mCUNormsValueDTO.setJanuary(row[15] != null ? Double.parseDouble(row[15].toString()) : null);
					mCUNormsValueDTO.setFebruary(row[16] != null ? Double.parseDouble(row[16].toString()) : null);
					mCUNormsValueDTO.setMarch(row[17] != null ? Double.parseDouble(row[17].toString()) : null);

					mCUNormsValueDTO.setFinancialYear(row[18].toString());
					mCUNormsValueDTO.setRemarks(row[19] != null ? row[19].toString() : " ");
					mCUNormsValueDTO.setCreatedOn(row[20] != null ? (Date) row[20] : null);
					mCUNormsValueDTO.setModifiedOn(row[21] != null ? (Date) row[21] : null);
					mCUNormsValueDTO.setMcuVersion(row[22] != null ? row[22].toString() : null);
					mCUNormsValueDTO.setUpdatedBy(row[23] != null ? row[23].toString() : null);
					mCUNormsValueDTO.setNormParameterTypeId(row[24] != null ? row[24].toString() : null);
					mCUNormsValueDTO.setNormParameterTypeName(row[25] != null ? row[25].toString() : null);
					mCUNormsValueDTO.setNormParameterTypeDisplayName(row[26] != null ? row[26].toString() : null);
					mCUNormsValueDTO.setUOM(row[27] != null ? row[27].toString() : null);
					mCUNormsValueDTO.setIsEditable(row[28] != null ? Boolean.valueOf(row[28].toString()) : null);
					mCUNormsValueDTO.setProductName(row[29] != null ? row[29].toString() : null);
				} else {
					mCUNormsValueDTO.setMaterialFkId(row[4].toString());

					mCUNormsValueDTO.setApril(row[5] != null ? Double.parseDouble(row[5].toString()) : null);
					mCUNormsValueDTO.setMay(row[6] != null ? Double.parseDouble(row[6].toString()) : null);
					mCUNormsValueDTO.setJune(row[7] != null ? Double.parseDouble(row[7].toString()) : null);
					mCUNormsValueDTO.setJuly(row[8] != null ? Double.parseDouble(row[8].toString()) : null);
					mCUNormsValueDTO.setAugust(row[9] != null ? Double.parseDouble(row[9].toString()) : null);
					mCUNormsValueDTO.setSeptember(row[10] != null ? Double.parseDouble(row[10].toString()) : null);
					mCUNormsValueDTO.setOctober(row[11] != null ? Double.parseDouble(row[11].toString()) : null);
					mCUNormsValueDTO.setNovember(row[12] != null ? Double.parseDouble(row[12].toString()) : null);
					mCUNormsValueDTO.setDecember(row[13] != null ? Double.parseDouble(row[13].toString()) : null);
					mCUNormsValueDTO.setJanuary(row[14] != null ? Double.parseDouble(row[14].toString()) : null);
					mCUNormsValueDTO.setFebruary(row[15] != null ? Double.parseDouble(row[15].toString()) : null);
					mCUNormsValueDTO.setMarch(row[16] != null ? Double.parseDouble(row[16].toString()) : null);

					mCUNormsValueDTO.setFinancialYear(row[17].toString());
					mCUNormsValueDTO.setRemarks(row[18] != null ? row[18].toString() : " ");
					mCUNormsValueDTO.setCreatedOn(row[19] != null ? (Date) row[19] : null);
					mCUNormsValueDTO.setModifiedOn(row[20] != null ? (Date) row[20] : null);
					mCUNormsValueDTO.setMcuVersion(row[21] != null ? row[21].toString() : null);
					mCUNormsValueDTO.setUpdatedBy(row[22] != null ? row[22].toString() : null);
					mCUNormsValueDTO.setNormParameterTypeId(row[23] != null ? row[23].toString() : null);
					mCUNormsValueDTO.setNormParameterTypeName(row[24] != null ? row[24].toString() : null);
					mCUNormsValueDTO.setNormParameterTypeDisplayName(row[25] != null ? row[25].toString() : null);
					mCUNormsValueDTO.setUOM(row[26] != null ? row[26].toString() : null);
					mCUNormsValueDTO.setIsEditable(row[27] != null ? Boolean.valueOf(row[27].toString()) : null);
					mCUNormsValueDTO.setProductName(row[28] != null ? row[28].toString() : null);
				}
				mCUNormsValueDTOList.add(mCUNormsValueDTO);
			}
			Map<String, Object> map = new HashMap<>();

			List<AopCalculation> aopCalculation = aopCalculationRepository
					.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "normal-op-norms");
			map.put("mcuNormsValueDTOList", mCUNormsValueDTOList);
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
	public List<MCUNormsValueDTO> saveNormalOperationNormsData(List<MCUNormsValueDTO> mCUNormsValueDTOList,
			UUID plantFKId, String year) {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String userId = authentication.getName();
			List<NormsTransactions> transactionsToSave = new ArrayList<>();

			for (MCUNormsValueDTO dto : mCUNormsValueDTOList) {
				Optional<MCUNormsValue> optionalValue = normalOperationNormsRepository
						.findById(UUID.fromString(dto.getId()));

				if (optionalValue.isEmpty()) {
					dto.setErrDescription("No record found with this id" +dto.getId());
					dto.setSaveStatus("Failed");
					continue; // or handle accordingly
				}

				MCUNormsValue value = optionalValue.get();

				for (int month = 1; month <= 12; month++) {
					Double oldVal = getMonthlyValue(value, month);
					Double newVal = getMonthlyValue(dto, month);

					if (newVal != null && !Objects.equals(oldVal, newVal)) {
						NormsTransactions normsTransactions = new NormsTransactions();
						normsTransactions.setAopMonth(month);
						normsTransactions.setAopYear(value.getFinancialYear());
						normsTransactions.setAttributeValue(newVal != null ? newVal.doubleValue() : null);
						normsTransactions.setNormParameterFkId(value.getMaterialFkId());
						normsTransactions.setPlantFkId(plantFKId);
						normsTransactions.setRemark(dto.getRemarks());
						normsTransactions.setVersion(1);
						normsTransactions.setCreatedDateTime(new Date());

						normsTransactions.setCreatedBy(userId);
						normsTransactions.setMcuNormsValueFkId((UUID.fromString(dto.getId())));

						transactionsToSave.add(normsTransactions);
					}
				}
			}

			normsTransactionRepository.saveAll(transactionsToSave);

			for (MCUNormsValueDTO mCUNormsValueDTO : mCUNormsValueDTOList) {
				year = mCUNormsValueDTO.getFinancialYear();
				System.out.println("MCUNormsValueDTO " + mCUNormsValueDTO);
				MCUNormsValue mCUNormsValue = new MCUNormsValue();
				System.out.println("mCUNormsValueDTO.getId() " + mCUNormsValueDTO.getId());
				if (mCUNormsValueDTO.getId() != null || !mCUNormsValueDTO.getId().isEmpty()) {
					mCUNormsValue = normalOperationNormsRepository.findById(UUID.fromString(mCUNormsValueDTO.getId()))
							.get();
					mCUNormsValue.setId(UUID.fromString(mCUNormsValueDTO.getId()));
					mCUNormsValue.setModifiedOn(new Date());
				} else {
					mCUNormsValue.setCreatedOn(new Date());
				}
				mCUNormsValue.setApril(Optional.ofNullable(mCUNormsValueDTO.getApril()).orElse(0.0));
				mCUNormsValue.setMay(Optional.ofNullable(mCUNormsValueDTO.getMay()).orElse(0.0));
				mCUNormsValue.setJune(Optional.ofNullable(mCUNormsValueDTO.getJune()).orElse(0.0));
				mCUNormsValue.setJuly(Optional.ofNullable(mCUNormsValueDTO.getJuly()).orElse(0.0));
				mCUNormsValue.setAugust(Optional.ofNullable(mCUNormsValueDTO.getAugust()).orElse(0.0));
				mCUNormsValue.setSeptember(Optional.ofNullable(mCUNormsValueDTO.getSeptember()).orElse(0.0));
				mCUNormsValue.setOctober(Optional.ofNullable(mCUNormsValueDTO.getOctober()).orElse(0.0));
				mCUNormsValue.setNovember(Optional.ofNullable(mCUNormsValueDTO.getNovember()).orElse(0.0));
				mCUNormsValue.setDecember(Optional.ofNullable(mCUNormsValueDTO.getDecember()).orElse(0.0));
				mCUNormsValue.setJanuary(Optional.ofNullable(mCUNormsValueDTO.getJanuary()).orElse(0.0));
				mCUNormsValue.setFebruary(Optional.ofNullable(mCUNormsValueDTO.getFebruary()).orElse(0.0));
				mCUNormsValue.setMarch(Optional.ofNullable(mCUNormsValueDTO.getMarch()).orElse(0.0));
				if (mCUNormsValueDTO.getSiteFkId() != null) {
					mCUNormsValue.setSiteFkId(UUID.fromString(mCUNormsValueDTO.getSiteFkId()));
				}
				if (plantFKId != null) {
					mCUNormsValue.setPlantFkId(plantFKId);
				}
				if (mCUNormsValueDTO.getVerticalFkId() != null) {
					mCUNormsValue.setVerticalFkId(UUID.fromString(mCUNormsValueDTO.getVerticalFkId()));
				}
				if (mCUNormsValueDTO.getMaterialFkId() != null) {
					mCUNormsValue.setMaterialFkId(UUID.fromString(mCUNormsValueDTO.getMaterialFkId()));
				}
				if (mCUNormsValueDTO.getNormParameterTypeId() != null) {
					mCUNormsValue.setNormParameterTypeFkId(UUID.fromString(mCUNormsValueDTO.getNormParameterTypeId()));
				}

				mCUNormsValue.setFinancialYear(mCUNormsValueDTO.getFinancialYear());
				mCUNormsValue.setRemarks(mCUNormsValueDTO.getRemarks());
				mCUNormsValue.setMcuVersion("V1");
				mCUNormsValue.setUpdatedBy(userId);

				System.out.println("Data Saved Succussfully" + mCUNormsValue);
				normalOperationNormsRepository.save(mCUNormsValue);
			}
			List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("normal-op-norms");
			for (ScreenMapping screenMapping : screenMappingList) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(plantFKId);
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			// TODO Auto-generated method stub
			return mCUNormsValueDTOList;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	@Override
	@Transactional
	public AOPMessageVM calculateExpressionConsumptionNorms(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		String storedProcedure = vertical.getName() + "_" + site.getName() + "_NormsCalculation";
		System.out.println("storedProcedure" + storedProcedure);
		int result = executeDynamicUpdateProcedure(storedProcedure, plantId, site.getId().toString(),
				vertical.getId().toString(), year);
		aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year,
				"normal-op-norms");
		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("normal-op-norms");
		for (ScreenMapping screenMapping : screenMappingList) {
			if (!screenMapping.getCalculationScreen().equalsIgnoreCase(screenMapping.getDependentScreen())) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("SP Executed successfully");
		aopMessageVM.setData(result);
		return aopMessageVM;
	}

	// @Transactional
	// public int executeDynamicUpdateProcedure(String procedureName, String
	// plantId, String siteId, String verticalId,
	// String finYear) {
	// try {
	// String sql = "EXEC " + procedureName
	// + " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId,
	// @finYear = :finYear";

	// Query query = entityManager.createNativeQuery(sql);

	// // Setting all parameters
	// query.setParameter("plantId", plantId);
	// query.setParameter("siteId", siteId);
	// query.setParameter("verticalId", verticalId);
	// query.setParameter("finYear", finYear);

	// int rowsUpdated = query.executeUpdate();

	// entityManager.flush(); // <-- force JPA to execute SQL immediately

	// return rowsUpdated;

	// } catch (Exception e) {
	// e.printStackTrace();
	// return 0;
	// }
	// }

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

			// Optional: commit if auto-commit is off
			if (!connection.getAutoCommit()) {
				connection.commit();
			}

			return rowsAffected;

		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Transactional
	public List<Object[]> getNormalOperationNormsDataFromView(String financialYear, UUID plantId, String gradeId) {
		try {
			Plants plant = plantsRepository.findById(plantId).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

			String viewName = "vwScrn" + vertical.getName() + "NormalOperationNorms";
			// Validate or sanitize viewName before using it directly in the query to
			// prevent SQL injection
			String sql = null;
			if (vertical.getName().equalsIgnoreCase("PE")) {
				sql = "SELECT * FROM " + viewName
						+ " WHERE FinancialYear = :financialYear AND Plant_FK_Id = :plantId AND Grade_FK_Id = :gradeId";
			} else {
				sql = "SELECT * FROM " + viewName + " WHERE FinancialYear = :financialYear AND Plant_FK_Id = :plantId";
			}

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("financialYear", financialYear);
			query.setParameter("plantId", plantId);
			if (vertical.getName().equalsIgnoreCase("PE")) {
				query.setParameter("gradeId", gradeId);
			}

			return query.getResultList(); // You can cast this to a DTO later
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getNormsTransaction(String plantId, String aopYear) {
		try {
			UUID plantUUID = UUID.fromString(plantId);

			List<Object[]> transactions = normsTransactionRepository
					.findDistinctTransactionsByMonthAndParameter(plantUUID, aopYear);

			List<Map<String, Object>> normsTransactions = transactions.stream()
					.map(tx -> {
						Map<String, Object> cell = new HashMap<>();
						cell.put("month", tx[0]); // AOPMonth
						cell.put("normParameterFKId", tx[1].toString()); // NormParameter_FK_Id
						cell.put("value", tx[2]); // AttributeValue
						return cell;
					})
					.collect(Collectors.toList());

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Norms Transactions retrieved successfully.");
			aopMessageVM.setData(normsTransactions);

			return aopMessageVM;

		} catch (Exception ex) {
			throw new RestInvalidArgumentException("normsTransaction", ex);
		}
	}

	private Double getMonthlyValue(Object obj, int month) {
		try {
			String methodName = switch (month) {
			case 1 -> "getJanuary";
			case 2 -> "getFebruary";
			case 3 -> "getMarch";
			case 4 -> "getApril";
			case 5 -> "getMay";
			case 6 -> "getJune";
			case 7 -> "getJuly";
			case 8 -> "getAugust";
			case 9 -> "getSeptember";
			case 10 -> "getOctober";
			case 11 -> "getNovember";
			case 12 -> "getDecember";
			default -> throw new IllegalArgumentException("Invalid month: " + month);
			};
			Method method = obj.getClass().getMethod(methodName);
			return (Double) method.invoke(obj);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public byte[] importExcel(String year, UUID plantFKId, MultipartFile file) {
		// TODO Auto-generated method stub
		try {
			List<MCUNormsValueDTO> data = readConfigurations(file.getInputStream(), plantFKId, year);
			List<MCUNormsValueDTO> savedData = saveNormalOperationNormsData(data, plantFKId, year);
			return createExcel(year, plantFKId, true, savedData);
			// return ResponseEntity.ok(data);
		} catch (Exception e) {
			e.printStackTrace();
			// return ResponseEntity.internalServerError().build();
		}
		return null;
	}

	public List<MCUNormsValueDTO> readConfigurations(InputStream inputStream, UUID plantFKId, String year) {
		List<MCUNormsValueDTO> configList = new ArrayList<>();

		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();

			if (rowIterator.hasNext())
				rowIterator.next(); // Skip header

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				MCUNormsValueDTO dto = new MCUNormsValueDTO();
				try {
					dto.setNormParameterTypeDisplayName(getStringCellValue(row.getCell(0), dto));
					dto.setProductName(getStringCellValue(row.getCell(1), dto));
					dto.setUOM(getStringCellValue(row.getCell(2), dto));

					dto.setFinancialYear(year);
					dto.setApril(getNumericCellValue(row.getCell(3), dto));
					dto.setMay(getNumericCellValue(row.getCell(4), dto));
					dto.setJune(getNumericCellValue(row.getCell(5), dto));
					dto.setJuly(getNumericCellValue(row.getCell(6), dto));
					dto.setAugust(getNumericCellValue(row.getCell(7), dto));
					dto.setSeptember(getNumericCellValue(row.getCell(8), dto));
					dto.setOctober(getNumericCellValue(row.getCell(9), dto));
					dto.setNovember(getNumericCellValue(row.getCell(10), dto));
					dto.setDecember(getNumericCellValue(row.getCell(11), dto));
					dto.setJanuary(getNumericCellValue(row.getCell(12), dto));
					dto.setFebruary(getNumericCellValue(row.getCell(13), dto));
					dto.setMarch(getNumericCellValue(row.getCell(14), dto));
					dto.setRemarks(getStringCellValue(row.getCell(15), dto));
					dto.setId(getStringCellValue(row.getCell(16), dto));
					dto.setMaterialFkId(getStringCellValue(row.getCell(17), dto));
					dto.setIsEditable(getBooleanCellValue(row.getCell(18), dto));
				} catch (Exception e) {
					e.printStackTrace();
					dto.setErrDescription(e.getMessage());
					dto.setSaveStatus("Failed");
				}
				configList.add(dto);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return configList;
	}

	private static String getStringCellValue(Cell cell, MCUNormsValueDTO dto) {
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

	private static Double getNumericCellValue(Cell cell, MCUNormsValueDTO dto) {
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
	
	public static Boolean getBooleanCellValue(Cell cell, MCUNormsValueDTO dto) {
	    if (cell == null) return null;

	    CellType type = cell.getCellType();
	    if (type == CellType.FORMULA) {
	        type = cell.getCachedFormulaResultType();
	    }

	    switch (type) {
	        case BOOLEAN:
	            return cell.getBooleanCellValue();
	        case STRING:
	            String text = cell.getStringCellValue().trim().toLowerCase();
	            if ("true".equals(text)) return true;
	            if ("false".equals(text)) return false;
	            return null;
	        case NUMERIC:
	            double num = cell.getNumericCellValue();
	            if (num == 1.0) return true;
	            if (num == 0.0) return false;
	            return null;
	        case BLANK:
	        case _NONE:
	        default:
	            return null;
	    }
	}

	public byte[] createExcel(String year, UUID plantFKId, boolean isAfterSave, List<MCUNormsValueDTO> dtoList) {
		try {
			AOPMessageVM aopMessageVM = getNormalOperationNormsData(year, plantFKId.toString(), "");

			if (!isAfterSave) {
				Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
				dtoList = (List<MCUNormsValueDTO>) responseMap.get("mcuNormsValueDTOList");
			}

			Workbook workbook = new XSSFWorkbook();

			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;
			// List<List<Object>> rows = new ArrayList<>();

			List<List<Object>> rows = new ArrayList<>();
			// Data rows
			for (MCUNormsValueDTO dto : dtoList) {
				if (dto.getIsEditable() != null && dto.getIsEditable()) {
					List<Object> list = new ArrayList<>();
					list.add(dto.getNormParameterTypeDisplayName());
					list.add(dto.getProductName());
					list.add(dto.getUOM());
					list.add(dto.getApril());
					list.add(dto.getMay());
					list.add(dto.getJune());
					list.add(dto.getJuly());
					list.add(dto.getAugust());
					list.add(dto.getSeptember());
					list.add(dto.getOctober());
					list.add(dto.getNovember());
					list.add(dto.getDecember());
					list.add(dto.getJanuary());
					list.add(dto.getFebruary());
					list.add(dto.getMarch());
					list.add(dto.getRemarks());
					list.add(dto.getId());
					list.add(dto.getMaterialFkId());
					list.add(dto.getIsEditable());
					if (isAfterSave) {
						list.add(dto.getSaveStatus());
						list.add(dto.getErrDescription());
					}
					rows.add(list);
				}
			}

			List<String> innerHeaders = new ArrayList<>();
			innerHeaders.add("Type");
			innerHeaders.add("Particulars");
			innerHeaders.add("UOM");
			List<String> monthsList = getAcademicYearMonths(year);
			innerHeaders.addAll(monthsList);
			innerHeaders.add("Remarks");
			innerHeaders.add("Id");
			innerHeaders.add("NormParamterId");
			innerHeaders.add("IsEditable");
			if (isAfterSave) {
				innerHeaders.add("Status");
				innerHeaders.add("Error Description");
			}
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

				}
			}
			sheet.setColumnHidden(16, true);
			sheet.setColumnHidden(17, true);
			sheet.setColumnHidden(18, true);
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
	public AOPMessageVM calculateNormalOpsNorms(String aopYear, String plantId, String siteId, String verticalId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		String storedProcedure = vertical.getName() + "_" + site.getName() + "_GetNormsValue";
		String callSql = "{call " + storedProcedure + "(?, ?, ?, ?)}";

		try (Connection connection = dataSource.getConnection();
				CallableStatement stmt = connection.prepareCall(callSql)) {

			// Set parameters
			stmt.setString(1, plantId);
			stmt.setString(2, siteId);
			stmt.setString(3, verticalId);
			stmt.setString(4, aopYear);

			// Execute the stored procedure
			stmt.executeUpdate();

			// Optional: commit if auto-commit is off
			if (!connection.getAutoCommit()) {
				connection.commit();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), aopYear,
				"normal-op-norms");

		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("normal-op-norms");
		for (ScreenMapping screenMapping : screenMappingList) {
			if (!screenMapping.getCalculationScreen().equalsIgnoreCase(screenMapping.getDependentScreen())) {

				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(aopYear);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("SP Executed successfully");
		// aopMessageVM.setData(rowsAffected);
		return aopMessageVM;

	}

	@Override
	public AOPMessageVM getNormalOperationNormsGrades(String financialYear, String plantId) {
		List<Map<String, Object>> gradeList = new ArrayList<>();
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

			String viewName = "vwScrn" + vertical.getName() + "Grade";
			// Validate or sanitize viewName before using it directly in the query to
			// prevent SQL injection
			String sql = "SELECT * FROM " + viewName
					+ " WHERE FinancialYear = :financialYear AND Plant_FK_Id = :plantId";

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
				map.put("financialYear", result[4].toString());
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
