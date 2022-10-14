package com.wks.caseengine.cases.instance;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.process.instance.ProcessInstanceService;
import com.wks.caseengine.repository.DataRepository;

@Component
public class CaseInstanceServiceImpl implements CaseInstanceService {

	@Autowired
	private DataRepository dataRepository;

	@Autowired
	private CaseInstanceCreateService caseInstanceCreateService;

	@Autowired
	private ProcessInstanceService processInstanceService;

	@Override
	public List<CaseInstance> find(final Optional<CaseStatus> status) throws Exception {
		return dataRepository.findCaseInstances(status);
	}

	@Override
	public CaseInstance get(final String businessKey) throws Exception {
		return dataRepository.getCaseInstance(businessKey);
	}

	@Override
	// TODO how to embrace in a single transation?
	public CaseInstance create(CaseInstance caseInstance) throws Exception {

		CaseDefinition caseDefinition = dataRepository.getCaseDefinition(caseInstance.getCaseDefinitionId());

		CaseInstance newCaseInstance = caseInstanceCreateService.create(caseInstance);

		processInstanceService.create(caseDefinition.getStagesLifecycleProcessKey(), newCaseInstance.getBusinessKey());

		return newCaseInstance;
	}

	// TODO Should be a generic update?
	@Override
	public void updateStatus(final String businessKey, final CaseStatus newStatus) throws Exception {
		dataRepository.updateCaseStatus(businessKey, newStatus);
	}

	// TODO Should replace by 'Stage ID/Key' parameter instead of 'Stage Name'
	@Override
	public void updateStage(final String businessKey, String caseStage) throws Exception {
		dataRepository.updateCaseStage(businessKey, caseStage);
	}

	// TODO should not allow to delete. Close or archive instead
	// Should ensure only one case is deleted - BusinessKey should be UNIQUE
	@Override
	public void delete(final String businessKey) throws Exception {
		List<CaseInstance> caseInstanceList = dataRepository.findCaseInstances(Optional.empty()).stream()
				.filter(o -> o.getBusinessKey().equals(businessKey)).collect(Collectors.toList());

		if (caseInstanceList.isEmpty()) {
			throw new CaseInstanceNotFoundException();
		}

		// TODO close/archive process in PostClose/Archive hook

		caseInstanceList.forEach(o -> {
			try {
				dataRepository.deleteCaseInstance(o);
			} catch (Exception e) {
				// TODO error handling
				e.printStackTrace();
			}
		});
	}

	public void setDataRepository(DataRepository dataRepository) {
		this.dataRepository = dataRepository;
	}

	public void setCaseInstanceCreateService(CaseInstanceCreateService caseInstanceCreateService) {
		this.caseInstanceCreateService = caseInstanceCreateService;
	}

}
