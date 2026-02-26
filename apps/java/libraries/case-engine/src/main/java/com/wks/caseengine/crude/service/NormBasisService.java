package com.wks.caseengine.crude.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.crude.dto.NormBasisDTO;

public interface NormBasisService {
    
    public List<NormBasisDTO> getAllNormBasis(UUID plantId, String aopYear);

}
