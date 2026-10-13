package com.wks.caseengine.RefineryUtility.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.RefineryUtility.dto.CommodityChemicalsConsumptionDTO;
import com.wks.caseengine.RefineryUtility.dto.CommoditySelectionDTO;
import com.wks.caseengine.RefineryUtility.dto.ConsumerDemandDTO;
import com.wks.caseengine.RefineryUtility.dto.MonthWiseConstantsDTO;
import com.wks.caseengine.RefineryUtility.dto.PlantOwnerDTO;
import com.wks.caseengine.RefineryUtility.dto.SelectedPlantOwnerDTO;
import com.wks.caseengine.RefineryUtility.dto.TreatmentVendorConsumptionDTO;
import com.wks.caseengine.RefineryUtility.dto.TreatmentVendorDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface RefineryUtilityConfigurationService {
    
    public AOPMessageVM getMonthWiseConstants(String year, String plantFKId);
    public List<MonthWiseConstantsDTO> saveMonthWiseConstants(String year, String plantFKId, List<MonthWiseConstantsDTO> monthWiseConstantsDTOList);
    public byte[] exportMonthWiseConstants(String year, String plantFKId,boolean isAfterSave,List<MonthWiseConstantsDTO> dtoList);
	public AOPMessageVM importMonthWiseConstants(String year,UUID plantId,MultipartFile file);
    public AOPMessageVM checkIsSummerWinterPlant(String plantId);
    public AOPMessageVM getTreatmentVendorData(String year, String plantFKId);
    public List<TreatmentVendorDTO> saveTreatmentVendorData(String year, String plantFKId, List<TreatmentVendorDTO> treatmentVendorDTOList);
    public AOPMessageVM deleteTreatmentVendorData(String normParameterFKId, String year);
    public AOPMessageVM getCommodityChemicalsData(String year, String plantFKId);
    public List<CommoditySelectionDTO> saveCommodityChemicalsData(String year, String plantFKId, List<CommoditySelectionDTO> commoditySelectionDTOList);
    public AOPMessageVM getPlantOwnerDropdown(String plantFKId);
    public AOPMessageVM getSelectedPlantOwner(String plantFKId);
    public List<SelectedPlantOwnerDTO> saveSelectedPlantOwner(String plantFKId, List<SelectedPlantOwnerDTO> dtoList);
    public AOPMessageVM getConsumerDemandData(String year, String plantFKId);
    public List<ConsumerDemandDTO> saveConsumerDemandData(String year, String plantFKId, List<ConsumerDemandDTO> consumerDemandDTOList);
    public AOPMessageVM getTreatmentVendorConsumption(String year, String plantFKId);
    public AOPMessageVM getCommodityChemicalsConsumption(String year, String plantFKId);
    public List<TreatmentVendorConsumptionDTO> saveRefinaryConsumptionTransaction(String year, String plantFKId, List<TreatmentVendorConsumptionDTO> treatmentVendorConsumptionDTOList);
}
