package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.wks.caseengine.entity.Sites;

@Repository
public interface SiteRepository extends JpaRepository<Sites, UUID> {

    @Query(value = "select sites.Id, sites.Name, sites.DisplayName, " +
            "plants.Id, plants.Name, plants.DisplayName, plants.Site_FK_Id " +
            "from [RIL.AOP].[dbo].[Sites] sites " +
            "join [RIL.AOP].[dbo].[Plants] plants " +
            "on sites.id = plants.Site_FK_Id", 
            nativeQuery = true)
    List<Object[]> getAllSitesAndPlants();
}
