package com.wks.caseengine.cases.instance;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.definition.event.CaseEventExecutor;
import com.wks.caseengine.repository.DataRepository;

@Component
public class CaseInstanceServiceImpl implements CaseInstanceService {

	@Autowired
	private DataRepository dataRepository;

	@Autowired
	private CaseInstanceCreateService caseInstanceCreateService;

	@Autowired
	private CaseEventExecutor caseEventExecutor;

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

		caseDefinition.getPostCaseCreateHook().getCaseEvents()
				.forEach(event -> caseEventExecutor.execute(event, newCaseInstance.getBusinessKey()));

		return newCaseInstance;
	}

	// TODO Should be a generic update?
	@Override
	public void updateStatus(final String businessKey, final CaseStatus newStatus) throws Exception {
		dataRepository.updateCaseStatus(businessKey, newStatus);
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

	public void setCaseEventExecutor(CaseEventExecutor caseEventExecutor) {
		this.caseEventExecutor = caseEventExecutor;
	}
}
