package com.wks.caseengine.rest.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
import com.wks.caseengine.dto.ShutdownNormsValueDTO;
import com.wks.caseengine.dto.SlowdownNormsValueDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ShutdownNormsService;
import com.wks.caseengine.service.SlowdownNormsService;

@RestController
@RequestMapping("task")
public class SlowdownNormsController {
	
	@Autowired
	private SlowdownNormsService slowdownNormsService;
	
	@GetMapping(value="/slowdownNorms")
	public AOPMessageVM getSlowdownNormsData(@RequestParam String year,@RequestParam String plantId,@RequestParam(required=false) String gradeId){
		return	slowdownNormsService.getSlowdownNormsData(year, plantId,gradeId);
	}
	
	@GetMapping(value = "/slowdown-consumption-export")
	public ResponseEntity<byte[]> exportOverallConsumption(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year) {
	    try {
			
	        byte[] excelBytes = slowdownNormsService.exportSlowdownNorms(year,UUID.fromString(plantId),false,null); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("slowdown-consumption.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	
	@PostMapping(value="/slowdownNorms")
	public List<SlowdownNormsValueDTO> saveSlowdownNormsData(@RequestBody List<SlowdownNormsValueDTO> slowdownNormsValueDTOList){
		return	slowdownNormsService.saveSlowdownNormsData(slowdownNormsValueDTOList);
	}
	
	@GetMapping(value="/getSlowdownNormsSPData")
	public List<SlowdownNormsValueDTO> getSlowdownNormsSPData(@RequestParam String year,@RequestParam String plantId){
		return	slowdownNormsService.getSlowdownNormsSPData(year, plantId);
	}

	 @GetMapping("/slowdown-months")
	    public ResponseEntity<List> getSlowdownMonths(@RequestParam UUID plantId,@RequestParam String maintenanceName,@RequestParam String year,@RequestParam(required=false) String gradeId){
	        List data = slowdownNormsService.getSlowdownMonths(plantId, maintenanceName,year,gradeId);
	        return ResponseEntity.ok(data);
	    }
	 
	 @GetMapping(value = "/calculate-slowdown-consumption")
		public AOPMessageVM calculateSlowdownConsumption(@RequestParam String year, @RequestParam String plantId) {
			return slowdownNormsService.getCalculateSlowdownNorms(year, plantId);
		}
	 
	 	@GetMapping(value = "/calculate-slowdown-norms")
		public AOPMessageVM calculateSlowdownNorms(@RequestParam String year, @RequestParam String plantId) {
			return slowdownNormsService.calculateSlowdownNorms(year, plantId);
		}
		
		@GetMapping("/slowdown-consumption-columns")
		  public AOPMessageVM getSlowdownNormsDynamicColumns(@RequestParam String year,@RequestParam String plantId){
			  return slowdownNormsService.getSlowdownNormsDynamicColumns(year,UUID.fromString(plantId));
		  }
		
		@GetMapping(value = "/slowdown-consumption")
	    public AOPMessageVM getSlowdownNormsConfigurationData(@RequestParam String plantId, @RequestParam String year) {
			
			try {
				return slowdownNormsService.getSlowdownNormsConfigurationData(plantId,year);
			}catch(Exception e) {
				e.printStackTrace();
			}
	        return null;
	    }
		
		@PostMapping(value="/slowdown-consumption")
		public AOPMessageVM saveSlowdowNormsConfigurationData(@RequestParam String plantId,@RequestParam String year, @RequestBody List<Map<String, Object>> payload){
			List<NormAttributeTransactionsDTO> dtoList = new ArrayList<>();

		    for (Map<String, Object> item : payload) {
		    	 UUID normParameterId = UUID.fromString(item.get("normParameterFKId").toString());

		        for (Map.Entry<String, Object> entry : item.entrySet()) {
		            String key = entry.getKey();

		            if (!"normParameterFKId".equals(key)) {
		                Object value = entry.getValue();
		                
		                NormAttributeTransactionsDTO dto = new NormAttributeTransactionsDTO();

		                dto.setNormParameterFKId(normParameterId); 
		                dto.setDescription(key);
		                if(value!=null) {
		                	dto.setAttributeValue(value.toString());   
		                }
		                        
		                dtoList.add(dto);
		            }
		        }
		    }
			
			return slowdownNormsService.saveSlowdownNormsConfigurationData(plantId,year,dtoList);		
		}
		
		@GetMapping(value="/slowdown-norms-grades")
		public AOPMessageVM getUniqueGrades(@RequestParam String year,@RequestParam String plantId){
			return	slowdownNormsService.getUniqueGrades(year,plantId);
		}

}

