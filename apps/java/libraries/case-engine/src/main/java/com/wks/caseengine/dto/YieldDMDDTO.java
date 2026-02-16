package com.wks.caseengine.dto;

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
public class YieldDMDDTO {
	
	
	private String saveStatus;
    private String errDescription;
    private String particulars;

  
    private Double fiveFC2C3;
    private Double fiveFPropane;
    private Double fiveFEthane;

   
    private Double fiveFDSC2C3;
    private Double fiveFDSPropane;
    private Double fiveFDSEthane;

   
    private Double sixFSFDC2C3;
    private Double sixFSFDPropane;
    private Double sixFSFDEthane;

   
    private Double sixFBFDC2C3;
    private Double sixFBFDPropane;
    private Double sixFBFDEthane;

   
    private Double fourFC2C3;
    private Double fourFPropane;
    private Double fourFEthane;

   
    private Double fourFDC2C3;
    private Double fourFDPropane;
    private Double fourFDEthane;
    
    
    private Double sevenFC2C3;
    private Double sevenFPropane;
    private Double sevenFEthane;
  
    private Double threeFC2C3;
    private Double threeFPropane;
    private Double threeFEthane;
    
    private Double fourF2SC2C3;
    private Double fourF2SPropane;
    private Double fourF2SEthane;
	

}
