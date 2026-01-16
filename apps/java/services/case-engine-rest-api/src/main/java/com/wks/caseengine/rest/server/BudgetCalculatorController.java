package com.wks.caseengine.rest.server;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for Power Plant Budget Calculator.
 * This controller exposes endpoints to calculate power plant budgets
 * by calling the Python Budget Calculator microservice.
 */
@RestController
@RequestMapping("task/budget")
@Tag(name = "Budget Calculator", description = "Power Plant Budget Calculation APIs")
public class BudgetCalculatorController {

    private static final Logger logger = LoggerFactory.getLogger(BudgetCalculatorController.class);

    @Autowired
    private BudgetCalculatorService budgetCalculatorService;

    /**
     * Health check endpoint for the Budget Calculator service.
     */
    @GetMapping("/health")
    @Operation(summary = "Check Budget Calculator Health", 
               description = "Verify if the Python Budget Calculator service is running")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        boolean isHealthy = budgetCalculatorService.isHealthy();
        if (isHealthy) {
            return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "pp-budget-calculator",
                "message", "Budget Calculator service is running"
            ));
        } else {
            return ResponseEntity.status(503).body(Map.of(
                "status", "unhealthy",
                "service", "pp-budget-calculator",
                "message", "Budget Calculator service is not available"
            ));
        }
    }

    /**
     * Calculate budget without USD iteration.
     * 
     * Request Body Example:
     * {
     *     "month": 1,
     *     "year": 2024,
     *     "lp_process": 30043.15,
     *     "lp_fixed": 5169.51,
     *     "mp_process": 14030.65,
     *     "mp_fixed": 518.00,
     *     "hp_process": 4971.91,
     *     "hp_fixed": 0.00,
     *     "shp_process": 20975.34,
     *     "shp_fixed": 0.00,
     *     "bfw_ufu": 300.00,
     *     "air_process": 6095102.0,
     *     "cw1_process": 15194.0,
     *     "cw2_process": 9016.0,
     *     "dm_process": 54779.0
     * }
     */
    @PostMapping("/calculate")
    @Operation(summary = "Calculate Budget", 
               description = "Calculate power plant budget without USD iteration")
    public ResponseEntity<Map<String, Object>> calculateBudget(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = budgetCalculatorService.calculateBudget(request);
        
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Calculate budget with USD iteration (Power-Steam balancing).
     * 
     * Request Body Example:
     * {
     *     "month": 1,
     *     "year": 2024,
     *     "lp_process": 30043.15,
     *     "lp_fixed": 5169.51,
     *     "mp_process": 14030.65,
     *     "mp_fixed": 518.00,
     *     "hp_process": 4971.91,
     *     "hp_fixed": 0.00,
     *     "shp_process": 20975.34,
     *     "shp_fixed": 0.00,
     *     "bfw_ufu": 300.00,
     *     "export_available": false,
     *     "air_process": 6095102.0,
     *     "cw1_process": 15194.0,
     *     "cw2_process": 9016.0,
     *     "dm_process": 54779.0,
     *     "save_to_db": false
     * }
     */
    @PostMapping("/calculate-with-iteration")
    @Operation(summary = "Calculate Budget with USD Iteration", 
               description = "Calculate power plant budget with Power-Steam balancing iteration")
    public ResponseEntity<Map<String, Object>> calculateBudgetWithIteration(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = budgetCalculatorService.calculateBudgetWithIteration(request);
        
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Run budget calculation for a full financial year (April to March).
     * 
     * Configuration values are fetched from BudgetCalculatorConfig table by default.
     * You can optionally override them in the request body.
     * 
     * Request Body Examples:
     * 
     * Minimal (uses all DB config):
     * {
     *     "financial_year": 2025
     * }
     * 
     * With overrides:
     * {
     *     "financial_year": 2025,
     *     "save_to_db": false,
     *     "save_logs": true,
     *     "python_exe_path": "C:\\Python310\\python.exe",
     *     "python_script_folder": "D:\\Custom\\Path"
     * }
     */
    @PostMapping("/run-full-year")
    @Operation(summary = "Run Full Year Budget Calculation", 
               description = "Calculate budget for all 12 months. Config from DB, can be overridden in request.")
    public ResponseEntity<Map<String, Object>> runFullYear(@RequestBody Map<String, Object> request) {
        logger.info("Received runFullYear request: financialYear={}, saveToDb={}, saveLogs={}, pythonExePath={}, pythonScriptFolder={}", 
            request.get("financial_year"),
            request.get("save_to_db"),
            request.get("save_logs"),
            request.get("python_exe_path"),
            request.get("python_script_folder"));
        
        Map<String, Object> result = budgetCalculatorService.runFullYear(request);
        
        if (Boolean.TRUE.equals(result.get("success"))) {
            logger.info("Full year budget calculation completed successfully");
            return ResponseEntity.ok(result);
        } else {
            logger.error("Full year budget calculation failed: {}", result.get("error"));
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Get list of available CPP plants.
     */
    @GetMapping("/cpp-plants")
    @Operation(summary = "Get CPP Plants", description = "Get list of available CPP plants")
    public ResponseEntity<Map<String, Object>> getCppPlants() {
        Map<String, Object> result = budgetCalculatorService.getCppPlants();
        return ResponseEntity.ok(result);
    }

    /**
     * Get list of all log runs.
     */
    @GetMapping("/logs")
    @Operation(summary = "List Log Runs", description = "Get list of all budget calculation log runs")
    public ResponseEntity<Map<String, Object>> getLogRuns() {
        Map<String, Object> result = budgetCalculatorService.getLogRuns();
        return ResponseEntity.ok(result);
    }

    /**
     * Get list of log files for a specific run.
     */
    @GetMapping("/logs/{runId}")
    @Operation(summary = "Get Run Logs", description = "Get list of log files for a specific run")
    public ResponseEntity<Map<String, Object>> getRunLogs(@PathVariable String runId) {
        Map<String, Object> result = budgetCalculatorService.getRunLogs(runId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get content of a specific log file.
     */
    @GetMapping("/logs/{runId}/{filename}/content")
    @Operation(summary = "Get Log Content", description = "Get content of a specific log file")
    public ResponseEntity<Map<String, Object>> getLogContent(
            @PathVariable String runId, 
            @PathVariable String filename) {
        Map<String, Object> result = budgetCalculatorService.getLogContent(runId, filename);
        return ResponseEntity.ok(result);
    }
}

