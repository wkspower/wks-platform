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
package com.wks.caseengine.record.type;

import java.util.List;

public interface RecordTypeService {

	void save(final RecordType recordType) throws Exception;

	RecordType get(final String id) throws Exception;

	List<RecordType> find() throws Exception;

	void delete(final String id) throws Exception;

	void update(final String id, final RecordType recordType) throws Exception;

}
