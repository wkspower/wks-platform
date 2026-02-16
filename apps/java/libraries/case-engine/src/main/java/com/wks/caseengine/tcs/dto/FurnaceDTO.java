package com.wks.caseengine.tcs.dto;

import lombok.Data;

@Data
public class FurnaceDTO {

   
    private String furnace;

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

    private String remarks;
  //  private Double gCalPerHr;

    // For import status tracking
    private String saveStatus;
    private String errDescription;
}



