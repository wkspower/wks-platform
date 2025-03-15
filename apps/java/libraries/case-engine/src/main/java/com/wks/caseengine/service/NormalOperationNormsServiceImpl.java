package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.MCUNormsValueDTO;
import com.wks.caseengine.entity.MCUNormsValue;
import com.wks.caseengine.repository.NormalOperationNormsRepository;

@Service
public class NormalOperationNormsServiceImpl implements NormalOperationNormsService{
	
	@Autowired
	private NormalOperationNormsRepository normalOperationNormsRepository;

	@Override
	public List<MCUNormsValueDTO> getNormalOperationNormsData(String year, String plantId) {
		List<Object[]> obj = normalOperationNormsRepository.findByYearAndPlantFkId(year, UUID.fromString(plantId));
		List<MCUNormsValueDTO> mCUNormsValueDTOList = new ArrayList<>();

		for (Object[] row : obj) {
		    MCUNormsValueDTO mCUNormsValueDTO = new MCUNormsValueDTO();
		    mCUNormsValueDTO.setId(row[0].toString());
		    mCUNormsValueDTO.setSiteFkId(row[1].toString());
		    mCUNormsValueDTO.setPlantFkId(row[2].toString());
		    mCUNormsValueDTO.setVerticalFkId(row[3].toString());
		    mCUNormsValueDTO.setMaterialFkId(row[4].toString());
		    
		    mCUNormsValueDTO.setApril(row[5] != null ? Float.parseFloat(row[5].toString()) : null);
		    mCUNormsValueDTO.setMay(row[6] != null ? Float.parseFloat(row[6].toString()) : null);
		    mCUNormsValueDTO.setJune(row[7] != null ? Float.parseFloat(row[7].toString()) : null);
		    mCUNormsValueDTO.setJuly(row[8] != null ? Float.parseFloat(row[8].toString()) : null);
		    mCUNormsValueDTO.setAugust(row[9] != null ? Float.parseFloat(row[9].toString()) : null);
		    mCUNormsValueDTO.setSeptember(row[10] != null ? Float.parseFloat(row[10].toString()) : null);
		    mCUNormsValueDTO.setOctober(row[11] != null ? Float.parseFloat(row[11].toString()) : null);
		    mCUNormsValueDTO.setNovember(row[12] != null ? Float.parseFloat(row[12].toString()) : null);
		    mCUNormsValueDTO.setDecember(row[13] != null ? Float.parseFloat(row[13].toString()) : null);
		    mCUNormsValueDTO.setJanuary(row[14] != null ? Float.parseFloat(row[14].toString()) : null);
		    mCUNormsValueDTO.setFebruary(row[15] != null ? Float.parseFloat(row[15].toString()) : null);
		    mCUNormsValueDTO.setMarch(row[16] != null ? Float.parseFloat(row[16].toString()) : null);
		    
		    mCUNormsValueDTO.setFinancialYear(row[17].toString());
		    mCUNormsValueDTO.setRemarks(row[18] != null ? row[18].toString() : null);
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
	public List<MCUNormsValueDTO> saveNormalOperationNormsData(List<MCUNormsValueDTO> mCUNormsValueDTOList) {
		
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
			mCUNormsValue.setSiteFkId(UUID.fromString(mCUNormsValueDTO.getSiteFkId()));
			mCUNormsValue.setPlantFkId(UUID.fromString(mCUNormsValueDTO.getPlantFkId()));
			mCUNormsValue.setVerticalFkId(UUID.fromString(mCUNormsValueDTO.getVerticalFkId()));
			mCUNormsValue.setMaterialFkId(UUID.fromString(mCUNormsValueDTO.getMaterialFkId()));
			mCUNormsValue.setFinancialYear(mCUNormsValueDTO.getFinancialYear());
			mCUNormsValue.setRemarks(mCUNormsValueDTO.getRemarks());
			mCUNormsValue.setMcuVersion("V1");
			mCUNormsValue.setUpdatedBy("System");
			normalOperationNormsRepository.save(mCUNormsValue);
		}
		// TODO Auto-generated method stub
		return mCUNormsValueDTOList;
	}

}
