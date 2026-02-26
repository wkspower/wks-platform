package com.wks.caseengine.service;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.dto.EnergyPerformanceDTO;
import com.wks.caseengine.entity.EnergyPerformance;
import com.wks.caseengine.entity.Sites;

import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.EnergyPerformanceRepository;

import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class EnergyPerformanceTranscationServiceImpl implements EnergyPerformanceTranscationService{
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private EnergyPerformanceRepository energyPerformanceRepository;
	
	@Autowired
	private SiteRepository siteRepository;

	@Override
	public AOPMessageVM getEnergyPerformanceTransaction(String siteId, String year) {
		
		try {
			List<Object[]> obj = new ArrayList<>();
			
			
			Sites site = siteRepository.findById(UUID.fromString(siteId)).orElseThrow();
				String procedureName = site.getName()+"_EnergyPerformanceTranscation";
				obj = findByYearAndSiteId(year, site.getId(), procedureName);
			
			List<EnergyPerformanceDTO> energyPerformanceDTOs = new ArrayList<>();
			
			for (Object[] row : obj) {
				EnergyPerformanceDTO energyPerformanceDTO = new EnergyPerformanceDTO();
				energyPerformanceDTO.setId(row[0] != null ? row[0].toString() : "");	
				energyPerformanceDTO.setPlant(row[1] != null ? row[1].toString() : "");
				energyPerformanceDTO.setUom(row[2] != null ? row[2].toString() : "");
				energyPerformanceDTO.setAopValue(
						(row[3] != null && !row[3].toString().trim().isEmpty())
								? Double.parseDouble(row[3].toString().trim())
								: 0.0);
				energyPerformanceDTO.setActualValue(
						(row[4] != null && !row[4].toString().trim().isEmpty())
								? Double.parseDouble(row[4].toString().trim())
								: 0.0);
				energyPerformanceDTO.setPlanValue(
						(row[5] != null && !row[5].toString().trim().isEmpty())
								? Double.parseDouble(row[5].toString().trim())
								: 0.0);
				energyPerformanceDTO.setRemark(row[6] != null ? row[6].toString() : "");
				energyPerformanceDTOs.add(energyPerformanceDTO);
				
			}
			Map<String, Object> map = new HashMap<>(); 
			
			map.put("Data", energyPerformanceDTOs);
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
	public AOPMessageVM saveEnergyPerformanceTransaction(String year, String plantFKId,
			List<EnergyPerformanceDTO> energyPerformanceDTOs) {
		try {
			List<EnergyPerformanceDTO> failedList = new ArrayList<>();

			for (EnergyPerformanceDTO energyPerformanceDTO : energyPerformanceDTOs) {
				if (energyPerformanceDTO.getSaveStatus() != null
						&& energyPerformanceDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(energyPerformanceDTO);
					continue;
				}
				EnergyPerformance energyPerformance =null;
				if(energyPerformanceDTO.getId()!=null) {
					Optional<EnergyPerformance> energyPerformanceOpt=energyPerformanceRepository.findById(UUID.fromString(energyPerformanceDTO.getId()));
					if(energyPerformanceOpt.isPresent()) {
						energyPerformance=energyPerformanceOpt.get();
					}else {
						energyPerformanceDTO.setSaveStatus("Failed");
						energyPerformanceDTO.setErrDescription("Data not present with given id");
						failedList.add(energyPerformanceDTO);
					}
				}
				energyPerformance.setAopValue(energyPerformanceDTO.getAopValue());
				energyPerformance.setActualValue(energyPerformanceDTO.getActualValue());			
				energyPerformance.setRemark(energyPerformanceDTO.getRemark());
				energyPerformance.setPlanValue(energyPerformanceDTO.getPlanValue());
				energyPerformance.setUpdatedBy(Utility.getUserName());
				energyPerformance.setUpdatedDateTime(new Date());
				energyPerformanceRepository.save(energyPerformance);
			}
			
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(failedList);
			aopMessageVM.setMessage("Data updated successfully");
			return aopMessageVM;
		} catch (Exception ex) {
			ex.printStackTrace();
			
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	
}
