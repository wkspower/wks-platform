package com.wks.caseengine.rest.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.product.ProductDTO;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.NormParametersService;

@RestController
@RequestMapping("task")
public class NormParametersController {

    @Autowired
    NormParametersService normParametersService;

    @GetMapping(value="/getAllGrades")
	public  List<NormParameters> getAllGrades(@RequestParam String plantId){
		return normParametersService.getAllGrades(plantId);	
	}
    
    @GetMapping(value="/norm-paramters")
    public AOPMessageVM getNormParameters(@RequestParam String plantId,@RequestParam String year,@RequestParam String type) {
    	return normParametersService.getNormParameters(plantId,year,type);
    }

    @GetMapping(value = "/products")
	public AOPMessageVM getAllProducts(@RequestParam  String year,@RequestParam String plantId) {
		return normParametersService.getAllProducts( plantId,year);
	}


     @GetMapping(value = "/lines")
    public AOPMessageVM getAllLines(@RequestParam String plantId) {
        return normParametersService.getAllLines(plantId);
    }


}
