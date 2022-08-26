package com.wks.rest.client.auth;

import com.wks.rest.client.WksHttpRequest;

/**
 * @author victor.franca
 *
 */
public interface TokenHttpPostRequestFactory {

	public WksHttpRequest create();
}
