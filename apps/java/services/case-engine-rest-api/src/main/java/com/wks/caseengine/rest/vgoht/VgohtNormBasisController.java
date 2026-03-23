package com.wks.caseengine.rest.vgoht;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.vgoht.dto.VgohtNormConfigurationDTO;
import com.wks.caseengine.vgoht.serviceimpl.VgohtNormBasisServiceImpl;

import org.springframework.web.bind.annotation.RequestMapping;
import java.util.*;

@RestController
@RequestMapping("task")
public class VgohtNormBasisController {
    
    @Autowired
    private VgohtNormBasisServiceImpl vgohtNormBasisServiceImpl;


	@GetMapping(value="/vgoht/norms-basis")
	public AOPMessageVM getConfigurationData(@RequestParam String year,@RequestParam UUID plantFKId,@RequestParam(required=false) String version) {
        if (plantFKId == null || year == null || year.isEmpty()) {
           throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }
		return vgohtNormBasisServiceImpl.getConfigurationData(year,plantFKId,version);
	}

    @PostMapping(value = "/vgoht/norms-basis")
    public AOPMessageVM saveConfigurationData(
            @RequestParam String year,
            @RequestParam UUID plantFKId,
            @RequestParam(required = false) String version,
            @RequestBody List<VgohtNormConfigurationDTO> vgohtNormConfigurationDTOList) {

        if (plantFKId == null || year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        return vgohtNormBasisServiceImpl.saveConfigurationData(year, plantFKId, version, vgohtNormConfigurationDTOList);
    }

    @PostMapping(value = "/vgoht/norms-basis/constant")
    public AOPMessageVM saveYearlyValues(
        @RequestParam String year,
        @RequestParam UUID plantFKId,
        @RequestBody List<VgohtNormConfigurationDTO> yearlyValuesList, @RequestParam String periodFrom, @RequestParam String periodTo)  {

        if (plantFKId == null || year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        return vgohtNormBasisServiceImpl.saveYearlyValues(year, plantFKId, yearlyValuesList, periodFrom, periodTo);
    }

    @GetMapping(value="/vgoht/norms-basis/constant")
    public AOPMessageVM getYearlyValues(@RequestParam String year, @RequestParam UUID plantFKId) {
        if (plantFKId == null || year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        return vgohtNormBasisServiceImpl.getYearlyValues(year, plantFKId);
    }

    @GetMapping(value="/vgoht/norms-basis/constant-sp")
	public AOPMessageVM getConfigurationConstants(@RequestParam String year,@RequestParam String plantFKId) {
		return vgohtNormBasisServiceImpl.getConfigurationConstants(year,plantFKId);
	}

    @GetMapping("vgoht/load-button-norm-calculation")
    public ResponseEntity<AOPMessageVM> loadButtonNormCalculation(@RequestParam String plantId, @RequestParam String aopYear, @RequestParam String siteId, @RequestParam String periodFrom, @RequestParam String periodTo) {
        AOPMessageVM aopMessageVM = vgohtNormBasisServiceImpl.LoadButtonNormCalculation(UUID.fromString(plantId), aopYear, UUID.fromString(siteId), periodFrom, periodTo);
        return ResponseEntity.ok(aopMessageVM);
    }
}
