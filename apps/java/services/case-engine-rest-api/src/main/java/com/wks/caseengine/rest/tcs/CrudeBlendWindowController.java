package com.wks.caseengine.rest.tcs;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    
    @PostMapping("/crude-blend-window/{table}/{financialYear}")
    public void updateCrudeBlendWindow(@RequestBody CrudeBlendWindowPostRequestDTO<?> payload, @PathVariable String table, @PathVariable String financialYear) {
      
       crudeBlendWindowService.updateCrudeBlendWindowData(payload, table, financialYear);
        
    }
}

