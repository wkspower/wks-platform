package com.wks.caseengine.tcs.service;

import java.util.Map;

public interface TCSSiteNetCapacityService {
    public Map<String, Object> getAll(
        String plantId,
        String aopYear,
        String capacityType,
    //    String uom,
        String siteId,
        String verticalId);

}


