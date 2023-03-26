package com.wks.storage.service.minio;

import java.time.ZonedDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.wks.storage.model.UploadFileUrl;
import com.wks.storage.service.BucketService;
import com.wks.storage.service.UploadService;

import io.minio.MinioClient;
import io.minio.PostPolicy;

@Service("MinioUploadService")
public class MinioUploadService implements UploadService {

	@Autowired
	private MinioClient client;
	
	@Autowired
	private MinioConfig configs;
	
	@Autowired
	@Qualifier("MinioBucketService")
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
