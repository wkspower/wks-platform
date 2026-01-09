package com.wks.caseengine.dto.tcs;


import java.util.Date;
import java.util.UUID;

public interface TCSCPPUnitsSDPlanProjection {

        UUID getId();
        String getMachine();
        Date getIBRDueDate();
        String getGTMaintenance();
        Integer getNoOfDays();
        Date getShutDownDate();
        Date getStartUpDate();
        String getMajorJobs();
       
}
