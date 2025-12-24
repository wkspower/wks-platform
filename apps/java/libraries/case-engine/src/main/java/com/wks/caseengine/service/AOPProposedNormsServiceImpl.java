package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.repository.AOPConsumptionNormGradeRepository;
import com.wks.caseengine.dto.AOPProposedNormsDTO;
import com.wks.caseengine.entity.AOPConsumptionNormGrade;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AOPProposedNormsGradeWiseRepository;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.UUID;


import jakarta.persistence.Query;

@Service
public class AOPProposedNormsServiceImpl implements AOPProposedNormsService {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;
	
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;

	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private AOPConsumptionNormGradeRepository aopConsumptionNormGradeRepository;

	
	@Override
	public AOPMessageVM getProposedNorms(String year,String plantId,String gradeId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<AOPProposedNormsDTO> aopProposedNormsDTOList = new ArrayList<AOPProposedNormsDTO>();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			List<Object[]> obj=null;
				String procedureName=vertical.getName()+"_"+site.getName()+"_"+"GetAOPProposedNorms";
				obj = getData(year,plantId,gradeId,procedureName);
			
				for (Object[] row : obj) {
				    AOPProposedNormsDTO dto = new AOPProposedNormsDTO();

				    
				    dto.setId(row[0] != null ? row[0].toString() : null);
				    dto.setNormParameterTypeDisplayName(row[1] != null ? row[1].toString() : null);
				    dto.setNormParameterDisplayName(row[2] != null ? row[2].toString() : null);
				    dto.setUOM(row[3] != null ? row[3].toString() : null);

				    
				    dto.setPrevYearBudgetApril(row[4] != null ? Double.valueOf(row[4].toString()) : null);
				    dto.setCurrYearBudgetApril(row[5] != null ? Double.valueOf(row[5].toString()) : null);
				    dto.setCurrYearProposedApril(row[6] != null ? Double.valueOf(row[6].toString()) : null);

				    
				    dto.setPrevYearBudgetMay(row[7] != null ? Double.valueOf(row[7].toString()) : null);
				    dto.setCurrYearBudgetMay(row[8] != null ? Double.valueOf(row[8].toString()) : null);
				    dto.setCurrYearProposedMay(row[9] != null ? Double.valueOf(row[9].toString()) : null);

				   
				    dto.setPrevYearBudgetJune(row[10] != null ? Double.valueOf(row[10].toString()) : null);
				    dto.setCurrYearBudgetJune(row[11] != null ? Double.valueOf(row[11].toString()) : null);
				    dto.setCurrYearProposedJune(row[12] != null ? Double.valueOf(row[12].toString()) : null);

				   
				    dto.setPrevYearBudgetJuly(row[13] != null ? Double.valueOf(row[13].toString()) : null);
				    dto.setCurrYearBudgetJuly(row[14] != null ? Double.valueOf(row[14].toString()) : null);
				    dto.setCurrYearProposedJuly(row[15] != null ? Double.valueOf(row[15].toString()) : null);

				   
				    dto.setPrevYearBudgetAugust(row[16] != null ? Double.valueOf(row[16].toString()) : null);
				    dto.setCurrYearBudgetAugust(row[17] != null ? Double.valueOf(row[17].toString()) : null);
				    dto.setCurrYearProposedAugust(row[18] != null ? Double.valueOf(row[18].toString()) : null);

				    dto.setPrevYearBudgetSeptember(row[19] != null ? Double.valueOf(row[19].toString()) : null);
				    dto.setCurrYearBudgetSeptember(row[20] != null ? Double.valueOf(row[20].toString()) : null);
				    dto.setCurrYearProposedSeptember(row[21] != null ? Double.valueOf(row[21].toString()) : null);

				    
				    dto.setPrevYearBudgetOctober(row[22] != null ? Double.valueOf(row[22].toString()) : null);
				    dto.setCurrYearBudgetOctober(row[23] != null ? Double.valueOf(row[23].toString()) : null);
				    dto.setCurrYearProposedOctober(row[24] != null ? Double.valueOf(row[24].toString()) : null);

				    
				    dto.setPrevYearBudgetNovember(row[25] != null ? Double.valueOf(row[25].toString()) : null);
				    dto.setCurrYearBudgetNovember(row[26] != null ? Double.valueOf(row[26].toString()) : null);
				    dto.setCurrYearProposedNovember(row[27] != null ? Double.valueOf(row[27].toString()) : null);

				    
				    dto.setPrevYearBudgetDecember(row[28] != null ? Double.valueOf(row[28].toString()) : null);
				    dto.setCurrYearBudgetDecember(row[29] != null ? Double.valueOf(row[29].toString()) : null);
				    dto.setCurrYearProposedDecember(row[30] != null ? Double.valueOf(row[30].toString()) : null);

				   
				    dto.setPrevYearBudgetJanuary(row[31] != null ? Double.valueOf(row[31].toString()) : null);
				    dto.setCurrYearBudgetJanuary(row[32] != null ? Double.valueOf(row[32].toString()) : null);
				    dto.setCurrYearProposedJanuary(row[33] != null ? Double.valueOf(row[33].toString()) : null);

				    
				    dto.setPrevYearBudgetFebruary(row[34] != null ? Double.valueOf(row[34].toString()) : null);
				    dto.setCurrYearBudgetFebruary(row[35] != null ? Double.valueOf(row[35].toString()) : null);
				    dto.setCurrYearProposedFebruary(row[36] != null ? Double.valueOf(row[36].toString()) : null);

				    
				    dto.setPrevYearBudgetMarch(row[37] != null ? Double.valueOf(row[37].toString()) : null);
				    dto.setCurrYearBudgetMarch(row[38] != null ? Double.valueOf(row[38].toString()) : null);
				    dto.setCurrYearProposedMarch(row[39] != null ? Double.valueOf(row[39].toString()) : null);

				    dto.setRemarks(row[40] != null ? row[40].toString() : null);
				    dto.setGradeId(row[41] != null ? row[41].toString() : null);
				    dto.setPlantId(row[42] != null ? row[42].toString() : null);
				    dto.setAopYear(row[43] != null ? row[43].toString() : null);
				    dto.setModifiedBy(row[44] != null ? row[44].toString() : null);
				    dto.setModifiedOn(row[45] != null ? (java.util.Date) row[45] : null);

				    if (row[46] != null) {
				        if (row[46] instanceof Boolean) {
				            dto.setIsEditable((Boolean) row[46]);
				        } else if (row[46] instanceof Number) {
				            dto.setIsEditable(((Number) row[46]).intValue() == 1);
				        }
				    } else {
				        dto.setIsEditable(null);
				    }

				    aopProposedNormsDTOList.add(dto);
				}				
				Map<String, Object> map = new HashMap<>();

				List<AopCalculation> aopCalculation = aopCalculationRepository
						.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "proposed-norms");
				map.put("aopProposedNormsDTOList", aopProposedNormsDTOList);
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
	
