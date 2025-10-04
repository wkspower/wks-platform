package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.NormTransactionsService;

@RestController
@RequestMapping("task")
public class NormsTransactionsController {

    @Autowired
    NormTransactionsService normTransactionsService;

    
    
    @GetMapping(value="/norm-transactions")
    public AOPMessageVM getNormTransactions(@RequestParam String plantId,@RequestParam String year,@RequestParam(required=false) String screen) {
    	return normTransactionsService.getNormTransactions(plantId,year,screen);
    }


    
}
