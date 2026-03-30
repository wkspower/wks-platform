package com.wks.caseengine.vgoht.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.vgoht.dto.VgohtNormConfigurationDTO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import jakarta.persistence.Query;


@Service
public class VgohtHelperService {

    @PersistenceContext
	private EntityManager entityManager;

    	@Transactional
	public AOPMessageVM saveConfigurationDataOnlyModified(String year, UUID plantFKId, String version,
			List<VgohtNormConfigurationDTO> vgohtNormConfigurationDTOList, String periodFrom, String periodTo) {

		try {
			System.out.println(":::::::::::::::::::::::::::::Saving parameter: " + plantFKId);
			System.out.println(":::::::::::::::::::::::::::::Saving parameter: " + vgohtNormConfigurationDTOList);
			System.out.println(":::::::::::::::::::::::::::::Saving parameter: " + vgohtNormConfigurationDTOList.size());
			for (VgohtNormConfigurationDTO dto : vgohtNormConfigurationDTOList) {
				System.out.println(":::::::::::::::::::::::::::Saving parameter");
				if (dto.getNormParameterFKId() == null) {
					System.out.println(":::::::::::::::::::::::::::Saving parameter: " );
					throw new IllegalArgumentException("NormParameterFKId is missing");
				}
				saveMonthValue(dto.getNormParameterFKId(), year, "1", dto.getJan(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "2", dto.getFeb(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "3", dto.getMar(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "4", dto.getApr(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "5", dto.getMay(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "6", dto.getJun(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "7", dto.getJul(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "8", dto.getAug(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "9", dto.getSep(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "10", dto.getOct(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "11", dto.getNov(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "12", dto.getDec(), dto.getRemarks());
			}
			AOPMessageVM response = new AOPMessageVM();
			response.setCode(200);
			response.setMessage("Configuration saved successfully");
			return response;

		} catch (Exception e) {
			System.out.println("print:::::::::::::" + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Error saving configuration data", e);
		}
	}


	private void saveMonthValue(String normParameterId, String year, String month, Double value, String remarks) {

		String sql = """
			MERGE INTO NormAttributeTransactions AS target
			USING (SELECT :normParameterId AS NormParameter_FK_Id,
						:year AS AuditYear,
						:month AS AOPMonth) AS source
			ON target.NormParameter_FK_Id = source.NormParameter_FK_Id
			AND target.AuditYear = source.AuditYear
			AND target.AOPMonth = source.AOPMonth

			WHEN MATCHED THEN
				UPDATE SET AttributeValue = :value,
						Remarks = :remarks

			WHEN NOT MATCHED THEN
				INSERT (Id, NormParameter_FK_Id, AuditYear, AOPMonth, AttributeValue, Remarks)
				VALUES (NEWID(), :normParameterId, :year, :month, :value, :remarks);
		""";

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("normParameterId", normParameterId);
		query.setParameter("year", year);
		query.setParameter("month", month);
		query.setParameter("value", value);
		query.setParameter("remarks", remarks);

		query.executeUpdate();
	}

    // @Transactional(propagation = Propagation.REQUIRES_NEW)
	public String executeNormCalculationProcedure(UUID plantId, String aopYear, UUID siteId,String periodFrom, String periodTo,String procedureName) {

		try {
            System.out.println("Is new transaction active: " + TransactionSynchronizationManager.isActualTransactionActive());

			StoredProcedureQuery query = entityManager
					.createStoredProcedureQuery(procedureName);

			// Input parameters
			query.registerStoredProcedureParameter("plantId", String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("AOPYear", String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("siteid", String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("PeriodFrom", String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("PeriodTo", String.class, ParameterMode.IN);

			// OUTPUT parameter
			query.registerStoredProcedureParameter("ErrorMessage", String.class, ParameterMode.OUT);

			query.setParameter("plantId", plantId.toString());
			query.setParameter("AOPYear", aopYear);
			query.setParameter("siteid", siteId.toString());
			query.setParameter("PeriodFrom", periodFrom);
			query.setParameter("PeriodTo", periodTo);

			query.execute();

			try {
				query.getResultList(); // flush any pending result sets
			} catch (Exception ignored) {}

			String errorMessage = (String) query.getOutputParameterValue("ErrorMessage");

			System.out.println("errorMessage string: " + errorMessage);

			return errorMessage;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to execute procedure", ex);
		}
	}

}
