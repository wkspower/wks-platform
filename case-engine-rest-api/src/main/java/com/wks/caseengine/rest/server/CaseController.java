package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.wks.caseengine.cases.instance.CaseDocument;
import com.wks.caseengine.cases.instance.CaseFilter;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.cases.instance.CaseInstanceService;
import com.wks.caseengine.cases.instance.Comment;
import com.wks.caseengine.pagination.Cursor;
import com.wks.caseengine.pagination.PageResult;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("case")
@Tag(name = "Case Instance", description = "A Case Instance is created based in a Case Definition and is the 'Digital Folder' for related information, documents, communication and processes for case")
public class CaseController {

	@Autowired
	private CaseInstanceService caseInstanceService;

	@GetMapping(value = "/")
	public ResponseEntity<Object> find(@RequestParam(required=false) String status,
																	@RequestParam(required=false) String caseDefinitionId, 
																	@RequestParam(required=false, name="before") String before,
																	@RequestParam(required=false, name="after") String after,
																	@RequestParam(required=false, name="sort") String sort,
																	@RequestParam(required=false, name="limit") String limit) throws Exception {
		Cursor cursor = Cursor.of(before, after);
		
		CaseFilter filter = new CaseFilter(status, caseDefinitionId, cursor, sort, limit);
		
		PageResult<CaseInstance> data = caseInstanceService.find(filter);
		
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

	@DeleteMapping(value = "/{businessKey}")
	public void delete(@PathVariable final String businessKey) throws Exception {
		try {
			caseInstanceService.delete(businessKey);
		} catch (CaseInstanceNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Case Instance Not Found - " + businessKey, e);
		}
	}

	@PostMapping(value = "/{businessKey}/document")
	public void saveDocument(@PathVariable final String businessKey, @RequestBody CaseDocument document)
			throws Exception {
		caseInstanceService.saveDocument(businessKey, document);
	}

	@PostMapping(value = "/{businessKey}/comment")
	public void saveComment(@PathVariable final String businessKey, @RequestBody final Comment newComment)
			throws Exception {
		caseInstanceService.saveComment(businessKey, newComment);
	}

	@PatchMapping(value = "/{businessKey}/comment/{commentId}")
	public void udpateComment(@PathVariable final String businessKey, @PathVariable final String commentId,
			@RequestBody final Comment comment) throws Exception {
		caseInstanceService.updateComment(businessKey, commentId, comment.getBody());
	}

	@DeleteMapping(value = "/{businessKey}/comment/{commentId}")
	public void deleteComment(@PathVariable final String businessKey, @PathVariable final String commentId)
			throws Exception {
		caseInstanceService.deleteComment(businessKey, commentId);
	}
	
}
