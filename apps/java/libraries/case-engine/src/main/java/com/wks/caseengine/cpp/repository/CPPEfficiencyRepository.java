package com.wks.caseengine.cpp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.cpp.entity.CPPEfficiency;

@Repository
@Transactional
public interface CPPEfficiencyRepository extends JpaRepository<CPPEfficiency, UUID> {

    List<CPPEfficiency> findByCppPlantFkIdInAndAopYearOrderByAssetName(
            List<UUID> plantIds, String aopYear);

    @Modifying
    @Transactional
    @Query(value =
        "UPDATE dbo.CPP_Efficiency SET " +
        "  Asset_FK_Id = :assetFkId, " +
        "  AssetName   = :assetName, " +
        "  UOM         = :uom, " +
        "  Apr = :apr, May = :may, Jun = :jun, Jul = :jul, " +
        "  Aug = :aug, Sep = :sep, Oct = :oct, Nov = :nov, " +
        "  Dec = :dec, Jan = :jan, Feb = :feb, Mar = :mar, " +
        "  Remarks     = :remarks, " +
        "  UpdatedDate = GETDATE() " +
        "WHERE Id = :id",
        nativeQuery = true)
    int updateEfficiency(
            @Param("id")        UUID id,
            @Param("assetFkId") UUID assetFkId,
            @Param("assetName") String assetName,
            @Param("uom")       String uom,
            @Param("apr")       Double apr,
            @Param("may")       Double may,
            @Param("jun")       Double jun,
            @Param("jul")       Double jul,
            @Param("aug")       Double aug,
            @Param("sep")       Double sep,
            @Param("oct")       Double oct,
            @Param("nov")       Double nov,
            @Param("dec")       Double dec,
            @Param("jan")       Double jan,
            @Param("feb")       Double feb,
            @Param("mar")       Double mar,
            @Param("remarks")   String remarks);
}
