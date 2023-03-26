package com.wks.storage.service.digitalocean;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.wks.storage.model.DownloadFileUrl;
import com.wks.storage.service.BucketService;
import com.wks.storage.service.DownloadService;

@Service("DigitalOceanDownloadService")
public class DigitalOceanDownloadService implements DownloadService {

	@Autowired
	private AmazonS3 client;
	
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

	private DownloadFileUrl createPresigned(String dir, String fileName, String contentType) throws Exception  {
		String bucketName = bucketService.createAssignedTenant();
		
		String objectName = fileName;
		if (dir != null  && !dir.isBlank()) {
			objectName = bucketService.createObjectWithPath(dir, fileName);
		}
		
		LocalDateTime dateTime = LocalDateTime.now().plus(Duration.of(10, ChronoUnit.MINUTES));
		
		Date tmfn = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
		
		URL url = client.generatePresignedUrl(bucketName, objectName, tmfn, HttpMethod.GET);

		return new DownloadFileUrl(url.toString());
	}

}
