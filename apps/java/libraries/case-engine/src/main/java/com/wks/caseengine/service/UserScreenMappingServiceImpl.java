package com.wks.caseengine.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.repository.UserScreenMappingRepository;

@Service
public class UserScreenMappingServiceImpl implements UserScreenMappingService {

	@Autowired
	private UserScreenMappingRepository userScreenMappingRepository;

	
	@Override
	public Map<String, Object> getUserScreenMapping(String verticalId, String plantId, String userId) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();

		List<String> userScreens = userScreenMappingRepository.findByVerticalFKIdAndPlantFKIdandUserId(verticalId, plantId, userId);
		
//        ObjectMapper objectMapper = new ObjectMapper();
//
//		// Convert List<UserScreenMapping> to List<UserScreenMappingDTO>
//		List<UserScreenMappingDTO> userScreenMappingsDTOs = userScreenMappings.stream()
//		    .map(entity -> objectMapper.convertValue(entity, UserScreenMappingDTO.class))
//		    .collect(Collectors.toList());
        
		result.put("status", 200);
		result.put("message", "Screens list by verticalId " + verticalId + ".");
		result.put("data", userScreens);
		return result;
	}

}
