package com.wks.caseengine.service;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface TurnAroundDataReportService {


    public AOPMessageVM getReportForTurnAroundPlanData(String plantId,String year,String reportType);
    
}
