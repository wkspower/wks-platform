package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wks.caseengine.dto.TCSUnitCapacityDTO;
import com.wks.caseengine.dto.TCSUnitCapacityUOMDTO;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.TCSUnitCapacityService;

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
        @RequestParam String uom,
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
            uom,
            siteId,
            verticalId);
    }

    @PostMapping("/tcs-unit-capacity")
    public AOPMessageVM saveOrUpdate(
        @RequestParam String plantId,
        @RequestParam String year,
        @RequestParam String capacityType,
        @RequestParam String uom,
        @RequestBody List<TCSUnitCapacityDTO> payload) {

        return tcsUnitCapacityService.saveOrUpdate(
            plantId,
            year,
            capacityType,
            uom,
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
}
