package com.wks.caseengine.service.tcs;

import java.util.List;
import java.util.Map;

import com.wks.caseengine.dto.tcs.TCSSlowdownDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface TCSSlowdownService {

    public Map<String, Object> getAll(String plantId, String year, String siteId, String verticalId);
    AOPMessageVM saveOrUpdate(String plantId, String year, List<TCSSlowdownDTO> dtoList);
}
