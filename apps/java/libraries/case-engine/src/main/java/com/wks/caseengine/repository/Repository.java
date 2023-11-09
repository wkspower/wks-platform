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
package com.wks.caseengine.repository;

import java.util.List;

public interface Repository<T> {

	List<T> find();

	T get(final String id) throws DatabaseRecordNotFoundException;

	void save(final T object);

	void update(final String id, final T object) throws DatabaseRecordNotFoundException;

	void delete(final String id) throws DatabaseRecordNotFoundException;

}
