package com.mmc.bpm.engine.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mmc.bpm.engine.cases.instance.CaseInstance;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class InMemoryDataRepository implements DataRepository {

	private List<CaseInstance> caseInstances = new ArrayList<>();

	public void saveCaseInstance(final CaseInstance caseInstance) {
		caseInstances.add(caseInstance);
	}

	@Override
	public List<CaseInstance> findCaseInstances() {
		return caseInstances;
	}

}
