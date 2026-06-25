/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.caseengine.rest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.wks.caseengine.rest.config.security.InjectorTenantHandlerInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Autowired
	private InjectorTenantHandlerInterceptor tenantHandler;

	// Comma-separated origin allow-list for the case portal. The default lives in
	// the api-security library's api-security-defaults.properties (env-overridable
	// via WKS_CORS_ALLOWED_ORIGINS), so it isn't hardcoded here.
	@Value("${wks.cors.allowed-origins}")
	private String[] allowedOrigins;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins(allowedOrigins)
				.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
				.allowedHeaders("*");
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// The dev-token issuer endpoints (/dev-auth/**) are public and tenant-less;
		// running them through the tenant resolver only produces noise, so exclude them.
		registry.addInterceptor(tenantHandler).excludePathPatterns("/dev-auth/**");
	}

}
