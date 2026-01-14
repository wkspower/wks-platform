package com.wks.caseengine.service.tcs;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.wks.caseengine.dto.tcs.TCSShutdownDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface TCSShutdownService {

    public Map<String, Object> getAll(String plantId, String year, String siteId, String verticalId);
    AOPMessageVM saveOrUpdate(String plantId, String year, List<TCSShutdownDTO> dtoList);

    public AOPMessageVM delete(UUID id);
}
