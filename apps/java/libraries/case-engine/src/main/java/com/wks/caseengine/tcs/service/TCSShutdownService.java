package com.wks.caseengine.tcs.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.tcs.dto.TCSShutdownDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface TCSShutdownService {

    public Map<String, Object> getAll(String plantId, String year, String siteId, String verticalId);
    AOPMessageVM saveOrUpdate(String plantId, String year, List<TCSShutdownDTO> dtoList);

    public AOPMessageVM delete(UUID id);

    public byte[] exportTCSShutdown(
        String plantId,
        String year,
        String siteId,
        String verticalId);

    public AOPMessageVM importExcel(
        String plantId,
        String year,
        MultipartFile file);
}


