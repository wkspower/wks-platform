package com.wks.caseengine.repository;

import org.springframework.stereotype.Component;

import com.mongodb.client.FindIterable;
import com.wks.caseengine.cases.instance.CaseInstance;

@Component
public class Paginator {

	private int page = 0;
	private int offset = 5;

	public FindIterable<CaseInstance> apply(final FindIterable<CaseInstance> findIterable) {
		return findIterable.skip(page > 0 ? ((page - 1) * offset) : 0).limit(offset);
	}
}
