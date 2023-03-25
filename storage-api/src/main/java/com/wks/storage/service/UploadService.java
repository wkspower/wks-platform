package com.wks.storage.service;

import com.wks.storage.model.UploadFileUrl;

public interface UploadService {

	UploadFileUrl createPresignedPostFormData(String dir, String fileName, String contentType) throws Exception;

	UploadFileUrl createPresignedPostFormData(String fileName, String contentType) throws Exception;

}
