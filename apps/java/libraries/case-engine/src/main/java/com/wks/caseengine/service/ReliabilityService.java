package com.wks.caseengine.service;

import java.util.List;



import com.wks.caseengine.dto.ReliabilityPerformanceDto;
import com.wks.caseengine.dto.ReliabilityRecordDto;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ReliabilityService {
	
	
	public AOPMessageVM getReliabilityPerformance(String plantId, String year,String type);
	public AOPMessageVM getReliabilityRecords(String plantId, String year,String type);
	public AOPMessageVM updateReliabilityPerformance(List<ReliabilityPerformanceDto> reliabilityPerformanceDtos);
	public AOPMessageVM updateReliabilityRecords(List<ReliabilityRecordDto> reliabilityRecordDtos);
}
