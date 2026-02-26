package com.wks.caseengine.crude.serviceimpl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.tcs.repository.NormBasisRepository;
import com.wks.caseengine.crude.service.NormBasisService;
import com.wks.caseengine.crude.dto.NormBasisDTO;
import com.wks.caseengine.crude.dto.NormBasisProjection;

@Service
public class NormBasisServiceImpl implements NormBasisService {
    
    @Autowired
    private NormBasisRepository normBasisRepository;

    @Override
    public List<NormBasisDTO> getAllNormBasis(UUID plantId, String aopYear) {
        
        List<NormBasisProjection> normBasisProjections = normBasisRepository.getAllNormBasis(plantId, aopYear);
        return normBasisProjections.stream()
            .map(this::fromProjection)
            .collect(Collectors.toList());
      
    }

    private NormBasisDTO fromProjection(NormBasisProjection projection) {
        return NormBasisDTO.builder()
            .id(UUID.fromString(projection.getId()))
            .name(projection.getName())
            .displayName(projection.getDisplayName())
            .uom(projection.getUOM())
            .attributeValue(projection.getAttributeValue())
            .remarks(projection.getRemarks())
            .type(projection.getType())
            .normParameterType(projection.getNormParameterType())
            .displayOrder(projection.getDisplayOrder())
            .build();
    }

}
