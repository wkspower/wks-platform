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
package com.wks.caseengine.loader.utils;

import java.security.SecureRandom;
import java.util.Random;

public class SecretGenerator {

	private static final char[] DEFAULT_CODEC = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_"
			.toCharArray();

	private Random random = new SecureRandom();

	private int length;

	public static String create(int size) {
		return new SecretGenerator(size).generate();
	}

	public SecretGenerator(int length) {
		this.length = length;
	}

	public String generate() {
		byte[] verifierBytes = new byte[length];
		random.nextBytes(verifierBytes);
		return getAuthorizationCodeString(verifierBytes);
	}

	protected String getAuthorizationCodeString(byte[] verifierBytes) {
		char[] chars = new char[verifierBytes.length];
		for (int i = 0; i < verifierBytes.length; i++) {
			chars[i] = DEFAULT_CODEC[((verifierBytes[i] & 0xFF) % DEFAULT_CODEC.length)];
		}
		return new String(chars);
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	public void setLength(int length) {
		if (length <= 0) {
			throw new IllegalArgumentException("length must be greater than 0");
		}
		this.length = length;
	}

}