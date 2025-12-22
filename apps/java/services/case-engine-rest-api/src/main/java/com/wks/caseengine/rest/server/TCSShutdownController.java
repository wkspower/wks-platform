package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wks.caseengine.dto.TCSShutdownDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.TCSShutdownService;

@RestController
@RequestMapping("task")
public class TCSShutdownController {
    @Autowired
    private TCSShutdownService tcsShutdownService;

    @GetMapping("/tcs-shutdown")
    public Map<String, Object> getAllTCSShutdown(
        @RequestParam String plantId,
        @RequestParam String year) {

        return tcsShutdownService.getAll(plantId, year);
    }

    @PostMapping("/tcs-shutdown")
    public AOPMessageVM saveOrUpdate(
        @RequestParam String plantId,
        @RequestParam String year,
        @RequestBody List<TCSShutdownDTO> payload) {
        return tcsShutdownService.saveOrUpdate(plantId, year, payload);
    }
}
