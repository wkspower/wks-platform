package com.wks.storage.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.storage.model.DownloadFileUrl;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;

@Component
public class DownloadServiceImpl implements DownloadService {

	@Autowired
	private MinioClient client;
	
	@Autowired
	private BucketService bucketService;
	
	@Override
	public DownloadFileUrl createPresignedObjectUrl(String fileName, String contentType) throws Exception {
		return createPresigned(null, fileName, contentType);
	}
	
	@Override
	public DownloadFileUrl createPresignedObjectUrl(String dir, String fileName, String contentType) throws Exception {
		return createPresigned(dir, fileName, contentType);
	}

	private DownloadFileUrl createPresigned(String dir, String fileName, String contentType) throws Exception  {
		Map<String, String> params = new HashMap<String, String>();
		params.put("response-content-type", contentType);
		
		String bucketName = bucketService.createAssignedTenant();
		
		String objectName = fileName;
		if (dir != null  && !dir.isBlank()) {
			objectName = bucketService.createObjectWithPath(dir, fileName);
		}
		
		GetPresignedObjectUrlArgs signed =
				GetPresignedObjectUrlArgs.builder()
																.method(Method.GET)
																.bucket(bucketName)
																.object(objectName)
																.expiry(1, TimeUnit.MINUTES)
																.extraQueryParams(params)
																.build();

		String url = client.getPresignedObjectUrl(signed);

		return new DownloadFileUrl(url);
	}

}
