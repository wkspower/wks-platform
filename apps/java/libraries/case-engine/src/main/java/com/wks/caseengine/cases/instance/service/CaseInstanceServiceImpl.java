/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.caseengine.cases.instance.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.instance.CaseAttribute;
import com.wks.caseengine.cases.instance.CaseAttributeType;
import com.wks.caseengine.cases.instance.CaseDocument;
import com.wks.caseengine.cases.instance.CaseFilter;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceCommentNotFoundException;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.cases.instance.CaseNotFoundException;
import com.wks.caseengine.cases.instance.Comment;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepository;
import com.wks.caseengine.pagination.PageResult;
import com.wks.caseengine.process.instance.ProcessInstanceService;
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
	public PageResult<CaseInstance> find(CaseFilter filters) throws Exception {
		return repository.find(filters);
	}

	@Override
	public CaseInstance get(final String businessKey) throws Exception {
		return repository.get(businessKey);
	}

	@Override
	public CaseInstance create(final CaseInstance caseInstance) throws Exception {

		caseInstance.getAttributes()
				.add(new CaseAttribute("createdAt", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
						CaseAttributeType.STRING.getValue()));

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

	@Override
	public CaseInstance patch(final String businessKey, final CaseInstance mergePatch) throws Exception {

		CaseInstance target = repository.get(businessKey);

		if (mergePatch.getStatus() != null) {
			target.setStatus(mergePatch.getStatus());
		}
		if (mergePatch.getStage() != null) {
			target.setStage(mergePatch.getStage());
		}
		if (mergePatch.getQueueId() != null) {
			target.setQueueId(mergePatch.getQueueId());
		}

		repository.update(businessKey, target);

		// TODO return the updated case instance from DB
		return target;
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
	public void updateComment(final String businessKey, final String commentId, final String body) throws Exception {
		CaseInstance caseInstance = repository.get(businessKey);

		if (caseInstance == null) {
			throw new CaseInstanceCommentNotFoundException("Case not found");
		}

		repository.updateComment(businessKey, commentId, body);
	}

	@Override
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