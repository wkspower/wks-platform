package com.wks.storage.service;

public interface BucketService {

	String createAssignedTenant() throws Exception;

	String createObjectWithPath(String dir, String fileName) throws Exception;

}