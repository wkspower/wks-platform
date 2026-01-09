package com.wks.caseengine.rest.server.tcs;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wks.caseengine.dto.tcs.TCSSlowdownDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.tcs.TCSSlowdownService;

@RestController
@RequestMapping("task")
public class TCSSlowdownController {

    @Autowired
    private TCSSlowdownService tcsSlowdownService;

    @GetMapping("/tcs-slowdown")
    public Map<String, Object> getAllTCSSlowdown(
        @RequestParam String plantId,
        @RequestParam String year) {
        
        return tcsSlowdownService.getAll(plantId, year);
    }

    @PostMapping("/tcs-slowdown")
    public AOPMessageVM saveOrUpdate(
        @RequestParam String plantId,
        @RequestParam String year,
        @RequestBody List<TCSSlowdownDTO> payload) {
        
        return tcsSlowdownService.saveOrUpdate(plantId, year, payload);
    }
}
