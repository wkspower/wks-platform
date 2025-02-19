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
	
	
	
}
