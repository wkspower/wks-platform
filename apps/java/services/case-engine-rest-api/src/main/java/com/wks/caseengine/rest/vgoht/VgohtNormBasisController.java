package com.wks.caseengine.rest.vgoht;

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

import com.wks.caseengine.dto.ManualExclusionDatesDTO;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.vgoht.dto.ShutDownCatChemDTO;
import com.wks.caseengine.vgoht.dto.VgohtNormConfigurationDTO;
import com.wks.caseengine.vgoht.serviceimpl.VgohtNormBasisServiceImpl;

import org.springframework.web.bind.annotation.RequestMapping;
import java.util.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;

@RestController
@RequestMapping("task")
public class VgohtNormBasisController {
    
    @Autowired
    private VgohtNormBasisServiceImpl vgohtNormBasisServiceImpl;
    
    @Autowired
   	private PlantsRepository plantsRepository;

    @Autowired
   	private SiteRepository siteRepository;

    @Autowired
   	private VerticalsRepository verticalRepository;


	@GetMapping(value="/vgoht/norms-basis")
	public AOPMessageVM getConfigurationData(@RequestParam String year,@RequestParam UUID plantFKId,@RequestParam(required=false) String version) {
        if (plantFKId == null || year == null || year.isEmpty()) {
           throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }
		return vgohtNormBasisServiceImpl.getConfigurationData(year,plantFKId,version);
	}

    @PostMapping(value = "/vgoht/norms-basis")
    public AOPMessageVM saveConfigurationData(
            @RequestParam String year,
            @RequestParam UUID plantFKId,
            @RequestParam(required = false) String version,
            @RequestBody List<VgohtNormConfigurationDTO> vgohtNormConfigurationDTOList, @RequestParam String periodFrom, @RequestParam String periodTo) {

        if (plantFKId == null || year == null || year.isEmpty()||vgohtNormConfigurationDTOList == null || vgohtNormConfigurationDTOList.isEmpty()) {
            throw new IllegalArgumentException("Plant ID, AOP Year and Configuration Data are required");
        }

        return vgohtNormBasisServiceImpl.saveConfigurationData(year, plantFKId, version, vgohtNormConfigurationDTOList, periodFrom, periodTo);
    }

    @GetMapping(value = "/vgoht/norms-basis/calculate")
    public AOPMessageVM calculateNorms(
            @RequestParam String year,
            @RequestParam UUID plantFKId,
            @RequestParam(required = false) String version, @RequestParam String periodFrom, @RequestParam String periodTo) {

        if (plantFKId == null || year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Plant ID, AOP Year are required");
        }

        return vgohtNormBasisServiceImpl.calculateNorms(year, plantFKId, version, periodFrom, periodTo);
    }

