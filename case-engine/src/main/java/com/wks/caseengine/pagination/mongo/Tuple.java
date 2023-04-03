package com.wks.caseengine.pagination.mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Tuple<S1, S2> {
	
	private S1 criteria;
	private S2 sorted;

}
