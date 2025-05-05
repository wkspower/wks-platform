package com.wks.caseengine.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CalculatedConsumptionNormsDTO extends MonthsDTO{
	
	 private UUID id;
	    private UUID siteFkId;
	    private UUID verticalFkId;
	    private String aopCaseId;
	    private String aopStatus;
	    private String aopRemarks;
	    private UUID materialFkId;
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
		public Float getJan() {
			return jan;
		}
		public void setJan(Float jan) {
			this.jan = jan;
		}
		public Float getFeb() {
			return feb;
		}
		public void setFeb(float feb) {
			this.feb = feb;
		}
		public Float getMarch() {
			return march;
		}
		public void setMarch(float march) {
			this.march = march;
		}
		public Float getApril() {
			return april;
		}
		public void setApril(float april) {
			this.april = april;
		}
		public Float getMay() {
			return may;
		}
		public void setMay(float may) {
			this.may = may;
		}
		public Float getJune() {
			return june;
		}
		public void setJune(float june) {
			this.june = june;
		}
		public Float getJuly() {
			return july;
		}
		public void setJuly(float july) {
			this.july = july;
		}
		public Float getAug() {
			return aug;
		}
		public void setAug(float aug) {
			this.aug = aug;
		}
		public Float getSep() {
			return sep;
		}
		public void setSep(float sep) {
			this.sep = sep;
		}
		public Float getOct() {
			return oct;
		}
		public void setOct(float oct) {
			this.oct = oct;
		}
		public Float getNov() {
			return nov;
		}
		public void setNov(float nov) {
			this.nov = nov;
		}
		public Float getDec() {
			return dec;
		}
		public void setDec(float dec) {
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
