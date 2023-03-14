package com.wks.api.security;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

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

	public JwksIssuerAuthenticationManagerResolver(String keycloakUrl) {
		super();
		this.keycloakUrl = keycloakUrl;
	}

	@Override
	public AuthenticationManager resolve(HttpServletRequest issuer) {
			return new ResolvingAuthenticationManager(keycloakUrl);
	}

	static class ResolvingAuthenticationManager implements AuthenticationManager {

		private Converter<BearerTokenAuthenticationToken, String> issuerConverter;;
		
		public ResolvingAuthenticationManager(String keycloakUrl) {
			this.issuerConverter = new JwtClaimIssuerConverter(keycloakUrl);
		}

		@Override
		public Authentication authenticate(Authentication authentication) throws AuthenticationException {
			BearerTokenAuthenticationToken token = (BearerTokenAuthenticationToken) authentication;
			
			String issuer = this.issuerConverter.convert(token);
			
			JwtAuthenticationManagerResolver authenticationManagerResolver = new JwtAuthenticationManagerResolver();
			
			AuthenticationManager authenticationManager = authenticationManagerResolver.resolve(issuer);
			if (authenticationManager == null) {
				throw new InvalidBearerTokenException("Invalid issuer");
			}
			
			return authenticationManager.authenticate(authentication);
		}

	}

	static class JwtClaimIssuerConverter implements Converter<BearerTokenAuthenticationToken, String> {

		private String keycloakUrl;

		public JwtClaimIssuerConverter(String keycloakUrl) {
			this.keycloakUrl = keycloakUrl;
		}

		@Override
		@SuppressWarnings("unchecked")
		public String convert(@NonNull BearerTokenAuthenticationToken authentication) {
			if (keycloakUrl == "") {
				throw new InvalidBearerTokenException("Missing issuer");
			}
			
			try {
				String token = authentication.getToken();
				JWTClaimsSet claimsSet  = JWTParser.parse(token).getJWTClaimsSet();
				
				String origin = ((List<String>) claimsSet.getClaim("allowed-origins")).get(0);
				URL url = new URL(origin);
				String hostname = url.getHost();
	
				String realm = "wks-platform";
				if (hostname.contains(".wkspower.")) {
					if (!hostname.startsWith("app")) {
						realm = hostname.substring(0, hostname.indexOf('.'));
					}
				}
				
				Object org = claimsSet.getClaim("org");
				if (org == null) {
					log.error("could not locate org by token");
					throw new 	InvalidBearerTokenException("could not locate org by token");
				}
				
				if (!realm.equals(org)) {
					log.error("invalid org name when compared with dns prefix. it expected '{}' but was '{}'", org, realm);
					throw new 	InvalidBearerTokenException("invalid org name when compared with dns prefix");
				}
				
				String issueUrl = String.format("%s/realms/%s/protocol/openid-connect/certs", keycloakUrl, realm);
				log.info("issuer url {}", issueUrl);
				
				return issueUrl;
			} catch (Exception ex) {
				throw new InvalidBearerTokenException(ex.getMessage(), ex);
			}
		}
	}

	static class JwtAuthenticationManagerResolver implements AuthenticationManagerResolver<String> {

		private final Map<String, AuthenticationManager> authenticationManagers = new ConcurrentHashMap<>();

		@Override
		public AuthenticationManager resolve(String issuer) {
			AuthenticationManager authenticationManager = this.authenticationManagers.computeIfAbsent(issuer, (k) -> {
				log.info("Constructing AuthenticationManager");
				log.info("Resolved AuthenticationManager for issuer '{}'", issuer);
				JwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(issuer).build();
				return new JwtAuthenticationProvider(jwtDecoder)::authenticate;
			});
			
			return authenticationManager;
		}
		
	}

}