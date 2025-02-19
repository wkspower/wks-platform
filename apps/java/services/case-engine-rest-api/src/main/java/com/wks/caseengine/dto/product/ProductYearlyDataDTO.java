package com.wks.caseengine.dto.product;

import java.util.HashMap;
import java.util.Map;

public class ProductYearlyDataDTO {
    private String productId; // Changed from Long to String
    private String productName;
    private Map<String, Long> monthlyData = new HashMap<>();
    private String remark;

    public ProductYearlyDataDTO(String productId, String productName) {
        this.productId = productId;
        this.productName = productName;
    }

    public void addMonthData(String month, Long value) {
        monthlyData.put(month, value);
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public double getAverageTPH() {
        return monthlyData.values().stream().mapToDouble(Long::doubleValue).sum() / 12;
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Map<String, Long> getMonthlyData() {
        return monthlyData;
    }

    public void setMonthlyData(Map<String, Long> monthlyData) {
        this.monthlyData = monthlyData;
    }

    public String getRemark() {
        return remark;
    }
}
