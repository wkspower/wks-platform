package com.wks.caseengine.cpp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.CPPEfficiencyDTO;
import com.wks.caseengine.cpp.dto.CPPFuelRatioDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface JMDEfficiencyAndFuelRatioService {

    AOPMessageVM getEfficiency(List<UUID> plantIds, String aopYear);

    AOPMessageVM saveEfficiency(List<UUID> plantIds, String aopYear,
                                List<CPPEfficiencyDTO> dtoList);

    byte[] exportEfficiency(List<UUID> plantIds, String aopYear);

    AOPMessageVM importEfficiency(List<UUID> plantIds, String aopYear,
                                  MultipartFile file);

    AOPMessageVM getFuelRatio(List<UUID> plantIds, String aopYear);

    AOPMessageVM saveFuelRatio(List<UUID> plantIds, String aopYear,
                               List<CPPFuelRatioDTO> dtoList);
}
