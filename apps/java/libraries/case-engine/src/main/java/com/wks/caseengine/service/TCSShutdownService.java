package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;

import com.wks.caseengine.dto.TCSShutdownDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface TCSShutdownService {

    public Map<String, Object> getAll(String plantId, String year);
    AOPMessageVM saveOrUpdate(String plantId, String year, List<TCSShutdownDTO> dtoList);
}
