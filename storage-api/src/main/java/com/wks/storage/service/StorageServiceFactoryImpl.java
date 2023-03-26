package com.wks.storage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StorageServiceFactoryImpl implements StorageServiceFactory {
	
	@Autowired
	@Qualifier("MinioServiceFactory")
	private ServiceFactory minio;
	
	@Autowired
	@Qualifier("DigitalOceanServiceFactory")
	private ServiceFactory digitalOcean;
	
	@Value("${driver.storage.factory.type}")
	private String factoryType;
	
	@Override
	public ServiceFactory getFactory() {
		if ("minio".equals(factoryType)) {
			return minio;
		}
		
		if ("do".equals(factoryType)) {
			return digitalOcean;
		}
		
		throw new IllegalArgumentException(String.format("Factory name '%s' not found", factoryType));
	}

}
