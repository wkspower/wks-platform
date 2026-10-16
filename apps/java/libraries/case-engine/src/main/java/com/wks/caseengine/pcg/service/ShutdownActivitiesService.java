package com.wks.caseengine.pcg.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.pcg.dto.AopProductionNormsDTO;
import com.wks.caseengine.pcg.dto.GasifierDropdownDTO;
import com.wks.caseengine.pcg.dto.ShutdownTaTransactionDTO;

public interface ShutdownActivitiesService {

    List<GasifierDropdownDTO> getGasifierDropdown(UUID plantId, String aopYear);

    List<ShutdownTaTransactionDTO> getShutdownTaTransactions(UUID plantId, String aopYear);

    List<ShutdownTaTransactionDTO> saveShutdownTaTransactions(UUID plantId, List<ShutdownTaTransactionDTO> shutdownTaTransactionDTOList);

    List<AopProductionNormsDTO> getAopProductionNorms(UUID plantId, String aopYear);

    byte[] exportShutdownTaTransactions(UUID plantId, String aopYear, boolean isAfterSave, List<ShutdownTaTransactionDTO> dtoList);

    AOPMessageVM importShutdownTaTransactions(UUID plantId, String aopYear, MultipartFile file);
}
