package com.wks.caseengine.db;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.wks.api.security.context.SecurityContextTenantHolder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EngineMongoDatabaseFactory extends SimpleMongoClientDatabaseFactory {

    @Autowired
    private SecurityContextTenantHolder holder;
    
    public EngineMongoDatabaseFactory(MongoClient mongoClient, String globalDB) {
        super(mongoClient, globalDB);
    }

    @Override
    public MongoDatabase getMongoDatabase() {
        return getMongoClient().getDatabase(getTenantDatabase());
    }

    private String getTenantDatabase() {
        Optional<String> tenantId = holder.getTenantId();
        
        if (!tenantId.isEmpty()) {
        	log.debug("using tenate database {}", tenantId.get());
            return tenantId.get();
        }
        
		throw new IllegalArgumentException("Could't locate tenan database in session context holder");
    }
    
}
