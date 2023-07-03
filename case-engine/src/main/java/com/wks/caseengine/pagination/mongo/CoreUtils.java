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
