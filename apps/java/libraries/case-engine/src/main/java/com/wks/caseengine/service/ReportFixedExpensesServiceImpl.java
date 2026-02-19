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

import com.wks.caseengine.dto.ReportFixedExpensesDTO;
import com.wks.caseengine.entity.ReportFixedExpenses;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.ReportFixedExpensesRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class ReportFixedExpensesServiceImpl implements ReportFixedExpensesService {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private ReportFixedExpensesRepository reportFixedExpensesRepository;
	
	@Autowired
	private SiteRepository siteRepository;

	@Override
	public AOPMessageVM getReportFixedExpensesTransaction(String siteId, String year) {
		
		try {
			List<Object[]> obj = new ArrayList<>();
			
			
			Sites site = siteRepository.findById(UUID.fromString(siteId)).orElseThrow();
				String procedureName = "RPT_"+site.getName()+"_GetFixedExpenses";
				obj = findByYearAndSiteId(year, site.getId(), procedureName);
			
			List<ReportFixedExpensesDTO> reportFixedExpensesDTOs = new ArrayList<>();
			
			for (Object[] row : obj) {
				ReportFixedExpensesDTO reportFixedExpensesDTO = new ReportFixedExpensesDTO();
				reportFixedExpensesDTO.setId(row[0] != null ? row[0].toString() : "");

				reportFixedExpensesDTO.setParticulars(row[1] != null ? row[1].toString() : "");

				reportFixedExpensesDTO.setFyPrevAOP(
				        (row[2] != null && !row[2].toString().trim().isEmpty())
				                ? Double.parseDouble(row[2].toString().trim())
				                : 0.0);

				reportFixedExpensesDTO.setFyPrevActual(
				        (row[3] != null && !row[3].toString().trim().isEmpty())
				                ? Double.parseDouble(row[3].toString().trim())
				                : 0.0);

				reportFixedExpensesDTO.setFyCurrAOP(
				        (row[4] != null && !row[4].toString().trim().isEmpty())
				                ? Double.parseDouble(row[4].toString().trim())
				                : 0.0);

				reportFixedExpensesDTO.setPercentageChange(
				        (row[5] != null && !row[5].toString().trim().isEmpty())
				                ? Double.parseDouble(row[5].toString().trim())
				                : 0.0);

				reportFixedExpensesDTO.setVariance(
				        (row[6] != null && !row[6].toString().trim().isEmpty())
				                ? Double.parseDouble(row[6].toString().trim())
				                : 0.0);

				reportFixedExpensesDTO.setRemarks(row[7] != null ? row[7].toString() : "");

				reportFixedExpensesDTO.setSiteId(row[8] != null ? row[8].toString() : "");
				reportFixedExpensesDTO.setAopYear(row[9] != null ? row[9].toString() : "");
				reportFixedExpensesDTO.setUpdatedBy(row[10] != null ? row[10].toString() : "");

				reportFixedExpensesDTO.setUpdatedDate(row[11] != null ? (java.util.Date) row[11] : null);

				reportFixedExpensesDTOs.add(reportFixedExpensesDTO);
				
			}
			Map<String, Object> map = new HashMap<>(); 
			
			map.put("Data", reportFixedExpensesDTOs);
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
	public AOPMessageVM saveReportFixedExpensesTransaction(String year, String plantFKId,
			List<ReportFixedExpensesDTO> reportFixedExpensesDTOs) {
		try {

			for (ReportFixedExpensesDTO reportFixedExpensesDTO : reportFixedExpensesDTOs) {
				
				ReportFixedExpenses reportFixedExpenses =null;
				if(reportFixedExpensesDTO.getId()!=null) {
					Optional<ReportFixedExpenses> ReportFixedExpensesOpt=reportFixedExpensesRepository.findById(UUID.fromString(reportFixedExpensesDTO.getId()));
					if(ReportFixedExpensesOpt.isPresent()) {
						reportFixedExpenses=ReportFixedExpensesOpt.get();
					}else {
						reportFixedExpenses = new ReportFixedExpenses();
					}
				}else {
					reportFixedExpenses = new ReportFixedExpenses();
				}
				
				
				reportFixedExpenses.setParticulars(reportFixedExpensesDTO.getParticulars());
				reportFixedExpenses.setFyPrevAOP(reportFixedExpensesDTO.getFyPrevAOP());
				reportFixedExpenses.setFyPrevActual(reportFixedExpensesDTO.getFyPrevActual());
				reportFixedExpenses.setFyCurrAOP(reportFixedExpensesDTO.getFyCurrAOP());
				reportFixedExpenses.setPercentageChange(reportFixedExpensesDTO.getPercentageChange());
				reportFixedExpenses.setVariance(reportFixedExpensesDTO.getVariance());
				reportFixedExpenses.setRemarks(reportFixedExpensesDTO.getRemarks());

				reportFixedExpenses.setSiteId(
				        reportFixedExpensesDTO.getSiteId() != null && !reportFixedExpensesDTO.getSiteId().trim().isEmpty()
				                ? UUID.fromString(reportFixedExpensesDTO.getSiteId())
				                : null);

				reportFixedExpenses.setAopYear(reportFixedExpensesDTO.getAopYear());

				reportFixedExpenses.setUpdatedBy(Utility.getUserName());
				reportFixedExpenses.setUpdatedDate(new Date());

				reportFixedExpensesRepository.save(reportFixedExpenses);
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
