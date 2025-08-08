 package com.wks.caseengine.service;

 import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.DecokeRunLengthDTO;
import com.wks.caseengine.dto.NormTransactionsDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

 @Service
 public class NormTransactionsServiceImpl implements NormTransactionsService {
	
 	@Autowired
 	private NormParametersRepository normParametersRepository;
 	
 	@PersistenceContext
	private EntityManager entityManager;
 	
 	@Autowired
	private PlantsRepository plantsRepository;

	@Override
	public AOPMessageVM getNormTransactions(String plantId, String year, String screen) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		
		List<NormTransactionsDTO> normTransactionsDTOs = new ArrayList<>();
		try {
			List<Object[]> results = null;
			results = getData(plantId,year,screen, "vwValueUpdateHistory");
			for (Object[] row : results) {
				NormTransactionsDTO normTransactionsDTO = new NormTransactionsDTO();
				normTransactionsDTO.setId(row[0] != null ? row[0].toString() : null);
				normTransactionsDTO.setPlantId(row[1] != null ? row[1].toString() : null);
				normTransactionsDTO.setAopYear(row[2] != null ? row[2].toString() : null);
				normTransactionsDTO.setNormParameterDisplayName(row[3] != null ? row[3].toString() : null);
				normTransactionsDTO.setAopMonth(row[4] != null ? Integer.parseInt(row[4].toString()) : null);
				normTransactionsDTO.setAopMonthName(row[5] != null ? row[5].toString() : null);
				normTransactionsDTO.setAttributeValue(row[6] != null ? row[6].toString() : null);
				normTransactionsDTO.setRemark(row[7] != null ? row[7].toString() : null);
				normTransactionsDTO.setVersion(row[8] != null ? Integer.parseInt(row[8].toString()) : null);
				normTransactionsDTO.setCreatedBy(row[9] != null ? row[9].toString() : null);
				normTransactionsDTO.setScreen(row[10] != null ? row[10].toString() : null);
				normTransactionsDTOs.add(normTransactionsDTO);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(normTransactionsDTOs);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}
	
	public List<Object[]> getData(String plantId,String aopYear,String screen ,String viewName) {
		try {

			// 2. Construct SQL with dynamic view name
			String sql = "SELECT * FROM " + viewName +
					" WHERE Plant_FK_Id = :plantId and AOPYear = :aopYear and Screen = :screen";

			// 3. Create and parameterize the native query
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);
			query.setParameter("screen", screen);

			// 4. Execute
			return query.getResultList();

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid argument: " + e.getMessage(), e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data from view " + viewName, ex);
		}
	}


 }
