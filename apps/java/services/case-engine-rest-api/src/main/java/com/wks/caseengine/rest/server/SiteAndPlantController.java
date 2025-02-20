package com.wks.caseengine.rest.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.product.SiteAndPlantDTO;
import com.wks.caseengine.service.NormParameterMonthlyTransactionService;
import com.wks.caseengine.service.PlantService;

@RestController
@RequestMapping("task")
public class SiteAndPlantController {

    private final PlantService plantService;
    
    @Autowired
    private NormParameterMonthlyTransactionService normParameterMonthlyTransactionService;

    // Constructor injection
    public SiteAndPlantController(PlantService plantService) {
        this.plantService = plantService;
    }

    @GetMapping(value = "/getPlantAndSite")
    public ResponseEntity<List<Object>> getPlantAndSite() {
        List<Object[]> listOfSite = plantService.getPlantAndSite();
        
        // Group plants by site ID
        Map<UUID, List<Object[]>> groupedBySite = listOfSite.stream()
                .collect(Collectors.groupingBy(result -> UUID.fromString((String) result[0])));

        List<Object> siteData = new ArrayList<>();

        // Iterate through grouped sites and build the response
        for (Map.Entry<UUID, List<Object[]>> entry : groupedBySite.entrySet()) {
            UUID siteId = entry.getKey();
            List<Object[]> plants = entry.getValue();
            
            // Create the Site object
            SiteResponse siteResponse = new SiteResponse();
            siteResponse.setId(siteId);
            siteResponse.setName((String) plants.get(0)[1]);
            
            List<PlantResponse> plantResponses = new ArrayList<>();
            for (Object[] plant : plants) {
                PlantResponse plantResponse = new PlantResponse();
                plantResponse.setId(UUID.fromString((String) plant[3]));
                plantResponse.setName((String) plant[4]);
                plantResponse.setDisplayName((String) plant[5]);
                plantResponses.add(plantResponse);
            }

            siteResponse.setPlants(plantResponses);
            siteData.add(siteResponse);
        }

        return ResponseEntity.ok(siteData);
    }

    // Inner DTOs for the response format
    public static class SiteResponse {
        private UUID id;
        private String name;
        private List<PlantResponse> plants;

        // Getters and setters
        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<PlantResponse> getPlants() {
            return plants;
        }

        public void setPlants(List<PlantResponse> plants) {
            this.plants = plants;
        }
    }

    public static class PlantResponse {
        private UUID id;
        private String name;
        private String displayName;

        // Getters and setters
        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }
}
