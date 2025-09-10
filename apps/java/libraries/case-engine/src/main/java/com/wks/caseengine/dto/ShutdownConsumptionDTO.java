package com.wks.caseengine.dto;

import java.util.Date;

import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ShutdownConsumptionDTO {
    private String material;
    private String uom;
    private Double qty;
    private String repeatedCNT;
    private Double avgQty;
    private String aopYear;
 }

