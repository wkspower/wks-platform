package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.ReliabilityPerformanceDto;
import com.wks.caseengine.dto.ReliabilityRecordDto;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ReliabilityService {
	
	
	public AOPMessageVM getReliabilityPerformance(String plantId, String year,String type);
	byte[] createExcel(String year, String plantId,  boolean isAfterSave,
			Map<String, List<ReliabilityPerformanceDto>> mapForExcel);
	byte[] exportReliabilityRecords(String year, String plantId,  boolean isAfterSave,
			Map<String, List<ReliabilityRecordDto>> mapForExcel);
	AOPMessageVM importExcel(String year, String plantFKId, MultipartFile file);
	AOPMessageVM importReliabilityRecords(String year, String plantFKId, MultipartFile file);
	public AOPMessageVM getReliabilityRecords(String plantId, String year,String type);
	public AOPMessageVM updateReliabilityPerformance(List<ReliabilityPerformanceDto> reliabilityPerformanceDtos);
	public AOPMessageVM updateReliabilityRecords(List<ReliabilityRecordDto> reliabilityRecordDtos);
}
