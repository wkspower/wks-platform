package com.wks.caseengine.dto.product;

import java.util.Date;


public class ShutDownPlanDTO {

	private String discription;
	private Date maintStartDateTime;
	private Date maintEndDateTime;
	private Integer durationInMins;
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
	public Integer getDurationInMins() {
		return durationInMins;
	}
	public void setDurationInMins(Integer durationInMins) {
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
	
}
