package com.wks.caseengine.service;

import java.util.List;
import com.wks.caseengine.dto.TurnAroundPlanReportDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface TurnAroundDataReportService {


    public AOPMessageVM getReportForTurnAroundPlanData(String plantId,String year,String reportType);
    
    public AOPMessageVM updateReportForTurnAroundData( String plantId,String year, List<TurnAroundPlanReportDTO> dataList);
			 
    
}
