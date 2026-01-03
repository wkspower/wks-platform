package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.CrudeBlendDTO;
import com.wks.caseengine.dto.CrudeBlendScreenDTO;
import com.wks.caseengine.dto.CrudeBlendWindowPostRequestDTO;
import com.wks.caseengine.dto.CrudeSpecificConstraintsDTO;
import com.wks.caseengine.dto.MasterCrudeBlendDTO;
import com.wks.caseengine.dto.VGOVRDropDTO;
import com.wks.caseengine.repository.CrudeBlendWindowRepository;
import com.wks.caseengine.repository.FinancialYearMonthRepository;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class CrudeBlendWindowService {

    @Autowired
    private CrudeBlendWindowRepository crudeRepo;

    @Autowired
    private ObjectMapper objectMapper;

    // @Autowired
    // private FinancialYearMonthRepository fyRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

     
 //   public List<MasterCrudeBlendDTO> getCrudeBlendWindowData(String plantId, String siteId) {

 public List<CrudeBlendScreenDTO> getCrudeBlendWindowData(String plantId, String siteId, String financialYear) {

        // List<MasterCrudeBlendDTO> responseList = new java.util.ArrayList<>();

        // get financial year month ids for given financial year
        // int startYear = Integer.parseInt(financialYear.substring(0, 4));
        // int endYear = Integer.parseInt("20" + financialYear.substring(5));

        // List<UUID> AllFyIdsInGivenYear =  fyRepo.findFinancialYearMonths(startYear, endYear).stream()
        // .map(row -> UUID.fromString(row[1].toString())).toList();


         List<CrudeBlendScreenDTO> crudeBlendScreenDTOs = new java.util.ArrayList<>();

        // *******  fetch CrudeBlendData ********** 

        List<CrudeBlendDTO> crudeBlend = crudeRepo.findCrudeBlendByPlantIdAndSiteId(java.util.UUID.fromString(plantId), java.util.UUID.fromString(siteId), financialYear).stream().map(proj -> {
            CrudeBlendDTO dto = new CrudeBlendDTO();
            dto.setId(proj.getId());
            dto.setProperty(proj.getProperty());
            dto.setStream(proj.getStream());
            dto.setUnit(proj.getUnit());
            dto.setMinValue(proj.getMinValue());
            dto.setMaxValue(proj.getMaxValue());
            dto.setCriticality(proj.getCriticality());
            dto.setRemarks(proj.getRemarks());
            dto.setType(proj.getType());
            return dto;
        }).toList();

        // hardcoding headers and keys for crude blend 
        List<String> headers = List.of("Id", "Property", "Stream", "Unit", "Min Value", "Max Value", "Criticality", "Remarks", "Type");
        List<String> keys = List.of( "id", "property", "stream", "unit", "minValue", "maxValue", "criticality", "remarks", "type");

        MasterCrudeBlendDTO<CrudeBlendDTO> masterCrudeBlendDTO = new MasterCrudeBlendDTO<>();
        masterCrudeBlendDTO.setHeaders(headers);
        masterCrudeBlendDTO.setKeys(keys);
        masterCrudeBlendDTO.setResults(crudeBlend);

        CrudeBlendScreenDTO crudeBlendScreenDTO = new CrudeBlendScreenDTO();
        crudeBlendScreenDTO.setTable("CrudeBlendWindow");
        crudeBlendScreenDTO.setData(masterCrudeBlendDTO);
        crudeBlendScreenDTOs.add(crudeBlendScreenDTO);

      //  responseList.add(masterCrudeBlendDTO);


        // ******** fetch CrudeSpecificConstraints *************

        List<CrudeSpecificConstraintsDTO> crudeSpecificConstraints = crudeRepo.findCrudeSpecificConstraintsByPlant_FK_IdAndSite_FK_Id(java.util.UUID.fromString(plantId), java.util.UUID.fromString(siteId), financialYear).stream().map(proj -> {
            CrudeSpecificConstraintsDTO dto = new CrudeSpecificConstraintsDTO();
            dto.setId(proj.getId());
            dto.setCrude(proj.getCrude());
            dto.setMaxBlendLimit(proj.getMaxBlendLimit());
            dto.setReasons(proj.getReasons());
            return dto;
        }).toList();

        List<String> csHeaders = List.of("Id", "Crude", "Max Blend Limit (%)", "Reasons");
        List<String> csKeys = List.of("id", "crude", "maxBlendLimit", "reasons");

        MasterCrudeBlendDTO<CrudeSpecificConstraintsDTO> masterCrudeSpecificConstraintsDTO = new MasterCrudeBlendDTO<>();
        masterCrudeSpecificConstraintsDTO.setResults(crudeSpecificConstraints);
        masterCrudeSpecificConstraintsDTO.setHeaders(csHeaders);
        masterCrudeSpecificConstraintsDTO.setKeys(csKeys);

         CrudeBlendScreenDTO crudeSpecificConstraintsScreenDTO = new CrudeBlendScreenDTO();
         crudeSpecificConstraintsScreenDTO.setTable("CrudeSpecificConstraints");
            crudeSpecificConstraintsScreenDTO.setData(masterCrudeSpecificConstraintsDTO);
            crudeBlendScreenDTOs.add(crudeSpecificConstraintsScreenDTO);

      //  responseList.add(masterCrudeSpecificConstraintsDTO);

        // ******** fetch VGOVRDrop *************

        List<com.wks.caseengine.dto.VGOVRDropDTO> vgovrDrop = crudeRepo.findVGOVRDropByPlant_FK_IdAndSite_FK_Id(java.util.UUID.fromString(plantId), java.util.UUID.fromString(siteId), financialYear).stream().map(proj -> {
            com.wks.caseengine.dto.VGOVRDropDTO dto = new com.wks.caseengine.dto.VGOVRDropDTO();
            dto.setId(proj.getId());
            dto.setKbpsd(proj.getkbpsd());
            dto.setValue_345(proj.getvalue_345());
            dto.setRemarks(proj.getRemarks());
            return dto;
        }).toList();

        List<String> vgovrHeaders = List.of("Id", "T'put, KBPSD", "345", "Remarks");
        List<String> vgovrKeys = List.of("id", "kbpsd", "value_345", "remarks");

        MasterCrudeBlendDTO<com.wks.caseengine.dto.VGOVRDropDTO> masterVGOVRDropDTO = new MasterCrudeBlendDTO<>();
        masterVGOVRDropDTO.setHeaders(vgovrHeaders);
        masterVGOVRDropDTO.setKeys(vgovrKeys);
        masterVGOVRDropDTO.setResults(vgovrDrop);

            CrudeBlendScreenDTO vgovrDropScreenDTO = new CrudeBlendScreenDTO();
            vgovrDropScreenDTO.setTable("VGOVRDROP");
            vgovrDropScreenDTO.setData(masterVGOVRDropDTO);
            crudeBlendScreenDTOs.add(vgovrDropScreenDTO);

      //  responseList.add(masterVGOVRDropDTO);

        return crudeBlendScreenDTOs;
    }

    public void updateCrudeBlendWindowData(CrudeBlendWindowPostRequestDTO<?> payload, String table, String financialYear) {
   
        if(financialYear == null || financialYear.isEmpty()) {
            throw new IllegalArgumentException("Financial year must be provided");
        }

        if(payload == null || payload.getResults() == null || payload.getResults().isEmpty()) {
            throw new IllegalArgumentException("Payload with results must be provided");
        }

     // get all financialYearMonth ids for given financial year
        // int startYear = Integer.parseInt(financialYear.substring(0, 4));
        // int endYear = startYear + 1;
      
        // List<UUID> AllfyIdsInGivenYear =  fyRepo.findFinancialYearMonths(startYear, endYear).stream()
        // .map(row -> UUID.fromString(row[1].toString())).toList();

        

     

      System.out.println("payload received: " + payload);

       if(table == null || table.isEmpty()) {
           throw new IllegalArgumentException("Table name must be provided");  }

        if(table.equals("CrudeBlendWindow")) {
 
              List<CrudeBlendDTO> crudeBlendDTOs =  convertList(payload.getResults(), CrudeBlendDTO.class);
              handleCrudeBlendWindowUpdate(crudeBlendDTOs);

              System.out.println("dtos to be updated: " + crudeBlendDTOs);

             
        }

        if(table.equals("CrudeSpecificConstraints")) { 
            List<CrudeSpecificConstraintsDTO> crudeSpecificConstraintsDTOs =  convertList(payload.getResults(), CrudeSpecificConstraintsDTO.class);
            System.out.println("dtos to be updated: " + crudeSpecificConstraintsDTOs);
            handleCrudeSpecificConstraintsUpdate(crudeSpecificConstraintsDTOs);
        }

        if(table.equals("VGOVRDrop")) {
            List<VGOVRDropDTO> vgovrDropDTOs =  convertList(payload.getResults(), VGOVRDropDTO.class);
            System.out.println("dtos to be updated: " + vgovrDropDTOs);
            handleVGOVRDropUpdate(vgovrDropDTOs);
        }
        
    }

    public void handleCrudeBlendWindowUpdate(List<CrudeBlendDTO> crudeBlendDTOs) {

           List<Object[]> updates = new ArrayList<>();

       for( CrudeBlendDTO dto : crudeBlendDTOs) {

      
             updates.add(new Object[] { dto.getMinValue(), dto.getMaxValue(), dto.getCriticality(), dto.getRemarks(), dto.getId() });      //Min, max, criticality and remarks
            
            }

             if (!updates.isEmpty()) { 
              String updateSql = " update CrudeBlendWindow set MinValue = ?, MaxValue = ?, Criticality = ?, Remarks = ? where Id = ?";

              jdbcTemplate.batchUpdate(updateSql, updates);

             }
         
    }

    public void handleCrudeSpecificConstraintsUpdate(List<CrudeSpecificConstraintsDTO> crudeSpecificConstraintsDTOs) { 
        List<Object[]> updates = new ArrayList<>();
        for( CrudeSpecificConstraintsDTO dto : crudeSpecificConstraintsDTOs) {
          updates.add(new Object[] { dto.getMaxBlendLimit(), dto.getReasons(), dto.getId() });           
        }
        if (!updates.isEmpty()) { 
            String updateSql = " update CrudeSpecificConstraints set MaxBlendLimit = ?, Reasons = ? where Id = ?";
            jdbcTemplate.batchUpdate(updateSql, updates);
        }
    }

    public void handleVGOVRDropUpdate(List<VGOVRDropDTO> vgovrDropDTOs) {
        List<Object[]> updates = new ArrayList<>();
        for( VGOVRDropDTO dto : vgovrDropDTOs) {
          updates.add(new Object[] { dto.getKbpsd(), dto.getValue_345(), dto.getRemarks(), dto.getId() });           
        }
        if (!updates.isEmpty()) { 
            String updateSql = " update VGOVRDrop set Kbpsd = ?, Value_345 = ?, Remarks = ? where Id = ?";
            jdbcTemplate.batchUpdate(updateSql, updates);
        }
    }

// private <T> List<T> convertList(List<?> raw, Class<T> clazz) {
//     return raw.stream()
//             .map((Object o) -> objectMapper.convertValue(o, clazz))
//             .toList();
// }

private <T> List<T> convertList(List<?> raw, Class<T> clazz) {

  //   ObjectMapper objectMapper = new ObjectMapper();
    return raw.stream()
            .map(o -> objectMapper.convertValue(o, clazz))
            .collect(Collectors.toList());
}






}