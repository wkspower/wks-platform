package com.wks.caseengine.cpp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.DummyEntity;

/**
 * Repository for the Spinning Margin Maintained report.
 *
 * <p>Reads directly from {@code dbo.CPPMonthlySpinningMarginMaintained}.
 * The table stores monthly spinning margin values per MarginType (POWER / STEAM)
 * and Plant. Rows with {@code Plant_FK_Id = NULL} are common to all plants.</p>
 */
@Repository
public interface CPPMonthlySpinningMarginMaintainedRepository
        extends JpaRepository<DummyEntity, Long> {

    /**
     * Fetch all Spinning Margin Maintained rows for an AOP year.
     *
     * <p>Returns every row for the year, including rows whose
     * {@code Plant_FK_Id} is NULL (common to all plants), joined with the
     * Plants table to resolve the plant name.</p>
     *
     * <p>Result row column order (Object[]):
     * <pre>
     *   0: Id, 1: AOPYear, 2: MarginType, 3: Plant_FK_Id,
     *   4: PlantName (from Plants.Name, null for common rows),
     *   5..16: Apr .. Mar
     * </pre></p>
     */
    @Query(value =
        "SELECT m.Id, m.AOPYear, m.MarginType, m.Plant_FK_Id, " +
        "       p.Name AS PlantName, " +
        "       m.Apr, m.May, m.Jun, m.Jul, m.Aug, m.Sep, " +
        "       m.Oct, m.Nov, m.Dec, m.Jan, m.Feb, m.Mar " +
        "FROM dbo.CPPMonthlySpinningMarginMaintained m " +
        "LEFT JOIN dbo.Plants p ON m.Plant_FK_Id = p.Id " +
        "WHERE m.AOPYear = :aopYear " +
        "ORDER BY m.MarginType, p.Name",
        nativeQuery = true)
    List<Object[]> findByAopYear(@Param("aopYear") String aopYear);

    /**
     * Fetch Spinning Margin Maintained rows for a set of plants and an AOP year.
     *
     * <p>Returns only rows whose {@code Plant_FK_Id} is in the given plant list,
     * joined with the Plants table to resolve the plant name.</p>
     *
     * <p>Result row column order (Object[]):
     * <pre>
     *   0: Id, 1: AOPYear, 2: MarginType, 3: Plant_FK_Id,
     *   4: PlantName (from Plants.Name),
     *   5..16: Apr .. Mar
     * </pre></p>
     */
    @Query(value =
        "SELECT m.Id, m.AOPYear, m.MarginType, m.Plant_FK_Id, " +
        "       p.Name AS PlantName, " +
        "       m.Apr, m.May, m.Jun, m.Jul, m.Aug, m.Sep, " +
        "       m.Oct, m.Nov, m.Dec, m.Jan, m.Feb, m.Mar " +
        "FROM dbo.CPPMonthlySpinningMarginMaintained m " +
        "LEFT JOIN dbo.Plants p ON m.Plant_FK_Id = p.Id " +
        "WHERE m.AOPYear = :aopYear " +
        "  AND m.Plant_FK_Id IN (:plantIds) " +
        "ORDER BY m.MarginType, p.Name",
        nativeQuery = true)
    List<Object[]> findByPlantIdsAndAopYear(
            @Param("plantIds") List<UUID> plantIds,
            @Param("aopYear") String aopYear);
}
