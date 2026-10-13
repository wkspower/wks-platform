package com.wks.caseengine.rest.RefineryUtility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.message.vm.AOPMessageVM;

import org.springframework.web.bind.annotation.RequestMapping;
import java.util.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;

import com.wks.caseengine.RefineryUtility.dto.CommoditySelectionDTO;
import com.wks.caseengine.RefineryUtility.dto.ConsumerDemandDTO;
import com.wks.caseengine.RefineryUtility.dto.MonthWiseConstantsDTO;
import com.wks.caseengine.RefineryUtility.dto.SelectedPlantOwnerDTO;
import com.wks.caseengine.RefineryUtility.dto.TreatmentVendorConsumptionDTO;
import com.wks.caseengine.RefineryUtility.dto.TreatmentVendorDTO;
import com.wks.caseengine.RefineryUtility.service.RefineryUtilityConfigurationService;

@RestController
@RequestMapping("task")
public class RefineryUtilityConfigurationController {


    @Autowired
    private RefineryUtilityConfigurationService refineryUtilityConfigurationService;

    @GetMapping("/refinery-utility-constants")
    public ResponseEntity<AOPMessageVM> getMonthWiseConstants(@RequestParam String year, @RequestParam String plantFKId) {
        return ResponseEntity.ok(refineryUtilityConfigurationService.getMonthWiseConstants(year, plantFKId));
    }

