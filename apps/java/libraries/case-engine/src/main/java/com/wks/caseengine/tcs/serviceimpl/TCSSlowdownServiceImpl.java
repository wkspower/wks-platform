package com.wks.caseengine.tcs.serviceimpl;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.tcs.dto.TCSSlowdownDTO;
import com.wks.caseengine.tcs.entity.TCSSlowdown;
import com.wks.caseengine.tcs.repository.TCSSlowdownRepository;
import com.wks.caseengine.tcs.service.TCSSlowdownService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class TCSSlowdownServiceImpl implements TCSSlowdownService {

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
    private TCSSlowdownRepository tcsSlowdownRepository;
    
    @Override
    public Map<String, Object> getAll(String plantId, String aopYear, String siteId, String verticalId) {
        // Validation
Sites site = null;
Verticals vertical = null;

if(plantId != null) {
        Plants plant = plantsRepository
            .findById(UUID.fromString(plantId))
            .orElseThrow(() -> new RuntimeException("Plant not found for ID: " + plantId));
        site = siteRepository
            .findById(plant.getSiteFkId())
            .orElseThrow(() -> new RuntimeException("Site not found for ID: " + plantId));
        vertical = verticalRepository
            .findById(plant.getVerticalFKId())
            .orElseThrow(() -> new RuntimeException("Vertical not found for ID: " + plant.getVerticalFKId()));  }

        else {
            site = siteRepository
            .findById(UUID.fromString(siteId))
            .orElseThrow(() -> new RuntimeException("Site not found for ID: " + siteId));
        vertical = verticalRepository
            .findById(UUID.fromString(verticalId))
            .orElseThrow(() -> new RuntimeException("Vertical not found for ID: " + verticalId));
        }
        
        Map<String, Object> map = new HashMap<>();
        try {
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            List<Object[]> results = getData(
                plantId,
                aopYear,
                vertical.getName().toUpperCase(),
                site.getId(),
                site.getName().toUpperCase());
            List<TCSSlowdownDTO> resultsList = new ArrayList<>();
            //values mapping
            for (Object[] row : results) {
                TCSSlowdownDTO dto = new TCSSlowdownDTO();
                dto.setId(row[0] != null ? row[0].toString() : null);
                dto.setParticulates(row[1] != null ? row[1].toString() : null);
                dto.setDurationInDays(row[2] != null ? Integer.parseInt(row[2].toString()) : null);
                dto.setThroughputDuringSlowdown(row[3] != null ? Double.parseDouble(row[3].toString()) : null);
                dto.setThroughputUOM(row[4] != null ? row[4].toString() : null);
                dto.setStartDate(row[5] != null ? dateFormatter.parse(row[5].toString()) : null);
                dto.setEndDate(row[6] != null ? dateFormatter.parse(row[6].toString()) : null);
                dto.setPurpose(row[7] != null ? row[7].toString() : null);
                dto.setInsertedDateTime(row[8] != null ? dateTimeFormatter.parse(row[8].toString()) : null);
                resultsList.add(dto);
            }
            map.put("results", resultsList);

            // headers mapping
            System.out.println("headers mapping started");
            List<String> headers = getHeaders(
                plantId,
                aopYear,
                vertical.getName().toUpperCase(),
                site.getId(),
                site.getName().toUpperCase());
            map.put("headers", headers);

            // keys mapping
            List<String> keys = new ArrayList<>();
            for (Field field : TCSSlowdownDTO.class.getDeclaredFields()) {
                String fieldName = field.getName();
                    keys.add(fieldName);
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
        UUID siteId,
        String siteName) {
            
        try {            
            // Stored Procedure name
            String procedureName = "GetTcsSlowdown";
            if (!"MEG".equalsIgnoreCase(verticalName)) {
                if(plantId != null) {
             //   procedureName = verticalName + "_" + siteName + "_GetTcsSlowdown"; 
                procedureName =  "CRUDE_DTA_GetTcsSlowdown";
             }

                else {
                    procedureName = "GetTcsSlowdown_OutPut";
                }
            }

            // Prepare native SQL call with parameters

            String sql = "";
            if(plantId != null) {
            sql = "EXEC " + procedureName + " @plantId = :plantId, @aopYear = :aopYear";
            }
            else {
                sql = "EXEC " + procedureName + " @siteId = :siteId, @aopYear = :aopYear";
            }

            // Call the stored procedure
            Query query = entityManager.createNativeQuery(sql);
            if(plantId != null) {
            query.setParameter("plantId", plantId);
            query.setParameter("aopYear", aopYear);  
        }
        else {
            query.setParameter("siteId", siteId);
            query.setParameter("aopYear", aopYear);
        }

        System.out.println("data fetched successfully");

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
        UUID siteId1,
        String siteName) {

           String siteId = siteId1.toString();

        String procedureName = "GetTcsSlowdown";
        if (!"MEG".equalsIgnoreCase(verticalName)) {
            if(plantId != null) {
            // procedureName = verticalName + "_" + siteName + "_GetTcsSlowdown";
            procedureName =  "CRUDE_DTA_GetTcsSlowdown";
            }
            else {
             //   procedureName = verticalName + "_" + siteName + "_GetTcsSlowdown_OutPut";
                procedureName = "GetTcsSlowdown_OutPut";
            }
        }
        String callableSql = "";
        if(plantId != null) {
        callableSql = "{call " + procedureName + "(?, ?)}";
        }
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
            }
            else {
             //   stmt.setString(1, siteId.toString());
                stmt.setString(1, siteId);
                stmt.setString(2, aopYear);
            }

			boolean hasResultSet = stmt.execute();

			// Move forward until we find a result set
			while (!hasResultSet && stmt.getUpdateCount() != -1) {
				hasResultSet = stmt.getMoreResults();
			}
            System.out.println("headers fetched successfully");
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
        List<TCSSlowdownDTO> dtoList) {

        if (dtoList == null || dtoList.isEmpty()) {
            throw new RestInvalidArgumentException("Payload cannot be empty", null);
        }

        AOPMessageVM vm = new AOPMessageVM();

        try {
            List<TCSSlowdown> savedList = new ArrayList<>();

            for (TCSSlowdownDTO dto : dtoList) {
                String existingId = null;
                if (dto.getId() != null && !dto.getId().isBlank()) {
                    try {
                        existingId = dto.getId();
                    } catch (IllegalArgumentException ex) {
                        throw new RestInvalidArgumentException("Invalid UUID format", ex);
                    }
                }

                if (dto.getDurationInDays() == null || dto.getDurationInDays() <= 0) {
                    throw new RestInvalidArgumentException("Tentative Duration (Days) must be greater than 0", null);
                }

                if (dto.getThroughputDuringSlowdown() == null || dto.getThroughputDuringSlowdown() < 0) {
                    throw new RestInvalidArgumentException("Throughput during Slowdown is required", null);
                }

                if (dto.getThroughputUOM() == null || dto.getThroughputUOM().isBlank()) {
                    throw new RestInvalidArgumentException("Throughput UoM is required", null);
                }

                if (dto.getStartDate() == null) {
                    throw new RestInvalidArgumentException("Start Date is required", null);
                }

                if (dto.getPurpose() == null || dto.getPurpose().isBlank()) {
                    throw new RestInvalidArgumentException("Purpose of Slowdown is required", null);
                }
               
                TCSSlowdown entity = new TCSSlowdown();
                if (existingId == null || existingId.trim().isEmpty()) {
                    // The entity is being created
                    entity.setInsertedDateTime(new Date());
                } else {
                    // The entity is being updated
                    entity.setId(UUID.fromString(dto.getId()));
                    entity.setInsertedDateTime(dto.getInsertedDateTime());
                    entity.setUpdatedDateTime(new Date());
                }
                entity.setPlantFkId(UUID.fromString(plantId));
                entity.setAopYear(year);
                entity.setTentativeDurationInDays(dto.getDurationInDays());
                entity.setThroughputDuringSlowdown(dto.getThroughputDuringSlowdown());
                entity.setThroughputUOM(dto.getThroughputUOM());
                entity.setStartDate(dto.getStartDate());
                entity.setPurpose(dto.getPurpose());

                tcsSlowdownRepository.save(entity);
                savedList.add(entity);
            }

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

    private TCSSlowdownDTO toDTO(TCSSlowdown entity) {
        return TCSSlowdownDTO.builder()
            .id(entity.getId() != null ? entity.getId().toString() : null)
            .durationInDays(entity.getTentativeDurationInDays())
            .throughputDuringSlowdown(entity.getThroughputDuringSlowdown())
            .throughputUOM(entity.getThroughputUOM())
            .startDate(entity.getStartDate())
            .purpose(entity.getPurpose())
            .build();
    }

    @Override
    public AOPMessageVM delete(UUID id) {
        
     AOPMessageVM  aopMessageVM = new AOPMessageVM();

      try {
    
          tcsSlowdownRepository.deleteById(id);
          aopMessageVM.setCode(200);
          aopMessageVM.setMessage("Data deleted successfully");
          return aopMessageVM;

      } catch (Exception ex) {
        throw new RuntimeException("Failed to delete data", ex);
      }

      

            
    }
}



