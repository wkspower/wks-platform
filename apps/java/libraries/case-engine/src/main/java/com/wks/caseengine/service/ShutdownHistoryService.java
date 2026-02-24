package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;


import com.wks.caseengine.dto.ShutdownHistoryConfigDTO;
import com.wks.caseengine.dto.SlowdownHistoryConfigDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ShutdownHistoryService {
	
	public AOPMessageVM getShutdownHistory(String plantId,String year);
	public AOPMessageVM getShutdownHistoryPTA(String plantId,String year);
	public AOPMessageVM getTypeOfSD(String plantId,String year);
	public AOPMessageVM getLineDetails(String plantId,String year);
	public AOPMessageVM saveShutdownHistory( String year, String plantFKId, List<ShutdownHistoryConfigDTO> shutdownHistoryConfigDTOs);
	public AOPMessageVM deleteShutdownHistory(UUID id);

	public AOPMessageVM getSlowdownHistory(String plantId, String year);

	public AOPMessageVM saveSlowdownHistory(String year, String plantFKId,
			List<SlowdownHistoryConfigDTO> slowdownHistoryConfigDTOs);

	public AOPMessageVM deleteSlowdownHistory(UUID id);		

}