    @PostMapping("/refinery-utility-constants")
    public ResponseEntity<AOPMessageVM> saveMonthWiseConstants(@RequestBody List<MonthWiseConstantsDTO> monthWiseConstantsDTOList, @RequestParam String year, @RequestParam String plantFKId) {
       List<MonthWiseConstantsDTO> failedList = refineryUtilityConfigurationService.saveMonthWiseConstants(year, plantFKId, monthWiseConstantsDTOList);
      
       if(failedList.isEmpty()) {
        return ResponseEntity.ok(new AOPMessageVM(200, "Data saved successfully", null));
       } else {
        return ResponseEntity.ok(new AOPMessageVM(400, "Partial Data Updated", failedList));
       }
    }
    @GetMapping(value = "/refinery-utility-constants-export")
	public ResponseEntity<byte[]> exportMonthWiseConstants(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = refineryUtilityConfigurationService.exportMonthWiseConstants(year,plantId,false,null); 

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("MonthWiseConstants.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PostMapping(value = "/refinery-utility-constants-import", consumes = "multipart/form-data")
	public AOPMessageVM importMonthWiseConstants(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	refineryUtilityConfigurationService.importMonthWiseConstants(year,UUID.fromString(plantId), file); 
	}

	@GetMapping("/refinery-utility/check-is-summer-winter-plant")
	public ResponseEntity<AOPMessageVM> checkIsSummerWinterPlant(@RequestParam String plantId) {
		return ResponseEntity.ok(refineryUtilityConfigurationService.checkIsSummerWinterPlant(plantId));
	}

	@GetMapping("/treatment-vendor")
    public ResponseEntity<AOPMessageVM> getTreatmentVendorData(@RequestParam String year, @RequestParam String plantFKId) {
        return ResponseEntity.ok(refineryUtilityConfigurationService.getTreatmentVendorData(year, plantFKId));
    }

	@PostMapping("/treatment-vendor")
    public ResponseEntity<AOPMessageVM> saveTreatmentVendorData(@RequestBody List<TreatmentVendorDTO> treatmentVendorDTOList, @RequestParam String year, @RequestParam String plantFKId) {
        List<TreatmentVendorDTO> failedList = refineryUtilityConfigurationService.saveTreatmentVendorData(year, plantFKId, treatmentVendorDTOList);
        if(failedList.isEmpty()) {
            return ResponseEntity.ok(new AOPMessageVM(200, "Data saved successfully", null));
        } else {
            return ResponseEntity.ok(new AOPMessageVM(400, "Partial Data Updated", failedList));
        }
    }

	@DeleteMapping("/treatment-vendor")
	public ResponseEntity<AOPMessageVM> deleteTreatmentVendorData(
			@RequestParam String normParameterFKId,
			@RequestParam String year) {
		return ResponseEntity.ok(refineryUtilityConfigurationService.deleteTreatmentVendorData(normParameterFKId, year));
	}

	@GetMapping("/commodity-chemicals")
    public ResponseEntity<AOPMessageVM> getCommodityChemicalsData(@RequestParam String year, @RequestParam String plantFKId) {
        return ResponseEntity.ok(refineryUtilityConfigurationService.getCommodityChemicalsData(year, plantFKId));
    }

	@PostMapping("/commodity-chemicals")
    public ResponseEntity<AOPMessageVM> saveCommodityChemicalsData(@RequestBody List<CommoditySelectionDTO> commoditySelectionDTOList, @RequestParam String year, @RequestParam String plantFKId) {
        List<CommoditySelectionDTO> failedList = refineryUtilityConfigurationService.saveCommodityChemicalsData(year, plantFKId, commoditySelectionDTOList);
        if(failedList.isEmpty()) {
            return ResponseEntity.ok(new AOPMessageVM(200, "Data saved successfully", null));
        } else {
            return ResponseEntity.ok(new AOPMessageVM(400, "Partial Data Updated", failedList));
        }
    }

	@GetMapping("/plant-owner-dropdown")
	public ResponseEntity<AOPMessageVM> getPlantOwnerDropdown(@RequestParam String plantFKId) {
		return ResponseEntity.ok(refineryUtilityConfigurationService.getPlantOwnerDropdown(plantFKId));
	}

	@GetMapping("/selected-plant-owner")
	public ResponseEntity<AOPMessageVM> getSelectedPlantOwner(@RequestParam String plantFKId) {
		return ResponseEntity.ok(refineryUtilityConfigurationService.getSelectedPlantOwner(plantFKId));
	}

	@PostMapping("/selected-plant-owner")
	public ResponseEntity<AOPMessageVM> saveSelectedPlantOwner(
			@RequestBody List<SelectedPlantOwnerDTO> dtoList,
			@RequestParam String plantFKId) {
		List<SelectedPlantOwnerDTO> failedList = refineryUtilityConfigurationService.saveSelectedPlantOwner(plantFKId, dtoList);
		if (failedList.isEmpty()) {
			return ResponseEntity.ok(new AOPMessageVM(200, "Data saved successfully", null));
		} else {
			return ResponseEntity.ok(new AOPMessageVM(400, "Partial Data Updated", failedList));
		}
	}

	@GetMapping("/consumer-demand")
	public ResponseEntity<AOPMessageVM> getConsumerDemandData(@RequestParam String year, @RequestParam String plantFKId) {
		return ResponseEntity.ok(refineryUtilityConfigurationService.getConsumerDemandData(year, plantFKId));
	}

	@PostMapping("/consumer-demand")
	public ResponseEntity<AOPMessageVM> saveConsumerDemandData(@RequestBody List<ConsumerDemandDTO> consumerDemandDTOList, @RequestParam String year, @RequestParam String plantFKId) {
		List<ConsumerDemandDTO> failedList = refineryUtilityConfigurationService.saveConsumerDemandData(year, plantFKId, consumerDemandDTOList);
		if(failedList.isEmpty()) {
			return ResponseEntity.ok(new AOPMessageVM(200, "Data saved successfully", null));
		} else {
			return ResponseEntity.ok(new AOPMessageVM(400, "Partial Data Updated", failedList));
		}
	}

	@GetMapping("/treatment-vendor-consumption")
	public ResponseEntity<AOPMessageVM> getTreatmentVendorConsumption(@RequestParam String year, @RequestParam String plantFKId) {
		return ResponseEntity.ok(refineryUtilityConfigurationService.getTreatmentVendorConsumption(year, plantFKId));
	}

	@GetMapping("/commodity-chemicals-consumption")
	public ResponseEntity<AOPMessageVM> getCommodityChemicalsConsumption(@RequestParam String year, @RequestParam String plantFKId) {
		return ResponseEntity.ok(refineryUtilityConfigurationService.getCommodityChemicalsConsumption(year, plantFKId));
	}

	// Comman API to save treatment-vendor-consumption and commodity-chemicals-consumption tabs
	@PostMapping("/refinary-consumption-transactions")
    public ResponseEntity<AOPMessageVM> saveRefinaryConsumptionTransactions(@RequestBody List<TreatmentVendorConsumptionDTO> treatmentVendorConsumptionDTOList, @RequestParam String year, @RequestParam String plantFKId) {
        List<TreatmentVendorConsumptionDTO> failedList = refineryUtilityConfigurationService.saveRefinaryConsumptionTransaction(year, plantFKId, treatmentVendorConsumptionDTOList);
        if(failedList.isEmpty()) {
            return ResponseEntity.ok(new AOPMessageVM(200, "Data saved successfully", null));
        } else {
            return ResponseEntity.ok(new AOPMessageVM(400, "Partial Data Updated", failedList));
        }
    }

}