    @PostMapping(value = "/vgoht/norms-basis/constant")
    public AOPMessageVM saveYearlyValues(
        @RequestParam String year,
        @RequestParam UUID plantFKId,
        @RequestBody List<VgohtNormConfigurationDTO> yearlyValuesList, @RequestParam String periodFrom, @RequestParam String periodTo)  {

        if (plantFKId == null || year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        return vgohtNormBasisServiceImpl.saveYearlyValues(year, plantFKId, yearlyValuesList, periodFrom, periodTo);
    }

    @PostMapping(value = "/vgoht/norms-basis/constant/import", consumes = "multipart/form-data")
    public AOPMessageVM importYearlyValues(
            @RequestParam String year,
            @RequestParam UUID plantFKId,
            @RequestParam(required = false) String periodFrom,
            @RequestParam(required = false) String periodTo,
            @RequestParam("file") MultipartFile file
    ) {
        if (plantFKId == null || year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        return vgohtNormBasisServiceImpl.importYearlyValues(
                year, plantFKId, periodFrom, periodTo, file
        );
    }

    @PostMapping(value = "/mannual-entry/monthly")
    public AOPMessageVM saveMonthlyValues(
            @RequestParam String year,
            @RequestParam UUID plantFKId,
            @RequestBody List<VgohtNormConfigurationDTO> monthlyValuesList,
            @RequestParam String periodFrom,
            @RequestParam String periodTo) {

        if (plantFKId == null || year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        return vgohtNormBasisServiceImpl.saveMonthlyValues(
                year,
                plantFKId,
                monthlyValuesList,
                periodFrom,
                periodTo);
    }

    @PostMapping(value = "/mannual-entry/import", consumes = "multipart/form-data")
    public AOPMessageVM importMonthlyValues(
            @RequestParam String year,
            @RequestParam UUID plantFKId,
            @RequestParam(required = false) String periodFrom,
            @RequestParam(required = false) String periodTo,
            @RequestParam("file") MultipartFile file) {

        if (plantFKId == null || year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        return vgohtNormBasisServiceImpl.importMonthlyValues(
                year,
                plantFKId,
                periodFrom,
                periodTo,
                file
        );
    }

    @GetMapping(value="/vgoht/norms-basis/constant")
    public AOPMessageVM getYearlyValues(@RequestParam String year, @RequestParam UUID plantFKId) {
        if (plantFKId == null || year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        return vgohtNormBasisServiceImpl.getYearlyValues(year, plantFKId);
    }

    @GetMapping(value="/mannual-entry/monthly")
    public AOPMessageVM getMonthlyValues(@RequestParam String year, @RequestParam UUID plantFKId) {
        if (plantFKId == null || year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        return vgohtNormBasisServiceImpl.getMonthlyValues(year, plantFKId);
    }

    @GetMapping(value = "/vgoht/norms-basis/constant/export")
    public ResponseEntity<byte[]> exportYearlyValues(
            @RequestParam String year,
            @RequestParam UUID plantFKId
    ) {
        try {

            byte[] excelBytes = vgohtNormBasisServiceImpl
                    .exportYearlyValues(year, plantFKId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("norms_basis_constant.xlsx")
                    .build());

            headers.setContentLength(excelBytes.length);

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/mannual-entry/export")
    public ResponseEntity<byte[]> exportMonthlyValues(
            @RequestParam String year,
            @RequestParam UUID plantFKId) {

        try {

            byte[] excelBytes = vgohtNormBasisServiceImpl
                    .exportMonthlyValues(year, plantFKId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("norms_basis_constant_monthly.xlsx")
                    .build());

            headers.setContentLength(excelBytes.length);

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }    

    @GetMapping(value="/vgoht/norms-basis/constant-sp")
	public AOPMessageVM getConfigurationConstants(@RequestParam String year,@RequestParam String plantFKId) {
		return vgohtNormBasisServiceImpl.getConfigurationConstants(year,plantFKId);
	}

    @GetMapping("vgoht/load-button-norm-calculation")
    public ResponseEntity<AOPMessageVM> loadButtonNormCalculation(@RequestParam String plantId, @RequestParam String aopYear, @RequestParam String siteId, @RequestParam String periodFrom, @RequestParam String periodTo) {
        AOPMessageVM aopMessageVM = vgohtNormBasisServiceImpl.LoadButtonNormCalculation(UUID.fromString(plantId), aopYear, UUID.fromString(siteId), periodFrom, periodTo);
        return ResponseEntity.ok(aopMessageVM);
    }

    @GetMapping("vgoht/norms-basis/production-demand")
    public AOPMessageVM getProductionDemand(@RequestParam String year, @RequestParam UUID plantFKId) {
        return vgohtNormBasisServiceImpl.getProductionDemand(year, plantFKId);
    }

    @PostMapping("vgoht/norms-basis/production-demand")
    public AOPMessageVM saveProductionDemand(@RequestParam String year, @RequestParam UUID plantFKId, @RequestBody List<VgohtNormConfigurationDTO> productionDemandList) {
        return vgohtNormBasisServiceImpl.saveProductionDemand(year, plantFKId, productionDemandList);
    }

    @GetMapping(value="/vgoht/constant")
    public AOPMessageVM getConfigurationDataWithTwoValues(@RequestParam String year, @RequestParam UUID plantFKId) {
        if (plantFKId == null || year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        Plants plant = plantsRepository.findById((plantFKId)).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

        boolean vgoht = vertical.getName().equalsIgnoreCase("VGOHT");
        boolean dht1 = plant.getName().equalsIgnoreCase("DHT1");
        boolean dht2 = plant.getName().equalsIgnoreCase("DHT2");
        boolean VGOHT1 = plant.getName().equalsIgnoreCase("VGOHT1");
        boolean VGOHT2 = plant.getName().equalsIgnoreCase("VGOHT2");
        boolean VGOHT3 = plant.getName().equalsIgnoreCase("VGOHT3");
        boolean VGOHT4 = plant.getName().equalsIgnoreCase("VGOHT4");

        boolean isVgohtWithThreeValues = vgoht && (dht1 || dht2 || VGOHT1 || VGOHT2 || VGOHT3 || VGOHT4);
       
        if(isVgohtWithThreeValues) {
        	return vgohtNormBasisServiceImpl.getConfigurationDataWithThreeValues(year, plantFKId);
        }else {
        	return vgohtNormBasisServiceImpl.getConfigurationDataWithTwoValues(year, plantFKId);
        }      
    }

    @PostMapping(value = "/vgoht/constant")
    public AOPMessageVM saveConfigurationDataWithTwoValues(
            @RequestParam String year,
            @RequestParam UUID plantFKId,
            @RequestBody List<VgohtNormConfigurationDTO> configurationDataList) {

        if (plantFKId == null || year == null || year.trim().isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        Plants plant = plantsRepository.findById(plantFKId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID associated with the plant"));

        boolean vgoht = vertical.getName().equalsIgnoreCase("VGOHT");
        boolean dht1 = plant.getName().equalsIgnoreCase("DHT1");
        boolean dht2 = plant.getName().equalsIgnoreCase("DHT2");
        boolean VGOHT1 = plant.getName().equalsIgnoreCase("VGOHT1");
        boolean VGOHT2 = plant.getName().equalsIgnoreCase("VGOHT2");
        boolean VGOHT3 = plant.getName().equalsIgnoreCase("VGOHT3");
        boolean VGOHT4 = plant.getName().equalsIgnoreCase("VGOHT4");

        boolean isVgohtWithThreeValues = vgoht && (dht1 || dht2 || VGOHT1 || VGOHT2 || VGOHT3 || VGOHT4);

        // Declare variable outside the if/else block so it is accessible in the return check
        List<VgohtNormConfigurationDTO> failedRecords;

        if(isVgohtWithThreeValues) {
            failedRecords = vgohtNormBasisServiceImpl.saveConfigurationDataWithThreeValues(year, plantFKId, configurationDataList);
        } else {
            failedRecords = vgohtNormBasisServiceImpl.saveConfigurationDataWithTwoValues(year, plantFKId, configurationDataList);
        }

        if (failedRecords == null || failedRecords.isEmpty()) {
            return new AOPMessageVM(200, "Configuration data saved successfully", null);
        } else {
            return new AOPMessageVM(400, "Partial Data Saved", failedRecords);
        }
    }
    
    @GetMapping(value = "/vgoht/constant/export")
    public ResponseEntity<byte[]> exportConfigurationDataWithTwoValues(
            @RequestParam String year,
            @RequestParam UUID plantFKId) {

        if (plantFKId == null || year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        try {
        	 Plants plant = plantsRepository.findById(plantFKId)
                     .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

             Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
                     .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID associated with the plant"));

                     
        boolean vgoht = vertical.getName().equalsIgnoreCase("VGOHT");
        boolean dht1 = plant.getName().equalsIgnoreCase("DHT1");
        boolean dht2 = plant.getName().equalsIgnoreCase("DHT2");
        boolean VGOHT1 = plant.getName().equalsIgnoreCase("VGOHT1");
        boolean VGOHT2 = plant.getName().equalsIgnoreCase("VGOHT2");
        boolean VGOHT3 = plant.getName().equalsIgnoreCase("VGOHT3");
        boolean VGOHT4 = plant.getName().equalsIgnoreCase("VGOHT4");

        boolean isVgohtWithThreeValues = vgoht && (dht1 || dht2 || VGOHT1 || VGOHT2 || VGOHT3 || VGOHT4);

             byte[] excelBytes;
             if(isVgohtWithThreeValues) {
            	  excelBytes = vgohtNormBasisServiceImpl
                         .exportConfigurationDataWithThreeValues(year, plantFKId, false, null);
             }else {
            	 excelBytes = vgohtNormBasisServiceImpl
                         .exportConfigurationDataWithTwoValues(year, plantFKId, false, null);
             }
             

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("norms_basis_two_values.xlsx")
                    .build());
            headers.setContentLength(excelBytes.length);

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/vgoht/constant/import", consumes = "multipart/form-data")
    public AOPMessageVM importConfigurationDataWithTwoValues(
            @RequestParam String year,
            @RequestParam UUID plantFKId,
            @RequestParam("file") MultipartFile file) {

        if (plantFKId == null || year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }
        Plants plant = plantsRepository.findById(plantFKId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID associated with the plant"));

                
        boolean vgoht = vertical.getName().equalsIgnoreCase("VGOHT");
        boolean dht1 = plant.getName().equalsIgnoreCase("DHT1");
        boolean dht2 = plant.getName().equalsIgnoreCase("DHT2");
        boolean VGOHT1 = plant.getName().equalsIgnoreCase("VGOHT1");
        boolean VGOHT2 = plant.getName().equalsIgnoreCase("VGOHT2");
        boolean VGOHT3 = plant.getName().equalsIgnoreCase("VGOHT3");
        boolean VGOHT4 = plant.getName().equalsIgnoreCase("VGOHT4");

        boolean isVgohtWithThreeValues = vgoht && (dht1 || dht2 || VGOHT1 || VGOHT2 || VGOHT3 || VGOHT4);
                
                if(isVgohtWithThreeValues) {
        	 return vgohtNormBasisServiceImpl.importConfigurationDataWithThreeValues(year, plantFKId, file);
        }else {
        	 return vgohtNormBasisServiceImpl.importConfigurationDataWithTwoValues(year, plantFKId, file);
        }  
    }

    // fetch summer winter as april and oct
    @GetMapping(value="/utility-consumption")
    public AOPMessageVM getUtilityConsumption(@RequestParam String year, @RequestParam String plantFKId) {
        if (plantFKId == null || year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        return vgohtNormBasisServiceImpl.getUtilityConsumption(year, plantFKId);
    }

    @PostMapping(value = "/utility-consumption")
    public AOPMessageVM saveUtilityConsumption(
        @RequestParam String year,
        @RequestParam UUID plantFKId,
        @RequestBody List<VgohtNormConfigurationDTO> configurationDataList)  {

        if (plantFKId == null || year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        List<VgohtNormConfigurationDTO> failedRecords = vgohtNormBasisServiceImpl.saveUtilityConsumption(year, plantFKId, configurationDataList);

        if(failedRecords.isEmpty()) {
            return new AOPMessageVM(200, "Utility consumption data saved successfully", null);
        } else {
            return new AOPMessageVM(400, "Partial Data Saved", failedRecords);
        }
    }

    @GetMapping(value = "/utility-consumption-export")
	public ResponseEntity<byte[]> exportUtilityConsumptionData(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = vgohtNormBasisServiceImpl.exportUtilityConsumptionData(year,plantId,false,null); 

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("utility-consumption.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PostMapping(value = "/utility-consumption-import", consumes = "multipart/form-data")
	public AOPMessageVM importUtilityConsumptionData(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	vgohtNormBasisServiceImpl.importUtilityConsumptionData(year,UUID.fromString(plantId), file); 
	}


    @GetMapping(value="/cat-chem")
    public AOPMessageVM getCatChemData(@RequestParam String year, @RequestParam String plantId) {
        if (plantId == null || year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        return vgohtNormBasisServiceImpl.getCatChemData(year, plantId);
    }

    @PostMapping(value = "/cat-chem")
    public AOPMessageVM saveCatChemData(
        @RequestParam String year,
        @RequestParam UUID plantId,
        @RequestBody List<VgohtNormConfigurationDTO> configurationDataList)  {

        if (plantId == null || year == null || year.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        List<VgohtNormConfigurationDTO> failedRecords = vgohtNormBasisServiceImpl.saveCatChemData(year, plantId, configurationDataList);

        if(failedRecords.isEmpty()) {
            return new AOPMessageVM(200, "Cat-Chem data saved successfully", null);
        } else {
            return new AOPMessageVM(400, "Partial Data Saved", failedRecords);
        }

    }
    
    @GetMapping(value = "/cat-chem-export")
	public ResponseEntity<byte[]> exportCatChemData(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = vgohtNormBasisServiceImpl.exportCatChemData(year,plantId,false,null,false); 

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("CatChemData.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PostMapping(value = "/cat-chem-import", consumes = "multipart/form-data")
	public AOPMessageVM importCatChemData(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	vgohtNormBasisServiceImpl.importCatChemData(year,UUID.fromString(plantId), file,false); 
	}

    @GetMapping(value="/manual-exclusion-dates")
    public AOPMessageVM getManualExclusionDates(@RequestParam String plantFKId, @RequestParam String year) {
        return vgohtNormBasisServiceImpl.getManualExclusionDates(plantFKId, year);
    }

    @PostMapping(value="/manual-exclusion-dates")
    public AOPMessageVM saveManualExclusionDates(@RequestParam String year, @RequestBody List<ManualExclusionDatesDTO> manualExclusionDatesDTOList) {
       List<ManualExclusionDatesDTO> failedRecords = vgohtNormBasisServiceImpl.saveManualExclusionDates( manualExclusionDatesDTOList, year);

       if(failedRecords.isEmpty()) {
        return new AOPMessageVM(200, "Manual exclusion dates saved successfully", null);
       } else {
        return new AOPMessageVM(400, "Partial Data Saved", failedRecords);
       }
    }

    @DeleteMapping (value="/manual-exclusion-dates")
    public AOPMessageVM deleteManualExclusionDates(@RequestBody List<ManualExclusionDatesDTO> manualExclusionDatesDTOList) {
        return vgohtNormBasisServiceImpl.deleteManualExclusionDates(manualExclusionDatesDTOList);
    }

    @GetMapping(value="/shut-down-cat-chem")
    public AOPMessageVM getShutDownCatChemData(@RequestParam String year, @RequestParam String plantId) {
        return vgohtNormBasisServiceImpl.getShutDownCatChemData(year, UUID.fromString(plantId));
    }

    @PostMapping(value = "/shut-down-cat-chem")
    public AOPMessageVM saveShutDownCatChemData(@RequestParam String year, @RequestParam UUID plantId, @RequestBody List<ShutDownCatChemDTO> shutDownCatChemDTOList) {
        List<ShutDownCatChemDTO> failedRecords = vgohtNormBasisServiceImpl.saveShutDownCatChemData(year, plantId.toString(), shutDownCatChemDTOList);
        if(failedRecords.isEmpty()) {
            return new AOPMessageVM(200, "Shut down cat chem data saved successfully", null);
        } else {
            return new AOPMessageVM(400, "Partial Data Saved", failedRecords);
        }
    }

}
