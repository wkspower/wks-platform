package com.wks.caseengine.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class BasisReportServiceImpl implements BasisReportService{
	
	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public AOPMessageVM getNormBasisReport(String plantId, String aopYear, String type,Date periodFrom, Date periodTo) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> normBasisList = new ArrayList<>();
		try {
					
		List<Object[]> obj=getReportData( plantId,  aopYear,  type,periodFrom,periodTo);
		for (Object[] row : obj) {
			Map<String, Object> map = new HashMap<>(); // Create a new map for each row

			if (type.equalsIgnoreCase("BEST ACHIEVED NORMS")) {

				map.put("account", row[0]);
				map.put("uom", row[1]);
				map.put("material", row[2]);
				map.put("grade", row[3]);
				map.put("january", row[4]);
				map.put("february", row[5]);
				map.put("march", row[6]);
				map.put("april", row[7]);
				map.put("may", row[8]);
				map.put("june", row[9]);
				map.put("july", row[10]);
				map.put("august", row[11]);
				map.put("september", row[12]);
				map.put("october", row[13]);
				map.put("november", row[14]);
				map.put("december", row[15]);
				normBasisList.add(map); // Add the map to the list here

			}if (type.equalsIgnoreCase("RAW MCU")) {
				map.put("gradeCode", row[0]);
				map.put("gradeMaxCap", row[1]);
				map.put("mcuDate", row[2]);
				map.put("gradeACTProd", row[3]);
				normBasisList.add(map); // Add the map to the list here
			}
			if (type.equalsIgnoreCase("MCU RANGE")) {
				map.put("gradeCode", row[0]);
				map.put("gradeMaxCap", row[1]);
				map.put("year", row[2]);
				map.put("highRange", row[3]);
				map.put("lowRange", row[4]);
				normBasisList.add(map); // Add the map to the list here
			}
			if (type.equalsIgnoreCase("MCU WITHIN RANGE")) {
				map.put("grade", row[0]);
				map.put("gradeMaxCap", row[1]);
				map.put("dateTime", row[2]);
				map.put("gradeACTProd", row[3]);
				map.put("highRange", row[4]);
				map.put("lowRange", row[5]);
				map.put("year", row[6]);
				normBasisList.add(map); // Add the map to the list here
			}
			if (type.equalsIgnoreCase("MIIS NORMS RAW DATA")) {
				map.put("material", row[0]);
				map.put("grade", row[1]);
				map.put("account", row[2]);
				map.put("uom", row[3]);
				map.put("actualQty", row[4]);
				map.put("dateTime", row[5]);
				map.put("contributionType", row[6]);
				normBasisList.add(map); // Add the map to the list here
			}
			if (type.equalsIgnoreCase("CONSECUTIVE DAYS")) {
				map.put("noOfConsecutiveDays", row[0]);
				map.put("grade", row[1]);
				map.put("dateTime", row[2]);
				normBasisList.add(map); // Add the map to the list here
			}
			if (type.equalsIgnoreCase("AVG ANNUAL NORMS")) {
				map.put("account", row[0]);
				map.put("grade", row[1]);
				map.put("material", row[2]);
				map.put("avgFinalNorms", row[3]);
				map.put("year", row[4]);
				normBasisList.add(map); // Add the map to the list here
			}
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("SP Executed successfully");
		aopMessageVM.setData(normBasisList);
		return aopMessageVM;
     
	} catch (Exception e) {
		e.printStackTrace();
		return aopMessageVM;
	}

	}

	@Override
	public AOPMessageVM getNormBasisReportForPE(String plantId, String aopYear, String type,Date periodFrom, Date periodTo) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> normBasisList = new ArrayList<>();
		try {
					
		List<Object[]> obj=getReportDataForPE( plantId,  aopYear,  type);
		for (Object[] row : obj) {
			Map<String, Object> map = new HashMap<>(); // Create a new map for each row

			if (type.equalsIgnoreCase("Production")) {

				map.put("norm", row[0]);
				map.put("particulars", row[1]);
				map.put("april", row[2]);
				map.put("may", row[3]);
				map.put("june", row[4]);
				map.put("july", row[5]);
				map.put("august", row[6]);
				map.put("september", row[7]);
				map.put("october", row[8]);
				map.put("november", row[9]);
				map.put("december", row[10]);
				map.put("january", row[11]);
				map.put("february", row[12]);
				map.put("march", row[13]);
				map.put("total", row[14]);
				
				normBasisList.add(map); // Add the map to the list here

			}	}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("SP Executed successfully");
		aopMessageVM.setData(normBasisList);
		return aopMessageVM;
     
	} catch (Exception e) {
		e.printStackTrace();
		return aopMessageVM;
	}

	}

	public List<Object[]> getReportData(String plantId, String aopYear, String type,Date PeriodFrom, Date PeriodTo){
		
		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		Sites site = siteRepository.findById(plant.getSiteFkId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

		UUID siteId = site.getId();
		UUID verticalId = vertical.getId();
		String storedProcedure = vertical.getName()+"_" +site.getName()+ "_NormsBasisReport";
		String sql = "EXEC " + storedProcedure
                + " @plantId = :plantId,@siteId = :siteId,@verticalId = :verticalId, @aopYear = :aopYear, @Type = :type, @PeriodFrom = :PeriodFrom, @PeriodTo = :PeriodTo";

        Query query = entityManager.createNativeQuery(sql);

        query.setParameter("plantId", plantId);
        query.setParameter("aopYear", aopYear);
        query.setParameter("type", type);
        query.setParameter("siteId", siteId);
        query.setParameter("verticalId", verticalId);
        query.setParameter("PeriodFrom", PeriodFrom);
        query.setParameter("PeriodTo", PeriodTo);

        return query.getResultList();

		
	}

public List<Object[]> getReportDataForPE(String plantId, String aopYear, String reportType){
		
		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

		
		String storedProcedure = vertical.getName()+ "_AnnualCostAOPReport";
		String sql = "EXEC " + storedProcedure
                + " @plantId = :plantId, @aopYear = :aopYear, @reportType = :reportType";

        Query query = entityManager.createNativeQuery(sql);

        query.setParameter("plantId", plantId);
        query.setParameter("aopYear", aopYear);
        query.setParameter("reportType", reportType);
       
       
        return query.getResultList();
	}

}
