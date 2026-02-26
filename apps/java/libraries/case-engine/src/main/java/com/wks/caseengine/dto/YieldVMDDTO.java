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
public class YieldVMDDTO {
	
	
	private String saveStatus;
    private String errDescription;
    private String particulars;
    private Double fiveNE;
    private Double fiveNS;
    private Double fourNE;
    private Double fourNS;
    private Double threeNE;

}
