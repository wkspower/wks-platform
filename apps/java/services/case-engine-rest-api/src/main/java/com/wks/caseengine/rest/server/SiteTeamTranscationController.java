package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.dto.SiteTeamTranscationDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.SiteTeamTranscationService;

@RestController
@RequestMapping("task")
public class SiteTeamTranscationController {
	
	@Autowired
	private SiteTeamTranscationService siteTeamTranscationService;
	
	@GetMapping(value="/site-team-transaction")
	public AOPMessageVM getSiteTeamTransaction(@RequestParam String siteId,@RequestParam String year){
		 return  siteTeamTranscationService.getSiteTeamTransaction(siteId,year);
	}
	
	@PostMapping(value="/site-team-transaction")
	public AOPMessageVM saveSiteTeamTransaction(@RequestParam String year,@RequestParam String siteId, @RequestBody List<SiteTeamTranscationDTO> siteTeamTranscationDTOs) {
		return 	siteTeamTranscationService.saveSiteTeamTransaction(year,siteId,siteTeamTranscationDTOs);
	}
	
		
}
