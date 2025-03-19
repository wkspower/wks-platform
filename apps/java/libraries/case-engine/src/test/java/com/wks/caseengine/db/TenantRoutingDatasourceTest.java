package com.wks.caseengine.db;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.caseengine.entity.TenantDatabase;
import com.zaxxer.hikari.HikariConfig;

@ExtendWith(MockitoExtension.class)
public class TenantRoutingDatasourceTest {

    @Mock
    private TenantIdentifierResolver tenantIdentifierResolver;

    @Mock
    private HikariConfig globalConfig;
    
    @Mock
    private DataSource dataSource;

    private TenantRoutingDatasource tenantRoutingDatasource;
    
    @BeforeEach
	public void setup() {
    	tenantRoutingDatasource = new TenantRoutingDatasource() {
    		@Override
    		protected DataSource createDataSource(HikariConfig hikariConfig) {
    			return dataSource;
    		}
    		
    		@Override
    		protected List<TenantDatabase> getTenants(DataSource ds) {
    			return Collections.emptyList();
    		}
    	};
	}

    @Test()
    void shouldCreateForOnlyDefaultTargetDataSourcesOnInit() {
    	tenantRoutingDatasource.setup();
    	tenantRoutingDatasource.afterPropertiesSet();
    	
        assertNotNull(tenantRoutingDatasource.getResolvedDefaultDataSource());
    }

    @Test
    void shouldCreateDefaultTargetDataSourcesAndTenantsOnInit() {
        TenantDatabase tenant = new TenantDatabase();
        tenant.setUid(UUID.randomUUID());
        tenant.setName("tenant_1");
        tenant.setJdbcUrl("jdbc:h2:mem:tenant1");
        tenant.setJdbcUserName("sa");
        tenant.setJdbcPassword("password");
        tenant.setPoolSize(10);
        tenant.setMinIdle(2);
        tenant.setIdleTimeout(30000L);
        tenant.setConnectionTimeout(30000L);
        tenant.setMaxLifeTime(1800000L);
        
    	tenantRoutingDatasource = new TenantRoutingDatasource() {
    		@Override
    		protected DataSource createDataSource(HikariConfig hikariConfig) {
    			return dataSource;
    		}
    		
    		@Override
    		protected List<TenantDatabase> getTenants(DataSource ds) {
    			return Arrays.asList(tenant);
    		}
    		
    		@Override
    		protected DataSource createDataSourceFromTenant(TenantDatabase tenant) {
    			return dataSource;
    		}
    	};

    	tenantRoutingDatasource.setup();
    	tenantRoutingDatasource.afterPropertiesSet();
 
    	assertNotNull(tenantRoutingDatasource.getResolvedDefaultDataSource());
    	assertNotNull(tenantRoutingDatasource.getResolvedDataSources());
    }
    
}
