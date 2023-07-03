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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.util.StdDateFormat;

public class RFC3339DateFormat extends DateFormat {
	private static final long serialVersionUID = 1L;
	private static final TimeZone TIMEZONE_Z = TimeZone.getTimeZone("UTC");

	private final StdDateFormat fmt = new StdDateFormat().withTimeZone(TIMEZONE_Z).withColonInTimeZone(true);

	public RFC3339DateFormat() {
		this.calendar = new GregorianCalendar();
		this.numberFormat = new DecimalFormat();
	}

	@Override
	public Date parse(String source) {
		return parse(source, new ParsePosition(0));
	}

	@Override
	public Date parse(String source, ParsePosition pos) {
		return fmt.parse(source, pos);
	}

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
		return fmt.format(date, toAppendTo, fieldPosition);
	}

	@Override
	public Object clone() {
		return super.clone();
	}
}