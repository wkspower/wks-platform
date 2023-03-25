package com.wks.storage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.api.security.context.SecurityContextTenantHolder;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;

@Component
public class BucketServiceImpl implements BucketService {

	@Autowired
	private SecurityContextTenantHolder tenantHolder;

	@Autowired
	private MinioClient client;

	@Override
	public String createAssignedTenant() throws Exception {
		String bucketByTenant = tenantHolder.getTenantId().get();
		

		boolean found = client.bucketExists(BucketExistsArgs.builder()
																									.bucket(bucketByTenant)
																									.build());
		if (!found) {
			client.makeBucket(MakeBucketArgs.builder()
																		 .bucket(bucketByTenant)
																		 .build());
		}

		return bucketByTenant;
	}
	
	@Override
	public String createObjectWithPath(String dir, String fileName) throws Exception {
		return String.format("%s/%s", dir, fileName);
	}

}
