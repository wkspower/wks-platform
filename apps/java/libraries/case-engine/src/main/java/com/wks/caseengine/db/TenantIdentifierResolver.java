package com.wks.caseengine.db;

import java.util.Map;
import java.util.Optional;

import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import com.wks.api.security.context.SecurityContextTenantHolder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "database.type", havingValue = "jpa", matchIfMissing = false)
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver, HibernatePropertiesCustomizer {

	@Autowired
	private SecurityContextTenantHolder holder;

	@Override
	public String resolveCurrentTenantIdentifier() {
		Optional<String> tenantId = holder.getTenantId();

		if (!tenantId.isEmpty()) {
			log.debug("using tenate database {}", tenantId.get());
			return tenantId.get();
		}

		return "public";
	}

	@Override
	public void customize(Map<String, Object> hibernateProperties) {
		hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
	}

	@Override
	public @UnknownKeyFor @NonNull @Initialized boolean validateExistingCurrentSessions() {
		return false;
	}
	
}