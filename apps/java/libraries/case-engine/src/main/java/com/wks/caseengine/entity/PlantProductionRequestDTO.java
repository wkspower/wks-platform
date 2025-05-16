package com.wks.caseengine.entity;

import java.util.List;
import com.wks.caseengine.dto.PlantProductionDataDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantProductionRequestDTO {
    private List<PlantProductionDataDTO> dataList;
}
