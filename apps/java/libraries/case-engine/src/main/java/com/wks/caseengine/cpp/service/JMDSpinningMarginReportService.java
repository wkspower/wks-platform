package com.wks.caseengine.cpp.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.wks.caseengine.message.vm.AOPMessageVM;

/**
 * Read-only Spinning Margin Maintained report service.
 *
 * <p>Provides GET and Excel export for the
 * {@code dbo.CPPMonthlySpinningMarginMaintained} table, filtered by AOP year
 * and optionally by plant(s).</p>
 */
public interface JMDSpinningMarginReportService {

    /**
     * Fetch Spinning Margin Maintained rows for the given AOP year.
     *
     * @param plantIds optional; when null or empty, all rows for the year are
     *                 returned (including rows common to all plants)
     */
    AOPMessageVM getSpinningMarginReport(List<UUID> plantIds, String aopYear);

    /**
     * Export the same Spinning Margin Maintained data to an Excel workbook.
     */
    byte[] exportSpinningMarginReport(List<UUID> plantIds, String aopYear) throws IOException;
}
