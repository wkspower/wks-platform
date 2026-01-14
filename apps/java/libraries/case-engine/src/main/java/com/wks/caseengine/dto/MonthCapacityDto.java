package com.wks.caseengine.dto;

import lombok.Data;

// DTO for AssetCapacityDTO
@Data
public class MonthCapacityDto {
    
    private Double min;
    private Double max;

    public MonthCapacityDto(Double min, Double max) {
        this.min = min;
        this.max = max;
    }

    public MonthCapacityDto() {
        this.min = null;
        this.max = null;
    }

}
