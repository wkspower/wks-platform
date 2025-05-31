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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.MCUNormsValueDTO;
import com.wks.caseengine.entity.AOPSummary;
import com.wks.caseengine.entity.MCUNormsValue;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.NormsTransactions;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.NormalOperationNormsRepository;
import com.wks.caseengine.repository.NormsTransactionRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.SQLException;
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

	private DataSource dataSource;

	// Inject or set your DataSource (e.g., via constructor or setter)
	public NormalOperationNormsServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public List<MCUNormsValueDTO> getNormalOperationNormsData(String year, String plantId) {
		try {
			List<Object[]> obj = getNormalOperationNormsDataFromView(year, UUID.fromString(plantId));
			List<MCUNormsValueDTO> mCUNormsValueDTOList = new ArrayList<>();

			for (Object[] row : obj) {
				MCUNormsValueDTO mCUNormsValueDTO = new MCUNormsValueDTO();
				mCUNormsValueDTO.setId(row[0].toString());
				mCUNormsValueDTO.setSiteFkId(row[1].toString());
				mCUNormsValueDTO.setPlantFkId(row[2].toString());
				mCUNormsValueDTO.setVerticalFkId(row[3].toString());
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
				mCUNormsValueDTOList.add(mCUNormsValueDTO);
			}

			return mCUNormsValueDTOList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public List<MCUNormsValueDTO> saveNormalOperationNormsData(List<MCUNormsValueDTO> mCUNormsValueDTOList) {
		
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String userId = authentication.getName();
			List<NormsTransactions> transactionsToSave = new ArrayList<>();

		    for (MCUNormsValueDTO dto : mCUNormsValueDTOList) {
		        Optional<MCUNormsValue> optionalValue = normalOperationNormsRepository.findById(UUID.fromString(dto.getId()));
		        if (optionalValue.isEmpty()) {
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
						normsTransactions.setPlantFkId(value.getPlantFkId());
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
				MCUNormsValue mCUNormsValue = new MCUNormsValue();
				if (mCUNormsValueDTO.getId() != null || !mCUNormsValueDTO.getId().isEmpty()) {
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
				if (mCUNormsValueDTO.getPlantFkId() != null) {
					mCUNormsValue.setPlantFkId(UUID.fromString(mCUNormsValueDTO.getPlantFkId()));
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

				System.out.println("Data Saved Succussfully");
				normalOperationNormsRepository.save(mCUNormsValue);
			}
			// TODO Auto-generated method stub
			return mCUNormsValueDTOList;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	@Override
	@Transactional
	public int calculateExpressionConsumptionNorms(String year, String plantId) {
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		String storedProcedure = vertical.getName() + "_" + site.getName() + "_NormsCalculation";
		System.out.println("storedProcedure" + storedProcedure);
		return executeDynamicUpdateProcedure(storedProcedure, plantId, site.getId().toString(),
				vertical.getId().toString(), year);
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
	public List<Object[]> getNormalOperationNormsDataFromView(String financialYear, UUID plantId) {
		try {
			Plants plant = plantsRepository.findById(plantId).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

			String viewName = "vwScrn" + vertical.getName() + "NormalOperationNorms";
			// Validate or sanitize viewName before using it directly in the query to
			// prevent SQL injection
			String sql = "SELECT * FROM " + viewName
					+ " WHERE FinancialYear = :financialYear AND Plant_FK_Id = :plantId";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("financialYear", financialYear);
			query.setParameter("plantId", plantId);

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

}
