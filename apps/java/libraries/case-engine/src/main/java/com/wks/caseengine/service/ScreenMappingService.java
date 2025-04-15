package com.wks.caseengine.service;

import java.util.List;
import com.wks.caseengine.dto.VerticalScreenMappingDTO;

public interface ScreenMappingService {
	
	public	List<VerticalScreenMappingDTO> getScreenMapping(String verticalId);

}
