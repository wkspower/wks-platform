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
import com.wks.caseengine.dto.TechnicalAvailabilityDTO;
import com.wks.caseengine.entity.ReportCapexPIOPlan;
import com.wks.caseengine.entity.ReportShutdownSlowdownPlan;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.TechnicalAvailability;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.ReportCapexPIOPlanRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.TechnicalAvailabilityRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class ReportTechnicalAvailabilityServiceImpl implements ReportTechnicalAvailabilityService {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private TechnicalAvailabilityRepository technicalAvailabilityRepository;
	
	@Autowired
	private SiteRepository siteRepository;

	@Override
	public AOPMessageVM getTechnicalAvailability(String siteId, String year) {
	    try {
	        List<Object[]> obj = new ArrayList<>();

	        Sites site = siteRepository.findById(UUID.fromString(siteId)).orElseThrow();
	        String procedureName = "RPT_" + site.getName() + "_GetTechnicalAvailability";
	        obj = findByYearAndSiteId(year, site.getId(), procedureName);

	        List<TechnicalAvailabilityDTO> technicalAvailabilityDTOs = new ArrayList<>();

	        for (Object[] row : obj) {
	            TechnicalAvailabilityDTO dto = new TechnicalAvailabilityDTO();
	            
	            
	            dto.setId(row[0] != null ? row[0].toString() : "");

	           
	            dto.setPlant(row[1] != null ? row[1].toString() : "");

	            
	            dto.setFyPrevAOP(
	                    (row[2] != null && !row[2].toString().trim().isEmpty())
	                            ? Double.parseDouble(row[2].toString().trim())
	                            : 0.0);

	            
	            dto.setFyPrevActual(
	                    (row[3] != null && !row[3].toString().trim().isEmpty())
	                            ? Double.parseDouble(row[3].toString().trim())
	                            : 0.0);

	           
	            dto.setFyCurrAOP(
	                    (row[4] != null && !row[4].toString().trim().isEmpty())
	                            ? Double.parseDouble(row[4].toString().trim())
	                            : 0.0);

	           
	            dto.setRemarks(row[5] != null ? row[5].toString() : "");

	          
	            dto.setSiteId(row[6] != null ? row[6].toString() : "");

	            
	            dto.setAopYear(row[7] != null ? row[7].toString() : "");

	            
	            dto.setUpdatedBy(row[8] != null ? row[8].toString() : "");

	           
	            dto.setUpdatedDate(row[9] != null ? (java.util.Date) row[9] : null);

	            technicalAvailabilityDTOs.add(dto);
	        }

	        Map<String, Object> map = new HashMap<>();
	        map.put("Data", technicalAvailabilityDTOs);
	        
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
	public AOPMessageVM saveTechnicalAvailability(String year, String plantFKId,
	        List<TechnicalAvailabilityDTO> technicalAvailabilityDTOs) {
	    try {
	        for (TechnicalAvailabilityDTO dto : technicalAvailabilityDTOs) {
	            
	        	TechnicalAvailability entity = null;
	            
	            if (dto.getId() != null && !dto.getId().trim().isEmpty()) {
	                Optional<TechnicalAvailability> entityOpt = technicalAvailabilityRepository.findById(UUID.fromString(dto.getId()));
	                if (entityOpt.isPresent()) {
	                    entity = entityOpt.get();
	                } else {
	                    entity = new TechnicalAvailability();
	                }
	            } else {
	                entity = new TechnicalAvailability();
	            }
	            entity.setPlant(dto.getPlant());
	            entity.setFyPrevAOP(dto.getFyPrevAOP());
	            entity.setFyPrevActual(dto.getFyPrevActual());
	            entity.setFyCurrAOP(dto.getFyCurrAOP());
	            
	            entity.setRemarks(dto.getRemarks());
	            entity.setSiteId(
	                    dto.getSiteId() != null && !dto.getSiteId().trim().isEmpty()
	                            ? UUID.fromString(dto.getSiteId())
	                            : null);

	            entity.setAopYear(dto.getAopYear());
	            
	          
	            entity.setUpdatedBy(Utility.getUserName());
	            entity.setUpdatedDate(new Date());

	           
	            technicalAvailabilityRepository.save(entity);
	        }

	        AOPMessageVM aopMessageVM = new AOPMessageVM();
	        aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("Data updated successfully");
	        return aopMessageVM;

	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new RuntimeException("Failed to save Technical Availability data", ex);
	    }
	}
}
