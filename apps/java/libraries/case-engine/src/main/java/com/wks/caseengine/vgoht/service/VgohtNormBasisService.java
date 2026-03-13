package com.wks.caseengine.vgoht.service;

import java.util.UUID;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface VgohtNormBasisService {

    	public AOPMessageVM getConfigurationData(String year, UUID plantFKId,String version);

}
