package com.wks.caseengine.crude.serviceimpl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.wks.caseengine.crude.repository.NormBasisRepository;
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
        List<NormBasisDTO> normBasisDTOs = normBasisProjections.stream()
            .map(this::fromProjection)
            .collect(Collectors.toList());

            //  String endYear = String.valueOf(Integer.parseInt(aopYear.substring(0, 4))  +1 );
            //     String normCycleStarts = endYear + "-" + "04" + "-" + "01"; 

            String startYear = String.valueOf(Integer.parseInt(aopYear.substring(0, 4))  );
            String normCycleStarts = startYear + "-" + "04" + "-" + "01"; 
                
            String normsPreparationTime = null;


                for(NormBasisDTO normBasisDTO : normBasisDTOs) {  

                     if( !normBasisDTO.getType().equals("date"))  continue;

                    if(normBasisDTO.getName().equals("Norms Preparation Time")) {  

                        normsPreparationTime = normBasisDTO.getAttributeValue();
                    }
                }

            for(NormBasisDTO normBasisDTO : normBasisDTOs) {  

                if( !normBasisDTO.getType().equals("date"))  continue;

                if (normBasisDTO.getName().equals("Norms Cycle Start")) {
   
                // set the attribute value to 1st april of end year
                normBasisDTO.setAttributeValue(normCycleStarts);  
            
            } 

            if(normBasisDTO.getName().equals("Days remaining time from norms preparation time to AOP next cycle start")) {   
   
                 // calculate the days betweeen normsPreparationTime and normCycleStarts
                 LocalDate normsPreparationTimeDate = LocalDate.parse(normsPreparationTime);
                 LocalDate normCycleStartsDate = LocalDate.parse(normCycleStarts);
                 long daysBetween = ChronoUnit.DAYS.between(normsPreparationTimeDate, normCycleStartsDate);
                 normBasisDTO.setAttributeValue(String.valueOf(daysBetween));

            }
            
            }

            return normBasisDTOs;
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
            .isEditable(projection.getIsEditable())
            .config(projection.getConfig())
            .build();
    }


    @Override
    public void updateNormBasis(List<NormBasisDTO> normBasisDTOs, UUID plantId, String aopYear, UUID siteid, String periodFrom, String periodTo) {
       
        List<Object[]> updates = new ArrayList<>();

        for(NormBasisDTO normBasisDTO : normBasisDTOs) {
            updates.add(new Object[]{normBasisDTO.getAttributeValue(), normBasisDTO.getRemarks(), normBasisDTO.getId()});
        }

        if(updates.size() > 0) {
            String sql = "update NormAttributeTransactions set AttributeValue = ?, Remarks = ? where Id = ?";
            jdbcTemplate.batchUpdate(sql, updates);
        }

        normBasisRepository.normCalculation(plantId, aopYear, siteid, periodFrom, periodTo);
  

    }

}
