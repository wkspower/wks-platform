package com.wks.storage.service.digitalocean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.wks.storage.driver.MinioClientDelegate;
import com.wks.storage.model.DownloadFileUrl;
import com.wks.storage.service.BucketService;
import com.wks.storage.service.DownloadService;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;

@Service("DigitalOceanDownloadService")
public class DigitalOceanDownloadService implements DownloadService {

	@Autowired
	@Qualifier("DigitalOceanClient")
	private MinioClientDelegate client;

	@Autowired
	@Qualifier("DigitalOceanBucketService")
	private BucketService bucketService;

	@Override
	public DownloadFileUrl createPresignedObjectUrl(String fileName, String contentType) throws Exception {
		return createPresigned(null, fileName, contentType);
	}

	@Override
	public DownloadFileUrl createPresignedObjectUrl(String dir, String fileName, String contentType) throws Exception {
		return createPresigned(dir, fileName, contentType);
	}

	private DownloadFileUrl createPresigned(String dir, String fileName, String contentType) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("response-content-type", contentType);

		String bucketName = bucketService.createAssignedTenant();

		String objectName = fileName;
		if (dir != null && !dir.isBlank()) {
			objectName = bucketService.createObjectWithPath(dir, fileName);
		}

		GetPresignedObjectUrlArgs signed = GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(bucketName)
				.object(objectName).expiry(1, TimeUnit.MINUTES).extraQueryParams(params).build();

		String url = client.getPresignedObjectUrl(signed);

		return new DownloadFileUrl(url);
	}

}
