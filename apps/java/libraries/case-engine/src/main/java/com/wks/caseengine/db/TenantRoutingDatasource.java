package com.wks.caseengine.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

import com.wks.caseengine.jpa.entity.TenantDatabase;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "database.type", havingValue = "jpa", matchIfMissing = false)
public class TenantRoutingDatasource extends AbstractRoutingDataSource {

	@Autowired
	private TenantIdentifierResolver tenantIdentifierResolver;
	
	@Autowired
	private HikariConfig globalConfig;
	
	@PostConstruct
	public void setup() {
		DataSource globalDataSource = createDataSource(globalConfig);
		
		setDefaultTargetDataSource(globalDataSource);
		
		HashMap<Object, Object> targetDataSources = new HashMap<>();
		
		getTenants(globalDataSource).forEach((tenant) -> {
			targetDataSources.put(tenant.getName(), createDataSourceFromTenant(tenant));
		});
		
		setTargetDataSources(targetDataSources);
	}

	protected DataSource createDataSourceFromTenant(TenantDatabase tenant) {
			HikariConfig cfg = new HikariConfig();
			cfg.setJdbcUrl(tenant.getJdbcUrl());
			cfg.setDriverClassName(globalConfig.getDriverClassName());
			cfg.setUsername(tenant.getJdbcUserName());
			cfg.setPassword(tenant.getJdbcPassword());
			cfg.setPoolName(String.format("Hikari-Tenant-%s-%s", tenant.getName(), tenant.getUid().toString()));
			cfg.setMaximumPoolSize(tenant.getPoolSize());
			cfg.setIdleTimeout(tenant.getIdleTimeout());
			cfg.setMinimumIdle(tenant.getMinIdle());
			cfg.setConnectionTimeout(tenant.getConnectionTimeout());
			cfg.setMaxLifetime(tenant.getMaxLifeTime());
			return new HikariDataSource(cfg);
	}

	protected List<TenantDatabase> getTenants(DataSource ds) {
		try {
			JdbcTemplate jdbc = new JdbcTemplate(ds);
			
			return jdbc.query("SELECT * FROM tenant_database", (rs, rowNum) -> {
			       TenantDatabase tenant = new TenantDatabase();
			        tenant.setUid(UUID.fromString(rs.getString("uid")));
			        tenant.setName(rs.getString("name"));
			        tenant.setJdbcUrl(rs.getString("jdbc_url"));
			        tenant.setJdbcUserName(rs.getString("jdbc_user_name"));
			        tenant.setJdbcPassword(rs.getString("jdbc_password"));
			        tenant.setPoolSize(rs.getInt("pool_size"));
			        tenant.setMinIdle(rs.getInt("min_idle"));
			        tenant.setIdleTimeout(rs.getLong("idle_timeout"));
			        tenant.setConnectionTimeout(rs.getLong("connection_timeout"));
			        tenant.setMaxLifeTime(rs.getLong("max_life_time"));
			        return tenant;
			});
		} catch (DataAccessException e) {
			log.warn("Tenant database not found", e);
			return Collections.emptyList();
		}
	}

	@Override
	protected String determineCurrentLookupKey() {
		return tenantIdentifierResolver.resolveCurrentTenantIdentifier();
	}

	protected DataSource createDataSource(HikariConfig hikariConfig) {
		return new HikariDataSource(hikariConfig);
	}
	
	public void setTenantIdentifierResolver(TenantIdentifierResolver tenantIdentifierResolver) {
		this.tenantIdentifierResolver = tenantIdentifierResolver;
	}
	
	public void setGlobalConfig(HikariConfig globalConfig) {
		this.globalConfig = globalConfig;
	}
	
}