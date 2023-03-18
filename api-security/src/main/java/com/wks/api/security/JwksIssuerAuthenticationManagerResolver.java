package com.wks.api.security;

import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JwksIssuerAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {

	private String keycloakUrl;
	
	private final Cache cache = new ConcurrentMapCache("jwkSet");

	public JwksIssuerAuthenticationManagerResolver(String keycloakUrl) {
		super();
		this.keycloakUrl = keycloakUrl;
	}

	@Override
	public AuthenticationManager resolve(HttpServletRequest request) {
			String origin = request.getHeader("Origin");
			return new ResolvingAuthenticationManager(new RequestProps(origin, keycloakUrl, cache));
	}
	
	static class RequestProps  {
		String origin;
		String keycloack;
		Cache cache;
		
		public RequestProps(String origin, String keycloack, Cache cache) {
			super();
			this.origin = origin;
			this.keycloack = keycloack;
			this.cache = cache;
		}
	};

	static class ResolvingAuthenticationManager implements AuthenticationManager {

		private Converter<BearerTokenAuthenticationToken, String> issuerConverter;
		
		private RequestProps request;
		
		public ResolvingAuthenticationManager(RequestProps request) {
			this.request = request;
			this.issuerConverter = new JwtClaimIssuerConverter(request);
		}

		@Override
		public Authentication authenticate(Authentication authentication) throws AuthenticationException {
			BearerTokenAuthenticationToken token = (BearerTokenAuthenticationToken) authentication;
			
			String issuer = this.issuerConverter.convert(token);
			
			JwtAuthenticationManagerResolver authenticationManagerResolver = new JwtAuthenticationManagerResolver(request.cache);
			
			AuthenticationManager authenticationManager = authenticationManagerResolver.resolve(issuer);
			if (authenticationManager == null) {
				throw new InvalidBearerTokenException("Invalid issuer");
			}
			
			return authenticationManager.authenticate(authentication);
		}

	}

	static class JwtClaimIssuerConverter implements Converter<BearerTokenAuthenticationToken, String> {
		
		private RequestProps request;

		public JwtClaimIssuerConverter(RequestProps request) {
			this.request = request;
		}

		@Override
		public String convert(@NonNull BearerTokenAuthenticationToken authentication) {
			if (request.keycloack == "") {
				throw new InvalidBearerTokenException("Missing issuer");
			}
			
			try {
				String realm = extractTenantIdFromToken(authentication);
				String issueUrl = String.format("%s/realms/%s/protocol/openid-connect/certs", request.keycloack, realm);
				log.debug("issuer url {}", issueUrl);
				return issueUrl;
			} catch (Exception ex) {
				throw new InvalidBearerTokenException(ex.getMessage(), ex);
			}
		}

		private String extractTenantIdFromToken(BearerTokenAuthenticationToken authentication) {
			try {
				String token = authentication.getToken();
				JWTClaimsSet claims = JWTParser.parse(token).getJWTClaimsSet();
				return (String) claims.getClaim("org");
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
	}

	static class JwtAuthenticationManagerResolver implements AuthenticationManagerResolver<String> {

		private final Map<String, AuthenticationManager> authenticationManagers = new ConcurrentHashMap<>();

		private Cache cache;
				
		public JwtAuthenticationManagerResolver(Cache cache) {
			this.cache = cache;
		}

		@Override
		public AuthenticationManager resolve(String issuer) {
			AuthenticationManager authenticationManager = this.authenticationManagers.computeIfAbsent(issuer, (k) -> {
				log.debug("Constructing AuthenticationManager");
				log.debug("Resolved AuthenticationManager for issuer '{}'", issuer);
				
				JwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(issuer)
																							   .cache(cache)
																							   .build();
				
				return new JwtAuthenticationProvider(jwtDecoder)::authenticate;
			});
			
			return authenticationManager;
		}
		
	}

}