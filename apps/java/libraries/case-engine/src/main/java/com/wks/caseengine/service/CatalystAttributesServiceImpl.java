package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.entity.CatalystAttributes;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.CatalystAttributesRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;

@Service
public class CatalystAttributesServiceImpl implements CatalystAttributesService{
	
	@Autowired
	private CatalystAttributesRepository catalystAttributesRepository;
	
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<CatalystAttributes> findAll() {
		return catalystAttributesRepository.findAll();
	}

	@Override
	public AOPMessageVM getDummySpValues() {
		
		List<List<Object[]>> obj=getProductionVolumnDataReport();
		
		for (List<Object[]> innerList : obj) {
		    for (Object[] row : innerList) {

		    	Object type=row[row.length - 1];
		    	System.out.println(type);
		    }
		}

		
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<List<Object[]>> getProductionVolumnDataReport() {
	    StoredProcedureQuery spq = entityManager
	        .createStoredProcedureQuery("usp_ReturnThreeDummySets");

	    boolean hasResults = spq.execute();

	    List<List<Object[]>> allResults = new ArrayList<>();

	    if (hasResults) {
	        allResults.add(spq.getResultList());
	    }

	    while (spq.hasMoreResults()) {
	        allResults.add(spq.getResultList());
	    }

	    return allResults;
	}


}
