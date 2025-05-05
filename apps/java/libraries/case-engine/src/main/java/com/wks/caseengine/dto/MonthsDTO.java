package com.wks.caseengine.dto;

import java.util.UUID;

import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(Include.ALWAYS)
public class MonthsDTO {
	
    protected Float jan;
    protected Float feb;
    protected Float march;
    protected Float april;
    protected Float may;
    protected Float june;
    protected Float july;
    protected Float aug;
    protected Float sep;
    protected Float oct;
    protected Float nov;
    protected Float dec;

}
