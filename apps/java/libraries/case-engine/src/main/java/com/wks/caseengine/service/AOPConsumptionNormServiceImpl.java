package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AOPConsumptionNormDTO;
import com.wks.caseengine.entity.AOPConsumptionNorm;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.repository.AOPConsumptionNormRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;


@Service
public class AOPConsumptionNormServiceImpl implements AOPConsumptionNormService {
	
	@Autowired
	private AOPConsumptionNormRepository aOPConsumptionNormRepository;
	
	 @PersistenceContext
	    private EntityManager entityManager;
	 @Autowired
		PlantsRepository plantsRepository;
		@Autowired
		SiteRepository siteRepository;
		@Autowired
		VerticalsRepository verticalRepository;

	@Override
	public List<AOPConsumptionNormDTO> getAOPConsumptionNorm(String plantId, String year) {
	    List<Object[]> resultList = aOPConsumptionNormRepository.findByPlantFkIdAndAopYear(UUID.fromString(plantId), year);
	    List<AOPConsumptionNormDTO> aOPConsumptionNormDTOList = new ArrayList<>();

	    for (Object[] row : resultList) {
	        AOPConsumptionNormDTO dto = new AOPConsumptionNormDTO();
	        
	        dto.setId(row[0] != null ? row[0].toString() : null);
	        dto.setSiteFkId(row[1] != null ? row[1].toString() : null);
	        dto.setVerticalFkId(row[2] != null ? row[2].toString() : null);
	        dto.setAopCaseId(row[3] != null ? row[3].toString() : null);
	        dto.setAopStatus(row[4] != null ? row[4].toString() : null);
	        dto.setAopRemarks(row[5] != null ? row[5].toString() : null);
	        dto.setMaterialFkId(row[6] != null ? row[6].toString() : null);
	        dto.setJan(row[7] != null ? Float.valueOf(row[7].toString()) : null);
	        dto.setFeb(row[8] != null ? Float.valueOf(row[8].toString()) : null);
	        dto.setMarch(row[9] != null ? Float.valueOf(row[9].toString()) : null);
	        dto.setApril(row[10] != null ? Float.valueOf(row[10].toString()) : null);
	        dto.setMay(row[11] != null ? Float.valueOf(row[11].toString()) : null);
	        dto.setJune(row[12] != null ? Float.valueOf(row[12].toString()) : null);
	        dto.setJuly(row[13] != null ? Float.valueOf(row[13].toString()) : null);
	        dto.setAug(row[14] != null ? Float.valueOf(row[14].toString()) : null);
	        dto.setSep(row[15] != null ? Float.valueOf(row[15].toString()) : null);
	        dto.setOct(row[16] != null ? Float.valueOf(row[16].toString()) : null);
	        dto.setNov(row[17] != null ? Float.valueOf(row[17].toString()) : null);
	        dto.setDec(row[18] != null ? Float.valueOf(row[18].toString()) : null);
	        dto.setAopYear(row[19] != null ? row[19].toString() : null);
	        dto.setPlantFkId(row[20] != null ? row[20].toString() : null);
	        dto.setNormParameterTypeDisplayName(row[21] != null ? row[21].toString() : null);
	        dto.setUOM(row[22] != null ? row[22].toString() : null);

	        aOPConsumptionNormDTOList.add(dto);
	    }

	    return aOPConsumptionNormDTOList;
	}

	@Override
	public List<AOPConsumptionNormDTO> saveAOPConsumptionNorm(List<AOPConsumptionNormDTO> aOPConsumptionNormDTOList) {
		
		for(AOPConsumptionNormDTO aOPConsumptionNormDTO:aOPConsumptionNormDTOList) {
			AOPConsumptionNorm aOPConsumptionNorm=new AOPConsumptionNorm();
			aOPConsumptionNorm.setAopCaseId(aOPConsumptionNormDTO.getAopCaseId());
			aOPConsumptionNorm.setAopCaseId(aOPConsumptionNormDTO.getAopCaseId());
			aOPConsumptionNorm.setAopRemarks(aOPConsumptionNormDTO.getAopRemarks());
			aOPConsumptionNorm.setAopStatus(aOPConsumptionNormDTO.getAopStatus());
			aOPConsumptionNorm.setAopYear(aOPConsumptionNormDTO.getAopYear());
			aOPConsumptionNorm.setJan(aOPConsumptionNormDTO.getJan());
			aOPConsumptionNorm.setFeb(aOPConsumptionNormDTO.getFeb());
			aOPConsumptionNorm.setMarch(aOPConsumptionNormDTO.getMarch());
			aOPConsumptionNorm.setApril(aOPConsumptionNormDTO.getApril());
		        aOPConsumptionNorm.setMay(aOPConsumptionNormDTO.getMay());
		        aOPConsumptionNorm.setJune(aOPConsumptionNormDTO.getJune());
		        aOPConsumptionNorm.setJuly(aOPConsumptionNormDTO.getJuly());
		        aOPConsumptionNorm.setAug(aOPConsumptionNormDTO.getAug());
		        aOPConsumptionNorm.setSep(aOPConsumptionNormDTO.getSep());
		        aOPConsumptionNorm.setOct(aOPConsumptionNormDTO.getOct());
		        aOPConsumptionNorm.setNov(aOPConsumptionNormDTO.getNov());
		        aOPConsumptionNorm.setDec(aOPConsumptionNormDTO.getDec());
		        if(aOPConsumptionNormDTO.getId()!=null) {
		        	aOPConsumptionNorm.setId(UUID.fromString(aOPConsumptionNormDTO.getId()));
		        }
		        if(aOPConsumptionNormDTO.getSiteFkId()!=null) {
		        	aOPConsumptionNorm.setSiteFkId(UUID.fromString(aOPConsumptionNormDTO.getSiteFkId()));
		        }
		        if(aOPConsumptionNormDTO.getVerticalFkId()!=null) {
		        	aOPConsumptionNorm.setVerticalFkId(UUID.fromString(aOPConsumptionNormDTO.getVerticalFkId()));
		        }
		        if(aOPConsumptionNormDTO.getMaterialFkId()!=null) {
		        	 aOPConsumptionNorm.setMaterialFkId(UUID.fromString(aOPConsumptionNormDTO.getMaterialFkId()));
		        }
		        if(aOPConsumptionNormDTO.getPlantFkId()!=null) {
		        	aOPConsumptionNorm.setPlantFkId(UUID.fromString(aOPConsumptionNormDTO.getPlantFkId()));
		        }
		        
		        aOPConsumptionNormRepository.save(aOPConsumptionNorm);
		}
		// TODO Auto-generated method stub
		return aOPConsumptionNormDTOList;
	}

	@Override
	public int calculateExpressionConsumptionNorms(String year,String plantId) {
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		String storedProcedure=vertical.getName()+"_HMD_CalculateConsumptionAOPValues";
		return executeDynamicUpdateProcedure(storedProcedure,year);
	}
	
	@Transactional
	public int executeDynamicUpdateProcedure(String procedureName, String finYear) {
	    String sql = "EXEC " + procedureName + " @finYear = :finYear"; // Ensure correct parameter format
	    Query query = entityManager.createNativeQuery(sql);
	    query.setParameter("finYear", finYear);
	    
	    return query.executeUpdate();
	}


}
