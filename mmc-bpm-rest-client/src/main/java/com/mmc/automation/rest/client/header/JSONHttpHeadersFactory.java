package com.mmc.automation.rest.client.header;

import java.util.Collections;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * @author victor.franca
 *
 */
@Component
public class JSONHttpHeadersFactory implements HttpHeadersFactory {

	@Override
	public HttpHeaders create() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		return headers;
	}

}
