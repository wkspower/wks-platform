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

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.tcs.repository.NormBasisRepository;
import com.wks.caseengine.vgoht.service.VgohtNormBasisService;
import jakarta.persistence.Query;

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
    private NormBasisRepository normBasisRepository;

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
			
			List<ConfigurationDTO> configurationDTOList = new ArrayList<>();
			int i = 0;
			for (Object[] row : obj) {
				ConfigurationDTO configurationDTO = new ConfigurationDTO();
				configurationDTO.setNormParameterFKId(row[0] != null ? row[0].toString() : "");

				configurationDTO.setJan(
						(row[1] != null && !row[1].toString().trim().isEmpty())
								? Double.parseDouble(row[1].toString().trim())
								: 0.0);
				configurationDTO.setFeb(
						(row[2] != null && !row[2].toString().trim().isEmpty()) ? Double.parseDouble(row[2].toString())
								: 0.0);
				configurationDTO.setMar(
						(row[3] != null && !row[3].toString().trim().isEmpty()) ? Double.parseDouble(row[3].toString())
								: 0.0);
				configurationDTO.setApr(
						(row[4] != null && !row[4].toString().trim().isEmpty()) ? Double.parseDouble(row[4].toString())
								: 0.0);
				configurationDTO.setMay(
						(row[5] != null && !row[5].toString().trim().isEmpty()) ? Double.parseDouble(row[5].toString())
								: 0.0);
				configurationDTO.setJun(
						(row[6] != null && !row[6].toString().trim().isEmpty()) ? Double.parseDouble(row[6].toString())
								: 0.0);
				configurationDTO.setJul(
						(row[7] != null && !row[7].toString().trim().isEmpty()) ? Double.parseDouble(row[7].toString())
								: 0.0);
				configurationDTO.setAug(
						(row[8] != null && !row[8].toString().trim().isEmpty()) ? Double.parseDouble(row[8].toString())
								: 0.0);
				configurationDTO.setSep(
						(row[9] != null && !row[9].toString().trim().isEmpty()) ? Double.parseDouble(row[9].toString())
								: 0.0);
				configurationDTO.setOct((row[10] != null && !row[10].toString().trim().isEmpty())
						? Double.parseDouble(row[10].toString())
						: 0.0);
				configurationDTO.setNov((row[11] != null && !row[11].toString().trim().isEmpty())
						? Double.parseDouble(row[11].toString())
						: 0.0);
				configurationDTO.setDec((row[12] != null && !row[12].toString().trim().isEmpty())
						? Double.parseDouble(row[12].toString())
						: 0.0);
				configurationDTO.setRemarks((row[13] != null ? row[13].toString() : ""));

				// if (verticalName.equalsIgnoreCase("PE") || verticalName.equalsIgnoreCase("PP") || verticalName.equalsIgnoreCase("PET") || verticalName.equalsIgnoreCase("PTA") || (verticalName.equalsIgnoreCase("VCM")) || (verticalName.equalsIgnoreCase("AROMATICS")) || (verticalName.equalsIgnoreCase("ELASTOMER")) || pvc) {
					configurationDTO.setId(row[14] != null ? row[14].toString() : i + "#");

					configurationDTO.setAuditYear(row[15] != null ? row[15].toString() : "");
					configurationDTO.setUOM(row[16] != null ? row[16].toString() : "");

					configurationDTO.setConfigTypeDisplayName(row[17] != null ? row[17].toString() : "");
					configurationDTO.setTypeDisplayName(row[18] != null ? row[18].toString() : "");
					// configurationDTO.setConfigTypeName(row[19] != null ? row[19].toString() : "");
					// configurationDTO.setTypeName(row[20] != null ? row[20].toString() : "");
					configurationDTO.setProductName(row[19] != null ? row[19].toString() : "");
					configurationDTO.setProductDisplayOrder(row[20] != null ? row[20].toString() : "");

				// }
				/*
				 * if(verticalName.equalsIgnoreCase("AROMATICS")) {
				 * configurationDTO.setVersion(row[22] != null ? row[22].toString() : ""); }
				 */

				// if (verticalName.equalsIgnoreCase("MEG")
				// 		|| verticalName.equalsIgnoreCase("CRACKER")) {

				// 	configurationDTO.setAuditYear(row[14] != null ? row[14].toString() : "");
				// 	configurationDTO.setUOM(row[15] != null ? row[15].toString() : "");
				// 	configurationDTO.setNormType(row[16] != null ? row[16].toString() : "");
				// 	configurationDTO.setIsEditable(row[17] != null ? ((Boolean) row[17]).booleanValue() : null);
				// 	configurationDTO.setProductName(row[18] != null ? row[18].toString() : "");
				// }
				configurationDTOList.add(configurationDTO);
				if (row[14] == null) {
					i++;
				}

			}
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(configurationDTOList);
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
			// String sql = "SELECT " + "    NP.NormParameter_FK_Id AS NormParameter_FK_Id, "
			// 		+ "    MAX(CASE WHEN NAT.AOPMonth = '1' THEN NAT.AttributeValue ELSE NULL END) AS Jan, "
			// 		+ "    MAX(CASE WHEN NAT.AOPMonth = '2' THEN NAT.AttributeValue ELSE NULL END) AS Feb, "
			// 		+ "    MAX(CASE WHEN NAT.AOPMonth = '3' THEN NAT.AttributeValue ELSE NULL END) AS Mar, "
			// 		+ "    MAX(CASE WHEN NAT.AOPMonth = '4' THEN NAT.AttributeValue ELSE NULL END) AS Apr, "
			// 		+ "    MAX(CASE WHEN NAT.AOPMonth = '5' THEN NAT.AttributeValue ELSE NULL END) AS May, "
			// 		+ "    MAX(CASE WHEN NAT.AOPMonth = '6' THEN NAT.AttributeValue ELSE NULL END) AS Jun, "
			// 		+ "    MAX(CASE WHEN NAT.AOPMonth = '7' THEN NAT.AttributeValue ELSE NULL END) AS Jul, "
			// 		+ "    MAX(CASE WHEN NAT.AOPMonth = '8' THEN NAT.AttributeValue ELSE NULL END) AS Aug, "
			// 		+ "    MAX(CASE WHEN NAT.AOPMonth = '9' THEN NAT.AttributeValue ELSE NULL END) AS Sep, "
			// 		+ "    MAX(CASE WHEN NAT.AOPMonth = '10' THEN NAT.AttributeValue ELSE NULL END) AS Oct, "
			// 		+ "    MAX(CASE WHEN NAT.AOPMonth = '11' THEN NAT.AttributeValue ELSE NULL END) AS Nov, "
			// 		+ "    MAX(CASE WHEN NAT.AOPMonth = '12' THEN NAT.AttributeValue ELSE NULL END) AS Dec, "
			// 		+ "    MAX(NAT.Remarks) AS Remarks, " + "    MAX(NAT.Id) AS NormAttributeTransaction_Id, "
			// 		+ "    MAX(NAT.AuditYear) AS AuditYear, " + "    MAX(NP.UOM) AS UOM, "
			// 		+ "    NP.ConfigTypeDisplayName AS ConfigTypeDisplayName, "
			// 		+ "    NP.TypeDisplayName AS TypeDisplayName, " + "    NP.ConfigTypeName AS ConfigTypeName, "
			// 		+ "    NP.TypeName AS TypeName, MAX(NP.DisplayName) " + "FROM " + viewName + " NP "
			// 		+ "JOIN NormParameterType NPT ON NP.NormParameterType_FK_Id = NPT.Id "
			// 		+ "LEFT JOIN NormAttributeTransactions NAT ON NAT.NormParameter_FK_Id = NP.NormParameter_FK_Id "
			// 		+ "    AND NAT.AuditYear = :year " + "WHERE (NPT.Name = 'Configuration'  OR NPT.Name = 'Constant') "
			// 		+ "  AND NP.Plant_FK_Id = :plantFKId " + "GROUP BY " + "    NP.NormParameter_FK_Id, "
			// 		+ "    NP.TypeDisplayName, " + "    NP.TypeDisplayOrder, " + "    NP.ConfigTypeDisplayName, "
			// 		+ "    NP.ConfigTypeName, " + "    NP.TypeName, " + "    NP.DisplayOrder "
			// 		+ "ORDER BY NP.TypeDisplayOrder, NP.DisplayOrder";

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
	
    // @Override
    // public List<NormBasisDTO> getAllNormBasis(UUID plantId, String aopYear) {
        
    //     List<NormBasisProjection> normBasisProjections = normBasisRepository.getAllNormBasis(plantId, aopYear);
    //     List<NormBasisDTO> normBasisDTOs = normBasisProjections.stream()
    //         .map(this::fromProjection)
    //         .collect(Collectors.toList());

    //          String endYear = String.valueOf(Integer.parseInt(aopYear.substring(0, 4))  +1 );
    //             String normCycleStarts = endYear + "-" + "04" + "-" + "01"; 
                
    //         String normsPreparationTime = null;


    //             for(NormBasisDTO normBasisDTO : normBasisDTOs) {  

    //                  if( !normBasisDTO.getType().equals("date"))  continue;

    //                 if(normBasisDTO.getName().equals("Norms Preparation Time")) {  

    //                     normsPreparationTime = normBasisDTO.getAttributeValue();
    //                 }
    //             }

    //         for(NormBasisDTO normBasisDTO : normBasisDTOs) {  

    //             if( !normBasisDTO.getType().equals("date"))  continue;

    //             if (normBasisDTO.getName().equals("Norms Cycle Start")) {
   
    //             // set the attribute value to 1st april of end year
    //             normBasisDTO.setAttributeValue(normCycleStarts);  
            
    //         } 

    //         if(normBasisDTO.getName().equals("Days remaining time from norms preparation time to AOP next cycle start")) {   
   
    //              // calculate the days betweeen normsPreparationTime and normCycleStarts
    //              LocalDate normsPreparationTimeDate = LocalDate.parse(normsPreparationTime);
    //              LocalDate normCycleStartsDate = LocalDate.parse(normCycleStarts);
    //              long daysBetween = ChronoUnit.DAYS.between(normsPreparationTimeDate, normCycleStartsDate);
    //              normBasisDTO.setAttributeValue(String.valueOf(daysBetween));

    //         }
            
    //         }

    //         return normBasisDTOs;
    // }

    // private NormBasisDTO fromProjection(NormBasisProjection projection) {
    //     return NormBasisDTO.builder()
    //         .id(UUID.fromString(projection.getId()))
    //         .name(projection.getName())
    //         .displayName(projection.getDisplayName())
    //         .uom(projection.getUOM())
    //         .attributeValue(projection.getAttributeValue())
    //         .remarks(projection.getRemarks())
    //         .type(projection.getType())
    //         .normParameterType(projection.getNormParameterType())
    //         .displayOrder(projection.getDisplayOrder())
    //         .config(projection.getConfig())
    //         .build();
    // }


    // @Override
    // public void updateNormBasis(List<NormBasisDTO> normBasisDTOs, UUID plantId, String aopYear, UUID siteid, String periodFrom, String periodTo) {
       
    //     List<Object[]> updates = new ArrayList<>();

    //     for(NormBasisDTO normBasisDTO : normBasisDTOs) {
    //         updates.add(new Object[]{normBasisDTO.getAttributeValue(), normBasisDTO.getRemarks(), normBasisDTO.getId()});
    //     }

    //     if(updates.size() > 0) {
    //         String sql = "update NormAttributeTransactions set AttributeValue = ?, Remarks = ? where Id = ?";
    //         jdbcTemplate.batchUpdate(sql, updates);
    //     }

    //     normBasisRepository.normCalculation(plantId, aopYear, siteid, periodFrom, periodTo);
  

    // }

}
