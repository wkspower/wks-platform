package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;
import com.wks.caseengine.entity.AOPMCCalculatedData;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.repository.AOPMCCalculatedDataRepository;

@Service
public class AOPMCCalculatedDataServiceImpl implements AOPMCCalculatedDataService{
	
	@Autowired
	private AOPMCCalculatedDataRepository aOPMCCalculatedDataRepository;

	@Override
	public List<AOPMCCalculatedDataDTO> getAOPMCCalculatedData(String plantId, String year) {
	    
		//  List<Object[]> obj= aOPMCCalculatedDataRepository.findByYearAndPlantFkId(year, UUID.fromString(plantId));
		List<Object[]> obj= aOPMCCalculatedDataRepository.getDataMCUValuesAllData(year, UUID.fromString(plantId));
	    List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList = new ArrayList<>();

	    for (Object[] row : obj) {
 	    	AOPMCCalculatedDataDTO aOPMCCalculatedDataDTO = new AOPMCCalculatedDataDTO();
			aOPMCCalculatedDataDTO.setId(row[0] != null ? row[0].toString() : null);
 	    	aOPMCCalculatedDataDTO.setSiteFKID(row[1] != null ? row[1].toString() : null);
 	    	aOPMCCalculatedDataDTO.setPlantFKID(row[2] != null ? row[2].toString() : null);
 	    	aOPMCCalculatedDataDTO.setMaterialFKID(row[3] != null ? row[3].toString() : null);
 	    	aOPMCCalculatedDataDTO.setApril(row[4] != null ? Float.parseFloat(row[4].toString()) : null);
 	    	aOPMCCalculatedDataDTO.setMay(row[4] != null ? Float.parseFloat(row[5].toString()) : null);
 	    	aOPMCCalculatedDataDTO.setJune(row[6] != null ? Float.parseFloat(row[6].toString()) : null);
 	    	aOPMCCalculatedDataDTO.setJuly(row[7] != null ? Float.parseFloat(row[7].toString()) : null);
 	    	aOPMCCalculatedDataDTO.setAugust(row[8] != null ? Float.parseFloat(row[8].toString()) : null);
 	    	aOPMCCalculatedDataDTO.setSeptember(row[9] != null ? Float.parseFloat(row[9].toString()) : null);
 	    	aOPMCCalculatedDataDTO.setOctober(row[10] != null ? Float.parseFloat(row[10].toString()) : null);
 	    	aOPMCCalculatedDataDTO.setNovember(row[11] != null ? Float.parseFloat(row[11].toString()) : null);
 	    	aOPMCCalculatedDataDTO.setDecember(row[12] != null ? Float.parseFloat(row[12].toString()) : null); 
			aOPMCCalculatedDataDTO.setJanuary(row[13] != null ? Float.parseFloat(row[13].toString()) : null);
 	    	aOPMCCalculatedDataDTO.setFebruary(row[14] != null ? Float.parseFloat(row[14].toString()) : null);
 	    	// aOPMCCalculatedDataDTO.setPlantFKId(row[15] != null ? row[15].toString() : null);
 	    	aOPMCCalculatedDataDTO.setMarch(row[15] != null ? Float.parseFloat(row[15].toString()) : null);
 	    	aOPMCCalculatedDataDTO.setFinancialYear(row[16] != null ? row[16].toString() : null);
 	    	// aOPMCCalculatedDataDTO.setDisplayOrder(row[19] != null ? Integer.parseInt(row[19].toString()) : null);
 	    	aOPMCCalculatedDataDTO.setRemark(row[17] != null ? row[17].toString() : null);
 	    	aOPMCCalculatedDataDTOList.add(aOPMCCalculatedDataDTO);
 	    }
	    
	   
 		// List<Object[]> list = aOPMCCalculatedDataRepository.getDataBusinessAllData(plantId,year);
 		// int i=1;
 		// for(Object[] obj1 :list){
               
 		// 	AOPMCCalculatedDataDTO aOPMCCalculatedDataDTO = new AOPMCCalculatedDataDTO();

 		// 	aOPMCCalculatedDataDTO.setNormParametersFKId(obj1[0]!=null? obj1[0].toString():null);
 		// 	aOPMCCalculatedDataDTO.setId(i+"#");
 		// 	aOPMCCalculatedDataDTOList.add(aOPMCCalculatedDataDTO);
 		// 	i++;
 		// }


	    return aOPMCCalculatedDataDTOList;
	}

	@Override
	public List<AOPMCCalculatedDataDTO> editAOPMCCalculatedData(List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList) {
		for(AOPMCCalculatedDataDTO aOPMCCalculatedDataDTO:aOPMCCalculatedDataDTOList) {
			AOPMCCalculatedData aOPMCCalculatedData = new AOPMCCalculatedData();
			if(aOPMCCalculatedDataDTO.getId()==null || aOPMCCalculatedDataDTO.getId().contains("#") ){
				aOPMCCalculatedData.setId(null);
			}else{
				aOPMCCalculatedData.setId(UUID.fromString(aOPMCCalculatedDataDTO.getId()));
			}
			aOPMCCalculatedData.setPlantFKId(UUID.fromString(aOPMCCalculatedDataDTO.getPlantFKId()));
			aOPMCCalculatedData.setSiteFKId(UUID.fromString(aOPMCCalculatedDataDTO.getSiteFKId()));
			aOPMCCalculatedData.setVerticalFKId(UUID.fromString(aOPMCCalculatedDataDTO.getVerticalFKId()));
			aOPMCCalculatedData.setMaterialFKId(UUID.fromString(aOPMCCalculatedDataDTO.getMaterialFKId()));
			
			aOPMCCalculatedData.setJanuary(aOPMCCalculatedDataDTO.getJanuary());
			aOPMCCalculatedData.setFebruary(aOPMCCalculatedDataDTO.getFebruary());
			aOPMCCalculatedData.setMarch(aOPMCCalculatedDataDTO.getMarch());
			aOPMCCalculatedData.setApril(aOPMCCalculatedDataDTO.getApril());
			aOPMCCalculatedData.setMay(aOPMCCalculatedDataDTO.getMay());
			aOPMCCalculatedData.setJune(aOPMCCalculatedDataDTO.getJune());
			aOPMCCalculatedData.setJuly(aOPMCCalculatedDataDTO.getJuly());
			aOPMCCalculatedData.setAugust(aOPMCCalculatedDataDTO.getAugust());
			aOPMCCalculatedData.setSeptember(aOPMCCalculatedDataDTO.getSeptember());
			aOPMCCalculatedData.setOctober(aOPMCCalculatedDataDTO.getOctober());
			aOPMCCalculatedData.setNovember(aOPMCCalculatedDataDTO.getNovember());
			aOPMCCalculatedData.setDecember(aOPMCCalculatedDataDTO.getDecember());			aOPMCCalculatedData.setJanuary(aOPMCCalculatedDataDTO.getJanuary());

			aOPMCCalculatedData.setFinancialYear(aOPMCCalculatedDataDTO.getFinancialYear());
			aOPMCCalculatedData.setRemark(aOPMCCalculatedDataDTO.getRemark());

			
			aOPMCCalculatedDataRepository.save(aOPMCCalculatedData);
		}
		// TODO Auto-generated method stub
		return aOPMCCalculatedDataDTOList;
	}

}
