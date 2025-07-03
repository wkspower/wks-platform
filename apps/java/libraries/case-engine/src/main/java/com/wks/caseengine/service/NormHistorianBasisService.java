
package com.wks.caseengine.service;

import com.wks.caseengine.message.vm.AOPMessageVM;

public interface NormHistorianBasisService {
    public AOPMessageVM getNormHistorianBasisData(String plantId,String year,String reportType,String uom);
    
}
