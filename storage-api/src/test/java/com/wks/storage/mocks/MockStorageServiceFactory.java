package com.wks.storage.mocks;

import com.wks.storage.service.BucketService;
import com.wks.storage.service.DownloadService;
import com.wks.storage.service.ServiceFactory;
import com.wks.storage.service.StorageServiceFactory;
import com.wks.storage.service.UploadService;

import lombok.Setter;

@Setter
public class MockStorageServiceFactory implements StorageServiceFactory {

	private DownloadService downloadService;

	private UploadService uploadService;
	
	private BucketService bucketService;
	
	@Override
	public ServiceFactory getFactory() {
		return new ServiceFactory() {

			@Override
			public BucketService getBucketService() {
				return bucketService;
			}

			@Override
			public DownloadService getDownloadService() {
				return downloadService;
			}

			@Override
			public UploadService getUploadService() {
				return uploadService;
			}
			
		};
	}
	
	@Override
	public ServiceFactory getFactory(String driver) {
		return getFactory();
	}

}
