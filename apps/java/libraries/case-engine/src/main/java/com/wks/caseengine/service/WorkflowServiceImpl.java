package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import java.util.UUID;
import com.wks.caseengine.entity.BusinessDemand;
import com.wks.caseengine.entity.Workflow;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.lang.reflect.Field;
import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.BusinessDemandDataDTO;
import com.wks.caseengine.dto.WorkflowDTO;
import com.wks.caseengine.dto.WorkflowYearDTO;
import com.wks.caseengine.repository.BusinessDemandDataRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.WorkflowRepository;

@Service
public class WorkflowServiceImpl implements WorkflowService {

	@Autowired
	private WorkflowRepository workflowRepository;

	@PersistenceContext
	private EntityManager entityManager;
	
	 @Autowired
	 private DataSource dataSource;


	@Override
	public List<WorkflowDTO> getCaseId(String year, String plantId, String siteId, String verticalId) {
		try {
			List<Workflow> list = workflowRepository.findAllByYearAndPlantFKIdAndSiteFKIdAndVerticalFKId(year,
					UUID.fromString(plantId), UUID.fromString(siteId), UUID.fromString(verticalId));

			List<WorkflowDTO> dtoList = new ArrayList<>();
			for (Workflow workflow : list) {
				WorkflowDTO dto = new WorkflowDTO();
				dto.setId(workflow.getId().toString());
				dto.setCaseDefId(workflow.getCaseDefId());
				dto.setCaseId(workflow.getCaseId());
				dto.setPlantFkId(workflow.getPlantFKId().toString());
				dto.setVerticalFKId(workflow.getVerticalFKId().toString());
				dto.setSiteFKId(workflow.getSiteFKId().toString());
				dto.setYear(workflow.getYear());
				dto.setIsDeleted(workflow.getIsDeleted());
				dtoList.add(dto);

			}
			return dtoList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid data format", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public WorkflowDTO saveWorkFlow(WorkflowDTO workflowDTO) {
		try {
			Workflow workFlow = new Workflow();
			workFlow.setCaseDefId(workflowDTO.getCaseDefId());
			workFlow.setCaseId(workflowDTO.getCaseId());
			workFlow.setPlantFKId(UUID.fromString(workflowDTO.getPlantFkId()));
			workFlow.setSiteFKId(UUID.fromString(workflowDTO.getSiteFKId()));
			workFlow.setVerticalFKId(UUID.fromString(workflowDTO.getVerticalFKId()));
			workFlow.setYear(workflowDTO.getYear());
			workflowRepository.save(workFlow);
			if (workFlow.getId() != null) {
				workflowDTO.setId(workFlow.getId().toString());
			}
			return workflowDTO;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}

	}

	@Override
	public Map<String, Object> getWorkFlow(String plantId,String year) {
		Map<String, Object> map = new HashMap<>();

		try {
			List<Object[]> results = getData(plantId,year);

			List<WorkflowYearDTO> workflowList = new ArrayList<>();
			for (Object[] row : results) {
				WorkflowYearDTO dto = new WorkflowYearDTO();	
				dto.setParticulates(row[0] != null ? row[0].toString() : null);
				dto.setUom(row[1] != null ? row[1].toString() : null);
				dto.setFy202425AOP(row[2] != null ? row[2].toString() : null);
				dto.setFy202425Actual(row[3] != null ? row[3].toString() : null);
				dto.setFy202526AOP(row[4] != null ? row[4].toString() : null);
				dto.setRemarks(row[5] != null ? row[5].toString() : null);

				workflowList.add(dto);
			}
			List<String> headers=getHeaders(plantId,year);
			List<String> keys = new ArrayList<>();
			for (Field field : WorkflowYearDTO.class.getDeclaredFields()) {
			    keys.add(field.getName());
			}
			map.put("headers", headers);
			map.put("keys", keys);
			map.put("results", workflowList);
			return map;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	
	public List<Object[]> getData(String plantId, String aopYear) {
	    try {
	        // Stored procedure name
	        String procedureName = "GetAnnualAOPCost";

	        // Prepare native SQL call with parameters
	        String sql = "EXEC " + procedureName + " @plantId = :plantId, @aopYear = :aopYear";

	        Query query = entityManager.createNativeQuery(sql);

	        // Set parameters
	        query.setParameter("plantId", plantId);
	        query.setParameter("aopYear", aopYear);

	        return query.getResultList();
	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid UUID format ", e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}

	public List<String> getHeaders(String plantId, String aopYear) {
        List<String> headers = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{call GetAnnualAOPCost(?, ?)}")) {

            stmt.setString(1, plantId);
            stmt.setString(2, aopYear);

            boolean hasResultSet = stmt.execute();

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

}
