package com.mmc.bpm.client.cases.instance;

import java.util.List;

import com.mmc.bpm.engine.model.spi.BusinessKey;

public interface CaseInstanceService {

	public CaseInstance create(final String attributes);
	
	public List<CaseInstance> find();
	
	public void delete(BusinessKey businessKey);
}
