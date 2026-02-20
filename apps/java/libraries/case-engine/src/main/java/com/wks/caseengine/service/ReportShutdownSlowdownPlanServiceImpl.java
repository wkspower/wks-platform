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

import com.wks.caseengine.dto.ReportCapexPIOPlanDTO;
import com.wks.caseengine.dto.ShutdownSlowdownPlanDTO;
import com.wks.caseengine.entity.ReportCapexPIOPlan;
import com.wks.caseengine.entity.ReportShutdownSlowdownPlan;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.ReportCapexPIOPlanRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class ReportShutdownSlowdownPlanServiceImpl implements ReportShutdownSlowdownPlanService {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private ReportShutdownSlowdownPlanRepository reportShutdownSlowdownPlanRepository;
	
	@Autowired
	private SiteRepository siteRepository;

	@Override
	public AOPMessageVM getShutdownSlowdownPlan(String siteId, String year) {
	    try {
	        List<Object[]> obj = new ArrayList<>();

	        Sites site = siteRepository.findById(UUID.fromString(siteId)).orElseThrow();
	        String procedureName = "RPT_GetShutdownSlowdownPlan";
	        obj = findByYearAndSiteId(year, site.getId(), procedureName);

	        
	        List<ShutdownSlowdownPlanDTO> shutdownSlowdownPlanDTOs = new ArrayList<>();

	        for (Object[] row : obj) {
	            ShutdownSlowdownPlanDTO dto = new ShutdownSlowdownPlanDTO();
	            
	            
	            dto.setId(row[0] != null ? row[0].toString() : "");

	            
	            dto.setPlant(row[1] != null ? row[1].toString() : "");

	            
	            dto.setNoOfShutdownDays(
	                    (row[2] != null && !row[2].toString().trim().isEmpty())
	                            ? Double.parseDouble(row[2].toString().trim())
	                            : 0.0);

	           
	            dto.setNoOfSlowdownDays(
	                    (row[3] != null && !row[3].toString().trim().isEmpty())
	                            ? Double.parseDouble(row[3].toString().trim())
	                            : 0.0);

	           
	            dto.setMonthPlan(row[4] != null ? row[4].toString() : "");

	           
	            dto.setShutdownSlowdownPlan(row[5] != null ? row[5].toString() : "");

	            
	            dto.setRemarks(row[6] != null ? row[6].toString() : "");

	           
	            dto.setSiteId(row[7] != null ? row[7].toString() : "");

	           
	            dto.setAopYear(row[8] != null ? row[8].toString() : "");

	           
	            dto.setUpdatedBy(row[9] != null ? row[9].toString() : "");

	            
	            dto.setUpdatedDate(row[10] != null ? (java.util.Date) row[10] : null);

	            shutdownSlowdownPlanDTOs.add(dto);
	        }

	        Map<String, Object> map = new HashMap<>();
	        map.put("Data", shutdownSlowdownPlanDTOs);
	        
	        AOPMessageVM aopMessageVM = new AOPMessageVM();
	        aopMessageVM.setCode(200);
	        aopMessageVM.setData(map);
	        aopMessageVM.setMessage("Data fetched successfully");

	        return aopMessageVM;

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}
	
	public List<Object[]> findByYearAndSiteId(String aopYear, UUID siteId, String procedureName) {
		try {

			String sql = "EXEC " + procedureName
					+ " @SiteId = :siteId, @AOPYear = :aopYear";

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
	public AOPMessageVM saveShutdownSlowdownPlan(String year, String plantFKId,
	        List<ShutdownSlowdownPlanDTO> shutdownSlowdownPlanDTOs) {
	    try {
	        for (ShutdownSlowdownPlanDTO dto : shutdownSlowdownPlanDTOs) {
	            
	        	ReportShutdownSlowdownPlan entity = null;
	            
	            // Check if ID exists for Update, otherwise create New for Insert
	            if (dto.getId() != null && !dto.getId().trim().isEmpty()) {
	                Optional<ReportShutdownSlowdownPlan> entityOpt = reportShutdownSlowdownPlanRepository.findById(UUID.fromString(dto.getId()));
	                if (entityOpt.isPresent()) {
	                    entity = entityOpt.get();
	                } else {
	                    entity = new ReportShutdownSlowdownPlan();
	                }
	            } else {
	                entity = new ReportShutdownSlowdownPlan();
	            }

	            // Mapping fields from DTO to Entity based on SQL Schema
	            entity.setPlant(dto.getPlant());
	            entity.setNoOfShutdownDays(dto.getNoOfShutdownDays());
	            entity.setNoOfSlowdownDays(dto.getNoOfSlowdownDays());
	            entity.setMonthPlan(dto.getMonthPlan());
	            entity.setShutdownSlowdownPlan(dto.getShutdownSlowdownPlan());
	            entity.setRemarks(dto.getRemarks());

	            // Handle SiteId conversion
	            entity.setSiteId(
	                    dto.getSiteId() != null && !dto.getSiteId().trim().isEmpty()
	                            ? UUID.fromString(dto.getSiteId())
	                            : null);

	            entity.setAopYear(dto.getAopYear());
	            
	            // Audit fields
	            entity.setUpdatedBy(Utility.getUserName());
	            entity.setUpdatedDate(new Date());

	            // Save performing either SQL INSERT or UPDATE
	            reportShutdownSlowdownPlanRepository.save(entity);
	        }

	        AOPMessageVM aopMessageVM = new AOPMessageVM();
	        aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("Data updated successfully");
	        return aopMessageVM;

	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new RuntimeException("Failed to save data", ex);
	    }
	}
}
