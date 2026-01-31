package com.wks.caseengine.cpp.serviceimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.cpp.repository.ImportPowerCapacityProjection;
import com.wks.caseengine.cpp.repository.ImportPowerCapacityRepository;
import com.wks.caseengine.cpp.service.ImportPowerCapacityService;
import com.wks.caseengine.dto.ImportPowerCapacityDto;
import com.wks.caseengine.entity.CPPImportPowerCapacity;
import com.wks.caseengine.exception.RestInvalidArgumentException;

@Service
public class ImportPowerCapacityServiceImpl implements ImportPowerCapacityService {

    @Autowired
    private ImportPowerCapacityRepository repository;

    /**
     * Get import power capacity for a financial year
     */
    @Override
    public List<ImportPowerCapacityDto> getImportPowerCapacity(UUID cppPlantId, String financialYear) {
        
        if (financialYear == null || financialYear.isBlank()) {
            throw new RestInvalidArgumentException("FinancialYear is required", null);
        }

        if (!financialYear.matches("^\\d{4}-\\d{2}$")) {
            throw new RestInvalidArgumentException(
                    "Invalid financialYear format. Expected format: YYYY-YY (e.g., 2026-27)", null);
        }

        List<ImportPowerCapacityProjection> projections = repository.getImportPowerCapacity(cppPlantId, financialYear);
        List<ImportPowerCapacityDto> result = new ArrayList<>();

        for (ImportPowerCapacityProjection proj : projections) {
            ImportPowerCapacityDto dto = new ImportPowerCapacityDto();
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
            dto.setUom(proj.getUOM() != null ? proj.getUOM() : "MW");
            dto.setRemarks(proj.getRemarks() != null ? proj.getRemarks() : "");
            dto.setEditable(true);

            result.add(dto);
        }

        return result;
    }

    /**
     * Upsert import power capacity
     */
    @Override
    @Transactional
    public void upsertImportPowerCapacity(List<ImportPowerCapacityDto> dtoList, String financialYear) {
        
        if (financialYear == null || financialYear.isBlank()) {
            throw new RestInvalidArgumentException("FinancialYear is required", null);
        }

        if (!financialYear.matches("^\\d{4}-\\d{2}$")) {
            throw new RestInvalidArgumentException(
                    "Invalid financialYear format. Expected format: YYYY-YY (e.g., 2026-27)", null);
        }

        for (ImportPowerCapacityDto dto : dtoList) {
            if (dto.getSourceId() == null) {
                throw new RestInvalidArgumentException("sourceId is required in ImportPowerCapacityDto", null);
            }

            try {
                Optional<CPPImportPowerCapacity> existingOpt = 
                    repository.findByImportPowerSourceFkIdAndFinancialYear(dto.getSourceId(), financialYear);

                CPPImportPowerCapacity record;
                if (existingOpt.isPresent()) {
                    record = existingOpt.get();
                    record.setUpdatedDate(LocalDateTime.now());
                } else {
                    record = new CPPImportPowerCapacity();
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
                record.setUom(dto.getUom() != null ? dto.getUom() : "MW");
                record.setRemarks(dto.getRemarks() != null ? dto.getRemarks() : "");

                repository.save(record);

            } catch (Exception e) {
                throw new RestInvalidArgumentException("Failed to upsert capacity for source: " + dto.getSourceName(), e);
            }
        }
    }
}
