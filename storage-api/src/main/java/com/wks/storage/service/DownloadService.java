package com.wks.storage.service;

import com.wks.storage.model.DownloadFileUrl;

public interface DownloadService {

	DownloadFileUrl createPresignedObjectUrl(String dir, String fileName, String contentType) throws Exception;

	DownloadFileUrl createPresignedObjectUrl(String fileName, String contentType) throws Exception;

}
