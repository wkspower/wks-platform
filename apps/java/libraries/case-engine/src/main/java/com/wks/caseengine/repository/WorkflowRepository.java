package com.wks.caseengine.repository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.Workflow;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, UUID>{

    

    List<Workflow> findAllByYearAndPlantFKIdAndSiteFKIdAndVerticalFKId(String year, UUID fromString, UUID fromString2,
            UUID fromString3);
    


}
