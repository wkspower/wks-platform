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
package com.wks.caseengine.cases.instance.email.repository;

import java.util.List;
import java.util.Optional;

import com.wks.caseengine.cases.instance.email.CaseEmail;
import com.wks.caseengine.repository.Repository;

public interface CaseEmailRepository extends Repository<CaseEmail> {

	List<CaseEmail> find(final Optional<String> caseInstanceBusinessKey);

}
