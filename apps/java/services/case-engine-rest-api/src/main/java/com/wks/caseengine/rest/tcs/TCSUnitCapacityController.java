package com.wks.caseengine.rest.tcs;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.tcs.dto.TCSUnitCapacityDTO;
import com.wks.caseengine.tcs.dto.TCSUnitCapacityUOMDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.tcs.service.TCSUnitCapacityService;

@RestController
@RequestMapping("task")
public class TCSUnitCapacityController {

    @Autowired
    private TCSUnitCapacityService tcsUnitCapacityService;

    @GetMapping("/tcs-unit-capacity")
    public Map<String, Object> getAllTCSUnitCapacity(
        @RequestParam(required = false) String plantId,
        @RequestParam String year,
        @RequestParam String capacityType,
      //  @RequestParam(required = false) String uom,
        @RequestParam(required = false) String siteId,
        @RequestParam(required = false) String verticalId)
       
        {
            //if PlantId is null, then siteId must not be null
if (plantId == null && (siteId == null || verticalId == null)) {
    throw new RestInvalidArgumentException("Plant ID and Site ID or Vertical ID cannot be null", null);
}
if (plantId != null && (siteId != null || verticalId != null)) {
    throw new RestInvalidArgumentException("Plant ID and Site ID or Vertical ID cannot be provided together", null);
}

        return tcsUnitCapacityService.getAll(
            plantId,
            year,
            capacityType,
      //      uom,
            siteId,
            verticalId);
    }

    @PostMapping("/tcs-unit-capacity/carry-forward")
    public AOPMessageVM carryForwardTCSUnitCapacity(
        @RequestParam String plantId,
        @RequestParam String year,
        @RequestParam String capacityType
        ) {
        
        return tcsUnitCapacityService.carryForwardTCSUnitCapacity(
            plantId,
            year,
            capacityType
           );
    }

    @PostMapping("/tcs-unit-capacity")
    public AOPMessageVM saveOrUpdate(
        @RequestParam String plantId,
        @RequestParam String year,
        @RequestParam String capacityType,
     //   @RequestParam(required = false) String uom,
        @RequestBody List<TCSUnitCapacityDTO> payload) {

        return tcsUnitCapacityService.saveOrUpdate(
            plantId,
            year,
            capacityType,
        //    uom,
            payload);
    }

    @GetMapping("/tcs-unit-capacity/uom")
    public List<TCSUnitCapacityUOMDTO> getAllUOM(
        @RequestParam (required = false) String plantId,
        @RequestParam String year,
        @RequestParam String capacityType,
        @RequestParam(required = false) String verticalId
    ) {

        if (plantId == null &&  verticalId == null) {
            throw new RestInvalidArgumentException("Plant ID and Vertical ID cannot be null", null);
        }
        if (plantId != null && verticalId != null) {
            throw new RestInvalidArgumentException("Plant ID and Vertical ID cannot be provided together", null);
        }

        return tcsUnitCapacityService.getAllUOM(
            plantId,
            year,
            capacityType,
            verticalId);
    }

    @GetMapping("/tcs-unit-capacity/export")
    public ResponseEntity<byte[]> exportTCSUnitCapacity(
        @RequestParam(required = false) String plantId,
        @RequestParam String year,
        @RequestParam String capacityType,
        @RequestParam(required = false) String siteId,
        @RequestParam(required = false) String verticalId) {

        if (plantId == null && (siteId == null || verticalId == null)) {
            throw new RestInvalidArgumentException("Plant ID and Site ID or Vertical ID cannot be null", null);
        }
        if (plantId != null && (siteId != null || verticalId != null)) {
            throw new RestInvalidArgumentException("Plant ID and Site ID or Vertical ID cannot be provided together", null);
        }

        byte[] excelData = tcsUnitCapacityService.exportTCSUnitCapacity(
            plantId,
            year,
            capacityType,
            siteId,
            verticalId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "TCSUnitCapacity_" + year + ".xlsx");

        return ResponseEntity.ok()
            .headers(headers)
            .body(excelData);
    }

   

    @PostMapping("/tcs-unit-capacity/import")
    public AOPMessageVM importTCSUnitCapacity(
        @RequestParam String plantId,
        @RequestParam String year,
        @RequestParam String capacityType,
        @RequestParam("file") MultipartFile file) {

        return tcsUnitCapacityService.importExcel(
            plantId,
            year,
            capacityType,
            file);
    }
}

