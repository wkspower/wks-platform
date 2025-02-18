package com.wks.caseengine.dto.product;

import java.util.Date;


public class ShutDownPlanDTO {

	private String discription;
	private Date maintStartDateTime;
	private Date maintEndDateTime;
	private Long durationInMins;
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
	public Long getDurationInMins() {
		return durationInMins;
	}
	public void setDurationInMins(Long durationInMins) {
		this.durationInMins = durationInMins;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	
	
	
}
