package com.wks.caseengine.dto;

import java.util.Date;
import java.util.UUID;

import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Configuration
public class DecokeRunLengthDTO {
	
	UUID id;
    Date date;
    Double hTenActual;
    Double tenProposed;
    Double hElevenActual;
    Double elevenProposed;
    Double hTwelveActual;
    Double twelveProposed;
    Double hThirteenActual;
    Double thirteenProposed;
    Double hFourteenActual;
    Double fourteenProposed;
    String demo;
    String aopYear;
    UUID plantFkId;
    String remarks;

}
