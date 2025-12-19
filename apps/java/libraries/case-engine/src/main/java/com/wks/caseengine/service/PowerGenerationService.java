package com.wks.caseengine.service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AssetMonthlyOperationalProjection;
import com.wks.caseengine.dto.AssetOperationalResponseDTO;
import com.wks.caseengine.dto.MonthlyHoursDTO;
import com.wks.caseengine.repository.FinancialYearMonthRepository;
import com.wks.caseengine.repository.PowerGenerationRepository;

@Service
public class PowerGenerationService {

    @Autowired
    private PowerGenerationRepository repository;

    @Autowired
    private FinancialYearMonthRepository financialYearMonthRepo;

    public List<AssetOperationalResponseDTO> getAssetOperationalHours(
            UUID cppPlantId,
            String financialYear) {

        List<AssetMonthlyOperationalProjection> data =
                repository.getOperationalHours(cppPlantId, financialYear);

        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;

        List<AssetOperationalResponseDTO> response = new  ArrayList<>();

        for (AssetMonthlyOperationalProjection row : data) {

            Map<String, MonthlyHoursDTO> monthMap = new LinkedHashMap<>();

            monthMap.put("April",     buildMonth(row.getApril(),     startYear, 4));
            monthMap.put("May",       buildMonth(row.getMay(),       startYear, 5));
            monthMap.put("June",      buildMonth(row.getJune(),      startYear, 6));
            monthMap.put("July",      buildMonth(row.getJuly(),      startYear, 7));
            monthMap.put("August",    buildMonth(row.getAugust(),    startYear, 8));
            monthMap.put("September", buildMonth(row.getSeptember(), startYear, 9));
            monthMap.put("October",   buildMonth(row.getOctober(),   startYear,10));
            monthMap.put("November",  buildMonth(row.getNovember(),  startYear,11));
            monthMap.put("December",  buildMonth(row.getDecember(),  startYear,12));

            monthMap.put("January",   buildMonth(row.getJanuary(),   endYear, 1));
            monthMap.put("February",  buildMonth(row.getFebruary(),  endYear, 2));
            monthMap.put("March",     buildMonth(row.getMarch(),     endYear, 3));

          
           
            AssetOperationalResponseDTO dto = new AssetOperationalResponseDTO();
             dto.setAssetName(row.getAssetName());
             dto.setAssetId(row.getAssetId());
             dto.setApril(monthMap.get("April"));
             dto.setMay(monthMap.get("May"));
             dto.setJune(monthMap.get("June"));
             dto.setJuly(monthMap.get("July"));
             dto.setAug(monthMap.get("August"));
             dto.setSep(monthMap.get("September"));
             dto.setOct(monthMap.get("October"));
             dto.setNov(monthMap.get("November"));
             dto.setDec(monthMap.get("December"));
             dto.setJan(monthMap.get("January"));
             dto.setFeb(monthMap.get("February"));
             dto.setMarch(monthMap.get("March"));

            response.add(dto);
        }

        return response;
    }

    public void setAssetOperationalHours( String financialYear, List<AssetOperationalResponseDTO> payload) {
         
            int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;

        for (AssetOperationalResponseDTO asset : payload) {

            saveMonth(asset, asset.getApril(), startYear, 4);
            saveMonth(asset, asset.getMay(), startYear, 5);
            saveMonth(asset, asset.getJune(), startYear, 6);
            saveMonth(asset, asset.getJuly(), startYear, 7);
            saveMonth(asset, asset.getAug(), startYear, 8);
            saveMonth(asset, asset.getSep(), startYear, 9);
            saveMonth(asset, asset.getOct(), startYear, 10);
            saveMonth(asset, asset.getNov(), startYear, 11);
            saveMonth(asset, asset.getDec(), startYear, 12);

            saveMonth(asset, asset.getJan(), endYear, 1);
            saveMonth(asset, asset.getFeb(), endYear, 2);
            saveMonth(asset, asset.getMarch(), endYear, 3);

    }
}


  private void saveMonth(
            AssetOperationalResponseDTO asset,
            MonthlyHoursDTO dto,
            int year,
            int month) {

        if (dto == null) return;

        validateMonth(asset.getAssetName(), dto, year, month);

        UUID financialMonthId = financialYearMonthRepo
                .findFinancialMonthId(year, month);

        if (financialMonthId == null) {
            throw new IllegalArgumentException(
                "FinancialYearMonth not found for " + year + "-" + month
            );
        }

        int updated = repository.updateOperationalHours(
                asset.getAssetId(),
                financialMonthId,
                dto.getNetOperationHrs()
        );

        if (updated == 0) {
            System.out.println(" Inserting operational hours for asset " + asset.getAssetName() + " for " + year + "-" + month);
            repository.insertOperationalHours(
                    asset.getAssetId(),
                    financialMonthId,
                    dto.getNetOperationHrs()
            );
        }
    }


      private void validateMonth(
            String assetName,
            MonthlyHoursDTO dto,
            int year,
            int month) {

        double net = dto.getNetOperationHrs();
        double shutdown = dto.getShutdownHrs();

        YearMonth ym = YearMonth.of(year, month);
        int totalHours = ym.lengthOfMonth() * 24;

        if (Double.compare(net + shutdown, totalHours) != 0) {
            throw new IllegalArgumentException(
                String.format(
                    "Validation failed for Asset [%s], %d-%02d → " +
                    "Net (%s) + Shutdown (%s) ≠ Total (%s)",
                    assetName, year, month, net, shutdown, totalHours
                )
            );
        }
    }

   
    private MonthlyHoursDTO buildMonth(Double netHours, int year, int month) {

        double operationalHours = netHours != null ? netHours : 0;

        YearMonth yearMonth = YearMonth.of(year, month);
        int totalHours = yearMonth.lengthOfMonth() * 24;

        double shutdownHours = totalHours - operationalHours;
        if (shutdownHours < 0) shutdownHours = 0;

        return new MonthlyHoursDTO(operationalHours, shutdownHours);
    }
}