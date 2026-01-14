package com.wks.caseengine.rest.server;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Service to execute Python Budget Calculator via SQL Server stored procedure.
 * This service calls usp_CalculateBalanceUSDIteration stored procedure using xp_cmdshell.
 */
@Service
public class BudgetCalculatorService {

    private static final Logger logger = LoggerFactory.getLogger(BudgetCalculatorService.class);

    @Autowired
    @Qualifier("db1DataSource")
    private DataSource dataSource;

    private final Gson gson = new Gson();

    /**
     * Check if the stored procedure is accessible.
     * 
     * @return true if the database connection is healthy, false otherwise
     */
    public boolean isHealthy() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            logger.error("Database health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Calculate budget without USD iteration.
     * Note: This method is deprecated. Use runFullYear instead.
     * 
     * @param request Map containing budget calculation parameters
     * @return Map containing the calculation result
     */
    @Deprecated
    public Map<String, Object> calculateBudget(Map<String, Object> request) {
        logger.warn("calculateBudget is deprecated. Use stored procedure approach via runFullYear.");
        return Map.of(
            "success", false,
            "error", "This method is deprecated. Use runFullYear instead."
        );
    }

    /**
     * Calculate budget with USD iteration (Power-Steam balancing).
     * Note: This method is deprecated. Use runFullYear instead.
     * 
     * @param request Map containing budget calculation parameters
     * @return Map containing the calculation result with iteration details
     */
    @Deprecated
    public Map<String, Object> calculateBudgetWithIteration(Map<String, Object> request) {
        logger.warn("calculateBudgetWithIteration is deprecated. Use stored procedure approach via runFullYear.");
        return Map.of(
            "success", false,
            "error", "This method is deprecated. Use runFullYear instead."
        );
    }

    /**
     * Get configuration value from BudgetCalculatorConfig table.
     */
    private String getConfigValue(Connection conn, String configKey, String defaultValue) {
        try (CallableStatement stmt = conn.prepareCall("{CALL dbo.usp_GetBudgetCalculatorConfig(?, ?)}")) {
            stmt.setString(1, configKey);
            stmt.registerOutParameter(2, java.sql.Types.NVARCHAR);
            stmt.execute();
            String value = stmt.getString(2);
            return (value != null && !value.isEmpty()) ? value : defaultValue;
        } catch (SQLException e) {
            logger.warn("Failed to get config value for {}, using default: {}", configKey, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Run budget calculation for a full financial year via stored procedure.
     * Configuration values are fetched from BudgetCalculatorConfig table.
     * Request parameters can optionally override database configuration.
     * 
     * @param request Map containing financial_year (required), optional: save_to_db, save_logs, python_exe_path, python_script_folder
     * @return Map containing the full year calculation results
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> runFullYear(Map<String, Object> request) {
        logger.info("Calling stored procedure usp_CalculateBalanceUSDIteration for full year");
        
        // Extract financial_year from request (JSON numbers come as Double)
        Integer financialYear = null;
        Object yearObj = request.get("financial_year");
        if (yearObj instanceof Number) {
            financialYear = ((Number) yearObj).intValue();
        }
        
        if (financialYear == null) {
            return Map.of(
                "success", false,
                "error", "financial_year is required"
            );
        }
        
        try (Connection conn = dataSource.getConnection()) {
            // Fetch configuration from database, allow request to override
            Boolean saveToDb = request.containsKey("save_to_db") 
                ? (Boolean) request.get("save_to_db")
                : Boolean.parseBoolean(getConfigValue(conn, "SAVE_TO_DB_DEFAULT", "true"));
            
            Boolean saveLogs = request.containsKey("save_logs")
                ? (Boolean) request.get("save_logs")
                : Boolean.parseBoolean(getConfigValue(conn, "SAVE_LOGS_DEFAULT", "true"));
            
            String pythonExePath = request.containsKey("python_exe_path")
                ? (String) request.get("python_exe_path")
                : getConfigValue(conn, "PYTHON_EXE_PATH", "py");
            
            String pythonScriptFolder = request.containsKey("python_script_folder")
                ? (String) request.get("python_script_folder")
                : getConfigValue(conn, "PYTHON_SCRIPT_FOLDER", null);
            
            logger.info("Configuration: saveToDb={} (from: {}), saveLogs={} (from: {}), pythonExePath={} (from: {}), pythonScriptFolder={} (from: {})", 
                saveToDb, request.containsKey("save_to_db") ? "request" : "database",
                saveLogs, request.containsKey("save_logs") ? "request" : "database",
                pythonExePath, request.containsKey("python_exe_path") ? "request" : "database",
                pythonScriptFolder, request.containsKey("python_script_folder") ? "request" : "database");
            // Call stored procedure with all parameters
            String sql = "{CALL dbo.usp_CalculateBalanceUSDIteration(?, ?, ?, ?, ?, ?)}";
            
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setInt(1, financialYear);
                stmt.setNull(2, java.sql.Types.NVARCHAR); // CPPPlantId - always NULL for now
                stmt.setBoolean(3, saveToDb);
                stmt.setBoolean(4, saveLogs);
                stmt.setString(5, pythonExePath);
                
                if (pythonScriptFolder != null && !pythonScriptFolder.isEmpty()) {
                    stmt.setString(6, pythonScriptFolder);
                } else {
                    stmt.setNull(6, java.sql.Types.NVARCHAR);
                }
                
                // Log the complete SP call with all parameters
                logger.info("Executing Stored Procedure: EXEC dbo.usp_CalculateBalanceUSDIteration @FinancialYear={}, @CPPPlantId=NULL, @SaveToDb={}, @SaveLogs={}, @PythonExePath='{}', @PythonScriptFolder='{}'", 
                    financialYear, 
                    saveToDb, 
                    saveLogs,
                    pythonExePath,
                    (pythonScriptFolder != null && !pythonScriptFolder.isEmpty() ? pythonScriptFolder : "NULL"));
                
                // Execute and capture output
                boolean hasResults = stmt.execute();
                List<String> outputLines = new ArrayList<>();
                
                // Read all result sets (xp_cmdshell returns output as result sets)
                while (hasResults || stmt.getUpdateCount() != -1) {
                    if (hasResults) {
                        try (ResultSet rs = stmt.getResultSet()) {
                            while (rs.next()) {
                                String line = rs.getString(1);
                                if (line != null && !line.trim().isEmpty()) {
                                    outputLines.add(line);
                                }
                            }
                        }
                    }
                    hasResults = stmt.getMoreResults();
                }
                
                // Join output and parse JSON
                String output = String.join("\n", outputLines);
                logger.debug("SP Output: {}", output);
                
                // Find JSON in output (starts with { and ends with })
                int jsonStart = output.indexOf("{");
                int jsonEnd = output.lastIndexOf("}");
                
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    String jsonOutput = output.substring(jsonStart, jsonEnd + 1);
                    try {
                        Map<String, Object> result = gson.fromJson(jsonOutput, Map.class);
                        logger.info("Full year budget calculation completed successfully");
                        return result;
                    } catch (JsonSyntaxException e) {
                        logger.error("Failed to parse JSON output: {}", e.getMessage());
                        return Map.of(
                            "success", false,
                            "error", "Failed to parse calculation result",
                            "raw_output", output
                        );
                    }
                } else {
                    logger.error("No JSON output found in SP result");
                    return Map.of(
                        "success", false,
                        "error", "No JSON output from stored procedure",
                        "raw_output", output
                    );
                }
                
            }
        } catch (SQLException e) {
            logger.error("Error executing stored procedure: {}", e.getMessage(), e);
            return Map.of(
                "success", false,
                "error", "Database error: " + e.getMessage()
            );
        }
    }

    /**
     * Get list of available CPP plants from database.
     */
    public Map<String, Object> getCppPlants() {
        logger.info("Fetching CPP plants from database");
        
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT DISTINCT CPPPLANT_FK_Id FROM PowerGenerationAssets WHERE CPPPLANT_FK_Id IS NOT NULL";
            
            try (CallableStatement stmt = conn.prepareCall(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                List<String> plants = new ArrayList<>();
                while (rs.next()) {
                    plants.add(rs.getString(1));
                }
                
                return Map.of(
                    "success", true,
                    "plants", plants
                );
            }
        } catch (SQLException e) {
            logger.error("Error fetching CPP plants: {}", e.getMessage());
            return Map.of(
                "success", false,
                "error", "Failed to fetch CPP plants: " + e.getMessage()
            );
        }
    }

    /**
     * Get list of all log runs.
     * Note: Log management via API is deprecated. Logs are stored on server filesystem.
     */
    @Deprecated
    public Map<String, Object> getLogRuns() {
        logger.warn("getLogRuns is deprecated. Logs are stored on server filesystem.");
        return Map.of(
            "success", false,
            "error", "Log management via API is deprecated. Check server logs folder."
        );
    }

    /**
     * Get list of log files for a specific run.
     * Note: Log management via API is deprecated. Logs are stored on server filesystem.
     */
    @Deprecated
    public Map<String, Object> getRunLogs(String runId) {
        logger.warn("getRunLogs is deprecated. Logs are stored on server filesystem.");
        return Map.of(
            "success", false,
            "error", "Log management via API is deprecated. Check server logs folder."
        );
    }

    /**
     * Get content of a specific log file.
     * Note: Log management via API is deprecated. Logs are stored on server filesystem.
     */
    @Deprecated
    public Map<String, Object> getLogContent(String runId, String filename) {
        logger.warn("getLogContent is deprecated. Logs are stored on server filesystem.");
        return Map.of(
            "success", false,
            "error", "Log management via API is deprecated. Check server logs folder."
        );
    }
}
