package com.wks.caseengine.service;
import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import java.util.UUID;
import com.wks.caseengine.entity.BusinessDemand;
import com.wks.caseengine.entity.Workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.BusinessDemandDataDTO;
import com.wks.caseengine.dto.WorkflowDTO;
import com.wks.caseengine.repository.BusinessDemandDataRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.WorkflowRepository;

@Service
public class WorkflowServiceImpl implements WorkflowService{

    
    @Autowired
	private WorkflowRepository workflowRepository;

    @Override
    public List<WorkflowDTO> getCaseId(String year, String plantId, String siteId, String verticalId) {
         
       List<Workflow> list = workflowRepository.findAllByYearAndPlantFKIdAndSiteFKIdAndVerticalFKId(year, UUID.fromString(plantId), UUID.fromString(siteId), UUID.fromString(verticalId));

       List<WorkflowDTO> dtoList = new ArrayList<>();
       for(Workflow workflow: list){
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
    }

    @Override
    public WorkflowDTO saveWorkFlow(WorkflowDTO workflowDTO) {
          Workflow workFlow = new Workflow();
          workFlow.setCaseDefId(workflowDTO.getCaseDefId());
          workFlow.setCaseId(workflowDTO.getCaseId());
          workFlow.setPlantFKId(UUID.fromString( workflowDTO.getPlantFkId()));
          workFlow.setSiteFKId(UUID.fromString( workflowDTO.getSiteFKId()));
          workFlow.setVerticalFKId(UUID.fromString( workflowDTO.getVerticalFKId()));
          workFlow.setYear(workflowDTO.getYear());
          workflowRepository.save(workFlow);
          if(workFlow.getId()!=null){
            workflowDTO.setId(workFlow.getId().toString());
          }
          return workflowDTO;

    }

    
}
