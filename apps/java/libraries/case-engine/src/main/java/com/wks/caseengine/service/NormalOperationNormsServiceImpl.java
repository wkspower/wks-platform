package com.wks.caseengine.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.AOPDTO;
import com.wks.caseengine.dto.MCUNormsValueDTO;
import com.wks.caseengine.dto.ModeWiseNormsDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.MCUNormsValue;
import com.wks.caseengine.entity.MCUNormsValueGrade;
import com.wks.caseengine.entity.NormParameterType;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.NormsTransactions;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AOPRepository;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.MCUNormsValueGradeRepository;
import com.wks.caseengine.repository.MCUNormsValueRepository;
import com.wks.caseengine.repository.NormParameterTypeRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.NormalOperationNormsRepository;
import com.wks.caseengine.repository.NormsTransactionRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

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
	private MCUNormsValueGradeRepository mcuNormsValueGradeRepository;

	private DataSource dataSource;

	@Autowired
	private NormParametersRepository normParametersRepository;

	@Autowired
	private FinalNormsService finalNormsService;

	@Autowired
	private NormParameterTypeRepository normParameterTypeRepository;

	@Autowired
	private MCUNormsValueRepository mcuNormsValueRepository;

	@Autowired
	private AOPRepository aopRepository;

	// Inject or set your DataSource (e.g., via constructor or setter)
	public NormalOperationNormsServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public AOPMessageVM getNormalOperationNormsData(String year, String plantId, String gradeId, String mode) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Boolean withGrade = false;
		if (plant.getName().equalsIgnoreCase("SBR") && site.getName().equalsIgnoreCase("HMD")
				&& vertical.getName().equalsIgnoreCase("ELASTOMER")) {
			withGrade = true;
		}
		try {
			List<Object[]> obj = null;
			if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("PTA")) {
				String procedureName = vertical.getName() + "_" + site.getName() + "_" + "GetNormalOperationNorms";
				obj = findByYearAndPlantId(year, UUID.fromString(plantId), procedureName);
			} else {
				obj = getNormalOperationNormsDataFromView(year, UUID.fromString(plantId), gradeId, mode);
			}

			List<MCUNormsValueDTO> mCUNormsValueDTOList = new ArrayList<>();

			for (Object[] row : obj) {
				MCUNormsValueDTO mCUNormsValueDTO = new MCUNormsValueDTO();
				mCUNormsValueDTO.setId(row[0].toString());
				mCUNormsValueDTO.setSiteFkId(row[1].toString());
				mCUNormsValueDTO.setPlantFkId(row[2].toString());
				mCUNormsValueDTO.setVerticalFkId(row[3].toString());

				if (vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")
						|| vertical.getName().equalsIgnoreCase("PET") || withGrade) {
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
					if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("PTA")) {
						mCUNormsValueDTO.setWtAverage(row[29] != null ? Double.parseDouble(row[29].toString()) : null);
					}
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

	public List<Object[]> findByYearAndPlantId(String aopYear, UUID plantId, String procedureName) {
		try {

			String sql = "EXEC " + procedureName + " @PlantId = :plantId, @FinYear = :aopYear";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public List<MCUNormsValueDTO> saveNormalOperationNormsData(List<MCUNormsValueDTO> mCUNormsValueDTOList,
			UUID plantFKId, String year, String gradeId, boolean isFromExcel) {

		try {

			List<NormsTransactions> transactionsToSave = new ArrayList<>();
			List<MCUNormsValueDTO> failedList = new ArrayList<>();
			Plants plant = plantsRepository.findById(plantFKId).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			for (MCUNormsValueDTO dto : mCUNormsValueDTOList) {
				System.out.println(dto.getProductName());
				Boolean changed = false;
				if (dto.getSaveStatus() != null && dto.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(dto);
					continue;
				}
				if (gradeId != null) {
					Optional<MCUNormsValueGrade> optionalValue = mcuNormsValueGradeRepository
							.findById(UUID.fromString(dto.getId()));

					if (optionalValue.isEmpty()) {
						dto.setErrDescription("No record found with this id" + dto.getId());
						dto.setSaveStatus("Failed");
						failedList.add(dto);
						continue; // or handle accordingly
					}

					MCUNormsValueGrade value = optionalValue.get();
					Optional<NormParameters> normParametersOpt = normParametersRepository
							.findById(value.getMaterialFkId());
					if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
						continue;
					}

					for (int month = 1; month <= 12; month++) {
						Double oldVal = getMonthlyValue(value, month);
						Double newVal = getMonthlyValue(dto, month);

						if (newVal != null && !Objects.equals(oldVal, newVal)
								&& Objects.equals(value.getRemarks(), dto.getRemarks())) {

							dto.setErrDescription("Please add/update remark");
							dto.setSaveStatus("Failed");
							failedList.add(dto);
							break;
						}
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
							normsTransactions.setCreatedBy(Utility.getUserName());
							normsTransactions.setMcuNormsValueFkId((UUID.fromString(dto.getId())));
							transactionsToSave.add(normsTransactions);
						}
					}

				} else {
					Optional<MCUNormsValue> optionalValue = normalOperationNormsRepository
							.findById(UUID.fromString(dto.getId()));

					if (optionalValue.isEmpty()) {
						dto.setErrDescription("No record found with this id" + dto.getId());
						dto.setSaveStatus("Failed");
						failedList.add(dto);
						continue; // or handle accordingly
					}

					MCUNormsValue value = optionalValue.get();
					Optional<NormParameters> normParametersOpt = normParametersRepository
							.findById(value.getMaterialFkId());
					if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
						continue;
					}

					for (int month = 1; month <= 12; month++) {
						Double oldVal = getMonthlyValue(value, month);
						Double newVal = getMonthlyValue(dto, month);

						Double normalizedNewVal = Optional.ofNullable(newVal).orElse(0.0);
						// if (!dto.getProductName().equalsIgnoreCase("Total Fuel")) {
						// if (newVal != null && !Objects.equals(oldVal, normalizedNewVal)
						// && Objects.equals(value.getRemarks(), dto.getRemarks())) {
						// dto.setErrDescription("Please add/update remark");
						// dto.setSaveStatus("Failed");
						// failedList.add(dto);
						// break;
						// }
						// }

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

							normsTransactions.setCreatedBy(Utility.getUserName());
							normsTransactions.setMcuNormsValueFkId((UUID.fromString(dto.getId())));

							transactionsToSave.add(normsTransactions);
						}
					}

				}

			}

			normsTransactionRepository.saveAll(transactionsToSave);

			for (MCUNormsValueDTO mCUNormsValueDTO : mCUNormsValueDTOList) {
				if (mCUNormsValueDTO.getSaveStatus() != null
						&& mCUNormsValueDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					if (!failedList.contains(mCUNormsValueDTO))
						failedList.add(mCUNormsValueDTO);
					continue;
				}

				year = mCUNormsValueDTO.getFinancialYear();
				MCUNormsValue mCUNormsValue = new MCUNormsValue();
				MCUNormsValueGrade mCUNormsValueGrade = new MCUNormsValueGrade();

				if (mCUNormsValueDTO.getId() != null || !mCUNormsValueDTO.getId().isEmpty()) {
					if (vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")
							|| vertical.getName().equalsIgnoreCase("PET")) {

						Optional<MCUNormsValueGrade> optionalNormsValue = mcuNormsValueGradeRepository
								.findById(UUID.fromString(mCUNormsValueDTO.getId()));
						if (optionalNormsValue.isPresent()) {
							mCUNormsValueGrade = optionalNormsValue.get();
							if (mCUNormsValueGrade.getMaterialFkId() != null) {
								Optional<NormParameters> normParametersOpt = normParametersRepository
										.findById(mCUNormsValueGrade.getMaterialFkId());
								if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
									continue;
								}

							}

							mCUNormsValueGrade.setId(UUID.fromString(mCUNormsValueDTO.getId()));
							mCUNormsValueGrade.setModifiedOn(new Date());
							boolean changed = false;

							// January
							double newJan = Optional.ofNullable(mCUNormsValueDTO.getJanuary()).orElse(0.0);
							double oldJan = Optional.ofNullable(mCUNormsValueGrade.getJanuary()).orElse(0.0);
							if (isDifferent(oldJan, newJan)) {
								mCUNormsValueGrade.setJanuary(newJan);
								changed = true;
							}

							// February
							double newFeb = Optional.ofNullable(mCUNormsValueDTO.getFebruary()).orElse(0.0);
							double oldFeb = Optional.ofNullable(mCUNormsValueGrade.getFebruary()).orElse(0.0);
							if (isDifferent(oldFeb, newFeb)) {
								mCUNormsValueGrade.setFebruary(newFeb);
								changed = true;
							}

							// March
							double newMar = Optional.ofNullable(mCUNormsValueDTO.getMarch()).orElse(0.0);
							double oldMar = Optional.ofNullable(mCUNormsValueGrade.getMarch()).orElse(0.0);
							if (isDifferent(oldMar, newMar)) {
								mCUNormsValueGrade.setMarch(newMar);
								changed = true;
							}

							// April
							double newApr = Optional.ofNullable(mCUNormsValueDTO.getApril()).orElse(0.0);
							double oldApr = Optional.ofNullable(mCUNormsValueGrade.getApril()).orElse(0.0);
							if (isDifferent(oldApr, newApr)) {
								mCUNormsValueGrade.setApril(newApr);
								changed = true;
							}

							// May
							double newMay = Optional.ofNullable(mCUNormsValueDTO.getMay()).orElse(0.0);
							double oldMay = Optional.ofNullable(mCUNormsValueGrade.getMay()).orElse(0.0);
							if (isDifferent(oldMay, newMay)) {
								mCUNormsValueGrade.setMay(newMay);
								changed = true;
							}

							// June
							double newJun = Optional.ofNullable(mCUNormsValueDTO.getJune()).orElse(0.0);
							double oldJun = Optional.ofNullable(mCUNormsValueGrade.getJune()).orElse(0.0);
							if (isDifferent(oldJun, newJun)) {
								mCUNormsValueGrade.setJune(newJun);
								changed = true;
							}

							// July
							double newJul = Optional.ofNullable(mCUNormsValueDTO.getJuly()).orElse(0.0);
							double oldJul = Optional.ofNullable(mCUNormsValueGrade.getJuly()).orElse(0.0);
							if (isDifferent(oldJul, newJul)) {
								mCUNormsValueGrade.setJuly(newJul);
								changed = true;
							}

							// August
							double newAug = Optional.ofNullable(mCUNormsValueDTO.getAugust()).orElse(0.0);
							double oldAug = Optional.ofNullable(mCUNormsValueGrade.getAugust()).orElse(0.0);
							if (isDifferent(oldAug, newAug)) {
								mCUNormsValueGrade.setAugust(newAug);
								changed = true;
							}

							// September
							double newSep = Optional.ofNullable(mCUNormsValueDTO.getSeptember()).orElse(0.0);
							double oldSep = Optional.ofNullable(mCUNormsValueGrade.getSeptember()).orElse(0.0);
							if (isDifferent(oldSep, newSep)) {
								mCUNormsValueGrade.setSeptember(newSep);
								changed = true;
							}

							// October
							double newOct = Optional.ofNullable(mCUNormsValueDTO.getOctober()).orElse(0.0);
							double oldOct = Optional.ofNullable(mCUNormsValueGrade.getOctober()).orElse(0.0);
							if (isDifferent(oldOct, newOct)) {
								mCUNormsValueGrade.setOctober(newOct);
								changed = true;
							}

							// November
							double newNov = Optional.ofNullable(mCUNormsValueDTO.getNovember()).orElse(0.0);
							double oldNov = Optional.ofNullable(mCUNormsValueGrade.getNovember()).orElse(0.0);
							if (isDifferent(oldNov, newNov)) {
								mCUNormsValueGrade.setNovember(newNov);
								changed = true;
							}

							// December
							double newDec = Optional.ofNullable(mCUNormsValueDTO.getDecember()).orElse(0.0);
							double oldDec = Optional.ofNullable(mCUNormsValueGrade.getDecember()).orElse(0.0);
							if (isDifferent(oldDec, newDec)) {
								mCUNormsValueGrade.setDecember(newDec);
								changed = true;
							}

							if (!isFromExcel) {
								if (mCUNormsValueDTO.getSiteFkId() != null) {
									mCUNormsValueGrade.setSiteFkId(UUID.fromString(mCUNormsValueDTO.getSiteFkId()));
								}
								if (plantFKId != null) {
									mCUNormsValueGrade.setPlantFkId(plantFKId);
								}
								if (mCUNormsValueDTO.getVerticalFkId() != null) {
									mCUNormsValueGrade
											.setVerticalFkId(UUID.fromString(mCUNormsValueDTO.getVerticalFkId()));
								}
								if (mCUNormsValueDTO.getMaterialFkId() != null) {
									mCUNormsValueGrade
											.setMaterialFkId(UUID.fromString(mCUNormsValueDTO.getMaterialFkId()));
								}
								if (mCUNormsValueDTO.getNormParameterTypeId() != null) {
									mCUNormsValueGrade.setNormParameterTypeFkId(
											UUID.fromString(mCUNormsValueDTO.getNormParameterTypeId()));
								}
								mCUNormsValueGrade.setFinancialYear(mCUNormsValueDTO.getFinancialYear());
							}

							mCUNormsValueGrade.setMcuVersion("V1");
							mCUNormsValueGrade.setUpdatedBy(Utility.getUserName());
							mCUNormsValueGrade.setModifiedOn(new Date());
							mCUNormsValueGrade.setGradeFkId(UUID.fromString(mCUNormsValueDTO.getGradeId()));
							System.out.println("Data Saved Succussfully" + mCUNormsValue);
							if (changed
									&& Objects.equals(mCUNormsValueGrade.getRemarks(), mCUNormsValueDTO.getRemarks())) {
								mCUNormsValueDTO.setErrDescription("Please add/update remark");
								mCUNormsValueDTO.setSaveStatus("Failed");
								failedList.add(mCUNormsValueDTO);
								continue;
							}
							mCUNormsValueGrade.setRemarks(mCUNormsValueDTO.getRemarks());
							mcuNormsValueGradeRepository.save(mCUNormsValueGrade);

						} else {
							if (isFromExcel) {
								mCUNormsValueDTO.setSaveStatus("Failed");
								mCUNormsValueDTO.setErrDescription("Invalid Id. Record not found.");
								failedList.add(mCUNormsValueDTO);
								continue;
							}
						}

					} else {
						Optional<MCUNormsValue> normsValue = normalOperationNormsRepository
								.findById(UUID.fromString(mCUNormsValueDTO.getId()));
						if (normsValue.isPresent()) {
							mCUNormsValue = normsValue.get();
							if (mCUNormsValue.getMaterialFkId() != null) {
								Optional<NormParameters> normParametersOpt = normParametersRepository
										.findById(mCUNormsValue.getMaterialFkId());
								if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
									continue;
								}
							}

							mCUNormsValue.setId(UUID.fromString(mCUNormsValueDTO.getId()));
							mCUNormsValue.setModifiedOn(new Date());
							boolean changed = false;

							double newJan = Optional.ofNullable(mCUNormsValueDTO.getJanuary()).orElse(0.0);
							double oldJan = Optional.ofNullable(mCUNormsValue.getJanuary()).orElse(0.0);
							if (isDifferent(oldJan, newJan)) {
								mCUNormsValue.setJanuary(newJan);
								changed = true;
							}

							// February
							double newFeb = Optional.ofNullable(mCUNormsValueDTO.getFebruary()).orElse(0.0);
							double oldFeb = Optional.ofNullable(mCUNormsValue.getFebruary()).orElse(0.0);
							if (isDifferent(oldFeb, newFeb)) {
								mCUNormsValue.setFebruary(newFeb);
								changed = true;
							}

							// March
							double newMar = Optional.ofNullable(mCUNormsValueDTO.getMarch()).orElse(0.0);
							double oldMar = Optional.ofNullable(mCUNormsValue.getMarch()).orElse(0.0);
							if (isDifferent(oldMar, newMar)) {
								mCUNormsValue.setMarch(newMar);
								changed = true;
							}

							// April
							double newApr = Optional.ofNullable(mCUNormsValueDTO.getApril()).orElse(0.0);
							double oldApr = Optional.ofNullable(mCUNormsValue.getApril()).orElse(0.0);
							if (isDifferent(oldApr, newApr)) {
								mCUNormsValue.setApril(newApr);
								changed = true;
							}

							// May
							double newMay = Optional.ofNullable(mCUNormsValueDTO.getMay()).orElse(0.0);
							double oldMay = Optional.ofNullable(mCUNormsValue.getMay()).orElse(0.0);
							if (isDifferent(oldMay, newMay)) {
								mCUNormsValue.setMay(newMay);
								changed = true;
							}

							// June
							double newJun = Optional.ofNullable(mCUNormsValueDTO.getJune()).orElse(0.0);
							double oldJun = Optional.ofNullable(mCUNormsValue.getJune()).orElse(0.0);
							if (isDifferent(oldJun, newJun)) {
								mCUNormsValue.setJune(newJun);
								changed = true;
							}

							// July
							double newJul = Optional.ofNullable(mCUNormsValueDTO.getJuly()).orElse(0.0);
							double oldJul = Optional.ofNullable(mCUNormsValue.getJuly()).orElse(0.0);
							if (isDifferent(oldJul, newJul)) {
								mCUNormsValue.setJuly(newJul);
								changed = true;
							}

							// August
							double newAug = Optional.ofNullable(mCUNormsValueDTO.getAugust()).orElse(0.0);
							double oldAug = Optional.ofNullable(mCUNormsValue.getAugust()).orElse(0.0);
							if (isDifferent(oldAug, newAug)) {
								mCUNormsValue.setAugust(newAug);
								changed = true;
							}

							// September
							double newSep = Optional.ofNullable(mCUNormsValueDTO.getSeptember()).orElse(0.0);
							double oldSep = Optional.ofNullable(mCUNormsValue.getSeptember()).orElse(0.0);
							if (isDifferent(oldSep, newSep)) {
								mCUNormsValue.setSeptember(newSep);
								changed = true;
							}

							// October
							double newOct = Optional.ofNullable(mCUNormsValueDTO.getOctober()).orElse(0.0);
							double oldOct = Optional.ofNullable(mCUNormsValue.getOctober()).orElse(0.0);
							if (isDifferent(oldOct, newOct)) {
								mCUNormsValue.setOctober(newOct);
								changed = true;
							}

							// November
							double newNov = Optional.ofNullable(mCUNormsValueDTO.getNovember()).orElse(0.0);
							double oldNov = Optional.ofNullable(mCUNormsValue.getNovember()).orElse(0.0);
							if (isDifferent(oldNov, newNov)) {
								mCUNormsValue.setNovember(newNov);
								changed = true;
							}

							// December
							double newDec = Optional.ofNullable(mCUNormsValueDTO.getDecember()).orElse(0.0);
							double oldDec = Optional.ofNullable(mCUNormsValue.getDecember()).orElse(0.0);
							if (isDifferent(oldDec, newDec)) {
								mCUNormsValue.setDecember(newDec);
								changed = true;
							}

							if (isFromExcel) {
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
									mCUNormsValue.setNormParameterTypeFkId(
											UUID.fromString(mCUNormsValueDTO.getNormParameterTypeId()));
								}

								mCUNormsValue.setFinancialYear(mCUNormsValueDTO.getFinancialYear());
							}

							mCUNormsValue.setMcuVersion("V1");
							mCUNormsValue.setUpdatedBy(Utility.getUserName());
							// Use Objects.equals to safely compare two strings even if one or both are null
							if (changed && Objects.equals(mCUNormsValue.getRemarks(), mCUNormsValueDTO.getRemarks())) {
								mCUNormsValueDTO.setErrDescription("Please add/update remark");
								mCUNormsValueDTO.setSaveStatus("Failed");
								failedList.add(mCUNormsValueDTO);
								continue;
							}
							mCUNormsValue.setRemarks(mCUNormsValueDTO.getRemarks());
							System.out.println("Data Saved Succussfully" + mCUNormsValue);
							normalOperationNormsRepository.save(mCUNormsValue);
						} else {
							if (isFromExcel) {
								mCUNormsValueDTO.setSaveStatus("Failed");
								mCUNormsValueDTO.setErrDescription("Invalid Id. Record not found.");
								failedList.add(mCUNormsValueDTO);
								continue;
							}
						}
					}
				}
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
			if (vertical.getName().equalsIgnoreCase("VCM")) {
				Sites site = siteRepository.findById(plant.getSiteFkId()).get();

				String procedure = vertical.getName() + "_" + site.getName() + "_CalculateTotalFuel";
				executeProcedure(procedure, plantFKId.toString(), year);
			}
			// TODO Auto-generated method stub
			return failedList;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	// Helper method for precise comparison of primitive double values
	private boolean isDifferent(double oldVal, double newVal) {
		return Double.compare(oldVal, newVal) != 0;
	}

	@Override
	@Transactional
	public AOPMessageVM loadGradeWiseConsumptionNorms(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		String storedProcedure = vertical.getName() + "_" + site.getName() + "_LoadGradewiseConsumptionNorms";
		System.out.println("storedProcedure" + storedProcedure);
		int result = executeDynamicUpdateProcedure(storedProcedure, plantId, year);
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("SP Executed successfully");
		aopMessageVM.setData(result);
		return aopMessageVM;
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
		List<ScreenMapping> calculateScreenMappingList = screenMappingRepository
				.findByDependentScreen("normal-op-norms-calculate");
		for (ScreenMapping screenMapping : calculateScreenMappingList) {
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

	public int executeDynamicUpdateProcedure(String procedureName, String plantId, String year) {
		String callSql = "{call " + procedureName + "(?, ?)}";

		try (Connection connection = dataSource.getConnection();
				CallableStatement stmt = connection.prepareCall(callSql)) {

			// Set parameters
			stmt.setString(1, plantId);
			stmt.setString(2, year);

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
	public List<Object[]> getNormalOperationNormsDataFromView(String financialYear, UUID plantId, String gradeId,
			String mode) {
		try {
			Plants plant = plantsRepository.findById(plantId).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Boolean withGrade = false;
			if (plant.getName().equalsIgnoreCase("SBR") && site.getName().equalsIgnoreCase("HMD")
					&& vertical.getName().equalsIgnoreCase("ELASTOMER")) {
				withGrade = true;
			}
			String viewName = "vwScrn" + vertical.getName() + "NormalOperationNorms";
			if (withGrade) {
				viewName = "vwScrn" + vertical.getName() + "NormalOperationNormsGrade";
			}
			// Validate or sanitize viewName before using it directly in the query to
			// prevent SQL injection
			String sql = null;
			if (vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")
					|| vertical.getName().equalsIgnoreCase("PET") || withGrade) {
				sql = "SELECT * FROM " + viewName
						+ " WHERE FinancialYear = :financialYear AND Plant_FK_Id = :plantId AND Grade_FK_Id = :gradeId";
			} else if (vertical.getName().equalsIgnoreCase("Cracker")) {
				sql = "SELECT * FROM " + viewName
						+ " WHERE FinancialYear = :financialYear AND Plant_FK_Id = :plantId AND mode = :mode";
			} else {
				sql = "SELECT * FROM " + viewName + " WHERE FinancialYear = :financialYear AND Plant_FK_Id = :plantId";
			}

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("financialYear", financialYear);
			query.setParameter("plantId", plantId);
			if (vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")
					|| vertical.getName().equalsIgnoreCase("PET") || withGrade) {
				query.setParameter("gradeId", UUID.fromString(gradeId));
			}
			if (vertical.getName().equalsIgnoreCase("Cracker")) {
				query.setParameter("mode", mode);
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

			List<Map<String, Object>> normsTransactions = transactions.stream().map(tx -> {
				Map<String, Object> cell = new HashMap<>();
				cell.put("month", tx[0]);
				cell.put("normParameterFKId", tx[1].toString());
				cell.put("value", tx[2]);
				return cell;
			}).collect(Collectors.toList());

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
	public AOPMessageVM importExcel(String year, UUID plantFKId, String gradeId, MultipartFile file, String mode) {
		// TODO Auto-generated method stub
		try {
			Plants plant = plantsRepository.findById(plantFKId).get();
			List<MCUNormsValueDTO> data = null;
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			if (vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")
					|| vertical.getName().equalsIgnoreCase("PET")) {
				data = readSteadyState(file.getInputStream(), plantFKId, year);
			} else {
				data = readConfigurations(file.getInputStream(), plantFKId, year);
			}

			List<MCUNormsValueDTO> failedRecords = saveNormalOperationNormsData(data, plantFKId, year, gradeId, true);

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = null;
				if (vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")
						|| vertical.getName().equalsIgnoreCase("PET")) {
					fileByteArray = exportSteadyStateNorms(year, plantFKId, true, failedRecords, mode);
				} else {
					fileByteArray = createExcel(year, plantFKId, true, failedRecords, mode, gradeId);
				}
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
		} catch (Exception e) {
			e.printStackTrace();
			// return ResponseEntity.internalServerError().build();
		}
		return null;
	}

	private Map<String, String> getGradeNameIdMap(String year, UUID plantFKId) {
		AOPMessageVM gradesVM = getNormalOperationNormsGrades(year, plantFKId.toString());
		List<Map<String, String>> gradeInfoList = extractGradeInfo(gradesVM); // The method you modified earlier

		Map<String, String> nameIdMap = new HashMap<>();
		for (Map<String, String> info : gradeInfoList) {
			String sanitizedName = Utility.sanitizeSheetName(info.get("displayName"));
			nameIdMap.put(sanitizedName, info.get("gradeId"));
		}
		return nameIdMap;
	}

	public List<MCUNormsValueDTO> readSteadyState(InputStream inputStream, UUID plantFKId, String year) {
		List<MCUNormsValueDTO> configList = new ArrayList<>();
		Map<String, String> gradeMap = getGradeNameIdMap(year, plantFKId);
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {

			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				Sheet sheet = workbook.getSheetAt(i);
				if (sheet == null) {
					continue;
				}
				String sheetName = sheet.getSheetName();
				String gradeId = gradeMap.get(Utility.sanitizeSheetName(sheetName));

				Iterator<Row> rowIterator = sheet.iterator();
				if (rowIterator.hasNext()) {
					rowIterator.next();
				}
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					if (row.getPhysicalNumberOfCells() == 0) {
						continue;
					}

					MCUNormsValueDTO dto = new MCUNormsValueDTO();
					try {
						dto.setNormParameterTypeDisplayName(getStringCellValue(row.getCell(0), dto));
						dto.setProductName(getStringCellValue(row.getCell(1), dto));
						dto.setUOM(getStringCellValue(row.getCell(2), dto));

						dto.setFinancialYear(year);
						dto.setPlantFkId(plantFKId.toString());
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
						dto.setGradeId(gradeId);

					} catch (Exception e) {
						e.printStackTrace();
						dto.setErrDescription(e.getMessage());
						dto.setSaveStatus("Failed");
					}
					configList.add(dto);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return configList;
	}

	public List<MCUNormsValueDTO> readConfigurations(InputStream inputStream, UUID plantFKId, String year) {
		List<MCUNormsValueDTO> configList = new ArrayList<>();
		List<MCUNormsValueDTO> ambientEthane = new ArrayList<>();
		List<MCUNormsValueDTO> hydrogen = new ArrayList<>();
		List<MCUNormsValueDTO> naturalGas = new ArrayList<>();
		List<Object[]> obj = null;
		Plants plant = plantsRepository.findById(plantFKId).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();

			if (rowIterator.hasNext())
				rowIterator.next();

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				MCUNormsValueDTO dto = new MCUNormsValueDTO();

				try {
					dto.setNormParameterTypeDisplayName(getStringCellValue(row.getCell(0), dto));
					dto.setProductName(getStringCellValue(row.getCell(1), dto));

					// if (dto.getProductName().equalsIgnoreCase("Total Fuel")) {
					if ("Total Fuel".equalsIgnoreCase(dto.getProductName())) {
						// calculateTotalFuel(dto, hydrogen, ambientEthane,naturalGas, vertical,
						// plantFKId, year);
					} else {
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

					}
					dto.setUOM(getStringCellValue(row.getCell(2), dto));

					dto.setFinancialYear(year);

					if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("PTA")) {
						dto.setWtAverage(getNumericCellValue(row.getCell(15), dto));
						dto.setRemarks(getStringCellValue(row.getCell(16), dto));
						dto.setId(getStringCellValue(row.getCell(17), dto));
					} else {
						dto.setRemarks(getStringCellValue(row.getCell(15), dto));
						dto.setId(getStringCellValue(row.getCell(16), dto));
					}
					// if (dto.getProductName().equalsIgnoreCase("AMBIENT ETHANE")) {
					// ambientEthane.add(dto);
					// }
					// if (dto.getProductName().equalsIgnoreCase("Hydrogen")
					// && dto.getNormParameterTypeDisplayName().equalsIgnoreCase("Utility
					// Consumption")) {
					// hydrogen.add(dto);
					// }
					// if (dto.getProductName().equalsIgnoreCase("NATURAL GAS")) {
					// naturalGas.add(dto);
					// }

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

	private void calculateTotalFuel(MCUNormsValueDTO dto, List<MCUNormsValueDTO> hydrogen,
	        List<MCUNormsValueDTO> ambientEthane, List<MCUNormsValueDTO> naturalGas, Verticals vertical, UUID plantFKId, String year) {

	    if (dto == null || hydrogen == null || hydrogen.isEmpty() || 
	        ambientEthane == null || ambientEthane.isEmpty() || 
	        naturalGas == null || naturalGas.isEmpty() || vertical == null) {
	        return;
	    }

	    String verticalName = vertical.getName() != null ? vertical.getName() : "Unknown";
	    String procedureName = verticalName + "_GetConfiguration_Constant";
	    Map<String, Double> constants = getConstantsMap(year, plantFKId != null ? plantFKId.toString() : "", procedureName);

	    double h2Const = (constants != null && constants.get("H2") != null) ? constants.get("H2") : 0.0;
	    double ethConst = (constants != null && constants.get("Ethane") != null) ? constants.get("Ethane") : 0.0;

	    List<Object[]> obj = aopRepository.findByAOPYearAndPlantFkId(year, plantFKId, "Production");
	    List<AOPDTO> prodList = getMonthlyProduction(obj);

	    if (prodList == null || prodList.isEmpty() || prodList.get(0) == null) {
	        return;
	    }

	    AOPDTO prod = prodList.get(0);
	    MCUNormsValueDTO h2Data = hydrogen.get(0);
	    MCUNormsValueDTO ethData = ambientEthane.get(0);
	    MCUNormsValueDTO ngData = naturalGas.get(0);
	    dto.setApril(calculateFuelFormula(h2Data.getApril(), ethData.getApril(), ngData.getApril(), prod.getApril(), h2Const, ethConst));
	    dto.setMay(calculateFuelFormula(h2Data.getMay(), ethData.getMay(), ngData.getMay(), prod.getMay(), h2Const, ethConst));
	    dto.setJune(calculateFuelFormula(h2Data.getJune(), ethData.getJune(), ngData.getJune(), prod.getJune(), h2Const, ethConst));
	    dto.setJuly(calculateFuelFormula(h2Data.getJuly(), ethData.getJuly(), ngData.getJuly(), prod.getJuly(), h2Const, ethConst));
	    dto.setAugust(calculateFuelFormula(h2Data.getAugust(), ethData.getAugust(), ngData.getAugust(), prod.getAug(), h2Const, ethConst));
	    dto.setSeptember(calculateFuelFormula(h2Data.getSeptember(), ethData.getSeptember(), ngData.getSeptember(), prod.getSep(), h2Const, ethConst));
	    dto.setOctober(calculateFuelFormula(h2Data.getOctober(), ethData.getOctober(), ngData.getOctober(), prod.getOct(), h2Const, ethConst));
	    dto.setNovember(calculateFuelFormula(h2Data.getNovember(), ethData.getNovember(), ngData.getNovember(), prod.getNov(), h2Const, ethConst));
	    dto.setDecember(calculateFuelFormula(h2Data.getDecember(), ethData.getDecember(), ngData.getDecember(), prod.getDec(), h2Const, ethConst));
	    dto.setJanuary(calculateFuelFormula(h2Data.getJanuary(), ethData.getJanuary(), ngData.getJanuary(), prod.getJan(), h2Const, ethConst));
	    dto.setFebruary(calculateFuelFormula(h2Data.getFebruary(), ethData.getFebruary(), ngData.getFebruary(), prod.getFeb(), h2Const, ethConst));
	    dto.setMarch(calculateFuelFormula(h2Data.getMarch(), ethData.getMarch(), ngData.getMarch(), prod.getMarch(), h2Const, ethConst));
	}

	private Double calculateFuelFormula(Double h2Norm, Double ethNorm, Double ngGBT, Double prodVal, double h2CV, double ethCV) {
	    double production = val(prodVal);
	    if (production == 0) return 0.0; 
	    double divisor = 1000000.0;
	    double A = (val(ethNorm) * production * 1000.0 * ethCV * 4.186 * 1.055) / divisor;
	    
	    double B = (val(h2Norm) * production * 1000.0 * h2CV * 4.186 * 1.055) / divisor;
	    
	    double C = val(ngGBT);

	    return (A + B + C) / production;
	}

	private double val(Double value) {
		return value == null ? 0.0 : value;
	}

	public List<AOPDTO> getMonthlyProduction(List<Object[]> obj) {
		List<AOPDTO> aopDTOList = new ArrayList<>();
		for (Object[] row : obj) {
			AOPDTO aopDTO = new AOPDTO();

			aopDTO.setId(row[0] != null ? row[0].toString() : null);
			aopDTO.setNormParameterName(row[1] != null ? row[1].toString() : null);
			aopDTO.setNormParameterDisplayName(row[2] != null ? row[2].toString() : null);
			aopDTO.setNormParameterTypeId(row[3] != null ? row[3].toString() : null);
			aopDTO.setMaterialFKId(row[4] != null ? row[4].toString() : null);
			aopDTO.setDisplayName(row[5] != null ? row[5].toString() : null);

			aopDTO.setApril(safeParseDouble(row[6]));
			aopDTO.setMay(safeParseDouble(row[7]));
			aopDTO.setJune(safeParseDouble(row[8]));
			aopDTO.setJuly(safeParseDouble(row[9]));
			aopDTO.setAug(safeParseDouble(row[10]));
			aopDTO.setSep(safeParseDouble(row[11]));
			aopDTO.setOct(safeParseDouble(row[12]));
			aopDTO.setNov(safeParseDouble(row[13]));
			aopDTO.setDec(safeParseDouble(row[14]));
			aopDTO.setJan(safeParseDouble(row[15]));
			aopDTO.setFeb(safeParseDouble(row[16]));
			aopDTO.setMarch(safeParseDouble(row[17]));
			aopDTO.setAvgTPH(safeParseDouble(row[18]));
			aopDTO.setRemark(row[19] != null ? row[19].toString() : null);
			aopDTO.setDisplayOrder(row[20] != null ? Integer.valueOf(row[20].toString()) : null);
			aopDTO.setIsEditable(row[21] != null ? Boolean.valueOf(row[21].toString()) : null);
			aopDTO.setIsVisible(row[22] != null ? Boolean.valueOf(row[22].toString()) : null);

			aopDTOList.add(aopDTO);
		}
		return aopDTOList;

	}

	private Double safeParseDouble(Object obj) {
		if (obj == null) {
			return null;
		}
		String s = obj.toString().trim();
		if (s.isEmpty()) {
			return null;
		}
		try {
			return Double.valueOf(s);
		} catch (NumberFormatException ex) {
			// Logging is optional but helpful to track bad data
			System.err.println("Warning: could not parse to Double: '" + s + "'");
			return null;
		}
	}

	public Map<String, Double> getConstantsMap(String aopYear, String plantId, String procedure) {
		Map<String, Double> constantsMap = new HashMap<>();
		List<Object[]> obj = findConstantsByYearAndPlantFkId(aopYear, plantId, procedure);

		for (Object[] row : obj) {
			String displayName = (row[3] != null) ? row[3].toString() : null;
			if (displayName != null) {
				Double value = (row[5] != null) ? Double.parseDouble(row[5].toString()) : 0.0;
				constantsMap.put(displayName, value);
			}
		}
		return constantsMap;
	}

	public List<Object[]> findConstantsByYearAndPlantFkId(String aopYear, String plantId, String procedureName) {
		try {
			String sql = "EXEC " + procedureName + " @plantId = :plantId, @aopYear = :aopYear";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	private static String getStringCellValue(Cell cell, MCUNormsValueDTO dto) {
		try {

			if (cell == null || cell.getCellType() == CellType.BLANK) {
				return null;
			}

			String value;
			if (cell.getCellType() == CellType.STRING) {
				value = cell.getStringCellValue();
			} else {

				cell.setCellType(CellType.STRING);
				value = cell.getStringCellValue();
			}

			if (value == null || value.trim().isEmpty()) {
				return null;
			}

			return value.trim();

		} catch (Exception e) {
			dto.setSaveStatus("Failed");
			dto.setErrDescription("Error reading string value");
			e.printStackTrace();
		}
		return null;
	}

	private static Double getNumericCellValue(Cell cell, MCUNormsValueDTO dto) {

		if (cell == null || cell.getCellType() == CellType.BLANK) {
			return null;
		}

		if (cell.getCellType() == CellType.NUMERIC) {
			return cell.getNumericCellValue();
		}

		if (cell.getCellType() == CellType.STRING) {
			String cellValue = cell.getStringCellValue().trim();
			if (cellValue.isEmpty()) {
				return null;
			}

			try {
				return Double.parseDouble(cellValue);
			} catch (NumberFormatException e) {
				dto.setSaveStatus("Failed");
				dto.setErrDescription("Invalid number format: " + cellValue);
				return null;
			}
		}
		if (cell.getCellType() == CellType.FORMULA) {
			try {

				return cell.getNumericCellValue();
			} catch (Exception e) {
				return null;
			}
		}

		return null;
	}

	public static Boolean getBooleanCellValue(Cell cell, MCUNormsValueDTO dto) {
		if (cell == null)
			return null;

		CellType type = cell.getCellType();
		if (type == CellType.FORMULA) {
			type = cell.getCachedFormulaResultType();
		}

		switch (type) {
			case BOOLEAN:
				return cell.getBooleanCellValue();
			case STRING:
				String text = cell.getStringCellValue().trim().toLowerCase();
				if ("true".equals(text))
					return true;
				if ("false".equals(text))
					return false;
				return null;
			case NUMERIC:
				double num = cell.getNumericCellValue();
				if (num == 1.0)
					return true;
				if (num == 0.0)
					return false;
				return null;
			case BLANK:
			case _NONE:
			default:
				return null;
		}
	}

	public List<Map<String, String>> extractGradeInfo(AOPMessageVM grades) {
		List<Map<String, String>> gradeInfoList = new ArrayList<>();

		Object data = grades.getData();

		if (data instanceof List) {
			try {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> gradeList = (List<Map<String, Object>>) data;

				for (Map<String, Object> gradeMap : gradeList) {
					Object gradeIdObj = gradeMap.get("gradeId");
					Object displayNameObj = gradeMap.get("displayName");

					if (gradeIdObj != null && displayNameObj != null) {
						Map<String, String> infoMap = new HashMap<>();
						infoMap.put("gradeId", gradeIdObj.toString());
						infoMap.put("displayName", displayNameObj.toString());
						gradeInfoList.add(infoMap);
					}
				}
			} catch (ClassCastException e) {
				System.err.println("Error casting data to List<Map<String, Object>>: " + e.getMessage());
			}
		}

		return gradeInfoList;
	}

	public byte[] exportSteadyStateNorms(String year, UUID plantFKId, boolean isAfterSave,
			List<MCUNormsValueDTO> dtoList, String mode) {
		try {
			AOPMessageVM gradesVM = getNormalOperationNormsGrades(year, plantFKId.toString());
			List<Map<String, String>> gradeInfoList = extractGradeInfo(gradesVM);
			Workbook workbook = new XSSFWorkbook();
			CellStyle lockedStyle = Utility.createLockedStyle(workbook);
			CellStyle unlockedStyle = Utility.createUnlockedStyle(workbook);

			for (Map<String, String> gradeInfo : gradeInfoList) {

				String currentGradeId = gradeInfo.get("gradeId");
				String sheetName = Utility.sanitizeSheetName(gradeInfo.get("displayName"));

				AOPMessageVM aopMessageVM = null;
				List<MCUNormsValueDTO> currentDtoList = new ArrayList<>();
				List<Boolean> isEditable = new ArrayList<>();
				if (!isAfterSave) {
					aopMessageVM = getNormalOperationNormsData(year, plantFKId.toString(), currentGradeId, mode);
				}
				if (aopMessageVM != null && aopMessageVM.getData() != null) {

					Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
					currentDtoList = (List<MCUNormsValueDTO>) responseMap.get("mcuNormsValueDTOList");
				} else if (isAfterSave) {
					currentDtoList = dtoList.stream().filter(dto -> currentGradeId.equals(dto.getGradeId()))
							.collect(Collectors.toList());
				} else {
					continue;
				}

				Sheet sheet = workbook.createSheet(sheetName);
				int currentRow = 0;

				List<List<Object>> rows = new ArrayList<>();
				for (MCUNormsValueDTO dto : currentDtoList) {
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
					isEditable.add(dto.getIsEditable());

					if (isAfterSave) {
						list.add(dto.getSaveStatus());
						list.add(dto.getErrDescription());
					}
					rows.add(list);
				}

				List<String> innerHeaders = new ArrayList<>();
				innerHeaders.add("Type");
				innerHeaders.add("Particulars");
				innerHeaders.add("UOM");
				List<String> monthsList = getAcademicYearMonths(year);
				innerHeaders.addAll(monthsList);
				innerHeaders.add("Remarks");
				innerHeaders.add("Id");
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
						cell.setCellStyle(Utility.createBoldBorderedStyle(workbook));
					}
				}

				for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
					List<Object> rowData = rows.get(rowIndex);
					boolean isRowEditable = true;

					if (rowIndex < isEditable.size() && isEditable.get(rowIndex) != null) {
						isRowEditable = isEditable.get(rowIndex);
					}

					Row row = sheet.createRow(currentRow++);
					for (int col = 0; col < rowData.size(); col++) {
						Cell cell = row.createCell(col);
						Object value = rowData.get(col);

						if (value instanceof Number) {
							cell.setCellValue(((Number) value).doubleValue());
						} else if (value instanceof Boolean) {
							cell.setCellValue((Boolean) value);
						} else if (value != null) {
							cell.setCellValue(value.toString());
						} else {
							cell.setCellValue("");
						}

						if (isRowEditable) {
							cell.setCellStyle(unlockedStyle);
						} else {
							cell.setCellStyle(lockedStyle);
						}
					}
				}
				sheet.setColumnHidden(16, true);

			}

			try {
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

	public byte[] createExcel(String year, UUID plantFKId, boolean isAfterSave, List<MCUNormsValueDTO> dtoList,
			String mode, String gradeId) {
		try {
			AOPMessageVM aopMessageVM = getNormalOperationNormsData(year, plantFKId.toString(), gradeId, mode);
			List<Boolean> isEditable = new ArrayList<>();
			Plants plant = plantsRepository.findById(plantFKId).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			if (!isAfterSave) {
				Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
				dtoList = (List<MCUNormsValueDTO>) responseMap.get("mcuNormsValueDTOList");
			}

			Workbook workbook = new XSSFWorkbook();

			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;
			// List<List<Object>> rows = new ArrayList<>();

			List<List<Object>> rows = new ArrayList<>();

			// Create styles for locking/unlocking cells
			CellStyle lockedStyle = workbook.createCellStyle();
			lockedStyle.setLocked(true);
			lockedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			lockedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle unlockedStyle = workbook.createCellStyle();
			unlockedStyle.setLocked(false);
			// Data rows
			for (MCUNormsValueDTO dto : dtoList) {
				// if (isAfterSave) {
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
				if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("PTA")) {
					list.add(dto.getWtAverage());
				}

				list.add(dto.getRemarks());
				list.add(dto.getId());
				isEditable.add(dto.getIsEditable());
				// list.add(dto.getMaterialFkId());
				// list.add(dto.getIsEditable());
				if (isAfterSave) {
					list.add(dto.getSaveStatus());
					list.add(dto.getErrDescription());
				}
				rows.add(list);
				// }
			}

			List<String> innerHeaders = new ArrayList<>();
			innerHeaders.add("Type");
			innerHeaders.add("Particulars");
			innerHeaders.add("UOM");
			List<String> monthsList = getAcademicYearMonths(year);
			innerHeaders.addAll(monthsList);
			if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("PTA")) {
				innerHeaders.add("Weighted Avg");
			}
			innerHeaders.add("Remarks");
			innerHeaders.add("Id");
			// innerHeaders.add("NormParamterId");
			// innerHeaders.add("IsEditable");
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
					cell.setCellStyle(Utility.createBoldBorderedStyle(workbook));
				}
			}
			for (List<Object> rowData : rows) {
				boolean isRowEditable = true;
				if (isEditable.get(currentRow - 1) != null) {
					isRowEditable = isEditable.get(currentRow - 1);
				}

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
					if (isRowEditable) {
						cell.setCellStyle(unlockedStyle);
					} else {
						cell.setCellStyle(lockedStyle);
					}

				}
			}
			if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("PTA")) {
				sheet.setColumnHidden(17, true);
			} else {
				sheet.setColumnHidden(16, true);
			}
			// sheet.setColumnHidden(18, true);
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

	@Override
	public AOPMessageVM getNormsTransactionFinalNormsModeWise(String plantId, String aopYear) {
		try {
			List<Map<String, Object>> result = new ArrayList<>();
			AOPMessageVM aopMessageVM = getNormsTransaction(plantId, aopYear);
			List<Map<String, Object>> normsTransactions = (List<Map<String, Object>>) aopMessageVM.getData();

			AOPMessageVM finalNorms = finalNormsService.getFinalNorms(aopYear, plantId, null, null);
			Map<String, Object> dataMap = (Map<String, Object>) finalNorms.getData();
			List<ModeWiseNormsDTO> finalNormsDTOList = (List<ModeWiseNormsDTO>) dataMap.get("mcuNormsValueDTOList");
			Map<String, String> sapMaterialCodeToIdMap = finalNormsDTOList.stream()
					.collect(Collectors.toMap(ModeWiseNormsDTO::getSapMaterialCode, ModeWiseNormsDTO::getMaterialFKId,
							(existing, replacement) -> existing));

			for (Map<String, Object> map : normsTransactions) {
				Object normParameterFKId = map.get("normParameterFKId");
				if (normParameterFKId != null) {
					UUID normParameterId = UUID.fromString(normParameterFKId.toString());
					Optional<NormParameters> normParametersOpt = normParametersRepository.findById(normParameterId);
					if (normParametersOpt.isPresent()) {
						NormParameters normParameters = normParametersOpt.get();
						if (!normParameters.getType().equalsIgnoreCase("Monthly")) {
							String sapMaterialCode = normParameters.getSapMaterialCode();
							if (sapMaterialCodeToIdMap.containsKey(sapMaterialCode)) {
								Map<String, Object> finalMap = new HashMap<>();
								finalMap.put("month", map.get("month"));
								finalMap.put("normParameterId", sapMaterialCodeToIdMap.get(sapMaterialCode));
								result.add(finalMap);
							}
						}
					}
				}
			}

			AOPMessageVM finalResult = new AOPMessageVM();
			finalResult.setCode(200);
			finalResult.setData(result);
			finalResult.setMessage("Data fetched successfully");
			return finalResult;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public AOPMessageVM getNormsTransactionFinalNorms(String plantId, String aopYear) {
		List<Map<String, Object>> result = new ArrayList<>();

		AOPMessageVM aopMessageVM = getNormsTransaction(plantId, aopYear);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> normsTransactions = (List<Map<String, Object>>) aopMessageVM.getData();

		AOPMessageVM finalNorms = finalNormsService.getFinalNorms(aopYear, plantId, null, null);
		@SuppressWarnings("unchecked")
		Map<String, Object> dataMap = (Map<String, Object>) finalNorms.getData();
		@SuppressWarnings("unchecked")
		List<ModeWiseNormsDTO> finalNormsDTOList = (List<ModeWiseNormsDTO>) dataMap.get("mcuNormsValueDTOList");

		Map<String, String> materialNameToFKId = finalNormsDTOList.stream()
				.collect(Collectors.toMap(ModeWiseNormsDTO::getSapMaterialCode, ModeWiseNormsDTO::getMaterialFKId,
						(existing, replacement) -> existing));

		for (Map<String, Object> map : normsTransactions) {
			Object normParameterFKIdObj = map.get("normParameterFKId");
			if (normParameterFKIdObj != null) {
				UUID normParameterId = UUID.fromString(normParameterFKIdObj.toString());
				Optional<NormParameters> normParametersOpt = normParametersRepository.findById(normParameterId);
				if (normParametersOpt.isPresent()) {
					NormParameters normParameters = normParametersOpt.get();
					if (normParameters.getType().equalsIgnoreCase("Monthly")) {
						UUID normParameterTypeFkId = normParameters.getNormParameterTypeFkId();
						Optional<NormParameterType> normParameterTypeOpt = normParameterTypeRepository
								.findById(normParameterTypeFkId);
						if (normParameterTypeOpt.isPresent()) {
							NormParameterType normParameterType = normParameterTypeOpt.get();
							String typeName = normParameterType.getName();
							if (typeName.equalsIgnoreCase("RawMaterial") || typeName.equalsIgnoreCase("ByProducts")) {

								String materialName = normParameters.getSapMaterialCode();
								if (materialNameToFKId.containsKey(materialName)) {
									Map<String, Object> finalMap = new HashMap<>();
									finalMap.put("month", map.get("month"));
									finalMap.put("normParameterId", materialNameToFKId.get(materialName));

									result.add(finalMap);
								}
							} else {

								List<MCUNormsValue> mcuNormsValues = mcuNormsValueRepository
										.findCheckedNormsByMaterialFkIdNative(normParameterId);
								if (!mcuNormsValues.isEmpty()) {
									String materialName = normParameters.getSapMaterialCode();
									if (materialNameToFKId.containsKey(materialName)) {
										Map<String, Object> finalMap = new HashMap<>();
										finalMap.put("month", map.get("month"));
										finalMap.put("normParameterId", materialNameToFKId.get(materialName));
										result.add(finalMap);
									}
								}
							}
						}
					}
				}
			}
		}

		AOPMessageVM finalResult = new AOPMessageVM();
		finalResult.setCode(200);
		finalResult.setData(result);
		finalResult.setMessage("Data fetched successfully");
		return finalResult;
	}

	public int executeProcedure(String procedureName, String plantId,
			String aopYear) {
		try {

			String callSql = "{call " + procedureName + "(?, ?)}";

			try (Connection connection = dataSource.getConnection();
					CallableStatement stmt = connection.prepareCall(callSql)) {
				stmt.setString(1, plantId);
				stmt.setString(2, aopYear);
				int rowsAffected = stmt.executeUpdate();
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

}
