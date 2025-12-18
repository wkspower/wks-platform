package com.wks.caseengine.service;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.TCSUnitCapacityDTO;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.TCSUnitCapacity;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.TCSUnitCapacityRepository;
import com.wks.caseengine.repository.VerticalsRepository;

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
    public Map<String, Object> getAll(String plantId, String aopYear) {
        Map<String, Object> map = new HashMap<>();

        try {
            List<Object[]> results = getData(plantId, aopYear);
            List<TCSUnitCapacityDTO> resultsList = new ArrayList<>();
            // values mapping
            for (Object[] row : results) {
                TCSUnitCapacityDTO dto = new TCSUnitCapacityDTO();
                dto.setId(row[0] != null ? row[0].toString() : null);
                dto.setParticulates(row[1] != null ? row[1].toString() : null);
                dto.setUom(row[2] != null ? row[2].toString() : null);
                dto.setKbpsd(row[3] != null ? Double.parseDouble(row[3].toString()) : null);
                dto.setRemark(row[4] != null ? row[4].toString() : null);
                resultsList.add(dto);
            }
            map.put("results", resultsList);

            // headers mapping
            List<String> headers = getHeaders(plantId, aopYear);
            map.put("headers", headers);

            List<String> keys = new ArrayList<>();
            // keys mapping
            for (Field field : TCSUnitCapacityDTO.class.getDeclaredFields()) {
                keys.add(field.getName());
            }
            map.put("keys", keys);

            return map;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data", e);
        }
    }

    private List<Object[]> getData(String plantId, String aopYear) {
        try {
            Plants plant = plantsRepository
                .findById(UUID.fromString(plantId))
                .orElseThrow(() -> new RuntimeException("Plant not found for ID: " + plantId));
            Verticals vertical = verticalRepository
                .findById(plant.getVerticalFKId())
                .orElseThrow(() -> new RuntimeException("Vertical not found for ID: " + plant.getVerticalFKId()));
            String verticalName = vertical.getName();
            Sites site = siteRepository.findById(plant.getSiteFkId()).get();
            
            // Stored Procedure name
            String procedureName = "GetTcsUnitCapacity";
            if (!"MEG".equalsIgnoreCase(verticalName)) {
                procedureName = verticalName + "_" + site.getName() + "_GetTcsUnitCapacity";
            }

            // Prepare native SQL call with parameters
            String sql = "EXEC " + procedureName + " @plantId = :plantId, @aopYear = :aopYear";

            // Call the stored procedure
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("plantId", plantId);
            query.setParameter("aopYear", aopYear);

            return query.getResultList();
        } catch (IllegalArgumentException e) {
            throw new RestInvalidArgumentException("Invalid UUID format", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data", e);
        }
    }

    private List<String> getHeaders(String plantId, String year)
    {
        List<String> headers = new ArrayList<>();

        Plants plant = plantsRepository
            .findById(UUID.fromString(plantId))
            .orElseThrow(() -> new RuntimeException("Plant not found for ID: " + plantId));
        Verticals vertical = verticalRepository
            .findById(plant.getVerticalFKId())
            .orElseThrow(() -> new RuntimeException("Vertical not found for ID: " + plant.getVerticalFKId()));
        
        String verticalName = vertical.getName();
        Sites site = siteRepository
            .findById(plant.getSiteFkId())
            .get();
        String procedureName = "GetTcsUnitCapacity";
        if (!"MEG".equalsIgnoreCase(verticalName)) {
            procedureName = verticalName + "_" + site.getName() + "_GetTcsUnitCapacity";
        }

        String callableSql = "{call " + procedureName + "(?, ?)}";

		try (
            Connection conn = dataSource.getConnection();
			CallableStatement stmt = conn.prepareCall(callableSql)) {

			stmt.setString(1, plantId);
			stmt.setString(2, year);

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

					for (int i = 1; i <= columnCount; i++) {
						headers.add(metaData.getColumnLabel(i));
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
        List<TCSUnitCapacityDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            throw new RestInvalidArgumentException("Payload cannot be empty", null);
        }

        try {
            List<TCSUnitCapacity> savedList = new ArrayList<>();
            for (TCSUnitCapacityDTO dto : dtoList) {
                if (dto.getUom() == null || dto.getUom().isBlank()) {
                    throw new RestInvalidArgumentException("UOM is required", null);
                }

                if (dto.getParticulates() == null || dto.getParticulates().isBlank()) {
                    throw new RestInvalidArgumentException("Particulates is required", null);
                }
                
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
                entity.setParticulates(dto.getParticulates());
                entity.setUom(dto.getUom());
                entity.setKbpsd(dto.getKbpsd());
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
            .particulates(entity.getParticulates())
            .uom(entity.getUom())
            .kbpsd(entity.getKbpsd())
            .remark(entity.getRemark())
            .build();
    }
}
