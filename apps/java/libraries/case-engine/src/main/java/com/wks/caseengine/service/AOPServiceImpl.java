package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AOPDTO;
import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;
import com.wks.caseengine.entity.AOP;
import com.wks.caseengine.entity.AOPMCCalculatedData;
import com.wks.caseengine.repository.AOPRepository;

import java.util.UUID;

@Service
public class AOPServiceImpl implements  AOPService{
	
	@Autowired
	private AOPRepository aOPRepository;

	@Override
	public List<AOPDTO> getAOP() {
		
		List<AOP> listAOP=aOPRepository.findAll();
		List<AOPDTO> aOPList=new ArrayList<>();
		
		for(AOP aOP: listAOP) {
			AOPDTO aOPDTO = new AOPDTO();
			aOPDTO.setId(aOP.getId().toString());
			aOPDTO.setAopCaseId(aOP.getAopCaseId());
			aOPDTO.setAopRemarks(aOP.getAopRemarks());
			aOPDTO.setAopStatus(aOP.getAopStatus());
			aOPDTO.setAopType(aOP.getAopType());
			aOPDTO.setAopYear(aOP.getAopYear());
			aOPDTO.setApril(aOP.getApril());
			aOPDTO.setAug(aOP.getAug());
			aOPDTO.setAvgTPH(aOP.getAvgTPH());
			aOPDTO.setDec(aOP.getDec());
			aOPDTO.setFeb(aOP.getFeb());
			aOPDTO.setJan(aOP.getJan());
			aOPDTO.setJuly(aOP.getJuly());
			aOPDTO.setJune(aOP.getJune());
			aOPDTO.setMarch(aOP.getMarch());
			aOPDTO.setMay(aOP.getMay());
			aOPDTO.setNormItem(aOP.getNormItem());
			aOPDTO.setNov(aOP.getNov());
			aOPDTO.setOct(aOP.getOct());
			aOPDTO.setPlantFkId(aOP.getPlantFkId().toString());
			aOPDTO.setSep(aOP.getSep());
			aOPList.add(aOPDTO);
		}
		
		// TODO Auto-generated method stub
		return aOPList;
	}

	@Override
	public List<AOPDTO> getAOPData(String plantId, String year) {
	    List<AOPDTO> aOPDTOList = new ArrayList<>();
	    List<Object[]> objList = aOPRepository.findBusinessDemandWithAOP(UUID.fromString(plantId), year);

	    for (Object[] obj : objList) {
	        AOP aOPData = (AOP) obj[0]; // First element (AOPMCCalculatedData)
	        UUID bdNormParametersFKId = obj[1] != null ? UUID.fromString(obj[1].toString()) : null; // Second element (BDNormParametersFKId)

	        AOPDTO aOPDTO = new AOPDTO();
	        aOPDTO.setId(aOPData.getId().toString());
			aOPDTO.setAopCaseId(aOPData.getAopCaseId());
			aOPDTO.setAopRemarks(aOPData.getAopRemarks());
			aOPDTO.setAopStatus(aOPData.getAopStatus());
			aOPDTO.setAopType(aOPData.getAopType());
			aOPDTO.setAopYear(aOPData.getAopYear());
			aOPDTO.setApril(aOPData.getApril());
			aOPDTO.setAug(aOPData.getAug());
			aOPDTO.setAvgTPH(aOPData.getAvgTPH());
			aOPDTO.setDec(aOPData.getDec());
			aOPDTO.setFeb(aOPData.getFeb());
			aOPDTO.setJan(aOPData.getJan());
			aOPDTO.setJuly(aOPData.getJuly());
			aOPDTO.setJune(aOPData.getJune());
			aOPDTO.setMarch(aOPData.getMarch());
			aOPDTO.setMay(aOPData.getMay());
			aOPDTO.setNormItem(aOPData.getNormItem());
			aOPDTO.setNov(aOPData.getNov());
			aOPDTO.setOct(aOPData.getOct());
			aOPDTO.setPlantFkId(aOPData.getPlantFkId().toString());
			aOPDTO.setSep(aOPData.getSep());
	        // Set BDNormParametersFKId in DTO
			aOPDTO.setBDNormParametersFKId(bdNormParametersFKId.toString());
			aOPDTOList.add(aOPDTO);
	    }
	    return aOPDTOList;
	}

	@Override
	public List<AOPDTO> updateAOP(List<AOPDTO> aOPDTOList) {
		for(AOPDTO aOPDTO:aOPDTOList) {
			AOP aOP=aOPRepository.findById(UUID.fromString(aOPDTO.getId())).get();
			aOP.setAopCaseId(aOPDTO.getAopCaseId());
			aOP.setAopRemarks(aOPDTO.getAopRemarks());
			aOP.setAopStatus(aOPDTO.getAopStatus());
			aOP.setAopType(aOPDTO.getAopType());
			aOP.setAopYear(aOPDTO.getAopYear());
			aOP.setApril(aOPDTO.getApril());
			aOP.setAug(aOPDTO.getAug());
			aOP.setAvgTPH(aOPDTO.getAvgTPH());
			aOP.setDec(aOPDTO.getDec());
			aOP.setFeb(aOPDTO.getFeb());
			aOP.setJan(aOPDTO.getJan());
			aOP.setJuly(aOPDTO.getJuly());
			aOP.setJune(aOPDTO.getJune());
			aOP.setMarch(aOPDTO.getMarch());
			aOP.setMay(aOPDTO.getMay());
			aOP.setNormItem(aOPDTO.getNormItem());
			aOP.setNov(aOPDTO.getNov());
			aOP.setOct(aOPDTO.getOct());
			aOP.setPlantFkId(UUID.fromString(aOPDTO.getPlantFkId()));
			aOP.setSep(aOPDTO.getSep());
			aOPRepository.save(aOP);
		}
		return aOPDTOList;
	}

}
