package com.wks.caseengine.tcs.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.tcs.dto.TCSSlowdownDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface TCSSlowdownService {

    public Map<String, Object> getAll(String plantId, String year, String siteId, String verticalId);

    public AOPMessageVM carryForwardTCSSlowdown(String plantId, String year);

    AOPMessageVM saveOrUpdate(String plantId, String year, List<TCSSlowdownDTO> dtoList);

    public AOPMessageVM delete(UUID id);
    
    public byte[] exportTCSSlowdown(String plantId, String year, String siteId, String verticalId);
    
    public AOPMessageVM importExcel(String plantId, String year, MultipartFile file);
}

    


