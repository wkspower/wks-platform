package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.form.Form;
import com.wks.caseengine.form.FormService;

@RestController
@RequestMapping("form")
public class FormController {

	@Autowired
	private FormService formService;

	@GetMapping(value = "/")
	public List<Form> find() throws Exception {
		return formService.find();
	}

	@GetMapping(value = "/{formKey}")
	public Form get(@PathVariable final String formKey) throws Exception {
		return formService.get(formKey);
	}

	@PostMapping(value = "/")
	public void save(@RequestBody final Form form) throws Exception {
		formService.save(form);
	}

	@DeleteMapping(value = "/{formKey}")
	public void delete(@PathVariable final String formKey) throws Exception {
		formService.delete(formKey);
	}

	@PatchMapping(value = "/{formKey}")
	public void update(@PathVariable final String formKey, @RequestBody final Form form) throws Exception {
		formService.update(formKey, form);
	}

}
