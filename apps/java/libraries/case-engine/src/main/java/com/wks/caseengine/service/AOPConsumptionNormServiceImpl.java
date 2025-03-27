package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AOPConsumptionNormDTO;
import com.wks.caseengine.entity.AOPConsumptionNorm;
import com.wks.caseengine.repository.AOPConsumptionNormRepository;


@Service
public class AOPConsumptionNormServiceImpl implements AOPConsumptionNormService {
	
	@Autowired
	private AOPConsumptionNormRepository aOPConsumptionNormRepository;

	@Override
	public List<AOPConsumptionNormDTO> getAOPConsumptionNorm(String plantId, String year) {
		List<AOPConsumptionNorm> aOPConsumptionNormList =aOPConsumptionNormRepository.findByPlantFkIdAndAopYear(UUID.fromString(plantId),year);
		List<AOPConsumptionNormDTO>  aOPConsumptionNormDTOList=new ArrayList<>();
		for(AOPConsumptionNorm aOPConsumptionNorm:aOPConsumptionNormList) {
			AOPConsumptionNormDTO aOPConsumptionNormDTO=new AOPConsumptionNormDTO();
			aOPConsumptionNormDTO.setAopCaseId(aOPConsumptionNorm.getAopCaseId());
			aOPConsumptionNormDTO.setAopRemarks(aOPConsumptionNorm.getAopRemarks());
			aOPConsumptionNormDTO.setAopStatus(aOPConsumptionNorm.getAopStatus());
			aOPConsumptionNormDTO.setAopYear(aOPConsumptionNorm.getAopYear());
			 aOPConsumptionNormDTO.setJan(aOPConsumptionNorm.getJan());
		        aOPConsumptionNormDTO.setFeb(aOPConsumptionNorm.getFeb());
		        aOPConsumptionNormDTO.setMarch(aOPConsumptionNorm.getMarch());
		        aOPConsumptionNormDTO.setApril(aOPConsumptionNorm.getApril());
		        aOPConsumptionNormDTO.setMay(aOPConsumptionNorm.getMay());
		        aOPConsumptionNormDTO.setJune(aOPConsumptionNorm.getJune());
		        aOPConsumptionNormDTO.setJuly(aOPConsumptionNorm.getJuly());
		        aOPConsumptionNormDTO.setAug(aOPConsumptionNorm.getAug());
		        aOPConsumptionNormDTO.setSep(aOPConsumptionNorm.getSep());
		        aOPConsumptionNormDTO.setOct(aOPConsumptionNorm.getOct());
		        aOPConsumptionNormDTO.setNov(aOPConsumptionNorm.getNov());
		        aOPConsumptionNormDTO.setDec(aOPConsumptionNorm.getDec());
		        aOPConsumptionNormDTO.setId(aOPConsumptionNorm.getId().toString());
		        aOPConsumptionNormDTO.setSiteFkId(aOPConsumptionNorm.getSiteFkId().toString());
		        aOPConsumptionNormDTO.setVerticalFkId(aOPConsumptionNorm.getVerticalFkId().toString());
		        aOPConsumptionNormDTO.setMaterialFkId(aOPConsumptionNorm.getMaterialFkId().toString());
		        aOPConsumptionNormDTO.setPlantFkId(aOPConsumptionNorm.getPlantFkId().toString());
		        aOPConsumptionNormDTOList.add(aOPConsumptionNormDTO);
		}
		return aOPConsumptionNormDTOList;
		
	}

}
