package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import com.wks.caseengine.repository.ReportShutdownSlowdownPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.dto.FinishingShutdownConfigDTO;
import com.wks.caseengine.dto.ReportCapexPIOPlanDTO;
import com.wks.caseengine.dto.ShutdownSlowdownPlanDTO;
import com.wks.caseengine.dto.TechnicalAvailabilityDTO;
import com.wks.caseengine.entity.FinishingShutdownConfig;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ReportCapexPIOPlan;
import com.wks.caseengine.entity.ReportShutdownSlowdownPlan;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.TechnicalAvailability;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.FinishingShutdownConfigRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ReportCapexPIOPlanRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.TechnicalAvailabilityRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class FinishingShutdownServiceImpl implements FinishingShutdownService {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private FinishingShutdownConfigRepository finishingShutdownConfigRepository;
	
	@Autowired
	private SiteRepository siteRepository;
	
	@Autowired
	private PlantsRepository plantsRepository;

	@Override
	public AOPMessageVM getFinishingShutdown(String plantId, String year) {
	    try {
	        String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
	        Plants plant = plantsRepository.findById(UUID.fromString(plantId))
	                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

	        // Dynamic procedure name based on vertical (e.g., Elastomer_GetFinishingShutdownConfig)
	        String procedureName = verticalName + "_GetFinishingShutdownConfig";
	        
	        List<Object[]> obj = findByYearAndPlantId(year, plant.getId(), procedureName);

	        List<FinishingShutdownConfigDTO> dtos = new ArrayList<>();

	        for (Object[] row : obj) {
	           FinishingShutdownConfigDTO dto = new FinishingShutdownConfigDTO();
	            
	            // Mapping based on the SELECT order in your Stored Procedure
	            dto.setId(row[0] != null ? row[0].toString() : "");
	            dto.setYear(row[1] != null ? Integer.parseInt(row[1].toString()) : null);
	            dto.setMonth(row[2] != null ? Integer.parseInt(row[2].toString()) : null);
	            
	            dto.setShutdownHours(
	                (row[3] != null && !row[3].toString().trim().isEmpty())
	                    ? Double.parseDouble(row[3].toString().trim())
	                    : 0.0);

	            dto.setShutdownDate(row[4] != null ? (java.util.Date) row[4] : null);
	            dto.setCategory(row[5] != null ? Integer.parseInt(row[5].toString()) : null);
	            dto.setAuditYear(row[6] != null ? row[6].toString() : "");
	            dto.setRemarks(row[7] != null ? row[7].toString() : "");
	            dto.setUpdatedOn(row[8] != null ? (java.util.Date) row[8] : null);
	            dto.setUpdatedBy(row[9] != null ? row[9].toString() : "");
	            dto.setPlantFkId(row[10] != null ? row[10].toString() : "");

	            dtos.add(dto);
	        }

	        Map<String, Object> map = new HashMap<>();
	        map.put("Data", dtos);
	        
	        AOPMessageVM aopMessageVM = new AOPMessageVM();
	        aopMessageVM.setCode(200);
	        aopMessageVM.setData(map);
	        aopMessageVM.setMessage("Data fetched successfully");

	        return aopMessageVM;

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	    } catch (Exception ex) {
	       
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}
	
	public List<Object[]> findByYearAndPlantId(String aopYear, UUID siteId, String procedureName) {
		try {

			String sql = "EXEC " + procedureName
					+ " @plantId = :siteId, @AOPYear = :aopYear";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("siteId", siteId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public AOPMessageVM saveFinishingShutdown(String year, String plantFKId,
	        List<FinishingShutdownConfigDTO> dtos) {
	    try {
	        for (FinishingShutdownConfigDTO dto : dtos) {
	            
	            FinishingShutdownConfig entity;
	            
	           
	            if (dto.getId() != null && !dto.getId().trim().isEmpty()) {
	                entity = finishingShutdownConfigRepository.findById(UUID.fromString(dto.getId()))
	                        .orElse(new FinishingShutdownConfig());
	            } else {
	                entity = new FinishingShutdownConfig();
	            }

	           
	            entity.setYear(dto.getYear());
	            entity.setMonth(dto.getMonth());
	            entity.setShutdownHours(dto.getShutdownHours());
	            
	            
	            if (dto.getShutdownDate() != null) {
	                entity.setShutdownDate(dto.getShutdownDate());
	            }

	            entity.setCategory(dto.getCategory());
	            entity.setAuditYear(dto.getAuditYear());
	            entity.setRemarks(dto.getRemarks());
	            
	           
	            entity.setPlantFkId(UUID.fromString(plantFKId));
	            entity.setUpdatedBy(Utility.getUserName());
	            entity.setUpdatedOn(new Date());

	           
	            finishingShutdownConfigRepository.save(entity);
	        }

	        AOPMessageVM aopMessageVM = new AOPMessageVM();
	        aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("Shutdown data saved successfully");
	        return aopMessageVM;

	    } catch (Exception ex) {
	       
	        throw new RuntimeException("Failed to save shutdown data", ex);
	    }
	}
}
