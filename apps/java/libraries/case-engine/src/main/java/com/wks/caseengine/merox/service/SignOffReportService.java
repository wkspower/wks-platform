package com.wks.caseengine.merox.service;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface SignOffReportService {

    AOPMessageVM getSignOffReport(String plantId, String aopYear, String periodFrom, String periodTo);

}
