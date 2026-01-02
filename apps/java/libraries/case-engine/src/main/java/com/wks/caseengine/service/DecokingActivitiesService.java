package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.CrackerConfigurationDTO;
import com.wks.caseengine.dto.DecokeRunLengthDTO;
import com.wks.caseengine.dto.DecokingActivitiesDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface DecokingActivitiesService {
	
	public AOPMessageVM getDecokingActivitiesData( String year, String plantId,String reportType);
	public AOPMessageVM getDecokingActivitiesIBRData( String year, String plantId,String reportType);
	public AOPMessageVM updateDecokingActivitiesData( String year, String plantId, String reportType, List<DecokingActivitiesDTO> decokingActivitiesDTOList);
	public AOPMessageVM updateDecokingActivitiesIBRData( String year, String plantId, String reportType, List<Map<String, Object>> payloadList);
    public byte[] createExcel(String year, String plantId, String reportType, boolean isAfterSave, List<Map<String, Object>> dynamicDataList);
	public AOPMessageVM updateDecokingActivitiesRunLengthData( String year, String plantId, String reportType,List<Map<String, Object>> payloadList);
    public AOPMessageVM importExcel(String year, UUID fromString, String reportType, MultipartFile file);
	public AOPMessageVM calculateDecokingActivities(String plantId,String year);
	public AOPMessageVM getNextYearEntry(String plantId,String year, String H10, String H11, String H12,String H13, String H14,String startDate);
	public AOPMessageVM getNextYearConfiguration(String plantId,String year,String startDate);
	public AOPMessageVM calculateData(String plantId, String year);
	
	}
