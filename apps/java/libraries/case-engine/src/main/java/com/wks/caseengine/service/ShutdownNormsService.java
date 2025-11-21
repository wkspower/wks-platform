package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.MCUNormsValueDTO;
import com.wks.caseengine.dto.ShutdownNormsValueDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ShutdownNormsService {
	
	public AOPMessageVM getShutdownNormsData( String year, String plantId,String gradeId);
	public byte[] exportShutdownNorms(String year, UUID plantFKId,boolean isAfterSave,List<ShutdownNormsValueDTO> dtoList);
	public AOPMessageVM saveShutDownNorms(String plantId,List<ShutdownNormsValueDTO> shutdownNormsValueDTOList);
	public AOPMessageVM getShutdownNormsSPData(String year, String plantId);
	public AOPMessageVM getUniqueGrades(String year, String plantId);
	public AOPMessageVM getShutConsumptionData( String year, String plantId,String gradeId);
	
}
