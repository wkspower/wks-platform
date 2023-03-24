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
	public void saveFiles(final String businessKey, final CaseDocument[] documents) throws Exception {
		CaseInstance caseInstance = repository.get(businessKey);

		caseInstance.addDocuments(documents);

		repository.update(businessKey, caseInstance);
	}

	@Override
	public void saveComment(final String businessKey, final Comment comment) throws Exception {
		CaseInstance caseInstance = repository.get(comment.getCaseId());
		if(caseInstance == null) {
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
	public void updateComment(final String businessKey, final String commentId, final Comment comment)
			throws Exception {
//		CaseInstance caseInstance = repository.get(comment.getCaseId());
//		
//		if (caseInstance.getComments() == null || caseInstance.getComments().isEmpty() ) {
//			throw new CaseInstanceCommentNotFoundException("Comment not found");
//		}
//		
//		for (Comment commentOnBase : caseInstance.getComments()) {
//			if (StringUtils.equals(commentOnBase.getId(), comment.getId())) {
//				if (StringUtils.equals(commentOnBase.getUserId(), comment.getUserId())) {
//					commentOnBase.setBody(comment.getBody());
//				} else {
//					throw new CaseInstanceCommentNotFoundException("Only the original user can edit a comment");
//				}
//			}
//		}
//		
//		repository.update(comment.getCaseId(), caseInstance);
	}

	public void deleteComment(final String businessKey, final String commentId) throws Exception {
//		CaseInstance caseInstance = repository.get(comment.getCaseId());
//		
//		if (caseInstance.getComments() == null || caseInstance.getComments().isEmpty() ) {
//			throw new CaseInstanceCommentNotFoundException("Comment not found");
//		}
//		
//		Comment commentToDelete = null;
//		
//		for (Comment commentOnBase : caseInstance.getComments()) {
//			if (StringUtils.equals(commentOnBase.getId(), comment.getId())) {
//				if (StringUtils.equals(commentOnBase.getUserId(), comment.getUserId())) {
//					commentToDelete = commentOnBase;
//				} else {
//					throw new CaseInstanceCommentNotFoundException("Only the original user can edit a comment");
//				}
//			}
//		}
//		
//		caseInstance.getComments().remove(commentToDelete);
//		
//		repository.update(comment.getCaseId(), caseInstance);
	}

	public void setRepository(CaseInstanceRepository repository) {
		this.repository = repository;
	}

	public void setCaseInstanceCreateService(CaseInstanceCreateService caseInstanceCreateService) {
		this.caseInstanceCreateService = caseInstanceCreateService;
	}
}