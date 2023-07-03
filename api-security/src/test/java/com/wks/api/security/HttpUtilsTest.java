package com.wks.api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.wks.api.security.utils.HttpUtils;

public class HttpUtilsTest {

	@Test
	public void shouldGetSubdomainByPosition() {
		assertEquals("localhost", HttpUtils.getSubdomain("http://localhost:9000", 0, "localhost"));
		assertEquals("sendgrid", HttpUtils.getSubdomain("new-case.sendgrid.wkspower.com", 1, "localhost"));
	}

	@Test
	public void shouldGetHost() {
		assertEquals("new-case.sendgrid.wkspower.com", HttpUtils.getHost("http://new-case.sendgrid.wkspower.com"));
	}

	@Test
	public void shouldGetEmptyStringWhenNullHost() {
		assertEquals("", HttpUtils.getHost(null));
		assertEquals("", HttpUtils.getHost(""));
	}

	@Test
	public void shouldGetMethodByRequest() {
		assertEquals("", HttpUtils.getMethod(new MockHttpServletRequest("", "http://localhost")));
		assertEquals("GET", HttpUtils.getMethod(new MockHttpServletRequest("get", "http://localhost")));
	}

}
