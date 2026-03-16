package com.wks.caseengine.crude.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.crude.dto.NormBasisDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface NormBasisService {
    
    public List<NormBasisDTO> getAllNormBasis(UUID plantId, String aopYear);

    public AOPMessageVM updateNormBasis(List<NormBasisDTO> normBasisDTOs, UUID plantId, String aopYear, UUID siteid, String periodFrom, String periodTo);

    // Pims throughput

    public List<NormBasisDTO> getPIMSThroughput(UUID plantId, String aopYear);

    public void updatePimsThroughput(List<NormBasisDTO> normBasisDTOs, UUID plantId, String aopYear, UUID siteId, String periodFrom, String periodTo);

}
