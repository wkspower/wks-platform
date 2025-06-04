package com.wks.caseengine.service;

import org.springframework.web.bind.annotation.RequestParam;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface SpyroInputService {
	
	public AOPMessageVM getSpyroInputData(@RequestParam String year,@RequestParam String plantId,@RequestParam String Mode);

}
