package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.service.PlantService;

@RestController
@RequestMapping("task")
public class SiteAndPlantController {

    private final PlantService plantService;

    // Constructor injection
    public SiteAndPlantController(PlantService plantService) {
        this.plantService = plantService;
    }

    @GetMapping(value = "/getPlantAndSite")
    public ResponseEntity<List> getPlantAndSite() {
        List listOfSite = plantService.getPlantAndSite();
        return ResponseEntity.ok(listOfSite);
    }
}
