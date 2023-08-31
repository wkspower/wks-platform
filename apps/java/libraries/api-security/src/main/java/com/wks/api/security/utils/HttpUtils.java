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
package com.wks.api.security.utils;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

public final class HttpUtils {

	public static String getHost(String base) {
		if (base == null || base.isBlank()) {
			return "";
		}

		try {
			String url = base;

			if (!url.contains("://")) {
				url = String.format("https://%s", base);
			}

			URL allowedOrigin = new URL(url);
			return allowedOrigin.getHost();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static String getSubdomain(String url, int pos, String defaultIfEmpty) {
		if (url == null || url.isBlank()) {
			return defaultIfEmpty;
		}

		String host = getHost(url);
		if (host.isBlank()) {
			return defaultIfEmpty;
		}

		String[] names = host.split("\\.");
		if (pos < 0 || pos >= names.length) {
			return defaultIfEmpty;
		}

		return names[pos];
	}

	public static String getMethod(HttpServletRequest request) {
		try {
			String method = request.getMethod();
			if (method == null) {
				return "";
			}

			return method.toUpperCase();
		} catch (java.lang.NoSuchMethodError e) {
			return "";
		}
	}

	public static String getPath(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		String[] paths = requestURI.replaceAll("^/|/$", "").split("/");
		return paths[0];
	}

}
