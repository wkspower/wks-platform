package com.wks.caseengine.dto;

import java.util.Date;

import org.springframework.context.annotation.Configuration;

@Configuration
public interface BusinessDemandDataProjection {
	
	 String getId();
	    String getRemark();
	    Float getJan();
	    Float getFeb();
	    Float getMarch();
	    Float getApril();
	    Float getMay();
	    Float getJune();
	    Float getJuly();
	    Float getAug();
	    Float getSep();
	    Float getOct();
	    Float getNov();
	    Float getDec();
	    String getYear();
	    String getPlantId();
	    String getNormParameterId();
	    Float getAvgTph();
	    Integer getDisplayOrder();
	    String getNormParameterTypeId();
	    String getNormParameterTypeName();
	    String getNormParameterTypeDisplayName();
	    Date getCreatedOn();
	    Date getModifiedOn();
	    String getUpdatedBy();
	    Boolean getIsDeleted();
	    Integer getMaterialDisplayOrder();
	    String getSiteFKId();
	    String getVerticalFKId();

}
