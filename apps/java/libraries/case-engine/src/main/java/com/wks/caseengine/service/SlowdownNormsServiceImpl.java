package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.SlowdownNormsValueDTO;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ShutdownNormsValue;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.SlowdownNormsRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@Service
public class SlowdownNormsServiceImpl implements SlowdownNormsService {

	@Autowired
	private SlowdownNormsRepository slowdownNormsRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@Override
	public List<SlowdownNormsValueDTO> getSlowdownNormsData(String year, String plantId) {
		try {
			List<Object[]> objList = null;
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			if (vertical.getName().equalsIgnoreCase("PE")) {
				objList = getSlowdownNorms(year, plant.getId(), "vwScrnPESlowdownNorms");
			} else if (vertical.getName().equalsIgnoreCase("MEG")) {
				objList = getSlowdownNorms(year, plant.getId(), "vwScrnSlowdownNorms");
			}
			
			List<SlowdownNormsValueDTO> slowdownNormsValueDTOList = new ArrayList<>();
			for (Object[] row : objList) {
				SlowdownNormsValueDTO slowdownNormsValueDTO = new SlowdownNormsValueDTO();
				slowdownNormsValueDTO.setId(row[0] != null ? row[0].toString() : null);
				slowdownNormsValueDTO.setSiteFkId(row[1] != null ? row[1].toString() : null);
				slowdownNormsValueDTO.setPlantFkId(row[2] != null ? row[2].toString() : null);
				slowdownNormsValueDTO.setVerticalFkId(row[3] != null ? row[3].toString() : null);
				slowdownNormsValueDTO.setMaterialFkId(row[4] != null ? row[4].toString() : null);
				slowdownNormsValueDTO.setApril(row[5] != null ? Float.parseFloat(row[5].toString()) : null);
				slowdownNormsValueDTO.setMay(row[6] != null ? Float.parseFloat(row[6].toString()) : null);
				slowdownNormsValueDTO.setJune(row[7] != null ? Float.parseFloat(row[7].toString()) : null);
				slowdownNormsValueDTO.setJuly(row[8] != null ? Float.parseFloat(row[8].toString()) : null);
				slowdownNormsValueDTO.setAugust(row[9] != null ? Float.parseFloat(row[9].toString()) : null);
				slowdownNormsValueDTO.setSeptember(row[10] != null ? Float.parseFloat(row[10].toString()) : null);
				slowdownNormsValueDTO.setOctober(row[11] != null ? Float.parseFloat(row[11].toString()) : null);
				slowdownNormsValueDTO.setNovember(row[12] != null ? Float.parseFloat(row[12].toString()) : null);
				slowdownNormsValueDTO.setDecember(row[13] != null ? Float.parseFloat(row[13].toString()) : null);
				slowdownNormsValueDTO.setJanuary(row[14] != null ? Float.parseFloat(row[14].toString()) : null);
				slowdownNormsValueDTO.setFebruary(row[15] != null ? Float.parseFloat(row[15].toString()) : null);
				slowdownNormsValueDTO.setMarch(row[16] != null ? Float.parseFloat(row[16].toString()) : null);
				slowdownNormsValueDTO.setFinancialYear(row[17] != null ? row[17].toString() : null);
				slowdownNormsValueDTO.setRemarks(row[18] != null ? row[18].toString() : " ");
				slowdownNormsValueDTO.setCreatedOn(row[19] != null ? (Date) row[19] : null);
				slowdownNormsValueDTO.setModifiedOn(row[20] != null ? (Date) row[20] : null);
				slowdownNormsValueDTO.setMcuVersion(row[21] != null ? row[21].toString() : null);
				slowdownNormsValueDTO.setUpdatedBy(row[22] != null ? row[22].toString() : null);
				slowdownNormsValueDTO.setNormParameterTypeId(row[23] != null ? row[23].toString() : null);
				slowdownNormsValueDTO.setNormParameterTypeName(row[24] != null ? row[24].toString() : null);
				slowdownNormsValueDTO.setNormParameterTypeDisplayName(row[25] != null ? row[25].toString() : null);
				slowdownNormsValueDTO.setUOM(row[28] != null ? row[28].toString() : null);
				slowdownNormsValueDTO.setIsEditable(row[29] != null ? Boolean.valueOf(row[29].toString()) : null);
				slowdownNormsValueDTOList.add(slowdownNormsValueDTO);
			}

			return slowdownNormsValueDTOList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public List<SlowdownNormsValueDTO> saveSlowdownNormsData(List<SlowdownNormsValueDTO> slowdownNormsValueDTOList) {
		try {
			for (SlowdownNormsValueDTO slowdownNormsValueDTO : slowdownNormsValueDTOList) {
				ShutdownNormsValue shutdownNormsValue = new ShutdownNormsValue();
				if (slowdownNormsValueDTO.getId() != null && !slowdownNormsValueDTO.getId().isEmpty()) {
					shutdownNormsValue.setId(UUID.fromString(slowdownNormsValueDTO.getId()));
					shutdownNormsValue.setModifiedOn(new Date());
				} else {
					UUID plantId = null;
					UUID siteId = null;
					UUID verticalId = null;
					UUID materialId = null;
					if (slowdownNormsValueDTO.getSiteFkId() != null) {
						siteId = UUID.fromString(slowdownNormsValueDTO.getSiteFkId());
					}
					if (slowdownNormsValueDTO.getPlantFkId() != null) {
						plantId = UUID.fromString(slowdownNormsValueDTO.getPlantFkId());
					}
					if (slowdownNormsValueDTO.getVerticalFkId() != null) {
						verticalId = UUID.fromString(slowdownNormsValueDTO.getVerticalFkId());
					}
					if (slowdownNormsValueDTO.getMaterialFkId() != null) {
						materialId = UUID.fromString(slowdownNormsValueDTO.getMaterialFkId());
					}
					UUID Id = slowdownNormsRepository.findIdByFilters(plantId, siteId, verticalId, materialId,
							slowdownNormsValueDTO.getFinancialYear());
					if (Id != null) {
						shutdownNormsValue.setId(Id);
					}

					shutdownNormsValue.setCreatedOn(new Date());
				}
				shutdownNormsValue.setApril(Optional.ofNullable(slowdownNormsValueDTO.getApril()).orElse(0.0f));
				shutdownNormsValue.setMay(Optional.ofNullable(slowdownNormsValueDTO.getMay()).orElse(0.0f));
				shutdownNormsValue.setJune(Optional.ofNullable(slowdownNormsValueDTO.getJune()).orElse(0.0f));
				shutdownNormsValue.setJuly(Optional.ofNullable(slowdownNormsValueDTO.getJuly()).orElse(0.0f));
				shutdownNormsValue.setAugust(Optional.ofNullable(slowdownNormsValueDTO.getAugust()).orElse(0.0f));
				shutdownNormsValue.setSeptember(Optional.ofNullable(slowdownNormsValueDTO.getSeptember()).orElse(0.0f));
				shutdownNormsValue.setOctober(Optional.ofNullable(slowdownNormsValueDTO.getOctober()).orElse(0.0f));
				shutdownNormsValue.setNovember(Optional.ofNullable(slowdownNormsValueDTO.getNovember()).orElse(0.0f));
				shutdownNormsValue.setDecember(Optional.ofNullable(slowdownNormsValueDTO.getDecember()).orElse(0.0f));
				shutdownNormsValue.setJanuary(Optional.ofNullable(slowdownNormsValueDTO.getJanuary()).orElse(0.0f));
				shutdownNormsValue.setFebruary(Optional.ofNullable(slowdownNormsValueDTO.getFebruary()).orElse(0.0f));
				shutdownNormsValue.setMarch(Optional.ofNullable(slowdownNormsValueDTO.getMarch()).orElse(0.0f));
				if (slowdownNormsValueDTO.getSiteFkId() != null) {
					shutdownNormsValue.setSiteFkId(UUID.fromString(slowdownNormsValueDTO.getSiteFkId()));
				}
				if (slowdownNormsValueDTO.getPlantFkId() != null) {
					shutdownNormsValue.setPlantFkId(UUID.fromString(slowdownNormsValueDTO.getPlantFkId()));
				}
				if (slowdownNormsValueDTO.getVerticalFkId() != null) {
					shutdownNormsValue.setVerticalFkId(UUID.fromString(slowdownNormsValueDTO.getVerticalFkId()));
				}
				if (slowdownNormsValueDTO.getMaterialFkId() != null) {
					shutdownNormsValue.setMaterialFkId(UUID.fromString(slowdownNormsValueDTO.getMaterialFkId()));
				}
				if (slowdownNormsValueDTO.getNormParameterTypeId() != null) {
					shutdownNormsValue
							.setNormParameterTypeFkId(UUID.fromString(slowdownNormsValueDTO.getNormParameterTypeId()));
				}

				shutdownNormsValue.setFinancialYear(slowdownNormsValueDTO.getFinancialYear());
				shutdownNormsValue.setRemarks(slowdownNormsValueDTO.getRemarks());
				shutdownNormsValue.setMcuVersion("V1");
				shutdownNormsValue.setUpdatedBy("System");

				System.out.println("Data Saved Succussfully");
				slowdownNormsRepository.save(shutdownNormsValue);
			}
			// TODO Auto-generated method stub
			return slowdownNormsValueDTOList;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	@Transactional
	public List<SlowdownNormsValueDTO> getSlowdownNormsSPData(String year, String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_HMD_CalculateShutdownNorms";
			List<Object[]> list = getCalculatedSlowdownNormsSP(storedProcedure, year, plant.getId().toString(),
					site.getId().toString(), vertical.getId().toString());
			List<SlowdownNormsValueDTO> slowdownNormsValueDTOList = new ArrayList<>();
			for (Object[] row : list) {
				SlowdownNormsValueDTO slowdownNormsValueDTO = new SlowdownNormsValueDTO();
				slowdownNormsValueDTO.setNormParameterTypeDisplayName(row[0] != null ? row[0].toString() : null);
				slowdownNormsValueDTO.setUOM(row[1] != null ? row[1].toString() : null);
				slowdownNormsValueDTO.setSiteFkId(row[2] != null ? row[2].toString() : null);
				slowdownNormsValueDTO.setVerticalFkId(row[3] != null ? row[3].toString() : null);
				slowdownNormsValueDTO.setAOPCaseId(row[4] != null ? row[4].toString() : null);
				slowdownNormsValueDTO.setAOPStatus(row[5] != null ? row[5].toString() : null);
				slowdownNormsValueDTO.setRemarks(row[6] != null ? row[6].toString() : "");
				slowdownNormsValueDTO.setMaterialFkId(row[7] != null ? row[7].toString() : null);
				slowdownNormsValueDTO.setJanuary(row[8] != null ? Float.parseFloat(row[8].toString()) : null);
				slowdownNormsValueDTO.setFebruary(row[9] != null ? Float.parseFloat(row[9].toString()) : null);
				slowdownNormsValueDTO.setMarch(row[10] != null ? Float.parseFloat(row[10].toString()) : null);
				slowdownNormsValueDTO.setApril(row[11] != null ? Float.parseFloat(row[11].toString()) : null);
				slowdownNormsValueDTO.setMay(row[12] != null ? Float.parseFloat(row[12].toString()) : null);
				slowdownNormsValueDTO.setJune(row[13] != null ? Float.parseFloat(row[13].toString()) : null);
				slowdownNormsValueDTO.setJuly(row[14] != null ? Float.parseFloat(row[14].toString()) : null);
				slowdownNormsValueDTO.setAugust(row[15] != null ? Float.parseFloat(row[15].toString()) : null);
				slowdownNormsValueDTO.setSeptember(row[16] != null ? Float.parseFloat(row[16].toString()) : null);
				slowdownNormsValueDTO.setOctober(row[17] != null ? Float.parseFloat(row[17].toString()) : null);
				slowdownNormsValueDTO.setNovember(row[18] != null ? Float.parseFloat(row[18].toString()) : null);
				slowdownNormsValueDTO.setDecember(row[19] != null ? Float.parseFloat(row[19].toString()) : null);
				slowdownNormsValueDTO.setFinancialYear(row[20] != null ? row[20].toString() : null);
				slowdownNormsValueDTO.setPlantFkId(row[21] != null ? row[21].toString() : null);
				slowdownNormsValueDTOList.add(slowdownNormsValueDTO);
			}

			return slowdownNormsValueDTOList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional
	public List<Object[]> getCalculatedSlowdownNormsSP(String procedureName, String finYear, String plantId,
			String siteId, String verticalId) {
		try {
			// Create a native query to execute the stored procedure
			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @finYear = :finYear";

			Query query = entityManager.createNativeQuery(sql);

			// Set parameters
			query.setParameter("plantId", plantId);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("finYear", finYear);

			return query.getResultList(); // Fetch results instead of executing an update
		} catch (Exception e) {
			e.printStackTrace(); // Log detailed exception for debugging
			return Collections.emptyList(); // Return an empty list instead of 0
		}
	}

	public List<Object[]> getSlowdownNorms(String year, UUID plantId, String viewName) {
		try {
			String sql = "SELECT TOP (1000) [Id], [Site_FK_Id], [Plant_FK_Id], [Vertical_FK_Id], "
					+ "[Material_FK_Id], [April], [May], [June], [July], [August], [September], "
					+ "[October], [November], [December], [January], [February], [March], "
					+ "[FinancialYear], [Remarks], [CreatedOn], [ModifiedOn], [MCUVersion], "
					+ "[UpdatedBy], [NormParameterTypeId], [NormParameterTypeName], "
					+ "[NormParameterTypeDisplayName], [NormTypeDisplayOrder], [MaterialDisplayOrder], [UOM],[isEditable] "
					+ "FROM " + viewName + " "
					+ "WHERE Plant_FK_Id = :plantId AND (FinancialYear = :year OR FinancialYear IS NULL) "
					+ "ORDER BY NormTypeDisplayOrder";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("year", year);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	@Override
	@Transactional
     public List getSlowdownMonths(UUID plantId,String maintenanceName){
		try {
			return	slowdownNormsRepository.getSlowdownMonths(plantId,maintenanceName);
		}catch(Exception e) {
			e.printStackTrace();		
		}  
		return null;
	  }

}
