package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.MCUNormsValueDTO;

public interface ShutdownNormsService {
	
	public List<MCUNormsValueDTO> getShutdownNormsData( String year, String plantId);
	public List<MCUNormsValueDTO> saveShutdownNormsData( List<MCUNormsValueDTO> mCUNormsValueDTOList);

}
