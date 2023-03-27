package com.wks.storage.service;

public interface StorageServiceFactory {

	ServiceFactory getFactory();

	ServiceFactory getFactory(String driver);

}