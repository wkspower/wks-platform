package com.mmc.bpm.cases.instance;

import java.util.List;

public interface CaseInstanceService {

	public CaseInstance create(final List<CaseAttribute> attributes) throws Exception;

	public List<CaseInstance> find() throws Exception;

	public void delete(String businessKey) throws CaseInstanceNotFoundException, Exception;
}
