package com.wks.caseengine.rest.crude;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.crude.service.NormBasisService;
import com.wks.caseengine.message.vm.AOPMessageVM;

import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.UUID;
import com.wks.caseengine.crude.dto.NormBasisDTO;

@RestController
@RequestMapping("task")
public class NormBasisController {
    
    @Autowired
    private NormBasisService normBasisService;


    @GetMapping("/norm-basis")
    public ResponseEntity<List<NormBasisDTO>> getAllNormBasis(@RequestParam String plantId, @RequestParam String aopYear) {

        if (plantId == null || plantId.isEmpty() || aopYear == null || aopYear.isEmpty()) {
           throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        List<NormBasisDTO> normBasisDTOs = normBasisService.getAllNormBasis(UUID.fromString(plantId), aopYear);
        return ResponseEntity.ok(normBasisDTOs);
    }

    @PostMapping("/norm-basis")
    public ResponseEntity<AOPMessageVM> updateNormBasis(@RequestBody List<NormBasisDTO> normBasisDTOs, @RequestParam String plantId, @RequestParam String aopYear, @RequestParam String siteId, @RequestParam String periodFrom, @RequestParam String periodTo) {
        AOPMessageVM aopMessageVM = normBasisService.updateNormBasis(normBasisDTOs, UUID.fromString(plantId), aopYear, UUID.fromString(siteId), periodFrom, periodTo);
        return ResponseEntity.ok(aopMessageVM);
    }


}
