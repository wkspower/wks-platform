package com.wks.caseengine.vgoht.service;

import java.util.UUID;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface VgohtNormBasisService {

	public AOPMessageVM getConfigurationData(String year, UUID plantFKId,String version);
	
	public AOPMessageVM getConfigurationConstants(String year,String plantFKId);

    public AOPMessageVM LoadButtonNormCalculation(UUID plantId, String aopYear, UUID siteId, String periodFrom, String periodTo);


}
