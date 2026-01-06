package com.wks.caseengine.message.vm;

import org.springframework.context.annotation.Configuration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
public class BudgetRunResult {
        private boolean success;
        private String financialYear;
        private String cppPlantId;
        private String runTimestamp;
        private int totalMonths;
        private int successfulMonths;
        private int failedMonths;
        private double totalPowerKwh;
        private double totalShpMt;
        private String error;
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getFinancialYear() { return financialYear; }
        public void setFinancialYear(String financialYear) { this.financialYear = financialYear; }
        
        public String getCppPlantId() { return cppPlantId; }
        public void setCppPlantId(String cppPlantId) { this.cppPlantId = cppPlantId; }
        
        public String getRunTimestamp() { return runTimestamp; }
        public void setRunTimestamp(String runTimestamp) { this.runTimestamp = runTimestamp; }
        
        public int getTotalMonths() { return totalMonths; }
        public void setTotalMonths(int totalMonths) { this.totalMonths = totalMonths; }
        
        public int getSuccessfulMonths() { return successfulMonths; }
        public void setSuccessfulMonths(int successfulMonths) { this.successfulMonths = successfulMonths; }
        
        public int getFailedMonths() { return failedMonths; }
        public void setFailedMonths(int failedMonths) { this.failedMonths = failedMonths; }
        
        public double getTotalPowerKwh() { return totalPowerKwh; }
        public void setTotalPowerKwh(double totalPowerKwh) { this.totalPowerKwh = totalPowerKwh; }
        
        public double getTotalShpMt() { return totalShpMt; }
        public void setTotalShpMt(double totalShpMt) { this.totalShpMt = totalShpMt; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
