 package com.wks.caseengine.service;

 import java.util.List;

 import com.wks.caseengine.entity.NormParameters;

 public interface NormParametersService   {
	
 	public List<NormParameters> findAllByType(String type);

    public List<NormParameters> getAllGrades(String plantId);

 }
