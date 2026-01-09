package com.wks.caseengine.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasterAssetOperationalResponseDTO {
    
    List<AssetOperationalResponseDTO> powerResponse;
    List<AssetOperationalResponseDTO> steamResponse;
}
