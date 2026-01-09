package com.wks.caseengine.rest.server;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Service to communicate with the Python Budget Calculator API.
 * This service acts as a bridge between Java and the Python microservice.
 */
@Service
public class BudgetCalculatorService {

    private static final Logger logger = LoggerFactory.getLogger(BudgetCalculatorService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${pp.budget.calculator.url:http://pp-budget-calculator:5000}")
    private String budgetCalculatorUrl;

    /**
     * Check if the Python Budget Calculator service is healthy.
     * 
     * @return true if the service is healthy, false otherwise
     */
    public boolean isHealthy() {
        try {
            String url = budgetCalculatorUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getStatusCode().is2xxSuccessful() 
                && "healthy".equals(response.getBody().get("status"));
        } catch (RestClientException e) {
            logger.error("Budget Calculator health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Calculate budget without USD iteration.
     * 
     * @param request Map containing budget calculation parameters
     * @return Map containing the calculation result
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> calculateBudget(Map<String, Object> request) {
        String url = budgetCalculatorUrl + "/api/budget/calculate";
        logger.info("Calling Budget Calculator API: {}", url);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Budget calculation completed successfully");
                return response.getBody();
            } else {
                logger.error("Budget calculation failed with status: {}", response.getStatusCode());
                return Map.of(
                    "success", false,
                    "error", "Budget calculation failed with status: " + response.getStatusCode()
                );
            }
        } catch (RestClientException e) {
            logger.error("Error calling Budget Calculator API: {}", e.getMessage());
            return Map.of(
                "success", false,
                "error", "Failed to connect to Budget Calculator service: " + e.getMessage()
            );
        }
    }

    /**
     * Calculate budget with USD iteration (Power-Steam balancing).
     * 
     * @param request Map containing budget calculation parameters
     * @return Map containing the calculation result with iteration details
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> calculateBudgetWithIteration(Map<String, Object> request) {
        String url = budgetCalculatorUrl + "/api/budget/calculate-with-iteration";
        logger.info("Calling Budget Calculator API with iteration: {}", url);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Budget calculation with iteration completed successfully");
                return response.getBody();
            } else {
                logger.error("Budget calculation with iteration failed with status: {}", response.getStatusCode());
                return Map.of(
                    "success", false,
                    "error", "Budget calculation failed with status: " + response.getStatusCode()
                );
            }
        } catch (RestClientException e) {
            logger.error("Error calling Budget Calculator API: {}", e.getMessage());
            return Map.of(
                "success", false,
                "error", "Failed to connect to Budget Calculator service: " + e.getMessage()
            );
        }
    }

    /**
     * Run budget calculation for a full financial year.
     * 
     * @param request Map containing financial_year, cpp_plant_id (optional), save_to_db, save_logs
     * @return Map containing the full year calculation results
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> runFullYear(Map<String, Object> request) {
        String url = budgetCalculatorUrl + "/api/budget/run-full-year";
        logger.info("Calling Budget Calculator API for full year: {}", url);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Full year budget calculation completed successfully");
                return response.getBody();
            } else {
                logger.error("Full year budget calculation failed with status: {}", response.getStatusCode());
                return Map.of(
                    "success", false,
                    "error", "Full year calculation failed with status: " + response.getStatusCode()
                );
            }
        } catch (RestClientException e) {
            logger.error("Error calling Budget Calculator API: {}", e.getMessage());
            return Map.of(
                "success", false,
                "error", "Failed to connect to Budget Calculator service: " + e.getMessage()
            );
        }
    }

    /**
     * Get list of available CPP plants.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCppPlants() {
        String url = budgetCalculatorUrl + "/api/cpp-plants";
        logger.info("Fetching CPP plants from: {}", url);
        
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            logger.error("Error fetching CPP plants: {}", e.getMessage());
            return Map.of(
                "success", false,
                "error", "Failed to fetch CPP plants: " + e.getMessage()
            );
        }
    }

    /**
     * Get list of all log runs.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getLogRuns() {
        String url = budgetCalculatorUrl + "/api/logs";
        logger.info("Fetching log runs from: {}", url);
        
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            logger.error("Error fetching log runs: {}", e.getMessage());
            return Map.of(
                "success", false,
                "error", "Failed to fetch log runs: " + e.getMessage()
            );
        }
    }

    /**
     * Get list of log files for a specific run.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getRunLogs(String runId) {
        String url = budgetCalculatorUrl + "/api/logs/" + runId;
        logger.info("Fetching logs for run: {}", runId);
        
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            logger.error("Error fetching run logs: {}", e.getMessage());
            return Map.of(
                "success", false,
                "error", "Failed to fetch run logs: " + e.getMessage()
            );
        }
    }

    /**
     * Get content of a specific log file.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getLogContent(String runId, String filename) {
        String url = budgetCalculatorUrl + "/api/logs/" + runId + "/" + filename + "/content";
        logger.info("Fetching log content: {}/{}", runId, filename);
        
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (RestClientException e) {
            logger.error("Error fetching log content: {}", e.getMessage());
            return Map.of(
                "success", false,
                "error", "Failed to fetch log content: " + e.getMessage()
            );
        }
    }
}
