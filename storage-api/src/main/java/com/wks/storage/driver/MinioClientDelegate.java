package com.wks.storage.driver;

import java.util.Map;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.PostPolicy;

public interface MinioClientDelegate {

	boolean bucketExists(BucketExistsArgs args);

	void makeBucket(MakeBucketArgs args);

	String getPresignedObjectUrl(GetPresignedObjectUrlArgs args);

	Map<String, String> getPresignedPostFormData(PostPolicy policy);

}
