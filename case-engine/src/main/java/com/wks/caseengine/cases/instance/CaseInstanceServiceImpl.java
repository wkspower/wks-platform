package com.wks.caseengine.cases.instance;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.process.instance.ProcessInstanceService;
import com.wks.caseengine.repository.CaseInstanceRepository;
import com.wks.caseengine.repository.Repository;

@Component
public class CaseInstanceServiceImpl implements CaseInstanceService {

	@Autowired
	private CaseInstanceRepository repository;

	@Autowired
	private Repository<CaseDefinition> caseDefRepository;

	@Autowired
	private CaseInstanceCreateService caseInstanceCreateService;

	@Autowired
	private ProcessInstanceService processInstanceService;

	@Override
	public List<CaseInstance> find(final Optional<CaseStatus> status, final Optional<String> caseDefinitionId)
			throws Exception {
		return repository.findCaseInstances(status, caseDefinitionId);
	}

	@Override
	public CaseInstance get(final String businessKey) throws Exception {
		return repository.get(businessKey);
	}

	@Override
	// TODO how to embrace in a single transation?
	public CaseInstance create(CaseInstance caseInstance) throws Exception {

		CaseDefinition caseDefinition = caseDefRepository.get(caseInstance.getCaseDefinitionId());

		CaseInstance newCaseInstance = caseInstanceCreateService.create(caseInstance);

		processInstanceService.create(caseDefinition.getStagesLifecycleProcessKey(), newCaseInstance.getBusinessKey(),
				newCaseInstance.getAttributes(), caseDefinition.getBpmEngineId());

		return newCaseInstance;
	}

	// TODO Should be a generic update?
	@Override
	public void updateStatus(final String businessKey, final CaseStatus newStatus) throws Exception {
		CaseInstance caseInstance = repository.get(businessKey);
		caseInstance.setStatus(newStatus);
		repository.update(businessKey, caseInstance);
	}

	// TODO Should replace by 'Stage ID/Key' parameter instead of 'Stage Name'
	@Override
	public void updateStage(final String businessKey, String caseStage) throws Exception {
		CaseInstance caseInstance = repository.get(businessKey);
		caseInstance.setStage(caseStage);
		repository.update(businessKey, caseInstance);
	}

	// TODO should not allow to delete. Close or archive instead
	// Should ensure only one case is deleted - BusinessKey should be UNIQUE
	@Override
	public void delete(final String businessKey) throws Exception {
		List<CaseInstance> caseInstanceList = repository.find().stream()
				.filter(o -> o.getBusinessKey().equals(businessKey)).collect(Collectors.toList());

		if (caseInstanceList.isEmpty()) {
			throw new CaseInstanceNotFoundException();
		}

		// TODO close/archive process in PostClose/Archive hook

		caseInstanceList.forEach(o -> {
			try {
				repository.delete(o.getBusinessKey());
			} catch (Exception e) {
				// TODO error handling
				e.printStackTrace();
			}
		});
	}

	public void setRepository(CaseInstanceRepository repository) {
		this.repository = repository;
	}

	public void setCaseInstanceCreateService(CaseInstanceCreateService caseInstanceCreateService) {
		this.caseInstanceCreateService = caseInstanceCreateService;
	}

}
