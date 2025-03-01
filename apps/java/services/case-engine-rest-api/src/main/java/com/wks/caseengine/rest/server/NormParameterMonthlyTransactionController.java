package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.BusinessDemandDTO;
import com.wks.caseengine.dto.CatalystAttributesDTO;
import com.wks.caseengine.dto.BusinessDemandDTO;
import com.wks.caseengine.service.NormParameterMonthlyTransactionService;

@RestController
//@RequestMapping("task")
public class NormParameterMonthlyTransactionController{
	
	// @Autowired
	// private NormParameterMonthlyTransactionService normParameterMonthlyTransactionService;
	
	/*
	 * @GetMapping(value="/getBusinessDemandData") public String
	 * getBusinessDemandData(@RequestParam int year,@RequestParam UUID
	 * plantId,@RequestParam UUID siteId){
	 * 
	 * String obj =
	 * normParameterMonthlyTransactionService.getBusinessDemandData(year, plantId,
	 * siteId); System.out.println("obj "+obj); return obj; }
	 * 
	 * 
	 * @GetMapping(value="/getProductionNormData") public String
	 * getProductionNormData(@RequestParam int year,@RequestParam UUID
	 * plantId,@RequestParam UUID siteId){
	 * 
	 * String obj =
	 * normParameterMonthlyTransactionService.getProductionNormData(year, plantId,
	 * siteId); System.out.println("obj "+obj); return obj; }
	 * 
	 * 
	 * // s
	 * 
	 * 
	 * @GetMapping(value="/getCosnumptionNormData") public String
	 * getCosnumptionNormData(@RequestParam int year,@RequestParam UUID
	 * plantId,@RequestParam UUID siteId){
	 * 
	 * String obj =
	 * normParameterMonthlyTransactionService.getCosnumptionNormData(year, plantId,
	 * siteId); System.out.println("obj "+obj); return obj; }
	 * 
	 * @PostMapping(value = "/saveBusinessDemand/{plantId}") public
	 * ResponseEntity<BusinessDemandDTO> saveShutdownData(@PathVariable UUID
	 * plantId, @RequestBody BusinessDemandDTO businessDemandDTO) {
	 * normParameterMonthlyTransactionService.saveBusinessDemandData(plantId,
	 * businessDemandDTO); return ResponseEntity.ok(businessDemandDTO); }
	 * 
	 * @PutMapping(value =
	 * "/editBusinessDemandData/{plantMaintenanceTransactionId}") public
	 * ResponseEntity<BusinessDemandDTO> editShutdownData(@PathVariable UUID
	 * plantMaintenanceTransactionId, @RequestBody BusinessDemandDTO
	 * businessDemandDTO) {
	 * normParameterMonthlyTransactionService.editBusinessDemandData(
	 * plantMaintenanceTransactionId,businessDemandDTO); return
	 * ResponseEntity.ok(businessDemandDTO); }
	 * 
	 * @DeleteMapping("/deleteBusinessDemandData") public Boolean
	 * deletePlant(@RequestBody CatalystAttributesDTO catalystAttributesDTO) {
	 * return normParameterMonthlyTransactionService.deleteBusinessDemandData(
	 * catalystAttributesDTO); }
	 * 
	 */


}
