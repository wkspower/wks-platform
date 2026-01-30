package com.wks.caseengine.cpp.serviceimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.cpp.repository.ImportPowerHoursProjection;
import com.wks.caseengine.cpp.repository.ImportPowerHoursRepository;
import com.wks.caseengine.cpp.service.ImportPowerHoursService;
import com.wks.caseengine.dto.ImportPowerHoursDto;
import com.wks.caseengine.entity.CPPImportPowerOperationalHours;
import com.wks.caseengine.exception.RestInvalidArgumentException;

@Service
public class ImportPowerHoursServiceImpl implements ImportPowerHoursService {

    @Autowired
    private ImportPowerHoursRepository repository;

    /**
     * Get import power operational hours for a financial year
     */
    @Override
    public List<ImportPowerHoursDto> getImportPowerOperationalHours(UUID cppPlantId, String financialYear) {
        
        if (financialYear == null || financialYear.isBlank()) {
            throw new RestInvalidArgumentException("FinancialYear is required", null);
        }

        if (!financialYear.matches("^\\d{4}-\\d{2}$")) {
            throw new RestInvalidArgumentException(
                    "Invalid financialYear format. Expected format: YYYY-YY (e.g., 2026-27)", null);
        }

        List<ImportPowerHoursProjection> projections = repository.getImportPowerOperationalHours(cppPlantId, financialYear);
        List<ImportPowerHoursDto> result = new ArrayList<>();

        for (ImportPowerHoursProjection proj : projections) {
            ImportPowerHoursDto dto = new ImportPowerHoursDto();
            dto.setSourceId(proj.getSourceId());
            dto.setSourceName(proj.getSourceName());
            dto.setMaterialCode(proj.getMaterialCode());
            dto.setSapCode(proj.getSAPMaterialCode());
            dto.setUtilityName(proj.getUtilityName());
            dto.setPlantName(proj.getPlantName());
            dto.setApril(proj.getApr());
            dto.setMay(proj.getMay());
            dto.setJune(proj.getJun());
            dto.setJuly(proj.getJul());
            dto.setAugust(proj.getAug());
            dto.setSeptember(proj.getSep());
            dto.setOctober(proj.getOct());
            dto.setNovember(proj.getNov());
            dto.setDecember(proj.getDec());
            dto.setJanuary(proj.getJan());
            dto.setFebruary(proj.getFeb());
            dto.setMarch(proj.getMar());
            dto.setRemarks(proj.getRemarks() != null ? proj.getRemarks() : "");
            dto.setEditable(true);

            result.add(dto);
        }

        return result;
    }

    /**
     * Upsert import power operational hours
     */
    @Override
    @Transactional
    public void upsertImportPowerOperationalHours(List<ImportPowerHoursDto> dtoList, String financialYear) {
        
        if (financialYear == null || financialYear.isBlank()) {
            throw new RestInvalidArgumentException("FinancialYear is required", null);
        }

        if (!financialYear.matches("^\\d{4}-\\d{2}$")) {
            throw new RestInvalidArgumentException(
                    "Invalid financialYear format. Expected format: YYYY-YY (e.g., 2026-27)", null);
        }

        for (ImportPowerHoursDto dto : dtoList) {
            if (dto.getSourceId() == null) {
                throw new RestInvalidArgumentException("sourceId is required in ImportPowerHoursDto", null);
            }

            try {
                Optional<CPPImportPowerOperationalHours> existingOpt = 
                    repository.findByImportPowerSourceFkIdAndFinancialYear(dto.getSourceId(), financialYear);

                CPPImportPowerOperationalHours record;
                if (existingOpt.isPresent()) {
                    record = existingOpt.get();
                    record.setUpdatedDate(LocalDateTime.now());
                } else {
                    record = new CPPImportPowerOperationalHours();
                    record.setImportPowerSourceFkId(dto.getSourceId());
                    record.setFinancialYear(financialYear);
                    record.setCreatedDate(LocalDateTime.now());
                }

                record.setApr(dto.getApril() != null ? dto.getApril() : 0.0);
                record.setMay(dto.getMay() != null ? dto.getMay() : 0.0);
                record.setJun(dto.getJune() != null ? dto.getJune() : 0.0);
                record.setJul(dto.getJuly() != null ? dto.getJuly() : 0.0);
                record.setAug(dto.getAugust() != null ? dto.getAugust() : 0.0);
                record.setSep(dto.getSeptember() != null ? dto.getSeptember() : 0.0);
                record.setOct(dto.getOctober() != null ? dto.getOctober() : 0.0);
                record.setNov(dto.getNovember() != null ? dto.getNovember() : 0.0);
                record.setDec(dto.getDecember() != null ? dto.getDecember() : 0.0);
                record.setJan(dto.getJanuary() != null ? dto.getJanuary() : 0.0);
                record.setFeb(dto.getFebruary() != null ? dto.getFebruary() : 0.0);
                record.setMar(dto.getMarch() != null ? dto.getMarch() : 0.0);
                record.setRemarks(dto.getRemarks() != null ? dto.getRemarks() : "");

                repository.save(record);

            } catch (Exception e) {
                throw new RestInvalidArgumentException("Failed to upsert operational hours for source: " + dto.getSourceName(), e);
            }
        }
    }
}
