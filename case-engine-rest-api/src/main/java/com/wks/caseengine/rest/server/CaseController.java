package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.wks.caseengine.cases.instance.Attachment;
import com.wks.caseengine.cases.instance.CaseFilter;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceFile;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.cases.instance.CaseInstanceService;
import com.wks.caseengine.cases.instance.Comment;
import com.wks.caseengine.pagination.CursorPage;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("case")
@Tag(name = "Case Instance", description = "A Case Instance is created based in a Case Definition and is the 'Digital Folder' for related information, documents, communication and processes for case")
public class CaseController {

	@Autowired
	private CaseInstanceService caseInstanceService;

	@GetMapping(value = "/")
	public ResponseEntity<Object> find(@RequestParam(required=false) String status,
																	@RequestParam(required=false) String caseDefId, 
																	@RequestParam(required=false, name="token") String token,
																	@RequestParam(required=false, name="sort") String sort,
																	@RequestParam(required=false, name="dir") String dir,
																	@RequestParam(required=false, name="limit") String limit) throws Exception {
		CaseFilter filter = new CaseFilter(status, caseDefId, token, sort, dir, limit);
		
		CursorPage<CaseInstance> data = caseInstanceService.find(filter);
		
		return new ResponseEntity<>(data.toJson(), HttpStatus.OK);
	}

	@GetMapping(value = "/{businessKey}")
	public CaseInstance get(@PathVariable final String businessKey) throws Exception {
		return caseInstanceService.get(businessKey);
	}

	@PostMapping(value = "/")
	public CaseInstance save(@RequestBody final CaseInstance caseInstance) throws Exception {
		return caseInstanceService.create(caseInstance);
	}

	@PatchMapping(value = "/{businessKey}")
	public void update(@PathVariable final String businessKey, @RequestBody final CaseInstance caseInstance)
			throws Exception {
		caseInstanceService.updateStatus(businessKey, caseInstance.getStatus());
	}
	
	@PutMapping(value = "/upload/{businessKey}")
	public void uploadFile(@PathVariable final String businessKey, @RequestBody CaseInstanceFile[] files)
			throws Exception {
		caseInstanceService.uploadFiles(businessKey, files);
	}

	@DeleteMapping(value = "/{businessKey}")
	public void delete(@PathVariable final String businessKey) throws Exception {
		try {
			caseInstanceService.delete(businessKey);
		} catch (CaseInstanceNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Case Instance Not Found - " + businessKey, e);
		}
	}
	
	@PostMapping(value = "/comment")
	public void addComment(@RequestBody final Comment newComment) throws Exception {
		caseInstanceService.addComment(newComment);
	}

	@PutMapping(value = "/comment")
	public void editComment(@RequestBody final Comment comment) throws Exception {
		caseInstanceService.editComment(comment);
	}
	
	@PostMapping(value = "/comment/delete")
	public void deleteComment(@RequestBody final Comment comment) throws Exception {
		caseInstanceService.deleteComment(comment);
	}
	
	@PutMapping(value = "/{businessKey}/attachments")
	public void addAttachment(@PathVariable String businessKey, @RequestBody Attachment newAttachment) throws Exception {
		caseInstanceService.addAttachment(businessKey, newAttachment);
	}

}
