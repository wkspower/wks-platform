package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;

import com.wks.caseengine.dto.TCSUnitCapacityDTO;
import com.wks.caseengine.dto.TCSUnitCapacityUOMDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface TCSUnitCapacityService {
    public Map<String, Object> getAll(
        String plantId,
        String aopYear,
        String capacityType,
        String uom);
    public AOPMessageVM saveOrUpdate(
        String plantId,
        String aopYear,
        String capacityType,
        String uom,
        List<TCSUnitCapacityDTO> dtoList);
    public List<TCSUnitCapacityUOMDTO> getAllUOM(
        String plantId,
        String aopYear,
        String capacityType);
}
