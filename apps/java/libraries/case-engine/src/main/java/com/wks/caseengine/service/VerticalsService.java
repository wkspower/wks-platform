package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.VerticalsDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;


public interface VerticalsService {
	
	public AOPMessageVM getAllVerticals();
	
	public AOPMessageVM getHierarchyData();

}
