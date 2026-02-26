package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;


import org.springframework.web.multipart.MultipartFile;


import com.wks.caseengine.dto.SpyroOutputDTO;
import com.wks.caseengine.dto.YieldDMDDTO;
import com.wks.caseengine.dto.YieldDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface SpyroOutputService {
	
	public AOPMessageVM getSpyroOutputData( String year, String plantId, String Mode,String type);
	
	public AOPMessageVM updateSpyroOutputData(String year,String plantId,  List<SpyroOutputDTO> spyroOutputDTOList);
	
	public AOPMessageVM getSpyroOutputYieldData( String year, String plantId);
	
	public AOPMessageVM getSpyroOutputYieldDMD( String year, String plantId);
	
	public AOPMessageVM getSpyroOutputYieldVMD( String year, String plantId);
	
	public byte[] exportYieldReport(String year, String plantFKId,boolean isAfterSave,List<YieldDTO> dtoList);
	
	public byte[] exportYieldDMD(String year, String plantFKId,boolean isAfterSave,List<YieldDMDDTO> dtoList);
	
	public AOPMessageVM updateSpyroOutputYieldData( String plantId, String year,  List<YieldDTO> yieldDTOs);
	
	public AOPMessageVM updateSpyroOutputYieldDMD( String plantId, String year,  List<YieldDMDDTO> yieldDTOs);
	
	byte[] createExcel(String year, String plantId, String mode, boolean isAfterSave,
			Map<String, List<SpyroOutputDTO>> mapForExcel);
	
	public AOPMessageVM importYieldExcel(String year,UUID plantId,MultipartFile file);
	
	public AOPMessageVM importYieldDMD(String year,UUID plantId,MultipartFile file);
	          
	AOPMessageVM importExcel(String year, String plantFKId, String mode, MultipartFile file);
	

}
