package com.mmc.bpm.rest.client.auth;

import com.mmc.bpm.rest.client.MmcHttpRequest;

/**
 * @author victor.franca
 *
 */
public interface TokenHttpPostRequestFactory {

	public MmcHttpRequest create();
}
