package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.MCUNormsValueDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface NormalOperationNormsService {
	
	public AOPMessageVM getNormalOperationNormsData( String year, String plantId,String gradeId,String mode);
	public AOPMessageVM getSteadyStateNorms( String year, String plantId,String gradeId,String mode);
	byte[] exportSteadyStateNormsDynamic(String year, String plantId, boolean isAfterSave, List<Map<String, Object>> dynamicData);
	AOPMessageVM importSteadyStateNorms(String year, String plantId, MultipartFile file);
	public AOPMessageVM updateSteadyStateNorms( String plantId,  String year, List<Map<String, Object>> payloadList);
	public AOPMessageVM updateAOPConsumptionNormGrade( String plantId,  String year, List<Map<String, Object>> payloadList);
	public List<MCUNormsValueDTO> saveNormalOperationNormsData( List<MCUNormsValueDTO> mCUNormsValueDTOList, UUID plantFKId, String year,String gradeId, boolean isFromExcel);
	public AOPMessageVM calculateExpressionConsumptionNorms(String year,String plantId);
	public AOPMessageVM loadGradeWiseConsumptionNorms(String year,String plantId);
	public AOPMessageVM calculateNormalOpsNorms(String aopYear, String plantId, String siteId, String verticalId);
	AOPMessageVM getNormsTransaction(String plantId, String aopYear);
	AOPMessageVM getNormsTransactionFinalNormsModeWise(String plantId, String aopYear);
	AOPMessageVM getNormsTransactionFinalNorms(String plantId, String aopYear);
	public byte[] createExcel(String year, UUID plantFKId,boolean isAfterSave,List<MCUNormsValueDTO> dtoList,String mode,String gradeId);
	public byte[] createExcelSAP(String year, UUID plantFKId,boolean isAfterSave,List<MCUNormsValueDTO> dtoList,String mode,String gradeId);
	public byte[] createExcelWithSapCode(String year, UUID plantFKId,boolean isAfterSave,List<MCUNormsValueDTO> dtoList,String mode,String gradeId);
	public byte[] exportSteadyStateNormsChemical(String year, UUID plantFKId,boolean isAfterSave,List<MCUNormsValueDTO> dtoList);
	public byte[] exportSteadyStateNorms(String year, UUID plantFKId,boolean isAfterSave,List<MCUNormsValueDTO> dtoList,String mode);
    public AOPMessageVM importExcel(String year, UUID fromString,String gradeId, MultipartFile file,String mode);
    public AOPMessageVM importExcelSAP(String year, UUID fromString,String gradeId, MultipartFile file,String mode);
	public AOPMessageVM importExcelWithSapCode(String year, UUID plantFKId, String gradeId, MultipartFile file, String mode);
    public AOPMessageVM importChemicalExcel(String year, UUID fromString, MultipartFile file);
    public AOPMessageVM getNormalOperationNormsGrades(String year,String plantId);
	// public int getCalculatedNormalOpsNorms( String year, String plantId);
	public AOPMessageVM saveNormalOperationNormsDataPolyester(List<MCUNormsValueDTO> mCUNormsValueDTOList,
			UUID fromString, String year, String gradeId, boolean b);
	public AOPMessageVM getCatChemCalculationData(String plantId, String year);
	public AOPMessageVM saveCatChemCalculationData(String plantId, String year, List<Map<String, Object>> payload);
		public AOPMessageVM importExcelPolyester(String year, UUID fromString, String gradeId, MultipartFile file,
			String mode);
	public AOPMessageVM checkAllGradeNormsPolyester(UUID fromString, String year, String gradeId);
	public AOPMessageVM calculateNormalOpsNormsPolyester(String aopYear, String plantId, String siteId, String verticalId);


}
