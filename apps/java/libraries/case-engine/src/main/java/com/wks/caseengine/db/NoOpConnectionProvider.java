package com.wks.caseengine.db;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "database.type", havingValue = "jpa", matchIfMissing = false)
public class NoOpConnectionProvider implements MultiTenantConnectionProvider, HibernatePropertiesCustomizer {

	private static final long serialVersionUID = 1L;
	
	@Autowired
	private DataSource dataSource;

	@Override
	public Connection getAnyConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public void releaseAnyConnection(Connection connection) throws SQLException {
		connection.close();
	}

	@Override
	public boolean supportsAggressiveRelease() {
		return false;
	}

	@Override
	public boolean isUnwrappableAs(Class<?> aClass) {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> aClass) {
		throw new UnsupportedOperationException("Can't unwrap this.");
	}

	@Override
	public void customize(Map<String, Object> hibernateProperties) {
		hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, this);
	}

	@Override
	public Connection getConnection(String tenantIdentifier) throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
		connection.close();
	}

}