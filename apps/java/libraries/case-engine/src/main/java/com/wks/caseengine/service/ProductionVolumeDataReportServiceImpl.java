package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class ProductionVolumeDataReportServiceImpl implements ProductionVolumeDataReportService{
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private PlantsRepository plantsRepository;

	@Override
	public AOPMessageVM getReportForProductionVolumnData(String plantId, String year, String type, String filter) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> productionVolumeReportList = new ArrayList<>();
		List<Object[]> obj= getProductionVolumnDataReport(plantId,year,type,filter);
		
		for(Object[] row : obj) {
			Map<String, Object> map = new HashMap<>();
			map.put("sno", row[0]);
            map.put("item", row[1]);
            map.put("unit", row[2]);
            map.put("part1Budget", row[3]);
            map.put("part1Actual", row[4]);
            map.put("part2Budget", row[5]);
            map.put("part1VarBudgetMT", row[6]);
            map.put("part2VarBudgetPct", row[7]);
            map.put("varActualMT", row[8]);
            map.put("varActualPct", row[9]);
            map.put("remarks", row[10]);
            productionVolumeReportList.add(map); // Add the map to the list here

		}
		aopMessageVM.setCode(200);
        aopMessageVM.setMessage("Data fetched successfully");
        aopMessageVM.setData(productionVolumeReportList);
        return aopMessageVM;
	}
	
	public List<Object[]> getProductionVolumnDataReport(String plantId, String year, String type, String filter) {
	    try {
	    	String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			String storedProcedure = verticalName + "_HMD_ProductionVolumeReport";
	        String sql = "EXEC " + storedProcedure +
	                     " @plantId = :plantId, @year = :year, @type = :type, @filter = :filter";

	        Query query = entityManager.createNativeQuery(sql);

	        query.setParameter("plantId", plantId);
	        query.setParameter("year", year);
	        query.setParameter("type", type);
	        query.setParameter("filter", filter);

	        return query.getResultList();
	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid UUID format ", e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}


}
