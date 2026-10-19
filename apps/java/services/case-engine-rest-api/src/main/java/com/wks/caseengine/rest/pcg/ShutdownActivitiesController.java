package com.wks.caseengine.rest.pcg;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.pcg.dto.AopProductionNormsDTO;
import com.wks.caseengine.pcg.dto.GasifierDropdownDTO;
import com.wks.caseengine.pcg.dto.ShutdownTaTransactionDTO;
import com.wks.caseengine.pcg.service.ShutdownActivitiesService;

@RestController
@RequestMapping("task")
public class ShutdownActivitiesController {

    @Autowired
    private ShutdownActivitiesService shutdownActivitiesService;

    @GetMapping("/gasifier-dropdown")
    public ResponseEntity<AOPMessageVM> getGasifierDropdown(
            @RequestParam String plantId,
            @RequestParam String aopYear) {

        if (plantId == null || plantId.isEmpty() || aopYear == null || aopYear.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        List<GasifierDropdownDTO> result = shutdownActivitiesService.getGasifierDropdown(UUID.fromString(plantId), aopYear);
        AOPMessageVM aopMessageVM = new AOPMessageVM();
        aopMessageVM.setMessage("Success");
        aopMessageVM.setData(result);
        return ResponseEntity.ok(aopMessageVM);
    }

    @GetMapping("/shutdown-transactions")
    public ResponseEntity<AOPMessageVM> getShutdownTaTransactions(
            @RequestParam String plantId,
            @RequestParam String aopYear) {

        if (plantId == null || plantId.isEmpty() || aopYear == null || aopYear.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        List<ShutdownTaTransactionDTO> result = shutdownActivitiesService.getShutdownTaTransactions(UUID.fromString(plantId), aopYear);
        AOPMessageVM aopMessageVM = new AOPMessageVM();
        aopMessageVM.setMessage("Success");
        aopMessageVM.setData(result);
        return ResponseEntity.ok(aopMessageVM);
    }

    @PostMapping ("/shutdown-transactions")
    public ResponseEntity<AOPMessageVM> saveShutdownTaTransactions( @RequestParam String plantId, @RequestParam String aopYear, @RequestBody List<ShutdownTaTransactionDTO> shutdownTaTransactionDTOList) {

        if (plantId == null || plantId.isEmpty() || aopYear == null || aopYear.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        List<ShutdownTaTransactionDTO> result = shutdownActivitiesService.saveShutdownTaTransactions(UUID.fromString(plantId), aopYear, shutdownTaTransactionDTOList);
        AOPMessageVM aopMessageVM = new AOPMessageVM();
        aopMessageVM.setMessage("Success");
        aopMessageVM.setData(result);
        return ResponseEntity.ok(aopMessageVM);
    }

    @GetMapping("/shutdown-transactions-export")
    public ResponseEntity<byte[]> exportShutdownTaTransactions(
            @RequestParam String plantId,
            @RequestParam String aopYear) {
        try {
            byte[] excelBytes = shutdownActivitiesService.exportShutdownTaTransactions(UUID.fromString(plantId), aopYear, false, null);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("shutdown-ta-transactions.xlsx")
                    .build());
            headers.setContentLength(excelBytes.length);

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/shutdown-transactions-import", consumes = "multipart/form-data")
    public AOPMessageVM importShutdownTaTransactions(
            @RequestParam("plantId") String plantId,
            @RequestParam("aopYear") String aopYear,
            @RequestParam("file") MultipartFile file) {
        try {
            if (plantId == null || plantId.isEmpty() || aopYear == null || aopYear.isEmpty()) {
                throw new IllegalArgumentException("Plant ID and AOP Year are required");
            }

            return shutdownActivitiesService.importShutdownTaTransactions(UUID.fromString(plantId), aopYear, file);
        } catch (Exception e) {
            AOPMessageVM aopMessageVM = new AOPMessageVM();
            aopMessageVM.setCode(500);
            aopMessageVM.setMessage("Failed to import shutdown TA transactions");
            return aopMessageVM;
        }
    }

    @GetMapping("/aop-production-norms")
    public ResponseEntity<AOPMessageVM> getAopProductionNorms(
            @RequestParam String plantId,
            @RequestParam String aopYear) {

        if (plantId == null || plantId.isEmpty() || aopYear == null || aopYear.isEmpty()) {
            throw new IllegalArgumentException("Plant ID and AOP Year are required");
        }

        List<AopProductionNormsDTO> result = shutdownActivitiesService.getAopProductionNorms(UUID.fromString(plantId), aopYear);
        AOPMessageVM aopMessageVM = new AOPMessageVM();
        aopMessageVM.setMessage("Success");
        aopMessageVM.setData(result);
        return ResponseEntity.ok(aopMessageVM);
    }
}
