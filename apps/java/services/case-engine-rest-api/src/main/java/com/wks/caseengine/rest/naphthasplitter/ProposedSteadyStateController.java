package com.wks.caseengine.rest.naphthasplitter;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.naphthasplitter.dto.DataFilterDTO;
import com.wks.caseengine.naphthasplitter.dto.EnergyFactorDTO;
import com.wks.caseengine.naphthasplitter.dto.NormsRationalizationFactorDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.naphthasplitter.service.ProposedSteadyStateService;

@RestController
@RequestMapping("/task")
public class ProposedSteadyStateController {

    @Autowired
    private ProposedSteadyStateService proposedSteadyStateService;

    @GetMapping(value = "/energy-factor")
    public AOPMessageVM getEnergyFactor(@RequestParam String year, @RequestParam String plantFKId) {
        return proposedSteadyStateService.getEnergyFactor(year, plantFKId);
    }

    @GetMapping(value = "/norms-rationalization-factor")
    public AOPMessageVM getNormsRationalizationFactor(@RequestParam String year, @RequestParam String plantFKId) {
        return proposedSteadyStateService.getNormsRationalizationFactor(year, plantFKId);
    }

    @PostMapping(value = "/energy-factor")
    public AOPMessageVM saveEnergyFactor(@RequestBody List<EnergyFactorDTO> dtos, @RequestParam String plantId, @RequestParam String aopYear) {
        List<EnergyFactorDTO> failedList = proposedSteadyStateService.saveEnergyFactor(plantId, aopYear, dtos);
       if(failedList.isEmpty()) {
        return new AOPMessageVM(200, "Data saved successfully", failedList);
       }
       return new AOPMessageVM(400, "Partial data saved", failedList);
    }

    @PostMapping(value = "/norms-rationalization-factor")
    public AOPMessageVM saveNormsRationalizationFactor(@RequestBody List<NormsRationalizationFactorDTO> dtos, @RequestParam String plantId, @RequestParam String aopYear) {
        List<NormsRationalizationFactorDTO> failedList = proposedSteadyStateService.saveNormsRationalizationFactor(plantId, aopYear, dtos);
        if(failedList.isEmpty()) {
            return new AOPMessageVM(200, "Data saved successfully", failedList);
        }
        return new AOPMessageVM(400, "Partial data saved", failedList);
    }

    @GetMapping(value = "/data-filter")
    public AOPMessageVM getDataFilter(@RequestParam String plantId, @RequestParam String aopYear) {
        List<DataFilterDTO> dataFilterDTOList = proposedSteadyStateService.getDataFilter(UUID.fromString(plantId), aopYear);
        return new AOPMessageVM(200, "Data fetched successfully", dataFilterDTOList);
    }

    @PostMapping(value = "/data-filter")
    public AOPMessageVM saveDataFilter(@RequestBody List<DataFilterDTO> dataFilterDTOList, @RequestParam String year) {
        return proposedSteadyStateService.saveDataFilter(dataFilterDTOList, year);
    }
}
