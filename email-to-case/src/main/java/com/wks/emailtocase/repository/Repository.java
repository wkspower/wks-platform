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
package com.wks.emailtocase.repository;

import java.util.List;

public interface Repository<T> {

	List<T> find() throws Exception;

	T get(final String id) throws Exception;

	void save(final T object) throws Exception;

	void update(final String id, final T object) throws Exception;

	void delete(final String id) throws Exception;

}
