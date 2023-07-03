package com.wks.caseengine.pagination.mongo;

import org.springframework.util.Base64Utils;

import com.wks.caseengine.pagination.PaginationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DigestUtils {

	public static String decode(Object token) {
		if (token == null) {
			return null;
		}

		try {
			byte[] decrypted = Base64Utils.decodeFromUrlSafeString(token.toString());
			return new String(decrypted);
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
			return Base64Utils.encodeToUrlSafeString(token.toString().getBytes());
		} catch (Exception e) {
			log.error("Error encrypting token", e);
			throw new PaginationException("Error encrypting token", e);
		}
	}

}
