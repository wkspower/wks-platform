package com.wks.caseengine.tcs.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.tcs.dto.TCSUnitCapacityDTO;
import com.wks.caseengine.tcs.dto.TCSUnitCapacityUOMDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface TCSUnitCapacityService {
    public Map<String, Object> getAll(
        String plantId,
        String aopYear,
        String capacityType,
    //    String uom,
        String siteId,
        String verticalId);

    public AOPMessageVM saveOrUpdate(
        String plantId,
        String aopYear,
        String capacityType,
     //   String uom,
        List<TCSUnitCapacityDTO> dtoList);

    public List<TCSUnitCapacityUOMDTO> getAllUOM(
        String plantId,
        String aopYear,
        String capacityType,
        String verticalId
        );

    public byte[] exportTCSUnitCapacity(
        String plantId,
        String year,
        String capacityType,
        String siteId,
        String verticalId);

    public AOPMessageVM importExcel(
        String plantId,
        String year,
        String capacityType,
        MultipartFile file);
}


