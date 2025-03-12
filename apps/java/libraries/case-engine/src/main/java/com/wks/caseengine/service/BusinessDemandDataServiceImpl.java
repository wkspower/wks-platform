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
		List<BusinessDemandDataDTO> businessDemandDataDTOList = new ArrayList<>();

		for (Object[] row : obj) {
		    BusinessDemandDataDTO businessDemandDataDTO = new BusinessDemandDataDTO();
		    
		    businessDemandDataDTO.setNormTypeId(row[0].toString());
		    businessDemandDataDTO.setNormName(row[1] != null ? row[1].toString() : null);
		    businessDemandDataDTO.setId(row[2].toString());
		    businessDemandDataDTO.setRemark(row[3] != null ? row[3].toString() : null);
		    businessDemandDataDTO.setJan(row[4] != null ? Float.parseFloat(row[4].toString()) : null);
		    businessDemandDataDTO.setFeb(row[5] != null ? Float.parseFloat(row[5].toString()) : null);
		    businessDemandDataDTO.setMarch(row[6] != null ? Float.parseFloat(row[6].toString()) : null);
		    businessDemandDataDTO.setApril(row[7] != null ? Float.parseFloat(row[7].toString()) : null);
		    businessDemandDataDTO.setMay(row[8] != null ? Float.parseFloat(row[8].toString()) : null);
		    businessDemandDataDTO.setJune(row[9] != null ? Float.parseFloat(row[9].toString()) : null);
		    businessDemandDataDTO.setJuly(row[10] != null ? Float.parseFloat(row[10].toString()) : null);
		    businessDemandDataDTO.setAug(row[11] != null ? Float.parseFloat(row[11].toString()) : null);
		    businessDemandDataDTO.setSep(row[12] != null ? Float.parseFloat(row[12].toString()) : null);
		    businessDemandDataDTO.setOct(row[13] != null ? Float.parseFloat(row[13].toString()) : null);
		    businessDemandDataDTO.setNov(row[14] != null ? Float.parseFloat(row[14].toString()) : null);
		    businessDemandDataDTO.setDec(row[15] != null ? Float.parseFloat(row[15].toString()) : null);
		    businessDemandDataDTO.setYear(row[16].toString());
		    businessDemandDataDTO.setPlantId(row[17] != null ? row[17].toString().toUpperCase() : null);
		    businessDemandDataDTO.setNormParameterId(row[18] != null ? row[18].toString() : null);
		    businessDemandDataDTO.setAvgTph(row[19] != null ? Float.parseFloat(row[19].toString()) : null);
		    businessDemandDataDTO.setDisplayOrder(row[20] != null ? Integer.parseInt(row[20].toString()) : null);
		    
		    businessDemandDataDTOList.add(businessDemandDataDTO);
		}
		 
		 
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
	
			if (businessDemandDataDTO.getId() != null && !businessDemandDataDTO.getId().isEmpty()) {
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
