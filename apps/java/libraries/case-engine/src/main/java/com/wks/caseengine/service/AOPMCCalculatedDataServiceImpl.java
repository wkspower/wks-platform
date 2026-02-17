package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import com.wks.caseengine.repository.MCUMaxCapacityRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.ExcelConstants;
import com.wks.caseengine.utility.Utility;
import jakarta.persistence.Query;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.sql.DataSource;


import org.apache.poi.ss.usermodel.Cell;

import org.apache.poi.ss.usermodel.CellType;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;

import com.wks.caseengine.entity.AOPMCCalculatedData;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.ExcelConfigurations;
import com.wks.caseengine.entity.MCUDesignCapacity;
import com.wks.caseengine.entity.MCUMaxCapacity;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AOPMCCalculatedDataRepository;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.ExcelConfigurationsRepository;
import com.wks.caseengine.repository.MCUValueCapacityRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.NormParametersRepository;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;

@Service
public class AOPMCCalculatedDataServiceImpl implements AOPMCCalculatedDataService {

	@Autowired
	private AOPMCCalculatedDataRepository aOPMCCalculatedDataRepository;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;

	private DataSource dataSource;

	@Autowired
	private AopCalculationRepository aopCalculationRepository;

	@Autowired
	private ScreenMappingRepository screenMappingRepository;

	@Autowired
	private MCUValueCapacityRepository mcuValueCapacityRepository;
	
	@Autowired
	private NormParametersRepository normParametersRepository;
	
	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionRepository;
	
	@Autowired
	private MCUMaxCapacityRepository mcuMaxCapacityRepository;
	
	@Autowired
	private ExcelConfigurationsRepository  excelConfigurationsRepository;
	
	@Autowired
	private ExcelUtilityService excelUtilityService;

