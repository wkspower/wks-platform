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

	private String id;
	private String date;
	private String month;
	private String hTenActual;
	private String tenProposed;
	private String hElevenActual;
	private String elevenProposed;
	private String hTwelveActual;
	private String twelveProposed;
	private String hThirteenActual;
	private String thirteenProposed;
	private String hFourteenActual;
	private String fourteenProposed;
	private String demo;
	private String aopYear;
	private String plantFkId;
	private String remarks;
	private String saveStatus;
	private String errDescription;

}
