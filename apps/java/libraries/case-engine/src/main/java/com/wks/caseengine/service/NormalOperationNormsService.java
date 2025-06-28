package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.MCUNormsValueDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface NormalOperationNormsService {
	
	public AOPMessageVM getNormalOperationNormsData( String year, String plantId);
	public List<MCUNormsValueDTO> saveNormalOperationNormsData( List<MCUNormsValueDTO> mCUNormsValueDTOList, UUID plantFKId, String year);
	public AOPMessageVM calculateExpressionConsumptionNorms(String year,String plantId);
	public AOPMessageVM calculateNormalOpsNorms(String aopYear, String plantId, String siteId, String verticalId);
	AOPMessageVM getNormsTransaction(String plantId, String aopYear);
	 public byte[] createExcel(String year, UUID plantFKId,boolean isAfterSave,List<MCUNormsValueDTO> dtoList);
    public byte[] importExcel(String year, UUID fromString, MultipartFile file);
    public AOPMessageVM getNormalOperationNormsGrades(String year,String plantId);
	// public int getCalculatedNormalOpsNorms( String year, String plantId);

}
