package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.NormParametersService;

@RestController
@RequestMapping("task")
public class NormParametersController {

	@Autowired
	NormParametersService normParametersService;

	@GetMapping(value = "/grades")
	public ResponseEntity<AOPMessageVM> getAllGrades(@RequestParam String plantId) {
		AOPMessageVM response = normParametersService.getAllGrades(plantId);
		return ResponseEntity.status(response.getCode()).body(response);

	}

}
