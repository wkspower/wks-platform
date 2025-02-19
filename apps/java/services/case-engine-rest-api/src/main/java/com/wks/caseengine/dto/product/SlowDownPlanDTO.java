package com.wks.caseengine.dto.product;

import java.util.Date;
import java.util.UUID;

public class SlowDownPlanDTO {

    private String discription;
    private Date maintStartDateTime;
    private Date maintEndDateTime;
    private Long durationInMins; 
    private Double rate;  // Added field for Rate
    private String remarks; // Added field for Remarks
    private String product;
    private UUID maintenanceId;

    public String getDiscription() {
        return discription;
    }

    public void setDiscription(String discription) {
        this.discription = discription;
    }

    public Date getMaintStartDateTime() {
        return maintStartDateTime;
    }

    public void setMaintStartDateTime(Date maintStartDateTime) {
        this.maintStartDateTime = maintStartDateTime;
    }

    public Date getMaintEndDateTime() {
        return maintEndDateTime;
    }

    public void setMaintEndDateTime(Date maintEndDateTime) {
        this.maintEndDateTime = maintEndDateTime;
    }

    public Long getDurationInMins() { 
        return durationInMins;
    }

    public void setDurationInMins(Long durationInMins) { 
        this.durationInMins = durationInMins;
    }

    public Double getRate() { 
        return rate;
    }

    public void setRate(Double rate) { 
        this.rate = rate;
    }

    public String getRemarks() { 
        return remarks;
    }

    public void setRemarks(String remarks) { 
        this.remarks = remarks;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public UUID getMaintenanceId() { 
        return maintenanceId;
    }

    public void setMaintenanceId(UUID maintenanceId) { 
        this.maintenanceId = maintenanceId;
    }
}

