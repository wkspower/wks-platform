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
package com.wks.storage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StorageServiceFactoryImpl implements StorageServiceFactory {

	@Autowired(required = false)
	@Qualifier("MinioServiceFactory")
	private ServiceFactory minio;

	@Autowired(required = false)
	@Qualifier("DigitalOceanServiceFactory")
	private ServiceFactory digitalOcean;

	@Autowired(required = false)
	@Qualifier("FilesystemServiceFactory")
	private ServiceFactory filesystem;

	@Value("${driver.storage.factoryclass}")
	private String factoryClass;

	@Override
	public ServiceFactory getFactory() {
		return getFactory(factoryClass);
	}

	@Override
	public ServiceFactory getFactory(String driver) {
		if ("minio".equals(driver)) {
			return requireLoaded(minio, driver);
		}

		if ("do".equals(driver)) {
			return requireLoaded(digitalOcean, driver);
		}

		if ("filesystem".equals(driver)) {
			return requireLoaded(filesystem, driver);
		}

		throw new IllegalArgumentException(String.format("Factory name '%s' not found", driver));
	}

	private static ServiceFactory requireLoaded(ServiceFactory factory, String driver) {
		if (factory == null) {
			throw new IllegalStateException(String.format(
					"Storage driver '%s' beans are not loaded; check driver.storage.factoryclass", driver));
		}
		return factory;
	}

}
