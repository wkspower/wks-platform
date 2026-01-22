package com.wks.caseengine.tcs.service;
import java.util.Map;

public interface TCSNetCapacityService {
    public Map<String, Object> getAll(
        String plantId,
        String aopYear,
        String capacityType,
        String siteId,
        String verticalId);

}