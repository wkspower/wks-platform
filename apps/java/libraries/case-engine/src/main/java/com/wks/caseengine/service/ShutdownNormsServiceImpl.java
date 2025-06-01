package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.ShutdownNormsValueDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.MCUNormsValue;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.ShutdownNormsValue;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.ShutdownNormsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@Service
public class ShutdownNormsServiceImpl implements ShutdownNormsService {

	@Autowired
	private ShutdownNormsRepository shutdownNormsRepository;

	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	PlantsRepository plantsRepository;
	@Autowired
	SiteRepository siteRepository;
	@Autowired
	VerticalsRepository verticalRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	


	@Override
	public List<ShutdownNormsValueDTO> getShutdownNormsData(String year, String plantId) {
		try {
			List<Object[]> objList = null;
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			// Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			if (vertical.getName().equalsIgnoreCase("MEG")) {
				objList = getShutdownNorms(year, plant.getId(), "vwScrnShutdownNorms");
			}else {
				String viewName="vwScrn"+vertical.getName()+"ShutdownNorms";
				objList = getShutdownNorms(year, plant.getId(), viewName);
			} 
			// List<Object[]> objList = shutdownNormsRepository.findByYearAndPlantFkId(year,
			// UUID.fromString(plantId));
			System.out.println("obj.size(): " + objList.size());
			List<ShutdownNormsValueDTO> shutdownNormsValueDTOList = new ArrayList<>();
			for (Object[] row : objList) {
				ShutdownNormsValueDTO shutdownNormsValueDTO = new ShutdownNormsValueDTO();
				shutdownNormsValueDTO.setId(row[0] != null ? row[0].toString() : null);
				shutdownNormsValueDTO.setSiteFkId(row[1] != null ? row[1].toString() : null);
				shutdownNormsValueDTO.setPlantFkId(row[2] != null ? row[2].toString() : null);
				shutdownNormsValueDTO.setVerticalFkId(row[3] != null ? row[3].toString() : null);
				shutdownNormsValueDTO.setMaterialFkId(row[4] != null ? row[4].toString() : null);
				shutdownNormsValueDTO.setApril(row[5] != null ? Double.parseDouble(row[5].toString()) : null);
				shutdownNormsValueDTO.setMay(row[6] != null ? Double.parseDouble(row[6].toString()) : null);
				shutdownNormsValueDTO.setJune(row[7] != null ? Double.parseDouble(row[7].toString()) : null);
				shutdownNormsValueDTO.setJuly(row[8] != null ? Double.parseDouble(row[8].toString()) : null);
				shutdownNormsValueDTO.setAugust(row[9] != null ? Double.parseDouble(row[9].toString()) : null);
				shutdownNormsValueDTO.setSeptember(row[10] != null ? Double.parseDouble(row[10].toString()) : null);
				shutdownNormsValueDTO.setOctober(row[11] != null ? Double.parseDouble(row[11].toString()) : null);
				shutdownNormsValueDTO.setNovember(row[12] != null ? Double.parseDouble(row[12].toString()) : null);
				shutdownNormsValueDTO.setDecember(row[13] != null ? Double.parseDouble(row[13].toString()) : null);
				shutdownNormsValueDTO.setJanuary(row[14] != null ? Double.parseDouble(row[14].toString()) : null);
				shutdownNormsValueDTO.setFebruary(row[15] != null ? Double.parseDouble(row[15].toString()) : null);
				shutdownNormsValueDTO.setMarch(row[16] != null ? Double.parseDouble(row[16].toString()) : null);

				shutdownNormsValueDTO.setFinancialYear(row[17] != null ? row[17].toString() : null);
				shutdownNormsValueDTO.setRemarks(row[18] != null ? row[18].toString() : " ");
				shutdownNormsValueDTO.setCreatedOn(row[19] != null ? (Date) row[19] : null);
				shutdownNormsValueDTO.setModifiedOn(row[20] != null ? (Date) row[20] : null);
				shutdownNormsValueDTO.setMcuVersion(row[21] != null ? row[21].toString() : null);
				shutdownNormsValueDTO.setUpdatedBy(row[22] != null ? row[22].toString() : null);
				shutdownNormsValueDTO.setNormParameterTypeId(row[23] != null ? row[23].toString() : null);
				shutdownNormsValueDTO.setNormParameterTypeName(row[24] != null ? row[24].toString() : null);
				shutdownNormsValueDTO.setNormParameterTypeDisplayName(row[25] != null ? row[25].toString() : null);

				shutdownNormsValueDTO.setUOM(row[28] != null ? row[28].toString() : null);
				shutdownNormsValueDTO.setIsEditable(row[29] != null ? Boolean.valueOf(row[29].toString()) : null);
				shutdownNormsValueDTOList.add(shutdownNormsValueDTO);
			}

			return shutdownNormsValueDTOList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public List<ShutdownNormsValueDTO> saveShutdownNormsData(List<ShutdownNormsValueDTO> shutdownNormsValueDTOList) {
		String year=null;
		UUID plantId=null;
		try {
			for (ShutdownNormsValueDTO shutdownNormsValueDTO : shutdownNormsValueDTOList) {
				year=shutdownNormsValueDTO.getFinancialYear();
				plantId=UUID.fromString(shutdownNormsValueDTO.getPlantFkId());
				ShutdownNormsValue shutdownNormsValue = new ShutdownNormsValue();
				if (shutdownNormsValueDTO.getId() != null && !shutdownNormsValueDTO.getId().isEmpty()) {
					shutdownNormsValue.setId(UUID.fromString(shutdownNormsValueDTO.getId()));
					shutdownNormsValue.setModifiedOn(new Date());
				} else {
					UUID siteId = null;
					UUID verticalId = null;
					UUID materialId = null;
					if (shutdownNormsValueDTO.getSiteFkId() != null) {
						siteId = UUID.fromString(shutdownNormsValueDTO.getSiteFkId());
					}
					if (shutdownNormsValueDTO.getPlantFkId() != null) {
						plantId = UUID.fromString(shutdownNormsValueDTO.getPlantFkId());
					}
					if (shutdownNormsValueDTO.getVerticalFkId() != null) {
						verticalId = UUID.fromString(shutdownNormsValueDTO.getVerticalFkId());
					}
					if (shutdownNormsValueDTO.getMaterialFkId() != null) {
						materialId = UUID.fromString(shutdownNormsValueDTO.getMaterialFkId());
					}
					UUID Id = shutdownNormsRepository.findIdByFilters(plantId, siteId, verticalId, materialId,
							shutdownNormsValueDTO.getFinancialYear());
					if (Id != null) {
						shutdownNormsValue.setId(Id);
					}

					shutdownNormsValue.setCreatedOn(new Date());
				}
				shutdownNormsValue.setApril(Optional.ofNullable(shutdownNormsValueDTO.getApril()).orElse(0.0));
				shutdownNormsValue.setMay(Optional.ofNullable(shutdownNormsValueDTO.getMay()).orElse(0.0));
				shutdownNormsValue.setJune(Optional.ofNullable(shutdownNormsValueDTO.getJune()).orElse(0.0));
				shutdownNormsValue.setJuly(Optional.ofNullable(shutdownNormsValueDTO.getJuly()).orElse(0.0));
				shutdownNormsValue.setAugust(Optional.ofNullable(shutdownNormsValueDTO.getAugust()).orElse(0.0));
				shutdownNormsValue.setSeptember(Optional.ofNullable(shutdownNormsValueDTO.getSeptember()).orElse(0.0));
				shutdownNormsValue.setOctober(Optional.ofNullable(shutdownNormsValueDTO.getOctober()).orElse(0.0));
				shutdownNormsValue.setNovember(Optional.ofNullable(shutdownNormsValueDTO.getNovember()).orElse(0.0));
				shutdownNormsValue.setDecember(Optional.ofNullable(shutdownNormsValueDTO.getDecember()).orElse(0.0));
				shutdownNormsValue.setJanuary(Optional.ofNullable(shutdownNormsValueDTO.getJanuary()).orElse(0.0));
				shutdownNormsValue.setFebruary(Optional.ofNullable(shutdownNormsValueDTO.getFebruary()).orElse(0.0));
				shutdownNormsValue.setMarch(Optional.ofNullable(shutdownNormsValueDTO.getMarch()).orElse(0.0));
				if (shutdownNormsValueDTO.getSiteFkId() != null) {
					shutdownNormsValue.setSiteFkId(UUID.fromString(shutdownNormsValueDTO.getSiteFkId()));
				}
				if (shutdownNormsValueDTO.getPlantFkId() != null) {
					shutdownNormsValue.setPlantFkId(UUID.fromString(shutdownNormsValueDTO.getPlantFkId()));
				}
				if (shutdownNormsValueDTO.getVerticalFkId() != null) {
					shutdownNormsValue.setVerticalFkId(UUID.fromString(shutdownNormsValueDTO.getVerticalFkId()));
				}
				if (shutdownNormsValueDTO.getMaterialFkId() != null) {
					shutdownNormsValue.setMaterialFkId(UUID.fromString(shutdownNormsValueDTO.getMaterialFkId()));
				}
				if (shutdownNormsValueDTO.getNormParameterTypeId() != null) {
					shutdownNormsValue
							.setNormParameterTypeFkId(UUID.fromString(shutdownNormsValueDTO.getNormParameterTypeId()));
				}

				shutdownNormsValue.setFinancialYear(shutdownNormsValueDTO.getFinancialYear());
				shutdownNormsValue.setRemarks(shutdownNormsValueDTO.getRemarks());
				shutdownNormsValue.setMcuVersion("V1");
				shutdownNormsValue.setUpdatedBy("System");

				System.out.println("Data Saved Succussfully");
				shutdownNormsRepository.save(shutdownNormsValue);
			}
			
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("shutdown-norms");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(plantId);
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			// TODO Auto-generated method stub
			return shutdownNormsValueDTOList;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	@Override
	@Transactional
	public List<ShutdownNormsValueDTO> getShutdownNormsSPData(String year, String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_"+site.getName()+"_CalculateShutdownNorms";
			List<Object[]> list = getCalculatedShutdownNormsSP(storedProcedure, year, plant.getId().toString(),
					site.getId().toString(), vertical.getId().toString());
			List<ShutdownNormsValueDTO> shutdownNormsValueDTOList = new ArrayList<>();
			for (Object[] row : list) {
				ShutdownNormsValueDTO shutdownNormsValueDTO = new ShutdownNormsValueDTO();
				shutdownNormsValueDTO.setNormParameterTypeDisplayName(row[0] != null ? row[0].toString() : null);
				shutdownNormsValueDTO.setUOM(row[1] != null ? row[1].toString() : null);
				shutdownNormsValueDTO.setSiteFkId(row[2] != null ? row[2].toString() : null);
				shutdownNormsValueDTO.setVerticalFkId(row[3] != null ? row[3].toString() : null);
				shutdownNormsValueDTO.setAOPCaseId(row[4] != null ? row[4].toString() : null);
				shutdownNormsValueDTO.setAOPStatus(row[5] != null ? row[5].toString() : null);
				shutdownNormsValueDTO.setRemarks(row[6] != null ? row[6].toString() : "");
				shutdownNormsValueDTO.setMaterialFkId(row[7] != null ? row[7].toString() : null);
				shutdownNormsValueDTO.setJanuary(row[8] != null ? Double.parseDouble(row[8].toString()) : null);
				shutdownNormsValueDTO.setFebruary(row[9] != null ? Double.parseDouble(row[9].toString()) : null);
				shutdownNormsValueDTO.setMarch(row[10] != null ? Double.parseDouble(row[10].toString()) : null);
				shutdownNormsValueDTO.setApril(row[11] != null ? Double.parseDouble(row[11].toString()) : null);
				shutdownNormsValueDTO.setMay(row[12] != null ? Double.parseDouble(row[12].toString()) : null);
				shutdownNormsValueDTO.setJune(row[13] != null ? Double.parseDouble(row[13].toString()) : null);
				shutdownNormsValueDTO.setJuly(row[14] != null ? Double.parseDouble(row[14].toString()) : null);
				shutdownNormsValueDTO.setAugust(row[15] != null ? Double.parseDouble(row[15].toString()) : null);
				shutdownNormsValueDTO.setSeptember(row[16] != null ? Double.parseDouble(row[16].toString()) : null);
				shutdownNormsValueDTO.setOctober(row[17] != null ? Double.parseDouble(row[17].toString()) : null);
				shutdownNormsValueDTO.setNovember(row[18] != null ? Double.parseDouble(row[18].toString()) : null);
				shutdownNormsValueDTO.setDecember(row[19] != null ? Double.parseDouble(row[19].toString()) : null);
				shutdownNormsValueDTO.setFinancialYear(row[20] != null ? row[20].toString() : null);
				shutdownNormsValueDTO.setPlantFkId(row[21] != null ? row[21].toString() : null);
				shutdownNormsValueDTOList.add(shutdownNormsValueDTO);
			}

			return shutdownNormsValueDTOList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional
	public List<Object[]> getCalculatedShutdownNormsSP(String procedureName, String finYear, String plantId,
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
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> getShutdownNorms(String year, UUID plantId, String viewName) {
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

}
