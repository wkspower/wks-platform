package com.wks.caseengine.dto;

import java.util.UUID;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators.Switch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BusinessDemandDTO {
	
	private String id;
    private String remarks;
    private String normParameterId;
    private String catalystId;
    private String type;
    private Float jan;
    private Float feb;
    private Float march;
    private Float april;
    private Float may;
    private Float june;
    private Float july;
    private Float aug;
    private Float sep;
    private Float oct;
    private Float nov;
    private Float dec;
    private String year;
    private String plantFkId;
    private Float TPH;
    private Float avgTPH;
    private String UOM;

   public  Float getMonthValue(Integer month){
       Float value = 0.0F;
        switch (month) {
            case 1:
                value = getJan();
                break; // Break out of the switch statement after a match
            case 2:
            value = getFeb();
                break;
            case 3:
               value = getMarch();
                break;
            case 4:
                value = getApril();
                break;
            case 5:
                value = getMay();
                break;
            case 6:
                value = getJune();
                break;
            case 7:
                value = getJuly();
                break;
                case 8:
                value = getAug();
                break;
                case 9:
                value = getSep();
                break;
                case 10:
                value = getOct();
                break;
                case 11:
                value = getNov();
                break;
                case 12:
                value = getDec();
                break;

            default:
                value = 0.0F; // Default case if no match is found
        }
        return value;
    }

}
