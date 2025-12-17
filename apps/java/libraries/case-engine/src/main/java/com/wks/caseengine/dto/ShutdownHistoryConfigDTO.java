package com.wks.caseengine.dto;

import lombok.*;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShutdownHistoryConfigDTO {

    private UUID id;
    private Integer month;
    private String year;
    private String aopYear;
    private String remark;
    private Date createdOn;
    private Date modifiedOn;
    private String modifiedBy;
    private String plantId;

}
