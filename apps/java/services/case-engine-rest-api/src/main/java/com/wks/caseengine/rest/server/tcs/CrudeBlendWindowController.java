package com.wks.caseengine.rest.server.tcs;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.tcs.CrudeBlendScreenDTO;
import com.wks.caseengine.dto.tcs.CrudeBlendWindowPostRequestDTO;
import com.wks.caseengine.service.tcs.CrudeBlendWindowService;

@RestController
@RequestMapping("task")
public class CrudeBlendWindowController {   

    @Autowired
    private CrudeBlendWindowService crudeBlendWindowService;
     
    @GetMapping("/crude-blend-window/{plantId}/{siteId}/{financialYear}")
    public ResponseEntity<List<CrudeBlendScreenDTO>> getCrudeBlendWindowByCppAndFY(@PathVariable String plantId, @PathVariable String siteId, @PathVariable String financialYear) {

        List<CrudeBlendScreenDTO> crudeBlendScreenDTO = crudeBlendWindowService.getCrudeBlendWindowData(plantId, siteId, financialYear);
        return ResponseEntity.ok(crudeBlendScreenDTO);
    }
    
    @PostMapping("/crude-blend-window/{table}/{financialYear}")
    public void updateCrudeBlendWindow(@RequestBody CrudeBlendWindowPostRequestDTO<?> payload, @PathVariable String table, @PathVariable String financialYear) {
      
       crudeBlendWindowService.updateCrudeBlendWindowData(payload, table, financialYear);
        
    }
}
