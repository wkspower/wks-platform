package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;

import com.wks.caseengine.dto.TCSUnitCapacityDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface TCSUnitCapacityService {
    public Map<String, Object> getAll(String plantId, String year);
    public AOPMessageVM saveOrUpdate(String plantId, String year, List<TCSUnitCapacityDTO> dtoList);
}