	public List<Object[]> getData(String aopYear,String plantId,String gradeId, String procedureName) {
		try {

			String sql = "EXEC " + procedureName
					+ " @PlantId = :plantId, @AOPYear = :aopYear, @GradeId = :gradeId";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);
			query.setParameter("gradeId", gradeId);
			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	@Transactional
	public AOPMessageVM updateProposedNorms(String year, String plantId, 
	                                       List<AOPProposedNormsDTO> aopProposedNormsDTOs) {
	    try {
	        for (AOPProposedNormsDTO dto : aopProposedNormsDTOs) {
	            Optional<AOPConsumptionNormGrade> entityOpt = 
	            		aopConsumptionNormGradeRepository.findById(UUID.fromString(dto.getId()));

	            if (entityOpt.isPresent()) {
	            	AOPConsumptionNormGrade entity = entityOpt.get();
	                entity.setAopRemarks(dto.getRemarks());
	                entity.setApril(dto.getCurrYearProposedApril());
	                entity.setMay(dto.getCurrYearProposedMay());
	                entity.setJune(dto.getCurrYearProposedJune());
	                entity.setJuly(dto.getCurrYearProposedJuly());
	                entity.setAug(dto.getCurrYearProposedAugust());
	                entity.setSep(dto.getCurrYearProposedSeptember());
	                entity.setOct(dto.getCurrYearProposedOctober());
	                entity.setNov(dto.getCurrYearProposedNovember());
	                entity.setDec(dto.getCurrYearProposedDecember());
	                entity.setJan(dto.getCurrYearProposedJanuary());
	                entity.setFeb(dto.getCurrYearProposedFebruary());
	                entity.setMarch(dto.getCurrYearProposedMarch());
	                
	                aopConsumptionNormGradeRepository.save(entity);
	            }
	        }
	        List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("normal-op-norms");
			for (ScreenMapping screenMapping : screenMappingList) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
	        AOPMessageVM aopMessageVM = new AOPMessageVM();
	        aopMessageVM.setCode(200);
	        aopMessageVM.setData(null);
	        aopMessageVM.setMessage("Data saved successfully");
	        return aopMessageVM;
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to update proposed norms", ex);
	    }
	}
}
