package com.wks.storage.service.digitalocean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.storage.service.BucketService;

@Service("DigitalOceanBucketService")
public class DigitalOceanBucketService implements BucketService {

	@Autowired
	private SecurityContextTenantHolder tenantHolder;

	@Autowired
	private AmazonS3 client;

	@Autowired
	private DigitalOceanConfig config;
	
	@Override
	public String createAssignedTenant() throws Exception {
		String bucketName = String.format("%s-%s", config.getBucketPrefix(), tenantHolder.getTenantId().get());

		if (client.doesBucketExist(bucketName)) {
			return bucketName;
		}
		
		client.createBucket(bucketName);

		return bucketName;
	}
	
	@Override
	public String createObjectWithPath(String dir, String fileName) throws Exception {
		return String.format("%s/%s", dir, fileName);
	}

}
