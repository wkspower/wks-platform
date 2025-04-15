package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface NormParametersService {

   public List<NormParameters> findAllByType(String type);

   public AOPMessageVM getAllGrades(String plantId);

}
