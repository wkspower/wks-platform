 package com.wks.caseengine.service;

 import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;

 import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

 @Service
 public class NormParametersServiceImpl implements NormParametersService {
	
 	@Autowired
 	private NormParametersRepository normParametersRepository;
 	
 	@PersistenceContext
	private EntityManager entityManager;
 	
 	@Autowired
	private PlantsRepository plantsRepository;

 	@Override
 	public List<NormParameters> findAllByType(String type) {
 		return	normParametersRepository.findAllByType(type);
 	}

	@Override
 	public List<NormParameters> getAllGrades(String type) {
		String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(type));
		String viewName="vwScrn"+verticalName+"ConfigurationGrades";
 		return	getAllGradesByPlantId(type,viewName);
 	}
	
	public List<NormParameters> getAllGradesByPlantId(String plantFkId, String viewName) {
		try {
			String sql = "SELECT * FROM " + viewName + " WHERE Plant_FK_Id = :plantFkId";
	        Query query = entityManager.createNativeQuery(sql);
	        query.setParameter("plantFkId", plantFkId);

	        return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

 }
