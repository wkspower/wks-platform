package com.wks.caseengine.dto;

import java.util.Date;
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
public class PeopleInitiativeDTO {
	
	private String id;
    private String initiative;
    private String outcome;
    private String recommendation;
    private Date targetDate;
    private String responsible;
    private String plantId;
    private String aopYear;
    private String remark;
    private String saveStatus;
    private String errDescription;
}
