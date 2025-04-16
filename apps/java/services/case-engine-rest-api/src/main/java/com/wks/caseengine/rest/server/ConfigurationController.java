package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.NormAttributeTransactionReceipeDTO;
import com.wks.caseengine.dto.NormAttributeTransactionReceipeRequestDTO;
import com.wks.caseengine.entity.NormAttributeTransactionReceipe;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ConfigurationService;

@RestController
@RequestMapping("api/configuration")
public class ConfigurationController {

	@Autowired
	private ConfigurationService configurationService;

	@GetMapping
	public ResponseEntity<AOPMessageVM> getConfigurationData(@RequestParam String year, @RequestParam UUID plantFKId) {
		AOPMessageVM response = configurationService.getConfigurationData(year, plantFKId);
		return ResponseEntity.status(response.getCode()).body(response);
	}

	@PostMapping
	public ResponseEntity<AOPMessageVM> saveConfigurationData(@RequestParam String year,
			@RequestBody List<ConfigurationDTO> configurationDTOList) {
		AOPMessageVM response = configurationService.saveConfigurationData(year, configurationDTOList);
		return ResponseEntity.status(response.getCode()).body(response);
	}

	@GetMapping(value = "/pe")
	public ResponseEntity<AOPMessageVM> getNormAttributeTransactionReceipeSp(@RequestParam String year,
			@RequestParam String plantId) {
		AOPMessageVM response = configurationService.getNormAttributeTransactionReceipe(year, plantId);
		return ResponseEntity.status(response.getCode()).body(response);

	}

	@PostMapping(value = "/pe/update")
	public ResponseEntity<AOPMessageVM> updateCalculatedConsumptionNorms(@RequestParam String year,
			@RequestParam String plantId,
			@RequestBody List<NormAttributeTransactionReceipeRequestDTO> normAttributeTransactionReceipeDTOList) {
				AOPMessageVM response = configurationService.updateCalculatedConsumptionNorms(year, plantId,
				normAttributeTransactionReceipeDTOList);

				return ResponseEntity.status(response.getCode()).body(response);

	}

}
