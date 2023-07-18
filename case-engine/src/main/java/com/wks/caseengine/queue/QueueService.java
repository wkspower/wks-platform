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
package com.wks.caseengine.queue;

import java.util.List;

public interface QueueService {

	void save(final Queue queue) throws Exception;

	Queue get(final String id) throws Exception;

	List<Queue> find() throws Exception;

	void delete(final String id) throws Exception;

	void update(final String id, final Queue queue) throws Exception;

}
