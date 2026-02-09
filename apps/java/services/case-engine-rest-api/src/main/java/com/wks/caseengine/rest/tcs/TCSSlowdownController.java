package com.wks.caseengine.rest.tcs;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.tcs.dto.TCSSlowdownDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.tcs.service.TCSSlowdownService;

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

    @DeleteMapping("/tcs-slowdown") 
    public AOPMessageVM delete(
        @RequestParam String id) {
        
        return tcsSlowdownService.delete(UUID.fromString(id));
    }

    @GetMapping("/tcs-slowdown/export")
    public ResponseEntity<byte[]> exportTCSSlowdown(
        @RequestParam(required = false) String plantId,
        @RequestParam String year,
        @RequestParam(required = false) String siteId,
        @RequestParam(required = false) String verticalId) {

        if (plantId == null && (siteId == null || verticalId == null)) {
            throw new RestInvalidArgumentException("Plant ID and Site ID or Vertical ID cannot be null", null);
        }
        if (plantId != null && (siteId != null || verticalId != null)) {
            throw new RestInvalidArgumentException("Plant ID and Site ID or Vertical ID cannot be provided together", null);
        }

        byte[] excelData = tcsSlowdownService.exportTCSSlowdown(
            plantId,
            year,
            siteId,
            verticalId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "TCSSlowdown_" + year + ".xlsx");

        return ResponseEntity.ok()
            .headers(headers)
            .body(excelData);
    }

    @PostMapping("/tcs-slowdown/import")
    public AOPMessageVM importTCSSlowdown(
        @RequestParam String plantId,
        @RequestParam String year,
        @RequestParam("file") MultipartFile file) {

        return tcsSlowdownService.importExcel(
            plantId,
            year,
            file);
    }

}

