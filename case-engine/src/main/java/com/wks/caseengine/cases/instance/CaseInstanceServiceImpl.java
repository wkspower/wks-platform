package com.wks.caseengine.cases.instance;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.http.client.utils.DateUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.process.instance.ProcessInstanceService;
import com.wks.caseengine.repository.CaseInstanceRepository;
import com.wks.caseengine.repository.Repository;

@Component
public class CaseInstanceServiceImpl implements CaseInstanceService {

	Gson gson = new Gson();

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
		return repository.find(status, caseDefinitionId);
	}

	@Override
	public CaseInstance get(final String businessKey) throws Exception {
		return repository.get(businessKey);
	}

	@Override
	public CaseInstance create(CaseInstance caseInstance) throws Exception {

		caseInstance.getAttributes()
				.add(new CaseAttribute("createdAt", DateUtils.formatDate(new Date(), "dd/MM/yyyy")));

		CaseDefinition caseDefinition = caseDefRepository.get(caseInstance.getCaseDefinitionId());

		CaseInstance newCaseInstance = caseInstanceCreateService.create(caseInstance);

		processInstanceService.create(caseDefinition.getStagesLifecycleProcessKey(), newCaseInstance.getBusinessKey(),
				newCaseInstance.getAttributes());

		return newCaseInstance;
	}

	@Override
	public CaseInstance create(final String caseDefinitionId) throws Exception {

		CaseDefinition caseDefinition = caseDefRepository.get(caseDefinitionId);

		CaseInstance newCaseInstance = caseInstanceCreateService.create(caseDefinition);

		processInstanceService.create(caseDefinition.getStagesLifecycleProcessKey(), newCaseInstance.getBusinessKey(),
				newCaseInstance.getAttributes());

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
	public void updateStage(final String businessKey, final String caseStage) throws Exception {
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

	@Override
	public void saveDocument(final String businessKey, final CaseDocument document) throws Exception {
		CaseInstance caseInstance = repository.get(businessKey);

		caseInstance.addDocument(document);

		repository.update(businessKey, caseInstance);
	}

	@Override
	public void saveComment(final String businessKey, final Comment comment) throws Exception {
		CaseInstance caseInstance = repository.get(comment.getCaseId());
		if (caseInstance == null) {
			throw new CaseNotFoundException("Case not found");
		}

		comment.setCreatedAt(new Date());

		comment.setId(ObjectId.get().toString());

		if (caseInstance.getComments() == null) {
			caseInstance.setComments(Arrays.asList(comment));
		} else {
			caseInstance.getComments().add(comment);
		}

		repository.update(comment.getCaseId(), caseInstance);
	}

	@Override
	public void updateComment(final String businessKey, final String commentId, final String body)
			throws Exception {
		CaseInstance caseInstance = repository.get(businessKey);

		if (caseInstance == null) {
			throw new CaseInstanceCommentNotFoundException("Case not found");
		}

		repository.updateComment(businessKey, commentId, body);
	}

	public void deleteComment(final String businessKey, final String commentId) throws Exception {
		CaseInstance caseInstance = repository.get(businessKey);

		if (caseInstance == null) {
			throw new CaseInstanceCommentNotFoundException("Case not found");
		}

		Comment comment = caseInstance.getComments().stream().filter(o -> commentId.equals(o.getId()))
				.reduce((a, b) -> {
					throw new IllegalStateException("Multiple elements: " + a + ", " + b);
				}).get();
		if (comment == null) {
			throw new CaseInstanceCommentNotFoundException("Comment not found");
		}

		repository.deleteComment(businessKey, comment);
	}

	public void setRepository(CaseInstanceRepository repository) {
		this.repository = repository;
	}

	public void setCaseInstanceCreateService(CaseInstanceCreateService caseInstanceCreateService) {
		this.caseInstanceCreateService = caseInstanceCreateService;
	}
}