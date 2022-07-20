package com.mmc.bpm.repository;

import java.util.ArrayList;
import java.util.List;

import com.mmc.bpm.cases.instance.CaseInstance;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InMemoryDataRepository implements DataRepository {

	private List<CaseInstance> caseInstances = new ArrayList<>();

	@Override
	public List<CaseInstance> findCaseInstances() {
		return caseInstances;
	}

	public void saveCaseInstance(final CaseInstance caseInstance) {
		caseInstances.add(caseInstance);
	}

	@Override
	public void delete(final CaseInstance caseInstance) {
		caseInstances.remove(caseInstance);
	}

}
