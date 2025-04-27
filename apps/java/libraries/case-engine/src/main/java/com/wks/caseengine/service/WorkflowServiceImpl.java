package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import com.wks.caseengine.entity.BusinessDemand;
import com.wks.caseengine.entity.Workflow;
import com.wks.caseengine.entity.WorkflowMaster;
import com.wks.caseengine.entity.WorkflowStepsMaster;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.process.instance.ProcessInstanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Repository;
import java.lang.reflect.Field;
import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.service.CaseInstanceService;
import com.wks.caseengine.dto.BusinessDemandDataDTO;
import com.wks.caseengine.dto.WorkflowDTO;
import com.wks.caseengine.dto.WorkflowMasterDTO;
import com.wks.caseengine.dto.WorkflowPageDTO;
import com.wks.caseengine.dto.WorkflowStepsMasterDTO;
import com.wks.caseengine.dto.WorkflowSubmitDTO;
import com.wks.caseengine.dto.WorkflowYearDTO;
import com.wks.caseengine.repository.BusinessDemandDataRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.WorkflowMasterRepository;
import com.wks.caseengine.repository.WorkflowRepository;
import com.wks.caseengine.repository.WorkflowStepsMasterRepository;
import com.wks.caseengine.rest.entity.OwnerDetails;
import com.wks.caseengine.tasks.TaskService;

@Service
public class WorkflowServiceImpl implements WorkflowService {

	@Autowired
	private WorkflowRepository workflowRepository;

	@Autowired
	private CaseInstanceService caseInstanceService;

	@Autowired
	private WorkflowMasterRepository workflowMasterRepository;

	@Autowired
	private WorkflowStepsMasterRepository workflowStepsMasterRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private ProcessInstanceService processInstanceService;

	@Autowired
	private TaskService taskService;

