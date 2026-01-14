package com.wks.caseengine.service.tcs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.tcs.FurnaceDTO;
import com.wks.caseengine.dto.tcs.GCalPerHrDTO;
import com.wks.caseengine.dto.tcs.MasterFurnaceDTO;
import com.wks.caseengine.repository.FinancialYearMonthRepository;
import com.wks.caseengine.repository.tcs.FurnaceProjection;
import com.wks.caseengine.repository.tcs.FurnaceRepository;

@Service
public class FurnaceService {

    @Autowired
    private FurnaceRepository furnaceRepository;

    @Autowired
    private FinancialYearMonthRepository fyRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public MasterFurnaceDTO getFurnaceData(
            String financialYear,
            UUID siteId,
            UUID plantId
    ) {

      
List<FurnaceProjection> projections = new ArrayList<>();

if(plantId != null) {
    projections = furnaceRepository.getFurnaceData(financialYear, siteId, plantId);
}
else {
    projections = furnaceRepository.getFurnaceOutputData(financialYear, siteId);
}

        List<FurnaceDTO> furnaceDTOs = projections.stream().map(p -> {
            FurnaceDTO dto = new FurnaceDTO();

           
            dto.setFurnace(p.getFurnace());

            dto.setApr(p.getApr());
            dto.setMay(p.getMay());
            dto.setJun(p.getJun());
            dto.setJul(p.getJul());
            dto.setAug(p.getAug());
            dto.setSep(p.getSep());
            dto.setOct(p.getOct());
            dto.setNov(p.getNov());
            dto.setDec(p.getDec());
            dto.setJan(p.getJan());
            dto.setFeb(p.getFeb());
            dto.setMar(p.getMar());

            dto.setRemarks(p.getRemarks());
          //  dto.setGCalPerHr(p.getGCalPerHr());

            return dto;
        }).toList();

        // get GCalPerHr data from Furnace_GCalPerHr_Mapping table
        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;

        List<Object[]> financialYearMonths = fyRepo.findFinancialYearMonths(startYear, endYear);
        Map<Integer, UUID> financialYearMonthIds = new HashMap<Integer, UUID>();
        for (Object[] financialYearMonth : financialYearMonths) {
            financialYearMonthIds.put(Integer.parseInt(financialYearMonth[0].toString()), UUID.fromString(financialYearMonth[1].toString()));
        }

        List<Object[]> furnaceGCalPerHrMapping = furnaceRepository.getFurnaceGCalPerHrMapping(financialYearMonthIds.values().stream().toList());

        GCalPerHrDTO gCalPerHrDTO = new GCalPerHrDTO();
        for (Object[]  GCalPerHrMapping : furnaceGCalPerHrMapping) {
           
              UUID financialYearMonthId = UUID.fromString(GCalPerHrMapping[1].toString());
              Integer month = financialYearMonthIds.entrySet().stream().filter(entry -> entry.getValue().equals(financialYearMonthId)).findFirst().get().getKey();
                 // convert integer month values to String eg 4: April
                 switch (month) {
                  case 4:
                    gCalPerHrDTO.setApr(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                  case 5:
                    gCalPerHrDTO.setMay(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                  case 6:
                    gCalPerHrDTO.setJun(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                     
                   case 7:
                    gCalPerHrDTO.setJul(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   case 8:
                    gCalPerHrDTO.setAug(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   case 9:
                    gCalPerHrDTO.setSep(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   case 10:
                    gCalPerHrDTO.setOct(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   case 11:
                    gCalPerHrDTO.setNov(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   case 12:
                    gCalPerHrDTO.setDec(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   case 1:
                    gCalPerHrDTO.setJan(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   case 2:
                    gCalPerHrDTO.setFeb(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   case 3:
                    gCalPerHrDTO.setMar(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   default:
                       throw new RuntimeException("Invalid month: " + month);
                   
                 }
        }
        MasterFurnaceDTO masterFurnaceDTO = new MasterFurnaceDTO();
        masterFurnaceDTO.setFurnaceData(furnaceDTOs);
        masterFurnaceDTO.setGCalPerHrData(gCalPerHrDTO);
        return masterFurnaceDTO;

    }

    public void updateFurnaceData(List<FurnaceDTO> furnaceDTOs, String financialYear, UUID siteId, UUID plantId) {

     

        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;

        // Get the financial year month ids for the financial year
        List<Object[]> financialYearMonths = fyRepo.findFinancialYearMonths(startYear, endYear);
        Map<Integer, UUID> financialYearMonthIds = new HashMap<Integer, UUID>();
        for (Object[] financialYearMonth : financialYearMonths) {
            financialYearMonthIds.put(Integer.parseInt(financialYearMonth[0].toString()), UUID.fromString(financialYearMonth[1].toString()));
        }

        List<Object[]> updates = new ArrayList<Object[]>();
        List<Object[]> inserts = new ArrayList<Object[]>();


         for (FurnaceDTO furnaceDTO : furnaceDTOs) {  

            

            if(furnaceDTO.getApr() != null) {
                UUID financialYearMonthId = financialYearMonthIds.get(4);
                updates.add(new Object[] { furnaceDTO.getApr(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
               
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(4), siteId, plantId});
         }

         if(furnaceDTO.getMay() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(5);
            updates.add(new Object[] { furnaceDTO.getMay(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(5), siteId, plantId});
         }
         
         if(furnaceDTO.getJun() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(6);
            updates.add(new Object[] { furnaceDTO.getJun(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(6), siteId, plantId});
         }
         
         if(furnaceDTO.getJul() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(7);
            updates.add(new Object[] { furnaceDTO.getJul(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(7), siteId, plantId});
         }
         
         if(furnaceDTO.getAug() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(8);
            updates.add(new Object[] { furnaceDTO.getAug(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(8), siteId, plantId});
         }
         
         if(furnaceDTO.getSep() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(9);
            updates.add(new Object[] { furnaceDTO.getSep(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(9), siteId, plantId});
         }
         
         if(furnaceDTO.getOct() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(10);
            updates.add(new Object[] { furnaceDTO.getOct(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(10), siteId, plantId});
         }
         
         if(furnaceDTO.getNov() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(11);
            updates.add(new Object[] { furnaceDTO.getNov(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(11), siteId, plantId});
         }
         
         if(furnaceDTO.getDec() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(12);
            updates.add(new Object[] { furnaceDTO.getDec(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(12), siteId, plantId});
         }
         
         if(furnaceDTO.getJan() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(1);
            updates.add(new Object[] { furnaceDTO.getJan(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(1), siteId, plantId});
         }
         
         if(furnaceDTO.getFeb() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(2);
            updates.add(new Object[] { furnaceDTO.getFeb(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(2), siteId, plantId});
         }
         
         if(furnaceDTO.getMar() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(3);
            updates.add(new Object[] { furnaceDTO.getMar(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(3), siteId, plantId});
         }
         
    }

    if(updates.size() > 0) { 
        String updateQuery = "UPDATE Furnace SET Value = ? , Remarks = ? WHERE Furnace = ? AND FinancialYearMonthId = ?";
        jdbcTemplate.batchUpdate(updateQuery, updates);
    }

    if(inserts.size() > 0) {
        String insertQuery = "INSERT INTO Furnace (Id, Furnace, Value, Remarks, FinancialYearMonthId, Site_FK_Id, Plant_FK_Id) VALUES (NEWID(), ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(insertQuery, inserts);
    }
}
}
