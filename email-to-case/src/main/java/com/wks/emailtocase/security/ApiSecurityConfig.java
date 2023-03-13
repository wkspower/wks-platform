package com.wks.emailtocase.security;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.wks.api.security.NopDecisionVoter;
import com.wks.api.security.OpenPolicyAuthzEnforcer;

@Configuration
public class ApiSecurityConfig {
	
	@Value("${opa.url}")
	private String opaUrl;
	
	@Value("${security.global.disabled}")
	private boolean isSecurityDisabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    	if (isSecurityDisabled) {
    		http.cors()
    		.and()
    		.csrf().disable()
    		.authorizeRequests()
    		.anyRequest()
    		.permitAll(); 
    		return http.build();
    	}
    	
    	 http.cors()
    	 		.and()
    	 		.csrf().disable()
    	 		.authorizeRequests(authz -> authz
	    	 			.antMatchers("/healthCheck")
	    	 			.hasAnyRole()
	    	            .anyRequest()
	    	            .authenticated()
	    	            .accessDecisionManager(accessDecisionManager())
    	 		)
    	        .oauth2ResourceServer(oauth2 -> oauth2.jwt());
    	 
        return http.build();
    }
    
    @Bean
	public AccessDecisionManager accessDecisionManager() {
    	if (isSecurityDisabled) {
    		return new UnanimousBased(Arrays.asList(new NopDecisionVoter()));
    	}
    	
		return new UnanimousBased(Arrays.asList(new OpenPolicyAuthzEnforcer(opaUrl)));
	}
    
}