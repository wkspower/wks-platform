package com.wks.caseengine.rest.model;

import java.time.LocalDateTime;

public class FaultDetail {
	private String faultTitle;
    private String faultDescription;
    private LocalDateTime faultStartTimeDate;
    private LocalDateTime faultEndTimeDate;
	public String getFaultTitle() {
		return faultTitle;
	}
	public void setFaultTitle(String faultTitle) {
		this.faultTitle = faultTitle;
	}
	public String getFaultDescription() {
		return faultDescription;
	}
	public void setFaultDescription(String faultDescription) {
		this.faultDescription = faultDescription;
	}
	public LocalDateTime getFaultStartTimeDate() {
		return faultStartTimeDate;
	}
	public void setFaultStartTimeDate(LocalDateTime faultStartTimeDate) {
		this.faultStartTimeDate = faultStartTimeDate;
	}
	public LocalDateTime getFaultEndTimeDate() {
		return faultEndTimeDate;
	}
	public void setFaultEndTimeDate(LocalDateTime faultEndTimeDate) {
		this.faultEndTimeDate = faultEndTimeDate;
	}
}
