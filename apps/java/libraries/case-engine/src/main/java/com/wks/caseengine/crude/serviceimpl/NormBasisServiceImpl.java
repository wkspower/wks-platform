package com.wks.caseengine.crude.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.wks.caseengine.tcs.repository.NormBasisRepository;
import com.wks.caseengine.crude.service.NormBasisService;
import com.wks.caseengine.crude.dto.NormBasisDTO;
import com.wks.caseengine.crude.dto.NormBasisProjection;

@Service
public class NormBasisServiceImpl implements NormBasisService {
    
    @Autowired
    private NormBasisRepository normBasisRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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


    @Override
    public void updateNormBasis(List<NormBasisDTO> normBasisDTOs) {
       
        List<Object[]> updates = new ArrayList<>();

        for(NormBasisDTO normBasisDTO : normBasisDTOs) {
            updates.add(new Object[]{normBasisDTO.getAttributeValue(), normBasisDTO.getRemarks(), normBasisDTO.getId()});
        }

        if(updates.size() > 0) {
            String sql = "update NormAttributeTransactions set AttributeValue = ?, Remarks = ? where Id = ?";
            jdbcTemplate.batchUpdate(sql, updates);
        }
  

    }

}
