package com.wks.caseengine.service;

import java.util.List;
import com.wks.caseengine.dto.ShutdownNormsValueDTO;

public interface ShutdownNormsService {
	
	public List<ShutdownNormsValueDTO> getShutdownNormsData( String year, String plantId);
	public List<ShutdownNormsValueDTO> saveShutdownNormsData( List<ShutdownNormsValueDTO> shutdownNormsValueDTOList);
	public List<ShutdownNormsValueDTO> getShutdownNormsSPData(String year, String plantId);

}
