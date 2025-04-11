package com.wks.caseengine.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.ScreenDataConfigDTO;
import com.wks.caseengine.entity.ScreenDataConfig;
import com.wks.caseengine.repository.ScreenDataConfigRepository;

@Service
public class ScreenDataConfigServiceImpl implements ScreenDataConfigService{
	
	@Autowired
	private ScreenDataConfigRepository screenDataConfigRepository;

	@Override
	public ScreenDataConfigDTO getScreenData(String verticalId, String screenName) {
		
		Optional<ScreenDataConfig> screenDataConfigOp=screenDataConfigRepository.findByScreenNameAndVerticalFkId(screenName,UUID.fromString(verticalId));
		ScreenDataConfig screenDataConfig=screenDataConfigOp.get();
		ScreenDataConfigDTO screenDataConfigDTO=new ScreenDataConfigDTO();
		screenDataConfigDTO.setHeaderJson(screenDataConfig.getHeaderJson());
		screenDataConfigDTO.setId(screenDataConfig.getId().toString());
		screenDataConfigDTO.setScreenName(screenDataConfig.getScreenName());
		screenDataConfigDTO.setVerticalFkId(screenDataConfig.getVerticalFkId().toString());
		// TODO Auto-generated method stub
		return screenDataConfigDTO;
	}

}
