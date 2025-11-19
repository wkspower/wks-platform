package com.wks.caseengine.dto;

import java.time.LocalDateTime;

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
public class TimeRangeWithIndex {
	
	LocalDateTime start;
    LocalDateTime end;
    int index; // Index in dtoList
	
}
