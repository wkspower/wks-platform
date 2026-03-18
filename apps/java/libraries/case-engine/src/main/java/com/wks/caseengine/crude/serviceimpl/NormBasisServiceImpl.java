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
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;

import com.wks.caseengine.crude.dto.NormBasisDTO;
import com.wks.caseengine.crude.dto.NormBasisProjection;

@Service
public class NormBasisServiceImpl implements NormBasisService {
    
    @Autowired
    private NormBasisRepository normBasisRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlantsRepository plantsRepository;

    @Autowired
    private VerticalsRepository verticalRepository;

    @Autowired
    private SiteRepository siteRepository;

    @PersistenceContext
	private EntityManager entityManager;

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

            boolean foundNormsPreparationTime = false;
            boolean foundNormsCycleStart = false;
            boolean foundDaysRemainingTime = false;


                for(NormBasisDTO normBasisDTO : normBasisDTOs) {  

                   //  if( !normBasisDTO.getType().equals("date"))  continue;

                    if(normBasisDTO.getName().equals("Norms Preparation Time")) {  

                        normsPreparationTime = normBasisDTO.getAttributeValue();
                        foundNormsPreparationTime = true;
                    }

                    if (normBasisDTO.getName().equals("Norms Cycle Start")) {
   
                        // set the attribute value to 1st april of end year
                        normBasisDTO.setAttributeValue(normCycleStarts);  
                        foundNormsCycleStart = true;
                    
                    } 


                }

            for(NormBasisDTO normBasisDTO : normBasisDTOs) {  

             
            if(normBasisDTO.getName().equals("Days remaining time from norms preparation time to AOP next cycle start")) {   
   
                 // calculate the days betweeen normsPreparationTime and normCycleStarts
                //  LocalDate normsPreparationTimeDate = LocalDate.parse(normsPreparationTime);
                //  LocalDate normCycleStartsDate = LocalDate.parse(normCycleStarts);

                LocalDate normsPreparationTimeDate =
        LocalDate.parse(normsPreparationTime.substring(0,10));

         LocalDate normCycleStartsDate =
        LocalDate.parse(normCycleStarts.substring(0,10));
                 long daysBetween = ChronoUnit.DAYS.between(normsPreparationTimeDate, normCycleStartsDate);

                 normBasisDTO.setAttributeValue(String.valueOf(daysBetween));

                 foundDaysRemainingTime = true;



            }
        }


         if(!(foundNormsPreparationTime && foundNormsCycleStart && foundDaysRemainingTime)) {  

            throw new RuntimeException("Norms Preparation Time or Norms Cycle Start or Days remaining time from norms preparation time to AOP next cycle start are not found");


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
    public AOPMessageVM updateNormBasis(List<NormBasisDTO> normBasisDTOs, UUID plantId, String aopYear, UUID siteId, String periodFrom, String periodTo) {
       
        List<Object[]> updates = new ArrayList<>();

        for(NormBasisDTO normBasisDTO : normBasisDTOs) {
            updates.add(new Object[]{normBasisDTO.getAttributeValue(), normBasisDTO.getRemarks(), normBasisDTO.getId()});
        }

        if(updates.size() > 0) {
            String sql = "update NormAttributeTransactions set AttributeValue = ?, Remarks = ? where Id = ?";
            jdbcTemplate.batchUpdate(sql, updates);
        }

        // call the norm calculation procedure

        Plants plant = plantsRepository.findById(plantId).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();

        // CRUDE_DTA_CDU1_NormCalculation
     String procedureName = vertical.getName()+"_"+site.getName()+"_"+  plant.getName() +"_"+"NormCalculation";

     String errorMessage = executeNormCalculationProcedure(plantId, aopYear, siteId, periodFrom, periodTo, procedureName );

     AOPMessageVM aopMessageVM = new AOPMessageVM();

     if(errorMessage != null ) { 
    
        aopMessageVM.setCode(422);
        aopMessageVM.setMessage(errorMessage);
        return aopMessageVM;

     }




    //    normBasisRepository.normCalculation(plantId, aopYear, siteId, periodFrom, periodTo);

      aopMessageVM.setCode(200);
      aopMessageVM.setMessage("Norm Calculations Executed Successfully");
      return aopMessageVM;
  

    }

    @Override
    public AOPMessageVM LoadButtonNormCalculation(UUID plantId, String aopYear, UUID siteId, String periodFrom, String periodTo) 

    {
        Plants plant = plantsRepository.findById(plantId).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();

        // CRUDE_DTA_CDU1_NormCalculation
     String procedureName = vertical.getName()+"_"+site.getName()+"_"+  plant.getName() +"_"+"NormCalculation";

     String errorMessage = executeNormCalculationProcedure(plantId, aopYear, siteId, periodFrom, periodTo, procedureName );

     AOPMessageVM aopMessageVM = new AOPMessageVM();

     if(errorMessage != null ) { 
    
        aopMessageVM.setCode(422);
        aopMessageVM.setMessage(errorMessage);
        return aopMessageVM;

     }


    //    normBasisRepository.normCalculation(plantId, aopYear, siteId, periodFrom, periodTo);

      aopMessageVM.setCode(200);
      aopMessageVM.setMessage("Norm Calculations Executed Successfully");
      return aopMessageVM;

}


private String executeNormCalculationProcedure(UUID plantId, String aopYear, UUID siteId,
                                             String periodFrom, String periodTo,
                                             String procedureName) {

    try {

        StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery(procedureName);

        // Input parameters
        query.registerStoredProcedureParameter("plantId", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("AOPYear", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("siteid", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("PeriodFrom", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("PeriodTo", String.class, ParameterMode.IN);

        // OUTPUT parameter
        query.registerStoredProcedureParameter("ErrorMessage", String.class, ParameterMode.OUT);

        query.setParameter("plantId", plantId.toString());
        query.setParameter("AOPYear", aopYear);
        query.setParameter("siteid", siteId.toString());
        query.setParameter("PeriodFrom", periodFrom);
        query.setParameter("PeriodTo", periodTo);

        query.execute();

        try {
            query.getResultList(); // flush any pending result sets
        } catch (Exception ignored) {}

        String errorMessage = (String) query.getOutputParameterValue("ErrorMessage");

        System.out.println("errorMessage string: " + errorMessage);

        return errorMessage;

    } catch (IllegalArgumentException e) {
        throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
    } catch (Exception ex) {
        throw new RuntimeException("Failed to execute procedure", ex);
    }
}


// Pims throughput

@Override
public List<NormBasisDTO> getPIMSThroughput(UUID plantId, String aopYear) {
    
    List<NormBasisProjection> normBasisProjections = normBasisRepository.getPIMSThroughput(plantId, aopYear);
    List<NormBasisDTO> normBasisDTOs = normBasisProjections.stream()
        .map(this::fromProjection)
        .collect(Collectors.toList());

    return normBasisDTOs;
}



@Override
public void updatePimsThroughput(List<NormBasisDTO> normBasisDTOs, UUID plantId, String aopYear, UUID siteId, String periodFrom, String periodTo) {
   
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
