package com.wks.caseengine.cpp.service;

import com.wks.caseengine.cpp.dto.IntersiteSteamTransferDto;
import com.wks.caseengine.message.vm.AOPMessageVM;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IntersiteSteamTransferService {

    AOPMessageVM getIntersiteSteamTransfer(String financialYear);

    AOPMessageVM saveIntersiteSteamTransfer(
            List<UUID> plantIds,
            String financialYear,
            List<IntersiteSteamTransferDto> payload);

    byte[] exportIntersiteSteamTransfer(
            List<UUID> plantIds,
            String financialYear);

    AOPMessageVM importIntersiteSteamTransfer(
            List<UUID> plantIds,
            String financialYear,
            MultipartFile file);
}
