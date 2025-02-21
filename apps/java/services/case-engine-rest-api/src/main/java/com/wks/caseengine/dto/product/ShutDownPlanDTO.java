package com.wks.caseengine.dto.product;

import java.util.Date;
import java.util.UUID;

public class ShutDownPlanDTO {

    private String discription;
    private Date maintStartDateTime;
    private Date maintEndDateTime;
	// Change from Integer to Long
    private Long durationInMins; 
    
	//FOR ID : pmt.Id
	private UUID maintenanceId; 
	

	private Double rate;
	private String remark;
	private String product;
	private String maintenanceTypeName;
	private Double durationInHrs;
	private Double durationInDays;


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


	// Changed return type to Long
    public Long getDurationInMins() { 
        return durationInMins;
    }

	// Changed parameter type to Long
    public void setDurationInMins(Long durationInMins) { 
		this.durationInMins = durationInMins;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public Double getRate() {
		return rate;
	}
	public void setRate(Double rate) {
		this.rate = rate;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getMaintenanceTypeName() {
		return maintenanceTypeName;
	}
	public void setMaintenanceTypeName(String maintenanceTypeName) {
		this.maintenanceTypeName = maintenanceTypeName;
	}
	public Double getDurationInHrs() {
		return durationInHrs;
	}
	public void setDurationInHrs(Double durationInHrs) {
		this.durationInHrs = durationInHrs;
	}
	public Double getDurationInDays() {
		return durationInDays;
	}
	public void setDurationInDays(Double durationInDays) {
		this.durationInDays = durationInDays;
	}
	 public void setMaintenanceId(UUID maintenanceId) { 
        this.maintenanceId = maintenanceId;
    }
	public UUID getMaintenanceId() { 
        return maintenanceId;
    }

	@Override
	public String toString() {
		return "ShutDownPlanDTO [discription=" + discription + ", maintStartDateTime=" + maintStartDateTime
				+ ", maintEndDateTime=" + maintEndDateTime + ", durationInMins=" + durationInMins + ", maintenanceId="
				+ maintenanceId + ", rate=" + rate + ", remark=" + remark + ", product=" + product
				+ ", maintenanceTypeName=" + maintenanceTypeName + ", durationInHrs=" + durationInHrs
				+ ", durationInDays=" + durationInDays + "]";
	}


	
}
