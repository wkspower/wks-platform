package com.wks.caseengine.service.tcs;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.tcs.TCSUnitCapacityDTO;
import com.wks.caseengine.dto.tcs.TCSUnitCapacityUOMDTO;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.entity.tcs.TCSUnitCapacity;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.repository.tcs.TCSUnitCapacityRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class TCSUnitCapacityServiceImpl implements TCSUnitCapacityService {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
	private DataSource dataSource;
    @Autowired
	private PlantsRepository plantsRepository;
    @Autowired
    private SiteRepository siteRepository;
	@Autowired
	private VerticalsRepository verticalRepository;
    @Autowired
    private TCSUnitCapacityRepository tcsUnitCapacityRepository;
  
    @Override
    public Map<String, Object> getAll(
        String plantId,
        String aopYear,
        String capacityType,
     //   String uom,
        String siteId,
        String verticalId) {
        


      
Sites site = null;
Verticals vertical = null;
//final Plants plant = null;
        // Validation
        if (plantId != null) {
       Plants plant = plantsRepository
            .findById(UUID.fromString(plantId))
            .orElseThrow(() -> new RuntimeException("Plant not found for ID: " + plantId));

       
         site = siteRepository
            .findById(plant.getSiteFkId())
            .orElseThrow(() -> new RuntimeException("Site not found for ID: " + plantId));

            vertical = verticalRepository
            .findById(plant.getVerticalFKId())
            .orElseThrow(() -> new RuntimeException("Vertical not found for ID: " + plant.getVerticalFKId()));
        }

    else  {
        site = siteRepository
            .findById(UUID.fromString(siteId))
            .orElseThrow(() -> new RuntimeException("Site not found for ID: " + siteId));
    

 
    vertical = verticalRepository
        .findById(UUID.fromString(verticalId))
        .orElseThrow(() -> new RuntimeException("Vertical not found for ID: " + verticalId));
    }

        Map<String, Object> map = new HashMap<>();
        try {
            List<Object[]> results = new ArrayList<>();
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

         
              results = getData(
                plantId,
                aopYear,
                vertical.getName().toUpperCase(),
                site.getName().toUpperCase(),
                capacityType
               // uom
            );  

                System.out.println("getData fetched successfully");
            
        //    for(Object[] row : results) {
        // System.out.println("row1: " + row[0].toString());
        // System.out.println("row2: " + row[1].toString());
        // System.out.println("row3: " + row[2].toString());
        // System.out.println("row4: " + Double.parseDouble(row[3].toString()));
        // System.out.println("row5: " + Double.parseDouble(row[4].toString()));
        // System.out.println("row6: " + row[5].toString());
        // System.out.println("row7: " + dateFormatter.parse(row[6].toString()));
        //     System.out.println(row[0].toString() + " " + row[1].toString() + " " + row[2].toString() + " " + Double.parseDouble(row[3].toString()) + " " + Double.parseDouble(row[4].toString()) + " " + row[5].toString() + " " + dateFormatter.parse(row[6].toString()));
        //    }

            List<TCSUnitCapacityDTO> resultsList = new ArrayList<>();
            for (Object[] row : results) {
                TCSUnitCapacityDTO dto = new TCSUnitCapacityDTO();
                dto.setId(row[0] != null ? row[0].toString() : null);
                dto.setParticulates(row[1] != null ? row[1].toString() : null);
             //   dto.setUom(row[2] != null ? row[2].toString() : null);
                dto.setSummer(row[2] != null ? Double.parseDouble(row[2].toString()) : null);
                dto.setWinter(row[3] != null ? Double.parseDouble(row[3].toString()) : null);
                dto.setRemark(row[4] != null ? row[4].toString() : null);
                dto.setInsertedDateTime(row[5] != null ? dateFormatter.parse(row[5].toString()) : null);
                resultsList.add(dto);
            }
            map.put("results", resultsList);

            // headers mapping
            List<String> headers = getHeaders(
                plantId,
                aopYear,
                vertical.getName().toUpperCase(),
                site.getName().toUpperCase(),
                capacityType
              //  uom
            );
            map.put("headers", headers);

            List<String> keys = new ArrayList<>();
            for (Field field : TCSUnitCapacityDTO.class.getDeclaredFields()) {
                keys.add(field.getName());
            }
            map.put("keys", keys);

            return map;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data", e);
        }
    }

    private List<Object[]> getData(
        String plantId,
        String aopYear,
        String verticalName,
        String siteName,
        String capacityType
    //    String uom
    ) {
            
        try {            
            // Stored Procedure name
            String procedureName = "GetTcsUnitCapacity";
            if (!"MEG".equalsIgnoreCase(verticalName)) {
                if(plantId != null) {
                procedureName = verticalName + "_" + "ALL" + "_GetTcsUnitCapacity";  
            }
            else  {
                procedureName = verticalName + "_" + "ALL" + "_GetTcsUnitCapacity_OutPut"; 
            }
            }

            // Prepare native SQL call with parameters
            String sql = "";
            if(plantId != null) {
            sql = "EXEC " + procedureName + " @plantId = :plantId, @aopYear = :aopYear, @capacityType = :capacityType";
            }
            else {
              //  sql = "EXEC " + procedureName + " @aopYear = :aopYear, @capacityType = :capacityType";
              sql = "EXEC " + procedureName + " ?, ?";

            }

            // Call the stored procedure
            Query query = entityManager.createNativeQuery(sql);
            if(plantId != null) {
            query.setParameter("plantId", plantId);
            
          
            query.setParameter("aopYear", aopYear);
            query.setParameter("capacityType", capacityType);  }

            else {
                query.setParameter(1, aopYear);      // First parameter
                query.setParameter(2, capacityType);

            }
         //   query.setParameter("uom", uom);

            return query.getResultList();
        } catch (IllegalArgumentException e) {
            throw new RestInvalidArgumentException("Invalid UUID format", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data", e);
        }
    }

    private List<String> getHeaders(
        String plantId,
        String aopYear,
        String verticalName,
        String siteName,
        String capacityType
    //    String uom
    ) {

        String procedureName = "GetTcsUnitCapacity";
        if (!"MEG".equalsIgnoreCase(verticalName)) {
            if(plantId != null) {
            procedureName = verticalName + "_" + "ALL" + "_GetTcsUnitCapacity";
            }
            else  {
                procedureName = verticalName + "_" + "ALL" + "_GetTcsUnitCapacity_OutPut";
            }
        }

        String callableSql = "";
        if(plantId != null) {
        callableSql = "{call " + procedureName + "(?, ?, ?)}";  }
        else {
            callableSql = "{call " + procedureName + "(?, ?)}";
        }

        List<String> headers = new ArrayList<>();
		try (
            Connection conn = dataSource.getConnection();
			CallableStatement stmt = conn.prepareCall(callableSql)) {
              if(plantId != null) {
			stmt.setString(1, plantId);  
                    
			stmt.setString(2, aopYear);
            stmt.setString(3, capacityType);  
             //     stmt.setString(4, uom);
        
        }

        else {
            stmt.setString(1, aopYear);
            stmt.setString(2, capacityType);
        }
      

			boolean hasResultSet = stmt.execute();

			// Move forward until we find a result set
			while (!hasResultSet && stmt.getUpdateCount() != -1) {
				hasResultSet = stmt.getMoreResults();
			}

			// If a result set is found, get metadata and headers
			if (hasResultSet) {
				try (ResultSet rs = stmt.getResultSet()) {
					ResultSetMetaData metaData = rs.getMetaData();
					int columnCount = metaData.getColumnCount();

					for (int i = 1; i <= columnCount; i++)
                    {
                        String columnLabel = metaData.getColumnLabel(i);
                        headers.add(columnLabel);
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to fetch headers", e);
		}

        return headers;
    }

    @Override
    public AOPMessageVM saveOrUpdate(
        String plantId,
        String year,
        String capacityType,
       // String uom,
        List<TCSUnitCapacityDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            throw new RestInvalidArgumentException("Payload cannot be empty", null);
        }

        try {
            List<TCSUnitCapacity> savedList = new ArrayList<>();
            for (TCSUnitCapacityDTO dto : dtoList) {                
                String existingId = null;
                if (dto.getId() != null && !dto.getId().isBlank()) {
                    try {
                        existingId = dto.getId();
                    } catch (IllegalArgumentException ex) {
                        throw new RestInvalidArgumentException("Invalid UUID format", ex);
                    }
                }

                TCSUnitCapacity entity = new TCSUnitCapacity();
                if (existingId == null || existingId.trim().isEmpty()) {
                    // The entity is being created
                    entity.setInsertedDateTime(new Date());
                } else {
                    // The entity is being updated
                    entity.setId(UUID.fromString(dto.getId()));
                    entity.setInsertedDateTime(dto.getInsertedDateTime());
                    entity.setUpdatedDateTime(new Date());
                }
                entity.setCapacityType(capacityType);
            //    entity.setUom(dto.getUom());
                entity.setSummer(dto.getSummer());
                entity.setWinter(dto.getWinter());
                entity.setRemark(dto.getRemark());
                entity.setAopYear(year);
                entity.setPlantFkId(UUID.fromString(plantId));

                tcsUnitCapacityRepository.save(entity);
                savedList.add(entity);
            }

            AOPMessageVM vm = new AOPMessageVM();
            vm.setCode(200);
            vm.setMessage("Data saved successfully");
            vm.setData(savedList.stream().map(this::toDTO).toList());
            return vm;

        } catch (RestInvalidArgumentException e) {
            throw e;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to save data", ex);
        }
    }
   
    private TCSUnitCapacityDTO toDTO(TCSUnitCapacity entity) {
        return TCSUnitCapacityDTO.builder()
            .id(entity.getId() != null ? entity.getId().toString() : null)
          //  .uom(entity.getUom())
            .summer(entity.getSummer())
            .winter(entity.getWinter())
            .remark(entity.getRemark())
            .build();
    }

    @Override
    public List<TCSUnitCapacityUOMDTO> getAllUOM(
        String plantId,
        String aopYear,
        String capacityType,
          String verticalId) {

            Verticals vertical = null;

            if(plantId != null) {
            Plants plant = plantsRepository
                .findById(UUID.fromString(plantId))
                .orElseThrow(() -> new RuntimeException("Plant not found for ID: " + plantId));  
           vertical = verticalRepository
                .findById(plant.getVerticalFKId())
                .orElseThrow(() -> new RuntimeException("Vertical not found for ID: " + plant.getVerticalFKId()));  
            
            }

            else if(verticalId != null) {
                vertical = verticalRepository
                .findById(UUID.fromString(verticalId))
                .orElseThrow(() -> new RuntimeException("Vertical not found for ID: " + verticalId));
            }

            try {
                List<TCSUnitCapacityUOMDTO> results = getAllUOMData(
                    vertical.getName().toUpperCase(),
                    plantId,
                    aopYear,
                    capacityType);
                return results;
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch data", e);
            }
        }

    private List<TCSUnitCapacityUOMDTO> getAllUOMData(
        String verticalName,
        String plantId,
        String aopYear,
        String capacityType) {
        try {            
            // Stored Procedure name
            String procedureName = "GetTcsUnitCapacity_UOM";
            if (!"MEG".equalsIgnoreCase(verticalName)) {
                if(plantId != null) {
                procedureName = verticalName + "_" + "ALL" + "_GetTcsUnitCapacity_UOM"; 
            }
            else {
                procedureName = verticalName + "_" + "ALL" + "_GetTcsUnitCapacity_UOM_OutPut";
            }
            }

            // Prepare native SQL call with parameters
            String sql = "";
            if(plantId != null) {
                sql = "EXEC " + procedureName + " @plantId = :plantId, @aopYear = :aopYear, @capacityType = :capacityType";
            }
            else {
                sql = "EXEC " + procedureName + " @aopYear = :aopYear, @capacityType = :capacityType";
            }

            // Call the stored procedure
            Query query = entityManager.createNativeQuery(sql);
            if(plantId != null) {
            query.setParameter("plantId", plantId);  }
            query.setParameter("aopYear", aopYear);
            query.setParameter("capacityType", capacityType);

            var queryResults = (List<Object[]>)query.getResultList();
            var results = new ArrayList<TCSUnitCapacityUOMDTO>();
            for (Object[] row : queryResults)
            {
                var dto = new TCSUnitCapacityUOMDTO();
                dto.setId(row[0] != null ? row[0].toString() : null);
                dto.setName(row[1] != null ? row[1].toString() : null);
                results.add(dto);
            }

            return results;
        } catch (IllegalArgumentException e) {
            throw new RestInvalidArgumentException("Invalid UUID format", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data", e);
        }
    }
}
