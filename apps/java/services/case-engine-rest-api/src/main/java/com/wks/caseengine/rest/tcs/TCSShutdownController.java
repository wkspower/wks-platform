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

    @PostMapping("/tcs-shutdown/carry-forward")
    public AOPMessageVM carryForwardTCSShutdown(
        @RequestParam String plantId,
        @RequestParam String year) {
            if(plantId == null || plantId.isEmpty()) {
                throw new RestInvalidArgumentException("Plant ID cannot be null", null);
            }
            if(year == null || year.isEmpty()) {
                throw new RestInvalidArgumentException("Year cannot be null", null);
            }
      
        return tcsShutdownService.carryForwardTCSShutdown(plantId, year);
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

    @GetMapping("/tcs-shutdown/export")
    public ResponseEntity<byte[]> exportTCSShutdown(
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

        byte[] excelData = tcsShutdownService.exportTCSShutdown(
            plantId,
            year,
            siteId,
            verticalId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "TCSShutdown_" + year + ".xlsx");

        return ResponseEntity.ok()
            .headers(headers)
            .body(excelData);
    }

    @PostMapping("/tcs-shutdown/import")
    public AOPMessageVM importTCSShutdown(
        @RequestParam String plantId,
        @RequestParam String year,
        @RequestParam("file") MultipartFile file) {

        return tcsShutdownService.importExcel(
            plantId,
            year,
            file);
    }
}

