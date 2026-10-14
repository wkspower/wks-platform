package com.wks.caseengine.cpp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.cpp.entity.CPPFuelRatio;

@Repository
@Transactional
public interface CPPFuelRatioRepository extends JpaRepository<CPPFuelRatio, UUID> {

    List<CPPFuelRatio> findByCppPlantFkIdInAndAopYearOrderByFuelName(
            List<UUID> plantIds, String aopYear);

    @Modifying
    @Transactional
    @Query(value =
        "UPDATE dbo.CPP_FuelRatio SET " +
        "  Fuel_FK_Id     = :fuelFkId, " +
        "  FuelName       = :fuelName, " +
        "  GCV_Apr = :gcvApr, GCV_May = :gcvMay, GCV_Jun = :gcvJun, " +
        "  GCV_Jul = :gcvJul, GCV_Aug = :gcvAug, GCV_Sep = :gcvSep, " +
        "  GCV_Oct = :gcvOct, GCV_Nov = :gcvNov, GCV_Dec = :gcvDec, " +
        "  GCV_Jan = :gcvJan, GCV_Feb = :gcvFeb, GCV_Mar = :gcvMar, " +
        "  PercentageByWt_Apr = :percentageByWtApr, PercentageByWt_May = :percentageByWtMay, " +
        "  PercentageByWt_Jun = :percentageByWtJun, PercentageByWt_Jul = :percentageByWtJul, " +
        "  PercentageByWt_Aug = :percentageByWtAug, PercentageByWt_Sep = :percentageByWtSep, " +
        "  PercentageByWt_Oct = :percentageByWtOct, PercentageByWt_Nov = :percentageByWtNov, " +
        "  PercentageByWt_Dec = :percentageByWtDec, PercentageByWt_Jan = :percentageByWtJan, " +
        "  PercentageByWt_Feb = :percentageByWtFeb, PercentageByWt_Mar = :percentageByWtMar, " +
        "  Remarks        = :remarks, " +
        "  UpdatedDate    = GETDATE() " +
        "WHERE Id = :id",
        nativeQuery = true)
    int updateFuelRatio(
            @Param("id")             UUID id,
            @Param("fuelFkId")       UUID fuelFkId,
            @Param("fuelName")       String fuelName,
            @Param("gcvApr")         Double gcvApr,
            @Param("gcvMay")         Double gcvMay,
            @Param("gcvJun")         Double gcvJun,
            @Param("gcvJul")         Double gcvJul,
            @Param("gcvAug")         Double gcvAug,
            @Param("gcvSep")         Double gcvSep,
            @Param("gcvOct")         Double gcvOct,
            @Param("gcvNov")         Double gcvNov,
            @Param("gcvDec")         Double gcvDec,
            @Param("gcvJan")         Double gcvJan,
            @Param("gcvFeb")         Double gcvFeb,
            @Param("gcvMar")         Double gcvMar,
            @Param("percentageByWtApr")  Double percentageByWtApr,
            @Param("percentageByWtMay")  Double percentageByWtMay,
            @Param("percentageByWtJun")  Double percentageByWtJun,
            @Param("percentageByWtJul")  Double percentageByWtJul,
            @Param("percentageByWtAug")  Double percentageByWtAug,
            @Param("percentageByWtSep")  Double percentageByWtSep,
            @Param("percentageByWtOct")  Double percentageByWtOct,
            @Param("percentageByWtNov")  Double percentageByWtNov,
            @Param("percentageByWtDec")  Double percentageByWtDec,
            @Param("percentageByWtJan")  Double percentageByWtJan,
            @Param("percentageByWtFeb")  Double percentageByWtFeb,
            @Param("percentageByWtMar")  Double percentageByWtMar,
            @Param("remarks")        String remarks);
}
