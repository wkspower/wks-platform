package com.wks.storage.service.digitalocean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.storage.config.StorageConfig;
import com.wks.storage.driver.MinioClientDelegate;
import com.wks.storage.service.BucketService;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;

@Service("DigitalOceanBucketService")
public class DigitalOceanBucketService implements BucketService {

	@Autowired
	private SecurityContextTenantHolder tenantHolder;

	@Autowired
	private StorageConfig config;

	@Autowired
	@Qualifier("DigitalOceanClient")
	private MinioClientDelegate client;

	@Override
	public String createAssignedTenant() throws Exception {
		String bucketName = String.format("%s-%s", config.getBucketPrefix(), tenantHolder.getTenantId().get());

		boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
		if (!found) {
			client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
		}

		return bucketName;
	}

	@Override
	public String createObjectWithPath(String dir, String fileName) throws Exception {
		return String.format("%s/%s", dir, fileName);
	}

	public void setConfig(StorageConfig config) {
		this.config = config;
	}

}
