package com.wks.caseengine.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AOPReportDTO;
import com.wks.caseengine.dto.WorkflowYearDTO;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class AOPReportServiceImpl implements AOPReportService{
	
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public AOPMessageVM getAnnualAOPReport(String plantId, String year, String reportType) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			List<Object[]> results = getAnnualAOPReportData(plantId, year,reportType);

			List<AOPReportDTO> aopReportDTOList = new ArrayList<>();
			for (Object[] row : results) {
				AOPReportDTO dto = new AOPReportDTO();
			
				dto.setNorm(row[0] != null ? row[0].toString() : null);
				dto.setMaterialFKId(row[1] != null ? row[1].toString() : null);	
				dto.setApril(row[2] != null ? Float.parseFloat(row[2].toString()) : null);
				dto.setMay(row[3] != null ? Float.parseFloat(row[3].toString()) : null);
				dto.setJune(row[4] != null ? Float.parseFloat(row[4].toString()) : null);
				dto.setJuly(row[5] != null ? Float.parseFloat(row[5].toString()) : null);
				dto.setAugust(row[6] != null ? Float.parseFloat(row[6].toString()) : null);
				dto.setSeptember(row[7] != null ? Float.parseFloat(row[7].toString()) : null);
				dto.setOctober(row[8] != null ? Float.parseFloat(row[8].toString()) : null);
				dto.setNovember(row[9] != null ? Float.parseFloat(row[9].toString()) : null);
				dto.setDecember(row[10] != null ? Float.parseFloat(row[10].toString()) : null);
				dto.setJanuary(row[11] != null ? Float.parseFloat(row[11].toString()) : null);
				dto.setFebruary(row[12] != null ? Float.parseFloat(row[12].toString()) : null);
				dto.setMarch(row[13] != null ? Float.parseFloat(row[13].toString()) : null);
				aopReportDTOList.add(dto);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(aopReportDTOList);
			return aopMessageVM;
			
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}
	
	public List<Object[]> getAnnualAOPReportData(String plantId, String aopYear,String reportType) {
		try {
			// Stored procedure name
			String procedureName = "AnnualCostAopReport";

			// Prepare native SQL call with parameters
			String sql = "EXEC " + procedureName + " @plantId = :plantId, @aopYear = :aopYear, @reportType = :reportType";

			Query query = entityManager.createNativeQuery(sql);

			// Set parameters
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);
			query.setParameter("reportType", reportType);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}


}
