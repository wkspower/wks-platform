package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.MCUNormsValueDTO;
import com.wks.caseengine.entity.MCUNormsValue;
import com.wks.caseengine.repository.ShutdownNormsRepository;

@Service
public class ShutdownNormsServiceImpl implements ShutdownNormsService{
	
	@Autowired
	private ShutdownNormsRepository shutdownNormsRepository;
	
	@Override
	public List<MCUNormsValueDTO> getShutdownNormsData(String year, String plantId) {
		List<Object[]> obj = shutdownNormsRepository.findByYearAndPlantFkId(year, UUID.fromString(plantId));
		List<MCUNormsValueDTO> mCUNormsValueDTOList = new ArrayList<>();
		System.out.println("obj.size()"+obj.size());
		for (Object[] row : obj) {
		    MCUNormsValueDTO mCUNormsValueDTO = new MCUNormsValueDTO();
		    mCUNormsValueDTO.setId(row[0].toString());
		    mCUNormsValueDTO.setSiteFkId(row[1].toString());
		    mCUNormsValueDTO.setPlantFkId(row[2].toString());
		    mCUNormsValueDTO.setVerticalFkId(row[3].toString());
		    mCUNormsValueDTO.setMaterialFkId(row[4].toString());
		    
		    mCUNormsValueDTO.setApril(0.0F);
		    mCUNormsValueDTO.setMay(0.0F);
		    mCUNormsValueDTO.setJune(0.0F);
		    mCUNormsValueDTO.setJuly(0.0F);
		    mCUNormsValueDTO.setAugust(0.0F);
		    mCUNormsValueDTO.setSeptember(0.0F);
		    mCUNormsValueDTO.setOctober(0.0F);
		    mCUNormsValueDTO.setNovember(0.0F);
		    mCUNormsValueDTO.setDecember(0.0F);
		    mCUNormsValueDTO.setJanuary(0.0F);
		    mCUNormsValueDTO.setFebruary(0.0F);
		    mCUNormsValueDTO.setMarch(0.0F);
		    
		    mCUNormsValueDTO.setFinancialYear(row[17].toString());
		    mCUNormsValueDTO.setRemarks(row[18] != null ? row[18].toString() : " ");
		    mCUNormsValueDTO.setCreatedOn(row[19] != null ? (Date) row[19] : null);
		    mCUNormsValueDTO.setModifiedOn(row[20] != null ? (Date) row[20] : null);
		    mCUNormsValueDTO.setMcuVersion(row[21] != null ? row[21].toString() : null);
		    mCUNormsValueDTO.setUpdatedBy(row[22] != null ? row[22].toString() : null);
		    mCUNormsValueDTO.setNormParameterTypeId(row[23] != null ? row[23].toString() : null);
		    mCUNormsValueDTO.setNormParameterTypeName(row[24] != null ? row[24].toString() : null);
		    mCUNormsValueDTO.setNormParameterTypeDisplayName(row[25] != null ? row[25].toString() : null);

		    mCUNormsValueDTOList.add(mCUNormsValueDTO);
		}

		return mCUNormsValueDTOList;
	}
	@Override
	public List<MCUNormsValueDTO> saveShutdownNormsData(List<MCUNormsValueDTO> mCUNormsValueDTOList) {
		
		for(MCUNormsValueDTO mCUNormsValueDTO:mCUNormsValueDTOList) {
			MCUNormsValue mCUNormsValue= new MCUNormsValue();
			if(mCUNormsValueDTO.getId()!=null || !mCUNormsValueDTO.getId().isEmpty()) {
				mCUNormsValue.setId(UUID.fromString(mCUNormsValueDTO.getId()));
				mCUNormsValue.setModifiedOn(new Date());
			}else {
				mCUNormsValue.setCreatedOn(new Date());
			}
			mCUNormsValue.setApril(mCUNormsValueDTO.getApril());
			mCUNormsValue.setMay(mCUNormsValueDTO.getMay());
			mCUNormsValue.setJune(mCUNormsValueDTO.getJune());
			mCUNormsValue.setJuly(mCUNormsValueDTO.getJuly());
			mCUNormsValue.setAugust(mCUNormsValueDTO.getAugust());
			mCUNormsValue.setSeptember(mCUNormsValueDTO.getSeptember());
			mCUNormsValue.setOctober(mCUNormsValueDTO.getOctober());
			mCUNormsValue.setNovember(mCUNormsValueDTO.getNovember());
			mCUNormsValue.setDecember(mCUNormsValueDTO.getDecember());
			mCUNormsValue.setJanuary(mCUNormsValueDTO.getJanuary());
			mCUNormsValue.setFebruary(mCUNormsValueDTO.getFebruary());
			mCUNormsValue.setMarch(mCUNormsValueDTO.getMarch());
			if(mCUNormsValueDTO.getSiteFkId()!=null) {
				mCUNormsValue.setSiteFkId(UUID.fromString(mCUNormsValueDTO.getSiteFkId()));
			}
			if(mCUNormsValueDTO.getPlantFkId()!=null) {
				mCUNormsValue.setPlantFkId(UUID.fromString(mCUNormsValueDTO.getPlantFkId()));
			}
			if(mCUNormsValueDTO.getVerticalFkId()!=null) {
				mCUNormsValue.setVerticalFkId(UUID.fromString(mCUNormsValueDTO.getVerticalFkId()));
			}
			if(mCUNormsValueDTO.getMaterialFkId()!=null) {
				mCUNormsValue.setMaterialFkId(UUID.fromString(mCUNormsValueDTO.getMaterialFkId()));
			}
			if(mCUNormsValueDTO.getNormParameterTypeId()!=null) {
				mCUNormsValue.setNormParameterTypeFkId(UUID.fromString(mCUNormsValueDTO.getNormParameterTypeId()));
			}
			
			mCUNormsValue.setFinancialYear(mCUNormsValueDTO.getFinancialYear());
			mCUNormsValue.setRemarks(mCUNormsValueDTO.getRemarks());
			mCUNormsValue.setMcuVersion("V1");
			mCUNormsValue.setUpdatedBy("System");
			
			System.out.println("Data Saved Succussfully");
			shutdownNormsRepository.save(mCUNormsValue);
		}
		// TODO Auto-generated method stub
		return mCUNormsValueDTOList;
	}


}
