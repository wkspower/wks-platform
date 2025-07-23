package com.wks.caseengine.utility;

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


    
}
