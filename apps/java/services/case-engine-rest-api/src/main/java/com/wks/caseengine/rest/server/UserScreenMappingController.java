package com.wks.caseengine.rest.server;

import java.util.Map;

import com.wks.caseengine.service.UserScreenMappingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("task/user/screen")
public class UserScreenMappingController {
	
	@Autowired
	private UserScreenMappingService userScreenMappingService;
	
	@GetMapping
	public Map<String, Object> getUserScreenMapping(@RequestParam String verticalId, @RequestParam String plantId,@RequestParam(value = "userId", required = false) String userId) throws Exception{
		
		if(userId!=null) {
			System.out.println("UserId " + userId);
		}else {
			 Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			 userId = authentication.getName(); 
			 System.out.println("UserId " + userId);	 
		}
		return userScreenMappingService.getUserScreenMapping(verticalId,plantId, userId);
					
	}

}

