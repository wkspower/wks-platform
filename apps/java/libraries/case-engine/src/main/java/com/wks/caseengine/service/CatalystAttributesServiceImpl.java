package com.wks.caseengine.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.entity.CatalystAttributes;
import com.wks.caseengine.repository.CatalystAttributesRepository;

@Service
public class CatalystAttributesServiceImpl implements CatalystAttributesService{
	
	@Autowired
	private CatalystAttributesRepository catalystAttributesRepository;

	@Override
	public List<CatalystAttributes> findAll() {
		return catalystAttributesRepository.findAll();
	}

}
