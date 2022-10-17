package com.wks.caseengine.repository;

import java.util.List;

public interface Repository<T> {

	List<T> find() throws Exception;

	T get(final String id) throws Exception;

	void save(final T object) throws Exception;

	void delete(final String id) throws Exception;

}
