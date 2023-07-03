package com.wks.storage.service.digitalocean;

import java.time.ZonedDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.wks.storage.config.StorageConfig;
import com.wks.storage.driver.MinioClientDelegate;
import com.wks.storage.model.UploadFileUrl;
import com.wks.storage.service.BucketService;
import com.wks.storage.service.UploadService;

import io.minio.PostPolicy;

@Service("DigitalOceanUploadService")
public class DigitalOceanUploadService implements UploadService {

	@Autowired
	private StorageConfig config;

	@Autowired
	@Qualifier("DigitalOceanClient")
	private MinioClientDelegate client;

	@Autowired
	@Qualifier("DigitalOceanBucketService")
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
		if (dir != null && !dir.isBlank()) {
			objectName = bucketService.createObjectWithPath(dir, fileName);
		}

		PostPolicy policy = new PostPolicy(bucketName, ZonedDateTime.now().plusMinutes(5));
		policy.addEqualsCondition("key", objectName);
		policy.addStartsWithCondition("Content-Type", contentType.split("/")[0]);
		policy.addContentLengthRangeCondition(config.getUploadsFileMinSize(), config.getUploadsFileMaxSize());

		Map<String, String> formData = client.getPresignedPostFormData(policy);

		String port = config.getUploadsPort() > 0 ? ":" + config.getUploadsPort() : "";

		String callBackUrl = String.format("%s://%s.%s%s", config.getUploadsProtocol(), bucketName,
				config.getUploadsBackendUrl(), port);

		return new UploadFileUrl(callBackUrl, formData);
	}

	public void setConfig(StorageConfig config) {
		this.config = config;
	}

}
