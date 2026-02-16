package com.wks.caseengine.rest.tcs;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.tcs.dto.CrudeBlendScreenDTO;
import com.wks.caseengine.tcs.dto.CrudeBlendWindowPostRequestDTO;
import com.wks.caseengine.tcs.service.CrudeBlendWindowService;

@RestController
@RequestMapping("task")
public class CrudeBlendWindowController {   

    @Autowired
    private CrudeBlendWindowService crudeBlendWindowService;
     
    @GetMapping({"/crude-blend-window/{plantId}/{siteId}/{financialYear}" , "/crude-blend-window/{siteId}/{financialYear}"})
    public ResponseEntity<List<CrudeBlendScreenDTO>> getCrudeBlendWindowByCppAndFY(@PathVariable(required = false) String plantId, @PathVariable String siteId, @PathVariable String financialYear) {


        if (plantId == null) {
            return ResponseEntity.ok(crudeBlendWindowService.getCrudeBlendWindowData(null, siteId, financialYear));
        }

        List<CrudeBlendScreenDTO> crudeBlendScreenDTO = crudeBlendWindowService.getCrudeBlendWindowData(plantId, siteId, financialYear);
        return ResponseEntity.ok(crudeBlendScreenDTO);
    }

    @PostMapping("/crude-blend-window/carry-forward/{financialYear}/{siteId}/{plantId}")
    public AOPMessageVM carryForwardCrudeBlendWindow(@PathVariable String financialYear, @PathVariable String siteId, @PathVariable String plantId) {
        if(plantId == null || financialYear == null || siteId == null) {
            throw new RestInvalidArgumentException("Invalid request parameters", null);
        }
        return crudeBlendWindowService.carryForwardCrudeBlendWindow(financialYear, UUID.fromString(siteId), UUID.fromString(plantId));
    }
    
    @PostMapping("/crude-blend-window/{plantId}/{siteId}/{financialYear}/{table}")
    public void updateCrudeBlendWindow(@RequestBody CrudeBlendWindowPostRequestDTO<?> payload, @PathVariable String plantId, @PathVariable String siteId, @PathVariable String financialYear, @PathVariable String table) {
      
       crudeBlendWindowService.updateCrudeBlendWindowData(payload, plantId, siteId, financialYear, table);
        
    }

    @GetMapping("/crude-blend-window/export")
    public ResponseEntity<byte[]> exportCrudeBlendWindow(
        @RequestParam(required = false) String plantId,
        @RequestParam String siteId,
        @RequestParam String financialYear,
        @RequestParam String table) {

        // Default to CrudeBlendWindow if table is not specified
   
       
        if(siteId == null || siteId.isEmpty()) {
            throw new IllegalArgumentException("Site ID is required");
        }
        if(financialYear == null || financialYear.isEmpty()) {
            throw new IllegalArgumentException("Financial year is required");
        }
        if(table == null || table.isEmpty()) {
            throw new IllegalArgumentException("Table is required");
        }

        byte[] excelData = crudeBlendWindowService.exportCrudeBlendWindow(
            plantId,
            siteId,
            financialYear,
            table);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        
        // Set file name based on table
        String fileName = table + "_" + financialYear + ".xlsx";
        headers.setContentDispositionFormData("attachment", fileName);

        return ResponseEntity.ok()
            .headers(headers)
            .body(excelData);
    }

    @PostMapping("/crude-blend-window/import")
    public AOPMessageVM importCrudeBlendWindow(
        @RequestParam String plantId,
        @RequestParam String siteId,
        @RequestParam String financialYear,
        @RequestParam String table,
        @RequestParam("file") MultipartFile file) {

        // Default to CrudeBlendWindow if table is not specified
        if (table == null || table.isEmpty()) {
           throw new IllegalArgumentException("Table is required");
        }

        if(plantId == null || plantId.isEmpty()) {
            throw new IllegalArgumentException("Plant ID is required");
        }
        if(siteId == null || siteId.isEmpty()) {
            throw new IllegalArgumentException("Site ID is required");
        }
        if(financialYear == null || financialYear.isEmpty()) {
            throw new IllegalArgumentException("Financial year is required");
        }

        return crudeBlendWindowService.importExcel(
            plantId,
            siteId,
            financialYear,
            table,
            file);
    }
}

