package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;
import com.wks.caseengine.entity.AOPMCCalculatedData;
import com.wks.caseengine.repository.AOPMCCalculatedDataRepository;

@Service
public class AOPMCCalculatedDataServiceImpl implements AOPMCCalculatedDataService{
	
	@Autowired
	private AOPMCCalculatedDataRepository aOPMCCalculatedDataRepository;

	@Override
	public List<AOPMCCalculatedDataDTO> getAOPMCCalculatedData(String plantId, String year) {
		
		List<AOPMCCalculatedData> aOPMCCalculatedDataList=aOPMCCalculatedDataRepository.findAllByPlantIdAndYear(UUID.fromString(plantId),year);
		List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList = new ArrayList<>();
		for(AOPMCCalculatedData aOPMCCalculatedData: aOPMCCalculatedDataList) {
			AOPMCCalculatedDataDTO aOPMCCalculatedDataDTO = new AOPMCCalculatedDataDTO();
			aOPMCCalculatedDataDTO.setApril(aOPMCCalculatedData.getApril());
			aOPMCCalculatedDataDTO.setAugust(aOPMCCalculatedData.getAugust());
			aOPMCCalculatedDataDTO.setDecember(aOPMCCalculatedData.getDecember());
			aOPMCCalculatedDataDTO.setFebruary(aOPMCCalculatedData.getFebruary());
			aOPMCCalculatedDataDTO.setId(aOPMCCalculatedData.getId().toString());
			aOPMCCalculatedDataDTO.setJanuary(aOPMCCalculatedData.getJanuary());
			aOPMCCalculatedDataDTO.setJuly(aOPMCCalculatedData.getJuly());
			aOPMCCalculatedDataDTO.setJune(aOPMCCalculatedData.getJune());
			aOPMCCalculatedDataDTO.setMarch(aOPMCCalculatedData.getMarch());
			aOPMCCalculatedDataDTO.setMaterial(aOPMCCalculatedData.getMaterial());
			aOPMCCalculatedDataDTO.setMay(aOPMCCalculatedData.getMay());
			aOPMCCalculatedDataDTO.setNovember(aOPMCCalculatedData.getNovember());
			aOPMCCalculatedDataDTO.setOctober(aOPMCCalculatedData.getOctober());
			aOPMCCalculatedDataDTO.setPlant(aOPMCCalculatedData.getPlant());
			aOPMCCalculatedDataDTO.setSeptember(aOPMCCalculatedData.getSeptember());
			aOPMCCalculatedDataDTO.setSite(aOPMCCalculatedData.getSite());
			aOPMCCalculatedDataDTOList.add(aOPMCCalculatedDataDTO);
		}
		// TODO Auto-generated method stub
		return aOPMCCalculatedDataDTOList;
	}

	@Override
	public List<AOPMCCalculatedDataDTO> editAOPMCCalculatedData(List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList) {
		for(AOPMCCalculatedDataDTO aOPMCCalculatedDataDTO:aOPMCCalculatedDataDTOList) {
			AOPMCCalculatedData aOPMCCalculatedData = new AOPMCCalculatedData();
			aOPMCCalculatedData.setApril(aOPMCCalculatedDataDTO.getApril());
			aOPMCCalculatedData.setAugust(aOPMCCalculatedDataDTO.getAugust());
			aOPMCCalculatedData.setDecember(aOPMCCalculatedDataDTO.getDecember());
			aOPMCCalculatedData.setFebruary(aOPMCCalculatedDataDTO.getFebruary());
			aOPMCCalculatedData.setId(UUID.fromString(aOPMCCalculatedDataDTO.getId()));
			aOPMCCalculatedData.setJanuary(aOPMCCalculatedDataDTO.getJanuary());
			aOPMCCalculatedData.setJuly(aOPMCCalculatedDataDTO.getJuly());
			aOPMCCalculatedData.setJune(aOPMCCalculatedDataDTO.getJune());
			aOPMCCalculatedData.setMarch(aOPMCCalculatedDataDTO.getMarch());
			aOPMCCalculatedData.setMaterial(aOPMCCalculatedDataDTO.getMaterial());
			aOPMCCalculatedData.setMay(aOPMCCalculatedDataDTO.getMay());
			aOPMCCalculatedData.setNovember(aOPMCCalculatedDataDTO.getNovember());
			aOPMCCalculatedData.setOctober(aOPMCCalculatedDataDTO.getOctober());
			aOPMCCalculatedData.setPlant(aOPMCCalculatedDataDTO.getPlant());
			aOPMCCalculatedData.setSeptember(aOPMCCalculatedDataDTO.getSeptember());
			aOPMCCalculatedData.setSite(aOPMCCalculatedDataDTO.getSite());
			aOPMCCalculatedDataRepository.save(aOPMCCalculatedData);
		}
		// TODO Auto-generated method stub
		return aOPMCCalculatedDataDTOList;
	}

}
