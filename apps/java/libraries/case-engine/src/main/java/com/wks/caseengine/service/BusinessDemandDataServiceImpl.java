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
	
	// @Autowired
	// private NormParametersService normParametersService;
	

	@Override
	public List<BusinessDemandDataDTO> getBusinessDemandData(String year,String plantId) {
		List<BusinessDemand> businessDemandDataList= businessDemandDataRepository.findAllByYearAndPlantId(year,UUID.fromString(plantId));
		List<BusinessDemandDataDTO> businessDemandDataDTOList=new ArrayList<>();
		for(BusinessDemand businessDemand:businessDemandDataList) {
			BusinessDemandDataDTO businessDemandDataDTO =new BusinessDemandDataDTO();
			businessDemandDataDTO.setApril(businessDemand.getApril());
			businessDemandDataDTO.setAug(businessDemand.getAug());
			businessDemandDataDTO.setAvgTph(businessDemand.getAvgTph());
			businessDemandDataDTO.setDec(businessDemand.getDec());
			businessDemandDataDTO.setFeb(businessDemand.getFeb());
			businessDemandDataDTO.setId(businessDemand.getId().toString());
			businessDemandDataDTO.setJan(businessDemand.getJan());
			businessDemandDataDTO.setJuly(businessDemand.getJuly());
			businessDemandDataDTO.setJune(businessDemand.getJune());
			businessDemandDataDTO.setMarch(businessDemand.getMarch());
			businessDemandDataDTO.setMay(businessDemand.getMay());
			businessDemandDataDTO.setNormParameterId(businessDemand.getNormParameterId().toString());
			businessDemandDataDTO.setNov(businessDemand.getNov());
			businessDemandDataDTO.setOct(businessDemand.getOct());
			businessDemandDataDTO.setPlantId(businessDemand.getPlantId().toString().toUpperCase());
			businessDemandDataDTO.setRemark(businessDemand.getRemark());
			businessDemandDataDTO.setSep(businessDemand.getSep());
			businessDemandDataDTO.setYear(businessDemand.getYear());
			businessDemandDataDTO.setPlantId(businessDemand.getPlantId().toString());
			businessDemandDataDTOList.add(businessDemandDataDTO);
		}
		// List<NormParameters> normParametersList=normParametersService.findAllByType("ProductionNorms");
		// // Create a Map of normParameterId -> displayOrder
        // Map<UUID, Integer> displayOrderMap = normParametersList.stream()
        //         .collect(Collectors.toMap(NormParameters::getId, NormParameters::getDisplayOrder));

        // // Sort businessDemandList based on displayOrder
        // businessDemandDataDTOList.sort(Comparator.comparing(dto -> displayOrderMap.getOrDefault(dto.getNormParameterId(), Integer.MAX_VALUE)));

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
