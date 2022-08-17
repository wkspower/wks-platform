package com.mmc.bpm.client.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mmc.bpm.client.cases.instance.CaseInstance;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InMemoryDataRepository implements DataRepository {

	private List<CaseInstance> caseInstancesRepo = new ArrayList<>();

	@Override
	public List<CaseInstance> findCaseInstances() {
		return caseInstancesRepo;
	}

	public void saveCaseInstance(final CaseInstance caseInstance) {
		caseInstancesRepo.add(caseInstance);
	}

	@Override
	// TODO test it
	public void updateCaseStatus(String businessKey, String newStatus) throws Exception {
		List<CaseInstance> cases = caseInstancesRepo.stream().filter(o -> businessKey.equals(o.getBusinessKey()))
				.collect(Collectors.toList());
		cases.forEach(o -> o.setStatus(newStatus));
	}

	@Override
	public void delete(final CaseInstance caseInstance) {
		caseInstancesRepo.remove(caseInstance);
	}

	@Override
	public CaseInstance getCaseInstance(String businessKey) throws Exception {
		// TODO handle more than 1 result
		return caseInstancesRepo.stream().filter(o -> businessKey.equals(o.getBusinessKey())).findFirst().get();
	}

}
