package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.ShutdownNormsValueDTO;
import com.wks.caseengine.entity.MCUNormsValue;
import com.wks.caseengine.entity.ShutdownNormsValue;
import com.wks.caseengine.repository.ShutdownNormsRepository;

@Service
public class ShutdownNormsServiceImpl implements ShutdownNormsService{
	
	@Autowired
	private ShutdownNormsRepository shutdownNormsRepository;
	
	@Override
	public List<ShutdownNormsValueDTO> getShutdownNormsData(String year, String plantId) {
		List<Object[]> objList = shutdownNormsRepository.findByYearAndPlantFkId(year, UUID.fromString(plantId));
		System.out.println("obj.size(): " + objList.size());
		List<ShutdownNormsValueDTO> shutdownNormsValueDTOList = new ArrayList<>();
		for (Object[] row : objList) {
			ShutdownNormsValueDTO shutdownNormsValueDTO = new ShutdownNormsValueDTO();
			shutdownNormsValueDTO.setId(row[0] != null ? row[0].toString() : null);
			shutdownNormsValueDTO.setSiteFkId(row[1] != null ? row[1].toString() : null);
			shutdownNormsValueDTO.setPlantFkId(row[2] != null ? row[2].toString() : null);
			shutdownNormsValueDTO.setVerticalFkId(row[3] != null ? row[3].toString() : null);
			shutdownNormsValueDTO.setMaterialFkId(row[4] != null ? row[4].toString() : null);
			shutdownNormsValueDTO.setApril(row[5] != null ? Float.parseFloat(row[5].toString()) : null);
			shutdownNormsValueDTO.setMay(row[6] != null ? Float.parseFloat(row[6].toString()) : null);
			shutdownNormsValueDTO.setJune(row[7] != null ? Float.parseFloat(row[7].toString()) : null);
			shutdownNormsValueDTO.setJuly(row[8] != null ? Float.parseFloat(row[8].toString()) : null);
			shutdownNormsValueDTO.setAugust(row[9] != null ? Float.parseFloat(row[9].toString()) : null);
			shutdownNormsValueDTO.setSeptember(row[10] != null ? Float.parseFloat(row[10].toString()) : null); 
			shutdownNormsValueDTO.setOctober(row[11] != null ? Float.parseFloat(row[11].toString()) : null);
			shutdownNormsValueDTO.setNovember(row[12] != null ? Float.parseFloat(row[12].toString()) : null);
			shutdownNormsValueDTO.setDecember(row[13] != null ? Float.parseFloat(row[13].toString()) : null);
			shutdownNormsValueDTO.setJanuary(row[14] != null ? Float.parseFloat(row[14].toString()) : null);
			shutdownNormsValueDTO.setFebruary(row[15] != null ? Float.parseFloat(row[15].toString()) : null);
			shutdownNormsValueDTO.setMarch(row[16] != null ? Float.parseFloat(row[16].toString()) : null);
		    
			shutdownNormsValueDTO.setFinancialYear(row[17] != null ? row[17].toString() : null);
			shutdownNormsValueDTO.setRemarks(row[18] != null ? row[18].toString() : " ");
			shutdownNormsValueDTO.setCreatedOn(row[19] != null ? (Date) row[19] : null);
			shutdownNormsValueDTO.setModifiedOn(row[20] != null ? (Date) row[20] : null);
			shutdownNormsValueDTO.setMcuVersion(row[21] != null ? row[21].toString() : null);
			shutdownNormsValueDTO.setUpdatedBy(row[22] != null ? row[22].toString() : null);
			shutdownNormsValueDTO.setNormParameterTypeId(row[23] != null ? row[23].toString() : null);
			shutdownNormsValueDTO.setNormParameterTypeName(row[24] != null ? row[24].toString() : null);
			shutdownNormsValueDTO.setNormParameterTypeDisplayName(row[25] != null ? row[25].toString() : null);
			
			
			// shutdownNormsValueDTO.setNormTypeDisplayOrder(row[26] != null ? row[26].toString() : null);
			// shutdownNormsValueDTO.setMaterialDisplayOrder(row[27] != null ? row[27].toString() : null);
			
			
			shutdownNormsValueDTO.setUOM(row[28] != null ? row[28].toString() : null);
		
			shutdownNormsValueDTOList.add(shutdownNormsValueDTO);
		}

		return shutdownNormsValueDTOList;
	}
	@Override
	public List<ShutdownNormsValueDTO> saveShutdownNormsData(List<ShutdownNormsValueDTO> shutdownNormsValueDTOList) {
		
		for (ShutdownNormsValueDTO shutdownNormsValueDTO : shutdownNormsValueDTOList) {
			ShutdownNormsValue shutdownNormsValue = new ShutdownNormsValue();
			if (shutdownNormsValueDTO.getId() != null && !shutdownNormsValueDTO.getId().isEmpty()) {
				shutdownNormsValue.setId(UUID.fromString(shutdownNormsValueDTO.getId()));
				shutdownNormsValue.setModifiedOn(new Date());
			}else {
				shutdownNormsValue.setCreatedOn(new Date());
			}
			shutdownNormsValue.setApril(Optional.ofNullable(shutdownNormsValueDTO.getApril()).orElse(0.0f));
			shutdownNormsValue.setMay(Optional.ofNullable(shutdownNormsValueDTO.getMay()).orElse(0.0f));
			shutdownNormsValue.setJune(Optional.ofNullable(shutdownNormsValueDTO.getJune()).orElse(0.0f));
			shutdownNormsValue.setJuly(Optional.ofNullable(shutdownNormsValueDTO.getJuly()).orElse(0.0f));
			shutdownNormsValue.setAugust(Optional.ofNullable(shutdownNormsValueDTO.getAugust()).orElse(0.0f));
			shutdownNormsValue.setSeptember(Optional.ofNullable(shutdownNormsValueDTO.getSeptember()).orElse(0.0f));
			shutdownNormsValue.setOctober(Optional.ofNullable(shutdownNormsValueDTO.getOctober()).orElse(0.0f));
			shutdownNormsValue.setNovember(Optional.ofNullable(shutdownNormsValueDTO.getNovember()).orElse(0.0f));
			shutdownNormsValue.setDecember(Optional.ofNullable(shutdownNormsValueDTO.getDecember()).orElse(0.0f));
			shutdownNormsValue.setJanuary(Optional.ofNullable(shutdownNormsValueDTO.getJanuary()).orElse(0.0f));
			shutdownNormsValue.setFebruary(Optional.ofNullable(shutdownNormsValueDTO.getFebruary()).orElse(0.0f));
			shutdownNormsValue.setMarch(Optional.ofNullable(shutdownNormsValueDTO.getMarch()).orElse(0.0f));
			if (shutdownNormsValueDTO.getSiteFkId() != null) {
				shutdownNormsValue.setSiteFkId(UUID.fromString(shutdownNormsValueDTO.getSiteFkId()));
			}
			if (shutdownNormsValueDTO.getPlantFkId() != null) {
				shutdownNormsValue.setPlantFkId(UUID.fromString(shutdownNormsValueDTO.getPlantFkId()));
			}
			if (shutdownNormsValueDTO.getVerticalFkId() != null) {
				shutdownNormsValue.setVerticalFkId(UUID.fromString(shutdownNormsValueDTO.getVerticalFkId()));
			}
			if (shutdownNormsValueDTO.getMaterialFkId() != null) {
				shutdownNormsValue.setMaterialFkId(UUID.fromString(shutdownNormsValueDTO.getMaterialFkId()));
			}
			if (shutdownNormsValueDTO.getNormParameterTypeId() != null) {
				shutdownNormsValue
						.setNormParameterTypeFkId(UUID.fromString(shutdownNormsValueDTO.getNormParameterTypeId()));
			}
			
			shutdownNormsValue.setFinancialYear(shutdownNormsValueDTO.getFinancialYear());
			shutdownNormsValue.setRemarks(shutdownNormsValueDTO.getRemarks());
			shutdownNormsValue.setMcuVersion("V1");
			shutdownNormsValue.setUpdatedBy("System");
			
			System.out.println("Data Saved Succussfully");
			shutdownNormsRepository.save(shutdownNormsValue);
		}
		// TODO Auto-generated method stub
		return shutdownNormsValueDTOList;
	}


}