	// Inject or set your DataSource (e.g., via constructor or setter)
	public AOPMCCalculatedDataServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public AOPMessageVM getAOPMCCalculatedData(String plantId, String year) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    try {
	        String view = "";
	        Plants plant = plantsRepository.findById(UUID.fromString(plantId))
	                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
	                .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
	        Sites site = siteRepository.findById(plant.getSiteFkId())
	                .orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
	        String procedureName=null;
	        if (vertical.getName().equalsIgnoreCase("PTA")) {
	            view = "vw" + vertical.getName() + "_" + site.getName() + "_AOPMCValues";
	        }else if(vertical.getName().equalsIgnoreCase("CRACKER")) {
	        	 procedureName=vertical.getName()+"_GetAOPMCValues";
				 
	        } else {
	            view = "vwAOPMCValues";
	        }
	        List<Object[]> obj=null;
	        if(vertical.getName().equalsIgnoreCase("CRACKER")) {
	        	obj= findByYearAndPlantId(year, UUID.fromString(plantId) ,  procedureName);
	        }else {
	        	  obj = getDataMCUValuesAllData(year, plantId, view);
	        }
	       
	        List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList = new ArrayList<>();

	        for (Object[] row : obj) {
	            AOPMCCalculatedDataDTO dto = new AOPMCCalculatedDataDTO();
	            dto.setId(row[0] != null ? row[0].toString() : "");
	            dto.setSiteFKId(row[1] != null ? row[1].toString() : "");
	            dto.setPlantFKId(row[2] != null ? row[2].toString() : "");
	            dto.setMaterialFKId(row[3] != null ? row[3].toString() : "");
	            dto.setApril(row[4] != null ? Double.parseDouble(row[4].toString()) : 0.0);
	            dto.setMay(row[5] != null ? Double.parseDouble(row[5].toString()) : 0.0);
	            dto.setJune(row[6] != null ? Double.parseDouble(row[6].toString()) : 0.0);
	            dto.setJuly(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
	            dto.setAugust(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
	            dto.setSeptember(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
	            dto.setOctober(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
	            dto.setNovember(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
	            dto.setDecember(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
	            dto.setJanuary(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
	            dto.setFebruary(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);
	            dto.setMarch(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0);

	            dto.setFinancialYear(row[16] != null ? row[16].toString() : null);
	            dto.setRemarks(row[17] != null ? row[17].toString() : " ");
	            dto.setVerticalFKId(row[22] != null ? row[22].toString() : null);
	            dto.setProductName(row[24] != null ? row[24].toString() : null);
	            dto.setMaterialDisplayName(row[24] != null ? row[24].toString() : null);
	            
	            aOPMCCalculatedDataDTOList.add(dto);
	        }

	        Map<String, Object> map = new HashMap<>();
	        List<AopCalculation> aopCalculation = aopCalculationRepository.findByPlantIdAndAopYearAndCalculationScreen(
	                UUID.fromString(plantId), year, "production-volume-data");
	        
	        map.put("aopMCCalculatedDataDTOList", aOPMCCalculatedDataDTOList);
	        map.put("aopCalculation", aopCalculation);
	        
	        aopMessageVM.setCode(200);
	        aopMessageVM.setData(map);
	        aopMessageVM.setMessage("Data fetched successfully");
	        return aopMessageVM;

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}

	@Override
	public AOPMessageVM getProductionTarget(String plantId, String year,String lineId) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    try {
	       
	        Plants plant = plantsRepository.findById(UUID.fromString(plantId))
	                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
	                .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
	        Sites site = siteRepository.findById(plant.getSiteFkId())
	                .orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
	        String procedureName=vertical.getName()+"_"+site.getName()+"_GetAOPMCValues";
	        
	         List<Object[]> obj= findByYearPlantIdAndLine(year, UUID.fromString(plantId),UUID.fromString(lineId) ,  procedureName);
	        
	        List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList = new ArrayList<>();

	        for (Object[] row : obj) {
	            AOPMCCalculatedDataDTO dto = new AOPMCCalculatedDataDTO();
	            dto.setId(row[0] != null ? row[0].toString() : "");
	            dto.setSiteFKId(row[1] != null ? row[1].toString() : "");
	            dto.setPlantFKId(row[2] != null ? row[2].toString() : "");
	            dto.setMaterialFKId(row[3] != null ? row[3].toString() : "");
	            dto.setApril(row[4] != null ? Double.parseDouble(row[4].toString()) : 0.0);
	            dto.setMay(row[5] != null ? Double.parseDouble(row[5].toString()) : 0.0);
	            dto.setJune(row[6] != null ? Double.parseDouble(row[6].toString()) : 0.0);
	            dto.setJuly(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
	            dto.setAugust(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
	            dto.setSeptember(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
	            dto.setOctober(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
	            dto.setNovember(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
	            dto.setDecember(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
	            dto.setJanuary(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
	            dto.setFebruary(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);
	            dto.setMarch(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0);

	            dto.setFinancialYear(row[16] != null ? row[16].toString() : null);
	            dto.setRemarks(row[17] != null ? row[17].toString() : " ");
	            dto.setVerticalFKId(row[22] != null ? row[22].toString() : null);
	            dto.setProductName(row[24] != null ? row[24].toString() : null);
	            dto.setMaterialDisplayName(row[24] != null ? row[24].toString() : null);
	            dto.setLineId(row[28] != null ? row[28].toString() : null);
	            aOPMCCalculatedDataDTOList.add(dto);
	        }

	        Map<String, Object> map = new HashMap<>();
	        List<AopCalculation> aopCalculation = aopCalculationRepository.findByPlantIdAndAopYearAndCalculationScreen(
	                UUID.fromString(plantId), year, "production-volume-data");
	        
	        map.put("aopMCCalculatedDataDTOList", aOPMCCalculatedDataDTOList);
	        map.put("aopCalculation", aopCalculation);
	        
	        aopMessageVM.setCode(200);
	        aopMessageVM.setData(map);
	        aopMessageVM.setMessage("Data fetched successfully");
	        return aopMessageVM;

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}

	public List<Object[]> findByYearAndPlantId(String aopYear, UUID plantId, String procedureName) {
		try {

			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @aopYear = :aopYear";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public List<Object[]> findByYearPlantIdAndLine(String aopYear, UUID plantId,UUID lineId, String procedureName) {
		try {

			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @aopYear = :aopYear, @lineId = :lineId";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);
			query.setParameter("lineId", lineId);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	@Override
	public AOPMessageVM getSummaryOfProposedOperating(String plantId, String year) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    try {
	        String view = "";
	        Plants plant = plantsRepository.findById(UUID.fromString(plantId))
	                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
	                .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
	        Sites site = siteRepository.findById(plant.getSiteFkId())
	                .orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));

	        if (vertical.getName().equalsIgnoreCase("PTA")) {
	            view = "vw" + vertical.getName() + "_" + site.getName() + "_AOPMCValues";
	        } else {
	            view = "vwAOPMCValues";
	        }

	        List<Object[]> obj = getDataMCUValuesAllData(year, plantId, view);
	        List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList = new ArrayList<>();

	        for (Object[] row : obj) {
	            AOPMCCalculatedDataDTO dto = new AOPMCCalculatedDataDTO();
	            dto.setId(row[0] != null ? row[0].toString() : "");
	            dto.setSiteFKId(row[1] != null ? row[1].toString() : "");
	            dto.setPlantFKId(row[2] != null ? row[2].toString() : "");
	            dto.setMaterialFKId(row[3] != null ? row[3].toString() : "");
	            double[] months = new double[12];
	            for (int i = 0; i < 12; i++) {
	                months[i] = (row[4 + i] != null) ? Double.parseDouble(row[4 + i].toString()) : 0.0;
	            }
	            double maxVal = 0.0;
	            for (double val : months) {
	                if (val > maxVal) maxVal = val;
	            }
	            dto.setApril(calculatePercentage(months[0], maxVal));
	            dto.setMay(calculatePercentage(months[1], maxVal));
	            dto.setJune(calculatePercentage(months[2], maxVal));
	            dto.setJuly(calculatePercentage(months[3], maxVal));
	            dto.setAugust(calculatePercentage(months[4], maxVal));
	            dto.setSeptember(calculatePercentage(months[5], maxVal));
	            dto.setOctober(calculatePercentage(months[6], maxVal));
	            dto.setNovember(calculatePercentage(months[7], maxVal));
	            dto.setDecember(calculatePercentage(months[8], maxVal));
	            dto.setJanuary(calculatePercentage(months[9], maxVal));
	            dto.setFebruary(calculatePercentage(months[10], maxVal));
	            dto.setMarch(calculatePercentage(months[11], maxVal));

	            dto.setFinancialYear(row[16] != null ? row[16].toString() : null);
	            dto.setRemarks(row[17] != null ? row[17].toString() : " ");
	            dto.setVerticalFKId(row[22] != null ? row[22].toString() : null);
	            dto.setProductName(row[24] != null ? row[24].toString() : null);
	            dto.setMaterialDisplayName(row[24] != null ? row[24].toString() : null);
	            
	            aOPMCCalculatedDataDTOList.add(dto);
	        }

	        Map<String, Object> map = new HashMap<>();
	        List<AopCalculation> aopCalculation = aopCalculationRepository.findByPlantIdAndAopYearAndCalculationScreen(
	                UUID.fromString(plantId), year, "production-volume-data");
	        
	        map.put("aopMCCalculatedDataDTOList", aOPMCCalculatedDataDTOList);
	        map.put("aopCalculation", aopCalculation);
	        
	        aopMessageVM.setCode(200);
	        aopMessageVM.setData(map);
	        aopMessageVM.setMessage("Data fetched successfully");
	        return aopMessageVM;

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}

	private Double calculatePercentage(double value, double max) {
	    if (max <= 0) return 0.0;
	    double result = (value / max) * 100;
	    return Math.round(result * 100.0) / 100.0;
	}
	
	
    public List<Object[]> getDataMCUValuesAllData(String year, String plantId, String viewName) {
        String sql = "SELECT TOP (1000) "
                   + "Id, Site_FK_Id, Plant_FK_Id, Material_FK_Id, "
                   + "April, May, June, July, August, September, October, November, December, "
                   + "January, February, March, "
                   + "FinancialYear, Remarks, CreatedOn, ModifiedOn, MCUVersion, UpdatedBy, "
                   + "Vertical_FK_Id, NormParameterDisplayOrder, ProductName "
                   + "FROM " + viewName + " " 
                   + "WHERE PLANT_FK_ID = :plantId "
                   + "AND FinancialYear = :year "
                   + "ORDER BY NormParameterDisplayOrder";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("year", year);
        query.setParameter("plantId", plantId);

        return query.getResultList();
    }

    @Override
    public AOPMessageVM getMaxAchievedCapacity(String plantId, String year) {
        AOPMessageVM aopMessageVM = new AOPMessageVM();
        try {
            String view = "";
            String procedureName = "";
            Plants plant = plantsRepository.findById(UUID.fromString(plantId))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
            Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
            Sites site = siteRepository.findById(plant.getSiteFkId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
            List<Object[]> obj =null;
            if (vertical.getName().equalsIgnoreCase("PTA")) {
                view = "vw" + vertical.getName() + "_" + site.getName() + "_AOPMCValuesMaxAchivedCapacity";
            }else if(vertical.getName().equalsIgnoreCase("CRACKER")) {
	        	 procedureName=vertical.getName()+"_GetAOPMCValuesMaxAchivedCapacity";
				 
	        }  else {
                view = "vwAOPMCValuesMaxAchivedCapacity";
            }
            if(vertical.getName().equalsIgnoreCase("CRACKER")) {
	        	obj= findByYearAndPlantId(year, UUID.fromString(plantId) ,  procedureName);
	        }else {
	        	 obj = getMaxAchievedCapacityData(year, plantId, view);
	        }
            
            List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList = new ArrayList<>();

            for (Object[] row : obj) {
                AOPMCCalculatedDataDTO dto = new AOPMCCalculatedDataDTO();
                dto.setId(row[0] != null ? row[0].toString() : "");
                dto.setMaterialFKId(row[1] != null ? row[1].toString() : "");
                dto.setMaterialDisplayName(row[2] != null ? row[2].toString() : "");
                dto.setApril(row[3] != null ? Double.parseDouble(row[3].toString()) : 0.0);
                dto.setMay(row[4] != null ? Double.parseDouble(row[4].toString()) : 0.0);
                dto.setJune(row[5] != null ? Double.parseDouble(row[5].toString()) : 0.0);
                dto.setJuly(row[6] != null ? Double.parseDouble(row[6].toString()) : 0.0);
                dto.setAugust(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
                dto.setSeptember(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
                dto.setOctober(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
                dto.setNovember(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
                dto.setDecember(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
                dto.setJanuary(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
                dto.setFebruary(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
                dto.setMarch(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);

                dto.setRemarks(row[16] != null ? row[16].toString() : " ");
                aOPMCCalculatedDataDTOList.add(dto);
            }

            Map<String, Object> map = new HashMap<>();
            map.put("aopMCCalculatedDataDTOList", aOPMCCalculatedDataDTOList);
            
            aopMessageVM.setCode(200);
            aopMessageVM.setData(map);
            aopMessageVM.setMessage("Data fetched successfully");
            return aopMessageVM;

        } catch (IllegalArgumentException e) {
            throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to fetch data", ex);
        }
    }
    
    public List<Object[]> getMaxAchievedCapacityData(String year, String plantId, String viewName) {
        String sql = "SELECT TOP (1000) "
                   + "Id, Material_FK_Id, MaterialDisplayName, "
                   + "April, May, June, July, August, September, October, November, December, "
                   + "January, February, March, "
                   + "FinancialYear, Remarks, CreatedOn, ModifiedOn, UpdatedBy, PlantId "
                   + "FROM " + viewName + " " 
                   + "WHERE PlantId = :plantId "
                   + "AND FinancialYear = :year";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("year", year);
        query.setParameter("plantId", plantId);

        return query.getResultList();
    }

    @Override
    public AOPMessageVM getDesignCapacity(String plantId, String year) {
        AOPMessageVM aopMessageVM = new AOPMessageVM();
        try {
        	List<Object[]> obj =null;
        	 Plants plant = plantsRepository.findById(UUID.fromString(plantId))
 	                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
 	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
 	                .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
        	 if(vertical.getName().equalsIgnoreCase("CRACKER")) {
        		 String procedureName=vertical.getName()+"_GetAOPMCValuesDesignCapacity";
 	        	obj= findByYearAndPlantId(year, UUID.fromString(plantId) ,  procedureName);
 	        }else {
 	        	 obj = aOPMCCalculatedDataRepository.getDesignCapacityData(year, plantId);
 	        }
            
            List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList = new ArrayList<>();

            for (Object[] row : obj) {
                AOPMCCalculatedDataDTO dto = new AOPMCCalculatedDataDTO();
                dto.setId(row[0] != null ? row[0].toString() : "");
                dto.setMaterialFKId(row[1] != null ? row[1].toString() : "");
                dto.setMaterialDisplayName(row[2] != null ? row[2].toString() : "");
                dto.setApril(row[3] != null ? Double.parseDouble(row[3].toString()) : 0.0);
                dto.setMay(row[4] != null ? Double.parseDouble(row[4].toString()) : 0.0);
                dto.setJune(row[5] != null ? Double.parseDouble(row[5].toString()) : 0.0);
                dto.setJuly(row[6] != null ? Double.parseDouble(row[6].toString()) : 0.0);
                dto.setAugust(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
                dto.setSeptember(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
                dto.setOctober(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
                dto.setNovember(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
                dto.setDecember(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
                dto.setJanuary(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
                dto.setFebruary(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
                dto.setMarch(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);
                
                dto.setRemarks(row[16] != null ? row[16].toString() : " ");
                aOPMCCalculatedDataDTOList.add(dto);
            }

            Map<String, Object> map = new HashMap<>();
            map.put("aopMCCalculatedDataDTOList", aOPMCCalculatedDataDTOList);
            
            aopMessageVM.setCode(200);
            aopMessageVM.setData(map);
            aopMessageVM.setMessage("Data fetched successfully");
            return aopMessageVM;

        } catch (IllegalArgumentException e) {
            throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to fetch data", ex);
        }
    }

	@Override
	public List<AOPMCCalculatedDataDTO> editAOPMCCalculatedData(List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList,
			boolean isFromExcel, String year, String plantId) {
		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
		Sites site = siteRepository.findById(plant.getSiteFkId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
		try {
			boolean outerChanged = false;
			// String finYear = "";
			// UUID plantId = null;
			List<AOPMCCalculatedDataDTO> failedList = new ArrayList<>();

			for (AOPMCCalculatedDataDTO aOPMCCalculatedDataDTO : aOPMCCalculatedDataDTOList) {
				if (aOPMCCalculatedDataDTO.getSaveStatus() != null
						&& aOPMCCalculatedDataDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(aOPMCCalculatedDataDTO);
					continue;
				}
				Optional<AOPMCCalculatedData> aOPMCCalculatedDataOptional =null;
				AOPMCCalculatedData aOPMCCalculatedData = new AOPMCCalculatedData();
				if (aOPMCCalculatedDataDTO.getId() == null || aOPMCCalculatedDataDTO.getId().contains("#")) {
					aOPMCCalculatedDataOptional =	aOPMCCalculatedDataRepository.findByPlantYearAndMaterial(UUID.fromString(plantId),year,UUID.fromString(aOPMCCalculatedDataDTO.getMaterialFKId()));
					if (!aOPMCCalculatedDataOptional.isPresent()) {
						aOPMCCalculatedData.setPlantFKId(UUID.fromString(plantId));
						aOPMCCalculatedData.setFinancialYear(year);
						aOPMCCalculatedData.setMaterialFKId(UUID.fromString(aOPMCCalculatedDataDTO.getMaterialFKId()));
						aOPMCCalculatedData.setSiteFKId(site.getId());
						aOPMCCalculatedData.setVerticalFKId(vertical.getId());
						aOPMCCalculatedData.setPlantFKId(plant.getId());					}

				} else {
					 aOPMCCalculatedDataOptional = aOPMCCalculatedDataRepository
							.findById(UUID.fromString(aOPMCCalculatedDataDTO.getId()));

					if (!aOPMCCalculatedDataOptional.isPresent()) {
						aOPMCCalculatedDataDTO.setSaveStatus("Failed");
						aOPMCCalculatedDataDTO.setErrDescription("Record not found");
						failedList.add(aOPMCCalculatedDataDTO);
						continue;
					}
					aOPMCCalculatedData = aOPMCCalculatedDataOptional.get();
				}
				
					
					
					if(vertical.getName().equalsIgnoreCase("Cracker")) {
						updateMaxEthyleneProduction( aOPMCCalculatedDataDTO, plant, year);
					}
					
					
					
					aOPMCCalculatedData.setModifiedOn(new Date());
				
				
				boolean changed = false;
				AOPMCCalculatedData saved = null;
				

				if (!Objects.equals(aOPMCCalculatedData.getJanuary(), aOPMCCalculatedDataDTO.getJanuary())) {
				    aOPMCCalculatedData.setJanuary(aOPMCCalculatedDataDTO.getJanuary());
				    changed = true;
				}

				if (!Objects.equals(aOPMCCalculatedData.getFebruary(), aOPMCCalculatedDataDTO.getFebruary())) {
				    aOPMCCalculatedData.setFebruary(aOPMCCalculatedDataDTO.getFebruary());
				    changed = true;
				}

				if (!Objects.equals(aOPMCCalculatedData.getMarch(), aOPMCCalculatedDataDTO.getMarch())) {
				    aOPMCCalculatedData.setMarch(aOPMCCalculatedDataDTO.getMarch());
				    changed = true;
				}

				if (!Objects.equals(aOPMCCalculatedData.getApril(), aOPMCCalculatedDataDTO.getApril())) {
				    aOPMCCalculatedData.setApril(aOPMCCalculatedDataDTO.getApril());
				    changed = true;
				}

				if (!Objects.equals(aOPMCCalculatedData.getMay(), aOPMCCalculatedDataDTO.getMay())) {
				    aOPMCCalculatedData.setMay(aOPMCCalculatedDataDTO.getMay());
				    changed = true;
				}

				if (!Objects.equals(aOPMCCalculatedData.getJune(), aOPMCCalculatedDataDTO.getJune())) {
				    aOPMCCalculatedData.setJune(aOPMCCalculatedDataDTO.getJune());
				    changed = true;
				}

				if (!Objects.equals(aOPMCCalculatedData.getJuly(), aOPMCCalculatedDataDTO.getJuly())) {
				    aOPMCCalculatedData.setJuly(aOPMCCalculatedDataDTO.getJuly());
				    changed = true;
				}

				if (!Objects.equals(aOPMCCalculatedData.getAugust(), aOPMCCalculatedDataDTO.getAugust())) {
				    aOPMCCalculatedData.setAugust(aOPMCCalculatedDataDTO.getAugust());
				    changed = true;
				}

				if (!Objects.equals(aOPMCCalculatedData.getSeptember(), aOPMCCalculatedDataDTO.getSeptember())) {
				    aOPMCCalculatedData.setSeptember(aOPMCCalculatedDataDTO.getSeptember());
				    changed = true;
				}

				if (!Objects.equals(aOPMCCalculatedData.getOctober(), aOPMCCalculatedDataDTO.getOctober())) {
				    aOPMCCalculatedData.setOctober(aOPMCCalculatedDataDTO.getOctober());
				    changed = true;
				}

				if (!Objects.equals(aOPMCCalculatedData.getNovember(), aOPMCCalculatedDataDTO.getNovember())) {
				    aOPMCCalculatedData.setNovember(aOPMCCalculatedDataDTO.getNovember());
				    changed = true;
				}

				if (!Objects.equals(aOPMCCalculatedData.getDecember(), aOPMCCalculatedDataDTO.getDecember())) {
				    aOPMCCalculatedData.setDecember(aOPMCCalculatedDataDTO.getDecember());
				    changed = true;
				}				
				aOPMCCalculatedData.setUpdatedBy(Utility.getUserName());
				String existingRemarks = aOPMCCalculatedData.getRemarks() != null ? aOPMCCalculatedData.getRemarks() : "";
				String newRemarks = aOPMCCalculatedDataDTO.getRemarks() != null ? aOPMCCalculatedDataDTO.getRemarks() : "";
				boolean remarksChanged = !existingRemarks.equalsIgnoreCase(newRemarks);

				if (remarksChanged && changed) {
				    outerChanged = true;
				    aOPMCCalculatedData.setRemarks(aOPMCCalculatedDataDTO.getRemarks());
				    saved = aOPMCCalculatedDataRepository.save(aOPMCCalculatedData);
				} 
				else if (!remarksChanged && changed) {
				    // If other data changed but remarks remain the same as the database, fail the validation
				    aOPMCCalculatedDataDTO.setErrDescription("Please add/update remark");
				    aOPMCCalculatedDataDTO.setSaveStatus("Failed");
				    failedList.add(aOPMCCalculatedDataDTO);
				}

				if (saved != null && saved.getId() == null) {
					aOPMCCalculatedDataDTO
							.setErrDescription("No record found with this id" + aOPMCCalculatedDataDTO.getId());
					aOPMCCalculatedDataDTO.setSaveStatus("Failed");
				}

			}

			List<ScreenMapping> screenMappingList = screenMappingRepository
					.findByDependentScreen("production-volume-data");
			for (ScreenMapping screenMapping : screenMappingList) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				if (outerChanged) {
					aopCalculationRepository.save(aopCalculation);
				}

			}

			// TODO Auto-generated method stub
			return failedList;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to edit data", ex);
		}
	}
	
	public void updateMaxEthyleneProduction(AOPMCCalculatedDataDTO aOPMCCalculatedDataDTO,Plants plant,String year) {
		
			String materialId=aOPMCCalculatedDataDTO.getMaterialFKId();
			Optional<NormParameters> normParametersOpt=normParametersRepository.findById(UUID.fromString(materialId));
			NormParameters normParameters=normParametersOpt.get();
			if(normParameters.getName().equalsIgnoreCase("4F") || normParameters.getName().equalsIgnoreCase("5F") || normParameters.getName().equalsIgnoreCase("4F+D"))
			{
				UUID normParameter=	normParametersRepository.findIdByPlantFkIdAndNameAndType(plant.getId(),"Max Ethylene Production",normParameters.getName());
				List<NormAttributeTransactions>	normAttributeTransactions=normAttributeTransactionRepository.findByNormParameterIdAndAuditYear(normParameter,year);
				for(NormAttributeTransactions normAttributeTransaction:normAttributeTransactions) {
					int month=normAttributeTransaction.getAopMonth();
					Double value=getValue(month,aOPMCCalculatedDataDTO);
					normAttributeTransaction.setAttributeValue(value.toString());
					normAttributeTransactionRepository.save(normAttributeTransaction);
				}
			}
	}
	
	public Double getValue(int month, AOPMCCalculatedDataDTO aOPMCCalculatedDataDTO) {
	    switch (month) {
	        case 1: {
	            return aOPMCCalculatedDataDTO.getJanuary();
	        }
	        case 2: {
	            return aOPMCCalculatedDataDTO.getFebruary();
	        }
	        case 3: {
	            return aOPMCCalculatedDataDTO.getMarch();
	        }
	        case 4: {
	            return aOPMCCalculatedDataDTO.getApril();
	        }
	        case 5: {
	            return aOPMCCalculatedDataDTO.getMay();
	        }
	        case 6: {
	            return aOPMCCalculatedDataDTO.getJune();
	        }
	        case 7: {
	            return aOPMCCalculatedDataDTO.getJuly();
	        }
	        case 8: {
	            return aOPMCCalculatedDataDTO.getAugust();
	        }
	        case 9: {
	            return aOPMCCalculatedDataDTO.getSeptember();
	        }
	        case 10: {
	            return aOPMCCalculatedDataDTO.getOctober();
	        }
	        case 11: {
	            return aOPMCCalculatedDataDTO.getNovember();
	        }
	        case 12: {
	            return aOPMCCalculatedDataDTO.getDecember();
	        }
	        default:
	            // Handle invalid month input, e.g., return null or throw an exception
	            return null;
	    }
	}

	@Override
	public AOPMessageVM getAOPMCCalculatedDataSP(String plantId, String finYear) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

			UUID siteId = site.getId();
			UUID verticalId = vertical.getId();
			String storedProcedure = vertical.getName() + "_LoadMCValues";

			String callSql = "{call " + storedProcedure + "(?, ?, ?, ?)}";

			try (Connection connection = dataSource.getConnection();
					CallableStatement stmt = connection.prepareCall(callSql)) {

				// Set parameters in the correct order
				stmt.setString(1, finYear); // @finYear
				stmt.setString(2, plantId); // @plantId
				stmt.setString(3, verticalId.toString()); // @verticalId
				stmt.setString(4, siteId.toString()); // @siteId

				// Execute the stored procedure
				int rowsAffected = stmt.executeUpdate();

				// Optional: commit if auto-commit is off
				if (!connection.getAutoCommit()) {
					connection.commit();
				}

				aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId),
						finYear, "production-volume-data");

				List<ScreenMapping> screenMappingList = screenMappingRepository
						.findByDependentScreen("production-volume-data");
				for (ScreenMapping screenMapping : screenMappingList) {
					if (!screenMapping.getCalculationScreen().equalsIgnoreCase(screenMapping.getDependentScreen())) {

						AopCalculation aopCalculation = new AopCalculation();
						aopCalculation.setAopYear(finYear);
						aopCalculation.setIsChanged(true);
						aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
						aopCalculation.setPlantId(UUID.fromString(plantId));
						aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
						aopCalculationRepository.save(aopCalculation);
					}
				}

				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("SP Executed successfully");
				aopMessageVM.setData(rowsAffected);
				return aopMessageVM;

			} catch (SQLException e) {
				e.printStackTrace();
				return aopMessageVM;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return aopMessageVM;
		}
	}

	public byte[] createExcel(String year, String plantFKId, boolean isAfterSave,
			List<AOPMCCalculatedDataDTO> dtoList) {
		 Plants plant = plantsRepository.findById(UUID.fromString(plantFKId))
	                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
	                .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
		try {
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;
			// List<List<Object>> rows = new ArrayList<>();
			List<Object[]> obj =null;
			List<List<Object>> rows = new ArrayList<>();
			if (!isAfterSave) {
				if(vertical.getName().equalsIgnoreCase("CRACKER")) {
					String procedureName=vertical.getName()+"_GetAOPMCValues";
		        	obj= findByYearAndPlantId(year, UUID.fromString(plantFKId) ,  procedureName);
		        }else {
		        	obj = aOPMCCalculatedDataRepository.getDataMCUValuesAllData(year, plantFKId.toString());
		        }
				
				 
				for (Object[] row : obj) {

					List<Object> list = new ArrayList<>();
					list.add(row[24] != null ? row[24].toString() : null);
					list.add(row[4] != null ? Double.parseDouble(row[4].toString()) : null);
					list.add(row[5] != null ? Double.parseDouble(row[5].toString()) : null);
					list.add(row[6] != null ? Double.parseDouble(row[6].toString()) : null);
					list.add(row[7] != null ? Double.parseDouble(row[7].toString()) : null);
					list.add(row[8] != null ? Double.parseDouble(row[8].toString()) : null);
					list.add(row[9] != null ? Double.parseDouble(row[9].toString()) : null);
					list.add(row[10] != null ? Double.parseDouble(row[10].toString()) : null);
					list.add(row[11] != null ? Double.parseDouble(row[11].toString()) : null);
					list.add(row[12] != null ? Double.parseDouble(row[12].toString()) : null);
					list.add(row[13] != null ? Double.parseDouble(row[13].toString()) : null);
					list.add(row[14] != null ? Double.parseDouble(row[14].toString()) : null);
					list.add(row[15] != null ? Double.parseDouble(row[15].toString()) : null);
					list.add(row[17] != null ? row[17].toString() : " ");

					list.add(row[0] != null ? row[0].toString() : null);
					// list.add(row[1] != null ? row[1].toString() : null);
					// list.add(row[2] != null ? row[2].toString() : null);
					
					 list.add(row[3] != null ? row[3].toString() : null);
					// list.add(row[16] != null ? row[16].toString() : null);
					// list.add(row[22] != null ? row[22].toString() : null);
					rows.add(list);
				}
			} else {
				for (AOPMCCalculatedDataDTO aopMCCalculatedDataDTO : dtoList) {
					List<Object> list = new ArrayList<>();
					list.add(aopMCCalculatedDataDTO.getProductName());
					list.add(aopMCCalculatedDataDTO.getApril());
					list.add(aopMCCalculatedDataDTO.getMay());
					list.add(aopMCCalculatedDataDTO.getJune());
					list.add(aopMCCalculatedDataDTO.getJuly());
					list.add(aopMCCalculatedDataDTO.getAugust());
					list.add(aopMCCalculatedDataDTO.getSeptember());
					list.add(aopMCCalculatedDataDTO.getOctober());
					list.add(aopMCCalculatedDataDTO.getNovember());
					list.add(aopMCCalculatedDataDTO.getDecember());
					list.add(aopMCCalculatedDataDTO.getJanuary());
					list.add(aopMCCalculatedDataDTO.getFebruary());
					list.add(aopMCCalculatedDataDTO.getMarch());
					list.add(aopMCCalculatedDataDTO.getRemarks());
					list.add(aopMCCalculatedDataDTO.getId());
					// list.add(aopMCCalculatedDataDTO.getSiteFKId());
					// list.add(aopMCCalculatedDataDTO.getPlantFKId());
					 list.add(aopMCCalculatedDataDTO.getMaterialFKId());
					// list.add(aopMCCalculatedDataDTO.getFinancialYear());
					// list.add(aopMCCalculatedDataDTO.getVerticalFKId());
					list.add(aopMCCalculatedDataDTO.getSaveStatus());
					list.add(aopMCCalculatedDataDTO.getErrDescription());
					rows.add(list);
				}
			}

			// Data rows

			List<String> innerHeaders = new ArrayList<>();
			innerHeaders.add("Particulars");
			List<String> monthsList = getAcademicYearMonths(year);
			innerHeaders.addAll(monthsList);
			innerHeaders.add("Remarks");
			innerHeaders.add("Id");
			// innerHeaders.add("SiteFKId");
			// innerHeaders.add("PlantFKId");
			 innerHeaders.add("MaterialFKId");
			// innerHeaders.add("FinancialYear");
			// innerHeaders.add("VerticalFKId");
			if (isAfterSave) {
				innerHeaders.add("Status");
				innerHeaders.add("Error Description");
			}
			List<List<String>> headers = new ArrayList<>();
			headers.add(innerHeaders);

			for (List<String> headerRowData : headers) {
				Row headerRow = sheet.createRow(currentRow++);
				for (int col = 0; col < headerRowData.size(); col++) {
					Cell cell = headerRow.createCell(col);
					cell.setCellValue(headerRowData.get(col));
					cell.setCellStyle(Utility.createBoldBorderedStyle(workbook));
				}
			}
			for (List<Object> rowData : rows) {
				Row row1 = sheet.createRow(currentRow++);
				for (int col = 0; col < rowData.size(); col++) {
					Cell cell = row1.createCell(col);
					Object value = rowData.get(col);

					if (value instanceof Number) {
						cell.setCellValue(((Number) value).doubleValue()); // Handles Integer, Double, etc.
					} else if (value instanceof Boolean) {
						cell.setCellValue((Boolean) value);
					} else if (value != null) {
						cell.setCellValue(value.toString());
					} else {
						cell.setCellValue("");
					}

				}
			}
			sheet.setColumnHidden(14, true);
			sheet.setColumnHidden(15, true);
			// sheet.setColumnHidden(15, true);
			// sheet.setColumnHidden(16, true);
			// sheet.setColumnHidden(17, true);
			// sheet.setColumnHidden(18, true);
			// sheet.setColumnHidden(19, true);
			try {// (FileOutputStream fileOut = new FileOutputStream("output/generated.xlsx")) {

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				workbook.write(outputStream);
				workbook.close();
				return outputStream.toByteArray();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public AOPMessageVM importExcelPE(String year, String plantFKId, MultipartFile file) {
	    try {
	        Map<String, List<AOPMCCalculatedDataDTO>> map = readDataPE(file.getInputStream(), UUID.fromString(plantFKId), year);
	        
	        List<AOPMCCalculatedDataDTO> allFailedRecords = new ArrayList<>();
	        Map<String, List<AOPMCCalculatedDataDTO>> mapForExcel = new HashMap<>();
	        for (String key : map.keySet()) {
	            List<AOPMCCalculatedDataDTO> failedList = editAOPMCCalculatedData(map.get(key), true, year, plantFKId);
	            
	            if (failedList != null && !failedList.isEmpty()) {
	                allFailedRecords.addAll(failedList);
	                mapForExcel.put(key, failedList);
	            }
	        }
	        AOPMessageVM aopMessageVM = new AOPMessageVM();
	        
	        if (!allFailedRecords.isEmpty()) {
	            byte[] fileByteArray = createExcel(year, plantFKId, true, allFailedRecords);
	            String base64File = Base64.getEncoder().encodeToString(fileByteArray);
	            
	            aopMessageVM.setData(base64File);
	            aopMessageVM.setCode(400);
	            aopMessageVM.setMessage("Partial data has been saved. Please check the downloaded file for errors.");
	        } else {
	            aopMessageVM.setCode(200);
	            aopMessageVM.setMessage("All data has been saved successfully");
	        }

	        return aopMessageVM;

	    } catch (Exception e) {
	        e.printStackTrace();
	        AOPMessageVM errorVM = new AOPMessageVM();
	        errorVM.setCode(500);
	        errorVM.setMessage("Error processing file: " + e.getMessage());
	        return errorVM;
	    }
	}
	
	@Override
	public AOPMessageVM importExcel(String year, String plantFKId, MultipartFile file) {
		// TODO Auto-generated method stub
		try {

			List<AOPMCCalculatedDataDTO> data = readData(file.getInputStream(), UUID.fromString(plantFKId), year);

			List<AOPMCCalculatedDataDTO> failedRecords = editAOPMCCalculatedData(data, true, year, plantFKId);

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = createExcel(year, plantFKId, true, failedRecords);
				String base64File = Base64.getEncoder().encodeToString(fileByteArray);
				aopMessageVM.setData(base64File);
				aopMessageVM.setCode(400);
				aopMessageVM.setMessage("Partial data has been saved");
			} else {
				// aopMessageVM.setData();
				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("All data has been saved");
			}

			return aopMessageVM;

			// return ResponseEntity.ok(data);
		} catch (Exception e) {
			e.printStackTrace();
			// return ResponseEntity.internalServerError().build();
		}
		return null;
	}
	
	public static List<String> getAcademicYearMonths(String year) {
		List<String> months = new ArrayList<>();
		int startYear = Integer.parseInt(year.substring(0, 4));
		int nextYear = startYear + 1;

		// Apr to Dec of startYear
		for (int month = 4; month <= 12; month++) {
			String label = formatMonthYear(month, startYear);
			months.add(label);
		}

		// Jan to Mar of nextYear
		for (int month = 1; month <= 3; month++) {
			String label = formatMonthYear(month, nextYear);
			months.add(label);
		}

		return months;
	}

	private static String formatMonthYear(int month, int year) {
		LocalDate date = LocalDate.of(year, month, 1);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH);
		return date.format(formatter);
	}
	
	public Map<String, List<AOPMCCalculatedDataDTO>> readDataPE(InputStream inputStream, UUID plantFKId, String year) {

		Map<String, List<AOPMCCalculatedDataDTO>> map = new HashMap<>();
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {

			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			List<AOPMCCalculatedDataDTO> aopMCCalculatedDataDTOs = new ArrayList<>();
			if (rowIterator.hasNext())
				rowIterator.next(); // Skip header

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				Cell tableIdCell = row.getCell(16, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
				if (tableIdCell == null || tableIdCell.getCellType() != CellType.STRING) {
					continue;
				}
				if(!tableIdCell.toString().equalsIgnoreCase("ProposedOperatingCapacity")) {
					continue;
				}

				AOPMCCalculatedDataDTO dto = new AOPMCCalculatedDataDTO();

				try {

					dto.setProductName(getStringCellValue(row.getCell(0), dto));
					dto.setApril(getNumericCellValue(row.getCell(1), dto));
					dto.setMay(getNumericCellValue(row.getCell(2), dto));
					dto.setJune(getNumericCellValue(row.getCell(3), dto));
					dto.setJuly(getNumericCellValue(row.getCell(4), dto));
					dto.setAugust(getNumericCellValue(row.getCell(5), dto));
					dto.setSeptember(getNumericCellValue(row.getCell(6), dto));
					dto.setOctober(getNumericCellValue(row.getCell(7), dto));
					dto.setNovember(getNumericCellValue(row.getCell(8), dto));
					dto.setDecember(getNumericCellValue(row.getCell(9), dto));
					dto.setJanuary(getNumericCellValue(row.getCell(10), dto));
					dto.setFebruary(getNumericCellValue(row.getCell(11), dto));
					dto.setMarch(getNumericCellValue(row.getCell(12), dto));
					dto.setRemarks(getStringCellValue(row.getCell(13), dto));
					dto.setId(getStringCellValue(row.getCell(14), dto));
					
				} catch (Exception e) {
					e.printStackTrace();
					dto.setErrDescription(e.getMessage());
					dto.setSaveStatus("Failed");
				}
				map.putIfAbsent(dto.getTableId(), new ArrayList<>());

				map.get(dto.getTableId()).add(dto);
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to read Data", e);
		}

		return map;
	}
	
	public List<AOPMCCalculatedDataDTO> readData(InputStream inputStream, UUID plantFKId, String year) {
		List<AOPMCCalculatedDataDTO> prodList = new ArrayList<>();
		Plants plant = plantsRepository.findById(plantFKId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
        Sites site = siteRepository.findById(plant.getSiteFkId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();

			if (rowIterator.hasNext())
				rowIterator.next(); // Skip header

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				AOPMCCalculatedDataDTO dto = new AOPMCCalculatedDataDTO();
				try {
					dto.setProductName(getStringCellValue(row.getCell(0), dto));
					dto.setApril(getNumericCellValue(row.getCell(1), dto));
					dto.setMay(getNumericCellValue(row.getCell(2), dto));
					dto.setJune(getNumericCellValue(row.getCell(3), dto));
					dto.setJuly(getNumericCellValue(row.getCell(4), dto));
					dto.setAugust(getNumericCellValue(row.getCell(5), dto));
					dto.setSeptember(getNumericCellValue(row.getCell(6), dto));
					dto.setOctober(getNumericCellValue(row.getCell(7), dto));
					dto.setNovember(getNumericCellValue(row.getCell(8), dto));
					dto.setDecember(getNumericCellValue(row.getCell(9), dto));
					dto.setJanuary(getNumericCellValue(row.getCell(10), dto));
					dto.setFebruary(getNumericCellValue(row.getCell(11), dto));
					dto.setMarch(getNumericCellValue(row.getCell(12), dto));
					dto.setRemarks(getStringCellValue(row.getCell(13), dto));
					dto.setId(getStringCellValue(row.getCell(14), dto));
					// dto.setSiteFKId(getStringCellValue(row.getCell(15), dto));
					// dto.setPlantFKId(getStringCellValue(row.getCell(16), dto));
					 dto.setMaterialFKId(getStringCellValue(row.getCell(15), dto));
					// dto.setFinancialYear(getStringCellValue(row.getCell(18), dto));
					// dto.setVerticalFKId(getStringCellValue(row.getCell(19), dto));
					 dto.setVerticalFKId(vertical.getId().toString());
					 dto.setSiteFKId(site.getId().toString());
					 dto.setPlantFKId(plant.getId().toString());
					 dto.setFinancialYear(year);
				} catch (Exception e) {
					e.printStackTrace();
					dto.setErrDescription(e.getMessage());
					dto.setSaveStatus("Failed");
				}
				prodList.add(dto);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return prodList;
	}


	private static String getStringCellValue(Cell cell, AOPMCCalculatedDataDTO dto) {
	    try {
	        if (cell == null || cell.getCellType() == CellType.BLANK) {
	            return null;
	        }
	        cell.setCellType(CellType.STRING);
	        String val = cell.getStringCellValue();
	        if (val == null || val.trim().isEmpty()) {
	            return null;
	        }

	        return val.trim();
	    } catch (Exception e) {
	        dto.setSaveStatus("Failed");
	        dto.setErrDescription("Invalid format in string cell");
	        e.printStackTrace();
	        return null;
	    }
	}

	private static Double getNumericCellValue(Cell cell, AOPMCCalculatedDataDTO dto) {
	    if (cell == null || cell.getCellType() == CellType.BLANK) {
	        return null;
	    }
	    if (cell.getCellType() == CellType.NUMERIC) {
	        return cell.getNumericCellValue();
	    } 
	    if (cell.getCellType() == CellType.STRING) {
	        try {
	            String val = cell.getStringCellValue();
	            if (val == null || val.trim().isEmpty()) {
	                return null;
	            }
	            
	            return Double.parseDouble(val.trim());
	        } catch (NumberFormatException e) {
	            dto.setSaveStatus("Failed");
	            dto.setErrDescription("Numeric value expected, but found: " + cell.getStringCellValue());
	        }
	    }
	    
	    return null;
	}	
	@Override
	public AOPMessageVM updateDesignCapacity(String plantId, String year,
			List<AOPMCCalculatedDataDTO> aopMCCalculatedDataDTOList) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			List<MCUDesignCapacity> mcuValueCapacityList = new ArrayList<>();
			for (AOPMCCalculatedDataDTO aopMCCalculatedDataDTO : aopMCCalculatedDataDTOList) {
				Optional<MCUDesignCapacity> optMCUValueCapacity =null;
				MCUDesignCapacity mcuValueCapacity =null;
				if(aopMCCalculatedDataDTO.getId()==null) {
					optMCUValueCapacity =	mcuValueCapacityRepository.findCapacityDetails(UUID.fromString(plantId),year,UUID.fromString(aopMCCalculatedDataDTO.getMaterialFKId()));
				
				}else {
					optMCUValueCapacity = mcuValueCapacityRepository
							.findById(UUID.fromString(aopMCCalculatedDataDTO.getId()));
				}
				
				if (optMCUValueCapacity.isPresent()) {
					 mcuValueCapacity = optMCUValueCapacity.get();
				}else {
					 mcuValueCapacity =new MCUDesignCapacity();
					mcuValueCapacity.setMaterialFkId(UUID.fromString(aopMCCalculatedDataDTO.getMaterialFKId()));
					mcuValueCapacity.setPlantId(UUID.fromString(plantId));
					mcuValueCapacity.setFinancialYear(year);
					}
				mcuValueCapacity.setApril(aopMCCalculatedDataDTO.getApril());
				mcuValueCapacity.setMay(aopMCCalculatedDataDTO.getMay());
				mcuValueCapacity.setJune(aopMCCalculatedDataDTO.getJune());
				mcuValueCapacity.setJuly(aopMCCalculatedDataDTO.getJuly());
				mcuValueCapacity.setAugust(aopMCCalculatedDataDTO.getAugust());
				mcuValueCapacity.setSeptember(aopMCCalculatedDataDTO.getSeptember());
				mcuValueCapacity.setOctober(aopMCCalculatedDataDTO.getOctober());
				mcuValueCapacity.setNovember(aopMCCalculatedDataDTO.getNovember());
				mcuValueCapacity.setDecember(aopMCCalculatedDataDTO.getDecember());
				mcuValueCapacity.setJanuary(aopMCCalculatedDataDTO.getJanuary());
				mcuValueCapacity.setFebruary(aopMCCalculatedDataDTO.getFebruary());
				mcuValueCapacity.setMarch(aopMCCalculatedDataDTO.getMarch());
				mcuValueCapacity.setModifiedOn(new Date());
				mcuValueCapacity.setRemarks(aopMCCalculatedDataDTO.getRemarks());
				mcuValueCapacity.setUpdatedBy(Utility.getUserName());
				mcuValueCapacityList.add(mcuValueCapacityRepository.save(mcuValueCapacity));


			}
			aopMessageVM.setCode(200);
			aopMessageVM.setData(mcuValueCapacityList);
			aopMessageVM.setMessage("Data Updated Successfully");
		} catch (Exception e) {
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return aopMessageVM;
	}

	@Override
	public AOPMessageVM updateMaxAchievedCapacity(String plantId, String year,
			List<AOPMCCalculatedDataDTO> aopMCCalculatedDataDTOs) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<MCUMaxCapacity> mcuMaxCapacities = new ArrayList<MCUMaxCapacity>();
		try {
			for(AOPMCCalculatedDataDTO aopMCCalculatedDataDTO: aopMCCalculatedDataDTOs) {
				MCUMaxCapacity mcuMaxCapacity=null;
				Optional<MCUMaxCapacity> mcuMaxCapacityOpt =null;
				if(aopMCCalculatedDataDTO.getId()==null) {
					
					mcuMaxCapacityOpt =	mcuMaxCapacityRepository.findMaxCapacity(UUID.fromString(plantId),year,UUID.fromString(aopMCCalculatedDataDTO.getMaterialFKId()));
				}else {
					mcuMaxCapacityOpt = mcuMaxCapacityRepository.findById(UUID.fromString(aopMCCalculatedDataDTO.getId()));
				}
				
				if(mcuMaxCapacityOpt.isPresent()) {
					mcuMaxCapacity=mcuMaxCapacityOpt.get();
				}else {
					aopMessageVM.setCode(201);
					aopMessageVM.setData(aopMCCalculatedDataDTOs);
					aopMessageVM.setMessage("No record found with id = "+aopMCCalculatedDataDTO.getId());
					return aopMessageVM;
				}
				mcuMaxCapacity.setApril(aopMCCalculatedDataDTO.getApril());
				mcuMaxCapacity.setMay(aopMCCalculatedDataDTO.getMay());
				mcuMaxCapacity.setJune(aopMCCalculatedDataDTO.getJune());
				mcuMaxCapacity.setJuly(aopMCCalculatedDataDTO.getJuly());
				mcuMaxCapacity.setAugust(aopMCCalculatedDataDTO.getAugust());
				mcuMaxCapacity.setSeptember(aopMCCalculatedDataDTO.getSeptember());
				mcuMaxCapacity.setOctober(aopMCCalculatedDataDTO.getOctober());
				mcuMaxCapacity.setNovember(aopMCCalculatedDataDTO.getNovember());
				mcuMaxCapacity.setDecember(aopMCCalculatedDataDTO.getDecember());
				mcuMaxCapacity.setJanuary(aopMCCalculatedDataDTO.getJanuary());
				mcuMaxCapacity.setFebruary(aopMCCalculatedDataDTO.getFebruary());
				mcuMaxCapacity.setMarch(aopMCCalculatedDataDTO.getMarch());
				mcuMaxCapacity.setRemarks(aopMCCalculatedDataDTO.getRemarks());
				mcuMaxCapacities.add(mcuMaxCapacityRepository.save(mcuMaxCapacity));
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		aopMessageVM.setCode(200);
		aopMessageVM.setData(mcuMaxCapacities);
		aopMessageVM.setMessage("Data updated successfully");
		return aopMessageVM;
	}
	
	public byte[] exportProductionTarget(String year, String plantId, boolean isAfterSave,
	        Map<String, List<AOPMCCalculatedDataDTO>> mapForExcel) {
	    try {
	        Plants plant = plantsRepository.findById(UUID.fromString(plantId))
	                .orElseThrow(() -> new RuntimeException("Plant not found"));
	        
	        Optional<ExcelConfigurations> optExcelConfiguration = excelConfigurationsRepository
	                .findByExcelIdAndVerticalFkIdAndSiteFkId("production_target", plant.getVerticalFKId(), plant.getSiteFkId());

	        if (optExcelConfiguration.isPresent()) {
	            String structureJson = optExcelConfiguration.get().getJsonValue();
	            ObjectMapper mapper = new ObjectMapper();
	            Map<String, List<List<Object>>> data = new HashMap<>();
	            Map<String, Object> structure = mapper.readValue(structureJson, Map.class);

	            for (String sheetName : structure.keySet()) {
	                Map<String, Object> sheetData = (Map<String, Object>) structure.get(sheetName);
	                List<Map<String, Object>> tables = (List<Map<String, Object>>) sheetData.get(ExcelConstants.TABLES);

	                for (Map<String, Object> table : tables) {
	                    String tableId = (String) table.get(ExcelConstants.TABLEID);
	                    String dataInput = (String) table.get(ExcelConstants.DATA_INPUT); // Logic identifier
	                    List<String> headers = (List<String>) table.get(ExcelConstants.HEADERS);
	                    List<List<String>> headersOuterTitles = (List<List<String>>) table.get(ExcelConstants.HEADERSTITLES);
	                    Integer startingIndexofMonths = (Integer) table.get(ExcelConstants.STARTING_INDEX_OF_MONTHS);
	                    if (startingIndexofMonths != null) {
	                    	if ("DesignCapacity".equalsIgnoreCase(dataInput) || "MaxAchievedCapacity".equalsIgnoreCase(dataInput)) {
	                    	     headersOuterTitles.get(0).addAll(startingIndexofMonths, excelUtilityService.getMonths(year));
	                        } else {
	                            headersOuterTitles.get(0).addAll(startingIndexofMonths, excelUtilityService.getAcademicYearMonths(year));  
	                        }
	                    }
	                    
	                    List<List<Object>> dataList = new ArrayList<>();

	                    if (isAfterSave) {
	                        if (!mapForExcel.containsKey(tableId)) {
	                            table.put("hideTable", true);
	                            continue;
	                        }
	                        
	                        headers.add("saveStatus");
	                        headers.add("errDescription");
	                        headersOuterTitles.get(0).add("SaveStatus");
	                        headersOuterTitles.get(0).add("ErrDescription");

	                        populateRowsFromDTOs(mapForExcel.get(tableId), headers, tableId, dataList);

	                    } else {
	                        List<AOPMCCalculatedDataDTO> sourceDTOs = new ArrayList<>();
	                        AOPMessageVM vm = null;

	                        if ("DesignCapacity".equalsIgnoreCase(dataInput)) {
	                            vm = getDesignCapacity(plantId, year);
	                        } else if ("MaxAchievedCapacity".equalsIgnoreCase(dataInput)) {
	                            vm = getMaxAchievedCapacity(plantId, year);
	                        } else if ("ProposedOperatingCapacity".equalsIgnoreCase(dataInput)) {
	                            vm = getAOPMCCalculatedData(plantId, year);
	                        }else if ("SummaryProposedOperatingCapacity".equalsIgnoreCase(dataInput)) {
	                            vm = getSummaryOfProposedOperating(plantId, year);
	                        }
	                        
	                        if (vm != null && vm.getData() != null) {
	                            Map<String, Object> dataMap = (Map<String, Object>) vm.getData();
	                            sourceDTOs = (List<AOPMCCalculatedDataDTO>) dataMap.get("aopMCCalculatedDataDTOList");
	                        }

	                        if (sourceDTOs == null || sourceDTOs.isEmpty()) {
	                            table.put("hideTable", true);
	                            continue;
	                        }

	                        populateRowsFromDTOs(sourceDTOs, headers, tableId, dataList);
	                    }

	                    data.put(tableId, dataList);
	                }
	            }
	            return excelUtilityService.generateFlexibleExcel(structure, data);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	/**
	 * Helper method to convert DTO objects into Excel row lists using reflection
	 */
	private void populateRowsFromDTOs(List<AOPMCCalculatedDataDTO> dtos, List<String> headers, String tableId, List<List<Object>> dataList) {
	    for (AOPMCCalculatedDataDTO dto : dtos) {
	        List<Object> row = new ArrayList<>();
	        for (String fieldName : headers) {
	            try {
	                // Skips reflection for custom status fields if they aren't in DTO
	                if(fieldName.equals("saveStatus")) { row.add(dto.getSaveStatus()); continue; }
	                if(fieldName.equals("errDescription")) { row.add(dto.getErrDescription()); continue; }

	                String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
	                Method method = dto.getClass().getMethod(methodName);
	                row.add(method.invoke(dto));
	            } catch (Exception e) {
	                row.add(""); // Add empty cell if field/getter is missing
	            }
	        }
	        // Metadata columns often used by the utility for styling or logic
	        row.add(tableId);
	        
	        // Add editable flag from repository if needed, or default to true
	        row.add(true); 
	        
	        dataList.add(row);
	    }
	}
	private static String capitalize(String str) {
		if (str == null || str.isEmpty())
			return str;
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

}
