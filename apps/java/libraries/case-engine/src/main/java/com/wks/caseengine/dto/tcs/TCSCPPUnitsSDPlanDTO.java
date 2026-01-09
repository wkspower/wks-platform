package com.wks.caseengine.dto.tcs;


import java.util.Date;
import java.util.UUID;



import lombok.Data;

@Data
public class TCSCPPUnitsSDPlanDTO {
     
    private UUID id;
    private String machine;
    //@JsonFormat(pattern = "dd-MM-yyyy")
    private Date ibrDueDate;
    private String gtMaintenance;
    private Integer noOfDays;
    //@JsonFormat(pattern = "dd-MM-yyyy")
    private Date shutDownDate;
    //@JsonFormat(pattern = "dd-MM-yyyy")
    private Date startUpDate;
    private String majorJobs;
    
}
