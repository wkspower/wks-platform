package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.CatalystAttributesDTO;
import com.wks.caseengine.entity.CatalystAttributes;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.service.CatalystAttributesService;
@RestController
@RequestMapping("/catalyst-attributes")
public class CatalystAttributesController {
	
	@Autowired
	private CatalystAttributesService catalystAttributesService;
	
	@GetMapping
	public List<CatalystAttributes> getAllCatalystAttributes(){
		return catalystAttributesService.getAllCatalystAttributes();
	}

}
