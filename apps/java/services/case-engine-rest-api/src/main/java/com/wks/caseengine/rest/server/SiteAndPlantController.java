package com.wks.caseengine.rest.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.product.SiteAndPlantDTO;
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
    public ResponseEntity<List<SiteAndPlantDTO>> getPlantAndSite() {
        List<Object[]> listOfSite = plantService.getPlantAndSite();
        List<SiteAndPlantDTO> dtoList = new ArrayList<>();
        for (Object[] result : listOfSite) {
            SiteAndPlantDTO dto = new SiteAndPlantDTO();
            dto.setSiteId(UUID.fromString((String) result[0])); // Convert String to UUID
            dto.setSiteName((String) result[1]);
            dto.setDisplayName((String) result[2]);
            dto.setPlantId(UUID.fromString((String) result[3])); // Convert String to UUID
            dto.setPlantName((String) result[4]);
            dto.setPlantDisplayName((String) result[5]);
            dtoList.add(dto);
        }

        return ResponseEntity.ok(dtoList);
    }
}
