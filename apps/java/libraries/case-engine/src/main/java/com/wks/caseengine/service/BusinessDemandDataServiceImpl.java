package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Objects;
import com.wks.caseengine.entity.BusinessDemand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.BusinessDemandDataDTO;
import com.wks.caseengine.repository.BusinessDemandDataRepository;

@Service
public class BusinessDemandDataServiceImpl implements BusinessDemandDataService{
	
	@Autowired
	private BusinessDemandDataRepository businessDemandDataRepository;
	
	

	@Override
	public List<BusinessDemandDataDTO> getBusinessDemandData(String year,String plantId) {
		List<Object[]> obj = businessDemandDataRepository.findByYearAndPlantFkId(year, UUID.fromString(plantId));
		List<BusinessDemandDataDTO> businessDemandDataDTOList = new ArrayList<>();

		for (Object[] row : obj) {
		    BusinessDemandDataDTO businessDemandDataDTO = new BusinessDemandDataDTO();
		    
		    businessDemandDataDTO.setId(Objects.toString(row[0], null));
		    businessDemandDataDTO.setRemark(row[1] != null ? row[1].toString() : null);
		    businessDemandDataDTO.setJan(row[2] != null ? Float.parseFloat(row[2].toString()) : 0.0f);
		    businessDemandDataDTO.setFeb(row[3] != null ? Float.parseFloat(row[3].toString()) : 0.0f);
		    businessDemandDataDTO.setMarch(row[4] != null ? Float.parseFloat(row[4].toString()) : 0.0f);
		    businessDemandDataDTO.setApril(row[5] != null ? Float.parseFloat(row[5].toString()) : 0.0f);
		    businessDemandDataDTO.setMay(row[6] != null ? Float.parseFloat(row[6].toString()) : 0.0f);
		    businessDemandDataDTO.setJune(row[7] != null ? Float.parseFloat(row[7].toString()) : 0.0f);
		    businessDemandDataDTO.setJuly(row[8] != null ? Float.parseFloat(row[8].toString()) : 0.0f);
		    businessDemandDataDTO.setAug(row[9] != null ? Float.parseFloat(row[9].toString()) : 0.0f);
		    businessDemandDataDTO.setSep(row[10] != null ? Float.parseFloat(row[10].toString()) : 0.0f);
		    businessDemandDataDTO.setOct(row[11] != null ? Float.parseFloat(row[11].toString()) : 0.0f);
		    businessDemandDataDTO.setNov(row[12] != null ? Float.parseFloat(row[12].toString()) : 0.0f);
		    businessDemandDataDTO.setDec(row[13] != null ? Float.parseFloat(row[13].toString()) : 0.0f);
		    
		    businessDemandDataDTO.setYear(Objects.toString(row[14], null));

		    // If Plant_FK_Id is UUID, convert it properly
		    businessDemandDataDTO.setPlantId(row[15] != null ? row[15].toString().toUpperCase() : null);

		    businessDemandDataDTO.setNormParameterId(row[16] != null ? row[16].toString() : null);
		    businessDemandDataDTO.setAvgTph(row[17] != null ? Float.parseFloat(row[17].toString()) : 0.0f);

		    businessDemandDataDTO.setDisplayOrder(row[18] != null ? Integer.parseInt(row[18].toString()) : 0);

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
