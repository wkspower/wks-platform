package com.wks.caseengine.rest.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.caseengine.rest.mocks.MockSecurityContext;

@ExtendWith(MockitoExtension.class)
public class InjectorTenantHandlerInterceptorTest {
	
	@InjectMocks
	private InjectorTenantHandlerInterceptor handler;
	
	@Mock
	private SecurityContextTenantHolder tenantHolder;
	
	private MockHttpServletRequest request;

	@BeforeEach
	public void setup() {
		request = new MockHttpServletRequest();
	}
	
	@AfterEach
	private void teardown() {
		SecurityContextHolder.clearContext();
	}
	
	@Test
	public  void shouldInjectTenantIdToContextOnProcessRequest() throws Exception {
		SecurityContextHolder.setContext(new MockSecurityContext("wks", "localhost"));
		
		boolean result = handler.preHandle(request, null, null);
		
		assertTrue(result);
		verify(tenantHolder).setTenantId("wks");
	}
	
	@Test
	public  void shouldThrowExceptionWhenGettingTenantIdEmptyOrNullOnProcessRequest() throws Exception {
		SecurityContextHolder.setContext(new MockSecurityContext(" ", "localhost"));
		
		IllegalArgumentException throwable = assertThrowsExactly(IllegalArgumentException.class, () -> handler.preHandle(request, null, null));
		
		assertInstanceOf(IllegalArgumentException.class, throwable);
		assertEquals("Could't find tenantId by mail server dns ", throwable.getMessage());
	}

	@Test
	public  void shouldCleanerContextOnAfterCompletion() throws Exception {
		handler.afterCompletion(request, null, null, null);
		
		verify(tenantHolder).clear();
	}
	
}
