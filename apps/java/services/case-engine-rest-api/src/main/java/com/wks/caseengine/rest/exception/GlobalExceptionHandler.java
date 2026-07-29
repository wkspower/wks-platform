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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.wks.caseengine.config.validation.ConfigValidationException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author victor.franca
 *
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(RestResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFoundException(RestResourceNotFoundException ex) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(RestInvalidArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(RestInvalidArgumentException ex) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(ConfigValidationException.class)
	public ResponseEntity<ErrorResponse> handleConfigValidationException(ConfigValidationException ex) {
		// Expected client error (config violates the Standard): log at warn, not error.
		log.warn("Rejected non-conforming config: {}", ex.getMessage());
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"Message not readable: " + ex.getMostSpecificCause().getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(MongoWriteException.class)
	public ResponseEntity<ErrorResponse> handleMongoWriteException(MongoWriteException ex) {
		if (ErrorCategory.DUPLICATE_KEY.equals(ex.getError().getCategory())) {
			// Expected client error: the natural key is already taken. Without this the
			// unique indexes on the config collections would surface as a 500, which the
			// portal reads as "save failed" — and the user retries.
			log.warn("Rejected duplicate record: {}", ex.getError().getMessage());
			ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT.getReasonPhrase(),
					"A record with the same identifier already exists");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
		}

		log.error("Mongo write error", ex);
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
				"Internal Server Error");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception ex) {
		log.error("Internal Error", ex);
		
		ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
				"Internal Server Error");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}
}
