package com.wks.caseengine.dto;

import java.util.Map;

public class NormAttributeTransactionReceipeRequestDTO {
	
	  private String recId;
	    private Map<String, String> grades;
	    private String saveStatus;
	    private String errDescription;
	    
		public String getRecId() {
			return recId;
		}
		public void setRecId(String recId) {
			this.recId = recId;
		}
		public Map<String, String> getGrades() {
			return grades;
		}
		public void setGrades(Map<String, String> grades) {
			this.grades = grades;
		}
		public String getSaveStatus() {
			return saveStatus;
		}
		public void setSaveStatus(String saveStatus) {
			this.saveStatus = saveStatus;
		}
		public String getErrDescription() {
			return errDescription;
		}
		public void setErrDescription(String errDescription) {
			this.errDescription = errDescription;
		}
	    
		
	    

	    
	    
	    

}
