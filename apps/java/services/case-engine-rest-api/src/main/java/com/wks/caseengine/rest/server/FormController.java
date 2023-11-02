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
package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.form.Form;
import com.wks.caseengine.form.FormNotFoundException;
import com.wks.caseengine.form.FormService;
import com.wks.caseengine.rest.exception.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("form")
@Tag(name = "Form", description = "User forms definitions with fields and attributes for cases")
public class FormController {

	@Autowired
	private FormService formService;

	@GetMapping
	public ResponseEntity<List<Form>> find() {
		return ResponseEntity.ok(formService.find());
	}

	@GetMapping(value = "/{formKey}")
	public ResponseEntity<Form> get(@PathVariable final String formKey) {
		try {
			return ResponseEntity.ok(formService.get(formKey));
		} catch (FormNotFoundException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
	}

	@PostMapping
	public ResponseEntity<Void> save(@RequestBody final Form form) {
		formService.save(form);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping(value = "/{formKey}")
	public ResponseEntity<Void> delete(@PathVariable final String formKey) {
		try {
			formService.delete(formKey);
			return ResponseEntity.noContent().build();
		} catch (FormNotFoundException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
	}

	@PatchMapping(value = "/{formKey}")
	public ResponseEntity<Void> update(@PathVariable final String formKey, @RequestBody final Form form) {
		try {
			formService.update(formKey, form);
			return ResponseEntity.noContent().build();
		} catch (FormNotFoundException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
	}

}
