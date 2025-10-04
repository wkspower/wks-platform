 package com.wks.caseengine.service;

 import java.util.List;

 import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.message.vm.AOPMessageVM;

 public interface NormParametersService   {
	
 	public List<NormParameters> findAllByType(String type);

    public List<NormParameters> getAllGrades(String plantId);
    
    public AOPMessageVM getNormParameters(String plantId,String year,String type);

 }
