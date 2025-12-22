package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AOPDTO;
import com.wks.caseengine.dto.AOPDashboardDTO;
import com.wks.caseengine.entity.AOP;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AOPRepository;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.UUID;


import javax.sql.DataSource;

import java.io.ByteArrayOutputStream;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Connection;
import jakarta.persistence.Query;

@Service
public class AOPDashboardServiceImpl implements AOPDashboardService {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Override
	public AOPMessageVM getAOPDashboard(String year) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<AOPDashboardDTO> aopDashboardDTOList = new ArrayList<AOPDashboardDTO>();
		try {
			List<Object[]> obj=null;
				String procedureName="sp_GetAOPDashboardUtility";
				obj = getData(year,procedureName);
			
			for (Object[] row : obj) {
				AOPDashboardDTO aopDashboardDTO = new AOPDashboardDTO();

				aopDashboardDTO.setId(row[0] != null ? row[0].toString() : null);
				aopDashboardDTO.setSiteId(row[1] != null ? row[1].toString() : null);
				
				aopDashboardDTO.setVerticalId(row[2] != null ? row[2].toString() : null);
				aopDashboardDTO.setStatus(row[3] != null ? row[3].toString() : null);
				aopDashboardDTO.setStatusColor(row[4] != null ? row[4].toString() : null);
				aopDashboardDTO.setStatusTextColor(row[5] != null ? row[5].toString() : null);
				aopDashboardDTO.setDisplayOrder(row[6] != null ? Integer.parseInt(row[6].toString()) : null);
				aopDashboardDTO.setIsActive(row[7] != null ? Boolean.valueOf(row[7].toString()) : null);
				aopDashboardDTO.setNotes(row[8] != null ? row[8].toString() : null);
				aopDashboardDTO.setAopYear(row[9] != null ? row[9].toString() : null);

				aopDashboardDTOList.add(aopDashboardDTO);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setData(aopDashboardDTOList);
			aopMessageVM.setMessage("Data fetched successfully");
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public List<Object[]> getData(String aopYear, String procedureName) {
		try {

			String sql = "EXEC " + procedureName
					+ " @aopYear = :aopYear";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
}
