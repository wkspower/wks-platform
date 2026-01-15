package com.wks.caseengine.rest.tcs;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wks.caseengine.tcs.dto.TCSShutdownDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;  
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.tcs.service.TCSShutdownService;

@RestController
@RequestMapping("task")
public class TCSShutdownController {
    @Autowired
    private TCSShutdownService tcsShutdownService;

    @GetMapping("/tcs-shutdown")
    public Map<String, Object> getAllTCSShutdown(
        @RequestParam (required = false) String plantId,
        @RequestParam String year,
        @RequestParam(required = false) String siteId,
        @RequestParam(required = false) String verticalId
    ) {

        if (plantId == null && (siteId == null || verticalId == null)) {
            throw new RestInvalidArgumentException("Plant ID and Site ID or Vertical ID cannot be null", null);
        }
        if (plantId != null && (siteId != null || verticalId != null)) {
            throw new RestInvalidArgumentException("Plant ID and Site ID or Vertical ID cannot be provided together", null);
        }

        return tcsShutdownService.getAll(plantId, year, siteId, verticalId);
    }

    @PostMapping("/tcs-shutdown")
    public AOPMessageVM saveOrUpdate(
        @RequestParam String plantId,
        @RequestParam String year,
        @RequestBody List<TCSShutdownDTO> payload) {
        return tcsShutdownService.saveOrUpdate(plantId, year, payload);
    }

    @DeleteMapping("/tcs-shutdown")
    public AOPMessageVM delete(
        @RequestParam String id) {
        return tcsShutdownService.delete(UUID.fromString(id));
    }
}

