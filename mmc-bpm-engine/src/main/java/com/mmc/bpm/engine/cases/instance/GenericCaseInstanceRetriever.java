package com.mmc.bpm.engine.cases.instance;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmc.bpm.engine.repository.DataRepository;

@Component
public class GenericCaseInstanceRetriever implements CaseInstanceRetriever {

	@Autowired
	private DataRepository dataRepository;

	@Override
	public List<CaseInstance> find() {
		return dataRepository.findCaseInstances();
	}

}
