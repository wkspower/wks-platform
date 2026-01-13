 package com.wks.caseengine.service;

 import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.NormParameterDTO;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.VerticalsRepository;

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
 	
 	@Autowired
	private VerticalsRepository verticalRepository;
	
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
	        List<NormParameters> normParametersList=new ArrayList<>();
	        List<Object[]> obj= query.getResultList();
	        
	        for(Object[] row : obj) {
	        	NormParameters normParameters = new NormParameters();
	        	normParameters.setId(row[0] != null ? UUID.fromString(row[0].toString()) : null);
	        	normParameters.setName(row[1] != null ? row[1].toString() : "");
	        	normParameters.setDisplayName(row[2] != null ? row[2].toString() : "");
	        	normParameters.setNormParameterTypeFkId(row[8] != null ? UUID.fromString(row[8].toString()) : null);
	        	normParameters.setPlantFkId(row[9] != null ? UUID.fromString(row[9].toString()) : null);
	        	normParameters.setNormTypeFKId(row[10] != null ? Integer.parseInt(row[10].toString()) : null);
	        	normParameters.setDisplayOrder(row[12] != null ? Integer.parseInt(row[12].toString()) : null);
	        	normParameters.setIsEditable(row[13] != null ? Boolean.parseBoolean(row[13].toString()) : null);
	        	normParameters.setIsVisible(row[14] != null ? Boolean.parseBoolean(row[14].toString()) : null);
	        	normParametersList.add(normParameters);
	        	
	        }
	        return normParametersList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getNormParameters(String plantId, String year,String type) {
		List<NormParameters> normParametersList = normParametersRepository.findByPlantFkId(UUID.fromString(plantId));
		
		
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AOPMessageVM getAllProducts(String plantId, String year) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
		List<NormParameterDTO> productDTOList = new ArrayList<>();
		String viewName="vw"+vertical.getName()+"GradeDetails";
		String sql = "SELECT * FROM " + viewName + " WHERE Plant_FK_Id = :plantFkId and aopYear = :year";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("plantFkId", plantId);
        query.setParameter("year", year);
        
        List<Object[]> data= query.getResultList();
		for (Object[] obj : data) {
			NormParameterDTO normParameterDTO = new NormParameterDTO();
	
			// Convert UUID to String (Avoid using Long)
			normParameterDTO.setId(obj[0] != null ? obj[0].toString() : null);
	
			normParameterDTO.setName((String) obj[1]);
			normParameterDTO.setDisplayName((String) obj[2]);
			productDTOList.add(normParameterDTO);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setData(productDTOList);
		aopMessageVM.setMessage("Data fetched successfully");
		return aopMessageVM;
		
	}

 }
