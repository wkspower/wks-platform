package com.wks.caseengine.rest.tcs;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.tcs.service.TCSSiteNetCapacityService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("task")
public class TCSSiteNetCapacityController {
    @Autowired
    private TCSSiteNetCapacityService tcsSiteNetCapacityService;

    @GetMapping("/site-capacity")
    public Map<String, Object> getAllTCSSiteNetCapacity(
            @RequestParam(required = false) String plantId,
            @RequestParam String year,
            @RequestParam String capacityType,
            @RequestParam(required = false) String siteId,
            @RequestParam(required = false) String verticalId)

    {
        log.info("Received request to getAllTCSSiteNetCapacity - plantId: {}, year: {}, capacityType: {}, siteId: {}, verticalId: {}",
                plantId, year, capacityType, siteId, verticalId);
        
        // if PlantId is null, then siteId must not be null
        if (plantId == null && (siteId == null || verticalId == null)) {
            log.error("Validation failed: Plant ID and Site ID or Vertical ID cannot be null");
            throw new RestInvalidArgumentException("Plant ID and Site ID or Vertical ID cannot be null", null);
        }
        if (plantId != null && (siteId != null || verticalId != null)) {
            log.error("Validation failed: Plant ID and Site ID or Vertical ID cannot be provided together");
            throw new RestInvalidArgumentException("Plant ID and Site ID or Vertical ID cannot be provided together",
                    null);
        }

        try {
            log.debug("Calling service method getAll with parameters - plantId: {}, year: {}, capacityType: {}, siteId: {}, verticalId: {}",
                    plantId, year, capacityType, siteId, verticalId);
            
            Map<String, Object> response = tcsSiteNetCapacityService.getAll(
                    plantId,
                    year,
                    capacityType,
                    // uom,
                    siteId,
                    verticalId);
            
            log.info("Successfully retrieved TCS Site Net Capacity data - Response keys: {}", response.keySet());
            log.debug("Response: {}", response);
            
            return response;
        } catch (Exception e) {
            log.error("Error occurred while fetching TCS Site Net Capacity data", e);
            throw e;
        }
    }
}
