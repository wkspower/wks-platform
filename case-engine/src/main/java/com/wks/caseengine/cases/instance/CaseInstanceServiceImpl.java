package com.wks.caseengine.cases.instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wks.api.security.context.SecurityContextTenantHolder;
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
	
    @Autowired
    private SecurityContextTenantHolder holder;

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
	public CaseInstance create(CaseInstance caseInstance) throws Exception {
		caseInstance.getAttributes().add(new CaseAttribute("createdAt", DateUtils.formatDate(new Date(), "dd/MM/yyyy")));

		CaseDefinition caseDefinition = caseDefRepository.get(caseInstance.getCaseDefinitionId());

		CaseInstance newCaseInstance = caseInstanceCreateService.create(caseInstance);

		processInstanceService.create(
				caseDefinition.getStagesLifecycleProcessKey(), 
				newCaseInstance.getBusinessKey(),
				newCaseInstance.getAttributes(), 
				caseDefinition.getBpmEngineId());

		return newCaseInstance;
	}

	@Override
	public CaseInstance create(final String caseDefinitionId) throws Exception {

		CaseDefinition caseDefinition = caseDefRepository.get(caseDefinitionId);

		CaseInstance newCaseInstance = caseInstanceCreateService.create(caseDefinition);

		processInstanceService.create(
				caseDefinition.getStagesLifecycleProcessKey(), 
				newCaseInstance.getBusinessKey(),
				newCaseInstance.getAttributes(), 
				caseDefinition.getBpmEngineId());

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
	
	@Override
	public void uploadFiles(String businessKey, CaseInstanceFile[] files) throws Exception {
		CaseInstance caseInstance = repository.get(businessKey);
		
		List<CaseInstanceFileAttribute> caseInstanceFileAttributeOriginal = new ArrayList<CaseInstanceFileAttribute>();
		
		caseInstance.getAttributes().forEach(attribute -> {
			if (StringUtils.equals(attribute.getName(), "file")) {
				caseInstanceFileAttributeOriginal.addAll(gson.fromJson(attribute.getValue(), new TypeToken<List<CaseInstanceFileAttribute>>(){}.getType()));
			}
		});
		
		List<CaseInstanceFileAttribute> caseInstanceFileAttributeToInclude = new ArrayList<CaseInstanceFileAttribute>();
		
		List<CaseInstanceFile> fileList = Arrays.asList(files);
		
		fileList.forEach(file -> {
			caseInstanceFileAttributeToInclude.add(new CaseInstanceFileAttribute("base64", file.getName(), file.getBase64(), file.getType(), file.getName()));
		});
		
		caseInstanceFileAttributeToInclude.addAll(caseInstanceFileAttributeOriginal);
		
		Boolean fileAttributeFound = false;
		
		for (CaseAttribute attribute : caseInstance.getAttributes()) {
			if (StringUtils.equals(attribute.getName(), "file")) {
				attribute.setValue(gson.toJson(caseInstanceFileAttributeToInclude));
				fileAttributeFound = true;
				break;
			}
		}
		
		if (!fileAttributeFound) {
			caseInstance.getAttributes().add(new CaseAttribute("file", gson.toJson(caseInstanceFileAttributeToInclude)));
		}
		
		repository.update(businessKey, caseInstance);
	}
	
	@Override
	public void addComment(Comment newComment) throws Exception {
		CaseInstance caseInstance = repository.get(newComment.getCaseId());
		
		List<Comment> comments = new ArrayList<Comment>();
		
		caseInstance.getAttributes().forEach(attribute -> {
			if (StringUtils.equals(attribute.getName(), "comments")) {
				comments.addAll(gson.fromJson(attribute.getValue(), new TypeToken<List<Comment>>(){}.getType()));
			}
		});
		
		newComment.setCreatedAt(new Date());
		
		newComment.setId(ObjectId.get().toString());
		
		comments.add(newComment);
		
		Boolean commentsAttributeFound = false;
		
		for (CaseAttribute attribute : caseInstance.getAttributes()) {
			if (StringUtils.equals(attribute.getName(), "comments")) {
				attribute.setValue(gson.toJson(comments));
				commentsAttributeFound = true;
				break;
			}
		}
		
		if (!commentsAttributeFound) {
			caseInstance.getAttributes().add(new CaseAttribute("comments", gson.toJson(comments)));
		}
		
		repository.update(newComment.getCaseId(), caseInstance);
	}
	
	@Override
	public void editComment(Comment comment) throws Exception {
		CaseInstance caseInstance = repository.get(comment.getCaseId());
		
		List<Comment> comments = new ArrayList<Comment>();
		
		caseInstance.getAttributes().forEach(attribute -> {
			if (StringUtils.equals(attribute.getName(), "comments")) {
				comments.addAll(gson.fromJson(attribute.getValue(), new TypeToken<List<Comment>>(){}.getType()));
			}
		});
		
		if (comments == null || comments.isEmpty() ) {
			throw new CaseInstanceCommentNotFoundException("Comment not found");
		}
		
		for (Comment commentOnBase : comments) {
			if (StringUtils.equals(commentOnBase.getId(), comment.getId())) {
				if (!StringUtils.equals(commentOnBase.getUserId(), comment.getUserId())) {
					commentOnBase.setBody(comment.getBody());
				} else {
					throw new CaseInstanceCommentNotFoundException("Only the original user can edit a comment");
				}
			}
		}
		
		for (CaseAttribute attribute : caseInstance.getAttributes()) {
			if (StringUtils.equals(attribute.getName(), "comments")) {
				attribute.setValue(gson.toJson(comments));
				break;
			}
		}
		
		repository.update(comment.getCaseId(), caseInstance);
	}

	public void setRepository(CaseInstanceRepository repository) {
		this.repository = repository;
	}

	public void setCaseInstanceCreateService(CaseInstanceCreateService caseInstanceCreateService) {
		this.caseInstanceCreateService = caseInstanceCreateService;
	}
}