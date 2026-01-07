package com.wks.caseengine.dto.cpp.norm;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class NormsMonthValueDTO {

   @JsonProperty("financialYearMonthFkId")
   private UUID financialYearMonthFkId;

    private String generationUom;     
    private BigDecimal norms;         
    private BigDecimal quantity;     
    private BigDecimal amount;        
    private BigDecimal price;         

 
    private String scenarioType;      
    private Integer displayOrder;     
    private BigDecimal qty;  
}
