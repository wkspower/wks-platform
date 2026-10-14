package com.wks.caseengine.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import javax.sql.DataSource;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;
import com.wks.caseengine.dto.BusinessDemandDataDTO;
import com.wks.caseengine.dto.BusinessDemandMonthlyDTO;
import com.wks.caseengine.dto.NetProductionDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.BusinessDemand;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.NormParameterType;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.BusinessDemandDataRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.NormParameterTypeRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class BusinessDemandDataServiceImpl implements BusinessDemandDataService {

	private static final String EXPORT_SHEET_PROTECTION_PASSWORD = "wks_business_demand_export";

	private static final int MAX_EXPORT_COLUMN_WIDTH = 60 * 256;

	// Extra width so the auto-filter dropdown arrow does not cover the header text
	private static final int FILTER_ARROW_PADDING = 4 * 256;

	@Autowired
	private BusinessDemandDataRepository businessDemandDataRepository;

	@Autowired
	private NormParameterTypeRepository normParameterTypeRepository;

	@Autowired
	private PlantsRepository plantsRepository;

	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;

	@Autowired
	private VerticalsRepository verticalRepository;
	
	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;
	
	@Autowired
	private NormParametersRepository normParametersRepository;
	
	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private ShutdownHistoryService shutdownHistoryService;

	@Autowired
	private AOPMCCalculatedDataService aopMCCalculatedDataService;

	private DataSource dataSource;
	
	public BusinessDemandDataServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}


	
	@Override
	public AOPMessageVM getBusinessDemandData(String year, String plantId) {
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
	                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String viewName = "vwScrn" + verticalName + "BusinessDemand";
			List<Object[]> obj=null;
			if(verticalName.equalsIgnoreCase("CRACKER") || verticalName.equalsIgnoreCase("PE") || verticalName.equalsIgnoreCase("PP") || verticalName.equalsIgnoreCase("PET") || verticalName.equalsIgnoreCase("Elastomer")  || verticalName.equalsIgnoreCase("Chemical") || vertical.getName().equalsIgnoreCase("Staple") || vertical.getName().equalsIgnoreCase("Filament")) {
				String procedureName=verticalName+"_GetBusinessDemand";
				obj=findByYearAndPlantId(year,UUID.fromString(plantId),procedureName);
				Map<String, Object> map = new HashMap<>();

				map.put("businessDemandDataDTOList", getBusinessDemand(obj));
				map.put("aopCalculation", null);
				AOPMessageVM aopMessageVM = new AOPMessageVM();
				aopMessageVM.setCode(200);
				aopMessageVM.setData(map);
				aopMessageVM.setMessage("Data fetched successfully");
				return aopMessageVM;
			}else {
				obj = findByYearAndPlantFkId(year, UUID.fromString(plantId), viewName);
			}
			 
			System.out.println("obj" + obj);
			List<BusinessDemandDataDTO> businessDemandDataDTOList = new ArrayList<>();

			for (Object[] row : obj) {
				BusinessDemandDataDTO businessDemandDataDTO = new BusinessDemandDataDTO();

				businessDemandDataDTO.setId(row[0] != null ? row[0].toString() : null);
				businessDemandDataDTO.setRemark(row[1] != null ? row[1].toString() : null);
				businessDemandDataDTO.setJan(row[2] != null ? Double.parseDouble(row[2].toString()) : 0.0);
				businessDemandDataDTO.setFeb(row[3] != null ? Double.parseDouble(row[3].toString()) : 0.0);
				businessDemandDataDTO.setMarch(row[4] != null ? Double.parseDouble(row[4].toString()) : 0.0);
				businessDemandDataDTO.setApril(row[5] != null ? Double.parseDouble(row[5].toString()) : 0.0);
				businessDemandDataDTO.setMay(row[6] != null ? Double.parseDouble(row[6].toString()) : 0.0);
				businessDemandDataDTO.setJune(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
				businessDemandDataDTO.setJuly(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
				businessDemandDataDTO.setAug(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
				businessDemandDataDTO.setSep(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
				businessDemandDataDTO.setOct(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
				businessDemandDataDTO.setNov(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
				businessDemandDataDTO.setDec(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
				businessDemandDataDTO.setYear(row[13] != null ? row[14].toString() : null);
				businessDemandDataDTO.setPlantId(row[15] != null ? row[15].toString().toUpperCase() : null);
				businessDemandDataDTO.setNormParameterId(row[16] != null ? row[16].toString() : null);
				businessDemandDataDTO.setAvgTph(row[17] != null ? Double.parseDouble(row[17].toString()) : null);
				businessDemandDataDTO.setDisplayOrder(row[18] != null ? Integer.parseInt(row[18].toString()) : null);
				businessDemandDataDTO.setNormParameterTypeId(row[19] != null ? row[19].toString() : null);
				businessDemandDataDTO.setNormParameterTypeName(row[20] != null ? row[20].toString() : null);
				businessDemandDataDTO.setNormParameterTypeDisplayName(row[21] != null ? row[21].toString() : null);
				businessDemandDataDTO.setIsEditable(row[29] != null ? Boolean.valueOf(row[29].toString()) : null);
				businessDemandDataDTO.setIsVisible(row[30] != null ? Boolean.valueOf(row[30].toString()) : null);
				businessDemandDataDTO.setUOM(row[31] != null ? row[31].toString() : null);
				businessDemandDataDTO.setDisplayName(row[32] != null ? row[32].toString() : null);

				businessDemandDataDTOList.add(businessDemandDataDTO);
			}

			Map<String, Object> map = new HashMap<>();

			List<AopCalculation> aopCalculation = aopCalculationRepository
					.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "business-demand");
			map.put("businessDemandDataDTOList", businessDemandDataDTOList);
			map.put("aopCalculation", aopCalculation);
			AOPMessageVM aopMessageVM = new AOPMessageVM();
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
	public AOPMessageVM getNetProductionData(String year, String plantId) {
	    try {
	        Plants plant = plantsRepository.findById(UUID.fromString(plantId))
	                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

	        Sites site = siteRepository.findById(plant.getSiteFkId())
	                .orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));

	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
	                .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

	        String procedure = vertical.getName() + "_" + site.getName() + "_GetNetProduction";
	        List<Object[]> obj = findNetProductionData(year, UUID.fromString(plantId), procedure);

	       List<NetProductionDTO> netProductionDTOs = new ArrayList<NetProductionDTO>();

	        if (obj != null) {
	            for (Object[] row : obj) {
	                NetProductionDTO newDto = new NetProductionDTO();
	                newDto.setSapMATCode(row[0] != null ? row[0].toString() : "");
	                newDto.setProduct(row[1] != null ? row[1].toString() : "");
	                newDto.setUom(row[2] != null ? row[2].toString() : "");
	                newDto.setApr(row[3] != null ? Double.parseDouble(row[3].toString()) : 0.0);
	                newDto.setMay(row[4] != null ? Double.parseDouble(row[4].toString()) : 0.0);
	                newDto.setJun(row[5] != null ? Double.parseDouble(row[5].toString()) : 0.0);
	                newDto.setJul(row[6] != null ? Double.parseDouble(row[6].toString()) : 0.0);
	                newDto.setAug(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
	                newDto.setSep(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
	                newDto.setOct(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
	                newDto.setNov(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
	                newDto.setDec(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
	                newDto.setJan(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
	                newDto.setFeb(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
	                newDto.setMar(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);

	                netProductionDTOs.add(newDto);
	            }
	        }

	        AOPMessageVM aopMessageVM = new AOPMessageVM();
	        aopMessageVM.setCode(200);
	        aopMessageVM.setData(netProductionDTOs);
	        aopMessageVM.setMessage("Data fetched successfully");
	        return aopMessageVM;

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}
	
	public List<Object[]> findNetProductionData(String aopYear, UUID plantId, String procedureName) {
	    try {
	        String sql = "EXEC " + "[" + procedureName + "]"
	                + " @plantId = :plantId, @aopYear = :aopYear";

	        Query query = entityManager.createNativeQuery(sql);
	        query.setParameter("plantId", plantId);
	        query.setParameter("aopYear", aopYear);

	        return query.getResultList();
	    } catch (RestInvalidArgumentException e) {
	        throw e;
	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}
	
	@Override
	public AOPMessageVM getBusinessDemandLineData(String year, String plantId, String lineId) {
		try {
		
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
	                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		
			List<Object[]> obj=null;
				String procedureName= vertical.getName()+"_"+site.getName()+"_GetBusinessDemand";
				obj=findByYearPlantIdAndLineId(year,UUID.fromString(plantId), UUID.fromString(lineId), procedureName);
		 
			List<BusinessDemandDataDTO> businessDemandDataDTOList = new ArrayList<>();

			for (Object[] row : obj) { 

					BusinessDemandDataDTO businessDemandDataDTO = new BusinessDemandDataDTO();

			businessDemandDataDTO.setId(row[0] != null ? row[0].toString() : null);
			businessDemandDataDTO.setPlantId(row[2] != null ? row[2].toString().toUpperCase() : null);
			businessDemandDataDTO.setNormParameterId(row[4] != null ? row[4].toString() : null);
			businessDemandDataDTO.setDisplayName(row[6] != null ? row[6].toString() : null);
			businessDemandDataDTO.setApril(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
			businessDemandDataDTO.setMay(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
			businessDemandDataDTO.setJune(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
			businessDemandDataDTO.setJuly(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
			businessDemandDataDTO.setAug(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
			businessDemandDataDTO.setSep(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
			businessDemandDataDTO.setOct(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
			businessDemandDataDTO.setNov(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);
			businessDemandDataDTO.setDec(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0);
			
			businessDemandDataDTO.setJan(row[16] != null ? Double.parseDouble(row[16].toString()) : 0.0);
			businessDemandDataDTO.setFeb(row[17] != null ? Double.parseDouble(row[17].toString()) : 0.0);
			businessDemandDataDTO.setMarch(row[18] != null ? Double.parseDouble(row[18].toString()) : 0.0);
			
			businessDemandDataDTO.setYear(row[19] != null ? row[19].toString() : null);
			businessDemandDataDTO.setRemark(row[20] != null ? row[20].toString() : null);
			
			businessDemandDataDTO.setAvgTph(row[24] != null ? Double.parseDouble(row[24].toString()) : null);
			businessDemandDataDTO.setNormParameterTypeId(row[25] != null ? row[25].toString() : null);
			businessDemandDataDTO.setNormParameterTypeName(row[26] != null ? row[26].toString() : null);
			businessDemandDataDTO.setNormParameterTypeDisplayName(row[27] != null ? row[27].toString() : null);
			businessDemandDataDTO.setDisplayOrder(row[29] != null ? Integer.parseInt(row[29].toString()) : null);
			
			
			
			businessDemandDataDTO.setIsEditable(row[31] != null ? Boolean.valueOf(row[31].toString()) : null);
			businessDemandDataDTO.setIsVisible(row[32] != null ? Boolean.valueOf(row[32].toString()) : null);
			businessDemandDataDTO.setUOM(row[33] != null ? row[33].toString() : null);

			businessDemandDataDTO.setLineId(row[35] != null ? row[35].toString() : null);
			businessDemandDataDTOList.add(businessDemandDataDTO);
			}
			Map<String, Object> map = new HashMap<>();
			List<AopCalculation> aopCalculation = aopCalculationRepository
			.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "business-demand");
	        map.put("businessDemandDataDTOList", businessDemandDataDTOList);
	        map.put("aopCalculation", aopCalculation);
			AOPMessageVM aopMessageVM = new AOPMessageVM();
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
	public List<BusinessDemandDataDTO> getBusinessDemandAllData(String year, String plantId) {
		try {
		
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
	                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		
			List<Object[]> obj=null;
				String procedureName= vertical.getName()+"_"+site.getName()+"_GetBusinessDemandAllData";
				obj=findByYearPlantId(year,UUID.fromString(plantId), procedureName);
		 
			List<BusinessDemandDataDTO> businessDemandDataDTOList = new ArrayList<>();

			for (Object[] row : obj) { 

					BusinessDemandDataDTO businessDemandDataDTO = new BusinessDemandDataDTO();

			businessDemandDataDTO.setId(row[0] != null ? row[0].toString() : null);
			businessDemandDataDTO.setPlantId(row[2] != null ? row[2].toString().toUpperCase() : null);
			businessDemandDataDTO.setNormParameterId(row[4] != null ? row[4].toString() : null);
			businessDemandDataDTO.setDisplayName(row[6] != null ? row[6].toString() : null);
			businessDemandDataDTO.setApril(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
			businessDemandDataDTO.setMay(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
			businessDemandDataDTO.setJune(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
			businessDemandDataDTO.setJuly(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
			businessDemandDataDTO.setAug(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
			businessDemandDataDTO.setSep(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
			businessDemandDataDTO.setOct(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
			businessDemandDataDTO.setNov(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);
			businessDemandDataDTO.setDec(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0);
			
			businessDemandDataDTO.setJan(row[16] != null ? Double.parseDouble(row[16].toString()) : 0.0);
			businessDemandDataDTO.setFeb(row[17] != null ? Double.parseDouble(row[17].toString()) : 0.0);
			businessDemandDataDTO.setMarch(row[18] != null ? Double.parseDouble(row[18].toString()) : 0.0);
			
			businessDemandDataDTO.setYear(row[19] != null ? row[19].toString() : null);
			businessDemandDataDTO.setRemark(row[20] != null ? row[20].toString() : null);
			
			businessDemandDataDTO.setAvgTph(row[24] != null ? Double.parseDouble(row[24].toString()) : null);
			businessDemandDataDTO.setNormParameterTypeId(row[25] != null ? row[25].toString() : null);
			businessDemandDataDTO.setNormParameterTypeName(row[26] != null ? row[26].toString() : null);
			businessDemandDataDTO.setNormParameterTypeDisplayName(row[27] != null ? row[27].toString() : null);
			businessDemandDataDTO.setDisplayOrder(row[29] != null ? Integer.parseInt(row[29].toString()) : null);
			
			
			
			businessDemandDataDTO.setIsEditable(row[31] != null ? Boolean.valueOf(row[31].toString()) : null);
			businessDemandDataDTO.setIsVisible(row[32] != null ? Boolean.valueOf(row[32].toString()) : null);
			businessDemandDataDTO.setUOM(row[33] != null ? row[33].toString() : null);

			businessDemandDataDTO.setLineId(row[35] != null ? row[35].toString() : null);
			businessDemandDataDTOList.add(businessDemandDataDTO);
			}

			return businessDemandDataDTOList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	
	public String getCrackerModeDisplayName(String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
			String viewName = "vw" + vertical.getName() + "Modes";

			String sql = "SELECT TOP 1 DisplayName FROM " + viewName
					+ " WHERE PlantId = :plantId AND Type = :type ORDER BY DisplayOrder";
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", UUID.fromString(plantId));
			query.setParameter("type", "1");

			@SuppressWarnings("unchecked")
			List<Object> result = query.getResultList();
			if (result == null || result.isEmpty() || result.get(0) == null) {
				return "";
			}
			return result.get(0).toString();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch cracker mode display name", ex);
		}
	}
	
	public String getCrackerFuelDisplayName(String plantId) {
    try {
        Plants plant = plantsRepository.findById(UUID.fromString(plantId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

        String viewName = "vw" + vertical.getName() + "FuelDropdown";

        String sql = "SELECT TOP 1 DisplayName FROM " + viewName
                + " WHERE PlantId = :plantId ORDER BY DisplayOrder";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("plantId", UUID.fromString(plantId));

        @SuppressWarnings("unchecked")
        List<Object> result = query.getResultList();

        if (result == null || result.isEmpty() || result.get(0) == null) {
            return "";
        }

        return result.get(0).toString();

    } catch (IllegalArgumentException e) {
        throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
    } catch (Exception ex) {
        throw new RuntimeException("Failed to fetch fuel display name", ex);
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

	public List<Object[]> findByYearPlantIdAndLineId(String aopYear, UUID plantId, UUID lineId, String procedureName) {
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

	public List<Object[]> findByYearPlantId(String aopYear, UUID plantId, String procedureName) {
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
	
	
	public List<BusinessDemandDataDTO> getBusinessDemand(List<Object[]> obj){
		List<BusinessDemandDataDTO> businessDemandDataDTOList = new ArrayList<>();

		for (Object[] row : obj) {
			BusinessDemandDataDTO businessDemandDataDTO = new BusinessDemandDataDTO();

			businessDemandDataDTO.setId(row[0] != null ? row[0].toString() : null);
			businessDemandDataDTO.setPlantId(row[2] != null ? row[2].toString().toUpperCase() : null);
			businessDemandDataDTO.setNormParameterId(row[4] != null ? row[4].toString() : null);
			businessDemandDataDTO.setDisplayName(row[6] != null ? row[6].toString() : null);
			businessDemandDataDTO.setApril(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
			businessDemandDataDTO.setMay(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
			businessDemandDataDTO.setJune(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
			businessDemandDataDTO.setJuly(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
			businessDemandDataDTO.setAug(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
			businessDemandDataDTO.setSep(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
			businessDemandDataDTO.setOct(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
			businessDemandDataDTO.setNov(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);
			businessDemandDataDTO.setDec(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0);
			
			businessDemandDataDTO.setJan(row[16] != null ? Double.parseDouble(row[16].toString()) : 0.0);
			businessDemandDataDTO.setFeb(row[17] != null ? Double.parseDouble(row[17].toString()) : 0.0);
			businessDemandDataDTO.setMarch(row[18] != null ? Double.parseDouble(row[18].toString()) : 0.0);
			
			businessDemandDataDTO.setYear(row[19] != null ? row[19].toString() : null);
			businessDemandDataDTO.setRemark(row[20] != null ? row[20].toString() : null);
			
			businessDemandDataDTO.setAvgTph(row[24] != null ? Double.parseDouble(row[24].toString()) : null);
			businessDemandDataDTO.setNormParameterTypeId(row[25] != null ? row[25].toString() : null);
			businessDemandDataDTO.setNormParameterTypeName(row[26] != null ? row[26].toString() : null);
			businessDemandDataDTO.setNormParameterTypeDisplayName(row[27] != null ? row[27].toString() : null);
			businessDemandDataDTO.setDisplayOrder(row[29] != null ? Integer.parseInt(row[29].toString()) : null);
			
			
			
			businessDemandDataDTO.setIsEditable(row[31] != null ? Boolean.valueOf(row[31].toString()) : null);
			businessDemandDataDTO.setIsVisible(row[32] != null ? Boolean.valueOf(row[32].toString()) : null);
			businessDemandDataDTO.setUOM(row[33] != null ? row[33].toString() : null);
			

			businessDemandDataDTOList.add(businessDemandDataDTO);
		}

		return businessDemandDataDTOList;

	}

	
	public AOPMessageVM getBusinessDemand(String year, UUID plantFKId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(plantFKId);
			List<Object[]> obj = new ArrayList<>();
			
				String procedureName = verticalName + "_GetBusinessDemandMonthly";
				obj = getData(year, plantFKId, procedureName);
			
			List<BusinessDemandMonthlyDTO> configurationDTOList = new ArrayList<BusinessDemandMonthlyDTO>();
			int i = 0;
			for (Object[] row : obj) {
				BusinessDemandMonthlyDTO configurationDTO = new BusinessDemandMonthlyDTO();
				configurationDTO.setNormParameterFKId(row[0] != null ? row[0].toString() : "");

				configurationDTO.setJan(row[1] != null ? row[1].toString() : "Propane Min");
				configurationDTO.setFeb(row[2] != null ? row[2].toString() : "Propane Min");
				configurationDTO.setMar(row[3] != null ? row[3].toString() : "Propane Min");
				configurationDTO.setApr(row[4] != null ? row[4].toString() : "Propane Min");
				configurationDTO.setMay(row[5] != null ? row[5].toString() : "Propane Min");
				configurationDTO.setJun(row[6] != null ? row[6].toString() : "Propane Min");
				configurationDTO.setJul(row[7] != null ? row[7].toString() : "Propane Min");
				configurationDTO.setAug(row[8] != null ? row[8].toString() : "Propane Min");
				configurationDTO.setSep(row[9] != null ? row[9].toString() : "Propane Min");
				configurationDTO.setOct(row[10] != null ? row[10].toString() : "Propane Min");
				configurationDTO.setNov(row[11] != null ? row[11].toString() : "Propane Min");
				configurationDTO.setDec(row[12] != null ? row[12].toString() : "Propane Min");
				configurationDTO.setRemarks((row[13] != null ? row[13].toString() : ""));
					configurationDTO.setAuditYear(row[14] != null ? row[14].toString() : "");
					configurationDTO.setUom(row[15] != null ? row[15].toString() : "");
					configurationDTO.setNormType(row[16] != null ? row[16].toString() : "");
					configurationDTO.setIsEditable(row[17] != null ? ((Boolean) row[17]).booleanValue() : null);
					configurationDTO.setProductName(row[18] != null ? row[18].toString() : "");
					configurationDTO.setType(row[19] != null ? row[19].toString() : "");				
					configurationDTOList.add(configurationDTO);
				if (row[14] == null) {
					i++;
				}
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(configurationDTOList);
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

public AOPMessageVM getBusinessDemandMode(String year, UUID plantFKId) {
    AOPMessageVM aopMessageVM = new AOPMessageVM();
    try {
        String verticalName = plantsRepository.findVerticalNameByPlantId(plantFKId);
        List<Object[]> obj = new ArrayList<>();

        String procedureName = verticalName + "_GetBusinessDemandMonthly";
        obj = getData(year, plantFKId, procedureName);

        String defaultModeValue = getCrackerModeDisplayName(plantFKId.toString());
        String defaultFuelValue = getCrackerFuelDisplayName(plantFKId.toString());

        List<BusinessDemandMonthlyDTO> configurationDTOList = new ArrayList<>();

        for (Object[] row : obj) {
            BusinessDemandMonthlyDTO configurationDTO = new BusinessDemandMonthlyDTO();

            String uom = row[15] != null ? row[15].toString() : "";
            String fallbackValue = "Fuel".equalsIgnoreCase(uom) ? defaultFuelValue : defaultModeValue;

            configurationDTO.setNormParameterFKId(row[0] != null ? row[0].toString() : "");

            configurationDTO.setJan(row[1] != null ? row[1].toString() : fallbackValue);
            configurationDTO.setFeb(row[2] != null ? row[2].toString() : fallbackValue);
            configurationDTO.setMar(row[3] != null ? row[3].toString() : fallbackValue);
            configurationDTO.setApr(row[4] != null ? row[4].toString() : fallbackValue);
            configurationDTO.setMay(row[5] != null ? row[5].toString() : fallbackValue);
            configurationDTO.setJun(row[6] != null ? row[6].toString() : fallbackValue);
            configurationDTO.setJul(row[7] != null ? row[7].toString() : fallbackValue);
            configurationDTO.setAug(row[8] != null ? row[8].toString() : fallbackValue);
            configurationDTO.setSep(row[9] != null ? row[9].toString() : fallbackValue);
            configurationDTO.setOct(row[10] != null ? row[10].toString() : fallbackValue);
            configurationDTO.setNov(row[11] != null ? row[11].toString() : fallbackValue);
            configurationDTO.setDec(row[12] != null ? row[12].toString() : fallbackValue);

            configurationDTO.setRemarks(row[13] != null ? row[13].toString() : "");
            configurationDTO.setAuditYear(row[14] != null ? row[14].toString() : "");
            configurationDTO.setUom(uom);
            configurationDTO.setNormType(row[16] != null ? row[16].toString() : "");
            configurationDTO.setIsEditable(row[17] != null ? ((Boolean) row[17]).booleanValue() : null);
            configurationDTO.setProductName(row[18] != null ? row[18].toString() : "");
            configurationDTO.setType(row[19] != null ? row[19].toString() : "");

            configurationDTOList.add(configurationDTO);
        }

        aopMessageVM.setCode(200);
        aopMessageVM.setMessage("Data fetched successfully");
        aopMessageVM.setData(configurationDTOList);
        return aopMessageVM;
    } catch (IllegalArgumentException e) {
        throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
    } catch (Exception ex) {
        ex.printStackTrace();
        throw new RuntimeException("Failed to fetch data", ex);
    }
}


	public List<Object[]> getData(String aopYear, UUID plantId, String procedureName) {
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

	
	public byte[] exportBusinessDemand(String year, String plantId, boolean isAfterSave, List<BusinessDemandDataDTO> dtoList) {

		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		boolean showProductionTarget = vertical.getName().equalsIgnoreCase("MEG");
		 if(vertical.getName().equalsIgnoreCase("Cracker") && site.getName().equalsIgnoreCase("HMD") ) {  
			// seperate export method to handle ON/OFF values
			return exportBusinessDemandV2(year, plantId, isAfterSave, dtoList);
		 }

		 boolean crackerDmd = vertical.getName().equalsIgnoreCase("Cracker") && site.getName().equalsIgnoreCase("DMD");

		try {
			
			List<Boolean> isEditable = new ArrayList<>();

			if (!isAfterSave) {
				Map<String, Object> map = (Map<String, Object>) getBusinessDemandData(year,plantId).getData();
				dtoList = (List<BusinessDemandDataDTO>) map.get("businessDemandDataDTOList");
			}

			Workbook workbook = new XSSFWorkbook();

			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;

			if(showProductionTarget) {

			// Production Target section (read-only) is written first on the same sheet
		      currentRow = writeProductionTargetSection(sheet, workbook, year, plantId, 0);

			// Business Demand title row
			Row bdTitleRow = sheet.createRow(currentRow++);
			Cell bdTitleCell = bdTitleRow.createCell(0);
			bdTitleCell.setCellValue("Business Demand");
			bdTitleCell.setCellStyle(Utility.createBoldBorderedStyle(workbook));

			}
			List<List<Object>> rows = new ArrayList<>();
			
			// Data rows
			for (BusinessDemandDataDTO dto : dtoList) {
			   
			if(crackerDmd && dto.getNormParameterTypeName().equalsIgnoreCase("Feed Stream")) {
				continue;
			}
				List<Object> list = new ArrayList<>();
				
				list.add(dto.getDisplayName());
				list.add(dto.getNormParameterTypeDisplayName());
				list.add(dto.getUOM());
					list.add(dto.getApril());
					list.add(dto.getMay());
					list.add(dto.getJune());
					list.add(dto.getJuly());
					list.add(dto.getAug());
					list.add(dto.getSep());
					list.add(dto.getOct());
					list.add(dto.getNov());
					list.add(dto.getDec());
					list.add(dto.getJan());
					list.add(dto.getFeb());
					list.add(dto.getMarch());
					list.add(dto.getRemark());
					list.add(dto.getId());
					list.add(dto.getNormParameterId());
					
					
					if (isAfterSave) {
						list.add(dto.getSaveStatus());
						list.add(dto.getErrDescription());
					}
					rows.add(list);
				//}
			}

			List<String> innerHeaders = new ArrayList<>();
			
		innerHeaders.add("Particulars");
		innerHeaders.add("Type");
		innerHeaders.add("UOM");
		innerHeaders.add(getMonth( year, 4));
		innerHeaders.add(getMonth( year, 5));
		innerHeaders.add(getMonth( year, 6));
		innerHeaders.add(getMonth( year, 7));
		innerHeaders.add(getMonth( year, 8));
		innerHeaders.add(getMonth( year, 9));
		innerHeaders.add(getMonth( year, 10));
		innerHeaders.add(getMonth( year, 11));
		innerHeaders.add(getMonth( year, 12));
		innerHeaders.add(getMonth( year, 1));
		innerHeaders.add(getMonth( year, 2));
		innerHeaders.add(getMonth( year, 3));
		innerHeaders.add("Remark");
		innerHeaders.add("Id");
		innerHeaders.add("NormParameterId");
		// innerHeaders.add("NormParamterId");
		 //innerHeaders.add("IsEditable");
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
	// Bordered + unlocked style for editable BD cells (months col 3–14 and Remark col 15)
	CellStyle unlockedBorderedStyle = workbook.createCellStyle();
	unlockedBorderedStyle.setLocked(false);
	unlockedBorderedStyle.setBorderBottom(BorderStyle.THIN);
	unlockedBorderedStyle.setBorderTop(BorderStyle.THIN);
	unlockedBorderedStyle.setBorderLeft(BorderStyle.THIN);
	unlockedBorderedStyle.setBorderRight(BorderStyle.THIN);
	// Bordered + locked style for read-only BD cells
	CellStyle lockedBorderedStyle = workbook.createCellStyle();
	lockedBorderedStyle.setLocked(true);
	lockedBorderedStyle.setBorderBottom(BorderStyle.THIN);
	lockedBorderedStyle.setBorderTop(BorderStyle.THIN);
	lockedBorderedStyle.setBorderLeft(BorderStyle.THIN);
	lockedBorderedStyle.setBorderRight(BorderStyle.THIN);
	// Plain bordered style for non-showProductionTarget case
	CellStyle borderedDataStyle = Utility.createBorderedStyle(workbook);

	for (List<Object> rowData : rows) {
		Row row = sheet.createRow(currentRow++);
		for (int col = 0; col < rowData.size(); col++) {
			Cell cell = row.createCell(col);
			Object value = rowData.get(col);

			if (value instanceof Number) {
				cell.setCellValue(((Number) value).doubleValue());
			} else if (value instanceof Boolean) {
				cell.setCellValue((Boolean) value);
			} else if (value != null) {
				cell.setCellValue(value.toString());
			} else {
				cell.setCellValue("");
			}

			if (showProductionTarget) {
				// Month columns (3–14) and Remark (15) remain editable; all others locked
				if ((col >= 3 && col <= 14) || col == 15) {
					cell.setCellStyle(unlockedBorderedStyle);
				} else {
					cell.setCellStyle(lockedBorderedStyle);
				}
			} else {
				cell.setCellStyle(borderedDataStyle);
			}
		}
	}

		// Protect the sheet so that locked PT cells become non-editable
		if (showProductionTarget) {
			sheet.protectSheet("");
		}

	// Auto-size all visible columns (0–15)
	for (int col = 0; col <= 15; col++) {
		sheet.autoSizeColumn(col);
	}

sheet.setColumnHidden(16, true);
sheet.setColumnHidden(17, true);
	//sheet.setColumnHidden(18, true);
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
	public byte[] exportBusinessDemandLine(String year, String plantId, String lineId, boolean isAfterSave, List<BusinessDemandDataDTO> dtoList) {
		


		try {
			
			List<Boolean> isEditable = new ArrayList<>();

			if (!isAfterSave) {
				Map<String, Object> map = (Map<String, Object>) getBusinessDemandLineData(year,plantId,lineId).getData();
				 dtoList = (List<BusinessDemandDataDTO>) map.get("businessDemandDataDTOList");

			}

			Workbook workbook = new XSSFWorkbook();

			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;
			// List<List<Object>> rows = new ArrayList<>();

			List<List<Object>> rows = new ArrayList<>();
			
			// Data rows
			for (BusinessDemandDataDTO dto : dtoList) {
			   
				
					List<Object> list = new ArrayList<>();
					
					list.add(dto.getDisplayName());
					list.add(dto.getUOM());
					list.add(dto.getApril());
					list.add(dto.getMay());
					list.add(dto.getJune());
					list.add(dto.getJuly());
					list.add(dto.getAug());
					list.add(dto.getSep());
					list.add(dto.getOct());
					list.add(dto.getNov());
					list.add(dto.getDec());
					list.add(dto.getJan());
					list.add(dto.getFeb());
					list.add(dto.getMarch());
					list.add(dto.getRemark());
					list.add(dto.getId());  //15
					list.add(dto.getNormParameterId());
					list.add(dto.getLineId());
					
					
					if (isAfterSave) {
						list.add(dto.getSaveStatus());
						list.add(dto.getErrDescription());
					}
					rows.add(list);
				//}
			}

			List<String> innerHeaders = new ArrayList<>();
			
			innerHeaders.add("Particulars");
			innerHeaders.add("UOM");
			innerHeaders.add(getMonth( year, 4));
			innerHeaders.add(getMonth( year, 5));
			innerHeaders.add(getMonth( year, 6));
			innerHeaders.add(getMonth( year, 7));
			innerHeaders.add(getMonth( year, 8));
			innerHeaders.add(getMonth( year, 9));
			innerHeaders.add(getMonth( year, 10));
			innerHeaders.add(getMonth( year, 11));
			innerHeaders.add(getMonth( year, 12));
			innerHeaders.add(getMonth( year, 1));
			innerHeaders.add(getMonth( year, 2));
			innerHeaders.add(getMonth( year, 3));
			innerHeaders.add("Remark");
			innerHeaders.add("Id");
			innerHeaders.add("NormParameterId");
			innerHeaders.add("LineId");
			// innerHeaders.add("NormParamterId");
			 //innerHeaders.add("IsEditable");
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
		final int REMARK_COL_INDEX = 14;
		final int REMARK_COL_WIDTH_CHARS = 50;
		final short DEFAULT_ROW_HEIGHT_TWIPS = 300; // 15pt * 20 twips/pt
		final short LINE_HEIGHT_TWIPS = 300;

		CellStyle wrapStyle = workbook.createCellStyle();
		wrapStyle.setWrapText(true);

		for (List<Object> rowData : rows) {
				
			Row row = sheet.createRow(currentRow++);
			for (int col = 0; col < rowData.size(); col++) {
				Cell cell = row.createCell(col);
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

				if (col == REMARK_COL_INDEX) {
					cell.setCellStyle(wrapStyle);
				}
			}

			// Adjust row height to fit wrapped Remark content
			String remarkText = (rowData.size() > REMARK_COL_INDEX && rowData.get(REMARK_COL_INDEX) != null)
					? rowData.get(REMARK_COL_INDEX).toString() : "";
			if (!remarkText.isEmpty()) {
				int numLines = (int) Math.ceil((double) remarkText.length() / REMARK_COL_WIDTH_CHARS);
				numLines = Math.max(1, numLines);
				row.setHeight((short) (numLines * LINE_HEIGHT_TWIPS));
			} else {
				row.setHeight(DEFAULT_ROW_HEIGHT_TWIPS);
			}
		}

		// Auto-size all visible non-Remark columns based on content
		int totalCols = isAfterSave ? 20 : 18;
		for (int col = 0; col < totalCols; col++) {
			if (col == REMARK_COL_INDEX || col == 15 || col == 16 || col == 17) continue;
			sheet.autoSizeColumn(col);
		}

		// Set a fixed wide width for the Remark column (50 chars * 256 units per char)
		sheet.setColumnWidth(REMARK_COL_INDEX, REMARK_COL_WIDTH_CHARS * 256);

		sheet.setColumnHidden(15, true);
		sheet.setColumnHidden(16, true);
		sheet.setColumnHidden(17, true); // LineId

		//sheet.setColumnHidden(18, true);
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
	public byte[] exportBusinessDemandAllLine(String year, String plantId) {
		try {
			AOPMessageVM lineVm = shutdownHistoryService.getLineDetails(plantId, year);
			List<Map<String, Object>> lines = new ArrayList<>();
			if (lineVm != null && lineVm.getData() instanceof List) {
				for (Object o : (List<?>) lineVm.getData()) {
					if (o instanceof Map) {
						lines.add((Map<String, Object>) o);
					}
				}
			}

			Workbook workbook = new XSSFWorkbook();
			Set<String> usedSheetNames = new HashSet<>();

			if (lines.isEmpty()) {
				writeBusinessDemandLineSheet(workbook, workbook.createSheet("Business Demand"), year,
						new ArrayList<>());
			} else {
				for (Map<String, Object> line : lines) {
					Object idObj = line.get("id");
					if (idObj == null || idObj.toString().isBlank()) {
						continue;
					}
					String lineId = idObj.toString();
					String display = line.get("displayName") != null ? line.get("displayName").toString()
							: (line.get("name") != null ? line.get("name").toString() : "Line");
					String sheetName = uniqueBusinessDemandSheetName(Utility.sanitizeSheetName(display),
							usedSheetNames);
					usedSheetNames.add(sheetName);
				Map<String, Object> map = (Map<String, Object>) getBusinessDemandLineData(year,plantId,lineId).getData();
				List<BusinessDemandDataDTO> lineDtos = (List<BusinessDemandDataDTO>) map.get("businessDemandDataDTOList");

					writeBusinessDemandLineSheet(workbook, workbook.createSheet(sheetName), year, lineDtos);
				}
				if (usedSheetNames.isEmpty()) {
					writeBusinessDemandLineSheet(workbook, workbook.createSheet("Business Demand"), year,
							new ArrayList<>());
				}
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			workbook.close();
			return outputStream.toByteArray();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String uniqueBusinessDemandSheetName(String sanitizedBase, Set<String> used) {
		String name = sanitizedBase;
		int counter = 1;
		while (used.contains(name)) {
			String suffix = "_" + (++counter);
			int maxBase = Math.max(1, 31 - suffix.length());
			String base = sanitizedBase.length() > maxBase ? sanitizedBase.substring(0, maxBase) : sanitizedBase;
			name = base + suffix;
			if (name.length() > 31) {
				name = name.substring(0, 31);
			}
		}
		return name;
	}

	private void writeBusinessDemandLineSheet(Workbook workbook, Sheet sheet, String year,
			List<BusinessDemandDataDTO> dtoList) {
		final int REMARK_COL_INDEX = 15;
		final int REMARK_COL_WIDTH_CHARS = 50;
		final short DEFAULT_ROW_HEIGHT_TWIPS = 300;
		final short LINE_HEIGHT_TWIPS = 300;

		CellStyle headerStyle = Utility.createBoldBorderedStyle(workbook);

		// Style for editable cells: bordered + unlocked
		CellStyle editableStyle = Utility.createBorderedUnlockedStyle(workbook);

		// Style for editable wrap cells: bordered + unlocked + wrap
		CellStyle editableWrapStyle = Utility.createBorderedWrapUnlockedStyle(workbook);

		// Style for read-only cells: bordered + grey background + locked
		CellStyle readOnlyStyle = Utility.createBorderedLockedStyle(workbook);

		// Style for read-only wrap cells: bordered + grey background + locked + wrap
		CellStyle readOnlyWrapStyle = Utility.createBorderedWrapLockedStyle(workbook);

		// Sheet protection must be enabled for cell locking to take effect
		sheet.protectSheet("");

		int currentRow = 0;

	List<String> innerHeaders = new ArrayList<>();
	innerHeaders.add("Particulars");
	innerHeaders.add("Type");
	innerHeaders.add("UOM");
	innerHeaders.add(getMonth(year, 4));
	innerHeaders.add(getMonth(year, 5));
	innerHeaders.add(getMonth(year, 6));
	innerHeaders.add(getMonth(year, 7));
	innerHeaders.add(getMonth(year, 8));
	innerHeaders.add(getMonth(year, 9));
	innerHeaders.add(getMonth(year, 10));
	innerHeaders.add(getMonth(year, 11));
	innerHeaders.add(getMonth(year, 12));
	innerHeaders.add(getMonth(year, 1));
	innerHeaders.add(getMonth(year, 2));
		innerHeaders.add(getMonth(year, 3));
		innerHeaders.add("Remark");
		innerHeaders.add("Id");
		innerHeaders.add("NormParameterId");
		innerHeaders.add("LineId");

		Row headerRow = sheet.createRow(currentRow++);
		for (int col = 0; col < innerHeaders.size(); col++) {
			Cell cell = headerRow.createCell(col);
			cell.setCellValue(innerHeaders.get(col));
			cell.setCellStyle(headerStyle);
		}

		for (BusinessDemandDataDTO dto : dtoList) {
			boolean isEditable = dto.getIsEditable() == null || Boolean.TRUE.equals(dto.getIsEditable());

			List<Object> rowData = new ArrayList<>();
			rowData.add(dto.getDisplayName());
			rowData.add(dto.getNormParameterTypeDisplayName());
			rowData.add(dto.getUOM());
			rowData.add(dto.getApril());
			rowData.add(dto.getMay());
			rowData.add(dto.getJune());
			rowData.add(dto.getJuly());
			rowData.add(dto.getAug());
			rowData.add(dto.getSep());
			rowData.add(dto.getOct());
			rowData.add(dto.getNov());
			rowData.add(dto.getDec());
			rowData.add(dto.getJan());
			rowData.add(dto.getFeb());
			rowData.add(dto.getMarch());
			rowData.add(dto.getRemark());
			rowData.add(dto.getId());
			rowData.add(dto.getNormParameterId());
			rowData.add(dto.getLineId());

			Row row = sheet.createRow(currentRow++);
			for (int col = 0; col < rowData.size(); col++) {
				Cell cell = row.createCell(col);
				Object value = rowData.get(col);

				if (value instanceof Number) {
					cell.setCellValue(((Number) value).doubleValue());
				} else if (value instanceof Boolean) {
					cell.setCellValue((Boolean) value);
				} else if (value != null) {
					cell.setCellValue(value.toString());
				} else {
					cell.setCellValue("");
				}

				if (col == REMARK_COL_INDEX) {
					cell.setCellStyle(isEditable ? editableWrapStyle : readOnlyWrapStyle);
				} else {
					cell.setCellStyle(isEditable ? editableStyle : readOnlyStyle);
				}
			}

			String remarkText = (rowData.size() > REMARK_COL_INDEX && rowData.get(REMARK_COL_INDEX) != null)
					? rowData.get(REMARK_COL_INDEX).toString() : "";
			if (!remarkText.isEmpty()) {
				int numLines = (int) Math.ceil((double) remarkText.length() / REMARK_COL_WIDTH_CHARS);
				numLines = Math.max(1, numLines);
				row.setHeight((short) (numLines * LINE_HEIGHT_TWIPS));
			} else {
				row.setHeight(DEFAULT_ROW_HEIGHT_TWIPS);
			}
		}

		for (int col = 0; col < 19; col++) {
			if (col == REMARK_COL_INDEX || col == 16 || col == 17 || col == 18) continue;
			sheet.autoSizeColumn(col);
		}
		sheet.setColumnWidth(REMARK_COL_INDEX, REMARK_COL_WIDTH_CHARS * 256);
		sheet.setColumnHidden(16, true);
		sheet.setColumnHidden(17, true);
		sheet.setColumnHidden(18, true);
	}


		
	public String getMonth(String year, int month) {
	    
	    if (year == null || !year.matches("\\d{4}-\\d{2}")) {
	        throw new IllegalArgumentException("Year must be in format YYYY-YY");
	    }
	    String[] parts = year.split("-");
	    int startYear = Integer.parseInt(parts[0]);   
	    int endYearSuffix = Integer.parseInt(parts[1]); 
	    int endYear = (startYear / 100) * 100 + endYearSuffix;  

	    
	    String[] monthNames = {
	        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
	        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
	    };
	    if (month < 1 || month > 12) {
	        throw new IllegalArgumentException("Month must be 1 to 12");
	    }

	    String mname = monthNames[month - 1];
	    int displayYear;
	    
	    if (month >= 4 && month <= 12) {
	        displayYear = startYear;
	    } else {  
	        displayYear = endYear;
	    }

	    
	    int yy = displayYear % 100;  
	    String yyStr = String.format("%02d", yy);

	    return mname + "-" + yyStr;
	}
	
	@Override
	public AOPMessageVM importExcel(String year, UUID plantFKId, MultipartFile file) {
		// TODO Auto-generated method stub

		Plants plant = plantsRepository.findById(plantFKId).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

		if(vertical.getName().equalsIgnoreCase("cracker") && site.getName().equalsIgnoreCase("HMD") ) {  
            // seperate import method to handle ON/OFF values
               return importExcelV2(year, plantFKId, file);
		}
		try {
			List<BusinessDemandDataDTO> data = readBusinessDemand(file.getInputStream(), plantFKId, year);
			List<BusinessDemandDataDTO> failedRecords = saveBusinessDemandData(data);

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = exportBusinessDemand(year, plantFKId.toString(), true, failedRecords);
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

	// ref : importExcel | seperate method to handle 'Total' column 
	@Override
	public AOPMessageVM importExcelWithTotal(String year, UUID plantFKId, MultipartFile file) {
		// TODO Auto-generated method stub

		Plants plant = plantsRepository.findById(plantFKId).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		
		if(vertical.getName().equalsIgnoreCase("cracker") && site.getName().equalsIgnoreCase("HMD") ) {  
            // seperate import method to handle ON/OFF values
               return importExcelV2(year, plantFKId, file);
		}
		try {
			List<BusinessDemandDataDTO> data = readBusinessDemandWithTotal(file.getInputStream(), plantFKId, year);
			List<BusinessDemandDataDTO> failedRecords;
			if(vertical.getName().equalsIgnoreCase("Filament") || vertical.getName().equalsIgnoreCase("Staple")) {
				 failedRecords = saveBusinessDemandDataNew(data);
			}else {
				 failedRecords = saveBusinessDemandData(data);
			}
			

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = exportBusinessDemandWithTotal(year, plantFKId.toString(), true, failedRecords);
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

	// ref : exportBusinessDemand | seperate method to include 'Total' column 
@Override
public byte[] exportBusinessDemandWithTotal(String year, String plantId, boolean isAfterSave, List<BusinessDemandDataDTO> dtoList) {

	Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
	Sites site = siteRepository.findById(plant.getSiteFkId()).get();
	Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
	boolean showProductionTarget = vertical.getName().equalsIgnoreCase("MEG");
	 if(vertical.getName().equalsIgnoreCase("Cracker") && site.getName().equalsIgnoreCase("HMD") ) {  
		// seperate export method to handle ON/OFF values
		return exportBusinessDemandV2(year, plantId, isAfterSave, dtoList);
	 }

	 boolean crackerDmd = vertical.getName().equalsIgnoreCase("Cracker") && site.getName().equalsIgnoreCase("DMD");

	try {
		
		List<Boolean> isEditable = new ArrayList<>();

		if (!isAfterSave) {
			Map<String, Object> map = (Map<String, Object>) getBusinessDemandData(year,plantId).getData();
			dtoList = (List<BusinessDemandDataDTO>) map.get("businessDemandDataDTOList");
		}

		XSSFWorkbook workbook = new XSSFWorkbook();

		XSSFSheet sheet = workbook.createSheet("Sheet1");
		int currentRow = 0;

		if(showProductionTarget) {

		// Production Target section (read-only) is written first on the same sheet
		  currentRow = writeProductionTargetSection(sheet, workbook, year, plantId, 0);

		// Business Demand title row
		Row bdTitleRow = sheet.createRow(currentRow++);
		Cell bdTitleCell = bdTitleRow.createCell(0);
		bdTitleCell.setCellValue("Business Demand");
		bdTitleCell.setCellStyle(Utility.createBoldBorderedStyle(workbook));

		}
		List<List<Object>> rows = new ArrayList<>();
		
		// Data rows
		for (BusinessDemandDataDTO dto : dtoList) {
		   
		if(crackerDmd && dto.getNormParameterTypeName().equalsIgnoreCase("Feed Stream")) {
			continue;
		}
			List<Object> list = new ArrayList<>();
			
			list.add(dto.getDisplayName());
			list.add(dto.getNormParameterTypeDisplayName());
			list.add(dto.getUOM());
				list.add(dto.getApril());
				list.add(dto.getMay());
				list.add(dto.getJune());
				list.add(dto.getJuly());
				list.add(dto.getAug());
				list.add(dto.getSep());
				list.add(dto.getOct());
				list.add(dto.getNov());
				list.add(dto.getDec());
				list.add(dto.getJan());
				list.add(dto.getFeb());
				list.add(dto.getMarch());
				// Row total (sum of all 12 months)
				double rowTotal = 0.0;
				rowTotal += (dto.getApril() != null ? dto.getApril() : 0.0);
				rowTotal += (dto.getMay() != null ? dto.getMay() : 0.0);
				rowTotal += (dto.getJune() != null ? dto.getJune() : 0.0);
				rowTotal += (dto.getJuly() != null ? dto.getJuly() : 0.0);
				rowTotal += (dto.getAug() != null ? dto.getAug() : 0.0);
				rowTotal += (dto.getSep() != null ? dto.getSep() : 0.0);
				rowTotal += (dto.getOct() != null ? dto.getOct() : 0.0);
				rowTotal += (dto.getNov() != null ? dto.getNov() : 0.0);
				rowTotal += (dto.getDec() != null ? dto.getDec() : 0.0);
				rowTotal += (dto.getJan() != null ? dto.getJan() : 0.0);
				rowTotal += (dto.getFeb() != null ? dto.getFeb() : 0.0);
				rowTotal += (dto.getMarch() != null ? dto.getMarch() : 0.0);
				list.add(rowTotal); // col 15: Total
				list.add(dto.getRemark());         // col 16: Remark
				list.add(dto.getId());             // col 17: Id
				list.add(dto.getNormParameterId()); // col 18: NormParameterId
				
				
				if (isAfterSave) {
					list.add(dto.getSaveStatus());
					list.add(dto.getErrDescription());
				}
				rows.add(list);
			//}
		}

		// Build Total row (column-wise sums for months + grand total)
		if (!rows.isEmpty()) {
			List<Object> totalRow = new ArrayList<>();
			totalRow.add("Total"); // col 0: Particulars
			totalRow.add("");      // col 1: Type
			totalRow.add("");      // col 2: UOM
			double grandTotal = 0.0;
			// cols 3-14: monthly sums
			for (int col = 3; col <= 14; col++) {
				double sum = 0.0;
				for (List<Object> r : rows) {
					if (col < r.size() && r.get(col) instanceof Number) {
						sum += ((Number) r.get(col)).doubleValue();
					}
				}
				totalRow.add(sum);
				grandTotal += sum;
			}
			totalRow.add(grandTotal); // col 15: Total
			totalRow.add("");          // col 16: Remark
			totalRow.add("");          // col 17: Id
			totalRow.add("");          // col 18: NormParameterId
			if (isAfterSave) {
				totalRow.add(""); // Status
				totalRow.add(""); // Error Description
			}
			rows.add(totalRow);
		}

		List<String> innerHeaders = new ArrayList<>();
		
	innerHeaders.add("Particulars");
	innerHeaders.add("Type");
	innerHeaders.add("UOM");
	innerHeaders.add(getMonth( year, 4));
	innerHeaders.add(getMonth( year, 5));
	innerHeaders.add(getMonth( year, 6));
	innerHeaders.add(getMonth( year, 7));
	innerHeaders.add(getMonth( year, 8));
	innerHeaders.add(getMonth( year, 9));
	innerHeaders.add(getMonth( year, 10));
	innerHeaders.add(getMonth( year, 11));
	innerHeaders.add(getMonth( year, 12));
	innerHeaders.add(getMonth( year, 1));
	innerHeaders.add(getMonth( year, 2));
	innerHeaders.add(getMonth( year, 3));
	innerHeaders.add("Total");
	innerHeaders.add("Remark");
	innerHeaders.add("Id");
	innerHeaders.add("NormParameterId");
	// innerHeaders.add("NormParamterId");
	 //innerHeaders.add("IsEditable");
	if (isAfterSave) {
		innerHeaders.add("Status");
		innerHeaders.add("Error Description");
	}
	List<List<String>> headers = new ArrayList<>();
	headers.add(innerHeaders);

	int bdHeaderRowIndex = currentRow;
	for (List<String> headerRowData : headers) {
		Row headerRow = sheet.createRow(currentRow++);
		for (int col = 0; col < headerRowData.size(); col++) {
			Cell cell = headerRow.createCell(col);
			cell.setCellValue(headerRowData.get(col));
			cell.setCellStyle(Utility.createBoldBorderedStyle(workbook));
		}
	}
// Bordered + unlocked style for editable BD cells (months col 3–14 and Remark col 15)
CellStyle unlockedBorderedStyle = workbook.createCellStyle();
unlockedBorderedStyle.setLocked(false);
unlockedBorderedStyle.setBorderBottom(BorderStyle.THIN);
unlockedBorderedStyle.setBorderTop(BorderStyle.THIN);
unlockedBorderedStyle.setBorderLeft(BorderStyle.THIN);
unlockedBorderedStyle.setBorderRight(BorderStyle.THIN);
// Bordered + locked style for read-only BD cells
CellStyle lockedBorderedStyle = workbook.createCellStyle();
lockedBorderedStyle.setLocked(true);
lockedBorderedStyle.setBorderBottom(BorderStyle.THIN);
lockedBorderedStyle.setBorderTop(BorderStyle.THIN);
lockedBorderedStyle.setBorderLeft(BorderStyle.THIN);
lockedBorderedStyle.setBorderRight(BorderStyle.THIN);
// Plain bordered style for non-showProductionTarget case; unlocked so cells stay editable under sheet protection
CellStyle borderedDataStyle = Utility.createBorderedStyle(workbook);
borderedDataStyle.setLocked(false);
CellStyle totalRowStyle = Utility.createBoldBorderedStyle(workbook);

for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
	List<Object> rowData = rows.get(rowIndex);
	boolean isTotalRow = (rowIndex == rows.size() - 1);
	Row row = sheet.createRow(currentRow++);
	for (int col = 0; col < rowData.size(); col++) {
		Cell cell = row.createCell(col);
		Object value = rowData.get(col);

		if (value instanceof Number) {
			cell.setCellValue(((Number) value).doubleValue());
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
		} else if (value != null) {
			cell.setCellValue(value.toString());
		} else {
			cell.setCellValue("");
		}

		if (isTotalRow) {
			cell.setCellStyle(totalRowStyle);
		} else if (showProductionTarget) {
			// Month columns (3–14) and Remark (col 16) remain editable; Total (col 15) and others locked
			if ((col >= 3 && col <= 14) || col == 16) {
				cell.setCellStyle(unlockedBorderedStyle);
			} else {
				cell.setCellStyle(lockedBorderedStyle);
			}
		} else {
			cell.setCellStyle(borderedDataStyle);
		}
	}
}

// Users cannot resize columns once the sheet is protected, so all visible columns are sized up front
for (int col = 0; col < innerHeaders.size(); col++) {
	if (col == 17 || col == 18) {
		continue;
	}
	sheet.autoSizeColumn(col);
	sheet.setColumnWidth(col, Math.min(sheet.getColumnWidth(col) + FILTER_ARROW_PADDING, MAX_EXPORT_COLUMN_WIDTH));
}

if (!showProductionTarget) {
	CellStyle unlockedDefaultStyle = workbook.createCellStyle();
	unlockedDefaultStyle.setLocked(false);
	for (int col = 0; col < innerHeaders.size(); col++) {
		sheet.setDefaultColumnStyle(col, unlockedDefaultStyle);
	}
}

sheet.setColumnHidden(17, true);
sheet.setColumnHidden(18, true);
//sheet.setColumnHidden(18, true);

// The Total summary row is kept out of the filter range so it stays at the bottom and is never sorted
int bdLastDataRowIndex = rows.isEmpty() ? bdHeaderRowIndex : currentRow - 2;
sheet.setAutoFilter(new CellRangeAddress(bdHeaderRowIndex, Math.max(bdLastDataRowIndex, bdHeaderRowIndex), 0, innerHeaders.size() - 1));

// Protection keeps locked PT cells non-editable, and keeping "Format columns" locked stops Excel
// from unhiding the Id / NormParameterId columns when the neighbouring Remark column is resized.
sheet.protectSheet(EXPORT_SHEET_PROTECTION_PASSWORD);
sheet.lockFormatColumns(true);
sheet.lockAutoFilter(false);
sheet.lockSort(false);
sheet.lockSelectLockedCells(false);
sheet.lockSelectUnlockedCells(false);
if (!showProductionTarget) {
	sheet.lockInsertRows(false);
	sheet.lockDeleteRows(false);
}
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
public AOPMessageVM importExcelLineWise(String year, UUID plantFKId, MultipartFile file) {
	// TODO Auto-generated method stub

	Plants plant = plantsRepository.findById(plantFKId).get();
	Sites site = siteRepository.findById(plant.getSiteFkId()).get();
	Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

	
	try {
		List<BusinessDemandDataDTO> data = readBusinessDemand(file.getInputStream(), plantFKId, year);
		List<BusinessDemandDataDTO> failedRecords = saveBusinessDemandLineData(data);

		AOPMessageVM aopMessageVM = new AOPMessageVM();
		if (failedRecords != null && failedRecords.size() > 0) {
			byte[] fileByteArray = exportBusinessDemand(year, plantFKId.toString(), true, failedRecords);
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

	
	public List<BusinessDemandDataDTO> readBusinessDemand(InputStream inputStream, UUID plantFKId, String year) {
	    List<BusinessDemandDataDTO> configList = new ArrayList<>();
	    String verticalName = plantsRepository.findVerticalNameByPlantId(plantFKId);
	    Plants plant = plantsRepository.findById((plantFKId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
	    boolean pvc= vertical.getName().equalsIgnoreCase("PVC") && (site.getName().equalsIgnoreCase("VMD") || site.getName().equalsIgnoreCase("DMD"));

		boolean pvcDmd = vertical.getName().equalsIgnoreCase("PVC") && site.getName().equalsIgnoreCase("DMD");
	    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
	        int numberOfSheets = workbook.getNumberOfSheets();
	        for (int sheetIndex = 0; sheetIndex < numberOfSheets; sheetIndex++) {
	            Sheet sheet = workbook.getSheetAt(sheetIndex);
	            int lastRowNum = sheet.getLastRowNum();

	            // Locate the Business Demand section.  In the combined layout the sheet
	            // starts with a Production Target section followed by a "Business Demand"
	            // title row; data begins two rows after that title (title + header skipped).
	            // Falls back to row 1 for legacy files that have only one section.
	            int bdDataStartRow = 1;
	            for (int r = 0; r <= lastRowNum; r++) {
	                Row scanRow = sheet.getRow(r);
	                if (scanRow == null) continue;
	                Cell firstCell = scanRow.getCell(0);
	                if (firstCell != null && firstCell.getCellType() == CellType.STRING
	                        && "Business Demand".equalsIgnoreCase(firstCell.getStringCellValue())) {
	                    bdDataStartRow = r + 2; // skip title row + header row
	                    break;
	                }
	            }

	            List<BusinessDemandDataDTO> productionDtos = new ArrayList<>();

	            for (int rowIdx = bdDataStartRow; rowIdx <= lastRowNum; rowIdx++) {
	                Row row = sheet.getRow(rowIdx);
	                if (row == null) continue;
	                BusinessDemandDataDTO dto = new BusinessDemandDataDTO();
	                try {
                    dto.setDisplayName(getStringCellValue(row.getCell(0), dto));
                    // col 1 (Type) is display-only; skip during import
                    dto.setUOM(getStringCellValue(row.getCell(2), dto));
                    dto.setApril(getNumericCellValue(row.getCell(3), dto));
                    dto.setMay(getNumericCellValue(row.getCell(4), dto));
                    dto.setJune(getNumericCellValue(row.getCell(5), dto));
                    dto.setJuly(getNumericCellValue(row.getCell(6), dto));
                    dto.setAug(getNumericCellValue(row.getCell(7), dto));
                    dto.setSep(getNumericCellValue(row.getCell(8), dto));
                    dto.setOct(getNumericCellValue(row.getCell(9), dto));
                    dto.setNov(getNumericCellValue(row.getCell(10), dto));
                    dto.setDec(getNumericCellValue(row.getCell(11), dto));
                    dto.setJan(getNumericCellValue(row.getCell(12), dto));
                    dto.setFeb(getNumericCellValue(row.getCell(13), dto));
                    dto.setMarch(getNumericCellValue(row.getCell(14), dto));
                    dto.setPlantId(plantFKId.toString());
                    String normParameterId = getStringCellValue(row.getCell(17), dto);
	                    dto.setNormParameterId(normParameterId);
	                    boolean isProduction = false;
	                    if (verticalName != null
	                            && (verticalName.equalsIgnoreCase("PE") || verticalName.equalsIgnoreCase("PP") || verticalName.equalsIgnoreCase("PET") || pvc)
	                            && normParameterId != null) {

	                        isProduction = isProductionType(normParameterId, normParametersRepository, normParameterTypeRepository);
	                    }

	                    if (isProduction) {
	                        productionDtos.add(dto);
	                    }
                    dto.setRemark(getStringCellValue(row.getCell(15), dto));

                    dto.setId(getStringCellValue(row.getCell(16), dto));

                    if (pvcDmd) {
                        dto.setLineId(getStringCellValue(row.getCell(18), dto));
                    }

	                    // Check if id is null AND all month values are zero or null
	                    boolean allMonthsZero = (dto.getApril() == null || dto.getApril() == 0.0)
	                            && (dto.getMay() == null || dto.getMay() == 0.0)
	                            && (dto.getJune() == null || dto.getJune() == 0.0)
	                            && (dto.getJuly() == null || dto.getJuly() == 0.0)
	                            && (dto.getAug() == null || dto.getAug() == 0.0)
	                            && (dto.getSep() == null || dto.getSep() == 0.0)
	                            && (dto.getOct() == null || dto.getOct() == 0.0)
	                            && (dto.getNov() == null || dto.getNov() == 0.0)
	                            && (dto.getDec() == null || dto.getDec() == 0.0)
	                            && (dto.getJan() == null || dto.getJan() == 0.0)
	                            && (dto.getFeb() == null || dto.getFeb() == 0.0)
	                            && (dto.getMarch() == null || dto.getMarch() == 0.0);

	                    if (dto.getId() == null && allMonthsZero) {
	                        continue;
	                    }

	                    dto.setVerticalFKId(vertical.getId().toString());
	                    dto.setSiteFKId(site.getId().toString());
	                    dto.setYear(year);

	                } catch (Exception e) {
	                    e.printStackTrace();
	                    dto.setErrDescription(e.getMessage());
	                    dto.setSaveStatus("Failed");
	                }
	                configList.add(dto);
	            }

	            if (!productionDtos.isEmpty() && (verticalName.equalsIgnoreCase("PE") || verticalName.equalsIgnoreCase("PP") || verticalName.equalsIgnoreCase("PET") || pvc)) {

	                Map<String, Double> monthlyProductionSums = new HashMap<>();
	                String[] months = {"April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "March"};

	                for (BusinessDemandDataDTO dto : productionDtos) {
	                    monthlyProductionSums.merge("April", dto.getApril() != null ? dto.getApril() : 0.0, Double::sum);
	                    monthlyProductionSums.merge("May", dto.getMay() != null ? dto.getMay() : 0.0, Double::sum);
	                    monthlyProductionSums.merge("June", dto.getJune() != null ? dto.getJune() : 0.0, Double::sum);
	                    monthlyProductionSums.merge("July", dto.getJuly() != null ? dto.getJuly() : 0.0, Double::sum);
	                    monthlyProductionSums.merge("Aug", dto.getAug() != null ? dto.getAug() : 0.0, Double::sum);
	                    monthlyProductionSums.merge("Sep", dto.getSep() != null ? dto.getSep() : 0.0, Double::sum);
	                    monthlyProductionSums.merge("Oct", dto.getOct() != null ? dto.getOct() : 0.0, Double::sum);
	                    monthlyProductionSums.merge("Nov", dto.getNov() != null ? dto.getNov() : 0.0, Double::sum);
	                    monthlyProductionSums.merge("Dec", dto.getDec() != null ? dto.getDec() : 0.0, Double::sum);
	                    monthlyProductionSums.merge("Jan", dto.getJan() != null ? dto.getJan() : 0.0, Double::sum);
	                    monthlyProductionSums.merge("Feb", dto.getFeb() != null ? dto.getFeb() : 0.0, Double::sum);
	                    monthlyProductionSums.merge("March", dto.getMarch() != null ? dto.getMarch() : 0.0, Double::sum);
	                }

	                for (String month : months) {
	                    Double sum = monthlyProductionSums.getOrDefault(month, 0.0);
	                    if (Math.abs(sum - 100.0) > 0.001) {
	                        for (BusinessDemandDataDTO dto : productionDtos) {
	                            if (!"Failed".equalsIgnoreCase(dto.getSaveStatus())) {
	                                dto.setSaveStatus("Failed");
	                                dto.setErrDescription(month + " Production sum is " + String.format("%.2f", sum) + ", but must be 100.");
	                            } else {
	                                String existingError = dto.getErrDescription() != null ? dto.getErrDescription() : "";
	                                dto.setErrDescription(existingError + "; " + month + " Production sum is " + String.format("%.2f", sum) + ", but must be 100.");
	                            }
	                        }
	                    }
	                }
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return configList;
	}


	private boolean isProductionType(String normParameterId, NormParametersRepository normParametersRepository, NormParameterTypeRepository normParameterTypeRepository) {
	    try {
	        UUID normId = UUID.fromString(normParameterId);
	        Optional<NormParameters> normParametersOpt = normParametersRepository.findById(normId);
	        
	        if (normParametersOpt.isPresent()) {
	            NormParameters normParameters = normParametersOpt.get();
	            Optional<NormParameterType> normParameterTypeOpt = normParameterTypeRepository.findById(normParameters.getNormParameterTypeFkId());
	            
	            if (normParameterTypeOpt.isPresent()) {
	                NormParameterType normParameterType = normParameterTypeOpt.get();
	                return normParameterType.getName().equalsIgnoreCase("Production");
	            }
	        }
	    } catch (IllegalArgumentException e) {
	        return false;
	    }
	    return false;
	}	
	
	private static Double getNumericCellValue(Cell cell, BusinessDemandDataDTO dto) {
		if (cell == null)
			return null;
		if (cell.getCellType() == CellType.NUMERIC) {
			return cell.getNumericCellValue();
		} else if (cell.getCellType() == CellType.STRING) {
			try {
				return Double.parseDouble(cell.getStringCellValue().trim());
			} catch (NumberFormatException e) {
				dto.setSaveStatus("Failed");
				dto.setErrDescription("Please enter numeric values");
			}
		}
		return null;
	}

	public static Boolean getBooleanCellValue(Cell cell, BusinessDemandDataDTO dto) {
		if (cell == null)
			return null;

		CellType type = cell.getCellType();
		if (type == CellType.FORMULA) {
			type = cell.getCachedFormulaResultType();
		}

		switch (type) {
			case BOOLEAN:
				return cell.getBooleanCellValue();
			case STRING:
				String text = cell.getStringCellValue().trim().toLowerCase();
				if ("true".equals(text))
					return true;
				if ("false".equals(text))
					return false;
				return null;
			case NUMERIC:
				double num = cell.getNumericCellValue();
				if (num == 1.0)
					return true;
				if (num == 0.0)
					return false;
				return null;
			case BLANK:
			case _NONE:
			default:
				return null;
		}
	}
	
	private static String getStringCellValue(Cell cell, BusinessDemandDataDTO dto) {
	    try {
	        if (cell == null) {
	            return null;
	        }

	        // Get value as string
	        cell.setCellType(CellType.STRING);
	        String value = cell.getStringCellValue();

	        // Return null if value is empty or whitespace only
	        if (value == null || value.trim().isEmpty()) {
	            return null;
	        }

	        return value.trim();

	    } catch (Exception e) {
	        dto.setSaveStatus("Failed");
	        dto.setErrDescription("Please enter correct values");
	        e.printStackTrace();
	    }

	    return null;
	}

	@Override
	public List<BusinessDemandDataDTO> saveBusinessDemandDataNew(List<BusinessDemandDataDTO> businessDemandDataDTOList) {
	    String year = null;
	    UUID plantId = null;
	    List<BusinessDemandDataDTO> failedList = new ArrayList<>();

	    try {
	        for (BusinessDemandDataDTO businessDemandDataDTO : businessDemandDataDTOList) {
	            
	            if (businessDemandDataDTO.getPlantId() != null && !businessDemandDataDTO.getPlantId().isEmpty()) {
	                plantId = UUID.fromString(businessDemandDataDTO.getPlantId());
	            }
	            if (businessDemandDataDTO.getYear() != null) {
	                year = businessDemandDataDTO.getYear();
	            }

	            if (businessDemandDataDTO.getSaveStatus() != null
	                    && businessDemandDataDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
	                failedList.add(businessDemandDataDTO);
	                continue;
	            }

	            BusinessDemand businessDemand = new BusinessDemand();
	            businessDemand.setApril(businessDemandDataDTO.getApril());
	            businessDemand.setAug(businessDemandDataDTO.getAug());
	            businessDemand.setAvgTph(businessDemandDataDTO.getAvgTph());
	            businessDemand.setDec(businessDemandDataDTO.getDec());
	            businessDemand.setFeb(businessDemandDataDTO.getFeb());

	            String newRemark = businessDemandDataDTO.getRemark() != null ? businessDemandDataDTO.getRemark().trim() : "";

	            if (businessDemandDataDTO.getId() == null || businessDemandDataDTO.getId().contains("#")) {
	                businessDemand.setId(null);
	                businessDemand.setCreatedOn(new Date());

	                if (newRemark.isEmpty()) {
	                    businessDemandDataDTO.setSaveStatus("Failed");
	                    businessDemandDataDTO.setErrDescription("Please provide a remark for new entry");
	                    failedList.add(businessDemandDataDTO);
	                    continue;
	                }
	            } else {
	                businessDemand.setId(UUID.fromString(businessDemandDataDTO.getId()));
	                businessDemand.setModifiedOn(new Date());

	                Optional<BusinessDemand> existingOpt = businessDemandDataRepository.findById(businessDemand.getId());
	                if (!existingOpt.isPresent()) {
	                    throw new RuntimeException("Business demand data not found for ID: " + businessDemand.getId());
	                }

	                BusinessDemand existingBusinessDemand = existingOpt.get();
	                String existingRemark = existingBusinessDemand.getRemark() != null ? existingBusinessDemand.getRemark().trim() : "";

	                boolean isValueChanged = isAnyBusinessDemandValueChanged(existingBusinessDemand, businessDemandDataDTO);
	                boolean isRemarkChanged = !existingRemark.equalsIgnoreCase(newRemark);

	                if (newRemark.isEmpty()) {
	                    businessDemandDataDTO.setSaveStatus("Failed");
	                    businessDemandDataDTO.setErrDescription("Remark is mandatory to update an existing record.");
	                    failedList.add(businessDemandDataDTO);
	                    continue;
	                }

	                if (isValueChanged && !isRemarkChanged) {
	                    businessDemandDataDTO.setSaveStatus("Failed");
	                    businessDemandDataDTO.setErrDescription("Value has changed; please provide an updated remark.");
	                    failedList.add(businessDemandDataDTO);
	                    continue;
	                }
	            }

	            businessDemand.setJan(businessDemandDataDTO.getJan());
	            businessDemand.setJuly(businessDemandDataDTO.getJuly());
	            businessDemand.setJune(businessDemandDataDTO.getJune());
	            businessDemand.setMarch(businessDemandDataDTO.getMarch());
	            businessDemand.setMay(businessDemandDataDTO.getMay());
	            businessDemand.setUpdatedBy(Utility.getUserName());

	            if (businessDemandDataDTO.getNormParameterId() != null
	                    && !businessDemandDataDTO.getNormParameterId().isEmpty()) {
	                businessDemand.setNormParameterId(UUID.fromString(businessDemandDataDTO.getNormParameterId()));
	            }

	            businessDemand.setNov(businessDemandDataDTO.getNov());
	            businessDemand.setOct(businessDemandDataDTO.getOct());

	            if (businessDemandDataDTO.getPlantId() != null && !businessDemandDataDTO.getPlantId().isEmpty()) {
	                businessDemand.setPlantId(plantId);
	                businessDemand.setRemark(newRemark);
	                businessDemand.setSep(businessDemandDataDTO.getSep());
	                businessDemand.setYear(businessDemandDataDTO.getYear());

	                if (businessDemandDataDTO.getSiteFKId() != null) {
	                    businessDemand.setSiteFKId(UUID.fromString(businessDemandDataDTO.getSiteFKId()));
	                }
	                if (businessDemandDataDTO.getVerticalFKId() != null) {
	                    businessDemand.setVerticalFKId(UUID.fromString(businessDemandDataDTO.getVerticalFKId()));
	                }

	                businessDemandDataRepository.save(businessDemand);
	            }
	        }

	        if (plantId != null) {
	            Plants plant = plantsRepository.findById(plantId)
	                    .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

	            Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
	                    .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

	            if ("Cracker".equalsIgnoreCase(vertical.getName())) {
	                for (BusinessDemandDataDTO businessDemandDataDTO : businessDemandDataDTOList) {
	                    if ("Failed".equalsIgnoreCase(businessDemandDataDTO.getSaveStatus())) {
	                        continue;
	                    }

	                    String normParameterName = normParametersRepository.findNormParameterName(UUID.fromString(businessDemandDataDTO.getNormParameterId()));
	                    if ("Ethane".equalsIgnoreCase(normParameterName)) {
	                        normParameterName = "Ethane-4F";
	                    }

	                    List<UUID> ids = normParametersRepository.findNormParameterIds(normParameterName, plantId);
	                    for (UUID id : ids) {
	                        for (int i = 1; i <= 12; i++) {
	                            Double attributeValue = getAttributeValue(businessDemandDataDTO, i);
	                            saveData(id, i, attributeValue, businessDemandDataDTO.getRemark(), plantId.toString(), businessDemandDataDTO.getYear());
	                        }
	                    }
	                }
	            }

	            List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("business-demand");
	            for (ScreenMapping screenMapping : screenMappingList) {
	                AopCalculation aopCalculation = new AopCalculation();
	                aopCalculation.setAopYear(year);
	                aopCalculation.setIsChanged(true);
	                aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
	                aopCalculation.setPlantId(plantId);
	                aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
	                aopCalculationRepository.save(aopCalculation);
	            }
	        }

	        return failedList;
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new RuntimeException("Failed to save data", ex);
	    }
	}

	private boolean isAnyBusinessDemandValueChanged(BusinessDemand existing, BusinessDemandDataDTO dto) {
	    return !areDoublesEqual(existing.getApril(), dto.getApril())
	            || !areDoublesEqual(existing.getMay(), dto.getMay())
	            || !areDoublesEqual(existing.getJune(), dto.getJune())
	            || !areDoublesEqual(existing.getJuly(), dto.getJuly())
	            || !areDoublesEqual(existing.getAug(), dto.getAug())
	            || !areDoublesEqual(existing.getSep(), dto.getSep())
	            || !areDoublesEqual(existing.getOct(), dto.getOct())
	            || !areDoublesEqual(existing.getNov(), dto.getNov())
	            || !areDoublesEqual(existing.getDec(), dto.getDec())
	            || !areDoublesEqual(existing.getJan(), dto.getJan())
	            || !areDoublesEqual(existing.getFeb(), dto.getFeb())
	            || !areDoublesEqual(existing.getMarch(), dto.getMarch())
	            || !areDoublesEqual(existing.getAvgTph(), dto.getAvgTph());
	}

	private boolean areDoublesEqual(Double d1, Double d2) {
	    
	    double val1 = (d1 != null) ? d1 : 0.0;
	    double val2 = (d2 != null) ? d2 : 0.0;

	    return Math.abs(val1 - val2) < 0.000001;
	}
	
	@Override
	public List<BusinessDemandDataDTO> saveBusinessDemandData(List<BusinessDemandDataDTO> businessDemandDataDTOList) {
		String year=null;
		UUID plantId=null;
		List<BusinessDemandDataDTO> failedList = new ArrayList<>();
		try {
			for (BusinessDemandDataDTO businessDemandDataDTO : businessDemandDataDTOList) {
				plantId=UUID.fromString(businessDemandDataDTO.getPlantId());
				if (businessDemandDataDTO.getSaveStatus() != null
						&& businessDemandDataDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(businessDemandDataDTO);
					continue;
				}



				BusinessDemand businessDemand = new BusinessDemand();
				businessDemand.setApril(businessDemandDataDTO.getApril());
				businessDemand.setAug(businessDemandDataDTO.getAug());
				businessDemand.setAvgTph(businessDemandDataDTO.getAvgTph());
				businessDemand.setDec(businessDemandDataDTO.getDec());
				businessDemand.setFeb(businessDemandDataDTO.getFeb());

				if (businessDemandDataDTO.getId() == null || businessDemandDataDTO.getId().contains("#")) {
					businessDemand.setId(null);
					businessDemand.setCreatedOn(new Date());
				} else {
					businessDemand.setId(UUID.fromString(businessDemandDataDTO.getId()));
					businessDemand.setModifiedOn(new Date());

					//remark validation
				BusinessDemand existingBusinessDemand = businessDemandDataRepository.findById(businessDemand.getId()).get();
				   if(existingBusinessDemand == null) { 
					throw new RuntimeException("Business demand data not found");
				   }
    
				 Double  existingAprilValue = existingBusinessDemand.getApril() != null ? existingBusinessDemand.getApril() : 0.0;
				 String existingRemark = existingBusinessDemand.getRemark();

				 Double newAprilValue = businessDemandDataDTO.getApril();
				 String newRemark = businessDemandDataDTO.getRemark();

				     if(!Objects.equals(existingAprilValue, newAprilValue) && Objects.equals(existingRemark, newRemark)) {  
  
						 businessDemandDataDTO.setSaveStatus("Failed");
						 businessDemandDataDTO.setErrDescription("Please update remark");
						 failedList.add(businessDemandDataDTO);
						 continue;
					 }
				}

				businessDemand.setJan(businessDemandDataDTO.getJan());
				businessDemand.setJuly(businessDemandDataDTO.getJuly());
				businessDemand.setJune(businessDemandDataDTO.getJune());
				businessDemand.setMarch(businessDemandDataDTO.getMarch());
				businessDemand.setMay(businessDemandDataDTO.getMay());
				businessDemand.setUpdatedBy(Utility.getUserName());
				if (businessDemandDataDTO.getNormParameterId() != null
						&& !businessDemandDataDTO.getNormParameterId().isEmpty()) {
					businessDemand.setNormParameterId(UUID.fromString(businessDemandDataDTO.getNormParameterId()));
				}

				businessDemand.setNov(businessDemandDataDTO.getNov());
				businessDemand.setOct(businessDemandDataDTO.getOct());
				
				if (businessDemandDataDTO.getPlantId() != null && !businessDemandDataDTO.getPlantId().isEmpty()) {
					businessDemand.setPlantId(UUID.fromString(businessDemandDataDTO.getPlantId()));
					businessDemand.setRemark(businessDemandDataDTO.getRemark());
					businessDemand.setSep(businessDemandDataDTO.getSep());
					businessDemand.setYear(businessDemandDataDTO.getYear());
					year=businessDemandDataDTO.getYear();
					
					if (businessDemandDataDTO.getSiteFKId() != null) {
						businessDemand.setSiteFKId(UUID.fromString(businessDemandDataDTO.getSiteFKId()));
					}
					if (businessDemandDataDTO.getVerticalFKId() != null) {
						businessDemand.setVerticalFKId(UUID.fromString(businessDemandDataDTO.getVerticalFKId()));
					}
					
					businessDemandDataRepository.save(businessDemand);

				}
			} 
			
			Plants plant = plantsRepository.findById((plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
			
			if(vertical.getName().equalsIgnoreCase("Cracker")) {
				for(BusinessDemandDataDTO businessDemandDataDTO : businessDemandDataDTOList) {
					String normParameterName=normParametersRepository.findNormParameterName(UUID.fromString(businessDemandDataDTO.getNormParameterId()));
					if(normParameterName.equalsIgnoreCase("Ethane")) {
						normParameterName = "Ethane-4F";
					}
					List<UUID> ids= normParametersRepository.findNormParameterIds(normParameterName,plantId);
					for(UUID id:ids) {
						for (int i = 1; i <= 12; i++) {	
							Double attributeValue = getAttributeValue(businessDemandDataDTO, i);	
							saveData(id,i,attributeValue,businessDemandDataDTO.getRemark(),plantId.toString(),businessDemandDataDTO.getYear());
						}
					}
				}
			}
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("business-demand");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(plantId);
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			return failedList;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	@Override
	public List<BusinessDemandDataDTO> saveBusinessDemandLineData(List<BusinessDemandDataDTO> businessDemandDataDTOList) {
		String year=null;
		UUID plantId=null;
		List<BusinessDemandDataDTO> failedList = new ArrayList<>();
		try {
			for (BusinessDemandDataDTO businessDemandDataDTO : businessDemandDataDTOList) {
				plantId=UUID.fromString(businessDemandDataDTO.getPlantId());
				if (businessDemandDataDTO.getSaveStatus() != null
						&& businessDemandDataDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(businessDemandDataDTO);
					continue;
				}

			    

				BusinessDemand businessDemand = new BusinessDemand();
				businessDemand.setApril(businessDemandDataDTO.getApril());
				businessDemand.setAug(businessDemandDataDTO.getAug());
				businessDemand.setAvgTph(businessDemandDataDTO.getAvgTph());
				businessDemand.setDec(businessDemandDataDTO.getDec());
				businessDemand.setFeb(businessDemandDataDTO.getFeb());
				//businessDemand.setLineId(UUID.fromString(businessDemandDataDTO.getLineId()));
				businessDemand.setLineId(businessDemandDataDTO.getLineId() != null ? UUID.fromString(businessDemandDataDTO.getLineId()) : null);


				if (businessDemandDataDTO.getId() == null || businessDemandDataDTO.getId().contains("#")) {
					businessDemand.setId(null);
					businessDemand.setCreatedOn(new Date());
				} else {
					businessDemand.setId(UUID.fromString(businessDemandDataDTO.getId()));
					businessDemand.setModifiedOn(new Date());

					//remark validation
				BusinessDemand existingBusinessDemand = businessDemandDataRepository.findById(businessDemand.getId()).get();
				   if(existingBusinessDemand == null) { 
					throw new RuntimeException("Business demand data not found");
				   }

				 Double  existingAprilValue = existingBusinessDemand.getApril() != null ? existingBusinessDemand.getApril() : 0.0;
				 String existingRemark = existingBusinessDemand.getRemark();

				 Double newAprilValue = businessDemandDataDTO.getApril();
				 String newRemark = businessDemandDataDTO.getRemark();

				     if(!Objects.equals(existingAprilValue, newAprilValue) && Objects.equals(existingRemark, newRemark)) {  
		
						 businessDemandDataDTO.setSaveStatus("Failed");
						 businessDemandDataDTO.setErrDescription("Please update remark");
						 failedList.add(businessDemandDataDTO);
						 continue;
					 }
				}

				businessDemand.setJan(businessDemandDataDTO.getJan());
				businessDemand.setJuly(businessDemandDataDTO.getJuly());
				businessDemand.setJune(businessDemandDataDTO.getJune());
				businessDemand.setMarch(businessDemandDataDTO.getMarch());
				businessDemand.setMay(businessDemandDataDTO.getMay());
				businessDemand.setUpdatedBy(Utility.getUserName());
				if (businessDemandDataDTO.getNormParameterId() != null
						&& !businessDemandDataDTO.getNormParameterId().isEmpty()) {
					businessDemand.setNormParameterId(UUID.fromString(businessDemandDataDTO.getNormParameterId()));
				}

				businessDemand.setNov(businessDemandDataDTO.getNov());
				businessDemand.setOct(businessDemandDataDTO.getOct());
				
				if (businessDemandDataDTO.getPlantId() != null && !businessDemandDataDTO.getPlantId().isEmpty()) {
					businessDemand.setPlantId(UUID.fromString(businessDemandDataDTO.getPlantId()));
					businessDemand.setRemark(businessDemandDataDTO.getRemark());
					businessDemand.setSep(businessDemandDataDTO.getSep());
					businessDemand.setYear(businessDemandDataDTO.getYear());
					year=businessDemandDataDTO.getYear();
					
					if (businessDemandDataDTO.getSiteFKId() != null) {
						businessDemand.setSiteFKId(UUID.fromString(businessDemandDataDTO.getSiteFKId()));
					}
					if (businessDemandDataDTO.getVerticalFKId() != null) {
						businessDemand.setVerticalFKId(UUID.fromString(businessDemandDataDTO.getVerticalFKId()));
					}
					
					businessDemandDataRepository.save(businessDemand);

				}
			} 

			
		
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("business-demand");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(plantId);
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			return failedList;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to save data", ex);
		}

	}

	

	void validateRemark(BusinessDemandDataDTO businessDemandDataDTO) {

		if(businessDemandDataDTO.getId() == null || businessDemandDataDTO.getId().contains("#")) {
			return;
		}
		BusinessDemand existingBusinessDemand = businessDemandDataRepository.findById(UUID.fromString(businessDemandDataDTO.getId())).get();
		String existingRemark = existingBusinessDemand.getRemark();
		String newRemark = businessDemandDataDTO.getRemark();

		Double existingAprilValue = existingBusinessDemand.getApril() != null ? existingBusinessDemand.getApril() : 0.0;
		Double newAprilValue = businessDemandDataDTO.getApril();

	
		if(!Objects.equals(existingAprilValue, newAprilValue) && Objects.equals(existingRemark, newRemark)) {  
			businessDemandDataDTO.setSaveStatus("Failed");
			businessDemandDataDTO.setErrDescription("Please update remark");
		}

		Double existingMayValue = existingBusinessDemand.getMay() != null ? existingBusinessDemand.getMay() : 0.0;
		Double newMayValue = businessDemandDataDTO.getMay();
		if(!Objects.equals(existingMayValue, newMayValue) && Objects.equals(existingRemark, newRemark)) {  
			businessDemandDataDTO.setSaveStatus("Failed");
			businessDemandDataDTO.setErrDescription("Please update remark");
		}
		
		Double existingJuneValue = existingBusinessDemand.getJune() != null ? existingBusinessDemand.getJune() : 0.0;
		Double newJuneValue = businessDemandDataDTO.getJune();
		if(!Objects.equals(existingJuneValue, newJuneValue) && Objects.equals(existingRemark, newRemark)) {  
			businessDemandDataDTO.setSaveStatus("Failed");
			businessDemandDataDTO.setErrDescription("Please update remark");
		}
		
		
		
		double existingJulyValue = existingBusinessDemand.getJuly() != null ? existingBusinessDemand.getJuly() : 0.0;
		Double newJulyValue = businessDemandDataDTO.getJuly();
		if(!Objects.equals(existingJulyValue, newJulyValue) && Objects.equals(existingRemark, newRemark)) {  
			businessDemandDataDTO.setSaveStatus("Failed");
			businessDemandDataDTO.setErrDescription("Please update remark");
		}
		
		
		
		double existingAugustValue = existingBusinessDemand.getAug() != null ? existingBusinessDemand.getAug() : 0.0;
		Double newAugustValue = businessDemandDataDTO.getAug();
		if(!Objects.equals(existingAugustValue, newAugustValue) && Objects.equals(existingRemark, newRemark)) {  
			businessDemandDataDTO.setSaveStatus("Failed");
			businessDemandDataDTO.setErrDescription("Please update remark");
		}
		
		
		double existingSeptemberValue = existingBusinessDemand.getSep() != null ? existingBusinessDemand.getSep() : 0.0;
		Double newSeptemberValue = businessDemandDataDTO.getSep();
		if(!Objects.equals(existingSeptemberValue, newSeptemberValue) && Objects.equals(existingRemark, newRemark)) {  
			businessDemandDataDTO.setSaveStatus("Failed");
			businessDemandDataDTO.setErrDescription("Please update remark");
		}
		
		
		double existingOctoberValue = existingBusinessDemand.getOct() != null ? existingBusinessDemand.getOct() : 0.0;
		Double newOctoberValue = businessDemandDataDTO.getOct();
		if(!Objects.equals(existingOctoberValue, newOctoberValue) && Objects.equals(existingRemark, newRemark)) {  
			businessDemandDataDTO.setSaveStatus("Failed");
			businessDemandDataDTO.setErrDescription("Please update remark");
		}
		
		
		double existingNovemberValue = existingBusinessDemand.getNov() != null ? existingBusinessDemand.getNov() : 0.0;
		Double newNovemberValue = businessDemandDataDTO.getNov();
		if(!Objects.equals(existingNovemberValue, newNovemberValue) && Objects.equals(existingRemark, newRemark)) {  
			businessDemandDataDTO.setSaveStatus("Failed");
			businessDemandDataDTO.setErrDescription("Please update remark");
		}
		
		
		double existingDecemberValue = existingBusinessDemand.getDec() != null ? existingBusinessDemand.getDec() : 0.0;
		Double newDecemberValue = businessDemandDataDTO.getDec();
		if(!Objects.equals(existingDecemberValue, newDecemberValue) && Objects.equals(existingRemark, newRemark)) {  
			businessDemandDataDTO.setSaveStatus("Failed");
			businessDemandDataDTO.setErrDescription("Please update remark");
		}
		
		double existingJanuaryValue = existingBusinessDemand.getJan() != null ? existingBusinessDemand.getJan() : 0.0;
		Double newJanuaryValue = businessDemandDataDTO.getJan();
		if(!Objects.equals(existingJanuaryValue, newJanuaryValue) && Objects.equals(existingRemark, newRemark)) {  
			businessDemandDataDTO.setSaveStatus("Failed");
			businessDemandDataDTO.setErrDescription("Please update remark");
		}
		
		
		double existingFebruaryValue = existingBusinessDemand.getFeb() != null ? existingBusinessDemand.getFeb() : 0.0;
		Double newFebruaryValue = businessDemandDataDTO.getFeb();
		if(!Objects.equals(existingFebruaryValue, newFebruaryValue) && Objects.equals(existingRemark, newRemark)) {  
			businessDemandDataDTO.setSaveStatus("Failed");
			businessDemandDataDTO.setErrDescription("Please update remark");
		}
		
		
		
		double existingMarchValue = existingBusinessDemand.getMarch() != null ? existingBusinessDemand.getMarch() : 0.0;
		Double newMarchValue = businessDemandDataDTO.getMarch();
		if(!Objects.equals(existingMarchValue, newMarchValue) && Objects.equals(existingRemark, newRemark)) {  
			businessDemandDataDTO.setSaveStatus("Failed");
			businessDemandDataDTO.setErrDescription("Please update remark");
		}
		
		
	}
	
	void saveData(UUID normParameterFKId, Integer i, Double attributeValue, String remark, String plantId,
			String year) {

		Optional<NormAttributeTransactions> existingRecord = normAttributeTransactionsRepository
				.findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameterFKId, i, year);
		
		Optional<NormParameters> normParametersOpt=normParametersRepository.findById(normParameterFKId);
		// No need to do anything 
		/*
		 * if(normParametersOpt.get().getDependantAttributeId()!=null &&
		 * normParametersOpt.get().getDependantAttributeId().equalsIgnoreCase("Output"))
		 * { if(attributeValue!=null) { attributeValue = attributeValue/24.0; } }
		 */

		NormAttributeTransactions normAttributeTransactions;

		if (existingRecord.isPresent()) {
			normAttributeTransactions = existingRecord.get();
			normAttributeTransactions.setModifiedOn(new Date());
		} else {

			normAttributeTransactions = new NormAttributeTransactions();
			normAttributeTransactions.setCreatedOn(new Date());
			normAttributeTransactions.setAttributeValueVersion("V1");
			normAttributeTransactions.setUserName(Utility.getUserName());
			normAttributeTransactions.setNormParameterFKId(normParameterFKId);
			normAttributeTransactions.setAopMonth(i);
			normAttributeTransactions.setAuditYear(year);
		}

		normAttributeTransactions
				.setAttributeValue(attributeValue != null ? attributeValue.toString() : "0.0");
		normAttributeTransactions.setRemarks(remark);
		normAttributeTransactions.setUserName(Utility.getUserName());
		normAttributeTransactionsRepository.save(normAttributeTransactions);
	}
	
	void saveValue(UUID normParameterFKId, Integer i, String attributeValue, String remark, String plantId,
			String year) {

		Optional<NormAttributeTransactions> existingRecord = normAttributeTransactionsRepository
				.findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameterFKId, i, year);
		
		NormAttributeTransactions normAttributeTransactions;

		if (existingRecord.isPresent()) {
			normAttributeTransactions = existingRecord.get();
			normAttributeTransactions.setModifiedOn(new Date());
		} else {

			normAttributeTransactions = new NormAttributeTransactions();
			normAttributeTransactions.setCreatedOn(new Date());
			normAttributeTransactions.setAttributeValueVersion("V1");
			normAttributeTransactions.setUserName(Utility.getUserName());
			normAttributeTransactions.setNormParameterFKId(normParameterFKId);
			normAttributeTransactions.setAopMonth(i);
			normAttributeTransactions.setAuditYear(year);
		}

		normAttributeTransactions
				.setAttributeValue(attributeValue != null ? attributeValue.toString() : "0.0");
		normAttributeTransactions.setRemarks(remark);
		normAttributeTransactions.setUserName(Utility.getUserName());
		normAttributeTransactionsRepository.save(normAttributeTransactions);
	}

	
	public Double getAttributeValue(BusinessDemandDataDTO businessDemandDataDTO, Integer i) {
		switch (i) {
			case 1:
				return businessDemandDataDTO.getJan();
			case 2:
				return businessDemandDataDTO.getFeb();
			case 3:
				return businessDemandDataDTO.getMarch();
			case 4:
				return businessDemandDataDTO.getApril();
			case 5:
				return businessDemandDataDTO.getMay();
			case 6:
				return businessDemandDataDTO.getJune();
			case 7:
				return businessDemandDataDTO.getJuly();
			case 8:
				return businessDemandDataDTO.getAug();
			case 9:
				return businessDemandDataDTO.getSep();
			case 10:
				return businessDemandDataDTO.getOct();
			case 11:
				return businessDemandDataDTO.getNov();
			case 12:
				return businessDemandDataDTO.getDec();

		}
		return businessDemandDataDTO.getJan();
	}

	public String getValue(BusinessDemandMonthlyDTO businessDemandDataDTO, Integer i) {
		switch (i) {
			case 1:
				return businessDemandDataDTO.getJan();
			case 2:
				return businessDemandDataDTO.getFeb();
			case 3:
				return businessDemandDataDTO.getMar();
			case 4:
				return businessDemandDataDTO.getApr();
			case 5:
				return businessDemandDataDTO.getMay();
			case 6:
				return businessDemandDataDTO.getJun();
			case 7:
				return businessDemandDataDTO.getJul();
			case 8:
				return businessDemandDataDTO.getAug();
			case 9:
				return businessDemandDataDTO.getSep();
			case 10:
				return businessDemandDataDTO.getOct();
			case 11:
				return businessDemandDataDTO.getNov();
			case 12:
				return businessDemandDataDTO.getDec();

		}
		return businessDemandDataDTO.getJan();
	}


	@Override
	public List<BusinessDemandDataDTO> editBusinessDemandData(List<BusinessDemandDataDTO> businessDemandDataDTOList) {
		try {
			for (BusinessDemandDataDTO businessDemandDataDTO : businessDemandDataDTOList) {
				BusinessDemand businessDemand = new BusinessDemand();

				businessDemand.setApril(businessDemandDataDTO.getApril());
				businessDemand.setAug(businessDemandDataDTO.getAug());
				businessDemand.setAvgTph(businessDemandDataDTO.getAvgTph());
				businessDemand.setDec(businessDemandDataDTO.getDec());
				businessDemand.setFeb(businessDemandDataDTO.getFeb());
				if (businessDemandDataDTO.getId() != null) {
					businessDemand.setId(UUID.fromString(businessDemandDataDTO.getId()));
				}
				businessDemand.setJan(businessDemandDataDTO.getJan());
				businessDemand.setJuly(businessDemandDataDTO.getJuly());
				businessDemand.setJune(businessDemandDataDTO.getJune());
				businessDemand.setMarch(businessDemandDataDTO.getMarch());
				businessDemand.setMay(businessDemandDataDTO.getMay());
				businessDemand.setNormParameterId(UUID.fromString(businessDemandDataDTO.getNormParameterId()));
				businessDemand.setNov(businessDemandDataDTO.getNov());
				businessDemand.setOct(businessDemandDataDTO.getOct());
				businessDemand.setPlantId(UUID.fromString(businessDemandDataDTO.getPlantId()));
				businessDemand.setRemark(businessDemandDataDTO.getRemark());
				businessDemand.setSep(businessDemandDataDTO.getSep());
				businessDemand.setYear(businessDemandDataDTO.getYear());
				businessDemandDataRepository.save(businessDemand);
			}
			// TODO Auto-generated method stub
			return businessDemandDataDTOList;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to edit data", ex);
		}
	}

	@Override
	public BusinessDemandDataDTO deleteBusinessDemandData(UUID id) {
		// businessDemandDataRepository.softDelete(UUID.fromString(businessDemandDataDTO.getId()));

		BusinessDemand businessDemand = new BusinessDemand();
		businessDemand.setId(id);
		businessDemandDataRepository.delete(businessDemand);
		return null;
	}

	public List<Object[]> findByYearAndPlantFkId(String year, UUID plantFkId, String viewName) {
		try {
			String sql = "SELECT " + "Id, Remark, Jan, Feb, March, April, May, June, July, Aug, Sep, Oct, Nov, Dec, "
					+ "Year, Plant_FK_Id, NormParameters_FK_Id, AvgTPH, NormTypeDisplayOrder, "
					+ "NormParameterTypeId, NormParameterTypeName, NormParameterTypeDisplayName, "
					+ "CreatedOn, ModifiedOn, UpdatedBy, IsDeleted, MaterialDisplayOrder, "
					+ "Site_FK_Id, Vertical_FK_Id,isEditable,isVisible,UOM,DisplayName " + "FROM " + "[" + viewName + "]" + " "
					+ "WHERE (Year = :year AND Year IS NOT NULL) " + "AND Plant_FK_Id = :plantFkId "
					+ "ORDER BY NormTypeDisplayOrder, MaterialDisplayOrder";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("year", year);
			query.setParameter("plantFkId", plantFkId);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public AOPMessageVM saveBusinessDemand(String year, String plantFKId,
			List<BusinessDemandMonthlyDTO> businessDemandMonthlyDTOs) {
		try {
			List<BusinessDemandMonthlyDTO> failedList = new ArrayList<>();
			
			for (BusinessDemandMonthlyDTO businessDemandMonthlyDTO : businessDemandMonthlyDTOs) {
				if (businessDemandMonthlyDTO.getSaveStatus() != null
						&& businessDemandMonthlyDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(businessDemandMonthlyDTO);
					continue;
				}

				UUID normParameterFKId = UUID.fromString(businessDemandMonthlyDTO.getNormParameterFKId());

				Optional<NormParameters> optionNormParameters = normParametersRepository.findById(normParameterFKId);
				if (!optionNormParameters.isPresent()) {
					businessDemandMonthlyDTO.setSaveStatus("Failed");
					businessDemandMonthlyDTO.setErrDescription("Norm Paramter not found");
					failedList.add(businessDemandMonthlyDTO);
					continue;
				}
				if (optionNormParameters.isPresent() && (!optionNormParameters.get().getIsEditable())) {
					continue;
				}

				for (int i = 1; i <= 12; i++) {
					String attributeValue = getValue(businessDemandMonthlyDTO, i);

					saveValue(optionNormParameters.get().getId(), i, attributeValue,businessDemandMonthlyDTO.getRemarks(), plantFKId, year);
					if(businessDemandMonthlyDTO.getSaveStatus()!=null && businessDemandMonthlyDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
						failedList.add(businessDemandMonthlyDTO);
					}
				}
			}
			List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("configuration");
			for (ScreenMapping screenMapping : screenMappingList) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantFKId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(failedList);
			aopMessageVM.setMessage("Data updated successfully");
			return aopMessageVM;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	@Override
	public AOPMessageVM loadPlantContribution(String year, String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_LoadPlantContributionSummaryBusinessDemand";
			// System.out.println(storedProcedure);
			int count = executeDynamicUpdateProcedure(storedProcedure, plantId, year);
			Map<String, Integer> map = new HashMap<>();
			map.put("count", count);
			AOPMessageVM response = new AOPMessageVM();
			response.setData(map);
			response.setCode(200);
			response.setMessage("success");
			return response;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}
	public int executeDynamicUpdateProcedure(String procedureName, String plantId,
			String aopYear) {
		try {

			String callSql = "{call " + procedureName + "(?, ?)}";

			try (Connection connection = dataSource.getConnection();
					CallableStatement stmt = connection.prepareCall(callSql)) {

				// Set parameters in the correct order
				stmt.setString(1, plantId);
				stmt.setString(2, aopYear);

				// Execute the stored procedure
				int rowsAffected = stmt.executeUpdate();

				// Optional: commit if auto-commit is off
				if (!connection.getAutoCommit()) {
					connection.commit();
				}

				return rowsAffected;

			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	// -------------------------------------------------------------------------
	// Enhanced Export: May-March columns are locked/read-only; April is editable
	// -------------------------------------------------------------------------

	@Override
	public byte[] exportBusinessDemandV2(String year, String plantId, boolean isAfterSave,
			List<BusinessDemandDataDTO> dtoList) {
		try {
			if (!isAfterSave) {
				Map<String, Object> map = (Map<String, Object>) getBusinessDemandData(year, plantId).getData();
				dtoList = (List<BusinessDemandDataDTO>) map.get("businessDemandDataDTOList");
			}

			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Sheet1");
			sheet.protectSheet("Secret_Password");
			int currentRow = 0;

		List<List<Object>> rows = new ArrayList<>();
		for (BusinessDemandDataDTO dto : dtoList) {
			List<Object> list = new ArrayList<>();
			list.add(dto.getDisplayName());
			list.add(dto.getNormParameterTypeDisplayName());
			list.add(dto.getUOM());
			list.add(dto.getApril());
			list.add(dto.getMay());
			list.add(dto.getJune());
			list.add(dto.getJuly());
			list.add(dto.getAug());
			list.add(dto.getSep());
			list.add(dto.getOct());
			list.add(dto.getNov());
			list.add(dto.getDec());
			list.add(dto.getJan());
			list.add(dto.getFeb());
			list.add(dto.getMarch());
			list.add(dto.getRemark());
			list.add(dto.getId());
			list.add(dto.getNormParameterId());
			if (isAfterSave) {
				list.add(dto.getSaveStatus());
				list.add(dto.getErrDescription());
			}
			rows.add(list);
		}

		List<String> innerHeaders = new ArrayList<>();
		innerHeaders.add("Particulars");
		innerHeaders.add("Type");
		innerHeaders.add("UOM");
		innerHeaders.add(getMonth(year, 4));
		innerHeaders.add(getMonth(year, 5));
		innerHeaders.add(getMonth(year, 6));
		innerHeaders.add(getMonth(year, 7));
		innerHeaders.add(getMonth(year, 8));
		innerHeaders.add(getMonth(year, 9));
		innerHeaders.add(getMonth(year, 10));
		innerHeaders.add(getMonth(year, 11));
		innerHeaders.add(getMonth(year, 12));
		innerHeaders.add(getMonth(year, 1));
		innerHeaders.add(getMonth(year, 2));
		innerHeaders.add(getMonth(year, 3));
		innerHeaders.add("Remark");
		innerHeaders.add("Id");
		innerHeaders.add("NormParameterId");
		if (isAfterSave) {
			innerHeaders.add("Status");
			innerHeaders.add("Error Description");
		}

		// Column index and sizing constants for the Remark column
		final int REMARK_COL = 15;
		final int REMARK_COL_WIDTH_CHARS = 50;
		final float DEFAULT_ROW_HEIGHT_POINTS = sheet.getDefaultRowHeightInPoints();

		// Styles: unlocked for all month columns (cols 3-14) and Remark (col 15),
		// locked default for all other columns; all styles include THIN borders
		CellStyle unlockedAprilStyle = Utility.createUnlockedStyle(workbook);
		unlockedAprilStyle.setBorderBottom(BorderStyle.THIN);
		unlockedAprilStyle.setBorderTop(BorderStyle.THIN);
		unlockedAprilStyle.setBorderLeft(BorderStyle.THIN);
		unlockedAprilStyle.setBorderRight(BorderStyle.THIN);

		// Remark style: unlocked, wrap text enabled, thin borders
		CellStyle unlockedRemarkStyle = Utility.createUnlockedStyle(workbook);
		unlockedRemarkStyle.setWrapText(true);
		unlockedRemarkStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.TOP);
		unlockedRemarkStyle.setBorderBottom(BorderStyle.THIN);
		unlockedRemarkStyle.setBorderTop(BorderStyle.THIN);
		unlockedRemarkStyle.setBorderLeft(BorderStyle.THIN);
		unlockedRemarkStyle.setBorderRight(BorderStyle.THIN);

		CellStyle lockedDefaultStyle = buildLockedDefaultStyle(workbook);
		lockedDefaultStyle.setBorderBottom(BorderStyle.THIN);
		lockedDefaultStyle.setBorderTop(BorderStyle.THIN);
		lockedDefaultStyle.setBorderLeft(BorderStyle.THIN);
		lockedDefaultStyle.setBorderRight(BorderStyle.THIN);

		// Track max content length per column for dynamic width sizing
		int[] maxColWidths = new int[innerHeaders.size()];
		for (int col = 0; col < innerHeaders.size(); col++) {
			maxColWidths[col] = innerHeaders.get(col).length();
		}

		// Header row (locked by default when sheet protection is active)
		Row headerRow = sheet.createRow(currentRow++);
		for (int col = 0; col < innerHeaders.size(); col++) {
			Cell cell = headerRow.createCell(col);
			cell.setCellValue(innerHeaders.get(col));
			cell.setCellStyle(Utility.createBoldBorderedStyle(workbook));
		}

		// Data rows with per-column locking styles
		for (List<Object> rowData : rows) {
			Row row = sheet.createRow(currentRow++);
			int remarkLen = 0;
			for (int col = 0; col < rowData.size(); col++) {
				Cell cell = row.createCell(col);
				Object value = rowData.get(col);
				String strValue;
				if (value instanceof Number) {
					cell.setCellValue(((Number) value).doubleValue());
					strValue = value.toString();
				} else if (value instanceof Boolean) {
					cell.setCellValue((Boolean) value);
					strValue = value.toString();
				} else if (value != null) {
					cell.setCellValue(value.toString());
					strValue = value.toString();
				} else {
					cell.setCellValue("");
					strValue = "";
				}

				// Track max content width for dynamic column sizing
				if (col < maxColWidths.length) {
					maxColWidths[col] = Math.max(maxColWidths[col], strValue.length());
				}

				if (col == REMARK_COL) {
					// Remark column: editable with text wrapping
					cell.setCellStyle(unlockedRemarkStyle);
					remarkLen = strValue.length();
				} else if (col >= 3 && col <= 14) {
					// Month columns (April through March): editable
					cell.setCellStyle(unlockedAprilStyle);
				} else {
					// Remaining columns (Particulars, Type, UOM, Id, NormParameterId, Status...)
					cell.setCellStyle(lockedDefaultStyle);
				}
			}
			// Expand row height to fit wrapped remark text
			if (remarkLen > 0) {
				int numLines = Math.max(1, (int) Math.ceil((double) remarkLen / REMARK_COL_WIDTH_CHARS));
				row.setHeightInPoints(numLines * DEFAULT_ROW_HEIGHT_POINTS);
			}
		}

		// Apply dynamic column widths based on tracked content length;
		// Remark column always gets its fixed preferred width
		for (int col = 0; col < maxColWidths.length; col++) {
			if (col == REMARK_COL) {
				sheet.setColumnWidth(col, REMARK_COL_WIDTH_CHARS * 256);
			} else {
				int width = Math.max(maxColWidths[col] + 4, 8); // +4 padding, 8 char minimum
				width = Math.min(width, 60);                     // cap at 60 chars
				sheet.setColumnWidth(col, width * 256);
			}
		}

		sheet.setColumnHidden(16, true);
		sheet.setColumnHidden(17, true);

		// Protect the sheet so that locked cells become non-editable
		sheet.protectSheet("");

			try {
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

	/**
	 * Writes a read-only Production Target section into the given sheet starting at
	 * {@code startRow}. The section consists of a title row, a header row (13 columns:
	 * Particulars + 12 months; no Remarks), data rows, and a blank separator row.
	 *
	 * @return the next available row index after the section
	 */
	@SuppressWarnings("unchecked")
	private int writeProductionTargetSection(Sheet sheet, Workbook workbook, String year, String plantId, int startRow) {
		try {
			AOPMessageVM vm = aopMCCalculatedDataService.getAOPMCCalculatedData(plantId, year);
			List<AOPMCCalculatedDataDTO> ptList = null;
			if (vm != null && vm.getData() != null) {
				Map<String, Object> dataMap = (Map<String, Object>) vm.getData();
				ptList = (List<AOPMCCalculatedDataDTO>) dataMap.get("aopMCCalculatedDataDTOList");
			}

		// Locked + grey + bordered style for all PT data cells
		CellStyle lockedGreyStyle = Utility.createLockedStyle(workbook);
		lockedGreyStyle.setBorderBottom(BorderStyle.THIN);
		lockedGreyStyle.setBorderTop(BorderStyle.THIN);
		lockedGreyStyle.setBorderLeft(BorderStyle.THIN);
		lockedGreyStyle.setBorderRight(BorderStyle.THIN);

		// Bold + locked + grey + bordered style for title and header rows
		CellStyle boldLockedGreyStyle = Utility.createLockedStyle(workbook);
		Font boldFont = workbook.createFont();
		boldFont.setBold(true);
		boldLockedGreyStyle.setFont(boldFont);
		boldLockedGreyStyle.setBorderBottom(BorderStyle.THIN);
		boldLockedGreyStyle.setBorderTop(BorderStyle.THIN);
		boldLockedGreyStyle.setBorderLeft(BorderStyle.THIN);
		boldLockedGreyStyle.setBorderRight(BorderStyle.THIN);

			// Title row
			Row titleRow = sheet.createRow(startRow++);
			Cell titleCell = titleRow.createCell(0);
			titleCell.setCellValue("Proposed Operating Capacity / Production Volume Target (PVT)");
			titleCell.setCellStyle(boldLockedGreyStyle);

			// Header row – Particulars + 12 academic-year months; NO Remarks column
			List<String> ptHeaders = new ArrayList<>();
			ptHeaders.add("Particulars");
			ptHeaders.add(getMonth(year, 4));
			ptHeaders.add(getMonth(year, 5));
			ptHeaders.add(getMonth(year, 6));
			ptHeaders.add(getMonth(year, 7));
			ptHeaders.add(getMonth(year, 8));
			ptHeaders.add(getMonth(year, 9));
			ptHeaders.add(getMonth(year, 10));
			ptHeaders.add(getMonth(year, 11));
			ptHeaders.add(getMonth(year, 12));
			ptHeaders.add(getMonth(year, 1));
			ptHeaders.add(getMonth(year, 2));
			ptHeaders.add(getMonth(year, 3));

			Row headerRow = sheet.createRow(startRow++);
			for (int col = 0; col < ptHeaders.size(); col++) {
				Cell cell = headerRow.createCell(col);
				cell.setCellValue(ptHeaders.get(col));
				cell.setCellStyle(boldLockedGreyStyle);
			}

			// Data rows – locked and greyed out; no ID or metadata columns
			if (ptList != null) {
				for (AOPMCCalculatedDataDTO dto : ptList) {
					Row row = sheet.createRow(startRow++);
					int col = 0;
					Object[] values = {
						dto.getMaterialDisplayName(),
						dto.getApril(), dto.getMay(), dto.getJune(), dto.getJuly(),
						dto.getAugust(), dto.getSeptember(), dto.getOctober(),
						dto.getNovember(), dto.getDecember(),
						dto.getJanuary(), dto.getFebruary(), dto.getMarch()
					};
					for (Object val : values) {
						Cell cell = row.createCell(col++);
						setCellValue(cell, val);
						cell.setCellStyle(lockedGreyStyle);
					}
				}
			}

			// Blank separator row before Business Demand section
			sheet.createRow(startRow++);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return startRow;
	}

	private void setCellValue(Cell cell, Object value) {
		if (value instanceof Number) {
			cell.setCellValue(((Number) value).doubleValue());
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
		} else if (value != null) {
			cell.setCellValue(value.toString());
		} else {
			cell.setCellValue("");
		}
	}

	private CellStyle buildLockedDefaultStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setLocked(true);
		return style;
	}

	// -------------------------------------------------------------------------
	// Enhanced Import: April is copied to all months; ON/OFF UOM validation
	// -------------------------------------------------------------------------

	@Override
	public AOPMessageVM importExcelV2(String year, UUID plantFKId, MultipartFile file) {
		try {
			List<BusinessDemandDataDTO> data = readBusinessDemandV2(file.getInputStream(), plantFKId, year);
			List<BusinessDemandDataDTO> failedRecords = saveBusinessDemandData(data);

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = exportBusinessDemandV2(year, plantFKId.toString(), true, failedRecords);
				String base64File = Base64.getEncoder().encodeToString(fileByteArray);
				aopMessageVM.setData(base64File);
				aopMessageVM.setCode(400);
				aopMessageVM.setMessage("Partial data has been saved");
			} else {
				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("All data has been saved");
			}

			return aopMessageVM;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<BusinessDemandDataDTO> readBusinessDemandV2(InputStream inputStream, UUID plantFKId, String year) {
		List<BusinessDemandDataDTO> configList = new ArrayList<>();
		String verticalName = plantsRepository.findVerticalNameByPlantId(plantFKId);
		Plants plant = plantsRepository.findById(plantFKId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		boolean pvc = vertical.getName().equalsIgnoreCase("PVC")
				&& (site.getName().equalsIgnoreCase("VMD") || site.getName().equalsIgnoreCase("DMD"));

		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();

			if (rowIterator.hasNext())
				rowIterator.next(); // skip header row

			List<BusinessDemandDataDTO> productionDtos = new ArrayList<>();

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				BusinessDemandDataDTO dto = new BusinessDemandDataDTO();
				try {
				dto.setDisplayName(getStringCellValue(row.getCell(0), dto));
				// col 1 is "Type" (display/export only) — not read or persisted during import
				dto.setUOM(getStringCellValue(row.getCell(2), dto));

			// Read each month individually from its own column (shifted +1 due to Type column)
			dto.setApril(getNumericCellValue(row.getCell(3), dto));
			dto.setMay(getNumericCellValue(row.getCell(4), dto));
			dto.setJune(getNumericCellValue(row.getCell(5), dto));
			dto.setJuly(getNumericCellValue(row.getCell(6), dto));
			dto.setAug(getNumericCellValue(row.getCell(7), dto));
			dto.setSep(getNumericCellValue(row.getCell(8), dto));
			dto.setOct(getNumericCellValue(row.getCell(9), dto));
			dto.setNov(getNumericCellValue(row.getCell(10), dto));
			dto.setDec(getNumericCellValue(row.getCell(11), dto));
			dto.setJan(getNumericCellValue(row.getCell(12), dto));
			dto.setFeb(getNumericCellValue(row.getCell(13), dto));
			dto.setMarch(getNumericCellValue(row.getCell(14), dto));

				// Validate all month values when UOM is ON/OFF
				String uom = dto.getUOM();
				if (uom != null && uom.equalsIgnoreCase("ON/OFF")
						&& !"Failed".equalsIgnoreCase(dto.getSaveStatus())) {
					Double[] monthValues = { dto.getApril(), dto.getMay(), dto.getJune(), dto.getJuly(),
							dto.getAug(), dto.getSep(), dto.getOct(), dto.getNov(), dto.getDec(),
							dto.getJan(), dto.getFeb(), dto.getMarch() };
					String[] monthNames = { "April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec",
							"Jan", "Feb", "March" };
					for (int m = 0; m < monthValues.length; m++) {
						Double val = monthValues[m];
						if (val == null || (Double.compare(val, 1.0) != 0 && Double.compare(val, 0.0) != 0)) {
							dto.setSaveStatus("Failed");
							dto.setErrDescription("For UOM 'ON/OFF', " + monthNames[m]
									+ " value must be 1 (ON) or 0 (OFF). Provided value: " + val);
							break;
						}
					}
				}

				dto.setPlantId(plantFKId.toString());
				String normParameterId = getStringCellValue(row.getCell(17), dto);
				dto.setNormParameterId(normParameterId);

					boolean isProduction = false;
					if (verticalName != null
							&& (verticalName.equalsIgnoreCase("PE") || verticalName.equalsIgnoreCase("PP")
									|| verticalName.equalsIgnoreCase("PET") || pvc)
							&& normParameterId != null) {
						isProduction = isProductionType(normParameterId, normParametersRepository,
								normParameterTypeRepository);
					}

					if (isProduction) {
						productionDtos.add(dto);
					}

				dto.setRemark(getStringCellValue(row.getCell(15), dto));
				dto.setId(getStringCellValue(row.getCell(16), dto));

					// Skip rows that have no id and all months are zero/null
					boolean allMonthsZero = (dto.getApril() == null || dto.getApril() == 0.0)
							&& (dto.getMay() == null || dto.getMay() == 0.0)
							&& (dto.getJune() == null || dto.getJune() == 0.0)
							&& (dto.getJuly() == null || dto.getJuly() == 0.0)
							&& (dto.getAug() == null || dto.getAug() == 0.0)
							&& (dto.getSep() == null || dto.getSep() == 0.0)
							&& (dto.getOct() == null || dto.getOct() == 0.0)
							&& (dto.getNov() == null || dto.getNov() == 0.0)
							&& (dto.getDec() == null || dto.getDec() == 0.0)
							&& (dto.getJan() == null || dto.getJan() == 0.0)
							&& (dto.getFeb() == null || dto.getFeb() == 0.0)
							&& (dto.getMarch() == null || dto.getMarch() == 0.0);

					if (dto.getId() == null && allMonthsZero) {
						continue;
					}

					dto.setVerticalFKId(vertical.getId().toString());
					dto.setSiteFKId(site.getId().toString());
					dto.setYear(year);

				} catch (Exception e) {
					e.printStackTrace();
					dto.setErrDescription(e.getMessage());
					dto.setSaveStatus("Failed");
				}
				configList.add(dto);
			}

			// Production percentage sum validation (same logic as readBusinessDemand)
			if (!productionDtos.isEmpty() && (verticalName.equalsIgnoreCase("PE")
					|| verticalName.equalsIgnoreCase("PP") || verticalName.equalsIgnoreCase("PET") || pvc)) {
				Map<String, Double> monthlyProductionSums = new HashMap<>();
				String[] months = { "April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan", "Feb",
						"March" };

				for (BusinessDemandDataDTO dto : productionDtos) {
					monthlyProductionSums.merge("April", dto.getApril() != null ? dto.getApril() : 0.0, Double::sum);
					monthlyProductionSums.merge("May", dto.getMay() != null ? dto.getMay() : 0.0, Double::sum);
					monthlyProductionSums.merge("June", dto.getJune() != null ? dto.getJune() : 0.0, Double::sum);
					monthlyProductionSums.merge("July", dto.getJuly() != null ? dto.getJuly() : 0.0, Double::sum);
					monthlyProductionSums.merge("Aug", dto.getAug() != null ? dto.getAug() : 0.0, Double::sum);
					monthlyProductionSums.merge("Sep", dto.getSep() != null ? dto.getSep() : 0.0, Double::sum);
					monthlyProductionSums.merge("Oct", dto.getOct() != null ? dto.getOct() : 0.0, Double::sum);
					monthlyProductionSums.merge("Nov", dto.getNov() != null ? dto.getNov() : 0.0, Double::sum);
					monthlyProductionSums.merge("Dec", dto.getDec() != null ? dto.getDec() : 0.0, Double::sum);
					monthlyProductionSums.merge("Jan", dto.getJan() != null ? dto.getJan() : 0.0, Double::sum);
					monthlyProductionSums.merge("Feb", dto.getFeb() != null ? dto.getFeb() : 0.0, Double::sum);
					monthlyProductionSums.merge("March", dto.getMarch() != null ? dto.getMarch() : 0.0, Double::sum);
				}

				for (String month : months) {
					Double sum = monthlyProductionSums.getOrDefault(month, 0.0);
					if (Math.abs(sum - 100.0) > 0.001) {
						for (BusinessDemandDataDTO dto : productionDtos) {
							if (!"Failed".equalsIgnoreCase(dto.getSaveStatus())) {
								dto.setSaveStatus("Failed");
								dto.setErrDescription(month + " Production sum is "
										+ String.format("%.2f", sum) + ", but must be 100.");
							} else {
								String existingError = dto.getErrDescription() != null ? dto.getErrDescription() : "";
								dto.setErrDescription(existingError + "; " + month + " Production sum is "
										+ String.format("%.2f", sum) + ", but must be 100.");
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return configList;
	}


	@Override
	public AOPMessageVM calculateBusinessDemand(String year, String plantId) {
		
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		String verticalName = plantsRepository.findVerticalNameByPlantId(plant.getId());

		String storedProcedure = verticalName + "_" + site.getName() + "_CalculateBusinessDemand";
		
		Integer result = executeBusinessDemandCalculationSP(storedProcedure, String.valueOf(plant.getId()), year);
				
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Data fetched successfully");
		aopMessageVM.setData(result);
		
		aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year,
				"business-demand");
		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("business-demand");
		for (ScreenMapping screenMapping : screenMappingList) {
			AopCalculation aopCalculation = new AopCalculation();
			aopCalculation.setAopYear(year);
			aopCalculation.setIsChanged(true);
			aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
			aopCalculation.setPlantId(UUID.fromString(plantId));
			aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
			aopCalculationRepository.save(aopCalculation);
		}
		return aopMessageVM;
	}


	public Integer executeBusinessDemandCalculationSP(String procedureName, String plantId,  String aopYear) {
	try {



		String callSql = "{call " + "[" + procedureName + "]" + "(?, ?)}";

		try (Connection connection = dataSource.getConnection();
				CallableStatement stmt = connection.prepareCall(callSql)) {

			// Set parameters in the correct order
			stmt.setString(1, plantId); 
			stmt.setString(2, aopYear); 
		

			// Execute the stored procedure
			int rowsAffected = stmt.executeUpdate();

			// Optional: commit if auto-commit is off
			if (!connection.getAutoCommit()) {
				connection.commit();
			}

			return rowsAffected;

		} catch (SQLException e) {
			throw new RuntimeException("Failed to execute business demand calculation", e);
		}

	} catch (IllegalArgumentException e) {
		throw new RestInvalidArgumentException("Invalid UUID format ", e);
	} catch (Exception ex) {
		throw new RuntimeException("Failed to fetch data", ex);
	}
}

public List<BusinessDemandDataDTO> readBusinessDemandWithTotal(InputStream inputStream, UUID plantFKId, String year) {
	List<BusinessDemandDataDTO> configList = new ArrayList<>();
	String verticalName = plantsRepository.findVerticalNameByPlantId(plantFKId);
	Plants plant = plantsRepository.findById((plantFKId))
			.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
	Sites site = siteRepository.findById(plant.getSiteFkId()).get();
	Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
	boolean pvc= vertical.getName().equalsIgnoreCase("PVC") && (site.getName().equalsIgnoreCase("VMD") || site.getName().equalsIgnoreCase("DMD"));

	boolean pvcDmd = vertical.getName().equalsIgnoreCase("PVC") && site.getName().equalsIgnoreCase("DMD");
	try (Workbook workbook = new XSSFWorkbook(inputStream)) {
		int numberOfSheets = workbook.getNumberOfSheets();
		for (int sheetIndex = 0; sheetIndex < numberOfSheets; sheetIndex++) {
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			int lastRowNum = sheet.getLastRowNum();

			// Locate the Business Demand section.  In the combined layout the sheet
			// starts with a Production Target section followed by a "Business Demand"
			// title row; data begins two rows after that title (title + header skipped).
			// Falls back to row 1 for legacy files that have only one section.
			int bdDataStartRow = 1;
			for (int r = 0; r <= lastRowNum; r++) {
				Row scanRow = sheet.getRow(r);
				if (scanRow == null) continue;
				Cell firstCell = scanRow.getCell(0);
				if (firstCell != null && firstCell.getCellType() == CellType.STRING
						&& "Business Demand".equalsIgnoreCase(firstCell.getStringCellValue())) {
					bdDataStartRow = r + 2; // skip title row + header row
					break;
				}
			}

			List<BusinessDemandDataDTO> productionDtos = new ArrayList<>();

			for (int rowIdx = bdDataStartRow; rowIdx <= lastRowNum; rowIdx++) {
				Row row = sheet.getRow(rowIdx);
				if (row == null) continue;
				BusinessDemandDataDTO dto = new BusinessDemandDataDTO();
				try {
				dto.setDisplayName(getStringCellValue(row.getCell(0), dto));
				// Skip the Total summary row added by the export
				if ("Total".equalsIgnoreCase(dto.getDisplayName())) continue;
				// col 1 (Type) is display-only; skip during import
				dto.setUOM(getStringCellValue(row.getCell(2), dto));
				dto.setApril(getNumericCellValue(row.getCell(3), dto));
				dto.setMay(getNumericCellValue(row.getCell(4), dto));
				dto.setJune(getNumericCellValue(row.getCell(5), dto));
				dto.setJuly(getNumericCellValue(row.getCell(6), dto));
				dto.setAug(getNumericCellValue(row.getCell(7), dto));
				dto.setSep(getNumericCellValue(row.getCell(8), dto));
				dto.setOct(getNumericCellValue(row.getCell(9), dto));
				dto.setNov(getNumericCellValue(row.getCell(10), dto));
				dto.setDec(getNumericCellValue(row.getCell(11), dto));
				dto.setJan(getNumericCellValue(row.getCell(12), dto));
				dto.setFeb(getNumericCellValue(row.getCell(13), dto));
				dto.setMarch(getNumericCellValue(row.getCell(14), dto));
				dto.setPlantId(plantFKId.toString());
				String normParameterId = getStringCellValue(row.getCell(18), dto);
					dto.setNormParameterId(normParameterId);
					boolean isProduction = false;
					if (verticalName != null
							&& (verticalName.equalsIgnoreCase("PE") || verticalName.equalsIgnoreCase("PP") || verticalName.equalsIgnoreCase("PET") || pvc)
							&& normParameterId != null) {

						isProduction = isProductionType(normParameterId, normParametersRepository, normParameterTypeRepository);
					}

					if (isProduction) {
						productionDtos.add(dto);
					}
				dto.setRemark(getStringCellValue(row.getCell(16), dto));

				dto.setId(getStringCellValue(row.getCell(17), dto));

				if (pvcDmd) {
					dto.setLineId(getStringCellValue(row.getCell(19), dto));
				}

					// Check if id is null AND all month values are zero or null
					boolean allMonthsZero = (dto.getApril() == null || dto.getApril() == 0.0)
							&& (dto.getMay() == null || dto.getMay() == 0.0)
							&& (dto.getJune() == null || dto.getJune() == 0.0)
							&& (dto.getJuly() == null || dto.getJuly() == 0.0)
							&& (dto.getAug() == null || dto.getAug() == 0.0)
							&& (dto.getSep() == null || dto.getSep() == 0.0)
							&& (dto.getOct() == null || dto.getOct() == 0.0)
							&& (dto.getNov() == null || dto.getNov() == 0.0)
							&& (dto.getDec() == null || dto.getDec() == 0.0)
							&& (dto.getJan() == null || dto.getJan() == 0.0)
							&& (dto.getFeb() == null || dto.getFeb() == 0.0)
							&& (dto.getMarch() == null || dto.getMarch() == 0.0);

					if (dto.getId() == null && allMonthsZero) {
						continue;
					}

					dto.setVerticalFKId(vertical.getId().toString());
					dto.setSiteFKId(site.getId().toString());
					dto.setYear(year);

				} catch (Exception e) {
					e.printStackTrace();
					dto.setErrDescription(e.getMessage());
					dto.setSaveStatus("Failed");
				}
				configList.add(dto);
			}

			if (!productionDtos.isEmpty() && (verticalName.equalsIgnoreCase("PE") || verticalName.equalsIgnoreCase("PP") || verticalName.equalsIgnoreCase("PET") || pvc)) {

				Map<String, Double> monthlyProductionSums = new HashMap<>();
				String[] months = {"April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "March"};

				for (BusinessDemandDataDTO dto : productionDtos) {
					monthlyProductionSums.merge("April", dto.getApril() != null ? dto.getApril() : 0.0, Double::sum);
					monthlyProductionSums.merge("May", dto.getMay() != null ? dto.getMay() : 0.0, Double::sum);
					monthlyProductionSums.merge("June", dto.getJune() != null ? dto.getJune() : 0.0, Double::sum);
					monthlyProductionSums.merge("July", dto.getJuly() != null ? dto.getJuly() : 0.0, Double::sum);
					monthlyProductionSums.merge("Aug", dto.getAug() != null ? dto.getAug() : 0.0, Double::sum);
					monthlyProductionSums.merge("Sep", dto.getSep() != null ? dto.getSep() : 0.0, Double::sum);
					monthlyProductionSums.merge("Oct", dto.getOct() != null ? dto.getOct() : 0.0, Double::sum);
					monthlyProductionSums.merge("Nov", dto.getNov() != null ? dto.getNov() : 0.0, Double::sum);
					monthlyProductionSums.merge("Dec", dto.getDec() != null ? dto.getDec() : 0.0, Double::sum);
					monthlyProductionSums.merge("Jan", dto.getJan() != null ? dto.getJan() : 0.0, Double::sum);
					monthlyProductionSums.merge("Feb", dto.getFeb() != null ? dto.getFeb() : 0.0, Double::sum);
					monthlyProductionSums.merge("March", dto.getMarch() != null ? dto.getMarch() : 0.0, Double::sum);
				}

				for (String month : months) {
					Double sum = monthlyProductionSums.getOrDefault(month, 0.0);
					if (Math.abs(sum - 100.0) > 0.001) {
						for (BusinessDemandDataDTO dto : productionDtos) {
							if (!"Failed".equalsIgnoreCase(dto.getSaveStatus())) {
								dto.setSaveStatus("Failed");
								dto.setErrDescription(month + " Production sum is " + String.format("%.2f", sum) + ", but must be 100.");
							} else {
								String existingError = dto.getErrDescription() != null ? dto.getErrDescription() : "";
								dto.setErrDescription(existingError + "; " + month + " Production sum is " + String.format("%.2f", sum) + ", but must be 100.");
							}
						}
					}
				}
			}
		}

	} catch (Exception e) {
		e.printStackTrace();
	}

	return configList;
}



	


}
