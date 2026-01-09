package com.wks.caseengine.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import com.wks.caseengine.dto.TCSCPPUnitsSDPlanDTO;
import com.wks.caseengine.dto.TCSCPPUnitsSDPlanProjection;
import com.wks.caseengine.repository.TCSCPPUnitsSDPlanRepository;

@Service
public class TCSCPPUnitsSDPlanServiceImpl implements TCSCPPUnitsSDPlanService {
    

    @Autowired
    private TCSCPPUnitsSDPlanRepository tcsCppUnitsSDPlanRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<TCSCPPUnitsSDPlanDTO> getTCSCPPUnitsSDPlan(String financialYear, UUID siteId) {

        //convert the projection to dto
        List<TCSCPPUnitsSDPlanProjection> tcsCppUnitsSDPlanProjections = tcsCppUnitsSDPlanRepository.findByFinancialYearAndSiteId(financialYear, siteId);
        System.out.println("successfully fetched the data");
        
        List<TCSCPPUnitsSDPlanDTO> tcsCppUnitsSDPlanDTOs = new ArrayList<>();
        
         // database date format : yyyy-MM-dd
          // response format : dd-M-yyyy
         // frontend displaydate format : dd-M-yyyy


     //   SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy");
      //    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

          DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
          DateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        
           for (TCSCPPUnitsSDPlanProjection projection : tcsCppUnitsSDPlanProjections) {
            TCSCPPUnitsSDPlanDTO dto = new TCSCPPUnitsSDPlanDTO();
            dto.setId(projection.getId());
            dto.setMachine(projection.getMachine());
             dto.setGtMaintenance(projection.getGTMaintenance());
             dto.setNoOfDays(projection.getNoOfDays());
          //   dto.setShutDownDate(projection.getShutDownDate() != null ? projection.getShutDownDate().format(formatter) : null);
          dto.setShutDownDate(projection.getShutDownDate());

       //      dto.setStartUpDate(projection.getStartUpDate() != null ? projection.getStartUpDate().format(formatter) : null);

            dto.setStartUpDate(projection.getStartUpDate());
             dto.setMajorJobs(projection.getMajorJobs());
        //     dto.setIbrDueDate(projection.getIBRDueDate() != null ? projection.getIBRDueDate().format(formatter) : null);

          // for IbrDueDate pass only date not time
          //  dto.setIbrDueDate(projection.getIBRDueDate());
          try {
            dto.setIbrDueDate(projection.getIBRDueDate() != null ? dateFormatter.parse(projection.getIBRDueDate().toString()) : null);
        } catch (ParseException e) {
            
            e.printStackTrace();
        }
           
            tcsCppUnitsSDPlanDTOs.add(dto);
           }
           return tcsCppUnitsSDPlanDTOs;
    }


    @Override
    public void saveTCSCPPUnitsSDPlan(List<TCSCPPUnitsSDPlanDTO> tcsCppUnitsSDPlanDTOs) {
       
        List<Object[]> updates = new ArrayList<>();

      

        for (TCSCPPUnitsSDPlanDTO dto : tcsCppUnitsSDPlanDTOs) {  

             // database date format : yyyy-MM-dd
          // response format : dd-M-yyyy
         // frontend display date format : dd-M-yyyy
        

        //  System.out.println("ibrDueDate: " + ibrDueDate);
        //  System.out.println("shutDownDate: " + shutDownDate);
        //  System.out.println("startUpDate: " + startUpDate);
             updates.add(new Object[] { dto.getMachine(), dto.getIbrDueDate(), dto.getGtMaintenance(), dto.getNoOfDays(), dto.getShutDownDate(), dto.getStartUpDate(), dto.getMajorJobs(), dto.getId() });
     
    }

    if(updates.size() > 0) {
        String sql = "UPDATE TCS_CPPUnitsSD_Plan SET Machine = ?, IBRDueDate = ?, GTMaintenance = ?, NoOfDays = ?, ShutDownDate = ?, StartUpDate = ?, MajorJobs = ? WHERE Id = ?";

        jdbcTemplate.batchUpdate(sql, updates);

    }
}
}
