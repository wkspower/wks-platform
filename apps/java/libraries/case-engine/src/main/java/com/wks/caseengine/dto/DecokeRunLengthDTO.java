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
    Double hTenProposed;
    Double hElevenActual;
    Double hElevenProposed;
    Double hTwelveActual;
    Double hTwelveProposed;
    Double hThirteenActual;
    Double hThirteenProposed;
    Double hFourteenActual;
    Double hFourteenProposed;
    String demo;
    String aopYear;
    UUID plantFkId;
    String remarks;

}
