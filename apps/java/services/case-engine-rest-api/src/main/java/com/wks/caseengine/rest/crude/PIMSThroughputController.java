package com.wks.caseengine.rest.crude;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.crude.dto.NormBasisDTO;
import com.wks.caseengine.crude.service.NormBasisService;

@RestController
@RequestMapping("task")
public class PIMSThroughputController {


    @Autowired
    private NormBasisService normBasisService;

    @GetMapping("/pims-throughput")
    public ResponseEntity<List<NormBasisDTO>> getAllPIMSThroughput(@RequestParam String plantId, @RequestParam String aopYear) {
        
        if (plantId == null || plantId.isEmpty() || aopYear == null || aopYear.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
         }
 
         List<NormBasisDTO> normBasisDTOs = normBasisService.getPIMSThroughput(UUID.fromString(plantId), aopYear);
         return ResponseEntity.ok(normBasisDTOs);
    }


    @PostMapping("/pims-throughput")
    public ResponseEntity<Void> updatePimsThroughput(@RequestBody List<NormBasisDTO> normBasisDTOs, @RequestParam String plantId, @RequestParam String aopYear, @RequestParam String siteId, @RequestParam String periodFrom, @RequestParam String periodTo) {
        normBasisService.updatePimsThroughput(normBasisDTOs, UUID.fromString(plantId), aopYear, UUID.fromString(siteId), periodFrom, periodTo);
        return ResponseEntity.ok().build();
    }
}