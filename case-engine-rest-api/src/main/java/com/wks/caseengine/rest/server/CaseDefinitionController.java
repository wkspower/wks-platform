package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseDefinitionNotFoundException;
import com.wks.caseengine.cases.definition.CaseDefinitionService;

@RestController
@RequestMapping("case-definition")
public class CaseDefinitionController {

	@Autowired
	private CaseDefinitionService caseDefinitionService;

	@GetMapping(value = "/")
	public List<CaseDefinition> find() throws Exception {
		return caseDefinitionService.find();
	}

	@GetMapping(value = "/{caseDefId}")
	public CaseDefinition get(@PathVariable final String caseDefId) throws Exception {
		return caseDefinitionService.get(caseDefId);
	}

	@PostMapping(value = "/")
	public CaseDefinition save(@RequestBody final CaseDefinition caseDefinition) throws Exception {
		return caseDefinitionService.create(caseDefinition);
	}

	@PatchMapping(value = "/{caseDefId}")
	public CaseDefinition update(@PathVariable final String caseDefId, @RequestBody final CaseDefinition caseDefinition)
			throws Exception {
		return caseDefinitionService.update(caseDefId, caseDefinition);
	}

	@DeleteMapping(value = "/{caseDefId}")
	public void delete(@PathVariable final String caseDefId) throws Exception {
		try {
			caseDefinitionService.delete(caseDefId);
		} catch (CaseDefinitionNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Case Definition Not Found - " + caseDefId, e);
		}
	}
}
