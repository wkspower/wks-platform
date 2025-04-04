/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.storage.driver;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import com.wks.storage.exception.StorageException;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PostPolicy;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

public class MinioClientDelegateImpl implements MinioClientDelegate {

	private MinioClient delegate;

	public MinioClientDelegateImpl(MinioClient delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean bucketExists(BucketExistsArgs args) {
		try {
			return delegate.bucketExists(args);
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IOException e) {
			throw new StorageException(e);
		}
	}

	@Override
	public void makeBucket(MakeBucketArgs args) {
		try {
			delegate.makeBucket(args);
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IOException e) {
			throw new StorageException(e);
		}
	}

	@Override
	public String getPresignedObjectUrl(GetPresignedObjectUrlArgs args) {
		try {
			return delegate.getPresignedObjectUrl(args);
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | XmlParserException | ServerException
				| IOException e) {
			throw new StorageException(e);
		}
	}

	@Override
	public Map<String, String> getPresignedPostFormData(PostPolicy policy) {
		try {
			return delegate.getPresignedPostFormData(policy);
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IOException e) {
			throw new StorageException(e);
		}
	}

	@Override
	public GetObjectResponse getObject(GetObjectArgs args) {
		try {
			return delegate.getObject(args);
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IOException e) {
			throw new StorageException(e);
		}
	}

}
