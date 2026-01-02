package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.PCGOutlookDTO;
import com.wks.caseengine.dto.PCGOutlookProjection;
import com.wks.caseengine.repository.FinancialYearMonthRepository;
import com.wks.caseengine.repository.PCGOutlookRepository;

@Service
public class PCGOutlookService {
     
    @Autowired
    private PCGOutlookRepository repository;

    @Autowired
    private FinancialYearMonthRepository fyRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<PCGOutlookDTO> getData(UUID siteId, String financialYear) {
       
        List<PCGOutlookDTO> projections = repository.getPcgOutlookBySiteAndFY(siteId, financialYear).stream().map(p -> new PCGOutlookDTO(p.getProduct(), p.getApr(), p.getMay(), p.getJun(), p.getJul(), p.getAug(), p.getSep(), p.getOct(), p.getNov(), p.getDec(), p.getJan(), p.getFeb(), p.getMar())).collect(Collectors.toList());
        return projections;
    }


    public void saveData(List<PCGOutlookDTO> data, String financialYear, UUID siteId) {

        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;

        List<Object[]> fyMonths = fyRepo.findFinancialYearMonths(startYear, endYear);
        Map<Integer, UUID> financialMonthIds = new LinkedHashMap<>();
        for (Object[] row : fyMonths) {
            Integer month = (Integer) row[0];
            UUID id = UUID.fromString((String) row[1]);
            financialMonthIds.put(month, id);
        }

        List<UUID> existingIds = repository.getPcgOutlookFinancialYearMonthIdsBySiteAndFY(siteId, financialMonthIds.values().stream().collect(Collectors.toList()));

        List<Object[]> gasifierAvailabilityupdates = new ArrayList<>();
        List<Object[]> SynGasProductionupdates = new ArrayList<>();
        List<Object[]> gasifierAvailabilityInserts = new ArrayList<>();
        List<Object[]> SynGasProductionInserts = new ArrayList<>();
        for (PCGOutlookDTO dto : data) { 

             if(dto.getApr() != null) {  

                    UUID fymId = financialMonthIds.get(4);

                    if(existingIds.contains(fymId)) {  

                        if ("GasifierAvailability".equals(dto.getProduct())) {
                            gasifierAvailabilityupdates.add(new Object[]{ dto.getApr(), siteId, fymId });
                        } else if ("SynGasProduction".equals(dto.getProduct())) {
                            SynGasProductionupdates.add(new Object[]{ dto.getApr(), siteId, fymId });
                        }
                    }
                    else {
                        if ("GasifierAvailability".equals(dto.getProduct())) {
                            gasifierAvailabilityInserts.add(new Object[]{ dto.getApr(), siteId, fymId });
                        } else if ("SynGasProduction".equals(dto.getProduct())) {
                            SynGasProductionInserts.add(new Object[]{ dto.getApr(), siteId, fymId });
                        }
                    }
                }

             if(dto.getMay() != null) {  

            UUID fymId = financialMonthIds.get(5);

            if(existingIds.contains(fymId)) {  
            if ("GasifierAvailability".equals(dto.getProduct())) {
                    gasifierAvailabilityupdates.add(new Object[]{ dto.getMay(), siteId, fymId });
                } else if ("SynGasProduction".equals(dto.getProduct())) {
                    SynGasProductionupdates.add(new Object[]{ dto.getMay(), siteId, fymId });
                }
            }
            else {
                if ("GasifierAvailability".equals(dto.getProduct())) {
                    gasifierAvailabilityInserts.add(new Object[]{ dto.getMay(), siteId, fymId });
                } else if ("SynGasProduction".equals(dto.getProduct())) {
                    SynGasProductionInserts.add(new Object[]{ dto.getMay(), siteId, fymId });
                }
            }
        }
        if(dto.getJun() != null) {  
            UUID fymId = financialMonthIds.get(6);

            if(existingIds.contains(fymId)) {  
                if ("GasifierAvailability".equals(dto.getProduct())) {
                    gasifierAvailabilityupdates.add(new Object[]{ dto.getJun(), siteId, fymId });
                } else if ("SynGasProduction".equals(dto.getProduct())) {
                    SynGasProductionupdates.add(new Object[]{ dto.getJun(), siteId, fymId });
                }
            }
            else {
                if ("GasifierAvailability".equals(dto.getProduct())) {
                    gasifierAvailabilityInserts.add(new Object[]{ dto.getJun(), siteId, fymId });
                } else if ("SynGasProduction".equals(dto.getProduct())) {
                    SynGasProductionInserts.add(new Object[]{ dto.getJun(), siteId, fymId });
                }
            }
        }
        if(dto.getJul() != null) {  
            UUID fymId = financialMonthIds.get(7);
            if(existingIds.contains(fymId)) {  
                if ("GasifierAvailability".equals(dto.getProduct())) {
                    gasifierAvailabilityupdates.add(new Object[]{ dto.getJul(), siteId, fymId });
                } else if ("SynGasProduction".equals(dto.getProduct())) {
                    SynGasProductionupdates.add(new Object[]{ dto.getJul(), siteId, fymId });
                }
            }
            else {
                if ("GasifierAvailability".equals(dto.getProduct())) {
                    gasifierAvailabilityInserts.add(new Object[]{ dto.getOct(), siteId, fymId });
                } else if ("SynGasProduction".equals(dto.getProduct())) {
                    SynGasProductionInserts.add(new Object[]{ dto.getOct(), siteId, fymId });
                }
            }
        }
        if(dto.getNov() != null) {  
            UUID fymId = financialMonthIds.get(11);
            if(existingIds.contains(fymId)) {  
                if ("GasifierAvailability".equals(dto.getProduct())) {
                    gasifierAvailabilityupdates.add(new Object[]{ dto.getNov(), siteId, fymId });
                } else if ("SynGasProduction".equals(dto.getProduct())) {
                    SynGasProductionupdates.add(new Object[]{ dto.getNov(), siteId, fymId });
                }
            }
            else {
                if ("GasifierAvailability".equals(dto.getProduct())) {
                    gasifierAvailabilityInserts.add(new Object[]{ dto.getNov(), siteId, fymId });
                } else if ("SynGasProduction".equals(dto.getProduct())) {
                    SynGasProductionInserts.add(new Object[]{ dto.getNov(), siteId, fymId });
                }
            }
        }
        if(dto.getDec() != null) {  
            UUID fymId = financialMonthIds.get(12);
            if(existingIds.contains(fymId)) {  
                if ("GasifierAvailability".equals(dto.getProduct())) {
                    gasifierAvailabilityupdates.add(new Object[]{ dto.getDec(), siteId, fymId });
                } else if ("SynGasProduction".equals(dto.getProduct())) {
                    SynGasProductionupdates.add(new Object[]{ dto.getDec(), siteId, fymId });
                }
            }
            else {
                if ("GasifierAvailability".equals(dto.getProduct())) {
                    gasifierAvailabilityInserts.add(new Object[]{ dto.getDec(), siteId, fymId });
                } else if ("SynGasProduction".equals(dto.getProduct())) {
                    SynGasProductionInserts.add(new Object[]{ dto.getDec(), siteId, fymId });
                }
            }
        }
        if(dto.getJan() != null) {  
            UUID fymId = financialMonthIds.get(1);
            if(existingIds.contains(fymId)) {  
                if ("GasifierAvailability".equals(dto.getProduct())) {
                    gasifierAvailabilityupdates.add(new Object[]{ dto.getJan(), siteId, fymId });
                } else if ("SynGasProduction".equals(dto.getProduct())) {
                    SynGasProductionupdates.add(new Object[]{ dto.getJan(), siteId, fymId });
                }
            }
            else {
                if ("GasifierAvailability".equals(dto.getProduct())) {
                    gasifierAvailabilityInserts.add(new Object[]{ dto.getJan(), siteId, fymId });
                } else if ("SynGasProduction".equals(dto.getProduct())) {
                    SynGasProductionInserts.add(new Object[]{ dto.getJan(), siteId, fymId });
                }
            }
        }
        if(dto.getFeb() != null) {  
            UUID fymId = financialMonthIds.get(2);
            if(existingIds.contains(fymId)) {  
                if ("GasifierAvailability".equals(dto.getProduct())) {
                    gasifierAvailabilityupdates.add(new Object[]{ dto.getFeb(), siteId, fymId });
                } else if ("SynGasProduction".equals(dto.getProduct())) {
                    SynGasProductionupdates.add(new Object[]{ dto.getFeb(), siteId, fymId });
                }
            }
            else {
                if ("GasifierAvailability".equals(dto.getProduct())) {
                    gasifierAvailabilityInserts.add(new Object[]{ dto.getFeb(), siteId, fymId });
                } else if ("SynGasProduction".equals(dto.getProduct())) {
                    SynGasProductionInserts.add(new Object[]{ dto.getFeb(), siteId, fymId });
                }
            }
        }
        if(dto.getMar() != null) {  
            UUID fymId = financialMonthIds.get(3);
            if(existingIds.contains(fymId)) {  
                if ("GasifierAvailability".equals(dto.getProduct())) {
                    gasifierAvailabilityupdates.add(new Object[]{ dto.getMar(), siteId, fymId });
                } else if ("SynGasProduction".equals(dto.getProduct())) {
                    SynGasProductionupdates.add(new Object[]{ dto.getMar(), siteId, fymId });
                }
            }
            else {
                if ("GasifierAvailability".equals(dto.getProduct())) {
                    gasifierAvailabilityInserts.add(new Object[]{ dto.getMar(), siteId, fymId });
                } else if ("SynGasProduction".equals(dto.getProduct())) {
                    SynGasProductionInserts.add(new Object[]{ dto.getMar(), siteId, fymId });
                }
            }
        }

   
        }

        if(!gasifierAvailabilityupdates.isEmpty()) {  

            String sql = "Update TCS_PCGOutlook set GasifierAvailability = ? where Site_FK_Id = ? and FinancialYearMonthId = ?";
            jdbcTemplate.batchUpdate(sql, gasifierAvailabilityupdates);
         }
         if(!SynGasProductionupdates.isEmpty()) {  
            String sql = "Update TCS_PCGOutlook set SynGasProduction = ? where Site_FK_Id = ? and FinancialYearMonthId = ?";
            jdbcTemplate.batchUpdate(sql, SynGasProductionupdates);
         
         }   
            if(!gasifierAvailabilityInserts.isEmpty()) {  
                String sql = "Insert into TCS_PCGOutlook (Id, GasifierAvailability, Site_FK_Id, FinancialYearMonthId) values (NEWID(), ?, ?, ?)";
                jdbcTemplate.batchUpdate(sql, gasifierAvailabilityInserts);
            }
            if(!SynGasProductionInserts.isEmpty()) {  
                String sql = "Insert into TCS_PCGOutlook (Id, SynGasProduction, Site_FK_Id, FinancialYearMonthId) values (NEWID(), ?, ?, ?)";
                jdbcTemplate.batchUpdate(sql, SynGasProductionInserts);
            }


    }
}
