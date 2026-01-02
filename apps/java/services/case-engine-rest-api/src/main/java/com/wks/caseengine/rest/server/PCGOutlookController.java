package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.PCGOutlookDTO;
import com.wks.caseengine.service.PCGOutlookService;

@RestController
@RequestMapping("task")
public class PCGOutlookController {
      
    @Autowired
    private PCGOutlookService service;

    @GetMapping("pcg-outlook/{siteId}/{financialYear}")
    public List<PCGOutlookDTO> getPCGOutlook(@PathVariable String siteId, @PathVariable String financialYear) {
        return service.getData(UUID.fromString(siteId), financialYear);
    }

    @PostMapping("pcg-outlook/{siteId}/{financialYear}")
    public void savePCGOutlook(@PathVariable String siteId, @PathVariable String financialYear, @RequestBody List<PCGOutlookDTO> data) {
        service.saveData(data, financialYear, UUID.fromString(siteId));
    }


}
