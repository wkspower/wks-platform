package com.wks.api.security;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;

public interface HandlerInputResolver {
	
	Map<String, Object> resolver(HttpServletRequest request, Authentication authentication);

}
