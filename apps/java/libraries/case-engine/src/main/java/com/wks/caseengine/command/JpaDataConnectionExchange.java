package com.wks.caseengine.command;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
@Profile("jpa")
public class JpaDataConnectionExchange implements DataConnectionExchange {

	  @PersistenceContext
	    private EntityManager entityManager;

	    @Override
	    public JsonObject exportFromDatabase(Gson gson) {
	    	throw new RuntimeException("not implemented");
	    }

	    @Override
	    @Transactional
	    public void importToDatabase(JsonObject data, Gson gson) {
	    	throw new RuntimeException("not implemented");
	    }

}
