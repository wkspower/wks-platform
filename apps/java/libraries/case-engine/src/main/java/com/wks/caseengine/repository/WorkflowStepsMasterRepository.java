package com.wks.caseengine.repository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.WorkflowStepsMaster;

@Repository
public interface WorkflowStepsMasterRepository extends JpaRepository<WorkflowStepsMaster, UUID>{

    

    @Query(value = "SELECT Id "+
      ",Name " +
      ",DisplayName "+
      ",Sequence "+
      ",isRemarksDisabled "+
      ",WorkflowMaster_FK_Id "+
      ",CreatedOn "+
      ",ModifiedOn "+
      "FROM dbo.WorkflowStepsMaster "+
      "where WorkflowMaster_FK_Id = :workflowMasterId " +
      "order by Sequence ",
            nativeQuery = true)
      List<Object[]> findAllByWorkflowMasterFKId(@Param("workflowMasterId") 
            UUID workflowMasterId);
    


}
