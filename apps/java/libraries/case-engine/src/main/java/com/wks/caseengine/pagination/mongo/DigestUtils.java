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
package com.wks.caseengine.pagination.mongo;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.wks.caseengine.pagination.PaginationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DigestUtils {

	public static String decode(Object token) {
		if (token == null) {
			return null;
		}

		try {
			byte[] decrypted = Base64.getUrlDecoder().decode(token.toString());
			return new String(decrypted, StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.error("Error encrypting token", e);
			throw new PaginationException("Error decrypting token", e);
		}
	}

	public static String encode(Object token) {
		if (token == null) {
			return null;
		}

		try {
			return Base64.getUrlEncoder().encodeToString(token.toString().getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			log.error("Error encrypting token", e);
			throw new PaginationException("Error encrypting token", e);
		}
	}

}
