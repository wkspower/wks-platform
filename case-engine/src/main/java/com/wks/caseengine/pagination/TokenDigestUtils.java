package com.wks.caseengine.pagination;

import org.springframework.util.Base64Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TokenDigestUtils {
	
	private  TokenDigestUtils() {
		super();
	}

	public static String decode(String token) {
		try {
			byte[] decrypted = Base64Utils.decodeFromUrlSafeString(token);
			return new String(decrypted);
		} catch (Exception e) {
			log.error("Error encrypting token", e);
			throw new QueryPaginatorException("Error decrypting token", e);
		}
	}

	public static String encode(String token) {
		try {
			return Base64Utils.encodeToUrlSafeString(token.getBytes());
		} catch (Exception e) {
			log.error("Error encrypting token", e);
			throw new QueryPaginatorException("Error encrypting token", e);
		}
	}
	
}
