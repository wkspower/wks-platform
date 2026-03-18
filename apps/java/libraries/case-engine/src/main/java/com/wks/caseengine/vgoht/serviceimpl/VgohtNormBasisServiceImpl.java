package com.wks.caseengine.vgoht.serviceimpl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.vgoht.dto.VgohtNormConfigurationDTO;
import com.wks.caseengine.vgoht.service.VgohtNormBasisService;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class VgohtNormBasisServiceImpl implements VgohtNormBasisService {

    @Autowired
	private PlantsRepository plantsRepository;

    @Autowired
	private SiteRepository siteRepository;

    @Autowired
	private VerticalsRepository verticalRepository;

    @PersistenceContext
	private EntityManager entityManager;
    

    @Autowired
    private JdbcTemplate jdbcTemplate;

	public AOPMessageVM getConfigurationData(String year, UUID plantFKId,String version) {
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(plantFKId);
			String viewName = "vwScrn" + verticalName + "GetConfigTypes";
			Plants plant = plantsRepository.findById((plantFKId))
	                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		    // boolean pvc= vertical.getName().equalsIgnoreCase("PVC") && site.getName().equalsIgnoreCase("VMD");
			List<Object[]> obj = new ArrayList<>();
			// if ((verticalName.equalsIgnoreCase("MEG"))
			// 		|| (verticalName.equalsIgnoreCase("CRACKER"))) {

			// 	String procedureName = verticalName + "_GetConfiguration";
			// 	obj = findByYearAndPlantFkIdMEG(year, plantFKId, procedureName);
			// }else if(verticalName.equalsIgnoreCase("AROMATICS")) {		
			// 	obj = findByYearAndPlantFkIdAROMATICS(year, plantFKId, viewName,getVersion(year,plantFKId));
			// } else {
			obj = findByYearAndPlantFkId(year, plantFKId, viewName);
			// }
			
			List<VgohtNormConfigurationDTO> vgohtNormConfigurationDTOList = new ArrayList<>();
			int i = 0;
			for (Object[] row : obj) {
				VgohtNormConfigurationDTO vgohtNormConfigurationDTO = new VgohtNormConfigurationDTO();
				vgohtNormConfigurationDTO.setNormParameterFKId(row[0] != null ? row[0].toString() : "");

				vgohtNormConfigurationDTO.setJan(
						(row[1] != null && !row[1].toString().trim().isEmpty())
								? Double.parseDouble(row[1].toString().trim())
								: 0.0);
				vgohtNormConfigurationDTO.setFeb(
						(row[2] != null && !row[2].toString().trim().isEmpty()) ? Double.parseDouble(row[2].toString())
								: 0.0);
				vgohtNormConfigurationDTO.setMar(
						(row[3] != null && !row[3].toString().trim().isEmpty()) ? Double.parseDouble(row[3].toString())
								: 0.0);
				vgohtNormConfigurationDTO.setApr(
						(row[4] != null && !row[4].toString().trim().isEmpty()) ? Double.parseDouble(row[4].toString())
								: 0.0);
				vgohtNormConfigurationDTO.setMay(
						(row[5] != null && !row[5].toString().trim().isEmpty()) ? Double.parseDouble(row[5].toString())
								: 0.0);
				vgohtNormConfigurationDTO.setJun(
						(row[6] != null && !row[6].toString().trim().isEmpty()) ? Double.parseDouble(row[6].toString())
								: 0.0);
				vgohtNormConfigurationDTO.setJul(
						(row[7] != null && !row[7].toString().trim().isEmpty()) ? Double.parseDouble(row[7].toString())
								: 0.0);
				vgohtNormConfigurationDTO.setAug(
						(row[8] != null && !row[8].toString().trim().isEmpty()) ? Double.parseDouble(row[8].toString())
								: 0.0);
				vgohtNormConfigurationDTO.setSep(
						(row[9] != null && !row[9].toString().trim().isEmpty()) ? Double.parseDouble(row[9].toString())
								: 0.0);
				vgohtNormConfigurationDTO.setOct((row[10] != null && !row[10].toString().trim().isEmpty())
						? Double.parseDouble(row[10].toString())
						: 0.0);
				vgohtNormConfigurationDTO.setNov((row[11] != null && !row[11].toString().trim().isEmpty())
						? Double.parseDouble(row[11].toString())
						: 0.0);
				vgohtNormConfigurationDTO.setDec((row[12] != null && !row[12].toString().trim().isEmpty())
						? Double.parseDouble(row[12].toString())
						: 0.0);
				vgohtNormConfigurationDTO.setRemarks((row[13] != null ? row[13].toString() : ""));

				// if (verticalName.equalsIgnoreCase("PE") || verticalName.equalsIgnoreCase("PP") || verticalName.equalsIgnoreCase("PET") || verticalName.equalsIgnoreCase("PTA") || (verticalName.equalsIgnoreCase("VCM")) || (verticalName.equalsIgnoreCase("AROMATICS")) || (verticalName.equalsIgnoreCase("ELASTOMER")) || pvc) {
					vgohtNormConfigurationDTO.setId(row[14] != null ? row[14].toString() : i + "#");

					vgohtNormConfigurationDTO.setAuditYear(row[15] != null ? row[15].toString() : "");
					vgohtNormConfigurationDTO.setUOM(row[16] != null ? row[16].toString() : "");

					vgohtNormConfigurationDTO.setConfigTypeDisplayName(row[17] != null ? row[17].toString() : "");
					vgohtNormConfigurationDTO.setTypeDisplayName(row[18] != null ? row[18].toString() : "");
					// vgohtNormConfigurationDTO.setConfigTypeName(row[19] != null ? row[19].toString() : "");
					// vgohtNormConfigurationDTO.setTypeName(row[20] != null ? row[20].toString() : "");
					vgohtNormConfigurationDTO.setProductName(row[19] != null ? row[19].toString() : "");
					vgohtNormConfigurationDTO.setProductDisplayOrder(row[20] != null ? row[20].toString() : "");

				// }
				/*
				 * if(verticalName.equalsIgnoreCase("AROMATICS")) {
				 * vgohtNormConfigurationDTO.setVersion(row[22] != null ? row[22].toString() : ""); }
				 */

				// if (verticalName.equalsIgnoreCase("MEG")
				// 		|| verticalName.equalsIgnoreCase("CRACKER")) {

				// 	vgohtNormConfigurationDTO.setAuditYear(row[14] != null ? row[14].toString() : "");
				// 	vgohtNormConfigurationDTO.setUOM(row[15] != null ? row[15].toString() : "");
				// 	vgohtNormConfigurationDTO.setNormType(row[16] != null ? row[16].toString() : "");
				// 	vgohtNormConfigurationDTO.setIsEditable(row[17] != null ? ((Boolean) row[17]).booleanValue() : null);
				// 	vgohtNormConfigurationDTO.setProductName(row[18] != null ? row[18].toString() : "");
				// }
				vgohtNormConfigurationDTOList.add(vgohtNormConfigurationDTO);
				if (row[14] == null) {
					i++;
				}

			}
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(vgohtNormConfigurationDTOList);
			aopMessageVM.setMessage("Data fetched successfully");
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> findByYearAndPlantFkId(String year, UUID plantFKId, String viewName) {
		try {

				String sql = "SELECT " + "    NP.Id AS NormParameter_FK_Id, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '1' THEN NAT.AttributeValue ELSE NULL END) AS Jan, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '2' THEN NAT.AttributeValue ELSE NULL END) AS Feb, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '3' THEN NAT.AttributeValue ELSE NULL END) AS Mar, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '4' THEN NAT.AttributeValue ELSE NULL END) AS Apr, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '5' THEN NAT.AttributeValue ELSE NULL END) AS May, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '6' THEN NAT.AttributeValue ELSE NULL END) AS Jun, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '7' THEN NAT.AttributeValue ELSE NULL END) AS Jul, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '8' THEN NAT.AttributeValue ELSE NULL END) AS Aug, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '9' THEN NAT.AttributeValue ELSE NULL END) AS Sep, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '10' THEN NAT.AttributeValue ELSE NULL END) AS Oct, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '11' THEN NAT.AttributeValue ELSE NULL END) AS Nov, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '12' THEN NAT.AttributeValue ELSE NULL END) AS Dec, "
					+ "    MAX(NAT.Remarks) AS Remarks, " + "    MAX(NAT.Id) AS NormAttributeTransaction_Id, "
					+ "    MAX(NAT.AuditYear) AS AuditYear, " + "    MAX(NP.UOM) AS UOM, "
					+ "    CP.DisplayName AS ConfigTypeDisplayName, "
					+ "    NPT.DisplayName AS TypeDisplayName, " + "    NP.DisplayName AS DisplayName, NP.DisplayOrder AS DisplayOrder "
					// + "    NP.TypeName AS TypeName, MAX(NP.DisplayName) "
					+ "    FROM NormParameters NP "
					+ "    JOIN NormParameterType NPT ON NP.NormParameterType_FK_Id = NPT.Id"
					+ "    JOIN ConfigurationTypes_NormParameter_Mapping CPJ ON CPJ.NormParameter_FK_Id=NP.Id  AND CPJ.NormParamterType_FK_Id = NPT.Id"
					+ "    JOIN ConfigurationTypes CP ON CP.Id = CPJ.ConfigurationType_FK_Id"
					+ "    LEFT JOIN NormAttributeTransactions NAT ON NAT.NormParameter_FK_Id = NP.Id "
					+ "    AND NAT.AuditYear = :year " 
					// + "WHERE (NPT.Name = 'Configuration'  OR NPT.Name = 'Constant') "
					+ "  WHERE NP.Plant_FK_Id = :plantFKId " + "GROUP BY " + "   NP.Id,  NP.NormParameterType_FK_Id, "
					+ "    NP.DisplayName, " + "    NP.DisplayOrder, " + "    CP.DisplayName, "
					+ "    NPT.DisplayName, " + "    NPT.DisplayOrder "
					+ "ORDER BY NPT.DisplayOrder, NP.DisplayOrder";

					                          

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("year", year);
			query.setParameter("plantFKId", plantFKId);

			return query.getResultList();
		} catch (Exception e) {
			throw new RuntimeException("Error fetching data with dynamic view name", e);
		}
	}
	
	@Transactional
	public AOPMessageVM saveConfigurationData(String year, UUID plantFKId, String version,
			List<VgohtNormConfigurationDTO> vgohtNormConfigurationDTOList) {

		try {

			for (VgohtNormConfigurationDTO dto : vgohtNormConfigurationDTOList) {

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


	@Transactional
	public AOPMessageVM saveYearlyValues(String year, UUID plantFKId, List<VgohtNormConfigurationDTO> dtoList) {

		try {
			for (VgohtNormConfigurationDTO dto : dtoList) {
				if (dto.getValue() != null) {
					saveYearlyValue(dto.getNormParameterFKId(), year, dto.getValue(), dto.getRemarks());
				}
			}

			AOPMessageVM response = new AOPMessageVM();
			response.setCode(200);
			response.setMessage("Yearly values saved successfully");
			return response;

		} catch (Exception e) {
			throw new RuntimeException("Error saving yearly values", e);
		}
	}
	private void saveYearlyValue(String normParameterId, String year, Double value, String remarks) {
		String sql = """
			MERGE INTO NormAttributeTransactions AS target
			USING (SELECT :normParameterId AS NormParameter_FK_Id,
						:year AS AuditYear) AS source
			ON target.NormParameter_FK_Id = source.NormParameter_FK_Id
			AND target.AuditYear = source.AuditYear
			AND target.AOPMonth IS NULL
			WHEN MATCHED THEN
				UPDATE SET AttributeValue = :value,
						Remarks = :remarks
			WHEN NOT MATCHED THEN
				INSERT (Id, NormParameter_FK_Id, AuditYear, AOPMonth, AttributeValue, Remarks)
				VALUES (NEWID(), :normParameterId, :year, NULL, :value, :remarks);
		""";

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("normParameterId", normParameterId);
		query.setParameter("year", year);
		query.setParameter("value", value);
		query.setParameter("remarks", remarks);
		query.executeUpdate();
	}
	public AOPMessageVM getYearlyValues(String year, UUID plantFKId) {

		try {
			String sql = """
				SELECT NP.Id AS NormParameter_FK_Id,
					NP.DisplayName,
					MAX(NAT.AttributeValue) AS value,
					MAX(NAT.Remarks) AS remarks,
					NP.UOM,
					MAX(NPT.DisplayName) AS NormParameterTypeDisplayName
				FROM NormParameters NP
				JOIN NormParameterType NPT on NP.NormParameterType_FK_Id = NPT.Id
				LEFT JOIN NormAttributeTransactions NAT
					ON NAT.NormParameter_FK_Id = NP.Id
					AND NAT.AuditYear = :year
				WHERE NP.Plant_FK_Id = :plantFKId
				AND NAT.AOPMonth IS NULL
				GROUP BY NP.Id, NP.DisplayName, NP.DisplayOrder, NP.UOM
				ORDER BY NP.DisplayOrder
			""";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("year", year);
			query.setParameter("plantFKId", plantFKId);

			List<Object[]> resultList = query.getResultList();
			List<VgohtNormConfigurationDTO> dtoList = new ArrayList<>();

			for (Object[] row : resultList) {
				VgohtNormConfigurationDTO dto = new VgohtNormConfigurationDTO();
				dto.setNormParameterFKId(row[0] != null ? row[0].toString() : "");
				dto.setProductName(row[1] != null ? row[1].toString() : "");
				dto.setValue(row[2] != null ? Double.parseDouble(row[2].toString()) : 0.0);
				dto.setRemarks(row[3] != null ? row[3].toString() : "");
				dto.setUOM(row[4] != null ? row[4].toString() : "");
				dto.setTypeDisplayName(row[5] != null ? row[5].toString() : "");
				dtoList.add(dto);
			}

			AOPMessageVM response = new AOPMessageVM();
			response.setCode(200);
			response.setData(dtoList);
			response.setMessage("Yearly values fetched successfully");

			return response;

		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch yearly values", e);
		}
	}
}
