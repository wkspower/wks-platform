package com.wks.caseengine.rest.vgoht;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.vgoht.serviceimpl.VgohtNormBasisServiceImpl;

import org.springframework.web.bind.annotation.RequestMapping;
import java.util.UUID;

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

}
