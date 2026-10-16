package com.wks.caseengine.merox.service;

import java.util.List;

import com.wks.caseengine.merox.dto.SignOffReportDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface SignOffReportService {

    AOPMessageVM getSignOffReport(String plantId, String aopYear, String periodFrom, String periodTo);

    AOPMessageVM saveSignOffReportRemarks(String year, List<SignOffReportDTO> signOffReportDTOList);

}
