package com.wks.caseengine.naphthasplitter.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.naphthasplitter.dto.DataFilterDTO;
import com.wks.caseengine.naphthasplitter.dto.EnergyFactorDTO;
import com.wks.caseengine.naphthasplitter.dto.NormsRationalizationFactorDTO;

public interface ProposedSteadyStateService {

    AOPMessageVM getEnergyFactor(String year, String plantFKId);

    AOPMessageVM getNormsRationalizationFactor(String year, String plantFKId);

    List<EnergyFactorDTO> saveEnergyFactor(String plantId, String aopYear, List<EnergyFactorDTO> dtos);

    List<NormsRationalizationFactorDTO> saveNormsRationalizationFactor(String plantId, String aopYear, List<NormsRationalizationFactorDTO> dtos);

    List<DataFilterDTO> getDataFilter(UUID plantId, String aopYear);

    AOPMessageVM saveDataFilter(List<DataFilterDTO> dataFilterDTOList, String year);
}
