package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.DecokePlanningIBRDTO;
import com.wks.caseengine.dto.DecokeRunLengthDTO;
import com.wks.caseengine.dto.DecokingActivitiesDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface DecokingActivitiesService {
	
	public AOPMessageVM getDecokingActivitiesData( String year, String plantId,String reportType);
	public AOPMessageVM getDecokingActivitiesIBRData( String year, String plantId,String reportType);
	public AOPMessageVM updateDecokingActivitiesData( String year, String plantId, String reportType, List<DecokingActivitiesDTO> decokingActivitiesDTOList);
	public AOPMessageVM updateDecokingActivitiesIBRData( String year, String plantId, String reportType, List<DecokePlanningIBRDTO> decokePlanningIBRDTOList);
    public byte[] createExcel(String year, String plantId, String reportType, boolean isAfterSave, List<DecokeRunLengthDTO> decokeRunLengthDTOList);
	public AOPMessageVM updateDecokingActivitiesRunLengthData( String year, String plantId, String reportType, List<DecokeRunLengthDTO> decokeRunLengthDTOList);
    public AOPMessageVM importExcel(String year, UUID fromString, String reportType, MultipartFile file);
	public AOPMessageVM calculateDecokingActivities(String plantId,String year);
	}
