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
import com.wks.caseengine.dto.AOPProposedNormsDTO;
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
public class AOPProposedNormsServiceImpl implements AOPProposedNormsService {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	
	@Override
	public AOPMessageVM getProposedNorms(String year,String plantId,String gradeId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<AOPProposedNormsDTO> aopProposedNormsDTOList = new ArrayList<AOPProposedNormsDTO>();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			List<Object[]> obj=null;
				String procedureName=vertical.getName()+"_"+site.getName()+"_"+"GetAOPProposedNormsGradeWise";
				obj = getData(year,plantId,gradeId,procedureName);
			
				for (Object[] row : obj) {
				    AOPProposedNormsDTO dto = new AOPProposedNormsDTO();

				    // Mapping fields by index (0, 1, 2, ...)
				    dto.setId(row[0] != null ? row[0].toString() : null);
				    dto.setNormParameterTypeDisplayName(row[1] != null ? row[1].toString() : null);
				    dto.setNormParameterDisplayName(row[2] != null ? row[2].toString() : null);
				    dto.setUOM(row[3] != null ? row[3].toString() : null);

				    // Previous Year Budgets (Indices 4 to 15)
				    dto.setPrevYearBudgetApril(row[4] != null ? Double.valueOf(row[4].toString()) : null);
				    dto.setPrevYearBudgetMay(row[5] != null ? Double.valueOf(row[5].toString()) : null);
				    dto.setPrevYearBudgetJune(row[6] != null ? Double.valueOf(row[6].toString()) : null);
				    dto.setPrevYearBudgetJuly(row[7] != null ? Double.valueOf(row[7].toString()) : null);
				    dto.setPrevYearBudgetAugust(row[8] != null ? Double.valueOf(row[8].toString()) : null);
				    dto.setPrevYearBudgetSeptember(row[9] != null ? Double.valueOf(row[9].toString()) : null);
				    dto.setPrevYearBudgetOctober(row[10] != null ? Double.valueOf(row[10].toString()) : null);
				    dto.setPrevYearBudgetNovember(row[11] != null ? Double.valueOf(row[11].toString()) : null);
				    dto.setPrevYearBudgetDecember(row[12] != null ? Double.valueOf(row[12].toString()) : null);
				    dto.setPrevYearBudgetJanuary(row[13] != null ? Double.valueOf(row[13].toString()) : null);
				    dto.setPrevYearBudgetFebruary(row[14] != null ? Double.valueOf(row[14].toString()) : null);
				    dto.setPrevYearBudgetMarch(row[15] != null ? Double.valueOf(row[15].toString()) : null);

				    // Current Year Budgets (Indices 16 to 27)
				    dto.setCurrYearBudgetApril(row[16] != null ? Double.valueOf(row[16].toString()) : null);
				    dto.setCurrYearBudgetMay(row[17] != null ? Double.valueOf(row[17].toString()) : null);
				    dto.setCurrYearBudgetJune(row[18] != null ? Double.valueOf(row[18].toString()) : null);
				    dto.setCurrYearBudgetJuly(row[19] != null ? Double.valueOf(row[19].toString()) : null);
				    dto.setCurrYearBudgetAugust(row[20] != null ? Double.valueOf(row[20].toString()) : null);
				    dto.setCurrYearBudgetSeptember(row[21] != null ? Double.valueOf(row[21].toString()) : null);
				    dto.setCurrYearBudgetOctober(row[22] != null ? Double.valueOf(row[22].toString()) : null);
				    dto.setCurrYearBudgetNovember(row[23] != null ? Double.valueOf(row[23].toString()) : null);
				    dto.setCurrYearBudgetDecember(row[24] != null ? Double.valueOf(row[24].toString()) : null);
				    dto.setCurrYearBudgetJanuary(row[25] != null ? Double.valueOf(row[25].toString()) : null);
				    dto.setCurrYearBudgetFebruary(row[26] != null ? Double.valueOf(row[26].toString()) : null);
				    dto.setCurrYearBudgetMarch(row[27] != null ? Double.valueOf(row[27].toString()) : null);

				    // Current Year Proposed (Indices 28 to 39)
				    dto.setCurrYearProposedApril(row[28] != null ? Double.valueOf(row[28].toString()) : null);
				    dto.setCurrYearProposedMay(row[29] != null ? Double.valueOf(row[29].toString()) : null);
				    dto.setCurrYearProposedJune(row[30] != null ? Double.valueOf(row[30].toString()) : null);
				    dto.setCurrYearProposedJuly(row[31] != null ? Double.valueOf(row[31].toString()) : null);
				    dto.setCurrYearProposedAugust(row[32] != null ? Double.valueOf(row[32].toString()) : null);
				    dto.setCurrYearProposedSeptember(row[33] != null ? Double.valueOf(row[33].toString()) : null);
				    dto.setCurrYearProposedOctober(row[34] != null ? Double.valueOf(row[34].toString()) : null);
				    dto.setCurrYearProposedNovember(row[35] != null ? Double.valueOf(row[35].toString()) : null);
				    dto.setCurrYearProposedDecember(row[36] != null ? Double.valueOf(row[36].toString()) : null);
				    dto.setCurrYearProposedJanuary(row[37] != null ? Double.valueOf(row[37].toString()) : null);
				    dto.setCurrYearProposedFebruary(row[38] != null ? Double.valueOf(row[38].toString()) : null);
				    dto.setCurrYearProposedMarch(row[39] != null ? Double.valueOf(row[39].toString()) : null);

				    // Final String fields (Indices 40 to 44)
				    dto.setRemarks(row[40] != null ? row[40].toString() : null);
				    dto.setGradeId(row[41] != null ? row[41].toString() : null);
				    dto.setPlantId(row[42] != null ? row[42].toString() : null);
				    dto.setAopYear(row[43] != null ? row[43].toString() : null);
				    dto.setModifiedBy(row[44] != null ? row[44].toString() : null);

				    aopProposedNormsDTOList.add(dto);
				}
				
			aopMessageVM.setCode(200);
			aopMessageVM.setData(aopProposedNormsDTOList);
			aopMessageVM.setMessage("Data fetched successfully");
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public List<Object[]> getData(String aopYear,String plantId,String gradeId, String procedureName) {
		try {

			String sql = "EXEC " + procedureName
					+ " @PlantId = :plantId, @AOPYear = :aopYear, @GradeId = :gradeId";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);
			query.setParameter("gradeId", gradeId);
			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
}
