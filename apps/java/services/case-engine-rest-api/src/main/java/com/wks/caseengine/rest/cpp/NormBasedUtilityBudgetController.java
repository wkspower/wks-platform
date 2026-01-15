package com.wks.caseengine.rest.cpp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wks.caseengine.cpp.dto.norm.NormsMonthUpdateRequestDTO;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.cpp.service.NormBasedUtilityBudgetService;

import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("task")
@Slf4j
public class NormBasedUtilityBudgetController {

    @Autowired
    private NormBasedUtilityBudgetService normBasedUtilityBudgetService;

    @GetMapping("/norm-based-utility-budget")
    public ResponseEntity<?> getNormBasedUtilityBudget(
            @RequestParam UUID cppPlantId,
            @RequestParam String financialYear
    ) {
        try {
            log.info("=== Controller Received Request ===");
            log.info("CPPPlantId: {}", cppPlantId);
            log.info("FinancialYear: {}", financialYear);
            
            AOPMessageVM result = normBasedUtilityBudgetService.getNormBasedUtilityBudget(cppPlantId, financialYear);
            
            log.info("=== Controller Returning Response ===");
            log.info("Response Code: {}", result.getCode());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("=== CONTROLLER EXCEPTION ===", e);
            
            // Create detailed error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("errorType", e.getClass().getName());
            errorResponse.put("errorMessage", e.getMessage());
            
            // Get full stack trace
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            errorResponse.put("stackTrace", sw.toString());
            
            // Get cause if available
            if (e.getCause() != null) {
                errorResponse.put("causeType", e.getCause().getClass().getName());
                errorResponse.put("causeMessage", e.getCause().getMessage());
            }
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }




    //Controller To Save The NormsMonthDetails




    @PostMapping("/saveOrUpdateNormsMonths/{financialYear}")
    public ResponseEntity<?> saveOrUpdateNormsMonth(
            @RequestBody List<NormsMonthUpdateRequestDTO> dtoList,
            @PathVariable String financialYear
    ) {

        try {
            log.info("=== saveOrUpdateNormsMonth BULK Request Received ===");

            if (dtoList == null || dtoList.isEmpty()) {
                AOPMessageVM errorResponse = new AOPMessageVM();
                errorResponse.setCode(400);
                errorResponse.setMessage("Request body cannot be empty");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            log.info("Total records received from frontend: {}", dtoList.size());

            AOPMessageVM response = normBasedUtilityBudgetService.saveOrUpdateBulk(dtoList, financialYear);

            return ResponseEntity.ok(response);

        } catch (RestInvalidArgumentException e) {
            log.error("Validation error: {}", e.getMessage());

            AOPMessageVM errorResponse = new AOPMessageVM();
            errorResponse.setCode(400);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("=== ERROR in saveOrUpdateNormsMonth BULK ===");
            log.error("Type: {}", e.getClass().getName());
            log.error("Message: {}", e.getMessage());
            e.printStackTrace();

            AOPMessageVM errorResponse = new AOPMessageVM();
            errorResponse.setCode(500);
            errorResponse.setMessage("Error: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    





}
