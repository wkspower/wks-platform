package com.wks.caseengine.service;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.dto.EnergyPerformanceDTO;
import com.wks.caseengine.dto.PerformanceHighlightDTO;
import com.wks.caseengine.entity.EnergyPerformance;
import com.wks.caseengine.entity.PerformanceHighlight;
import com.wks.caseengine.entity.Sites;

import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.EnergyPerformanceRepository;
import com.wks.caseengine.repository.PerformanceHighlightsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class PerformanceHighlightsServiceImpl implements PerformanceHighlightsService{
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private PerformanceHighlightsRepository performanceHighlightsRepository;
	
	@Autowired
	private SiteRepository siteRepository;

	@Override
	public AOPMessageVM getPerformanceHighlights(String siteId, String year) {
	    try {
	        UUID siteUuid = UUID.fromString(siteId);

	        List<PerformanceHighlight> highlights = performanceHighlightsRepository.findBySiteAndYearNative(siteUuid, year);
	        
	       
	        List<PerformanceHighlightDTO> dtos = highlights.stream().map(h -> {
	            PerformanceHighlightDTO dto = new PerformanceHighlightDTO();
	            dto.setId(h.getId().toString());
	            dto.setSummary(h.getSummary());
	            dto.setSiteId(h.getSiteId().toString());
	            dto.setAopYear(h.getAopYear());
	            dto.setUpdatedBy(h.getUpdatedBy());
	            dto.setUpdatedDate(h.getUpdatedDate());
	            return dto;
	        }).collect(Collectors.toList());

	        
	        Map<String, Object> map = new HashMap<>();
	        map.put("Data", dtos);

	        AOPMessageVM aopMessageVM = new AOPMessageVM();
	        aopMessageVM.setCode(200);
	        aopMessageVM.setData(map);
	        aopMessageVM.setMessage("Data fetched successfully");

	        return aopMessageVM;

	    } catch (IllegalArgumentException e) {
	        
	        throw new RestInvalidArgumentException("Invalid UUID format for Site ID: " + siteId, e);
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new RuntimeException("Failed to fetch performance highlights", ex);
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
	public AOPMessageVM savePerformanceHighlights(String year, String siteId,
	        List<PerformanceHighlightDTO> performanceHighlightDTOs) {
	    try {
	        List<PerformanceHighlightDTO> failedList = new ArrayList<>();
	        String currentUser = Utility.getUserName();
	        Date currentDate = new Date(); 

	        for (PerformanceHighlightDTO dto : performanceHighlightDTOs) {
	            
	            if ("Failed".equalsIgnoreCase(dto.getSaveStatus())) {
	                failedList.add(dto);
	                continue;
	            }

	            PerformanceHighlight entity=null;

	            try {
	                if (dto.getId() != null) {
	                   
	                    Optional<PerformanceHighlight> existing = performanceHighlightsRepository.findById(UUID.fromString(dto.getId()));
	                    if (existing.isPresent()) {
	                        entity = existing.get();
	                    } else {
	                        dto.setSaveStatus("Failed");
	                        dto.setErrDescription("Record not found for ID: " + dto.getId());
	                        failedList.add(dto);
	                        continue;
	                    }
	                } 

	               
	                entity.setSummary(dto.getSummary());
	                
	                
	                entity.setUpdatedBy(currentUser);
	                entity.setUpdatedDate(currentDate);

	                performanceHighlightsRepository.save(entity);

	            } catch (Exception e) {
	                dto.setSaveStatus("Failed");
	                dto.setErrDescription("Error processing record: " + e.getMessage());
	                failedList.add(dto);
	            }
	        }

	       
	        AOPMessageVM aopMessageVM = new AOPMessageVM();
	        aopMessageVM.setCode(failedList.isEmpty() ? 200 : 207); // 207 = Multi-Status (Partial success)
	        aopMessageVM.setData(failedList);
	        aopMessageVM.setMessage(failedList.isEmpty() ? "Data updated successfully" : "Data processed with some errors");
	        
	        return aopMessageVM;

	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new RuntimeException("Global failure while saving performance highlights", ex);
	    }
	}
	
}
