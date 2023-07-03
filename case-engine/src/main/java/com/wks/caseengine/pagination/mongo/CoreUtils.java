package com.wks.caseengine.pagination.mongo;

import org.bson.types.ObjectId;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class CoreUtils {

	public static Object toObjectId(Object id) {
		try {
			return new ObjectId(id.toString());
		} catch (IllegalArgumentException e) {
			log.error("Error to convert objectId", e);
			return id;
		}
	}

	public static Object decode(Object value) {
		String decoded = DigestUtils.decode(value);
		try {
			return new ObjectId(decoded);
		} catch (Exception e) {
			log.error("Error to decode objectId", e);
			return decoded;
		}
	}

	public static String encode(Object value) {
		return DigestUtils.encode(value);
	}

}
