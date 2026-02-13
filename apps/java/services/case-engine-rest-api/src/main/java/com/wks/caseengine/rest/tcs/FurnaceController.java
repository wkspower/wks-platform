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
import com.wks.caseengine.tcs.dto.FurnaceDTO;
import com.wks.caseengine.tcs.dto.MasterFurnaceDTO;
import com.wks.caseengine.tcs.service.FurnaceService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("task")
@Tag(name = "Furnace", description = "Furnace")
public class FurnaceController {

    @Autowired
    private FurnaceService furnaceService;

    @GetMapping({"/furnace/{financialYear}/{siteId}/{plantId}" , "/furnace/{financialYear}/{siteId}"})
    public MasterFurnaceDTO getFurnaceData(@PathVariable String financialYear, @PathVariable String siteId, @PathVariable(required = false) String plantId) {

        if (plantId == null) { 
            return furnaceService.getFurnaceData(financialYear, UUID.fromString(siteId), null);
        }
        return furnaceService.getFurnaceData(financialYear, UUID.fromString(siteId), UUID.fromString(plantId));
    }

    @PostMapping("/furnace/carry-forward/{financialYear}/{siteId}/{plantId}")
    public AOPMessageVM carryForwardFurnace(@PathVariable String financialYear, @PathVariable String siteId, @PathVariable String plantId) {
        if(plantId == null || financialYear == null || siteId == null) {
            throw new RestInvalidArgumentException("Invalid request parameters", null);
        }
        return furnaceService.carryForwardFurnace(financialYear, UUID.fromString(siteId), UUID.fromString(plantId));
    }

   @PostMapping("/furnace/{financialYear}/{siteId}/{plantId}")
   public void updateFurnaceData(@RequestBody List<FurnaceDTO> furnaceDTOs, @PathVariable String financialYear, @PathVariable String siteId, @PathVariable String plantId) {
    
    furnaceService.updateFurnaceData(furnaceDTOs, financialYear, UUID.fromString(siteId), UUID.fromString(plantId));
}

    @GetMapping("/furnace/export")
    public ResponseEntity<byte[]> exportFurnace(
        @RequestParam String siteId,
        @RequestParam String financialYear,
        @RequestParam(required = false) String plantId) {

        byte[] excelData = furnaceService.exportFurnace(
            UUID.fromString(siteId),
            financialYear,
            plantId != null ? UUID.fromString(plantId) : null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "Furnace_" + financialYear + ".xlsx");

        return ResponseEntity.ok()
            .headers(headers)
            .body(excelData);
    }

    @PostMapping("/furnace/import")
    public AOPMessageVM importFurnace(
        @RequestParam String siteId,
        @RequestParam String financialYear,
        @RequestParam String plantId,
        @RequestParam("file") MultipartFile file) {

        return furnaceService.importFurnace(
            UUID.fromString(siteId),
            financialYear,
            UUID.fromString(plantId),
            file);
    }

}

