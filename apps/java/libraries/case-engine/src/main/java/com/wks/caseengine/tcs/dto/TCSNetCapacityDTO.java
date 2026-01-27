package com.wks.caseengine.tcs.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TCSNetCapacityDTO {
  private String id;          
  private String particulates;
  private Double apr;
  private Double may;
  private Double jun;
  private Double jul;
  private Double aug;
  private Double sep;
  private Double oct;
  private Double nov;
  private Double dec;
  private Double jan;
  private Double feb;
  private Double mar;
  
  private String remark;
  private Date insertedDateTime;
}


