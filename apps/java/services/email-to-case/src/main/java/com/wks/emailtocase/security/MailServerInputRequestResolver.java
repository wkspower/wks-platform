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
package com.wks.emailtocase.security;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.support.MultipartResolutionDelegate;

import com.wks.api.security.HandlerInputResolver;
import com.wks.api.security.utils.HttpUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

public final class MailServerInputRequestResolver implements HandlerInputResolver {

	@Override
	public Map<String, Object> resolver(HttpServletRequest request, Authentication authentication) {
		return inputResolver(request, authentication);
	}

	private Map<String, Object> inputResolver(HttpServletRequest request, Authentication authentication) {
		Map<String, String> formData = getParamsFromMultipartFormDataRequest(request);
		String origin = "";
		String org = "";

		if (!formData.isEmpty()) {
			if (formData.get("to") != null && !formData.get("to").isBlank()) {
				origin = formData.get("to").split("@")[1];
			}

			org = HttpUtils.getSubdomain(origin, 1, "");
			if (org.equalsIgnoreCase("sendgrid")) {
				throw new IllegalArgumentException(
						"Invalid origin declared on 'to', tenantId could not to be '" + org + "'");
			}
		}

		Map<String, String> headers = new HashMap<>();
		for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
			String header = headerNames.nextElement();
			headers.put(header, request.getHeader(header));
		}

		Map<String, Object> input = new HashMap<>();
		input.put("method", HttpUtils.getMethod(request));
		input.put("host", origin);
		input.put("org", org);
		input.put("path", HttpUtils.getPath(request));

		return input;
	}

	private Map<String, String> getParamsFromMultipartFormDataRequest(HttpServletRequest request) {
		try {
			Map<String, String> parameters = new HashMap<>();

			if (MultipartResolutionDelegate.isMultipartRequest(request)) {
				Collection<Part> parts = request.getParts();
				for (Part part : parts) {
					String value = request.getParameter(part.getName());
					parameters.put(part.getName(), value);
				}
			}

			return parameters;
		} catch (ServletException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
