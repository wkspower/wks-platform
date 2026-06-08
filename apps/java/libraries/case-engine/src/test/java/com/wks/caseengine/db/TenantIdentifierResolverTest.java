package com.wks.caseengine.db;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.hibernate.cfg.AvailableSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.caseengine.tenancy.TenantResolver;

@ExtendWith(MockitoExtension.class)
public class TenantIdentifierResolverTest {

    @Mock
    private TenantResolver tenantResolver;

    @InjectMocks
    private TenantIdentifierResolver resolver;

    @Test
    void shouldReturnTenantIdWhenPresent() {
        // The JPA resolver delegates to the non-throwing resolveTenantOrDefault()
        // so Hibernate's bootstrap/DDL calls (no request context) never fail closed.
        when(tenantResolver.resolveTenantOrDefault()).thenReturn("tenant_123");

        String tenantId = resolver.resolveCurrentTenantIdentifier();
        assertEquals("tenant_123", tenantId);
    }

    @Test
    void shouldReturnResolvedTenantFromStrategy() {
        when(tenantResolver.resolveTenantOrDefault()).thenReturn("public");

        String tenantId = resolver.resolveCurrentTenantIdentifier();
        assertEquals("public", tenantId);
    }

    @Test
    void shouldCustomizeHibernateProperties() {
        Map<String, Object> properties = new HashedMap<String, Object>();

        resolver.customize(properties);

        assertNotNull(properties.get(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER));
    }

    @Test
    void shouldReturnFalseForValidateExistingCurrentSessions() {
        assertFalse(resolver.validateExistingCurrentSessions());
    }
}
