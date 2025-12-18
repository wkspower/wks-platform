package com.wks.caseengine.dto;

public class MonthlyHoursDTO {

    private double netOperationHrs;
    private double shutdownHrs;

    public MonthlyHoursDTO(double netOperationHrs, double shutdownHrs) {
        this.netOperationHrs = netOperationHrs;
        this.shutdownHrs = shutdownHrs;
    }

    public double getNetOperationHrs() {
        return netOperationHrs;
    }

    public double getShutdownHrs() {
        return shutdownHrs;
    }
}

