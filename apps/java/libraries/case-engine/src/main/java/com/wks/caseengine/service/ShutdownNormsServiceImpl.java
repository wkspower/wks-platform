package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
		List<Object[]> objList = shutdownNormsRepository.findByYearAndPlantFkId(year, UUID.fromString(plantId));
		System.out.println("obj.size(): " + objList.size());
		List<MCUNormsValueDTO> mcuNormsValueDTOList = new ArrayList<>();
		for (Object[] row : objList) {
		    MCUNormsValueDTO mCUNormsValueDTO = new MCUNormsValueDTO();
		    mCUNormsValueDTO.setId(row[0] != null ? row[0].toString() : null);
		    mCUNormsValueDTO.setSiteFkId(row[1] != null ? row[1].toString() : null);
		    mCUNormsValueDTO.setPlantFkId(row[2] != null ? row[2].toString() : null);
		    mCUNormsValueDTO.setVerticalFkId(row[3] != null ? row[3].toString() : null);
		    mCUNormsValueDTO.setMaterialFkId(row[4] != null ? row[4].toString() : null);
		    mCUNormsValueDTO.setApril(row[5] != null ? Float.parseFloat(row[5].toString()) : null);
		    mCUNormsValueDTO.setMay(row[6] != null ? Float.parseFloat(row[6].toString()) : null);
		    mCUNormsValueDTO.setJune(row[7] != null ? Float.parseFloat(row[7].toString()) : null);
		    mCUNormsValueDTO.setJuly(row[8] != null ? Float.parseFloat(row[8].toString()) : null);
		    mCUNormsValueDTO.setAugust(row[9] != null ? Float.parseFloat(row[9].toString()) : null);
		    mCUNormsValueDTO.setSeptember(row[10] != null ? Float.parseFloat(row[11].toString()) : null);
		    mCUNormsValueDTO.setOctober(row[11] != null ? Float.parseFloat(row[11].toString()) : null);
		    mCUNormsValueDTO.setNovember(row[12] != null ? Float.parseFloat(row[12].toString()) : null);
		    mCUNormsValueDTO.setDecember(row[13] != null ? Float.parseFloat(row[13].toString()) : null);
		    mCUNormsValueDTO.setJanuary(row[14] != null ? Float.parseFloat(row[14].toString()) : null);
		    mCUNormsValueDTO.setFebruary(row[15] != null ? Float.parseFloat(row[15].toString()) : null);
		    mCUNormsValueDTO.setMarch(row[16] != null ? Float.parseFloat(row[16].toString()) : null);
		    
		    mCUNormsValueDTO.setFinancialYear(row[17] != null ? row[17].toString() : null);
		    mCUNormsValueDTO.setRemarks(row[18] != null ? row[18].toString() : " ");
		    mCUNormsValueDTO.setCreatedOn(row[19] != null ? (Date) row[19] : null);
		    mCUNormsValueDTO.setModifiedOn(row[20] != null ? (Date) row[20] : null);
		    mCUNormsValueDTO.setMcuVersion(row[21] != null ? row[21].toString() : null);
		    mCUNormsValueDTO.setUpdatedBy(row[22] != null ? row[22].toString() : null);
		    mCUNormsValueDTO.setNormParameterTypeId(row[23] != null ? row[23].toString() : null);
		    mCUNormsValueDTO.setNormParameterTypeName(row[24] != null ? row[24].toString() : null);
		    mCUNormsValueDTO.setNormParameterTypeDisplayName(row[25] != null ? row[25].toString() : null);

		    mcuNormsValueDTOList.add(mCUNormsValueDTO);
		}

		return mcuNormsValueDTOList;
	}
	@Override
	public List<MCUNormsValueDTO> saveShutdownNormsData(List<MCUNormsValueDTO> mCUNormsValueDTOList) {
		
		for(MCUNormsValueDTO mCUNormsValueDTO:mCUNormsValueDTOList) {
			MCUNormsValue mCUNormsValue= new MCUNormsValue();
			if(mCUNormsValueDTO.getId()!=null && !mCUNormsValueDTO.getId().isEmpty()) {
				mCUNormsValue.setId(UUID.fromString(mCUNormsValueDTO.getId()));
				mCUNormsValue.setModifiedOn(new Date());
			}else {
				mCUNormsValue.setCreatedOn(new Date());
			}
			mCUNormsValue.setApril(Optional.ofNullable(mCUNormsValueDTO.getApril()).orElse(0.00f));
			mCUNormsValue.setMay(Optional.ofNullable(mCUNormsValueDTO.getMay()).orElse(0.00f));
			mCUNormsValue.setJune(Optional.ofNullable(mCUNormsValueDTO.getJune()).orElse(0.00f));
			mCUNormsValue.setJuly(Optional.ofNullable(mCUNormsValueDTO.getJuly()).orElse(0.00f));
			mCUNormsValue.setAugust(Optional.ofNullable(mCUNormsValueDTO.getAugust()).orElse(0.00f));
			mCUNormsValue.setSeptember(Optional.ofNullable(mCUNormsValueDTO.getSeptember()).orElse(0.00f));
			mCUNormsValue.setOctober(Optional.ofNullable(mCUNormsValueDTO.getOctober()).orElse(0.00f));
			mCUNormsValue.setNovember(Optional.ofNullable(mCUNormsValueDTO.getNovember()).orElse(0.00f));
			mCUNormsValue.setDecember(Optional.ofNullable(mCUNormsValueDTO.getDecember()).orElse(0.00f));
			mCUNormsValue.setJanuary(Optional.ofNullable(mCUNormsValueDTO.getJanuary()).orElse(0.00f));
			mCUNormsValue.setFebruary(Optional.ofNullable(mCUNormsValueDTO.getFebruary()).orElse(0.00f));
			mCUNormsValue.setMarch(Optional.ofNullable(mCUNormsValueDTO.getMarch()).orElse(0.00f));
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
