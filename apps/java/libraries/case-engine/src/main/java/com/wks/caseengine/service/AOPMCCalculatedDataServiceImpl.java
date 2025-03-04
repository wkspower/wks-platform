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
	    List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList = new ArrayList<>();
	    List<AOPMCCalculatedData> objList = aOPMCCalculatedDataRepository.findAllByYearAndPlantFKId(year, UUID.fromString(plantId));

	    for (AOPMCCalculatedData aopData: objList) {

	        AOPMCCalculatedDataDTO aOPMCCalculatedDataDTO = new AOPMCCalculatedDataDTO();
	        aOPMCCalculatedDataDTO.setApril(aopData.getApril());
	        aOPMCCalculatedDataDTO.setAugust(aopData.getAugust());
	        aOPMCCalculatedDataDTO.setDecember(aopData.getDecember());
	        aOPMCCalculatedDataDTO.setFebruary(aopData.getFebruary());
	        aOPMCCalculatedDataDTO.setId(aopData.getId().toString());
	        aOPMCCalculatedDataDTO.setJanuary(aopData.getJanuary());
	        aOPMCCalculatedDataDTO.setJuly(aopData.getJuly());
	        aOPMCCalculatedDataDTO.setJune(aopData.getJune());
	        aOPMCCalculatedDataDTO.setMarch(aopData.getMarch());
	        aOPMCCalculatedDataDTO.setMaterial(aopData.getMaterial());
	        aOPMCCalculatedDataDTO.setMay(aopData.getMay());
	        aOPMCCalculatedDataDTO.setNovember(aopData.getNovember());
	        aOPMCCalculatedDataDTO.setOctober(aopData.getOctober());
	        aOPMCCalculatedDataDTO.setPlant(aopData.getPlant());
	        aOPMCCalculatedDataDTO.setSeptember(aopData.getSeptember());
	        aOPMCCalculatedDataDTO.setSite(aopData.getSite());
			aOPMCCalculatedDataDTO.setNormParametersFKId(aopData.getNormParametersFKId()!=null?aopData.getNormParametersFKId().toString():null);

	        // Set BDNormParametersFKId in DTO
	        //aOPMCCalculatedDataDTO.setBDNormParametersFKId(bdNormParametersFKId.toString());
	        aOPMCCalculatedDataDTOList.add(aOPMCCalculatedDataDTO);
	    }


		List<Object[]> list = aOPMCCalculatedDataRepository.getDataBusinessAllData(plantId,year);
int i=1;
		for(Object[] obj :list){
               
			AOPMCCalculatedDataDTO aOPMCCalculatedDataDTO = new AOPMCCalculatedDataDTO();

			aOPMCCalculatedDataDTO.setNormParametersFKId(obj[0]!=null? obj[0].toString():null);
			aOPMCCalculatedDataDTO.setId(i+"#");
			aOPMCCalculatedDataDTOList.add(aOPMCCalculatedDataDTO);
i++;
		}


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
			if(aOPMCCalculatedDataDTO.getId()==null || aOPMCCalculatedDataDTO.getId().contains("#") ){
				aOPMCCalculatedData.setId(null);
			}else{
				aOPMCCalculatedData.setId(UUID.fromString(aOPMCCalculatedDataDTO.getId()));
			}
			
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


			aOPMCCalculatedData.setPlantFKId(UUID.fromString(aOPMCCalculatedDataDTO.getPlantFKId()));
			aOPMCCalculatedData.setYear(aOPMCCalculatedDataDTO.getYear());
			aOPMCCalculatedData.setNormParametersFKId(UUID.fromString(aOPMCCalculatedDataDTO.getNormParametersFKId()));

			
			aOPMCCalculatedDataRepository.save(aOPMCCalculatedData);
		}
		// TODO Auto-generated method stub
		return aOPMCCalculatedDataDTOList;
	}

}
