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

import com.wks.caseengine.dto.ReportCapexPIOPlanDTO;
import com.wks.caseengine.entity.ReportCapexPIOPlan;
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
public class ReportCapexPIOPlanServiceImpl implements ReportCapexPIOPlanService {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private ReportCapexPIOPlanRepository reportCapexPIOPlanRepository;
	
	@Autowired
	private SiteRepository siteRepository;

	@Override
	public AOPMessageVM getReportCapexPIOPlanTransaction(String siteId, String year) {
		
		try {
			List<Object[]> obj = new ArrayList<>();
			
			
			Sites site = siteRepository.findById(UUID.fromString(siteId)).orElseThrow();
				String procedureName = "RPT_"+site.getName()+"_GetCapexPIOPlan";
				obj = findByYearAndSiteId(year, site.getId(), procedureName);
			
			List<ReportCapexPIOPlanDTO> reportCapexPIOPlanDTOs = new ArrayList<>();
			
			for (Object[] row : obj) {
				ReportCapexPIOPlanDTO reportCapexPIOPlanDTO = new ReportCapexPIOPlanDTO();
				reportCapexPIOPlanDTO.setId(row[0] != null ? row[0].toString() : "");

				reportCapexPIOPlanDTO.setProposal(row[1] != null ? row[1].toString() : "");
				reportCapexPIOPlanDTO.setCategory(row[2] != null ? row[2].toString() : "");
				reportCapexPIOPlanDTO.setJustification(row[3] != null ? row[3].toString() : "");

				reportCapexPIOPlanDTO.setCostRsCr(
				        (row[4] != null && !row[4].toString().trim().isEmpty())
				                ? Double.parseDouble(row[4].toString().trim())
				                : 0.0);

				reportCapexPIOPlanDTO.setBenefitRsCr(
				        (row[5] != null && !row[5].toString().trim().isEmpty())
				                ? Double.parseDouble(row[5].toString().trim())
				                : 0.0);

				reportCapexPIOPlanDTO.setTargetPlan(row[6] != null ? row[6].toString() : "");
				reportCapexPIOPlanDTO.setStatusPlan(row[7] != null ? row[7].toString() : "");
				reportCapexPIOPlanDTO.setRemarks(row[8] != null ? row[8].toString() : "");

				reportCapexPIOPlanDTO.setSiteId(row[9] != null ? row[9].toString() : "");
				reportCapexPIOPlanDTO.setAopYear(row[10] != null ? row[10].toString() : "");
				reportCapexPIOPlanDTO.setUpdatedBy(row[11] != null ? row[11].toString() : "");

				reportCapexPIOPlanDTO.setUpdatedDate(row[12] != null ? (java.util.Date) row[12] : null);

				reportCapexPIOPlanDTOs.add(reportCapexPIOPlanDTO);
				
			}
			Map<String, Object> map = new HashMap<>(); 
			
			map.put("Data", reportCapexPIOPlanDTOs);
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
	public AOPMessageVM saveReportCapexPIOPlanTransaction(String year, String plantFKId,
			List<ReportCapexPIOPlanDTO> reportCapexPIOPlanDTOs) {
		try {

			for (ReportCapexPIOPlanDTO reportCapexPIOPlanDTO : reportCapexPIOPlanDTOs) {
				
				ReportCapexPIOPlan reportCapexPIOPlan =null;
				if(reportCapexPIOPlanDTO.getId()!=null) {
					Optional<ReportCapexPIOPlan> ReportCapexPIOPlanOpt=reportCapexPIOPlanRepository.findById(UUID.fromString(reportCapexPIOPlanDTO.getId()));
					if(ReportCapexPIOPlanOpt.isPresent()) {
						reportCapexPIOPlan=ReportCapexPIOPlanOpt.get();
					}
				}else {
					reportCapexPIOPlan = new ReportCapexPIOPlan();
				}
				
				
				reportCapexPIOPlan.setProposal(reportCapexPIOPlanDTO.getProposal());
				reportCapexPIOPlan.setCategory(reportCapexPIOPlanDTO.getCategory());
				reportCapexPIOPlan.setJustification(reportCapexPIOPlanDTO.getJustification());

				reportCapexPIOPlan.setCostRsCr(reportCapexPIOPlanDTO.getCostRsCr());
				reportCapexPIOPlan.setBenefitRsCr(reportCapexPIOPlanDTO.getBenefitRsCr());

				reportCapexPIOPlan.setTargetPlan(reportCapexPIOPlanDTO.getTargetPlan());
				reportCapexPIOPlan.setStatusPlan(reportCapexPIOPlanDTO.getStatusPlan());
				reportCapexPIOPlan.setRemarks(reportCapexPIOPlanDTO.getRemarks());

				reportCapexPIOPlan.setSiteId(
				        reportCapexPIOPlanDTO.getSiteId() != null && !reportCapexPIOPlanDTO.getSiteId().trim().isEmpty()
				                ? UUID.fromString(reportCapexPIOPlanDTO.getSiteId())
				                : null);

				reportCapexPIOPlan.setAopYear(reportCapexPIOPlanDTO.getAopYear());

				reportCapexPIOPlan.setUpdatedBy(Utility.getUserName());
				reportCapexPIOPlan.setUpdatedDate(new Date());

				
				reportCapexPIOPlanRepository.save(reportCapexPIOPlan);
			}
			
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			//aopMessageVM.setData(failedList);
			aopMessageVM.setMessage("Data updated successfully");
			return aopMessageVM;
		} catch (Exception ex) {
			ex.printStackTrace();
			
			throw new RuntimeException("Failed to save data", ex);
		}
	}

}
