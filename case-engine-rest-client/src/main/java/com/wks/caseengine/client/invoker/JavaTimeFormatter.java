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

package com.wks.caseengine.client.invoker;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Class that add parsing/formatting support for Java 8+ {@code OffsetDateTime}
 * class. It's generated for java clients when
 * {@code AbstractJavaCodegen#dateLibrary} specified as {@code java8}.
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-05-26T12:12:09.236578+01:00[Europe/Dublin]")
public class JavaTimeFormatter {

	private DateTimeFormatter offsetDateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	/**
	 * Get the date format used to parse/format {@code OffsetDateTime} parameters.
	 *
	 * @return DateTimeFormatter
	 */
	public DateTimeFormatter getOffsetDateTimeFormatter() {
		return offsetDateTimeFormatter;
	}

	/**
	 * Set the date format used to parse/format {@code OffsetDateTime} parameters.
	 *
	 * @param offsetDateTimeFormatter {@code DateTimeFormatter}
	 */
	public void setOffsetDateTimeFormatter(DateTimeFormatter offsetDateTimeFormatter) {
		this.offsetDateTimeFormatter = offsetDateTimeFormatter;
	}

	/**
	 * Parse the given string into {@code OffsetDateTime} object.
	 *
	 * @param str String
	 * @return {@code OffsetDateTime}
	 */
	public OffsetDateTime parseOffsetDateTime(String str) {
		try {
			return OffsetDateTime.parse(str, offsetDateTimeFormatter);
		} catch (DateTimeParseException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Format the given {@code OffsetDateTime} object into string.
	 *
	 * @param offsetDateTime {@code OffsetDateTime}
	 * @return {@code OffsetDateTime} in string format
	 */
	public String formatOffsetDateTime(OffsetDateTime offsetDateTime) {
		return offsetDateTimeFormatter.format(offsetDateTime);
	}
}
