package com.wks.caseengine.pagination;

import org.springframework.data.mongodb.core.query.Criteria;

public interface CriteriaArgs  {
	Criteria add(Criteria c);
}
