package com.wks.caseengine.bpm;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.BpmEngine;
import com.wks.caseengine.repository.Repository;

@Component
public class BpmEngineServiceImpl implements BpmEngineService {

	@Autowired
	private Repository<BpmEngine> repository;

	@Override
	public BpmEngine save(BpmEngine bpmEngine) throws Exception {
		repository.save(bpmEngine);
		return bpmEngine;
	}

	@Override
	public BpmEngine get(String id) throws Exception {
		return repository.get(id);
	}

	@Override
	public List<BpmEngine> find() throws Exception {
		return repository.find();
	}

	@Override
	public void delete(String id) throws Exception {
		repository.delete(id);

	}

}
