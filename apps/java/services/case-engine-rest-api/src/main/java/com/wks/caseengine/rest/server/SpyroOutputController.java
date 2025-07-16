package com.wks.caseengine.rest.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.wks.caseengine.service.SpyroOutputService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
import com.wks.caseengine.dto.SpyroOutputDTO;

import com.wks.caseengine.message.vm.AOPMessageVM;

@RestController
@RequestMapping("task")
public class SpyroOutputController {
	
	@Autowired
	private SpyroOutputService spyroOutputService;
	
	@GetMapping(value="/spyro-output")
	public AOPMessageVM getSpyroOutputData(@RequestParam String year,@RequestParam String plantId,@RequestParam String Mode,@RequestParam(value = "type", required = false) String type){
		return	spyroOutputService.getSpyroOutputData(year, plantId,Mode,type);
	}

	@PostMapping(value="/spyro-output")
	public AOPMessageVM updateSpyroOutputData(@RequestBody List<SpyroOutputDTO> spyroOutputDTOList){
		return spyroOutputService.updateSpyroOutputData(spyroOutputDTOList);
	}
	
	@GetMapping(value="/spyro-output/yield")
	public AOPMessageVM getSpyroOutputYieldData(@RequestParam String year,@RequestParam String plantId){
		return	spyroOutputService.getSpyroOutputYieldData(year, plantId);
	}
	
	@PostMapping(value="/spyro-output/yield")
	public AOPMessageVM updateSpyroOutputYieldData(
	    @RequestParam String plantId,
	    @RequestParam String year,
	    @RequestBody List<Map<String, Object>> payload
	) {
	    List<NormAttributeTransactionsDTO> dtoList = new ArrayList<>();

	    for (Map<String, Object> item : payload) {
	        String particulars = (String) item.get("particulars"); // e.g. "Ethylene"

	        for (Map.Entry<String, Object> e : item.entrySet()) {
	            String key = e.getKey();
	            if ("NormParameterFKID".equalsIgnoreCase(key) || "particulars".equalsIgnoreCase(key))
	                continue;

	            // Build "Ethylene_5F_C2C3"
	            String combined = particulars + "_" + key;

	            // Split on '_'
	            String[] parts = combined.split("_", 3); 
	            // parts[0] = "Ethylene", parts[1] = "5F", parts[2] = "C2C3"

	            String normName;
	            if (parts.length == 3) {
	                normName = parts[1] + "_" + parts[0] + "_" + parts[2];
	            } else {
	                // Fallback to original if format unexpected
	                normName = combined;
	            }

	            String valStr = Optional.ofNullable(e.getValue())
	                                    .map(Object::toString)
	                                    .orElse(null);

	            NormAttributeTransactionsDTO dto = new NormAttributeTransactionsDTO();
	            
	            dto.setNormParameterName(normName);
	            dto.setAttributeValue(valStr);

	            dtoList.add(dto);
	        }
	    }

	    return spyroOutputService.updateSpyroOutputYieldData(plantId, year, dtoList);
	}

}
