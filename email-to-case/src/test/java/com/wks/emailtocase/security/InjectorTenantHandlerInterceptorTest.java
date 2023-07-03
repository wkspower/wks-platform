package com.wks.emailtocase.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockPart;
import org.springframework.security.core.context.SecurityContextHolder;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.emailtocase.mocks.MockSecurityContext;

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
	public void shouldInjectTenantIdToContextOnProcessRequest() throws Exception {
		SecurityContextHolder.setContext(new MockSecurityContext("wks", "localhost"));
		request.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
		request.setParameter("to", "client@new-case.myorg.sendgrid.com");
		request.addPart(new MockPart("to", "client@new-case.myorg.sendgrid.com".getBytes()));

		boolean result = handler.preHandle(request, null, null);

		assertTrue(result);
		verify(tenantHolder).setTenantId("myorg");
	}

	@Test
	public void shouldInjectDefaultTenantIdIfPrefixDnsNotFoundOnProcessRequest() throws Exception {
		SecurityContextHolder.setContext(new MockSecurityContext(" ", "localhost"));
		request.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
		request.setParameter("to", "client@new-case.sendgrid.com");
		request.addPart(new MockPart("to", "client@new-case.sendgrid.com".getBytes()));

		IllegalArgumentException assertThrows = assertThrows(IllegalArgumentException.class,
				() -> handler.preHandle(request, null, null));

		assertEquals("Invalid origin declared on 'to', tenantId could not to be 'sendgrid'", assertThrows.getMessage());
		verifyNoInteractions(tenantHolder);
	}

	@Test
	public void shouldCleanerContextOnAfterCompletion() throws Exception {
		handler.afterCompletion(request, null, null, null);

		verify(tenantHolder).clear();
	}

}
