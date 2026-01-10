package com.wks.caseengine.rest.server.tcs;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wks.caseengine.dto.tcs.TCSSlowdownDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.service.tcs.TCSSlowdownService;

@RestController
@RequestMapping("task")
public class TCSSlowdownController {

    @Autowired
    private TCSSlowdownService tcsSlowdownService;

    @GetMapping("/tcs-slowdown")
    public Map<String, Object> getAllTCSSlowdown(
        @RequestParam (required = false) String plantId,
        @RequestParam String year,
        @RequestParam(required = false) String siteId,
        @RequestParam(required = false) String verticalId) {
        
        if (plantId == null && (siteId == null || verticalId == null)) {
            throw new RestInvalidArgumentException("Plant ID and Site ID or Vertical ID cannot be null", null);
        }
        if (plantId != null && (siteId != null || verticalId != null)) {
            throw new RestInvalidArgumentException("Plant ID and Site ID or Vertical ID cannot be provided together", null);
        }

        return tcsSlowdownService.getAll(plantId, year, siteId, verticalId);
    }

    @PostMapping("/tcs-slowdown")
    public AOPMessageVM saveOrUpdate(
        @RequestParam String plantId,
        @RequestParam String year,
        @RequestBody List<TCSSlowdownDTO> payload) {
        
        return tcsSlowdownService.saveOrUpdate(plantId, year, payload);
    }
}
