package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AOPDTO;
import com.wks.caseengine.entity.AOP;
import com.wks.caseengine.repository.AOPRepository;

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
			aOPDTO.setId(aOP.getId());
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
			aOPDTO.setPlantFkId(aOP.getPlantFkId());
			aOPDTO.setSep(aOP.getSep());
			aOPList.add(aOPDTO);
		}
		
		// TODO Auto-generated method stub
		return aOPList;
	}

	@Override
	public AOPDTO updateAOP(AOPDTO aOPDTO) {
		AOP aOP=aOPRepository.findById(aOPDTO.getId()).get();
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
		aOP.setPlantFkId(aOPDTO.getPlantFkId());
		aOP.setSep(aOPDTO.getSep());
		aOPRepository.save(aOP);
		// TODO Auto-generated method stub
		return aOPDTO;
	}

}
