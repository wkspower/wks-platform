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
package com.wks.caseengine.rest.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.bson.BsonDocument;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.mongodb.MongoWriteException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteError;

/**
 * @author victor.franca
 */
public class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	public void shouldAnswerConflictWhenTheNaturalKeyIsAlreadyTaken() {
		ResponseEntity<ErrorResponse> response = handler.handleMongoWriteException(duplicateKey());

		// A 500 here would read as "save failed" in the portal, and the user would
		// retry — which is how duplicates piled up in the first place.
		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
	}

	@Test
	public void shouldNotLeakTheDriverMessageToTheClient() {
		ResponseEntity<ErrorResponse> response = handler.handleMongoWriteException(duplicateKey());

		assertEquals("A record with the same identifier already exists", response.getBody().getErrorMessage());
	}

	@Test
	public void shouldStillAnswerServerErrorForOtherWriteFailures() {
		MongoWriteException ex = new MongoWriteException(new WriteError(121, "Document failed validation", new BsonDocument()),
				new ServerAddress(), Collections.emptySet());

		ResponseEntity<ErrorResponse> response = handler.handleMongoWriteException(ex);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
	}

	private MongoWriteException duplicateKey() {
		return new MongoWriteException(
				new WriteError(11000, "E11000 duplicate key error collection: tenant.form index: key_1",
						new BsonDocument()),
				new ServerAddress(), Collections.emptySet());
	}

}
