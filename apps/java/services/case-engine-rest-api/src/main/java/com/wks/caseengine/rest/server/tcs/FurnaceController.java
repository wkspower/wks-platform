package com.wks.caseengine.rest.server.tcs;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.tcs.FurnaceDTO;
import com.wks.caseengine.dto.tcs.MasterFurnaceDTO;
import com.wks.caseengine.service.tcs.FurnaceService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("task")
@Tag(name = "Furnace", description = "Furnace")
public class FurnaceController {

    @Autowired
    private FurnaceService furnaceService;

    @GetMapping({"/furnace/{financialYear}/{siteId}/{plantId}" , "/furnace/{financialYear}/{siteId}"})
    public MasterFurnaceDTO getFurnaceData(@PathVariable String financialYear, @PathVariable String siteId, @PathVariable(required = false) String plantId) {

        if (plantId == null) { 
            return furnaceService.getFurnaceData(financialYear, UUID.fromString(siteId), null);
        }
        return furnaceService.getFurnaceData(financialYear, UUID.fromString(siteId), UUID.fromString(plantId));
    }

   @PostMapping("/furnace/{financialYear}/{siteId}/{plantId}")
   public void updateFurnaceData(@RequestBody List<FurnaceDTO> furnaceDTOs, @PathVariable String financialYear, @PathVariable String siteId, @PathVariable String plantId) {
    
    furnaceService.updateFurnaceData(furnaceDTOs, financialYear, UUID.fromString(siteId), UUID.fromString(plantId));
}

}
