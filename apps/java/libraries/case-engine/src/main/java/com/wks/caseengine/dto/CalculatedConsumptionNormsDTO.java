package com.wks.caseengine.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CalculatedConsumptionNormsDTO {
	
	 private UUID id;
	    private UUID siteFkId;
	    private UUID verticalFkId;
	    private String aopCaseId;
	    private String aopStatus;
	    private String aopRemarks;
	    private UUID materialFkId;
	    private Double jan;
	    private Double feb;
	    private Double march;
	    private Double april;
	    private Double may;
	    private Double june;
	    private Double july;
	    private Double aug;
	    private Double sep;
	    private Double oct;
	    private Double nov;
	    private Double dec;
	    private String aopYear;
	    private UUID plantFkId;
	    private String normParameterTypeDisplayName;
	    
	    @JsonProperty("UOM") 
	    private String uom;
	    
	    
		public UUID getId() {
			return id;
		}
		public void setId(UUID id) {
			this.id = id;
		}
		public UUID getSiteFkId() {
			return siteFkId;
		}
		public void setSiteFkId(UUID siteFkId) {
			this.siteFkId = siteFkId;
		}
		public UUID getVerticalFkId() {
			return verticalFkId;
		}
		public void setVerticalFkId(UUID verticalFkId) {
			this.verticalFkId = verticalFkId;
		}
		public String getAopCaseId() {
			return aopCaseId;
		}
		public void setAopCaseId(String aopCaseId) {
			this.aopCaseId = aopCaseId;
		}
		public String getAopStatus() {
			return aopStatus;
		}
		public void setAopStatus(String aopStatus) {
			this.aopStatus = aopStatus;
		}
		public String getAopRemarks() {
			return aopRemarks;
		}
		public void setAopRemarks(String aopRemarks) {
			this.aopRemarks = aopRemarks;
		}
		public UUID getMaterialFkId() {
			return materialFkId;
		}
		public void setMaterialFkId(UUID materialFkId) {
			this.materialFkId = materialFkId;
		}
		public Double getJan() {
			return jan;
		}
		public void setJan(Double jan) {
			this.jan = jan;
		}
		public Double getFeb() {
			return feb;
		}
		public void setFeb(Double feb) {
			this.feb = feb;
		}
		public Double getMarch() {
			return march;
		}
		public void setMarch(Double march) {
			this.march = march;
		}
		public Double getApril() {
			return april;
		}
		public void setApril(Double april) {
			this.april = april;
		}
		public Double getMay() {
			return may;
		}
		public void setMay(Double may) {
			this.may = may;
		}
		public Double getJune() {
			return june;
		}
		public void setJune(Double june) {
			this.june = june;
		}
		public Double getJuly() {
			return july;
		}
		public void setJuly(Double july) {
			this.july = july;
		}
		public Double getAug() {
			return aug;
		}
		public void setAug(Double aug) {
			this.aug = aug;
		}
		public Double getSep() {
			return sep;
		}
		public void setSep(Double sep) {
			this.sep = sep;
		}
		public Double getOct() {
			return oct;
		}
		public void setOct(Double oct) {
			this.oct = oct;
		}
		public Double getNov() {
			return nov;
		}
		public void setNov(Double nov) {
			this.nov = nov;
		}
		public Double getDec() {
			return dec;
		}
		public void setDec(Double dec) {
			this.dec = dec;
		}
		public String getAopYear() {
			return aopYear;
		}
		public void setAopYear(String aopYear) {
			this.aopYear = aopYear;
		}
		public UUID getPlantFkId() {
			return plantFkId;
		}
		public void setPlantFkId(UUID plantFkId) {
			this.plantFkId = plantFkId;
		}
		public String getNormParameterTypeDisplayName() {
			return normParameterTypeDisplayName;
		}
		public void setNormParameterTypeDisplayName(String normParameterTypeDisplayName) {
			this.normParameterTypeDisplayName = normParameterTypeDisplayName;
		}
		public String getUOM() {
			return uom;
		}
		public void setUOM(String uOM) {
			uom = uOM;
		}
	    
	    
	    

}
