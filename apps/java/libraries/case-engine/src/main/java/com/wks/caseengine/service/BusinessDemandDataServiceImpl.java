package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.wks.caseengine.entity.BusinessDemand;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.Product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;
import com.wks.caseengine.dto.BusinessDemandDataDTO;
import com.wks.caseengine.repository.BusinessDemandDataRepository;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BusinessDemandDataServiceImpl implements BusinessDemandDataService{
	
	@Autowired
	private BusinessDemandDataRepository businessDemandDataRepository;
	
	 @Autowired
	 private NormParametersService normParametersService;
	

	@Override
	public List<BusinessDemandDataDTO> getBusinessDemandData(String year,String plantId) {
		List<Object[]> obj = businessDemandDataRepository.findByYearAndPlantFkId(year, UUID.fromString(plantId));
		System.out.println("obj"+obj);
		List<BusinessDemandDataDTO> businessDemandDataDTOList = new ArrayList<>();

		for (Object[] row : obj) {
		    BusinessDemandDataDTO businessDemandDataDTO = new BusinessDemandDataDTO();

		    businessDemandDataDTO.setId(row[0] != null ? row[0].toString() : null);
		    businessDemandDataDTO.setRemark(row[1] != null ? row[1].toString() : null);
		    businessDemandDataDTO.setJan(row[2] != null ? Float.parseFloat(row[2].toString()) : null);
		    businessDemandDataDTO.setFeb(row[3] != null ? Float.parseFloat(row[3].toString()) : null);
		    businessDemandDataDTO.setMarch(row[4] != null ? Float.parseFloat(row[4].toString()) : null);
		    businessDemandDataDTO.setApril(row[5] != null ? Float.parseFloat(row[5].toString()) : null);
		    businessDemandDataDTO.setMay(row[6] != null ? Float.parseFloat(row[6].toString()) : null);
		    businessDemandDataDTO.setJune(row[7] != null ? Float.parseFloat(row[7].toString()) : null);
		    businessDemandDataDTO.setJuly(row[8] != null ? Float.parseFloat(row[8].toString()) : null);
		    businessDemandDataDTO.setAug(row[9] != null ? Float.parseFloat(row[9].toString()) : null);
		    businessDemandDataDTO.setSep(row[10] != null ? Float.parseFloat(row[10].toString()) : null);
		    businessDemandDataDTO.setOct(row[11] != null ? Float.parseFloat(row[11].toString()) : null);
		    businessDemandDataDTO.setNov(row[12] != null ? Float.parseFloat(row[12].toString()) : null);
		    businessDemandDataDTO.setDec(row[13] != null ? Float.parseFloat(row[13].toString()) : null);
		    businessDemandDataDTO.setYear(row[13] != null ? row[14].toString() : null);
		    businessDemandDataDTO.setPlantId(row[15] != null ? row[15].toString().toUpperCase() : null);
		    businessDemandDataDTO.setNormParameterId(row[16] != null ? row[16].toString() : null);
		    businessDemandDataDTO.setAvgTph(row[17] != null ? Float.parseFloat(row[17].toString()) : null);
		    businessDemandDataDTO.setDisplayOrder(row[18] != null ? Integer.parseInt(row[18].toString()) : null);
		    businessDemandDataDTO.setNormParameterTypeId(row[19] != null ? row[19].toString() : null);
		    businessDemandDataDTO.setNormParameterTypeName(row[20] != null ? row[20].toString() : null);
		    businessDemandDataDTO.setNormParameterTypeDisplayName(row[21] != null ? row[21].toString() : null);

		    businessDemandDataDTOList.add(businessDemandDataDTO);
		}
		
//		List<Object[]> list = businessDemandDataRepository.getAllBusinessDemandData(plantId);
// 		int i=1;
// 		for(Object[] obj1 :list){
//            System.out.println("obj1"+obj1);
// 			BusinessDemandDataDTO businessDemandDataDTO = new BusinessDemandDataDTO();

// 			businessDemandDataDTO.setNormParameterId(obj1[0]!=null? obj1[0].toString():null);
// 			businessDemandDataDTO.setNormParameterTypeDisplayName(obj1[1]!=null? obj1[1].toString():null);
// 			businessDemandDataDTO.setId(i+"#");
// 			businessDemandDataDTOList.add(businessDemandDataDTO);
// 			i++;
// 		}

		 
        return businessDemandDataDTOList;
	}

	@Override
	public List<BusinessDemandDataDTO> saveBusinessDemandData(List<BusinessDemandDataDTO> businessDemandDataDTOList) {
		
		for(BusinessDemandDataDTO businessDemandDataDTO: businessDemandDataDTOList){
            BusinessDemand businessDemand =new BusinessDemand();
			businessDemand.setApril(businessDemandDataDTO.getApril());
			businessDemand.setAug(businessDemandDataDTO.getAug());
			businessDemand.setAvgTph(businessDemandDataDTO.getAvgTph());
			businessDemand.setDec(businessDemandDataDTO.getDec());
			businessDemand.setFeb(businessDemandDataDTO.getFeb());
	
			if(businessDemandDataDTO.getId()==null || businessDemandDataDTO.getId().contains("#") ){
				businessDemand.setId(null);
			}else {
				businessDemand.setId(UUID.fromString(businessDemandDataDTO.getId()));
			}
	
			businessDemand.setJan(businessDemandDataDTO.getJan());
			businessDemand.setJuly(businessDemandDataDTO.getJuly());
			businessDemand.setJune(businessDemandDataDTO.getJune());
			businessDemand.setMarch(businessDemandDataDTO.getMarch());
			businessDemand.setMay(businessDemandDataDTO.getMay());
	
			if (businessDemandDataDTO.getNormParameterId() != null && !businessDemandDataDTO.getNormParameterId().isEmpty()) {
				businessDemand.setNormParameterId(UUID.fromString(businessDemandDataDTO.getNormParameterId()));
			}
	
			businessDemand.setNov(businessDemandDataDTO.getNov());
			businessDemand.setOct(businessDemandDataDTO.getOct());
	
			if (businessDemandDataDTO.getPlantId() != null && !businessDemandDataDTO.getPlantId().isEmpty()) {
			businessDemand.setPlantId(UUID.fromString(businessDemandDataDTO.getPlantId()));
			businessDemand.setRemark(businessDemandDataDTO.getRemark());
			businessDemand.setSep(businessDemandDataDTO.getSep());
			businessDemand.setYear(businessDemandDataDTO.getYear());
			if(businessDemandDataDTO.getSiteFKId()!=null) {
				businessDemand.setSiteFKId(UUID.fromString(businessDemandDataDTO.getSiteFKId()));
			}
			if(businessDemandDataDTO.getVerticalFKId()!=null) {
				businessDemand.setVerticalFKId(UUID.fromString(businessDemandDataDTO.getVerticalFKId()));
			}
			businessDemandDataRepository.save(businessDemand);
			
			
	   }
	}			// TODO Auto-generated method stub
		return businessDemandDataDTOList;
	
	}

	@Override
	public List<BusinessDemandDataDTO> editBusinessDemandData(List<BusinessDemandDataDTO> businessDemandDataDTOList) {
		
		for(BusinessDemandDataDTO businessDemandDataDTO: businessDemandDataDTOList){
            BusinessDemand businessDemand =new BusinessDemand();

			businessDemand.setApril(businessDemandDataDTO.getApril());
			businessDemand.setAug(businessDemandDataDTO.getAug());
			businessDemand.setAvgTph(businessDemandDataDTO.getAvgTph());
			businessDemand.setDec(businessDemandDataDTO.getDec());
			businessDemand.setFeb(businessDemandDataDTO.getFeb());
			if(businessDemandDataDTO.getId()!=null) {
				businessDemand.setId(UUID.fromString(businessDemandDataDTO.getId()));
			}
			businessDemand.setJan(businessDemandDataDTO.getJan());
			businessDemand.setJuly(businessDemandDataDTO.getJuly());
			businessDemand.setJune(businessDemandDataDTO.getJune());
			businessDemand.setMarch(businessDemandDataDTO.getMarch());
			businessDemand.setMay(businessDemandDataDTO.getMay());
			businessDemand.setNormParameterId(UUID.fromString(businessDemandDataDTO.getNormParameterId()));
			businessDemand.setNov(businessDemandDataDTO.getNov());
			businessDemand.setOct(businessDemandDataDTO.getOct());
			businessDemand.setPlantId(UUID.fromString(businessDemandDataDTO.getPlantId()));
			businessDemand.setRemark(businessDemandDataDTO.getRemark());
			businessDemand.setSep(businessDemandDataDTO.getSep());
			businessDemand.setYear(businessDemandDataDTO.getYear());
			businessDemandDataRepository.save(businessDemand);
	   }
				// TODO Auto-generated method stub
		return businessDemandDataDTOList;
	}

	@Override
	public BusinessDemandDataDTO deleteBusinessDemandData(UUID id) {
		//businessDemandDataRepository.softDelete(UUID.fromString(businessDemandDataDTO.getId()));
		
		BusinessDemand businessDemand = new BusinessDemand();
		businessDemand.setId(id);
		businessDemandDataRepository.delete(businessDemand);
		return null;
	}

}
