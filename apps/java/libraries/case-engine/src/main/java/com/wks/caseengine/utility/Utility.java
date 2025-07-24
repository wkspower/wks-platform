package com.wks.caseengine.utility;

import java.util.Collections;
import java.util.List;
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


    
}