	@Override
	public WorkflowPageDTO getCaseId(String year, String plantId, String siteId, String verticalId) {
		try {
			WorkflowPageDTO workflowPageDTO = new WorkflowPageDTO();
			List<Workflow> list = workflowRepository.findAllByYearAndPlantFKIdAndSiteFKIdAndVerticalFKId(year,
					UUID.fromString(plantId), UUID.fromString(siteId), UUID.fromString(verticalId));

			List<WorkflowDTO> dtoList = new ArrayList<>();
			for (Workflow workflow : list) {
				WorkflowDTO dto = new WorkflowDTO();
				dto.setId(workflow.getId().toString());
				dto.setCaseDefId(workflow.getCaseDefId());
				List<Task> tasks = new ArrayList<>();
				if(workflow.getCaseId()!=null){
					//could not get tasks while submitting the aop report due to transactional policies. Hence writing here
					List<String> rolesList = extractRoles();
					System.out.println("processInsatance Id "+workflow.getProcessInstanceId());
					if(workflow.getProcessInstanceId()==null){
					    while(tasks.size()==0){
							System.out.println("in while loop");
							Thread.sleep(2000); 
							tasks = taskService.find(Optional.ofNullable(workflow.getCaseId()));
							 
						}
						workflow.setProcessInstanceId(tasks.get(0).getProcessInstanceId());
					}else{
						tasks = taskService.find(Optional.ofNullable(workflow.getCaseId()));
					}
					
					
					
					for(Task task :tasks){
                        for(String role: rolesList){
							workflowPageDTO.setRole(role);
							System.out.println("roles "+role + "assignee "+task.getAssignee() );
							if(task.getAssignee().equalsIgnoreCase(role)){
								workflowPageDTO.setTaskId(task.getId());
								workflowPageDTO.setRole(role);
								
								break;
							}
						}
					}
					
					workflowRepository.save(workflow);
				}
				dto.setCaseId(workflow.getCaseId());
				dto.setPlantFkId(workflow.getPlantFKId().toString());
				dto.setVerticalFKId(workflow.getVerticalFKId().toString());
				dto.setSiteFKId(workflow.getSiteFKId().toString());
				dto.setYear(workflow.getYear());
				dto.setIsDeleted(workflow.getIsDeleted());
				dto.setProcessInstanceId(workflow.getProcessInstanceId());
				dtoList.add(dto);
			}

			workflowPageDTO.setWorkflowList(dtoList);
			List<WorkflowMaster> wmlist = workflowMasterRepository.findAllByVerticalFKId(UUID.fromString(verticalId));

			if (wmlist.size() > 0) {
				WorkflowMasterDTO wmDTO = new WorkflowMasterDTO();
				wmDTO.setId(wmlist.get(0).getId().toString());
				wmDTO.setCasedefId(wmlist.get(0).getCaseDefId());
				wmDTO.setVerticalFKId(wmlist.get(0).getVerticalFKId().toString());
				wmDTO.setWorkflowId(wmlist.get(0).getWorkflowId());

				List<Object[]> wsmlist = workflowStepsMasterRepository
						.findAllByWorkflowMasterFKId(wmlist.get(0).getId());

				List<WorkflowStepsMasterDTO> steps = new ArrayList<>();
				for (Object[] wsm : wsmlist) {
					WorkflowStepsMasterDTO dto = new WorkflowStepsMasterDTO();
					dto.setId(wsm[0] != null ? wsm[0].toString() : null);
					dto.setName(wsm[1] != null ? wsm[1].toString() : null);
					dto.setDisplayName(wsm[2] != null ? wsm[2].toString() : null);
					dto.setSequence(wsm[3] != null ? Integer.parseInt(wsm[3].toString()) : null);
					System.out.println("boolean");
					System.out.println(wsm[4]);
					dto.setIsRemarksDisabled(wsm[4] != null ? Boolean.parseBoolean(wsm[4].toString()) : false);
					dto.setWorkflowMasterFKId(wsm[5] != null ? wsm[5].toString() : null);
					steps.add(dto);
				}
				if ((dtoList == null || dtoList.isEmpty())) {
					if (steps.size() > 0) {
						steps.get(0).setStatus("inprogress");
					}
				} else {
					List<ActivityInstance> actiList = processInstanceService
							.getActivityInstances(dtoList.get(0).getProcessInstanceId());
					if (actiList != null && actiList.size() > 0) {
						String status = actiList.get(0).getActivityId().split("-")[0];
						workflowPageDTO.setStatus(status);
						for (WorkflowStepsMasterDTO dto : steps) {
							if (dto.getName().equalsIgnoreCase(status)) {
								System.out.println("in in progress status" + status);
								dto.setStatus("inprogress");
							}
						}
						steps = updateStatuses(steps);
					}
				}

				wmDTO.setSteps(steps);
				workflowPageDTO.setWorkflowMasterDTO(wmDTO);
			}

			return workflowPageDTO;
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
			workFlow.setProcessInstanceId(workflowDTO.getProcessInstanceId());
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
	public Map<String, Object> getWorkFlow(String plantId, String year) {
		Map<String, Object> map = new HashMap<>();

		try {
			List<Object[]> results = getData(plantId, year);

			List<WorkflowYearDTO> workflowList = new ArrayList<>();
			for (Object[] row : results) {
				WorkflowYearDTO dto = new WorkflowYearDTO();
				dto.setParticulates(row[0] != null ? row[0].toString() : null);
				dto.setUom(row[1] != null ? row[1].toString() : null);
				dto.setFy202425AOP(row[2] != null ? row[2].toString() : null);
				dto.setFy202425Actual(row[3] != null ? row[3].toString() : null);
				dto.setFy202526AOP(row[4] != null ? row[4].toString() : null);
				dto.setRemark(row[5] != null ? row[5].toString() : null);
				workflowList.add(dto);
			}
			List<String> headers = getHeaders(plantId, year);
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

	@Override
	public Map<String, Object> getProductionAOPWorkflowData(String plantId, String year) {
		Map<String, Object> map = new HashMap<>();

		try {
			List<Object[]> results = getProductionWorkflowData(plantId, year);

			List<WorkflowYearDTO> workflowList = new ArrayList<>();
			for (Object[] row : results) {
				WorkflowYearDTO dto = new WorkflowYearDTO();
				dto.setParticulates(row[0] != null ? row[0].toString() : null);
				dto.setUom(row[1] != null ? row[1].toString() : null);
				dto.setFy202425AOP(row[2] != null ? row[2].toString() : null);
				dto.setFy202425Actual(row[3] != null ? row[3].toString() : null);
				dto.setFy202526AOP(row[4] != null ? row[4].toString() : null);
				dto.setRemark(row[5] != null ? row[5].toString() : null);
				workflowList.add(dto);
			}
			List<String> headers = getProductionWorkflowHeaders(plantId, year);
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

	public List<Object[]> getProductionWorkflowData(String plantId, String aopYear) {
		try {
			// Stored procedure name
			String procedureName = "GetAnnualProductionCost";

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

	public List<String> getProductionWorkflowHeaders(String plantId, String aopYear) {
		List<String> headers = new ArrayList<>();

		try (Connection conn = dataSource.getConnection();
				CallableStatement stmt = conn.prepareCall("{call GetAnnualProductionCost(?, ?)}")) {

			stmt.setString(1, plantId);
			stmt.setString(2, aopYear);

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

	public List<WorkflowStepsMasterDTO> updateStatuses(List<WorkflowStepsMasterDTO> items) {
		AtomicBoolean inProgressFound = new AtomicBoolean(false);

		// Looping with index-like logic via stream
		IntStream.range(0, items.size())
				.forEach(i -> {
					WorkflowStepsMasterDTO item = items.get(i);
					System.out.println("getstatus"+item.getStatus());
					if ("inprogress".equalsIgnoreCase(item.getStatus())) {
						inProgressFound.set(true);
					}
					if (!inProgressFound.get() && !"completed".equalsIgnoreCase(item.getStatus())) {
						item.setStatus("completed");
					}
				});
		return items;

	}


        @Transactional
		@Override
		public WorkflowDTO submitWorkflow(WorkflowSubmitDTO workflowSubmitDTO) {
			CaseInstance caseInstance = caseInstanceService.startWithValues(workflowSubmitDTO.getCaseInstance());
			System.out.println("case created "+ caseInstance.getBusinessKey());
            
			//System.out.println("tasks found2" + tasks2.size());
			System.out.println("tasks completed");
			workflowSubmitDTO.getWorkflowDTO().setCaseId(caseInstance.getBusinessKey());
			return saveWorkFlow(workflowSubmitDTO.getWorkflowDTO());
			//return workflowSubmitDTO.getWorkflowDTO();
		}


        @Override
		public void completeTaskWithComment(WorkflowSubmitDTO workflowSubmitDTO){

			taskService.complete(workflowSubmitDTO.getTaskId(),workflowSubmitDTO.getVariables());
            caseInstanceService.saveComment(workflowSubmitDTO.getWorkflowDTO().getCaseId(), workflowSubmitDTO.getCaseComment());
	}


	public static List<String> extractRoles() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication instanceof JwtAuthenticationToken) {
		    JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
		    Jwt jwt = jwtAuth.getToken();
		    
		    String userId = jwt.getClaimAsString("sub"); // or "preferred_username"
		    Map<String, Object> claims = jwt.getClaims();
		    
		    System.out.println("userId: " + userId);
		    System.out.println("Claims: " + claims);
		    
		    claims.entrySet().stream()
		    .forEach(entry -> System.out.println(entry.getKey() + " : " + entry.getValue()));
		
		
		Object realmAccessObj = claims.get("realm_access");




        if (realmAccessObj instanceof Map<?, ?>) {
            Map<?, ?> realmAccessMap = (Map<?, ?>) realmAccessObj;
            Object rolesObj = realmAccessMap.get("roles");

            if (rolesObj instanceof List<?>) {
                // Safe cast with filtering
                List<?> rawList = (List<?>) rolesObj;
                return rawList.stream()
                        .filter(item -> item instanceof String)
                        .map(String.class::cast)
                        .toList();
            }
        }
	}

        return Collections.emptyList(); // Return empty list if roles not found
    }

}
