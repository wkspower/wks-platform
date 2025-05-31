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
	
    protected Double jan;
    protected Double feb;
    protected Double march;
    protected Double april;
    protected Double may;
    protected Double june;
    protected Double july;
    protected Double aug;
    protected Double sep;
    protected Double oct;
    protected Double nov;
    protected Double dec;

}
