package com.wks.caseengine.repository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.WorkflowMaster;

@Repository
public interface WorkflowMasterRepository extends JpaRepository<WorkflowMaster, UUID>{

    

    List<WorkflowMaster> findAllByVerticalFKId(UUID verticalId);
    
    


}
