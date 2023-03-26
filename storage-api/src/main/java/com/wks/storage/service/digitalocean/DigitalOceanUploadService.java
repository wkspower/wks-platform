package com.wks.storage.service.digitalocean;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.wks.storage.model.UploadFileUrl;
import com.wks.storage.service.BucketService;
import com.wks.storage.service.UploadService;

import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service("DigitalOceanUploadService")
public class DigitalOceanUploadService implements UploadService {

	@Autowired
	private AmazonS3 client;
	
	@Autowired
	private S3Presigner presignerClient;
	
	@Autowired
	private DigitalOceanConfig configs;
	
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
		if (dir != null  && !dir.isBlank()) {
			objectName = bucketService.createObjectWithPath(dir, fileName);
		}
		
		PutObjectRequest objectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(objectName)
            .contentType(contentType)
            .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(java.time.Duration.ofMinutes(10))
            .putObjectRequest(objectRequest)
            .build();

        PresignedPutObjectRequest presignedRequest = presignerClient.presignPutObject(presignRequest);
        Map<String, List<String>> headers = presignedRequest.signedHeaders();
        URL url = presignedRequest.url();
        
//		PostPolicy policy = new PostPolicy(bucketName, ZonedDateTime.now().plusMinutes(5));
//		policy.addEqualsCondition("key", objectName);
//		policy.addStartsWithCondition("Content-Type", contentType.split("/")[0]);
//		policy.addContentLengthRangeCondition(configs.getUploadsFileMinSize(), configs.getUploadsFileMaxSize());
//		Map<String, String> formData = client.getPresignedPostFormData(policy);
//		String callBackUrl = configs.getUploadsBackendUrl(bucketName);
        
        Map<String, String> formData = new HashMap<String, String>();
        for ( Map.Entry<String, List<String>> e : headers.entrySet()) {
        	formData.put(e.getKey(), e.getValue().get(0));
        }
		
		return new UploadFileUrl(url.toString(), formData);
	}

}
