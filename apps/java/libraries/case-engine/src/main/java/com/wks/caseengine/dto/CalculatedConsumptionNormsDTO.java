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
	    private float jan;
	    private float feb;
	    private float march;
	    private float april;
	    private float may;
	    private float june;
	    private float july;
	    private float aug;
	    private float sep;
	    private float oct;
	    private float nov;
	    private float dec;
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
		public float getJan() {
			return jan;
		}
		public void setJan(float jan) {
			this.jan = jan;
		}
		public float getFeb() {
			return feb;
		}
		public void setFeb(float feb) {
			this.feb = feb;
		}
		public float getMarch() {
			return march;
		}
		public void setMarch(float march) {
			this.march = march;
		}
		public float getApril() {
			return april;
		}
		public void setApril(float april) {
			this.april = april;
		}
		public float getMay() {
			return may;
		}
		public void setMay(float may) {
			this.may = may;
		}
		public float getJune() {
			return june;
		}
		public void setJune(float june) {
			this.june = june;
		}
		public float getJuly() {
			return july;
		}
		public void setJuly(float july) {
			this.july = july;
		}
		public float getAug() {
			return aug;
		}
		public void setAug(float aug) {
			this.aug = aug;
		}
		public float getSep() {
			return sep;
		}
		public void setSep(float sep) {
			this.sep = sep;
		}
		public float getOct() {
			return oct;
		}
		public void setOct(float oct) {
			this.oct = oct;
		}
		public float getNov() {
			return nov;
		}
		public void setNov(float nov) {
			this.nov = nov;
		}
		public float getDec() {
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
