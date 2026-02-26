package com.wks.caseengine.command;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
@ConditionalOnProperty(name = "database.type", havingValue = "jpa", matchIfMissing = false)
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
