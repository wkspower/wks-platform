package com.wks.caseengine.utility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class Utility {


        public static String getUserName() {
        	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    		String userId=null;
    		if (authentication instanceof JwtAuthenticationToken) {
    		    JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
    		    Jwt jwt = jwtAuth.getToken();
    		    userId = jwt.getClaimAsString("preferred_username"); // or "preferred_username"
    		}	
    		return userId;
        }

		public static Map<String, List<Map<String, Object>>> groupByNormParameterTypeName(List<Map<String, Object>> inputDataList) {
        if (inputDataList == null) return Collections.emptyMap();

        return inputDataList.stream()
                .filter(map -> map.get("normParameterTypeName") != null)
                .collect(Collectors.groupingBy(
                        map -> map.get("normParameterTypeName").toString()
                ));
    }

	public static List<String> getAcademicYearMonths(String year) {
		List<String> months = new ArrayList<>();
		int startYear = Integer.parseInt(year.substring(0, 4));
		int nextYear = startYear + 1;

		// Apr to Dec of startYear
		for (int month = 4; month <= 12; month++) {
			String label = formatMonthYear(month, startYear);
			months.add(label);
		}

		// Jan to Mar of nextYear
		for (int month = 1; month <= 3; month++) {
			String label = formatMonthYear(month, nextYear);
			months.add(label);
		}

		return months;
	}
	
	private static String formatMonthYear(int month, int year) {
		LocalDate date = LocalDate.of(year, month, 1);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH);
		return date.format(formatter);
	}
    
}
