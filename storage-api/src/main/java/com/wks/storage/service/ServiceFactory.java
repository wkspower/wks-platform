package com.wks.storage.service;

public interface ServiceFactory {
	
	BucketService getBucketService();
	
	DownloadService getDownloadService();
	
	UploadService getUploadService();

}
