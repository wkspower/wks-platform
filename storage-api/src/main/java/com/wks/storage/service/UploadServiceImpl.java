package com.wks.storage.service;

import java.time.ZonedDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.storage.configs.MinioConfiguration;
import com.wks.storage.model.UploadFileUrl;

import io.minio.MinioClient;
import io.minio.PostPolicy;

@Component
public class UploadServiceImpl implements UploadService {

	@Autowired
	private MinioClient client;
	
	@Autowired
	private MinioConfiguration configs;
	
	@Autowired
	private BucketService bucketService;
	
	@Override
	public UploadFileUrl createPresignedPostFormData(String fileName, String contentType) throws Exception {
		return createPresigned(null, fileName, contentType);
	}

	@Override
	public UploadFileUrl createPresignedPostFormData(String dir, String fileName, String contentType) throws Exception {
		return createPresigned(dir, fileName, contentType);
	}

	private UploadFileUrl createPresigned(String dir, String fileName, String contentType) throws Exception {
		String bucketName = bucketService.createAssignedTenant();
		
		String objectName = fileName;
		if (dir != null  && !dir.isBlank()) {
			objectName = bucketService.createObjectWithPath(dir, fileName);
		}
		
		PostPolicy policy = new PostPolicy(bucketName, ZonedDateTime.now().plusMinutes(5));
		policy.addEqualsCondition("key", objectName);
		policy.addStartsWithCondition("Content-Type", contentType.split("/")[0]);
		policy.addContentLengthRangeCondition(configs.getUploadsFileMinSize(), configs.getUploadsFileMaxSize());

		Map<String, String> formData = client.getPresignedPostFormData(policy);

		String callBackUrl = configs.getUploadsBackendUrl(bucketName);
		
		return new UploadFileUrl(callBackUrl, formData);
	}

}
