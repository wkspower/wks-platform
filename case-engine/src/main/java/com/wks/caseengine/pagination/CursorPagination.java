package com.wks.caseengine.pagination;

public interface CursorPagination {

	<T> PageResult<T> executeQuery(Args args, Class<T> clz);

}