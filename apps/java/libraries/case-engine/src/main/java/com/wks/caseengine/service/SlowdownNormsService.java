package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.SlowdownNormsValueDTO;

public interface SlowdownNormsService {
	
	public List<SlowdownNormsValueDTO> getSlowdownNormsData( String year, String plantId);
	public List<SlowdownNormsValueDTO> saveSlowdownNormsData( List<SlowdownNormsValueDTO> slowdownNormsValueDTOList);
	public List<SlowdownNormsValueDTO> getSlowdownNormsSPData(String year, String plantId);
	public List getSlowdownMonths(UUID plantId,String maintenanceName);

}
