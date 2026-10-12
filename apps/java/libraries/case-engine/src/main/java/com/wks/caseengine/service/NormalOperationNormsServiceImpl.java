package com.wks.caseengine.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.io.ByteArrayOutputStream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;

import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.caseengine.dto.AOPDTO;
import com.wks.caseengine.dto.MCUNormsValueDTO;
import com.wks.caseengine.dto.ModeWiseNormsDTO;
import com.wks.caseengine.dto.NormConfigurationDTO;
import com.wks.caseengine.dto.SteadyStateNormDTO;
import com.wks.caseengine.dto.ValidationErrorDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.MCUNormsValue;
import com.wks.caseengine.entity.MCUNormsValueGrade;
import com.wks.caseengine.entity.NormParameterType;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.NormsTransactions;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AOPRepository;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.MCUNormsValueGradeRepository;
import com.wks.caseengine.repository.MCUNormsValueRepository;
import com.wks.caseengine.repository.NormParameterTypeRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.NormalOperationNormsRepository;
import com.wks.caseengine.repository.NormsTransactionRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Service
public class NormalOperationNormsServiceImpl implements NormalOperationNormsService {

	@Autowired
	private NormalOperationNormsRepository normalOperationNormsRepository;

	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	PlantsRepository plantsRepository;
	@Autowired
	SiteRepository siteRepository;
	@Autowired
	VerticalsRepository verticalRepository;
	@Autowired
	private NormsTransactionRepository normsTransactionRepository;

	@Autowired
	private ScreenMappingRepository screenMappingRepository;

	@Autowired
	private AopCalculationRepository aopCalculationRepository;

	@Autowired
	private MCUNormsValueGradeRepository mcuNormsValueGradeRepository;

	private DataSource dataSource;

	@Autowired
	private NormParametersRepository normParametersRepository;

	@Autowired
	private FinalNormsService finalNormsService;

	@Autowired
	private NormParameterTypeRepository normParameterTypeRepository;

	@Autowired
	private MCUNormsValueRepository mcuNormsValueRepository;

	@Autowired
	private AOPRepository aopRepository;
	
	@Autowired
	private AOPServiceImpl aOPServiceImpl;
	
	@Autowired
    private MCUNormsValueGradeRepository mCUNormsValueGradeRepository;
	// Inject or set your DataSource (e.g., via constructor or setter)
	public NormalOperationNormsServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public AOPMessageVM getNormalOperationNormsData(String year, String plantId, String gradeId, String mode) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
	    boolean pvc= verticalName.equalsIgnoreCase("PVC") && (site.getName().equalsIgnoreCase("VMD") || site.getName().equalsIgnoreCase("DMD") || site.getName().equalsIgnoreCase("HMD"));
		boolean chemical = verticalName.equalsIgnoreCase("Chemical");
		boolean ptaPmdPia = verticalName.equalsIgnoreCase("PTA") && site.getName().equalsIgnoreCase("PMD") && plant.getName().equalsIgnoreCase("PIA");
		Boolean withGrade = false;
		Boolean elastomer = verticalName.equalsIgnoreCase("ELASTOMER") && site.getName().equalsIgnoreCase("JMD") && plant.getName().equalsIgnoreCase("HIIR");
		if ((plant.getName().equalsIgnoreCase("SBR") && site.getName().equalsIgnoreCase("HMD")
				&& vertical.getName().equalsIgnoreCase("ELASTOMER")) || (vertical.getName().equalsIgnoreCase("STAPLE")&& gradeId != null && !gradeId.trim().isEmpty() ) ||  (vertical.getName().equalsIgnoreCase("Filament")&& gradeId != null && !gradeId.trim().isEmpty() )) {
			withGrade = true;
		}
		try {
			List<Object[]> obj = null;
			if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("PTA") || vertical.getName().equalsIgnoreCase("Chemical")) {
				String procedureName = vertical.getName() + "_" + site.getName() + "_" + "GetNormalOperationNorms";
				obj = findByYearAndPlantId(year, UUID.fromString(plantId), procedureName);
			} else {
				obj = getNormalOperationNormsDataFromView(year, UUID.fromString(plantId), gradeId, mode);
			}

			List<MCUNormsValueDTO> mCUNormsValueDTOList = new ArrayList<>();

			for (Object[] row : obj) {
				MCUNormsValueDTO mCUNormsValueDTO = new MCUNormsValueDTO();
				mCUNormsValueDTO.setId(row[0].toString());
				mCUNormsValueDTO.setSiteFkId(row[1].toString());
				mCUNormsValueDTO.setPlantFkId(row[2].toString());
				mCUNormsValueDTO.setVerticalFkId(row[3].toString());

				if (vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")
						|| vertical.getName().equalsIgnoreCase("PET") || withGrade || pvc || elastomer) {
					mCUNormsValueDTO.setGradeId(row[4].toString());
					mCUNormsValueDTO.setMaterialFkId(row[5].toString());
					mCUNormsValueDTO.setApril(row[6] != null ? Double.parseDouble(row[6].toString()) : null);
					mCUNormsValueDTO.setMay(row[7] != null ? Double.parseDouble(row[7].toString()) : null);
					mCUNormsValueDTO.setJune(row[8] != null ? Double.parseDouble(row[8].toString()) : null);
					mCUNormsValueDTO.setJuly(row[9] != null ? Double.parseDouble(row[9].toString()) : null);
					mCUNormsValueDTO.setAugust(row[10] != null ? Double.parseDouble(row[10].toString()) : null);
					mCUNormsValueDTO.setSeptember(row[11] != null ? Double.parseDouble(row[11].toString()) : null);
					mCUNormsValueDTO.setOctober(row[12] != null ? Double.parseDouble(row[12].toString()) : null);
					mCUNormsValueDTO.setNovember(row[13] != null ? Double.parseDouble(row[13].toString()) : null);
					mCUNormsValueDTO.setDecember(row[14] != null ? Double.parseDouble(row[14].toString()) : null);
					mCUNormsValueDTO.setJanuary(row[15] != null ? Double.parseDouble(row[15].toString()) : null);
					mCUNormsValueDTO.setFebruary(row[16] != null ? Double.parseDouble(row[16].toString()) : null);
					mCUNormsValueDTO.setMarch(row[17] != null ? Double.parseDouble(row[17].toString()) : null);

					mCUNormsValueDTO.setFinancialYear(row[18].toString());
					mCUNormsValueDTO.setRemarks(row[19] != null ? row[19].toString() : "");
					mCUNormsValueDTO.setCreatedOn(row[20] != null ? (Date) row[20] : null);
					mCUNormsValueDTO.setModifiedOn(row[21] != null ? (Date) row[21] : null);
					mCUNormsValueDTO.setMcuVersion(row[22] != null ? row[22].toString() : null);
					mCUNormsValueDTO.setUpdatedBy(row[23] != null ? row[23].toString() : null);
					mCUNormsValueDTO.setNormParameterTypeId(row[24] != null ? row[24].toString() : null);
					mCUNormsValueDTO.setNormParameterTypeName(row[25] != null ? row[25].toString() : null);
					mCUNormsValueDTO.setNormParameterTypeDisplayName(row[26] != null ? row[26].toString() : null);
					mCUNormsValueDTO.setUOM(row[27] != null ? row[27].toString() : null);
					//mCUNormsValueDTO.setIsEditable(row[28] != null ? Boolean.valueOf(row[28].toString()) : null);
					Boolean isEditable = null;
					if (row[28] != null) {
						if (row[28] instanceof Boolean) {
							isEditable = (Boolean) row[28];
						} else if (row[28] instanceof Number) {
							isEditable = ((Number) row[28]).intValue() == 1;
						}
					}
					mCUNormsValueDTO.setIsEditable(isEditable);
					
					if(vertical.getName().equalsIgnoreCase("Filament")) {
						if(mCUNormsValueDTO.getNormParameterTypeName().equalsIgnoreCase("Manual Entry")) {
							mCUNormsValueDTO.setIsEditable(true);
						}else {
							mCUNormsValueDTO.setIsEditable(false);
						}
					}
					mCUNormsValueDTO.setProductName(row[29] != null ? row[29].toString() : null);
					if(vertical.getName().equalsIgnoreCase("STAPLE") || vertical.getName().equalsIgnoreCase("Filament")){
					mCUNormsValueDTO.setSapCode(row[30] != null ? row[30].toString() : "");
					}
				} else {
					mCUNormsValueDTO.setMaterialFkId(row[4].toString());

					mCUNormsValueDTO.setApril(row[5] != null ? Double.parseDouble(row[5].toString()) : null);
					mCUNormsValueDTO.setMay(row[6] != null ? Double.parseDouble(row[6].toString()) : null);
					mCUNormsValueDTO.setJune(row[7] != null ? Double.parseDouble(row[7].toString()) : null);
					mCUNormsValueDTO.setJuly(row[8] != null ? Double.parseDouble(row[8].toString()) : null);
					mCUNormsValueDTO.setAugust(row[9] != null ? Double.parseDouble(row[9].toString()) : null);
					mCUNormsValueDTO.setSeptember(row[10] != null ? Double.parseDouble(row[10].toString()) : null);
					mCUNormsValueDTO.setOctober(row[11] != null ? Double.parseDouble(row[11].toString()) : null);
					mCUNormsValueDTO.setNovember(row[12] != null ? Double.parseDouble(row[12].toString()) : null);
					mCUNormsValueDTO.setDecember(row[13] != null ? Double.parseDouble(row[13].toString()) : null);
					mCUNormsValueDTO.setJanuary(row[14] != null ? Double.parseDouble(row[14].toString()) : null);
					mCUNormsValueDTO.setFebruary(row[15] != null ? Double.parseDouble(row[15].toString()) : null);
					mCUNormsValueDTO.setMarch(row[16] != null ? Double.parseDouble(row[16].toString()) : null);

					mCUNormsValueDTO.setFinancialYear(row[17].toString());
					mCUNormsValueDTO.setRemarks(row[18] != null ? row[18].toString() : "");
					mCUNormsValueDTO.setCreatedOn(row[19] != null ? (Date) row[19] : null);
					mCUNormsValueDTO.setModifiedOn(row[20] != null ? (Date) row[20] : null);
					mCUNormsValueDTO.setMcuVersion(row[21] != null ? row[21].toString() : null);
					mCUNormsValueDTO.setUpdatedBy(row[22] != null ? row[22].toString() : null);
					mCUNormsValueDTO.setNormParameterTypeId(row[23] != null ? row[23].toString() : null);
					mCUNormsValueDTO.setNormParameterTypeName(row[24] != null ? row[24].toString() : null);
					mCUNormsValueDTO.setNormParameterTypeDisplayName(row[25] != null ? row[25].toString() : null);
					mCUNormsValueDTO.setUOM(row[26] != null ? row[26].toString() : null);
				//	mCUNormsValueDTO.setIsEditable(row[27] != null ? Boolean.valueOf(row[27].toString()) : null);
			     	Boolean isEditable = null;
                    	if (row[27] != null) {
					if (row[27] instanceof Boolean) {
						isEditable = (Boolean) row[27];
					} else if (row[27] instanceof Number) {
						isEditable = ((Number) row[27]).intValue() == 1;
					}
				}
				mCUNormsValueDTO.setIsEditable(isEditable);
				
					mCUNormsValueDTO.setProductName(row[28] != null ? row[28].toString() : null);
					if(vertical.getName().equalsIgnoreCase("STAPLE") || vertical.getName().equalsIgnoreCase("Filament") || vertical.getName().equalsIgnoreCase("RefineryUtility")){
					mCUNormsValueDTO.setSapCode(row[29] != null ? row[29].toString() : "");
					}
					if(vertical.getName().equalsIgnoreCase("CRUDE") || vertical.getName().equalsIgnoreCase("Coker") || vertical.getName().equalsIgnoreCase("MEROX") || vertical.getName().equalsIgnoreCase("VGOHT") || vertical.getName().equalsIgnoreCase("PCG") || vertical.getName().equalsIgnoreCase("FCC")) {
						mCUNormsValueDTO.setSapCode(row[29] != null ? row[29].toString() : "");
					}
					if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("PTA") || vertical.getName().equalsIgnoreCase("Chemical")) {
						mCUNormsValueDTO.setWtAverage(row[29] != null ? Double.parseDouble(row[29].toString()) : null);	
					}
					if (chemical || ptaPmdPia) {	
						mCUNormsValueDTO.setSapCode(row[30] != null ? row[30].toString() : "");
					}
					if (vertical.getName().equalsIgnoreCase("MEROX")) {
						mCUNormsValueDTO.setLastFy(row[30] != null ? Double.parseDouble(row[30].toString()) : null);	
					}
				}
				mCUNormsValueDTOList.add(mCUNormsValueDTO);
			}
			Map<String, Object> map = new HashMap<>();

			List<AopCalculation> aopCalculation = aopCalculationRepository
					.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "normal-op-norms");
			map.put("mcuNormsValueDTOList", mCUNormsValueDTOList);
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
	public AOPMessageVM getSteadyStateNorms(String year, String plantId, String gradeId, String mode) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    try {
	        List<Object[]> results = getSteadyStateNormsData(plantId, year);
	        List<String> columnNames = getSteadyStateNormsColumns(plantId, year);

	        List<Map<String, Object>> resultList = new ArrayList<>();
	        for (Object[] row : results) {
	            Map<String, Object> rowMap = new LinkedHashMap<>();
	            for (int i = 0; i < columnNames.size(); i++) {
	                String colName = columnNames.get(i);
	                Object value = row[i];

	                
	                if (value == null && ("Remarks".equalsIgnoreCase(colName) || isValidUUID(colName))) {
	                    value = "";
	                }

	                rowMap.put(colName, value);
	            }
	            resultList.add(rowMap);
	        }

	        
	        List<Map<String, Object>> columnMetadata = getSteadyStateNormsColumnMetadata(plantId, year, columnNames);

	        Map<String, Object> data = new HashMap<>();
	        data.put("data", resultList);
	        data.put("columns", columnMetadata);

	        List<AopCalculation> aopCalculation = aopCalculationRepository
	                .findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "normal-op-norms");
	        data.put("aopCalculation", aopCalculation);

	        aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("SP Executed successfully");
	        aopMessageVM.setData(data);
	        return aopMessageVM;

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid UUID format ", e);
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}
	
	public List<Object[]> getSteadyStateNormsData(String plantId, String FinYear) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			String storedProcedure = "[" + vertical.getName() + "_" + site.getName() + "_GetGradewiseSteadyStateNorms]";

			String sql = "EXEC " + storedProcedure
					+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @FinYear = :FinYear";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("siteId", site.getId());
			query.setParameter("verticalId", vertical.getId());
			query.setParameter("FinYear", FinYear);
			
			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<String> getSteadyStateNormsColumns(String plantId, String FinYear) {
		return entityManager.unwrap(Session.class).doReturningWork(connection -> {
			List<String> columnNames = new ArrayList<>();
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

			String storedProcedure = "[" + vertical.getName() + "_" + site.getName() + "_GetGradewiseSteadyStateNorms]";
			String sql = "EXEC " + storedProcedure
					+ " @plantId = ?, @siteId = ?, @verticalId = ?, @FinYear = ?";

			try (PreparedStatement ps = connection.prepareStatement(sql)) {
				ps.setString(1, plantId);
				ps.setString(2, site.getId().toString());
				ps.setString(3, vertical.getId().toString());
				ps.setString(4, FinYear);

				try (ResultSet rs = ps.executeQuery()) {
					ResultSetMetaData rsMetaData = rs.getMetaData();
					for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
						columnNames.add(rsMetaData.getColumnLabel(i));
					}
				}
			}
			return columnNames;
		});
	}

	public List<Map<String, Object>> getSteadyStateNormsColumnMetadata(String plantId, String FinYear, List<String> columnNames) {
	    
	    List<UUID> normParameterUuids = new ArrayList<>();
	    for (String colName : columnNames) {
	        if (isValidUUID(colName)) {
	            normParameterUuids.add(UUID.fromString(colName));
	        }
	    }

	    
	    Map<String, String> displayNameMap = new HashMap<>();
	    if (!normParameterUuids.isEmpty()) {
	        List<NormParameters> normParams = normParametersRepository.findAllById(normParameterUuids);
	        for (NormParameters param : normParams) {
	            displayNameMap.put(param.getId().toString().toLowerCase(), param.getDisplayName());
	        }
	    }

	    
	    return entityManager.unwrap(Session.class).doReturningWork(connection -> {
	        List<Map<String, Object>> columnMetadata = new ArrayList<>();
	        Plants plant = plantsRepository.findById(UUID.fromString(plantId))
	                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
	        Sites site = siteRepository.findById(plant.getSiteFkId())
	                .orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
	                .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

	        String storedProcedure = "[" + vertical.getName() + "_" + site.getName() + "_GetGradewiseSteadyStateNorms]";
	        String sql = "EXEC " + storedProcedure
	                + " @plantId = ?, @siteId = ?, @verticalId = ?, @FinYear = ?";

	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setString(1, plantId);
	            ps.setString(2, site.getId().toString());
	            ps.setString(3, vertical.getId().toString());
	            ps.setString(4, FinYear);

	            try (ResultSet rs = ps.executeQuery()) {
	                ResultSetMetaData rsMetaData = rs.getMetaData();
	                for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
	                    Map<String, Object> columnInfo = new HashMap<>();
	                    String columnName = rsMetaData.getColumnLabel(i);
	                    String columnType = rsMetaData.getColumnTypeName(i);

	                    
	                    String title;
	                    if (isValidUUID(columnName) && displayNameMap.containsKey(columnName.toLowerCase())) {
	                        title = displayNameMap.get(columnName.toLowerCase());
	                    } else {
	                        title = formatTitle(columnName);
	                    }

	                    columnInfo.put("field", columnName);
	                    columnInfo.put("title", title);
	                    columnInfo.put("editable", false);
	                    columnInfo.put("type", getFrontendType(columnType));
	                    columnMetadata.add(columnInfo);
	                }
	            }
	        }
	        return columnMetadata;
	    });
	}
	
	@Override
	public byte[] exportSteadyStateNormsDynamic(String year, String plantId, boolean isAfterSave, List<Map<String, Object>> dynamicData) {
	    try {
	        if (!isAfterSave) {
	            List<Object[]> results = getSteadyStateNormsData(plantId, year);
	            List<String> rawColumnNames = getSteadyStateNormsColumns(plantId, year);

	            dynamicData = new ArrayList<>();
	            for (Object[] row : results) {
	                Map<String, Object> rowMap = new LinkedHashMap<>();
	                for (int i = 0; i < rawColumnNames.size(); i++) {
	                    String colName = rawColumnNames.get(i);
	                    Object value = row[i];
	                    if (value == null && ("Remarks".equalsIgnoreCase(colName) || isValidUUID(colName))) {
	                        value = "";
	                    }
	                    rowMap.put(colName, value);
	                }
	                dynamicData.add(rowMap);
	            }
	        }

	        if (dynamicData == null || dynamicData.isEmpty()) {
	            return null;
	        }

	        List<String> rawHeaders = new ArrayList<>(dynamicData.get(0).keySet());

	        List<Map<String, Object>> metadataList = getSteadyStateNormsColumnMetadata(plantId, year, rawHeaders);

	        Map<String, String> fieldToTitleMap = new HashMap<>();
	        for (Map<String, Object> meta : metadataList) {
	            String field = (String) meta.get("field");
	            String title = (String) meta.get("title");
	            fieldToTitleMap.put(field, title != null ? title : field);
	        }

	        Map<UUID, String> normParamsDisplayNameMap = new HashMap<>();
	        List<UUID> materialFkIds = new ArrayList<>();

	        for (Map<String, Object> row : dynamicData) {
	            Object matFkVal = row.entrySet().stream()
	                    .filter(e -> e.getKey().replaceAll("[_ ]", "").equalsIgnoreCase("MaterialFKId"))
	                    .map(Map.Entry::getValue)
	                    .findFirst()
	                    .orElse(null);

	            if (matFkVal != null && isValidUUID(matFkVal.toString())) {
	                materialFkIds.add(UUID.fromString(matFkVal.toString()));
	            }
	        }

	        if (!materialFkIds.isEmpty()) {
	            List<NormParameters> normParamsList = normParametersRepository.findAllById(materialFkIds);
	            for (NormParameters param : normParamsList) {
	                normParamsDisplayNameMap.put(param.getId(), param.getDisplayName());
	            }
	        }

        List<String> dynamicUuidHeaders = new ArrayList<>();
        String matFkKey = null;
        String normParamTypeKey = null;
        String sapCodeKey = null;
        String uomKey = null;
        String ytdKey = null;
        String wtAvgKey = null;
        String remarksKey = null;
        String isEditableKey = null;

        for (String key : rawHeaders) {
            String sanitizedKey = key.replaceAll("[_ ]", "");
            if (sanitizedKey.equalsIgnoreCase("MaterialFKId")) {
                matFkKey = key;
            } else if (sanitizedKey.equalsIgnoreCase("NormParameterTypeId") || sanitizedKey.equalsIgnoreCase("NormParameterTypeFKId")) {
                normParamTypeKey = key;
            } else if (sanitizedKey.equalsIgnoreCase("SAPMaterialCode")) {
                sapCodeKey = key;
            } else if (sanitizedKey.equalsIgnoreCase("UOM")) {
                uomKey = key;
            } else if (sanitizedKey.equalsIgnoreCase("YTD") || sanitizedKey.equalsIgnoreCase("OverallAOPWtAvg")) {
                ytdKey = key;
            } else if (sanitizedKey.equalsIgnoreCase("WtAvg") || sanitizedKey.equalsIgnoreCase("WtAvg(YTD)")) {
                wtAvgKey = key;
            } else if (sanitizedKey.equalsIgnoreCase("Remarks")) {
                remarksKey = key;
            } else if (sanitizedKey.equalsIgnoreCase("IsEditable")) {
                isEditableKey = key;
            } else if (isValidUUID(key)) {
                dynamicUuidHeaders.add(key);
            }
        }

	        if (normParamTypeKey == null && matFkKey != null) {
	            normParamTypeKey = "NormParameterTypeId";
	        }

        List<String> orderedKeys = new ArrayList<>();
        if (sapCodeKey != null) orderedKeys.add(sapCodeKey);
        orderedKeys.add("PARTICULARS_HEADER"); 
        if (uomKey != null) orderedKeys.add(uomKey);
        if (ytdKey != null) orderedKeys.add(ytdKey);
        orderedKeys.addAll(dynamicUuidHeaders); // Visible dynamic Grade columns
        if (wtAvgKey != null) orderedKeys.add(wtAvgKey);
        if (remarksKey != null) orderedKeys.add(remarksKey);

	        List<String> hiddenKeys = new ArrayList<>();
	        if (matFkKey != null) {
	            hiddenKeys.add(matFkKey);
	        }
	        if (normParamTypeKey != null) {
	            hiddenKeys.add(normParamTypeKey);
	        }
	        if (isEditableKey != null) {
	            hiddenKeys.add(isEditableKey);
	        }

	        int visibleColumnCount = orderedKeys.size();

	        Workbook workbook = new XSSFWorkbook();
	        Sheet sheet = workbook.createSheet("Steady State Norms");
	        sheet.protectSheet("secret_password");

	        CellStyle headerStyle = Utility.createBoldBorderedStyle(workbook);
	        CellStyle lockedStyle = Utility.createLockedStyle(workbook);
	        CellStyle unlockedStyle = Utility.createUnlockedStyle(workbook);

	        
	        Row headerRow = sheet.createRow(0);

	        
	        for (int i = 0; i < visibleColumnCount; i++) {
	            Cell cell = headerRow.createCell(i);
	            String key = orderedKeys.get(i);

            String displayHeader;
            if ("PARTICULARS_HEADER".equals(key)) {
                displayHeader = "Particulars";
            } else if (key != null && key.equals(sapCodeKey)) {
                displayHeader = "SAP MAT Code";
            } else if (key != null && key.equals(ytdKey)) {
                displayHeader = "Overall AOP WtAvg";
            }
			else if (key != null && key.equals(uomKey)) { 
                 displayHeader = "UOM / MT";
			}
			else if (fieldToTitleMap.containsKey(key)) {
                displayHeader = fieldToTitleMap.get(key);
            } 
	
			else {
                displayHeader = formatTitle(key);
            }

	            cell.setCellValue(displayHeader);
	            cell.setCellStyle(headerStyle);
	        }

	        
	        for (int i = 0; i < hiddenKeys.size(); i++) {
	            int colIdx = visibleColumnCount + i;
	            Cell cell = headerRow.createCell(colIdx);
	            cell.setCellValue(hiddenKeys.get(i));
	            cell.setCellStyle(headerStyle);
	        }

	        
	        int rowIdx = 1;

	        for (Map<String, Object> rowData : dynamicData) {
	            Row row = sheet.createRow(rowIdx++);

	            boolean isRowEditable = true;
	            if (isEditableKey != null) {
	                Object editableVal = rowData.get(isEditableKey);
	                if (editableVal != null) {
	                    if (editableVal instanceof Boolean) {
	                        isRowEditable = (Boolean) editableVal;
	                    } else if (editableVal instanceof Number) {
	                        isRowEditable = ((Number) editableVal).intValue() == 1;
	                    } else {
	                        String valStr = editableVal.toString().trim();
	                        isRowEditable = "true".equalsIgnoreCase(valStr) || "1".equals(valStr);
	                    }
	                }
	            }

	            for (int colIdx = 0; colIdx < visibleColumnCount; colIdx++) {
	                String key = orderedKeys.get(colIdx);
	                Cell cell = row.createCell(colIdx);

	                if ("PARTICULARS_HEADER".equals(key)) {
	                    Object matFkVal = matFkKey != null ? rowData.get(matFkKey) : null;
	                    if (matFkVal != null && isValidUUID(matFkVal.toString())) {
	                        UUID uuidKey = UUID.fromString(matFkVal.toString());
	                        cell.setCellValue(normParamsDisplayNameMap.getOrDefault(uuidKey, matFkVal.toString()));
	                    } else {
	                        cell.setCellValue("");
	                    }
	                } else {
	                    Object value = rowData.get(key);

	                    if (value instanceof Number) {
	                        cell.setCellValue(((Number) value).doubleValue());
	                    } else if (value != null) {
	                        cell.setCellValue(value.toString());
	                    } else {
	                        cell.setCellValue("");
	                    }
	                }
	                cell.setCellStyle(isRowEditable ? unlockedStyle : lockedStyle);
	               
	            }

	           
	            for (int i = 0; i < hiddenKeys.size(); i++) {
	                int colIdx = visibleColumnCount + i;
	                String key = hiddenKeys.get(i);
	                Cell cell = row.createCell(colIdx);

	                Object value = rowData.get(key);
	                // Fallback: If NormParameterTypeId value is missing in map, reuse MaterialFKId
	                if (value == null && "NormParameterTypeId".equalsIgnoreCase(key) && matFkKey != null) {
	                    value = rowData.get(matFkKey);
	                }

	                if (value != null) {
	                    cell.setCellValue(value.toString());
	                } else {
	                    cell.setCellValue("");
	                }
	            }
	        }

	       
	        for (int i = 0; i < visibleColumnCount; i++) {
	            sheet.autoSizeColumn(i);
	        }

	        for (int i = 0; i < hiddenKeys.size(); i++) {
	            int colIdx = visibleColumnCount + i;
	            sheet.setColumnHidden(colIdx, true);
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
	
	@Transactional
	@Override
	public AOPMessageVM importSteadyStateNorms(String year, String plantId, MultipartFile file) {
	    try {
	        
	        List<Map<String, Object>> payloadList = readSteadyStateNorms(file.getInputStream(), plantId, year);

	        
	        AOPMessageVM aopMessageVM = updateSteadyStateNorms(plantId, year, payloadList);

	       
	        Map<String, Object> responseData = (Map<String, Object>) aopMessageVM.getData();
	        List<SteadyStateNormDTO> failedDtos = (List<SteadyStateNormDTO>) responseData.get("failedList");

	        
	        List<Map<String, Object>> failedList = regroupFailedPayload(failedDtos, payloadList);

	       
	        if (!failedList.isEmpty()) {
	            byte[] fileByteArray = exportSteadyStateNormsDynamic(year, plantId, true, failedList);
	            String base64File = Base64.getEncoder().encodeToString(fileByteArray);

	            aopMessageVM.setData(base64File);
	            aopMessageVM.setCode(400);
	            aopMessageVM.setMessage("Partial data saved. Please check the downloaded file for errors.");
	        } else {
	            aopMessageVM.setCode(200);
	            aopMessageVM.setMessage("All data has been saved successfully.");
	        }

	        return aopMessageVM;

	    } catch (Exception e) {
	        e.printStackTrace();
	        throw new RuntimeException("Import process failed: " + e.getMessage());
	    }
	}
	
	private List<Map<String, Object>> regroupFailedPayload(List<SteadyStateNormDTO> failedDtos, List<Map<String, Object>> originalPayloadList) {
	    if (failedDtos == null || failedDtos.isEmpty()) {
	        return new ArrayList<>();
	    }

	    
	    Map<String, String> failedMaterialErrors = new HashMap<>();
	    for (SteadyStateNormDTO dto : failedDtos) {
	        if (dto.getMaterialFkId() != null) {
	            String err = dto.getErrDescription() != null ? dto.getErrDescription() : "Validation failed";
	            failedMaterialErrors.put(dto.getMaterialFkId().toLowerCase(), err);
	        }
	    }

	    
	    List<Map<String, Object>> regroupedList = new ArrayList<>();

	    for (Map<String, Object> originalRow : originalPayloadList) {
	        
	        String matFkKey = originalRow.keySet().stream()
	                .filter(k -> k.replaceAll("[_ ]", "").equalsIgnoreCase("MaterialFKId"))
	                .findFirst()
	                .orElse(null);

	        if (matFkKey != null && originalRow.get(matFkKey) != null) {
	            String matFkVal = originalRow.get(matFkValKey(originalRow, matFkKey)).toString().toLowerCase();
	            
	            if (failedMaterialErrors.containsKey(matFkVal)) {
	                Map<String, Object> failedRowMap = new LinkedHashMap<>(originalRow);
	                
	               
	                String remarksKey = originalRow.keySet().stream()
	                        .filter(k -> k.replaceAll("[_ ]", "").equalsIgnoreCase("Remarks"))
	                        .findFirst()
	                        .orElse("Remarks");

	               
	                failedRowMap.put(remarksKey, failedMaterialErrors.get(matFkVal));
	                failedRowMap.put("saveStatus", "Failed");

	                regroupedList.add(failedRowMap);
	            }
	        }
	    }

	    return regroupedList;
	}

	private String matFkValKey(Map<String, Object> map, String matFkKey) {
	    return matFkKey;
	}

	public List<Map<String, Object>> readSteadyStateNorms(InputStream inputStream, String plantId, String year) {
	    List<Map<String, Object>> payloadList = new ArrayList<>();

	    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
	        Sheet sheet = workbook.getSheetAt(0);
	        int totalRows = sheet.getLastRowNum();

	        Row headerRow = sheet.getRow(0);
	        if (headerRow == null) return payloadList;

	        int totalCols = headerRow.getLastCellNum();

	        
	        List<String> rawColumnNames = getSteadyStateNormsColumns(plantId, year);
	        List<Map<String, Object>> metadataList = getSteadyStateNormsColumnMetadata(plantId, year, rawColumnNames);

	        Map<String, String> titleToFieldMap = new HashMap<>();
	        for (Map<String, Object> meta : metadataList) {
	            String field = (String) meta.get("field");
	            String title = (String) meta.get("title");
	            if (title != null && !title.trim().isEmpty()) {
	                titleToFieldMap.put(title.trim().toLowerCase(), field);
	            }
	            if (field != null) {
	                titleToFieldMap.put(field.trim().toLowerCase(), field);
	            }
	        }

	       
	        int materialFkColIndex = -1;
	        int normParamTypeColIndex = -1;
	        int particularsColIndex = 0; // defaults to column 0; updated below when header is found

	        for (int j = 0; j < totalCols; j++) {
	            String headerTitle = getStringCellValue(headerRow.getCell(j));
	            if (headerTitle == null) continue;
	            String sanitized = headerTitle.trim().replaceAll("[_ ]", "");

	            if (sanitized.equalsIgnoreCase("MaterialFKId")) {
	                materialFkColIndex = j;
	            } else if (sanitized.equalsIgnoreCase("NormParameterTypeId") || sanitized.equalsIgnoreCase("NormParameterTypeFKId")) {
	                normParamTypeColIndex = j;
	            } else if (sanitized.equalsIgnoreCase("Particulars")) {
	                particularsColIndex = j;
	            }
	        }

	        UUID plantUuid = (plantId != null && isValidUUID(plantId)) ? UUID.fromString(plantId) : null;

	        
	        for (int i = 1; i <= totalRows; i++) {
	            Row row = sheet.getRow(i);
	            if (row == null) continue;

	            Cell firstCell = row.getCell(particularsColIndex);
	            String firstCellValue = getStringCellValue(firstCell);

	            
	            if (firstCellValue != null && firstCellValue.trim().equalsIgnoreCase("Total")) {
	                continue;
	            }

	            
	            String matFkValStr = materialFkColIndex != -1 ? getStringCellValue(row.getCell(materialFkColIndex)) : null;
	            String normParamTypeValStr = normParamTypeColIndex != -1 ? getStringCellValue(row.getCell(normParamTypeColIndex)) : matFkValStr;

	            UUID normParamTypeFkId = (normParamTypeValStr != null && isValidUUID(normParamTypeValStr))
	                    ? UUID.fromString(normParamTypeValStr)
	                    : null;

	            Map<String, Object> rowData = new LinkedHashMap<>();
	            boolean rowError = false;
	            StringBuilder rowErrorMsg = new StringBuilder("Error at row " + (i + 1) + ": ");

	            try {
	               
	                for (int j = 0; j < totalCols; j++) {
	                    String headerTitle = getStringCellValue(headerRow.getCell(j));
	                    if (headerTitle == null || headerTitle.trim().isEmpty()) {
	                        continue;
	                    }

	                    String trimmedHeader = headerTitle.trim();
	                    String sanitized = trimmedHeader.replaceAll("[_ ]", "");
	                    Cell cell = row.getCell(j);
	                    String mappedKey = null;
	                    Object value = null;

	                   
                    if (sanitized.equalsIgnoreCase("Particulars")) {
                        mappedKey = "PARTICULARS_DISPLAY";
                        value = getStringCellValue(cell);
                    } else if (sanitized.equalsIgnoreCase("SAPMaterialCode") || sanitized.equalsIgnoreCase("SAPMATCode")) {
                        mappedKey = "SAPMaterialCode";
                        value = getStringCellValue(cell);
                    } else if (sanitized.equalsIgnoreCase("UOM")) {
                        mappedKey = "UOM";
                        value = getStringCellValue(cell);
                    } else if (sanitized.equalsIgnoreCase("YTDNorms") || sanitized.equalsIgnoreCase("YTD") || sanitized.equalsIgnoreCase("OverallAOPWtAvg")) {
                        mappedKey = "YTD";
                        value = getStringCellValue(cell);
                    } else if (sanitized.equalsIgnoreCase("WtAvg") || sanitized.equalsIgnoreCase("WtAvg(YTD)")) {
	                        mappedKey = "WtAvg";
	                        value = getNumericCellValue(cell);
	                    } else if (sanitized.equalsIgnoreCase("Remarks")) {
	                        mappedKey = "Remarks";
	                        value = getStringCellValue(cell);
	                    } else if (sanitized.equalsIgnoreCase("MaterialFKId")) {
	                        mappedKey = "Material_FK_Id";
	                        value = getStringCellValue(cell);
	                    } else if (sanitized.equalsIgnoreCase("NormParameterTypeId") || sanitized.equalsIgnoreCase("NormParameterTypeFKId")) {
	                        mappedKey = "NormParameterTypeId";
	                        value = getStringCellValue(cell);
	                    } else {
	                        // Title lookup via metadata map
	                        String mappedField = titleToFieldMap.get(trimmedHeader.toLowerCase());

	                        if (mappedField != null && isValidUUID(mappedField)) {
	                            mappedKey = mappedField; // UUID field from metadata
	                        } else {
	                            // Dynamic Grade Header Title: Search NormParameters by displayName, normParameterTypeFkId, and plantFkId
	                            NormParameters matchedNormParam = null;
	                            if (normParamTypeFkId != null && plantUuid != null) {
	                                matchedNormParam = normParametersRepository
	                                        .findByDisplayNameAndNormParameterTypeFkIdAndPlantFkId(trimmedHeader, normParamTypeFkId, plantUuid);
	                            }

	                            if (matchedNormParam != null && matchedNormParam.getId() != null) {
	                                mappedKey = matchedNormParam.getId().toString();
	                            } else {
	                                mappedKey = trimmedHeader; // Fallback to raw header string
	                            }
	                        }

	                        value = getNumericCellValue(cell);
	                    }

	                    // Numeric validation for grade/weight values
	                    if (value instanceof Number && !isValidUUID(mappedKey) && !mappedKey.equalsIgnoreCase("WtAvg")) {
	                        double numVal = ((Number) value).doubleValue();
	                        if (numVal < 0) {
	                            rowError = true;
	                            rowErrorMsg.append("[").append(trimmedHeader).append("] value cannot be negative. ");
	                        }
	                    }

	                    rowData.put(mappedKey, value);
	                }

	                // Guarantee hidden ID keys exist in output payload map
	                if (!rowData.containsKey("Material_FK_Id") && matFkValStr != null) {
	                    rowData.put("Material_FK_Id", matFkValStr);
	                }
	                if (!rowData.containsKey("NormParameterTypeId") && normParamTypeValStr != null) {
	                    rowData.put("NormParameterTypeId", normParamTypeValStr);
	                }

	                if (rowError) {
	                    rowData.put("saveStatus", "Failed");
	                    rowData.put("errDescription", rowErrorMsg.toString());
	                } else {
	                    rowData.put("saveStatus", "Success");
	                }

	            } catch (Exception e) {
	                rowData.put("saveStatus", "Failed");
	                rowData.put("errDescription", "Error at row " + (i + 1) + ": " + e.getMessage());
	            }

	            payloadList.add(rowData);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return payloadList;
	}
	// Utility Cell Value Helper Methods (Ensure these exist or reuse your project's helper utility)
	private String getStringCellValue(Cell cell) {
	    if (cell == null) return null;

	    String value = "";
	    CellType type = cell.getCellType();

	    if (type == CellType.FORMULA) {
	        type = cell.getCachedFormulaResultType();
	    }

	    if (type == CellType.STRING) {
	        value = cell.getStringCellValue();
	    } else if (type == CellType.NUMERIC) {
	        // Formats numbers properly (e.g. integer IDs won't end up with ".0")
	        DataFormatter formatter = new DataFormatter();
	        value = formatter.formatCellValue(cell);
	    } else if (type == CellType.BOOLEAN) {
	        value = String.valueOf(cell.getBooleanCellValue());
	    }

	    if (value == null || value.trim().isEmpty()) {
	        return null;
	    }

	    return value.trim();
	}

	private Double getNumericCellValue(Cell cell) {
	    if (cell == null) return null;

	    CellType type = cell.getCellType();
	    if (type == CellType.FORMULA) {
	        type = cell.getCachedFormulaResultType();
	    }

	    if (type == CellType.NUMERIC) {
	        return cell.getNumericCellValue();
	    } else if (type == CellType.STRING) {
	        String val = cell.getStringCellValue();
	        if (val == null || val.trim().isEmpty()) {
	            return null; // Blank strings treated as null
	        }
	        try {
	            return Double.parseDouble(val.trim());
	        } catch (NumberFormatException e) {
	            return null;
	        }
	    }

	    return null;
	}
	
	/**
	 * Helper utility to validate whether a column header is a standard UUID string.
	 */
	private boolean isValidUUID(String str) {
	    if (str == null || str.trim().isEmpty()) {
	        return false;
	    }
	    try {
	        UUID.fromString(str);
	        return true;
	    } catch (IllegalArgumentException e) {
	        return false;
	    }
	}	

		private String formatTitle(String columnName) {
			return columnName.replace("_", " ");
		}

		
		private String getFrontendType(String sqlTypeName) {
		    if (sqlTypeName == null) {
		        return "string";
		    }

		    switch (sqlTypeName.toUpperCase()) {
		        case "VARCHAR":
		        case "NVARCHAR":
		        case "CHAR":
		        case "TEXT":
		        case "NTEXT":
		        case "UUID":
		        case "UNIQUEIDENTIFIER":
		            return "string";
		            
		        case "INT":
		        case "TINYINT":
		        case "BIGINT":
		        case "SMALLINT":
		        case "DECIMAL":
		        case "FLOAT":
		        case "DOUBLE":
		        case "NUMERIC":
		        case "REAL":
		            return "number";
		            
		        case "DATE":
		        case "DATETIME":
		        case "DATETIME2":
		        case "TIMESTAMP":
		            return "date";
		            
		        case "BIT":
		        case "BOOLEAN":
		            return "boolean";
		            
		        default:
		            return "string";
		    }
		}

	public List<Object[]> findByYearAndPlantId(String aopYear, UUID plantId, String procedureName) {
		try {

			String sql = "EXEC " + "[" + procedureName + "]" + " @PlantId = :plantId, @FinYear = :aopYear";

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
	
	private double nz(Double val) {
	    return val == null ? 0.0 : val;
	}

	private boolean isMismatch(double a, double b) {
	    return Math.abs(a - b) > 0.0001; // tolerance for double comparison
	}
	
	private double getAOPMonthValue(AOPDTO dto, String month) {
	    switch (month) {
	        case "Apr": return nz(dto.getApril());
	        case "May": return nz(dto.getMay());
	        case "Jun": return nz(dto.getJune());
	        case "Jul": return nz(dto.getJuly());
	        case "Aug": return nz(dto.getAug());
	        case "Sep": return nz(dto.getSep());
	        case "Oct": return nz(dto.getOct());
	        case "Nov": return nz(dto.getNov());
	        case "Dec": return nz(dto.getDec());
	        case "Jan": return nz(dto.getJan());
	        case "Feb": return nz(dto.getFeb());
	        case "Mar": return nz(dto.getMarch());
	        default: return 0;
	    }
	}

	private double getDtoMonthValue(MCUNormsValueDTO dto, String month) {
	    switch (month) {
	        case "Apr": return nz(dto.getApril());
	        case "May": return nz(dto.getMay());
	        case "Jun": return nz(dto.getJune());
	        case "Jul": return nz(dto.getJuly());
	        case "Aug": return nz(dto.getAugust());
	        case "Sep": return nz(dto.getSeptember());
	        case "Oct": return nz(dto.getOctober());
	        case "Nov": return nz(dto.getNovember());
	        case "Dec": return nz(dto.getDecember());
	        case "Jan": return nz(dto.getJanuary());
	        case "Feb": return nz(dto.getFebruary());
	        case "Mar": return nz(dto.getMarch());
	        default: return 0;
	    }
	}
	
	private static final String[] MONTHS = {
		    "Apr","May","Jun","Jul","Aug","Sep",
		    "Oct","Nov","Dec","Jan","Feb","Mar"
		};
	
	private double getGradeMonthValue(MCUNormsValueGrade grade, String month) {

	    switch (month) {

	        case "Apr":
	            return nz(grade.getApril());

	        case "May":
	            return nz(grade.getMay());

	        case "Jun":
	            return nz(grade.getJune());

	        case "Jul":
	            return nz(grade.getJuly());

	        case "Aug":
	            return nz(grade.getAugust());

	        case "Sep":
	            return nz(grade.getSeptember());

	        case "Oct":
	            return nz(grade.getOctober());

	        case "Nov":
	            return nz(grade.getNovember());

	        case "Dec":
	            return nz(grade.getDecember());

	        case "Jan":
	            return nz(grade.getJanuary());

	        case "Feb":
	            return nz(grade.getFebruary());

	        case "Mar":
	            return nz(grade.getMarch());

	        default:
	            return 0.0;
	    }
	}


	@SuppressWarnings("unchecked")
	private boolean validateStapleGradeTotals(UUID plantFKId, String year,
	        List<MCUNormsValueDTO> stapleDtoList,
	        List<ValidationErrorDTO> validationErrorList) throws Exception {

	    boolean allValid = true;
	    Map<UUID, String> materialNames = new HashMap<>();
	    System.out.println("========================================");
	    System.out.println("STAPLE VALIDATION S T A R T | Plant=" + plantFKId + " | Year=" + year);
	    System.out.println("========================================");

	    // ============================
	    // Grade Production Data (AOP)
	    // ============================
	    AOPMessageVM aopResponse =
	            aOPServiceImpl.getAOPData(plantFKId.toString(), year, "Production");

	    Map<String, Object> aopMap = (Map<String, Object>) aopResponse.getData();
	    List<AOPDTO> aopList = (List<AOPDTO>) aopMap.get("aopDTOList");

	    Map<UUID, AOPDTO> gradeAOP = new HashMap<>();
	    for (AOPDTO dto : aopList) {
	        if (dto.getMaterialFKId() != null) {
	            gradeAOP.put(UUID.fromString(dto.getMaterialFKId()), dto);
	        }
	    }
	    System.out.println("[AOP] Loaded production data for " + gradeAOP.size() + " grade(s)");

	    // ============================
	    // UI Edited Grade Map
	    // ============================
	    Map<UUID, MCUNormsValueDTO> uiGradeMap = new HashMap<>();
	    for (MCUNormsValueDTO dto : stapleDtoList) {
	        if (dto.getGradeId() != null) {
	            uiGradeMap.put(UUID.fromString(dto.getGradeId()), dto);
	        }
	    }
	    System.out.println("[UI] Received " + uiGradeMap.size() + " grade value(s) from UI: " + uiGradeMap.keySet());

	    // ============================
	    // Totals
	    // ============================
	    Map<UUID, Map<String, Double>> uiTotals = new HashMap<>();
	    Map<UUID, Map<String, Double>> calculatedTotals = new HashMap<>();

	    // materialId -> month -> UI-entered grade rows (partial ValidationErrorDTO: gradeId + enteredValue set)
	    Map<UUID, Map<String, List<ValidationErrorDTO>>> materialMonthGradeRows = new HashMap<>();

	    // materialId -> month -> gradeId -> production   (ValidationErrorDTO mein production field nahi hai, isliye alag se)
	    Map<UUID, Map<String, Map<UUID, Double>>> materialMonthGradeProduction = new HashMap<>();

	    // ============================
	    // Get Materials from UI
	    // ============================
	    Set<UUID> materialIds = new HashSet<>();
	    for (MCUNormsValueDTO dto : stapleDtoList) {
	        if (dto.getMaterialFkId() != null) {
	            materialIds.add(UUID.fromString(dto.getMaterialFkId()));
	        }
	    }
	    System.out.println("[UI] Materials sent from UI: " + materialIds);

	    // ============================
	    // Material wise Grade Calculation
	    // ============================
	    for (UUID materialId : materialIds) {

	        List<MCUNormsValueGrade> gradeList =
	                mCUNormsValueGradeRepository
	                .findByPlantFkIdAndFinancialYearAndMaterialFkId(plantFKId, year, materialId);

	        System.out.println("---------------------------------------------");
	        System.out.println("[MATERIAL] " + materialId + " | total grades found in DB = " + gradeList.size());

	        Map<String, Double> uiMap = uiTotals.computeIfAbsent(materialId, k -> new LinkedHashMap<>());
	        Map<String, Double> calcMap = calculatedTotals.computeIfAbsent(materialId, k -> new LinkedHashMap<>());
	        Map<String, List<ValidationErrorDTO>> monthRows =
	                materialMonthGradeRows.computeIfAbsent(materialId, k -> new LinkedHashMap<>());
	        Map<String, Map<UUID, Double>> monthProduction =
	                materialMonthGradeProduction.computeIfAbsent(materialId, k -> new LinkedHashMap<>());

	        for (MCUNormsValueGrade grade : gradeList) {

	            UUID gradeId = grade.getGradeFkId();
	            MCUNormsValueDTO uiDto = uiGradeMap.get(gradeId);
	            boolean isFromUI = (uiDto != null);
	            AOPDTO aop = gradeAOP.get(gradeId);

	            if (aop == null) {
	                System.out.println("[SKIP] Grade=" + gradeId + " -> no AOP/production data found, skipping");
	                continue;
	            }

	            for (String month : MONTHS) {

	                double gradeValue = isFromUI
	                        ? getDtoMonthValue(uiDto, month)
	                        : getGradeMonthValue(grade, month);

	                double production = getAOPMonthValue(aop, month);

	                System.out.println("[GRADE] Material=" + materialId
	                        + " Grade=" + gradeId
	                        + " Month=" + month
	                        + " Value(" + (isFromUI ? "UI" : "DB") + ")=" + gradeValue
	                        + " Production=" + production
	                        + " Contribution=" + (gradeValue * production));

	                uiMap.merge(month, gradeValue, Double::sum);
	                calcMap.merge(month, gradeValue * production, Double::sum);

	               
	                if (isFromUI) {
	                    ValidationErrorDTO row = new ValidationErrorDTO();
	                    row.setGradeId(gradeId.toString());
	                    row.setEnteredValue(gradeValue);
	                    monthRows.computeIfAbsent(month, k -> new ArrayList<>()).add(row);

	                    monthProduction.computeIfAbsent(month, k -> new HashMap<>())
	                            .put(gradeId, production);
	                }
	            }
	        }
	    }

	    // ============================
	    // Existing Total Grid
	    // ============================
	    AOPMessageVM totalResponse = getNormalOperationNormsData(year, plantFKId.toString(), null, "");
	    Map<String, Object> totalMap = (Map<String, Object>) totalResponse.getData();
	    List<MCUNormsValueDTO> totalDtoList = (List<MCUNormsValueDTO>) totalMap.get("mcuNormsValueDTOList");

	    Map<UUID, MCUNormsValueDTO> existingByMaterial = new HashMap<>();
	    for (MCUNormsValueDTO dto : totalDtoList) {
	        if (dto.getMaterialFkId() != null) {
	            existingByMaterial.put(UUID.fromString(dto.getMaterialFkId()), dto);
	        }
	    }
	    System.out.println("[EXISTING] Loaded existing total-grid values for " + existingByMaterial.size() + " material(s)");

	    // ============================
	    // Validation
	    // ============================
	    for (UUID materialId : calculatedTotals.keySet()) {

	        MCUNormsValueDTO existing = existingByMaterial.get(materialId);
	        if (existing == null) {
	            System.out.println("[SKIP] Material=" + materialId + " -> no existing total-grid row found");
	            continue;
	        }

	        Map<String, Double> calc = calculatedTotals.get(materialId);
	        Map<String, Double> ui = uiTotals.get(materialId);
	        Map<String, List<ValidationErrorDTO>> monthRows = materialMonthGradeRows.get(materialId);
	        Map<String, Map<UUID, Double>> monthProduction = materialMonthGradeProduction.get(materialId);

	        for (String month : MONTHS) {

	            double uiSum = nz(ui.get(month));
	            double calcSum = nz(calc.get(month));
	            double expected = uiSum == 0 ? 0 : calcSum / uiSum;
	            double actual = getDtoMonthValue(existing, month);

	            System.out.println("[CHECK] Material=" + materialId
	                    + " Month=" + month
	                    + " UI Total=" + uiSum
	                    + " Calc Sum=" + calcSum
	                    + " Expected=" + expected
	                    + " DB(actual)=" + actual);

	            if (isMismatch(expected, actual)) {

	                allValid = false;

	                double difference = Math.abs(expected - actual);
	                for (UUID materialId1 : materialIds) {
	                    Optional<NormParameters> normParamOpt = normParametersRepository.findById(materialId);
	                    if (normParamOpt.isPresent()) {
	                        String name = normParamOpt.get().getDisplayName(); 
	                        materialNames.put(materialId1, name != null ? name : "Unknown");
	                    } else {
	                        materialNames.put(materialId1, "Unknown");
	                    }
	                }
	                String materialName = materialNames.getOrDefault(materialId, "Unknown");

	                List<ValidationErrorDTO> rows = monthRows != null ? monthRows.get(month) : null;
	                Map<UUID, Double> productionMap = monthProduction != null ? monthProduction.get(month) : null;
	                boolean addedGradeWiseRow = false;

	                if (rows != null) {
	                    for (ValidationErrorDTO row : rows) {

	                        UUID gradeId = UUID.fromString(row.getGradeId());
	                        double value = row.getEnteredValue();
	                        double production = (productionMap != null && productionMap.get(gradeId) != null)
	                                ? productionMap.get(gradeId) : 0.0;

	                        double otherUiSum = uiSum - value;
	                        double otherCalcSum = calcSum - (value * production);
	                        double denominator = production - actual;

	                        if (Math.abs(denominator) < 1e-9) {
	                            System.out.println("[SUGGEST] Grade=" + gradeId
	                                    + " Month=" + month
	                                    + " -> unique suggested value nahi nikal sakte (production == target ratio)");
	                            continue;
	                        }

	                        double suggestedValue = (actual * otherUiSum - otherCalcSum) / denominator;

	                        ValidationErrorDTO err = new ValidationErrorDTO();
	                        err.setMaterialName(materialName);
	                        err.setMonth(month);
	                        err.setYear(year);
	                        err.setExpectedValue(expected);
	                        err.setActualValue(actual);
	                        err.setDifference(difference);
	                        err.setGradeId(gradeId.toString());
	                        err.setGradeName(null);
	                        err.setEnteredValue(value);
	                        err.setSuggestedValue(suggestedValue);

	                        validationErrorList.add(err);
	                        addedGradeWiseRow = true;

	                        System.out.println("[SUGGEST] Grade=" + gradeId
	                                + " Month=" + month
	                                + " Entered=" + value
	                                + " -> Suggested=" + suggestedValue);
	                    }
	                }

	           
	                if (!addedGradeWiseRow) {
	                    ValidationErrorDTO err = new ValidationErrorDTO();
	                    err.setMaterialName(materialName);
	                    err.setMonth(month);
	                    err.setYear(year);
	                    err.setExpectedValue(expected);
	                    err.setActualValue(actual);
	                    err.setDifference(difference);
	                    validationErrorList.add(err);
	                }

	                System.out.println("[FAIL] Material=" + materialId
	                        + " Month=" + month
	                        + " UI Total=" + uiSum
	                        + " Calc Sum=" + calcSum
	                        + " Expected=" + expected
	                        + " DB Value=" + actual);
	            }
	        }
	    }

	    System.out.println("========================================");
	    System.out.println("STAPLE VALIDATION END | allValid=" + allValid);
	    System.out.println("========================================");

	    return allValid;
	}
	   private static String toStringOrEmpty(Object[] row, int index) {
	        if (row.length <= index || row[index] == null) {
	            return "";
	        }
	        Object value = row[index];
	        return value.toString();
	    }
	   
	   private static Double toDouble(Object[] row, int index) {
	        if (row.length <= index || row[index] == null) {
	            return 0.0;
	        }
	        Object value = row[index];
	        if (value instanceof Number) {
	            return ((Number) value).doubleValue();
	        }
	        try {
	            return Double.parseDouble(value.toString());
	        } catch (NumberFormatException e) {
	            return 0.0;
	        }
	    }
	   
	   @Transactional
	   @Override
	   public AOPMessageVM updateSteadyStateNorms(String plantId, String year, List<Map<String, Object>> payloadList) {
	       
	       List<SteadyStateNormDTO> dtos = processPayload(payloadList);

	       Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
	       Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).orElseThrow();
	       Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();

	       List<MCUNormsValueGrade> mcuNormsValueGrades = new ArrayList<>();
	       List<SteadyStateNormDTO> failedList = new ArrayList<>();

	       for (SteadyStateNormDTO steadyStateNormDTO : dtos) {
	           
	           if (steadyStateNormDTO.getSaveStatus() != null && steadyStateNormDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
	               failedList.add(steadyStateNormDTO);
	               continue;
	           }

	           UUID materialId = UUID.fromString(steadyStateNormDTO.getMaterialFkId());
	           UUID gradeId = UUID.fromString(steadyStateNormDTO.getGradeId());

	           Optional<MCUNormsValueGrade> opt = mcuNormsValueGradeRepository.findByMaterialGradeAndFinancialYear(
	                   materialId, gradeId, year, UUID.fromString(plantId)
	           );

	           String newRemark = steadyStateNormDTO.getRemarks() != null ? steadyStateNormDTO.getRemarks().trim() : "";

	           if (opt.isPresent()) {
	               MCUNormsValueGrade existing = opt.get();
	               String existingRemark = existing.getRemarks() != null ? existing.getRemarks().trim() : "";

	            //    if (newRemark.isEmpty()) {
	            //        steadyStateNormDTO.setSaveStatus("Failed");
	            //        steadyStateNormDTO.setErrDescription("Remark is mandatory to update an existing record.");
	            //        failedList.add(steadyStateNormDTO);
	            //        continue;
	            //    }

	               boolean isValueChanged = isAnyMonthValueChanged(existing, steadyStateNormDTO);
	               boolean isRemarkChanged = !existingRemark.equalsIgnoreCase(newRemark);

	               if (isValueChanged && !isRemarkChanged) {
	                   steadyStateNormDTO.setSaveStatus("Failed");
	                   steadyStateNormDTO.setErrDescription("Value has changed; please provide an updated remark.");
	                   failedList.add(steadyStateNormDTO);
	                   continue;
	               }

	               if (isValueChanged || isRemarkChanged) {
	                   setMonthlyValues(existing, steadyStateNormDTO);
	                   existing.setRemarks(newRemark);
	                   existing.setModifiedOn(new Date());
	                   existing.setUpdatedBy(Utility.getUserName());
	                   mcuNormsValueGrades.add(existing);
	               }
	           } else {
	               // Check if all monthly values are null in the incoming DTO
	               boolean areAllMonthsNull = steadyStateNormDTO.getApril() == null &&
	                       steadyStateNormDTO.getMay() == null &&
	                       steadyStateNormDTO.getJune() == null &&
	                       steadyStateNormDTO.getJuly() == null &&
	                       steadyStateNormDTO.getAugust() == null &&
	                       steadyStateNormDTO.getSeptember() == null &&
	                       steadyStateNormDTO.getOctober() == null &&
	                       steadyStateNormDTO.getNovember() == null &&
	                       steadyStateNormDTO.getDecember() == null &&
	                       steadyStateNormDTO.getJanuary() == null &&
	                       steadyStateNormDTO.getFebruary() == null &&
	                       steadyStateNormDTO.getMarch() == null;

	               // Skip creation and validation if no monthly data exists for this grade
	               if (areAllMonthsNull) {
	                   continue;
	               }

	               if (newRemark.isEmpty()) {
	                   steadyStateNormDTO.setSaveStatus("Failed");
	                   steadyStateNormDTO.setErrDescription("Remark is mandatory for creating a new record.");
	                   failedList.add(steadyStateNormDTO);
	                   continue;
	               }
	               
	               MCUNormsValueGrade newEntity = new MCUNormsValueGrade();
	               setMonthlyValues(newEntity, steadyStateNormDTO);

	               newEntity.setRemarks(newRemark);
	               newEntity.setCreatedOn(new Date());
	               newEntity.setModifiedOn(new Date());
	               newEntity.setFinancialYear(year);
	               newEntity.setGradeFkId(gradeId);
	               newEntity.setMaterialFkId(materialId);
	               newEntity.setMcuVersion("V1");
	               newEntity.setNormParameterTypeFkId(UUID.fromString(steadyStateNormDTO.getNormParameterTypeFkId()));
	               newEntity.setPlantFkId(UUID.fromString(plantId));
	               newEntity.setSiteFkId(site.getId());
	               newEntity.setUpdatedBy(Utility.getUserName());
	               newEntity.setVerticalFkId(vertical.getId());
	               
	               mcuNormsValueGrades.add(newEntity);
	           }
	       }

	       if (!mcuNormsValueGrades.isEmpty()) {
	           mcuNormsValueGradeRepository.saveAll(mcuNormsValueGrades);

	           List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("normal-op-norms");
	           for (ScreenMapping screenMapping : screenMappingList) {
	               AopCalculation aopCalculation = new AopCalculation();
	               aopCalculation.setAopYear(year);
	               aopCalculation.setIsChanged(true);
	               aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
	               aopCalculation.setPlantId(plant.getId());
	               aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
	               aopCalculationRepository.save(aopCalculation);
	           }
	       }

	       AOPMessageVM aopMessageVM = new AOPMessageVM();
	       aopMessageVM.setCode(failedList.isEmpty() ? 200 : 207); // 207 Multi-Status if partial failure
	       
	       Map<String, Object> responseData = new HashMap<>();
	       responseData.put("updatedList", mcuNormsValueGrades);
	       responseData.put("failedList", failedList);

	       aopMessageVM.setData(responseData);
	       aopMessageVM.setMessage(failedList.isEmpty() ? "Data updated successfully" : "Processed with some validation errors");
	       return aopMessageVM;
	   }	   

	   private void setMonthlyValues(MCUNormsValueGrade entity, SteadyStateNormDTO dto) {
	       entity.setApril(dto.getApril());
	       entity.setMay(dto.getMay());
	       entity.setJune(dto.getJune());
	       entity.setJuly(dto.getJuly());
	       entity.setAugust(dto.getAugust());
	       entity.setSeptember(dto.getSeptember());
	       entity.setOctober(dto.getOctober());
	       entity.setNovember(dto.getNovember());
	       entity.setDecember(dto.getDecember());
	       entity.setJanuary(dto.getJanuary());
	       entity.setFebruary(dto.getFebruary());
	       entity.setMarch(dto.getMarch());
	   }

	   
	   private boolean isAnyMonthValueChanged(MCUNormsValueGrade existing, SteadyStateNormDTO dto) {
	       return isDoubleChanged(existing.getApril(), dto.getApril()) ||
	              isDoubleChanged(existing.getMay(), dto.getMay()) ||
	              isDoubleChanged(existing.getJune(), dto.getJune()) ||
	              isDoubleChanged(existing.getJuly(), dto.getJuly()) ||
	              isDoubleChanged(existing.getAugust(), dto.getAugust()) ||
	              isDoubleChanged(existing.getSeptember(), dto.getSeptember()) ||
	              isDoubleChanged(existing.getOctober(), dto.getOctober()) ||
	              isDoubleChanged(existing.getNovember(), dto.getNovember()) ||
	              isDoubleChanged(existing.getDecember(), dto.getDecember()) ||
	              isDoubleChanged(existing.getJanuary(), dto.getJanuary()) ||
	              isDoubleChanged(existing.getFebruary(), dto.getFebruary()) ||
	              isDoubleChanged(existing.getMarch(), dto.getMarch());
	   }

	   
	//    private boolean isDoubleChanged(Double val1, Double val2) {
	//        if (val1 == null && val2 == null) return false;
	//        if (val1 == null || val2 == null) return true;
	//        return Double.compare(val1, val2) != 0;
	//    }

	private boolean isDoubleChanged(Double val1, Double val2) {
		double value1 = val1 == null ? 0.0 : val1;
		double value2 = val2 == null ? 0.0 : val2;
	
		return Double.compare(value1, value2) != 0;
	}
	   
	   public List<SteadyStateNormDTO> processPayload(List<Map<String, Object>> payloadList) {
		    List<SteadyStateNormDTO> dtoList = new ArrayList<>();

		    // Set of metadata keys to ignore when picking Grade UUID columns
		    Set<String> knownFixedKeys = Set.of(
		        "Site_FK_Id", "Plant_FK_ID", "Vertical_FK_Id", "Material_FK_Id", "MaterialFKId",
		        "FinancialYear", "Remarks", "CreatedOn", "ModifiedOn", "MCUVersion",
		        "UpdatedBy", "NormParameterTypeId", "NormParameterTypeName",
		        "NormParameterTypeDisplayName", "UOM", "IsEditable", "ProductName",
		        "SAPMaterialCode", "NormParameterDisplayOrder", "WtAvg",
		        "PARTICULARS_DISPLAY", "saveStatus", "errDescription"
		    );

		    for (Map<String, Object> map : payloadList) {
		        // Extract Material_FK_Id (fallback to NormParameterTypeId if necessary)
		        String materialFkId = (String) map.get("Material_FK_Id");
		        if (materialFkId == null) {
		            materialFkId = (String) map.get("MaterialFKId");
		        }
		        
		        String normParameterTypeId = (String) map.get("NormParameterTypeId");
		        if (normParameterTypeId == null) {
		            normParameterTypeId = materialFkId; // Fallback to Material_FK_Id
		        }
		        
		        String remarks = (String) map.get("Remarks");
		        String saveStatus = (String) map.get("saveStatus");
		        String errDescription = (String) map.get("errDescription");

		        for (Map.Entry<String, Object> entry : map.entrySet()) {
		            String key = entry.getKey();
		            Object value = entry.getValue();

		            // Only pick dynamic grade columns whose keys are valid UUIDs
		            if (!knownFixedKeys.contains(key) && isValidUUID(key)) {
		                Double gradeValue = null;
		                if (value instanceof Number) {
		                    gradeValue = ((Number) value).doubleValue();
		                }

		                SteadyStateNormDTO dto = SteadyStateNormDTO.builder()
		                        .materialFkId(materialFkId)
		                        .normParameterTypeFkId(normParameterTypeId)
		                        .remarks(remarks)
		                        .gradeId(key) // Key is the Grade_FK_Id UUID
		                        .april(gradeValue) // Or map monthly values accordingly
		                        .saveStatus(saveStatus)
		                        .errDescription(errDescription)
		                        .build();

		                dtoList.add(dto);
		            }
		        }
		    }

		    return dtoList;
		}
	   
	@Override
	public AOPMessageVM saveNormalOperationNormsDataPolyester(List<MCUNormsValueDTO> mCUNormsValueDTOList,
	        UUID plantFKId, String year, String gradeId, boolean isFromExcel) {

	    try {

	        List<NormsTransactions> transactionsToSave = new ArrayList<>();
	        List<MCUNormsValueDTO> failedList = new ArrayList<>();
	        List<ValidationErrorDTO> gradeValidationErrors = new ArrayList<>(); 

	        Plants plant = plantsRepository.findById(plantFKId).get();
	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
	        Sites site = siteRepository.findById(plant.getSiteFkId()).get();
	        boolean pvc = vertical.getName().equalsIgnoreCase("PVC") && (site.getName().equalsIgnoreCase("VMD") || site.getName().equalsIgnoreCase("DMD") || site.getName().equalsIgnoreCase("HMD"));
	        boolean elastomerJMDIIR = vertical.getName().equalsIgnoreCase("Elastomer") && site.getName().equalsIgnoreCase("JMD") && plant.getName().equalsIgnoreCase("IIR");
	        boolean isStapleWithGrade = vertical.getName().equalsIgnoreCase("STAPLE") && gradeId != null && !gradeId.trim().isEmpty();
	        boolean isFilamentWithGrade = vertical.getName().equalsIgnoreCase("Filament") && gradeId != null && !gradeId.trim().isEmpty();
	        boolean aromaticPmd = vertical.getName().equalsIgnoreCase("AROMATICS") && site.getName().equalsIgnoreCase("PMD");
	        boolean aromaticSEZ = vertical.getName().equalsIgnoreCase("AROMATICS") && site.getName().equalsIgnoreCase("SEZ");

	      
	       
	        if (isStapleWithGrade) {
	        	
	        	  String procedureName = vertical.getName() + "_" + site.getName() + "_GradeValidation";

	        	  String sql = "EXEC " + "[" + procedureName + "]" +
	        	            " @plantId = :plantId," +
	        	            " @siteId = :siteId," +
	        	            " @verticalId = :verticalId," +
	        	            " @finYear = :finYear," +
	        	            " @GradeInputJson = :gradeInputJson";

	        	    // Convert DTO List to JSON
	        	    ObjectMapper objectMapper = new ObjectMapper();
	        	    String gradeInputJson = objectMapper.writeValueAsString(mCUNormsValueDTOList);

	        	    Query query = entityManager.createNativeQuery(sql);
	        	    
	        	    query.setParameter("plantId", plant.getId());
	        	    query.setParameter("siteId", site.getId());
	        	    query.setParameter("verticalId", vertical.getId());
	        	    query.setParameter("finYear", year);
	        	    query.setParameter("gradeInputJson", gradeInputJson);

	        	
	          

	              @SuppressWarnings("unchecked")
	              List<Object[]> results = query.getResultList();
	              
					List<ValidationErrorDTO> list = new ArrayList<>();

					for (Object[] row : results) {

						ValidationErrorDTO dto = new ValidationErrorDTO();

						dto.setMaterialTypeId(toStringOrEmpty(row, 0));
						dto.setMaterialTypeName(toStringOrEmpty(row, 1));
						dto.setMaterialId(toStringOrEmpty(row, 2));
						dto.setMaterialName(toStringOrEmpty(row, 3));
						dto.setUom(toStringOrEmpty(row, 4));

						dto.setMonth(toStringOrEmpty(row, 5));
						dto.setYear(year);

						dto.setExpectedValue(toDouble(row, 6)); // WeightedValue
						dto.setActualValue(toDouble(row, 7)); // MCUNormValue
						dto.setDifference(toDouble(row, 8));
						dto.setMatchStatus(toStringOrEmpty(row, 9));

// Optional
						dto.setEnteredValue(toDouble(row, 7));
						dto.setSuggestedValue(0);

						list.add(dto);

					}

//	            boolean allValid = validateStapleGradeTotals(plantFKId, year, mCUNormsValueDTOList, gradeValidationErrors);

//	            System.out.println("[VALIDATION] allValid=" + allValid + " | Errors=" + gradeValidationErrors.size());

	            if (!list.isEmpty()) {
	                System.out.println("[STOP] Grade validation failed -> save aborted.");
	                return AOPMessageVM.builder()
	                        .code(400)
	                        .message("Validation Failed")
	                        .data(list)   
	                        .build();
	            }
	        }

	        // ============================
	        // STEP 2: Per-DTO save logic 
	        // ============================
	        for (MCUNormsValueDTO dto : mCUNormsValueDTOList) {
	            System.out.println(dto.getProductName());
	            Boolean changed = false;
	            if (dto.getSaveStatus() != null && dto.getSaveStatus().equalsIgnoreCase("Failed")) {
	                failedList.add(dto);
	                continue;
	            }
	            if (isStapleWithGrade) {

	            
	                Optional<MCUNormsValueGrade> optionalValue = mcuNormsValueGradeRepository
	                        .findById(UUID.fromString(dto.getId()));

	                if (optionalValue.isEmpty()) {
	                    dto.setErrDescription("No record found with this id" + dto.getId());
	                    dto.setSaveStatus("Failed");
	                    failedList.add(dto);
	                    continue; 
	                }

	                MCUNormsValueGrade value = optionalValue.get();
	                Optional<NormParameters> normParametersOpt = normParametersRepository
	                        .findById(value.getMaterialFkId());
	                if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
	                    continue;
	                }

	                for (int month = 1; month <= 12; month++) {
	                    Double oldVal = getMonthlyValue(value, month);
	                    Double newVal = getMonthlyValue(dto, month);

	                    if (newVal != null && !Objects.equals(oldVal, newVal)
	                            && Objects.equals(value.getRemarks(), dto.getRemarks())) {

	                        dto.setErrDescription("Please add/update remark");
	                        dto.setSaveStatus("Failed");
	                        failedList.add(dto);
	                        break;
	                    }
	                    if (newVal != null && !Objects.equals(oldVal, newVal)) {
	                        NormsTransactions normsTransactions = new NormsTransactions();
	                        normsTransactions.setAopMonth(month);
	                        normsTransactions.setAopYear(value.getFinancialYear());
	                        normsTransactions.setAttributeValue(newVal != null ? newVal.doubleValue() : null);
	                        normsTransactions.setNormParameterFkId(value.getMaterialFkId());
	                        normsTransactions.setPlantFkId(plantFKId);
	                        normsTransactions.setRemark(dto.getRemarks());
	                        normsTransactions.setVersion(1);
	                        normsTransactions.setCreatedDateTime(new Date());
	                        normsTransactions.setCreatedBy(Utility.getUserName());
	                        normsTransactions.setMcuNormsValueFkId((UUID.fromString(dto.getId())));
	                        transactionsToSave.add(normsTransactions);
	                    }
	                }

	            } else if (gradeId != null && !elastomerJMDIIR) {
	                Optional<MCUNormsValueGrade> optionalValue = mcuNormsValueGradeRepository
	                        .findById(UUID.fromString(dto.getId()));

	                if (optionalValue.isEmpty()) {
	                    dto.setErrDescription("No record found with this id" + dto.getId());
	                    dto.setSaveStatus("Failed");
	                    failedList.add(dto);
	                    continue; // or handle accordingly
	                }

	                MCUNormsValueGrade value = optionalValue.get();
	                Optional<NormParameters> normParametersOpt = normParametersRepository
	                        .findById(value.getMaterialFkId());
	                if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
	                    continue;
	                }

	                for (int month = 1; month <= 12; month++) {
	                    Double oldVal = getMonthlyValue(value, month);
	                    Double newVal = getMonthlyValue(dto, month);

	                    if (newVal != null && !Objects.equals(oldVal, newVal)
	                            && Objects.equals(value.getRemarks(), dto.getRemarks())) {

	                        dto.setErrDescription("Please add/update remark");
	                        dto.setSaveStatus("Failed");
	                        failedList.add(dto);
	                        break;
	                    }
	                    if (newVal != null && !Objects.equals(oldVal, newVal)) {
	                        NormsTransactions normsTransactions = new NormsTransactions();
	                        normsTransactions.setAopMonth(month);
	                        normsTransactions.setAopYear(value.getFinancialYear());
	                        normsTransactions.setAttributeValue(newVal != null ? newVal.doubleValue() : null);
	                        normsTransactions.setNormParameterFkId(value.getMaterialFkId());
	                        normsTransactions.setPlantFkId(plantFKId);
	                        normsTransactions.setRemark(dto.getRemarks());
	                        normsTransactions.setVersion(1);
	                        normsTransactions.setCreatedDateTime(new Date());
	                        normsTransactions.setCreatedBy(Utility.getUserName());
	                        normsTransactions.setMcuNormsValueFkId((UUID.fromString(dto.getId())));
	                        transactionsToSave.add(normsTransactions);
	                    }
	                }

	            } else {

	                if (vertical.getName().equalsIgnoreCase("Elastomer") && site.getName().equalsIgnoreCase("JMD") && plant.getName().equalsIgnoreCase("HIIR")) {

	                    Optional<MCUNormsValueGrade> optionalValue = mcuNormsValueGradeRepository
	                            .findById(UUID.fromString(dto.getId()));

	                    if (optionalValue.isEmpty()) {
	                        dto.setErrDescription("No record found with this id" + dto.getId());
	                        dto.setSaveStatus("Failed");
	                        failedList.add(dto);
	                        continue; 
	                    }

	                    MCUNormsValueGrade value = optionalValue.get();
	                    Optional<NormParameters> normParametersOpt = normParametersRepository
	                            .findById(value.getMaterialFkId());
	                    if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
	                        continue;
	                    }

	                    for (int month = 1; month <= 12; month++) {
	                        Double oldVal = getMonthlyValue(value, month);
	                        Double newVal = getMonthlyValue(dto, month);

	                        Double normalizedNewVal = Optional.ofNullable(newVal).orElse(0.0);

	                        if (newVal != null && !Objects.equals(oldVal, newVal)) {
	                            NormsTransactions normsTransactions = new NormsTransactions();
	                            normsTransactions.setAopMonth(month);
	                            normsTransactions.setAopYear(value.getFinancialYear());
	                            normsTransactions.setAttributeValue(newVal != null ? newVal.doubleValue() : null);
	                            normsTransactions.setNormParameterFkId(value.getMaterialFkId());
	                            normsTransactions.setPlantFkId(plantFKId);
	                            normsTransactions.setRemark(dto.getRemarks());
	                            normsTransactions.setVersion(1);
	                            normsTransactions.setCreatedDateTime(new Date());
	                            normsTransactions.setCreatedBy(Utility.getUserName());
	                            normsTransactions.setMcuNormsValueFkId((UUID.fromString(dto.getId())));
	                            transactionsToSave.add(normsTransactions);
	                        }
	                    }

	                } else if (vertical.getName().equalsIgnoreCase("Elastomer") && site.getName().equalsIgnoreCase("JMD") && plant.getName().equalsIgnoreCase("IIR")) {

	                    Optional<MCUNormsValue> optionalValue = mcuNormsValueRepository.findById(UUID.fromString(dto.getId()));

	                    if (optionalValue.isEmpty()) {
	                        dto.setErrDescription("No record found with this id" + dto.getId());
	                        dto.setSaveStatus("Failed");
	                        failedList.add(dto);
	                        continue; 
	                    }

	                    MCUNormsValue value = optionalValue.get();
	                    Optional<NormParameters> normParametersOpt = normParametersRepository
	                            .findById(value.getMaterialFkId());
	                    if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
	                        continue;
	                    }

	                    for (int month = 1; month <= 12; month++) {
	                        Double oldVal = getMonthlyValue(value, month);
	                        Double newVal = getMonthlyValue(dto, month);

	                        if (newVal != null && !Objects.equals(oldVal, newVal)) {
	                            NormsTransactions normsTransactions = new NormsTransactions();
	                            normsTransactions.setAopMonth(month);
	                            normsTransactions.setAopYear(value.getFinancialYear());
	                            normsTransactions.setAttributeValue(newVal != null ? newVal.doubleValue() : null);
	                            normsTransactions.setNormParameterFkId(value.getMaterialFkId());
	                            normsTransactions.setPlantFkId(plantFKId);
	                            normsTransactions.setRemark(dto.getRemarks());
	                            normsTransactions.setVersion(1);
	                            normsTransactions.setCreatedDateTime(new Date());
	                            normsTransactions.setCreatedBy(Utility.getUserName());
	                            normsTransactions.setMcuNormsValueFkId((UUID.fromString(dto.getId())));
	                            transactionsToSave.add(normsTransactions);
	                        }
	                    }

	                } else {

	                    Optional<MCUNormsValue> optionalValue = normalOperationNormsRepository
	                            .findById(UUID.fromString(dto.getId()));

	                    if (optionalValue.isEmpty()) {
	                        dto.setErrDescription("No record found with this id" + dto.getId());
	                        dto.setSaveStatus("Failed");
	                        failedList.add(dto);
	                        continue; // or handle accordingly
	                    }

	                    MCUNormsValue value = optionalValue.get();
	                    Optional<NormParameters> normParametersOpt = normParametersRepository
	                            .findById(value.getMaterialFkId());
	                    if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
	                        continue;
	                    }

	                    for (int month = 1; month <= 12; month++) {
	                        Double oldVal = getMonthlyValue(value, month);
	                        Double newVal = getMonthlyValue(dto, month);

	                        Double normalizedNewVal = Optional.ofNullable(newVal).orElse(0.0);

	                        if (newVal != null && !Objects.equals(oldVal, newVal)) {
	                            NormsTransactions normsTransactions = new NormsTransactions();
	                            normsTransactions.setAopMonth(month);
	                            normsTransactions.setAopYear(value.getFinancialYear());
	                            normsTransactions.setAttributeValue(newVal != null ? newVal.doubleValue() : null);
	                            normsTransactions.setNormParameterFkId(value.getMaterialFkId());
	                            normsTransactions.setPlantFkId(plantFKId);
	                            normsTransactions.setRemark(dto.getRemarks());
	                            normsTransactions.setVersion(1);
	                            normsTransactions.setCreatedDateTime(new Date());
	                            normsTransactions.setCreatedBy(Utility.getUserName());
	                            normsTransactions.setMcuNormsValueFkId((UUID.fromString(dto.getId())));
	                            transactionsToSave.add(normsTransactions);
	                        }
	                    }
	                }
	            }
	        }

	        normsTransactionRepository.saveAll(transactionsToSave);

	        for (MCUNormsValueDTO mCUNormsValueDTO : mCUNormsValueDTOList) {
	            if (mCUNormsValueDTO.getSaveStatus() != null
	                    && mCUNormsValueDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
	                if (!failedList.contains(mCUNormsValueDTO))
	                    failedList.add(mCUNormsValueDTO);
	                continue;
	            }

	            year = mCUNormsValueDTO.getFinancialYear();
	            MCUNormsValue mCUNormsValue = new MCUNormsValue();
	            MCUNormsValueGrade mCUNormsValueGrade = new MCUNormsValueGrade();

	            if (mCUNormsValueDTO.getId() != null || !mCUNormsValueDTO.getId().isEmpty()) {

	                if (isFilamentWithGrade || isStapleWithGrade || vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")
	                        || vertical.getName().equalsIgnoreCase("PET") || pvc) {

	                    Optional<MCUNormsValueGrade> optionalNormsValue = mcuNormsValueGradeRepository
	                            .findById(UUID.fromString(mCUNormsValueDTO.getId()));
	                    if (optionalNormsValue.isPresent()) {
	                        mCUNormsValueGrade = optionalNormsValue.get();
	                        if (mCUNormsValueGrade.getMaterialFkId() != null) {
	                            Optional<NormParameters> normParametersOpt = normParametersRepository
	                                    .findById(mCUNormsValueGrade.getMaterialFkId());
	                            if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
	                                continue;
	                            }
	                        }

	                        mCUNormsValueGrade.setId(UUID.fromString(mCUNormsValueDTO.getId()));
	                        mCUNormsValueGrade.setModifiedOn(new Date());
	                        boolean changed = false;

	                        double newJan = Optional.ofNullable(mCUNormsValueDTO.getJanuary()).orElse(0.0);
	                        double oldJan = Optional.ofNullable(mCUNormsValueGrade.getJanuary()).orElse(0.0);
	                        if (isDifferent(oldJan, newJan)) { mCUNormsValueGrade.setJanuary(newJan); changed = true; }

	                        double newFeb = Optional.ofNullable(mCUNormsValueDTO.getFebruary()).orElse(0.0);
	                        double oldFeb = Optional.ofNullable(mCUNormsValueGrade.getFebruary()).orElse(0.0);
	                        if (isDifferent(oldFeb, newFeb)) { mCUNormsValueGrade.setFebruary(newFeb); changed = true; }

	                        double newMar = Optional.ofNullable(mCUNormsValueDTO.getMarch()).orElse(0.0);
	                        double oldMar = Optional.ofNullable(mCUNormsValueGrade.getMarch()).orElse(0.0);
	                        if (isDifferent(oldMar, newMar)) { mCUNormsValueGrade.setMarch(newMar); changed = true; }

	                        double newApr = Optional.ofNullable(mCUNormsValueDTO.getApril()).orElse(0.0);
	                        double oldApr = Optional.ofNullable(mCUNormsValueGrade.getApril()).orElse(0.0);
	                        if (isDifferent(oldApr, newApr)) { mCUNormsValueGrade.setApril(newApr); changed = true; }

	                        double newMay = Optional.ofNullable(mCUNormsValueDTO.getMay()).orElse(0.0);
	                        double oldMay = Optional.ofNullable(mCUNormsValueGrade.getMay()).orElse(0.0);
	                        if (isDifferent(oldMay, newMay)) { mCUNormsValueGrade.setMay(newMay); changed = true; }

	                        double newJun = Optional.ofNullable(mCUNormsValueDTO.getJune()).orElse(0.0);
	                        double oldJun = Optional.ofNullable(mCUNormsValueGrade.getJune()).orElse(0.0);
	                        if (isDifferent(oldJun, newJun)) { mCUNormsValueGrade.setJune(newJun); changed = true; }

	                        double newJul = Optional.ofNullable(mCUNormsValueDTO.getJuly()).orElse(0.0);
	                        double oldJul = Optional.ofNullable(mCUNormsValueGrade.getJuly()).orElse(0.0);
	                        if (isDifferent(oldJul, newJul)) { mCUNormsValueGrade.setJuly(newJul); changed = true; }

	                        double newAug = Optional.ofNullable(mCUNormsValueDTO.getAugust()).orElse(0.0);
	                        double oldAug = Optional.ofNullable(mCUNormsValueGrade.getAugust()).orElse(0.0);
	                        if (isDifferent(oldAug, newAug)) { mCUNormsValueGrade.setAugust(newAug); changed = true; }

	                        double newSep = Optional.ofNullable(mCUNormsValueDTO.getSeptember()).orElse(0.0);
	                        double oldSep = Optional.ofNullable(mCUNormsValueGrade.getSeptember()).orElse(0.0);
	                        if (isDifferent(oldSep, newSep)) { mCUNormsValueGrade.setSeptember(newSep); changed = true; }

	                        double newOct = Optional.ofNullable(mCUNormsValueDTO.getOctober()).orElse(0.0);
	                        double oldOct = Optional.ofNullable(mCUNormsValueGrade.getOctober()).orElse(0.0);
	                        if (isDifferent(oldOct, newOct)) { mCUNormsValueGrade.setOctober(newOct); changed = true; }

	                        double newNov = Optional.ofNullable(mCUNormsValueDTO.getNovember()).orElse(0.0);
	                        double oldNov = Optional.ofNullable(mCUNormsValueGrade.getNovember()).orElse(0.0);
	                        if (isDifferent(oldNov, newNov)) { mCUNormsValueGrade.setNovember(newNov); changed = true; }

	                        double newDec = Optional.ofNullable(mCUNormsValueDTO.getDecember()).orElse(0.0);
	                        double oldDec = Optional.ofNullable(mCUNormsValueGrade.getDecember()).orElse(0.0);
	                        if (isDifferent(oldDec, newDec)) { mCUNormsValueGrade.setDecember(newDec); changed = true; }

	                        if (!isFromExcel) {
	                            if (mCUNormsValueDTO.getSiteFkId() != null) {
	                                mCUNormsValueGrade.setSiteFkId(UUID.fromString(mCUNormsValueDTO.getSiteFkId()));
	                            }
	                            if (plantFKId != null) {
	                                mCUNormsValueGrade.setPlantFkId(plantFKId);
	                            }
	                            if (mCUNormsValueDTO.getVerticalFkId() != null) {
	                                mCUNormsValueGrade.setVerticalFkId(UUID.fromString(mCUNormsValueDTO.getVerticalFkId()));
	                            }
	                            if (mCUNormsValueDTO.getMaterialFkId() != null) {
	                                mCUNormsValueGrade.setMaterialFkId(UUID.fromString(mCUNormsValueDTO.getMaterialFkId()));
	                            }
	                            if (mCUNormsValueDTO.getNormParameterTypeId() != null) {
	                                mCUNormsValueGrade.setNormParameterTypeFkId(
	                                        UUID.fromString(mCUNormsValueDTO.getNormParameterTypeId()));
	                            }
	                            mCUNormsValueGrade.setFinancialYear(mCUNormsValueDTO.getFinancialYear());
	                        }

	                        mCUNormsValueGrade.setMcuVersion("V1");
	                        mCUNormsValueGrade.setUpdatedBy(Utility.getUserName());
	                        mCUNormsValueGrade.setModifiedOn(new Date());
	                        mCUNormsValueGrade.setGradeFkId(UUID.fromString(mCUNormsValueDTO.getGradeId()));
	                        System.out.println("Data Saved Succussfully" + mCUNormsValue);
	                        if (changed
	                                && Objects.equals(mCUNormsValueGrade.getRemarks(), mCUNormsValueDTO.getRemarks())) {
	                            mCUNormsValueDTO.setErrDescription("Please add/update remark");
	                            mCUNormsValueDTO.setSaveStatus("Failed");
	                            failedList.add(mCUNormsValueDTO);
	                            continue;
	                        }
	                        mCUNormsValueGrade.setRemarks(mCUNormsValueDTO.getRemarks());
	                        mcuNormsValueGradeRepository.save(mCUNormsValueGrade);

	                    } else {
	                        if (isFromExcel) {
	                            mCUNormsValueDTO.setSaveStatus("Failed");
	                            mCUNormsValueDTO.setErrDescription("Invalid Id. Record not found.");
	                            failedList.add(mCUNormsValueDTO);
	                            continue;
	                        }
	                    }

	                } else {
	                    if (vertical.getName().equalsIgnoreCase("Elastomer") && site.getName().equalsIgnoreCase("JMD") && plant.getName().equalsIgnoreCase("HIIR")) {
	                        updateMCUNormsValueGrade(mCUNormsValueDTO, plantFKId, isFromExcel, failedList);
	                        continue;
	                    }

	                    Optional<MCUNormsValue> normsValue = normalOperationNormsRepository
	                            .findById(UUID.fromString(mCUNormsValueDTO.getId()));

	                    if (normsValue.isPresent()) {
	                        mCUNormsValue = normsValue.get();
	                        entityManager.detach(mCUNormsValue);
	                        if (mCUNormsValue.getMaterialFkId() != null) {
	                            Optional<NormParameters> normParametersOpt = normParametersRepository
	                                    .findById(mCUNormsValue.getMaterialFkId());
	                            if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
	                                continue;
	                            }
	                        }

	                        mCUNormsValue.setId(UUID.fromString(mCUNormsValueDTO.getId()));
	                        mCUNormsValue.setModifiedOn(new Date());
	                        boolean changed = false;

	                        double newJan = Optional.ofNullable(mCUNormsValueDTO.getJanuary()).orElse(0.0);
	                        double oldJan = Optional.ofNullable(mCUNormsValue.getJanuary()).orElse(0.0);
	                        if (isDifferent(oldJan, newJan)) { mCUNormsValue.setJanuary(newJan); changed = true; }

	                        double newFeb = Optional.ofNullable(mCUNormsValueDTO.getFebruary()).orElse(0.0);
	                        double oldFeb = Optional.ofNullable(mCUNormsValue.getFebruary()).orElse(0.0);
	                        if (isDifferent(oldFeb, newFeb)) { mCUNormsValue.setFebruary(newFeb); changed = true; }

	                        double newMar = Optional.ofNullable(mCUNormsValueDTO.getMarch()).orElse(0.0);
	                        double oldMar = Optional.ofNullable(mCUNormsValue.getMarch()).orElse(0.0);
	                        if (isDifferent(oldMar, newMar)) { mCUNormsValue.setMarch(newMar); changed = true; }

	                        double newApr = Optional.ofNullable(mCUNormsValueDTO.getApril()).orElse(0.0);
	                        double oldApr = Optional.ofNullable(mCUNormsValue.getApril()).orElse(0.0);
	                        if (isDifferent(oldApr, newApr)) { mCUNormsValue.setApril(newApr); changed = true; }

	                        double newMay = Optional.ofNullable(mCUNormsValueDTO.getMay()).orElse(0.0);
	                        double oldMay = Optional.ofNullable(mCUNormsValue.getMay()).orElse(0.0);
	                        if (isDifferent(oldMay, newMay)) { mCUNormsValue.setMay(newMay); changed = true; }

	                        double newJun = Optional.ofNullable(mCUNormsValueDTO.getJune()).orElse(0.0);
	                        double oldJun = Optional.ofNullable(mCUNormsValue.getJune()).orElse(0.0);
	                        if (isDifferent(oldJun, newJun)) { mCUNormsValue.setJune(newJun); changed = true; }

	                        double newJul = Optional.ofNullable(mCUNormsValueDTO.getJuly()).orElse(0.0);
	                        double oldJul = Optional.ofNullable(mCUNormsValue.getJuly()).orElse(0.0);
	                        if (isDifferent(oldJul, newJul)) { mCUNormsValue.setJuly(newJul); changed = true; }

	                        double newAug = Optional.ofNullable(mCUNormsValueDTO.getAugust()).orElse(0.0);
	                        double oldAug = Optional.ofNullable(mCUNormsValue.getAugust()).orElse(0.0);
	                        if (isDifferent(oldAug, newAug)) { mCUNormsValue.setAugust(newAug); changed = true; }

	                        double newSep = Optional.ofNullable(mCUNormsValueDTO.getSeptember()).orElse(0.0);
	                        double oldSep = Optional.ofNullable(mCUNormsValue.getSeptember()).orElse(0.0);
	                        if (isDifferent(oldSep, newSep)) { mCUNormsValue.setSeptember(newSep); changed = true; }

	                        double newOct = Optional.ofNullable(mCUNormsValueDTO.getOctober()).orElse(0.0);
	                        double oldOct = Optional.ofNullable(mCUNormsValue.getOctober()).orElse(0.0);
	                        if (isDifferent(oldOct, newOct)) { mCUNormsValue.setOctober(newOct); changed = true; }

	                        double newNov = Optional.ofNullable(mCUNormsValueDTO.getNovember()).orElse(0.0);
	                        double oldNov = Optional.ofNullable(mCUNormsValue.getNovember()).orElse(0.0);
	                        if (isDifferent(oldNov, newNov)) { mCUNormsValue.setNovember(newNov); changed = true; }

	                        double newDec = Optional.ofNullable(mCUNormsValueDTO.getDecember()).orElse(0.0);
	                        double oldDec = Optional.ofNullable(mCUNormsValue.getDecember()).orElse(0.0);
	                        if (isDifferent(oldDec, newDec)) { mCUNormsValue.setDecember(newDec); changed = true; }

	                        if (isFromExcel) {
	                            if (mCUNormsValueDTO.getSiteFkId() != null) {
	                                mCUNormsValue.setSiteFkId(UUID.fromString(mCUNormsValueDTO.getSiteFkId()));
	                            }
	                            if (plantFKId != null) {
	                                mCUNormsValue.setPlantFkId(plantFKId);
	                            }
	                            if (mCUNormsValueDTO.getVerticalFkId() != null) {
	                                mCUNormsValue.setVerticalFkId(UUID.fromString(mCUNormsValueDTO.getVerticalFkId()));
	                            }
	                            if (mCUNormsValueDTO.getMaterialFkId() != null) {
	                                mCUNormsValue.setMaterialFkId(UUID.fromString(mCUNormsValueDTO.getMaterialFkId()));
	                            }
	                            if (mCUNormsValueDTO.getNormParameterTypeId() != null) {
	                                mCUNormsValue.setNormParameterTypeFkId(
	                                        UUID.fromString(mCUNormsValueDTO.getNormParameterTypeId()));
	                            }
	                            mCUNormsValue.setFinancialYear(mCUNormsValueDTO.getFinancialYear());
	                        }

	                        mCUNormsValue.setMcuVersion("V1");
	                        mCUNormsValue.setUpdatedBy(Utility.getUserName());
	                        if (changed && Objects.equals(mCUNormsValue.getRemarks(), mCUNormsValueDTO.getRemarks())) {
	                            mCUNormsValueDTO.setErrDescription("Please add/update remark");
	                            mCUNormsValueDTO.setSaveStatus("Failed");
	                            failedList.add(mCUNormsValueDTO);
	                            continue;
	                        }
	                        mCUNormsValue.setRemarks(mCUNormsValueDTO.getRemarks());
	                        System.out.println("Data Saved Succussfully" + mCUNormsValue);

	                        normalOperationNormsRepository.save(mCUNormsValue);
	                    } else {
	                        if (isFromExcel) {
	                            mCUNormsValueDTO.setSaveStatus("Failed");
	                            mCUNormsValueDTO.setErrDescription("Invalid Id. Record not found.");
	                            failedList.add(mCUNormsValueDTO);
	                            continue;
	                        }
	                    }
	                }
	            }
	        }

	        List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("normal-op-norms");
	        for (ScreenMapping screenMapping : screenMappingList) {
	            AopCalculation aopCalculation = new AopCalculation();
	            aopCalculation.setAopYear(year);
	            aopCalculation.setIsChanged(true);
	            aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
	            aopCalculation.setPlantId(plantFKId);
	            aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
	            aopCalculationRepository.save(aopCalculation);
	        }
	        if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("Chemical") || aromaticPmd || aromaticSEZ) {
	            String procedure = vertical.getName() + "_" + site.getName() + "_CalculateTotalFuelNorms";
	            executeProcedure(procedure, plantFKId.toString(), year);
	        }

	        // ============================
	        // STEP 3: Response
	        // ============================
	        if (!failedList.isEmpty()) {
	            return AOPMessageVM.builder()
	                    .code(400)
	                    .message("Some records failed to save")
	                    .data(failedList)
	                    .build();
	        }

	        return AOPMessageVM.builder()
	                .code(200)
	                .message("Data Saved Successfully")
	                .data(mCUNormsValueDTOList)
	                .build();

	    } catch (Exception ex) {
	        return AOPMessageVM.builder()
	                .code(400)
	                .message("Failed to save data: " + ex.getMessage())
	                .data(null)
	                .build();
	    }
	}
	
	@Override
	public List<MCUNormsValueDTO> saveNormalOperationNormsData(List<MCUNormsValueDTO> mCUNormsValueDTOList,
			UUID plantFKId, String year, String gradeId, boolean isFromExcel) {

		try {

			List<NormsTransactions> transactionsToSave = new ArrayList<>();
			List<MCUNormsValueDTO> failedList = new ArrayList<>();
			Plants plant = plantsRepository.findById(plantFKId).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			boolean pvc = vertical.getName().equalsIgnoreCase("PVC") && (site.getName().equalsIgnoreCase("VMD")
					|| site.getName().equalsIgnoreCase("DMD") || site.getName().equalsIgnoreCase("HMD"));
			boolean elastomerJMDIIR = vertical.getName().equalsIgnoreCase("Elastomer")
					&& site.getName().equalsIgnoreCase("JMD") && plant.getName().equalsIgnoreCase("IIR");
			boolean aromaticPmd = vertical.getName().equalsIgnoreCase("AROMATICS")
					&& site.getName().equalsIgnoreCase("PMD");
			boolean aromaticSEZ = vertical.getName().equalsIgnoreCase("AROMATICS")
					&& site.getName().equalsIgnoreCase("SEZ");
			for (MCUNormsValueDTO dto : mCUNormsValueDTOList) {
				System.out.println(dto.getProductName());
				if (dto.getSaveStatus() != null && dto.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(dto);
					continue;
				}
				if (gradeId != null && !elastomerJMDIIR) {
					Optional<MCUNormsValueGrade> optionalValue = mcuNormsValueGradeRepository
							.findById(UUID.fromString(dto.getId()));

					if (optionalValue.isEmpty()) {
						dto.setErrDescription("No record found with this id" + dto.getId());
						dto.setSaveStatus("Failed");
						failedList.add(dto);
						continue; // or handle accordingly
					}

					MCUNormsValueGrade value = optionalValue.get();
					Optional<NormParameters> normParametersOpt = normParametersRepository
							.findById(value.getMaterialFkId());
					if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
						continue;
					}

					for (int month = 1; month <= 12; month++) {
						Double oldVal = getMonthlyValue(value, month);
						Double newVal = getMonthlyValue(dto, month);

						if (newVal != null && !Objects.equals(oldVal, newVal)
								&& Objects.equals(value.getRemarks(), dto.getRemarks())) {

							dto.setErrDescription("Please add/update remark");
							dto.setSaveStatus("Failed");
							failedList.add(dto);
							break;
						}
						if (newVal != null && !Objects.equals(oldVal, newVal)) {
							NormsTransactions normsTransactions = new NormsTransactions();
							normsTransactions.setAopMonth(month);
							normsTransactions.setAopYear(value.getFinancialYear());
							normsTransactions.setAttributeValue(newVal != null ? newVal.doubleValue() : null);
							normsTransactions.setNormParameterFkId(value.getMaterialFkId());
							normsTransactions.setPlantFkId(plantFKId);
							normsTransactions.setRemark(dto.getRemarks());
							normsTransactions.setVersion(1);
							normsTransactions.setCreatedDateTime(new Date());
							normsTransactions.setCreatedBy(Utility.getUserName());
							normsTransactions.setMcuNormsValueFkId((UUID.fromString(dto.getId())));
							transactionsToSave.add(normsTransactions);
						}
					}

				} else {

					if ((vertical.getName().equalsIgnoreCase("Elastomer") && site.getName().equalsIgnoreCase("JMD")
							&& plant.getName().equalsIgnoreCase("HIIR")) ||
							(vertical.getName().equalsIgnoreCase("Elastomer") && site.getName().equalsIgnoreCase("HMD")
									&& plant.getName().equalsIgnoreCase("SBR"))) {

						Optional<MCUNormsValueGrade> optionalValue = mcuNormsValueGradeRepository
								.findById(UUID.fromString(dto.getId()));

						if (optionalValue.isEmpty()) {
							dto.setErrDescription("No record found with this id" + dto.getId());
							dto.setSaveStatus("Failed");
							failedList.add(dto);
							continue; // or handle accordingly
						}

						MCUNormsValueGrade value = optionalValue.get();
						Optional<NormParameters> normParametersOpt = normParametersRepository
								.findById(value.getMaterialFkId());
						if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
							continue;
						}

						for (int month = 1; month <= 12; month++) {
							Double oldVal = getMonthlyValue(value, month);
							Double newVal = getMonthlyValue(dto, month);
							
							if (newVal != null && !Objects.equals(oldVal, newVal)) {
								NormsTransactions normsTransactions = new NormsTransactions();
								normsTransactions.setAopMonth(month);
								normsTransactions.setAopYear(value.getFinancialYear());
								normsTransactions.setAttributeValue(newVal != null ? newVal.doubleValue() : null);
								normsTransactions.setNormParameterFkId(value.getMaterialFkId());
								normsTransactions.setPlantFkId(plantFKId);
								normsTransactions.setRemark(dto.getRemarks());
								normsTransactions.setVersion(1);
								normsTransactions.setCreatedDateTime(new Date());

								normsTransactions.setCreatedBy(Utility.getUserName());
								normsTransactions.setMcuNormsValueFkId((UUID.fromString(dto.getId())));

								transactionsToSave.add(normsTransactions);
							}
						}

					}

					else if (vertical.getName().equalsIgnoreCase("Elastomer") && site.getName().equalsIgnoreCase("JMD")
							&& plant.getName().equalsIgnoreCase("IIR")) {

						Optional<MCUNormsValue> optionalValue = mcuNormsValueRepository
								.findById(UUID.fromString(dto.getId()));

						if (optionalValue.isEmpty()) {
							dto.setErrDescription("No record found with this id" + dto.getId());
							dto.setSaveStatus("Failed");
							failedList.add(dto);
							continue; // or handle accordingly
						}

						MCUNormsValue value = optionalValue.get();
						Optional<NormParameters> normParametersOpt = normParametersRepository
								.findById(value.getMaterialFkId());
						if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
							continue;
						}

						for (int month = 1; month <= 12; month++) {
							Double oldVal = getMonthlyValue(value, month);
							Double newVal = getMonthlyValue(dto, month);

							if (newVal != null && !Objects.equals(oldVal, newVal)) {
								NormsTransactions normsTransactions = new NormsTransactions();
								normsTransactions.setAopMonth(month);
								normsTransactions.setAopYear(value.getFinancialYear());
								normsTransactions.setAttributeValue(newVal != null ? newVal.doubleValue() : null);
								normsTransactions.setNormParameterFkId(value.getMaterialFkId());
								normsTransactions.setPlantFkId(plantFKId);
								normsTransactions.setRemark(dto.getRemarks());
								normsTransactions.setVersion(1);
								normsTransactions.setCreatedDateTime(new Date());

								normsTransactions.setCreatedBy(Utility.getUserName());
								normsTransactions.setMcuNormsValueFkId((UUID.fromString(dto.getId())));

								transactionsToSave.add(normsTransactions);
							}
						}

					} else {

						Optional<MCUNormsValue> optionalValue = normalOperationNormsRepository
								.findById(UUID.fromString(dto.getId()));

						if (optionalValue.isEmpty()) {
							dto.setErrDescription("No record found with this id" + dto.getId());
							dto.setSaveStatus("Failed");
							failedList.add(dto);
							continue; // or handle accordingly
						}

						MCUNormsValue value = optionalValue.get();
						Optional<NormParameters> normParametersOpt = normParametersRepository
								.findById(value.getMaterialFkId());
						if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
							continue;
						}

						for (int month = 1; month <= 12; month++) {
							Double oldVal = getMonthlyValue(value, month);
							Double newVal = getMonthlyValue(dto, month);

							if (newVal != null && !Objects.equals(oldVal, newVal)) {
								NormsTransactions normsTransactions = new NormsTransactions();
								normsTransactions.setAopMonth(month);
								normsTransactions.setAopYear(value.getFinancialYear());
								normsTransactions.setAttributeValue(newVal != null ? newVal.doubleValue() : null);
								normsTransactions.setNormParameterFkId(value.getMaterialFkId());
								normsTransactions.setPlantFkId(plantFKId);
								normsTransactions.setRemark(dto.getRemarks());
								normsTransactions.setVersion(1);
								normsTransactions.setCreatedDateTime(new Date());

								normsTransactions.setCreatedBy(Utility.getUserName());
								normsTransactions.setMcuNormsValueFkId((UUID.fromString(dto.getId())));

								transactionsToSave.add(normsTransactions);
							}
						}

					}
				}
			}

			normsTransactionRepository.saveAll(transactionsToSave);

			for (MCUNormsValueDTO mCUNormsValueDTO : mCUNormsValueDTOList) {
				if (mCUNormsValueDTO.getSaveStatus() != null
						&& mCUNormsValueDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					if (!failedList.contains(mCUNormsValueDTO))
						failedList.add(mCUNormsValueDTO);
					continue;
				}

				year = mCUNormsValueDTO.getFinancialYear();
				MCUNormsValue mCUNormsValue = new MCUNormsValue();
				MCUNormsValueGrade mCUNormsValueGrade = new MCUNormsValueGrade();

				if (mCUNormsValueDTO.getId() != null || !mCUNormsValueDTO.getId().isEmpty()) {

					if (vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")
							|| vertical.getName().equalsIgnoreCase("PET")
							|| (vertical.getName().equalsIgnoreCase("STAPLE") && gradeId != null
									&& !gradeId.trim().isEmpty())
							|| pvc) {

						Optional<MCUNormsValueGrade> optionalNormsValue = mcuNormsValueGradeRepository
								.findById(UUID.fromString(mCUNormsValueDTO.getId()));
						if (optionalNormsValue.isPresent()) {
							mCUNormsValueGrade = optionalNormsValue.get();
							if (mCUNormsValueGrade.getMaterialFkId() != null) {
								Optional<NormParameters> normParametersOpt = normParametersRepository
										.findById(mCUNormsValueGrade.getMaterialFkId());
								if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
									continue;
								}

							}

							mCUNormsValueGrade.setId(UUID.fromString(mCUNormsValueDTO.getId()));
							mCUNormsValueGrade.setModifiedOn(new Date());
							boolean changed = false;

							// January
							double newJan = Optional.ofNullable(mCUNormsValueDTO.getJanuary()).orElse(0.0);
							double oldJan = Optional.ofNullable(mCUNormsValueGrade.getJanuary()).orElse(0.0);
							if (isDifferent(oldJan, newJan)) {
								mCUNormsValueGrade.setJanuary(newJan);
								changed = true;
							}

							// February
							double newFeb = Optional.ofNullable(mCUNormsValueDTO.getFebruary()).orElse(0.0);
							double oldFeb = Optional.ofNullable(mCUNormsValueGrade.getFebruary()).orElse(0.0);
							if (isDifferent(oldFeb, newFeb)) {
								mCUNormsValueGrade.setFebruary(newFeb);
								changed = true;
							}

							// March
							double newMar = Optional.ofNullable(mCUNormsValueDTO.getMarch()).orElse(0.0);
							double oldMar = Optional.ofNullable(mCUNormsValueGrade.getMarch()).orElse(0.0);
							if (isDifferent(oldMar, newMar)) {
								mCUNormsValueGrade.setMarch(newMar);
								changed = true;
							}

							// April
							double newApr = Optional.ofNullable(mCUNormsValueDTO.getApril()).orElse(0.0);
							double oldApr = Optional.ofNullable(mCUNormsValueGrade.getApril()).orElse(0.0);
							if (isDifferent(oldApr, newApr)) {
								mCUNormsValueGrade.setApril(newApr);
								changed = true;
							}

							// May
							double newMay = Optional.ofNullable(mCUNormsValueDTO.getMay()).orElse(0.0);
							double oldMay = Optional.ofNullable(mCUNormsValueGrade.getMay()).orElse(0.0);
							if (isDifferent(oldMay, newMay)) {
								mCUNormsValueGrade.setMay(newMay);
								changed = true;
							}

							// June
							double newJun = Optional.ofNullable(mCUNormsValueDTO.getJune()).orElse(0.0);
							double oldJun = Optional.ofNullable(mCUNormsValueGrade.getJune()).orElse(0.0);
							if (isDifferent(oldJun, newJun)) {
								mCUNormsValueGrade.setJune(newJun);
								changed = true;
							}

							// July
							double newJul = Optional.ofNullable(mCUNormsValueDTO.getJuly()).orElse(0.0);
							double oldJul = Optional.ofNullable(mCUNormsValueGrade.getJuly()).orElse(0.0);
							if (isDifferent(oldJul, newJul)) {
								mCUNormsValueGrade.setJuly(newJul);
								changed = true;
							}

							// August
							double newAug = Optional.ofNullable(mCUNormsValueDTO.getAugust()).orElse(0.0);
							double oldAug = Optional.ofNullable(mCUNormsValueGrade.getAugust()).orElse(0.0);
							if (isDifferent(oldAug, newAug)) {
								mCUNormsValueGrade.setAugust(newAug);
								changed = true;
							}

							// September
							double newSep = Optional.ofNullable(mCUNormsValueDTO.getSeptember()).orElse(0.0);
							double oldSep = Optional.ofNullable(mCUNormsValueGrade.getSeptember()).orElse(0.0);
							if (isDifferent(oldSep, newSep)) {
								mCUNormsValueGrade.setSeptember(newSep);
								changed = true;
							}

							// October
							double newOct = Optional.ofNullable(mCUNormsValueDTO.getOctober()).orElse(0.0);
							double oldOct = Optional.ofNullable(mCUNormsValueGrade.getOctober()).orElse(0.0);
							if (isDifferent(oldOct, newOct)) {
								mCUNormsValueGrade.setOctober(newOct);
								changed = true;
							}

							// November
							double newNov = Optional.ofNullable(mCUNormsValueDTO.getNovember()).orElse(0.0);
							double oldNov = Optional.ofNullable(mCUNormsValueGrade.getNovember()).orElse(0.0);
							if (isDifferent(oldNov, newNov)) {
								mCUNormsValueGrade.setNovember(newNov);
								changed = true;
							}

							// December
							double newDec = Optional.ofNullable(mCUNormsValueDTO.getDecember()).orElse(0.0);
							double oldDec = Optional.ofNullable(mCUNormsValueGrade.getDecember()).orElse(0.0);
							if (isDifferent(oldDec, newDec)) {
								mCUNormsValueGrade.setDecember(newDec);
								changed = true;
							}

							if (!isFromExcel) {
								if (mCUNormsValueDTO.getSiteFkId() != null) {
									mCUNormsValueGrade.setSiteFkId(UUID.fromString(mCUNormsValueDTO.getSiteFkId()));
								}
								if (plantFKId != null) {
									mCUNormsValueGrade.setPlantFkId(plantFKId);
								}
								if (mCUNormsValueDTO.getVerticalFkId() != null) {
									mCUNormsValueGrade
											.setVerticalFkId(UUID.fromString(mCUNormsValueDTO.getVerticalFkId()));
								}
								if (mCUNormsValueDTO.getMaterialFkId() != null) {
									mCUNormsValueGrade
											.setMaterialFkId(UUID.fromString(mCUNormsValueDTO.getMaterialFkId()));
								}
								if (mCUNormsValueDTO.getNormParameterTypeId() != null) {
									mCUNormsValueGrade.setNormParameterTypeFkId(
											UUID.fromString(mCUNormsValueDTO.getNormParameterTypeId()));
								}
								mCUNormsValueGrade.setFinancialYear(mCUNormsValueDTO.getFinancialYear());
							}

							mCUNormsValueGrade.setMcuVersion("V1");
							mCUNormsValueGrade.setUpdatedBy(Utility.getUserName());
							mCUNormsValueGrade.setModifiedOn(new Date());
							mCUNormsValueGrade.setGradeFkId(UUID.fromString(mCUNormsValueDTO.getGradeId()));
							System.out.println("Data Saved Succussfully" + mCUNormsValue);
							if (changed
									&& Objects.equals(mCUNormsValueGrade.getRemarks(), mCUNormsValueDTO.getRemarks())) {
								mCUNormsValueDTO.setErrDescription("Please add/update remark");
								mCUNormsValueDTO.setSaveStatus("Failed");
								failedList.add(mCUNormsValueDTO);
								continue;
							}
							mCUNormsValueGrade.setRemarks(mCUNormsValueDTO.getRemarks());
							mcuNormsValueGradeRepository.save(mCUNormsValueGrade);

						} else {
							if (isFromExcel) {
								mCUNormsValueDTO.setSaveStatus("Failed");
								mCUNormsValueDTO.setErrDescription("Invalid Id. Record not found.");
								failedList.add(mCUNormsValueDTO);
								continue;
							}
						}

					} else {
						if (vertical.getName().equalsIgnoreCase("Elastomer") && site.getName().equalsIgnoreCase("JMD")
								&& plant.getName().equalsIgnoreCase("HIIR")) {
							updateMCUNormsValueGrade(mCUNormsValueDTO, plantFKId, isFromExcel, failedList);
							continue;
						}
						if (vertical.getName().equalsIgnoreCase("Elastomer") && site.getName().equalsIgnoreCase("HMD")
								&& plant.getName().equalsIgnoreCase("SBR")) {
							updateMCUNormsValueGrade(mCUNormsValueDTO, plantFKId, isFromExcel, failedList);
							continue;
						}

						Optional<MCUNormsValue> normsValue = normalOperationNormsRepository
								.findById(UUID.fromString(mCUNormsValueDTO.getId()));
						if (normsValue.isPresent()) {
							mCUNormsValue = normsValue.get();
							entityManager.detach(mCUNormsValue);
							if (mCUNormsValue.getMaterialFkId() != null) {
								Optional<NormParameters> normParametersOpt = normParametersRepository
										.findById(mCUNormsValue.getMaterialFkId());
								if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
									continue;
								}
							}

							mCUNormsValue.setId(UUID.fromString(mCUNormsValueDTO.getId()));
							mCUNormsValue.setModifiedOn(new Date());
							boolean changed = false;

							double newJan = Optional.ofNullable(mCUNormsValueDTO.getJanuary()).orElse(0.0);
							double oldJan = Optional.ofNullable(mCUNormsValue.getJanuary()).orElse(0.0);
							if (isDifferent(oldJan, newJan)) {
								mCUNormsValue.setJanuary(newJan);
								changed = true;
							}

							// February
							double newFeb = Optional.ofNullable(mCUNormsValueDTO.getFebruary()).orElse(0.0);
							double oldFeb = Optional.ofNullable(mCUNormsValue.getFebruary()).orElse(0.0);
							if (isDifferent(oldFeb, newFeb)) {
								mCUNormsValue.setFebruary(newFeb);
								changed = true;
							}

							// March
							double newMar = Optional.ofNullable(mCUNormsValueDTO.getMarch()).orElse(0.0);
							double oldMar = Optional.ofNullable(mCUNormsValue.getMarch()).orElse(0.0);
							if (isDifferent(oldMar, newMar)) {
								mCUNormsValue.setMarch(newMar);
								changed = true;
							}

							// April
							double newApr = Optional.ofNullable(mCUNormsValueDTO.getApril()).orElse(0.0);
							double oldApr = Optional.ofNullable(mCUNormsValue.getApril()).orElse(0.0);
							if (isDifferent(oldApr, newApr)) {
								mCUNormsValue.setApril(newApr);
								changed = true;
							}

							// May
							double newMay = Optional.ofNullable(mCUNormsValueDTO.getMay()).orElse(0.0);
							double oldMay = Optional.ofNullable(mCUNormsValue.getMay()).orElse(0.0);
							if (isDifferent(oldMay, newMay)) {
								mCUNormsValue.setMay(newMay);
								changed = true;
							}

							// June
							double newJun = Optional.ofNullable(mCUNormsValueDTO.getJune()).orElse(0.0);
							double oldJun = Optional.ofNullable(mCUNormsValue.getJune()).orElse(0.0);
							if (isDifferent(oldJun, newJun)) {
								mCUNormsValue.setJune(newJun);
								changed = true;
							}

							// July
							double newJul = Optional.ofNullable(mCUNormsValueDTO.getJuly()).orElse(0.0);
							double oldJul = Optional.ofNullable(mCUNormsValue.getJuly()).orElse(0.0);
							if (isDifferent(oldJul, newJul)) {
								mCUNormsValue.setJuly(newJul);
								changed = true;
							}

							// August
							double newAug = Optional.ofNullable(mCUNormsValueDTO.getAugust()).orElse(0.0);
							double oldAug = Optional.ofNullable(mCUNormsValue.getAugust()).orElse(0.0);
							if (isDifferent(oldAug, newAug)) {
								mCUNormsValue.setAugust(newAug);
								changed = true;
							}

							// September
							double newSep = Optional.ofNullable(mCUNormsValueDTO.getSeptember()).orElse(0.0);
							double oldSep = Optional.ofNullable(mCUNormsValue.getSeptember()).orElse(0.0);
							if (isDifferent(oldSep, newSep)) {
								mCUNormsValue.setSeptember(newSep);
								changed = true;
							}

							// October
							double newOct = Optional.ofNullable(mCUNormsValueDTO.getOctober()).orElse(0.0);
							double oldOct = Optional.ofNullable(mCUNormsValue.getOctober()).orElse(0.0);
							if (isDifferent(oldOct, newOct)) {
								mCUNormsValue.setOctober(newOct);
								changed = true;
							}

							// November
							double newNov = Optional.ofNullable(mCUNormsValueDTO.getNovember()).orElse(0.0);
							double oldNov = Optional.ofNullable(mCUNormsValue.getNovember()).orElse(0.0);
							if (isDifferent(oldNov, newNov)) {
								mCUNormsValue.setNovember(newNov);
								changed = true;
							}

							// December
							double newDec = Optional.ofNullable(mCUNormsValueDTO.getDecember()).orElse(0.0);
							double oldDec = Optional.ofNullable(mCUNormsValue.getDecember()).orElse(0.0);
							if (isDifferent(oldDec, newDec)) {
								mCUNormsValue.setDecember(newDec);
								changed = true;
							}

							if (isFromExcel) {
								if (mCUNormsValueDTO.getSiteFkId() != null) {
									mCUNormsValue.setSiteFkId(UUID.fromString(mCUNormsValueDTO.getSiteFkId()));
								}
								if (plantFKId != null) {
									mCUNormsValue.setPlantFkId(plantFKId);
								}
								if (mCUNormsValueDTO.getVerticalFkId() != null) {
									mCUNormsValue.setVerticalFkId(UUID.fromString(mCUNormsValueDTO.getVerticalFkId()));
								}
								if (mCUNormsValueDTO.getMaterialFkId() != null) {
									mCUNormsValue.setMaterialFkId(UUID.fromString(mCUNormsValueDTO.getMaterialFkId()));
								}
								if (mCUNormsValueDTO.getNormParameterTypeId() != null) {
									mCUNormsValue.setNormParameterTypeFkId(
											UUID.fromString(mCUNormsValueDTO.getNormParameterTypeId()));
								}

								mCUNormsValue.setFinancialYear(mCUNormsValueDTO.getFinancialYear());
							}

							mCUNormsValue.setMcuVersion("V1");
							mCUNormsValue.setUpdatedBy(Utility.getUserName());
							// Use Objects.equals to safely compare two strings even if one or both are null
							if (changed && Objects.equals(mCUNormsValue.getRemarks(), mCUNormsValueDTO.getRemarks())) {
								mCUNormsValueDTO.setErrDescription("Please add/update remark");
								mCUNormsValueDTO.setSaveStatus("Failed");
								failedList.add(mCUNormsValueDTO);
								continue;
							}
							mCUNormsValue.setRemarks(mCUNormsValueDTO.getRemarks());
							System.out.println("Data Saved Succussfully" + mCUNormsValue);

							normalOperationNormsRepository.save(mCUNormsValue);
						} else {
							if (isFromExcel) {
								mCUNormsValueDTO.setSaveStatus("Failed");
								mCUNormsValueDTO.setErrDescription("Invalid Id. Record not found.");
								failedList.add(mCUNormsValueDTO);
								continue;
							}
						}

					}
				}
			}
			List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("normal-op-norms");
			for (ScreenMapping screenMapping : screenMappingList) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(plantFKId);
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("Chemical")
					|| aromaticPmd || aromaticSEZ) {
				String procedure = vertical.getName() + "_" + site.getName() + "_CalculateTotalFuelNorms";
				executeProcedure(procedure, plantFKId.toString(), year);
			}
			// TODO Auto-generated method stub
			return failedList;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	private List<MCUNormsValueDTO> buildDTOListFromSavedGradeData(
	        UUID plantFKId, UUID siteId, UUID verticalId, String gradeId, String year) {

	
		UUID gradeFkId = UUID.fromString(gradeId);

		List<MCUNormsValueGrade> savedRows =
		        mcuNormsValueGradeRepository
		                .findByPlantFkIdAndGradeFkIdAndFinancialYear(
		                        plantFKId,
		                        gradeFkId,
		                        year);
	          

	    List<MCUNormsValueDTO> dtoList = new ArrayList<>();
	    for (MCUNormsValueGrade row : savedRows) {
	        MCUNormsValueDTO dto = new MCUNormsValueDTO();
	        dto.setMaterialFkId(row.getMaterialFkId().toString());
	        dto.setGradeId(row.getGradeFkId().toString());
	        dto.setFinancialYear(year);
	        dto.setPlantFkId(plantFKId.toString());

	        dto.setJanuary(row.getJanuary());
	        dto.setFebruary(row.getFebruary());
	        dto.setMarch(row.getMarch());
	        dto.setApril(row.getApril());
	        dto.setMay(row.getMay());
	        dto.setJune(row.getJune());
	        dto.setJuly(row.getJuly());
	        dto.setAugust(row.getAugust());
	        dto.setSeptember(row.getSeptember());
	        dto.setOctober(row.getOctober());
	        dto.setNovember(row.getNovember());
	        dto.setDecember(row.getDecember());

	        dtoList.add(dto);
	    }
	    return dtoList;
	}

	public void updateMCUNormsValueGrade(MCUNormsValueDTO mCUNormsValueDTO, UUID plantFKId, boolean isFromExcel, List<MCUNormsValueDTO> failedList) {

		Optional<MCUNormsValueGrade> normsValue = mcuNormsValueGradeRepository
								.findById(UUID.fromString(mCUNormsValueDTO.getId()));
						if (normsValue.isPresent()) {
							MCUNormsValueGrade mCUNormsValueGrade = normsValue.get();
							if (mCUNormsValueGrade.getMaterialFkId() != null) {
								Optional<NormParameters> normParametersOpt = normParametersRepository
										.findById(mCUNormsValueGrade.getMaterialFkId());
								if (!normParametersOpt.isEmpty() && (!normParametersOpt.get().getIsEditable())) {
									return;
								}
							}

							mCUNormsValueGrade.setId(UUID.fromString(mCUNormsValueDTO.getId()));
							mCUNormsValueGrade.setModifiedOn(new Date());
							boolean changed = false;

							double newJan = Optional.ofNullable(mCUNormsValueDTO.getJanuary()).orElse(0.0);
							double oldJan = Optional.ofNullable(mCUNormsValueGrade.getJanuary()).orElse(0.0);
							if (isDifferent(oldJan, newJan)) {
								mCUNormsValueGrade.setJanuary(newJan);
								changed = true;
							}

							// February
							double newFeb = Optional.ofNullable(mCUNormsValueDTO.getFebruary()).orElse(0.0);
							double oldFeb = Optional.ofNullable(mCUNormsValueGrade.getFebruary()).orElse(0.0);
							if (isDifferent(oldFeb, newFeb)) {
								mCUNormsValueGrade.setFebruary(newFeb);
								changed = true;
							}

							// March
							double newMar = Optional.ofNullable(mCUNormsValueDTO.getMarch()).orElse(0.0);
							double oldMar = Optional.ofNullable(mCUNormsValueGrade.getMarch()).orElse(0.0);
							if (isDifferent(oldMar, newMar)) {
								mCUNormsValueGrade.setMarch(newMar);
								changed = true;
							}

							// April
							double newApr = Optional.ofNullable(mCUNormsValueDTO.getApril()).orElse(0.0);
							double oldApr = Optional.ofNullable(mCUNormsValueGrade.getApril()).orElse(0.0);
							if (isDifferent(oldApr, newApr)) {
								mCUNormsValueGrade.setApril(newApr);
								changed = true;
							}

							// May
							double newMay = Optional.ofNullable(mCUNormsValueDTO.getMay()).orElse(0.0);
							double oldMay = Optional.ofNullable(mCUNormsValueGrade.getMay()).orElse(0.0);
							if (isDifferent(oldMay, newMay)) {
								mCUNormsValueGrade.setMay(newMay);
								changed = true;
							}

							// June
							double newJun = Optional.ofNullable(mCUNormsValueDTO.getJune()).orElse(0.0);
							double oldJun = Optional.ofNullable(mCUNormsValueGrade.getJune()).orElse(0.0);
							if (isDifferent(oldJun, newJun)) {
								mCUNormsValueGrade.setJune(newJun);
								changed = true;
							}

							// July
							double newJul = Optional.ofNullable(mCUNormsValueDTO.getJuly()).orElse(0.0);
							double oldJul = Optional.ofNullable(mCUNormsValueGrade.getJuly()).orElse(0.0);
							if (isDifferent(oldJul, newJul)) {
								mCUNormsValueGrade.setJuly(newJul);
								changed = true;
							}

							// August
							double newAug = Optional.ofNullable(mCUNormsValueDTO.getAugust()).orElse(0.0);
							double oldAug = Optional.ofNullable(mCUNormsValueGrade.getAugust()).orElse(0.0);
							if (isDifferent(oldAug, newAug)) {
								mCUNormsValueGrade.setAugust(newAug);
								changed = true;
							}

							// September
							double newSep = Optional.ofNullable(mCUNormsValueDTO.getSeptember()).orElse(0.0);
							double oldSep = Optional.ofNullable(mCUNormsValueGrade.getSeptember()).orElse(0.0);
							if (isDifferent(oldSep, newSep)) {
								mCUNormsValueGrade.setSeptember(newSep);
								changed = true;
							}

							// October
							double newOct = Optional.ofNullable(mCUNormsValueDTO.getOctober()).orElse(0.0);
							double oldOct = Optional.ofNullable(mCUNormsValueGrade.getOctober()).orElse(0.0);
							if (isDifferent(oldOct, newOct)) {
								mCUNormsValueGrade.setOctober(newOct);
								changed = true;
							}

							// November
							double newNov = Optional.ofNullable(mCUNormsValueDTO.getNovember()).orElse(0.0);
							double oldNov = Optional.ofNullable(mCUNormsValueGrade.getNovember()).orElse(0.0);
							if (isDifferent(oldNov, newNov)) {
								mCUNormsValueGrade.setNovember(newNov);
								changed = true;
							}

							// December
							double newDec = Optional.ofNullable(mCUNormsValueDTO.getDecember()).orElse(0.0);
							double oldDec = Optional.ofNullable(mCUNormsValueGrade.getDecember()).orElse(0.0);
							if (isDifferent(oldDec, newDec)) {
								mCUNormsValueGrade.setDecember(newDec);
								changed = true;
							}

							if (isFromExcel) {
								if (mCUNormsValueDTO.getSiteFkId() != null) {
									mCUNormsValueGrade.setSiteFkId(UUID.fromString(mCUNormsValueDTO.getSiteFkId()));
								}
								if (plantFKId != null) {
									mCUNormsValueGrade.setPlantFkId(plantFKId);
								}
								if (mCUNormsValueDTO.getVerticalFkId() != null) {
									mCUNormsValueGrade.setVerticalFkId(UUID.fromString(mCUNormsValueDTO.getVerticalFkId()));
								}
								if (mCUNormsValueDTO.getMaterialFkId() != null) {
									mCUNormsValueGrade.setMaterialFkId(UUID.fromString(mCUNormsValueDTO.getMaterialFkId()));
								}
								if (mCUNormsValueDTO.getNormParameterTypeId() != null) {
									mCUNormsValueGrade.setNormParameterTypeFkId(
											UUID.fromString(mCUNormsValueDTO.getNormParameterTypeId()));
								}

								mCUNormsValueGrade.setFinancialYear(mCUNormsValueDTO.getFinancialYear());
							}

							mCUNormsValueGrade.setMcuVersion("V1");
							mCUNormsValueGrade.setUpdatedBy(Utility.getUserName());
							// Use Objects.equals to safely compare two strings even if one or both are null
							if (changed && Objects.equals(mCUNormsValueGrade.getRemarks(), mCUNormsValueDTO.getRemarks())) {
								mCUNormsValueDTO.setErrDescription("Please add/update remark");
								mCUNormsValueDTO.setSaveStatus("Failed");
								failedList.add(mCUNormsValueDTO);
								return;
							}
							mCUNormsValueGrade.setRemarks(mCUNormsValueDTO.getRemarks());
							System.out.println("Data Saved Succussfully" + mCUNormsValueGrade);
							mcuNormsValueGradeRepository.save(mCUNormsValueGrade);
						} else {
							if (isFromExcel) {
								mCUNormsValueDTO.setSaveStatus("Failed");
								mCUNormsValueDTO.setErrDescription("Invalid Id. Record not found.");
								failedList.add(mCUNormsValueDTO);
								return;
							}
						}
	}

	// Helper method for precise comparison of primitive double values
	private boolean isDifferent(double oldVal, double newVal) {
		return Double.compare(oldVal, newVal) != 0;
	}

	@Override
	@Transactional
	public AOPMessageVM loadGradeWiseConsumptionNorms(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		String storedProcedure = vertical.getName() + "_" + site.getName() + "_LoadGradewiseConsumptionNorms";
		System.out.println("storedProcedure" + storedProcedure);
		int result = executeDynamicUpdateProcedure(storedProcedure, plantId, year);
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("SP Executed successfully");
		aopMessageVM.setData(result);
		return aopMessageVM;
	}

	@Override
	@Transactional
	public AOPMessageVM calculateExpressionConsumptionNorms(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		String storedProcedure = vertical.getName() + "_" + site.getName() + "_NormsCalculation";
		if(vertical.getName().equalsIgnoreCase("PCG")) {
			storedProcedure = "[RIL.AOP.Refinery].[dbo].[" + storedProcedure + "]";
		}
		System.out.println("storedProcedure" + storedProcedure);
		int result = executeDynamicUpdateProcedure(storedProcedure, plantId, site.getId().toString(),
				vertical.getId().toString(), year);
		aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year,
				"normal-op-norms");
		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("normal-op-norms");
		for (ScreenMapping screenMapping : screenMappingList) {
			if (!screenMapping.getCalculationScreen().equalsIgnoreCase(screenMapping.getDependentScreen())) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
		}
		List<ScreenMapping> calculateScreenMappingList = screenMappingRepository
				.findByDependentScreen("normal-op-norms-calculate");
		for (ScreenMapping screenMapping : calculateScreenMappingList) {
			if (!screenMapping.getCalculationScreen().equalsIgnoreCase(screenMapping.getDependentScreen())) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("SP Executed successfully");
		aopMessageVM.setData(result);
		return aopMessageVM;
	}

	// @Transactional
	// public int executeDynamicUpdateProcedure(String procedureName, String
	// plantId, String siteId, String verticalId,
	// String finYear) {
	// try {
	// String sql = "EXEC " + procedureName
	// + " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId,
	// @finYear = :finYear";

	// Query query = entityManager.createNativeQuery(sql);

	// // Setting all parameters
	// query.setParameter("plantId", plantId);
	// query.setParameter("siteId", siteId);
	// query.setParameter("verticalId", verticalId);
	// query.setParameter("finYear", finYear);

	// int rowsUpdated = query.executeUpdate();

	// entityManager.flush(); // <-- force JPA to execute SQL immediately

	// return rowsUpdated;

	// } catch (Exception e) {
	// e.printStackTrace();
	// return 0;
	// }
	// }

	public int executeDynamicUpdateProcedure(String procedureName, String plantId, String siteId, String verticalId,
			String finYear) {

			String verticalName = verticalRepository.findById(UUID.fromString(verticalId)).get().getName();

		String callSql = "{call " + "[" + procedureName + "]" + "(?, ?, ?, ?)}";

		if(verticalName.equalsIgnoreCase("PCG")) {
			callSql = "{call " + procedureName + "(?, ?, ?, ?)}";
		}

		try (Connection connection = dataSource.getConnection();
				CallableStatement stmt = connection.prepareCall(callSql)) {

			// Set parameters
			stmt.setString(1, plantId);
			stmt.setString(2, siteId);
			stmt.setString(3, verticalId);
			stmt.setString(4, finYear);

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
	}

	public int executeDynamicUpdateProcedure(String procedureName, String plantId, String year) {
		String callSql = "{call " + "[" + procedureName + "]" + "(?, ?)}";

		try (Connection connection = dataSource.getConnection();
				CallableStatement stmt = connection.prepareCall(callSql)) {

			// Set parameters
			stmt.setString(1, plantId);
			stmt.setString(2, year);

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
	}

	@Transactional
	public List<Object[]> getNormalOperationNormsDataFromView(String financialYear, UUID plantId, String gradeId,
			String mode) {
		try {
			Plants plant = plantsRepository.findById(plantId).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();

			boolean pvc = vertical.getName().equalsIgnoreCase("PVC") && (site.getName().equalsIgnoreCase("VMD") || site.getName().equalsIgnoreCase("DMD") || site.getName().equalsIgnoreCase("HMD"));
			boolean elastomerHmdSbr = vertical.getName().equalsIgnoreCase("ELASTOMER") && site.getName().equalsIgnoreCase("HMD") && plant.getName().equalsIgnoreCase("SBR");

			Boolean withGrade = false;
			if (elastomerHmdSbr || pvc || (vertical.getName().equalsIgnoreCase("STAPLE")&& gradeId != null && !gradeId.trim().isEmpty() ) || (vertical.getName().equalsIgnoreCase("Filament")&& gradeId != null && !gradeId.trim().isEmpty() )) {
				withGrade = true;
			}
			
			Boolean elastomer=vertical.getName().equalsIgnoreCase("ELASTOMER") && site.getName().equalsIgnoreCase("JMD") && plant.getName().equalsIgnoreCase("HIIR");
			String viewName = "vwScrn" + vertical.getName() + "NormalOperationNorms";
			if (withGrade || elastomer ) {
				viewName = "vwScrn" + vertical.getName() + "NormalOperationNormsGrade";
			}
			else if (vertical.getName().equalsIgnoreCase("PCG")) { 
				// [RIL.AOP.Refinery].[dbo].
				 viewName = "[RIL.AOP.Refinery].[dbo].[vwScrn" + vertical.getName() + "NormalOperationNorms]";
			}
			// Validate or sanitize viewName before using it directly in the query to
			// prevent SQL injection
			String sql = null;
			if (vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")
					|| vertical.getName().equalsIgnoreCase("PET") || withGrade || pvc || elastomer) {
				sql = "SELECT * FROM " + viewName
						+ " WHERE FinancialYear = :financialYear AND Plant_FK_Id = :plantId AND Grade_FK_Id = :gradeId";
			} else if (vertical.getName().equalsIgnoreCase("Cracker")) {
				sql = "SELECT * FROM " + viewName
						+ " WHERE FinancialYear = :financialYear AND Plant_FK_Id = :plantId AND mode = :mode";
			} else {
				sql = "SELECT * FROM " + viewName + " WHERE FinancialYear = :financialYear AND Plant_FK_Id = :plantId";
			}

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("financialYear", financialYear);
			query.setParameter("plantId", plantId);
			if (vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")
					|| vertical.getName().equalsIgnoreCase("PET") || withGrade || pvc || elastomer) {
				query.setParameter("gradeId", UUID.fromString(gradeId));
			}
			if (vertical.getName().equalsIgnoreCase("Cracker")) {
				query.setParameter("mode", mode);
			}

			return query.getResultList(); // You can cast this to a DTO later
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getNormsTransaction(String plantId, String aopYear) {
		try {
			UUID plantUUID = UUID.fromString(plantId);

			List<Object[]> transactions = normsTransactionRepository
					.findDistinctTransactionsByMonthAndParameter(plantUUID, aopYear);

			List<Map<String, Object>> normsTransactions = transactions.stream().map(tx -> {
				Map<String, Object> cell = new HashMap<>();
				cell.put("month", tx[0]);
				cell.put("normParameterFKId", tx[1].toString());
				cell.put("value", tx[2]);
				return cell;
			}).collect(Collectors.toList());

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Norms Transactions retrieved successfully.");
			aopMessageVM.setData(normsTransactions);

			return aopMessageVM;

		} catch (Exception ex) {
			throw new RestInvalidArgumentException("normsTransaction", ex);
		}
	}

	private Double getMonthlyValue(Object obj, int month) {
		try {
			String methodName = switch (month) {
			case 1 -> "getJanuary";
			case 2 -> "getFebruary";
			case 3 -> "getMarch";
			case 4 -> "getApril";
			case 5 -> "getMay";
			case 6 -> "getJune";
			case 7 -> "getJuly";
			case 8 -> "getAugust";
			case 9 -> "getSeptember";
			case 10 -> "getOctober";
			case 11 -> "getNovember";
			case 12 -> "getDecember";
			default -> throw new IllegalArgumentException("Invalid month: " + month);
			};
			Method method = obj.getClass().getMethod(methodName);
			return (Double) method.invoke(obj);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public AOPMessageVM importExcel(String year, UUID plantFKId, String gradeId, MultipartFile file, String mode) {
		// TODO Auto-generated method stub
		try {
			Plants plant = plantsRepository.findById(plantFKId).get();
			List<MCUNormsValueDTO> data = null;
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			boolean pvc = vertical.getName().equalsIgnoreCase("PVC") && (site.getName().equalsIgnoreCase("VMD") || site.getName().equalsIgnoreCase("DMD") || site.getName().equalsIgnoreCase("HMD"));

			boolean ptaPmdPia = vertical.getName().equalsIgnoreCase("PTA") && site.getName().equalsIgnoreCase("PMD") && plant.getName().equalsIgnoreCase("PIA");

			if(ptaPmdPia){ 
				// seperate method to handle sapcode column
				return importExcelWithSapCode(year, plantFKId, gradeId, file, mode);
			}

			if (vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")
					|| vertical.getName().equalsIgnoreCase("PET") || (vertical.getName().equalsIgnoreCase("STAPLE")&& gradeId != null && !gradeId.trim().isEmpty() ) || pvc) {
				data = readSteadyState(file.getInputStream(), plantFKId, year);
			} else {
				data = readConfigurations(file.getInputStream(), plantFKId, year);
			}

			List<MCUNormsValueDTO> failedRecords = saveNormalOperationNormsData(data, plantFKId, year, gradeId, true);

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = null;
				if (vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")
						|| vertical.getName().equalsIgnoreCase("PET") || (vertical.getName().equalsIgnoreCase("STAPLE")&& gradeId != null && !gradeId.trim().isEmpty() ) || pvc) {
					fileByteArray = exportSteadyStateNorms(year, plantFKId, true, failedRecords, mode);
				} else {
					fileByteArray = createExcel(year, plantFKId, true, failedRecords, mode, gradeId);
				}
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

	@Override
	public AOPMessageVM importExcelSAP(String year, UUID plantFKId, String gradeId, MultipartFile file, String mode) {
		// TODO Auto-generated method stub
		try {

			Plants plant = plantsRepository.findById(plantFKId).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

			boolean merox = vertical.getName().equalsIgnoreCase("MEROX");
			
			List<MCUNormsValueDTO> data = null;

				if (merox) {
				      data = readConfigurationsSAPMerox(file.getInputStream(), plantFKId, year);
					} else {
							data = readConfigurationsSAP(file.getInputStream(), plantFKId, year);
					}
			List<MCUNormsValueDTO> failedRecords = saveNormalOperationNormsData(data, plantFKId, year, gradeId, true);

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[]	fileByteArray = createExcelSAP(year, plantFKId, true, failedRecords, mode, gradeId);
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

	// ref: importExcel | separate method to include sap code
	@Override
	public AOPMessageVM importExcelWithSapCode(String year, UUID plantFKId, String gradeId, MultipartFile file, String mode) {
		try {
			Plants plant = plantsRepository.findById(plantFKId).get();
			List<MCUNormsValueDTO> data = null;
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			boolean pvc = vertical.getName().equalsIgnoreCase("PVC") && (site.getName().equalsIgnoreCase("VMD") || site.getName().equalsIgnoreCase("DMD") || site.getName().equalsIgnoreCase("HMD"));
			if (vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")
					|| vertical.getName().equalsIgnoreCase("PET") || (vertical.getName().equalsIgnoreCase("STAPLE") && gradeId != null && !gradeId.trim().isEmpty()) || pvc) {
				data = readSteadyStateWithSapCode(file.getInputStream(), plantFKId, year);
			} else {
				data = readConfigurationsWithSapCode(file.getInputStream(), plantFKId, year);
			}

			List<MCUNormsValueDTO> failedRecords = saveNormalOperationNormsData(data, plantFKId, year, gradeId, true);

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = null;
				if (vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")
						|| vertical.getName().equalsIgnoreCase("PET") || (vertical.getName().equalsIgnoreCase("STAPLE") && gradeId != null && !gradeId.trim().isEmpty()) || pvc) {
					fileByteArray = exportSteadyStateNorms(year, plantFKId, true, failedRecords, mode);
				} else {
					fileByteArray = createExcelWithSapCode(year, plantFKId, true, failedRecords, mode, gradeId);
				}
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

	public List<MCUNormsValueDTO> readSteadyStateWithSapCode(InputStream inputStream, UUID plantFKId, String year) {
		List<MCUNormsValueDTO> configList = new ArrayList<>();
		Map<String, String> gradeMap = getGradeNameIdMap(year, plantFKId);
		Map<String, String> materialMap = getMaterialNameIdMap(plantFKId);
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {

			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				Sheet sheet = workbook.getSheetAt(i);
				if (sheet == null) {
					continue;
				}
				String sheetName = sheet.getSheetName();
				String gradeId = gradeMap.get(Utility.sanitizeSheetName(sheetName));

				Iterator<Row> rowIterator = sheet.iterator();
				if (rowIterator.hasNext()) {
					rowIterator.next();
				}
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					if (row.getPhysicalNumberOfCells() == 0) {
						continue;
					}

				MCUNormsValueDTO dto = new MCUNormsValueDTO();
				try {
					dto.setNormParameterTypeDisplayName(getStringCellValue(row.getCell(0), dto));

					String productName = getStringCellValue(row.getCell(1), dto);
					String materialFkId = materialMap.get(Utility.sanitizeSheetName(productName));
					dto.setProductName(productName);
					dto.setMaterialFkId(materialFkId);

					dto.setUOM(getStringCellValue(row.getCell(2), dto));

					dto.setFinancialYear(year);
					dto.setPlantFkId(plantFKId.toString());
					dto.setApril(getNumericCellValue(row.getCell(4), dto));
						dto.setMay(getNumericCellValue(row.getCell(5), dto));
						dto.setJune(getNumericCellValue(row.getCell(6), dto));
						dto.setJuly(getNumericCellValue(row.getCell(7), dto));
						dto.setAugust(getNumericCellValue(row.getCell(8), dto));
						dto.setSeptember(getNumericCellValue(row.getCell(9), dto));
						dto.setOctober(getNumericCellValue(row.getCell(10), dto));
						dto.setNovember(getNumericCellValue(row.getCell(11), dto));
						dto.setDecember(getNumericCellValue(row.getCell(12), dto));
						dto.setJanuary(getNumericCellValue(row.getCell(13), dto));
						dto.setFebruary(getNumericCellValue(row.getCell(14), dto));
						dto.setMarch(getNumericCellValue(row.getCell(15), dto));
						dto.setRemarks(getStringCellValue(row.getCell(16), dto));
						dto.setId(getStringCellValue(row.getCell(17), dto));
						dto.setGradeId(gradeId);

					} catch (Exception e) {
						e.printStackTrace();
						dto.setErrDescription(e.getMessage());
						dto.setSaveStatus("Failed");
					}
					configList.add(dto);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return configList;
	}

	public List<MCUNormsValueDTO> readConfigurationsWithSapCode(InputStream inputStream, UUID plantFKId, String year) {
		List<MCUNormsValueDTO> configList = new ArrayList<>();
		Plants plant = plantsRepository.findById(plantFKId).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
				Sheet sheet = workbook.getSheetAt(sheetIndex);
				Iterator<Row> rowIterator = sheet.iterator();

				if (rowIterator.hasNext())
					rowIterator.next();

				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
				MCUNormsValueDTO dto = new MCUNormsValueDTO();

				try {
					dto.setNormParameterTypeDisplayName(getStringCellValue(row.getCell(0), dto));
					dto.setProductName(getStringCellValue(row.getCell(1), dto));

					if ("Total Fuel".equalsIgnoreCase(dto.getProductName())) {
						// placeholder — same as readConfigurations
					} else {
						dto.setApril(getNumericCellValue(row.getCell(4), dto));
							dto.setMay(getNumericCellValue(row.getCell(5), dto));
							dto.setJune(getNumericCellValue(row.getCell(6), dto));
							dto.setJuly(getNumericCellValue(row.getCell(7), dto));
							dto.setAugust(getNumericCellValue(row.getCell(8), dto));
							dto.setSeptember(getNumericCellValue(row.getCell(9), dto));
							dto.setOctober(getNumericCellValue(row.getCell(10), dto));
							dto.setNovember(getNumericCellValue(row.getCell(11), dto));
							dto.setDecember(getNumericCellValue(row.getCell(12), dto));
							dto.setJanuary(getNumericCellValue(row.getCell(13), dto));
							dto.setFebruary(getNumericCellValue(row.getCell(14), dto));
							dto.setMarch(getNumericCellValue(row.getCell(15), dto));
						}
					dto.setUOM(getStringCellValue(row.getCell(2), dto));

					dto.setFinancialYear(year);

					if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("Chemical") || vertical.getName().equalsIgnoreCase("PTA")) {
						dto.setWtAverage(getNumericCellValue(row.getCell(16), dto));
						dto.setRemarks(getStringCellValue(row.getCell(17), dto));
						dto.setId(getStringCellValue(row.getCell(18), dto));
					} else {
						dto.setRemarks(getStringCellValue(row.getCell(16), dto));
						dto.setId(getStringCellValue(row.getCell(17), dto));
					}

				} catch (Exception e) {
						e.printStackTrace();
						dto.setErrDescription(e.getMessage());
						dto.setSaveStatus("Failed");
					}

					configList.add(dto);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return configList;
	}

	private List<ValidationErrorDTO> validateGradeNorms(
	        List<MCUNormsValueDTO> mCUNormsValueDTOList,
	        Plants plant, Verticals vertical, Sites site, String year) throws Exception {

	    List<ValidationErrorDTO> list = new ArrayList<>();

	    boolean hasGradeData = mCUNormsValueDTOList.stream()
	            .anyMatch(d -> d.getGradeId() != null && !d.getGradeId().trim().isEmpty());

	    boolean isStapleWithGrade = vertical.getName().equalsIgnoreCase("STAPLE") && hasGradeData;
	    boolean isFilamentWithGrade = vertical.getName().equalsIgnoreCase("FILAMENT") && hasGradeData;

	  

	    if (!isStapleWithGrade && !isFilamentWithGrade) {
	        System.out.println("DEBUG -> validation SKIPPED, condition false hai");
	        return list;
	    }

	    String procedureName = vertical.getName() + "_" + site.getName() + "_GradeValidation";
	  

	    String sql = "EXEC " + "[" + procedureName + "]" +
	            " @plantId = :plantId," +
	            " @siteId = :siteId," +
	            " @verticalId = :verticalId," +
	            " @finYear = :finYear," +
	            " @GradeInputJson = :gradeInputJson";

	    ObjectMapper objectMapper = new ObjectMapper();
	    String gradeInputJson = objectMapper.writeValueAsString(mCUNormsValueDTOList);

	    Query query = entityManager.createNativeQuery(sql);
	    query.setParameter("plantId", plant.getId());
	    query.setParameter("siteId", site.getId());
	    query.setParameter("verticalId", vertical.getId());
	    query.setParameter("finYear", year);
	    query.setParameter("gradeInputJson", gradeInputJson);

	    @SuppressWarnings("unchecked")
	    List<Object[]> results = query.getResultList();

	    for (Object[] row : results) {
	        ValidationErrorDTO dto = new ValidationErrorDTO();
	        dto.setMaterialTypeId(toStringOrEmpty(row, 0));
	        dto.setMaterialTypeName(toStringOrEmpty(row, 1));
	        dto.setMaterialId(toStringOrEmpty(row, 2));
	        dto.setMaterialName(toStringOrEmpty(row, 3));
	        dto.setUom(toStringOrEmpty(row, 4));
	        dto.setMonth(toStringOrEmpty(row, 5));
	        dto.setYear(year);
	        dto.setExpectedValue(toDouble(row, 6));
	        dto.setActualValue(toDouble(row, 7));
	        dto.setDifference(toDouble(row, 8));
	        dto.setMatchStatus(toStringOrEmpty(row, 9));
	        dto.setEnteredValue(toDouble(row, 7));
	        dto.setSuggestedValue(0);
	        list.add(dto);
	    }

	    return list;
	}
	@Override
	public AOPMessageVM importExcelPolyester(String year, UUID plantFKId, String gradeId, MultipartFile file, String mode) {
	    try {
	        Plants plant = plantsRepository.findById(plantFKId).get();
	        List<MCUNormsValueDTO> data = null;
	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
	        Sites site = siteRepository.findById(plant.getSiteFkId()).get();

	        boolean isStapleWithGrade = vertical.getName().equalsIgnoreCase("STAPLE")
	                && gradeId != null && !gradeId.trim().isEmpty();
	        boolean isFilamentWithGrade = vertical.getName().equalsIgnoreCase("FILAMENT")
	                && gradeId != null && !gradeId.trim().isEmpty();
	        boolean pvc = vertical.getName().equalsIgnoreCase("PVC") && (site.getName().equalsIgnoreCase("VMD")
	                || site.getName().equalsIgnoreCase("DMD") || site.getName().equalsIgnoreCase("HMD"));

	        if (vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")
	                || vertical.getName().equalsIgnoreCase("PET")
	                || (vertical.getName().equalsIgnoreCase("STAPLE") && gradeId != null && !gradeId.trim().isEmpty())
	                || pvc) {
	            data = readSteadyState(file.getInputStream(), plantFKId, year);
	        }else if(vertical.getName().equalsIgnoreCase("STAPLE") || vertical.getName().equalsIgnoreCase("Filament")) {
	        	data = readConfigurationsSAP(file.getInputStream(), plantFKId, year);
	        } else {
	            data = readConfigurations(file.getInputStream(), plantFKId, year);
	        }
	        boolean hasGradeData = data.stream()
	                .anyMatch(d -> d.getGradeId() != null && !d.getGradeId().trim().isEmpty());
	 
	        List<ValidationErrorDTO> gradeValidationErrors =
	                validateGradeNorms(data, plant, vertical, site, year);

	        if (!gradeValidationErrors.isEmpty()) {
	            System.out.println("[STOP] Grade validation failed on import -> save aborted.");
	            AOPMessageVM aopMessageVM = new AOPMessageVM();
	            aopMessageVM.setCode(400);
	            aopMessageVM.setMessage("Validation Failed");
	            aopMessageVM.setData(gradeValidationErrors);
	            return aopMessageVM;
	        }

	        List<MCUNormsValueDTO> failedRecords = saveNormalOperationNormsData(data, plantFKId, year, gradeId, true);
	        AOPMessageVM aopMessageVM = new AOPMessageVM();
	        if (failedRecords != null && failedRecords.size() > 0) {
	            byte[] fileByteArray = null;
	            if (vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP")
	                    || vertical.getName().equalsIgnoreCase("PET")
	                    || (vertical.getName().equalsIgnoreCase("STAPLE") && gradeId != null && !gradeId.trim().isEmpty())
	                    || pvc) {
	                fileByteArray = exportSteadyStateNorms(year, plantFKId, true, failedRecords, mode);
	            }else if(vertical.getName().equalsIgnoreCase("STAPLE") || vertical.getName().equalsIgnoreCase("Filament")) {
	            	fileByteArray = createExcelSAP(year, plantFKId, true, failedRecords, mode, gradeId);
	            }else {
	                fileByteArray = createExcel(year, plantFKId, true, failedRecords, mode, gradeId);
	            }
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
	
	@Override
	public AOPMessageVM checkAllGradeNormsPolyester(UUID plantFKId, String year, String gradeId) {
	    try {
	        Plants plant = plantsRepository.findById(plantFKId).get();
	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
	        Sites site = siteRepository.findById(plant.getSiteFkId()).get();

	        boolean isStapleWithGrade = vertical.getName().equalsIgnoreCase("STAPLE")
	                && gradeId != null && !gradeId.trim().isEmpty();
	        boolean isFilamentWithGrade = vertical.getName().equalsIgnoreCase("FILAMENT")
	                && gradeId != null && !gradeId.trim().isEmpty();

	        if (!isStapleWithGrade && !isFilamentWithGrade) {
	            return AOPMessageVM.builder().code(200).message("No validation applicable").data(null).build();
	        }

	      
	        List<MCUNormsValueDTO> savedDataList =
	                buildDTOListFromSavedGradeData(plantFKId, site.getId(), vertical.getId(), gradeId, year);

	        if (savedDataList.isEmpty()) {
	            return AOPMessageVM.builder().code(200).message("No saved grade data found").data(null).build();
	        }

	     
	        List<ValidationErrorDTO> gradeValidationErrors =
	                validateGradeNorms(savedDataList, plant, vertical, site, year);

	        AOPMessageVM aopMessageVM = new AOPMessageVM();
	        if (!gradeValidationErrors.isEmpty()) {
	            aopMessageVM.setCode(400);
	            aopMessageVM.setMessage("Validation Failed");
	            aopMessageVM.setData(gradeValidationErrors);
	        } else {
	            aopMessageVM.setCode(200);
	            aopMessageVM.setMessage("All Matched");
	        }
	        return aopMessageVM;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return AOPMessageVM.builder().code(500).message("Error while checking norms").build();
	    }
	}
	
	
	@Override
	public AOPMessageVM importChemicalExcel(String year, UUID plantFKId, MultipartFile file) {
		// TODO Auto-generated method stub
		try {
			
			List<MCUNormsValueDTO>	data = readSteadyStateChemical(file.getInputStream(), plantFKId, year);
			List<MCUNormsValueDTO> failedRecords = saveNormalOperationNormsData(data, plantFKId, year, null, true);

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = null;
				fileByteArray = exportSteadyStateNormsChemical(year, plantFKId, true, failedRecords);
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

	private Map<String, String> getGradeNameIdMap(String year, UUID plantFKId) {
		AOPMessageVM gradesVM = getNormalOperationNormsGrades(year, plantFKId.toString());
		List<Map<String, String>> gradeInfoList = extractGradeInfo(gradesVM); // The method you modified earlier

		Map<String, String> nameIdMap = new HashMap<>();
		for (Map<String, String> info : gradeInfoList) {
			String sanitizedName = Utility.sanitizeSheetName(info.get("displayName"));
			nameIdMap.put(sanitizedName, info.get("gradeId"));
		}
		return nameIdMap;
	}
	private Map<String, String> getMaterialNameIdMap(UUID plantFKId) {
	    Map<String, String> materialMap = new HashMap<>();
	    try {
	        Plants plant = plantsRepository.findById(plantFKId).get();
	        
	        List<NormParameters> normParametersList =
	                normParametersRepository.findByPlantFkId(plantFKId);
	      

	        for (NormParameters np : normParametersList) {
	            if (np.getDisplayName() != null) {
	                materialMap.put(
	                        Utility.sanitizeSheetName(np.getDisplayName()),
	                        np.getId().toString()
	                );
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return materialMap;
	}

	public List<MCUNormsValueDTO> readSteadyState(InputStream inputStream, UUID plantFKId, String year) {
		List<MCUNormsValueDTO> configList = new ArrayList<>();
		Map<String, String> gradeMap = getGradeNameIdMap(year, plantFKId);
		  Map<String, String> materialMap = getMaterialNameIdMap(plantFKId); 
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {

			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				Sheet sheet = workbook.getSheetAt(i);
				if (sheet == null) {
					continue;
				}
				String sheetName = sheet.getSheetName();
				String gradeId = gradeMap.get(Utility.sanitizeSheetName(sheetName));

				Iterator<Row> rowIterator = sheet.iterator();
				if (rowIterator.hasNext()) {
					rowIterator.next();
				}
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					if (row.getPhysicalNumberOfCells() == 0) {
						continue;
					}

					MCUNormsValueDTO dto = new MCUNormsValueDTO();
					try {
						dto.setNormParameterTypeDisplayName(getStringCellValue(row.getCell(0), dto));
						dto.setProductName(getStringCellValue(row.getCell(1), dto));
						
						 String productName = getStringCellValue(row.getCell(1), dto);
						  String materialFkId = materialMap.get(Utility.sanitizeSheetName(productName));
		                    dto.setProductName(productName);
		                    dto.setMaterialFkId(materialFkId);

						dto.setUOM(getStringCellValue(row.getCell(2), dto));

						dto.setFinancialYear(year);
						dto.setPlantFkId(plantFKId.toString());
						dto.setApril(getNumericCellValue(row.getCell(3), dto));
						dto.setMay(getNumericCellValue(row.getCell(4), dto));
						dto.setJune(getNumericCellValue(row.getCell(5), dto));
						dto.setJuly(getNumericCellValue(row.getCell(6), dto));
						dto.setAugust(getNumericCellValue(row.getCell(7), dto));
						dto.setSeptember(getNumericCellValue(row.getCell(8), dto));
						dto.setOctober(getNumericCellValue(row.getCell(9), dto));
						dto.setNovember(getNumericCellValue(row.getCell(10), dto));
						dto.setDecember(getNumericCellValue(row.getCell(11), dto));
						dto.setJanuary(getNumericCellValue(row.getCell(12), dto));
						dto.setFebruary(getNumericCellValue(row.getCell(13), dto));
						dto.setMarch(getNumericCellValue(row.getCell(14), dto));
						dto.setRemarks(getStringCellValue(row.getCell(15), dto));
						dto.setId(getStringCellValue(row.getCell(16), dto));
						dto.setGradeId(gradeId);

					} catch (Exception e) {
						e.printStackTrace();
						dto.setErrDescription(e.getMessage());
						dto.setSaveStatus("Failed");
					}
					configList.add(dto);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return configList;
	}

	public List<MCUNormsValueDTO> readConfigurations(InputStream inputStream, UUID plantFKId, String year) {
		List<MCUNormsValueDTO> configList = new ArrayList<>();
		List<MCUNormsValueDTO> ambientEthane = new ArrayList<>();
		List<MCUNormsValueDTO> hydrogen = new ArrayList<>();
		List<MCUNormsValueDTO> naturalGas = new ArrayList<>();
		List<Object[]> obj = null;
		Plants plant = plantsRepository.findById(plantFKId).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
				Sheet sheet = workbook.getSheetAt(sheetIndex);
				Iterator<Row> rowIterator = sheet.iterator();

				if (rowIterator.hasNext())
					rowIterator.next();

				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					MCUNormsValueDTO dto = new MCUNormsValueDTO();

					try {
						dto.setNormParameterTypeDisplayName(getStringCellValue(row.getCell(0), dto));
						dto.setProductName(getStringCellValue(row.getCell(1), dto));

						// if (dto.getProductName().equalsIgnoreCase("Total Fuel")) {
						if ("Total Fuel".equalsIgnoreCase(dto.getProductName())) {
							// calculateTotalFuel(dto, hydrogen, ambientEthane,naturalGas, vertical,
							// plantFKId, year);
						} else {
							dto.setApril(getNumericCellValue(row.getCell(3), dto));
							dto.setMay(getNumericCellValue(row.getCell(4), dto));
							dto.setJune(getNumericCellValue(row.getCell(5), dto));
							dto.setJuly(getNumericCellValue(row.getCell(6), dto));
							dto.setAugust(getNumericCellValue(row.getCell(7), dto));
							dto.setSeptember(getNumericCellValue(row.getCell(8), dto));
							dto.setOctober(getNumericCellValue(row.getCell(9), dto));
							dto.setNovember(getNumericCellValue(row.getCell(10), dto));
							dto.setDecember(getNumericCellValue(row.getCell(11), dto));
							dto.setJanuary(getNumericCellValue(row.getCell(12), dto));
							dto.setFebruary(getNumericCellValue(row.getCell(13), dto));
							dto.setMarch(getNumericCellValue(row.getCell(14), dto));

						}
						dto.setUOM(getStringCellValue(row.getCell(2), dto));

						dto.setFinancialYear(year);

						if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("Chemical") || vertical.getName().equalsIgnoreCase("PTA")) {
							dto.setWtAverage(getNumericCellValue(row.getCell(15), dto));
							dto.setRemarks(getStringCellValue(row.getCell(16), dto));
							dto.setId(getStringCellValue(row.getCell(17), dto));
						} else {
							dto.setRemarks(getStringCellValue(row.getCell(15), dto));
							dto.setId(getStringCellValue(row.getCell(16), dto));
						}
						// if (dto.getProductName().equalsIgnoreCase("AMBIENT ETHANE")) {
						// ambientEthane.add(dto);
						// }
						// if (dto.getProductName().equalsIgnoreCase("Hydrogen")
						// && dto.getNormParameterTypeDisplayName().equalsIgnoreCase("Utility
						// Consumption")) {
						// hydrogen.add(dto);
						// }
						// if (dto.getProductName().equalsIgnoreCase("NATURAL GAS")) {
						// naturalGas.add(dto);
						// }

					} catch (Exception e) {
						e.printStackTrace();
						dto.setErrDescription(e.getMessage());
						dto.setSaveStatus("Failed");
					}

					configList.add(dto);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return configList;
	}

	public List<MCUNormsValueDTO> readConfigurationsSAP(InputStream inputStream, UUID plantFKId, String year) {
		List<MCUNormsValueDTO> configList = new ArrayList<>();
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
				Sheet sheet = workbook.getSheetAt(sheetIndex);
				Iterator<Row> rowIterator = sheet.iterator();

				if (rowIterator.hasNext())
					rowIterator.next();

				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					MCUNormsValueDTO dto = new MCUNormsValueDTO();

					try {
						dto.setNormParameterTypeDisplayName(getStringCellValue(row.getCell(0), dto));
						dto.setSapCode(getStringCellValue(row.getCell(1), dto));
						dto.setProductName(getStringCellValue(row.getCell(2), dto));
							dto.setApril(getNumericCellValue(row.getCell(4), dto));
							dto.setMay(getNumericCellValue(row.getCell(5), dto));
							dto.setJune(getNumericCellValue(row.getCell(6), dto));
							dto.setJuly(getNumericCellValue(row.getCell(7), dto));
							dto.setAugust(getNumericCellValue(row.getCell(8), dto));
							dto.setSeptember(getNumericCellValue(row.getCell(9), dto));
							dto.setOctober(getNumericCellValue(row.getCell(10), dto));
							dto.setNovember(getNumericCellValue(row.getCell(11), dto));
							dto.setDecember(getNumericCellValue(row.getCell(12), dto));
							dto.setJanuary(getNumericCellValue(row.getCell(13), dto));
							dto.setFebruary(getNumericCellValue(row.getCell(14), dto));
							dto.setMarch(getNumericCellValue(row.getCell(15), dto));
						dto.setUOM(getStringCellValue(row.getCell(3), dto));
						dto.setFinancialYear(year);
						dto.setRemarks(getStringCellValue(row.getCell(16), dto));
						dto.setId(getStringCellValue(row.getCell(17), dto));
						
					} catch (Exception e) {
						e.printStackTrace();
						dto.setErrDescription(e.getMessage());
						dto.setSaveStatus("Failed");
					}

				configList.add(dto);
			}
		}

		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return configList;
	}


	public List<MCUNormsValueDTO> readConfigurationsSAPMerox(InputStream inputStream, UUID plantFKId, String year) {
		List<MCUNormsValueDTO> configList = new ArrayList<>();
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
				Sheet sheet = workbook.getSheetAt(sheetIndex);
				Iterator<Row> rowIterator = sheet.iterator();

				if (rowIterator.hasNext())
					rowIterator.next(); // skip header row

				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					MCUNormsValueDTO dto = new MCUNormsValueDTO();

					try {
						dto.setNormParameterTypeDisplayName(getStringCellValue(row.getCell(0), dto));
						dto.setSapCode(getStringCellValue(row.getCell(1), dto));
						dto.setProductName(getStringCellValue(row.getCell(2), dto));
						dto.setUOM(getStringCellValue(row.getCell(3), dto));
						// Col 4 is Last FY — read-only reference data, intentionally skipped
						dto.setApril(getNumericCellValue(row.getCell(5), dto));
						dto.setMay(getNumericCellValue(row.getCell(6), dto));
						dto.setJune(getNumericCellValue(row.getCell(7), dto));
						dto.setJuly(getNumericCellValue(row.getCell(8), dto));
						dto.setAugust(getNumericCellValue(row.getCell(9), dto));
						dto.setSeptember(getNumericCellValue(row.getCell(10), dto));
						dto.setOctober(getNumericCellValue(row.getCell(11), dto));
						dto.setNovember(getNumericCellValue(row.getCell(12), dto));
						dto.setDecember(getNumericCellValue(row.getCell(13), dto));
						dto.setJanuary(getNumericCellValue(row.getCell(14), dto));
						dto.setFebruary(getNumericCellValue(row.getCell(15), dto));
						dto.setMarch(getNumericCellValue(row.getCell(16), dto));
						dto.setFinancialYear(year);
						dto.setRemarks(getStringCellValue(row.getCell(17), dto));
						dto.setId(getStringCellValue(row.getCell(18), dto));

					} catch (Exception e) {
						e.printStackTrace();
						dto.setErrDescription(e.getMessage());
						dto.setSaveStatus("Failed");
					}

					configList.add(dto);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return configList;
	}


	private String getPreviousFinancialYear(String year) {
		try {
			String[] parts = year.split("-");
			int startYear = Integer.parseInt(parts[0]);
			int prevStartYear = startYear - 1;
			String prevEndYearStr = String.format("%02d", startYear % 100);
			return prevStartYear + "-" + prevEndYearStr;
		} catch (Exception e) {
			return year; // fallback to current year if parsing fails
		}
	}

	public List<MCUNormsValueDTO> readSteadyStateChemical(InputStream inputStream, UUID plantFKId, String year) {
		List<MCUNormsValueDTO> configList = new ArrayList<>();
		
		Plants plant = plantsRepository.findById(plantFKId).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();

			if (rowIterator.hasNext())
				rowIterator.next();

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				MCUNormsValueDTO dto = new MCUNormsValueDTO();

				try {
					dto.setSapCode(getStringCellValue(row.getCell(0), dto));
					dto.setProductName(getStringCellValue(row.getCell(1), dto));

					
						dto.setApril(getNumericCellValue(row.getCell(3), dto));
						dto.setMay(getNumericCellValue(row.getCell(4), dto));
						dto.setJune(getNumericCellValue(row.getCell(5), dto));
						dto.setJuly(getNumericCellValue(row.getCell(6), dto));
						dto.setAugust(getNumericCellValue(row.getCell(7), dto));
						dto.setSeptember(getNumericCellValue(row.getCell(8), dto));
						dto.setOctober(getNumericCellValue(row.getCell(9), dto));
						dto.setNovember(getNumericCellValue(row.getCell(10), dto));
						dto.setDecember(getNumericCellValue(row.getCell(11), dto));
						dto.setJanuary(getNumericCellValue(row.getCell(12), dto));
						dto.setFebruary(getNumericCellValue(row.getCell(13), dto));
						dto.setMarch(getNumericCellValue(row.getCell(14), dto));

					
					dto.setUOM(getStringCellValue(row.getCell(2), dto));

					dto.setFinancialYear(year);

					
						dto.setWtAverage(getNumericCellValue(row.getCell(15), dto));
						dto.setRemarks(getStringCellValue(row.getCell(16), dto));
						dto.setId(getStringCellValue(row.getCell(17), dto));
					
				} catch (Exception e) {
					e.printStackTrace();
					dto.setErrDescription(e.getMessage());
					dto.setSaveStatus("Failed");
				}

				configList.add(dto);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return configList;
	}

	private void calculateTotalFuel(MCUNormsValueDTO dto, List<MCUNormsValueDTO> hydrogen,
	        List<MCUNormsValueDTO> ambientEthane, List<MCUNormsValueDTO> naturalGas, Verticals vertical, UUID plantFKId, String year) {

	    if (dto == null || hydrogen == null || hydrogen.isEmpty() || 
	        ambientEthane == null || ambientEthane.isEmpty() || 
	        naturalGas == null || naturalGas.isEmpty() || vertical == null) {
	        return;
	    }

	    String verticalName = vertical.getName() != null ? vertical.getName() : "Unknown";
	    String procedureName = verticalName + "_GetConfiguration_Constant";
	    Map<String, Double> constants = getConstantsMap(year, plantFKId != null ? plantFKId.toString() : "", procedureName);

	    double h2Const = (constants != null && constants.get("H2") != null) ? constants.get("H2") : 0.0;
	    double ethConst = (constants != null && constants.get("Ethane") != null) ? constants.get("Ethane") : 0.0;

	    List<Object[]> obj = aopRepository.findByAOPYearAndPlantFkId(year, plantFKId, "Production");
	    List<AOPDTO> prodList = getMonthlyProduction(obj);

	    if (prodList == null || prodList.isEmpty() || prodList.get(0) == null) {
	        return;
	    }

	    AOPDTO prod = prodList.get(0);
	    MCUNormsValueDTO h2Data = hydrogen.get(0);
	    MCUNormsValueDTO ethData = ambientEthane.get(0);
	    MCUNormsValueDTO ngData = naturalGas.get(0);
	    dto.setApril(calculateFuelFormula(h2Data.getApril(), ethData.getApril(), ngData.getApril(), prod.getApril(), h2Const, ethConst));
	    dto.setMay(calculateFuelFormula(h2Data.getMay(), ethData.getMay(), ngData.getMay(), prod.getMay(), h2Const, ethConst));
	    dto.setJune(calculateFuelFormula(h2Data.getJune(), ethData.getJune(), ngData.getJune(), prod.getJune(), h2Const, ethConst));
	    dto.setJuly(calculateFuelFormula(h2Data.getJuly(), ethData.getJuly(), ngData.getJuly(), prod.getJuly(), h2Const, ethConst));
	    dto.setAugust(calculateFuelFormula(h2Data.getAugust(), ethData.getAugust(), ngData.getAugust(), prod.getAug(), h2Const, ethConst));
	    dto.setSeptember(calculateFuelFormula(h2Data.getSeptember(), ethData.getSeptember(), ngData.getSeptember(), prod.getSep(), h2Const, ethConst));
	    dto.setOctober(calculateFuelFormula(h2Data.getOctober(), ethData.getOctober(), ngData.getOctober(), prod.getOct(), h2Const, ethConst));
	    dto.setNovember(calculateFuelFormula(h2Data.getNovember(), ethData.getNovember(), ngData.getNovember(), prod.getNov(), h2Const, ethConst));
	    dto.setDecember(calculateFuelFormula(h2Data.getDecember(), ethData.getDecember(), ngData.getDecember(), prod.getDec(), h2Const, ethConst));
	    dto.setJanuary(calculateFuelFormula(h2Data.getJanuary(), ethData.getJanuary(), ngData.getJanuary(), prod.getJan(), h2Const, ethConst));
	    dto.setFebruary(calculateFuelFormula(h2Data.getFebruary(), ethData.getFebruary(), ngData.getFebruary(), prod.getFeb(), h2Const, ethConst));
	    dto.setMarch(calculateFuelFormula(h2Data.getMarch(), ethData.getMarch(), ngData.getMarch(), prod.getMarch(), h2Const, ethConst));
	}

	private Double calculateFuelFormula(Double h2Norm, Double ethNorm, Double ngGBT, Double prodVal, double h2CV, double ethCV) {
	    double production = val(prodVal);
	    if (production == 0) return 0.0; 
	    double divisor = 1000000.0;
	    double A = (val(ethNorm) * production * 1000.0 * ethCV * 4.186 * 1.055) / divisor;
	    
	    double B = (val(h2Norm) * production * 1000.0 * h2CV * 4.186 * 1.055) / divisor;
	    
	    double C = val(ngGBT);

	    return (A + B + C) / production;
	}

	private double val(Double value) {
		return value == null ? 0.0 : value;
	}

	public List<AOPDTO> getMonthlyProduction(List<Object[]> obj) {
		List<AOPDTO> aopDTOList = new ArrayList<>();
		for (Object[] row : obj) {
			AOPDTO aopDTO = new AOPDTO();

			aopDTO.setId(row[0] != null ? row[0].toString() : null);
			aopDTO.setNormParameterName(row[1] != null ? row[1].toString() : null);
			aopDTO.setNormParameterDisplayName(row[2] != null ? row[2].toString() : null);
			aopDTO.setNormParameterTypeId(row[3] != null ? row[3].toString() : null);
			aopDTO.setMaterialFKId(row[4] != null ? row[4].toString() : null);
			aopDTO.setDisplayName(row[5] != null ? row[5].toString() : null);

			aopDTO.setApril(safeParseDouble(row[6]));
			aopDTO.setMay(safeParseDouble(row[7]));
			aopDTO.setJune(safeParseDouble(row[8]));
			aopDTO.setJuly(safeParseDouble(row[9]));
			aopDTO.setAug(safeParseDouble(row[10]));
			aopDTO.setSep(safeParseDouble(row[11]));
			aopDTO.setOct(safeParseDouble(row[12]));
			aopDTO.setNov(safeParseDouble(row[13]));
			aopDTO.setDec(safeParseDouble(row[14]));
			aopDTO.setJan(safeParseDouble(row[15]));
			aopDTO.setFeb(safeParseDouble(row[16]));
			aopDTO.setMarch(safeParseDouble(row[17]));
			aopDTO.setAvgTPH(safeParseDouble(row[18]));
			aopDTO.setRemark(row[19] != null ? row[19].toString() : null);
			aopDTO.setDisplayOrder(row[20] != null ? Integer.valueOf(row[20].toString()) : null);
			aopDTO.setIsEditable(row[21] != null ? Boolean.valueOf(row[21].toString()) : null);
			aopDTO.setIsVisible(row[22] != null ? Boolean.valueOf(row[22].toString()) : null);

			aopDTOList.add(aopDTO);
		}
		return aopDTOList;

	}

	private Double safeParseDouble(Object obj) {
		if (obj == null) {
			return null;
		}
		String s = obj.toString().trim();
		if (s.isEmpty()) {
			return null;
		}
		try {
			return Double.valueOf(s);
		} catch (NumberFormatException ex) {
			// Logging is optional but helpful to track bad data
			System.err.println("Warning: could not parse to Double: '" + s + "'");
			return null;
		}
	}

	public Map<String, Double> getConstantsMap(String aopYear, String plantId, String procedure) {
		Map<String, Double> constantsMap = new HashMap<>();
		List<Object[]> obj = findConstantsByYearAndPlantFkId(aopYear, plantId, procedure);

		for (Object[] row : obj) {
			String displayName = (row[3] != null) ? row[3].toString() : null;
			if (displayName != null) {
				Double value = (row[5] != null) ? Double.parseDouble(row[5].toString()) : 0.0;
				constantsMap.put(displayName, value);
			}
		}
		return constantsMap;
	}

	public List<Object[]> findConstantsByYearAndPlantFkId(String aopYear, String plantId, String procedureName) {
		try {
			String sql = "EXEC " + "[" + procedureName + "]" + " @plantId = :plantId, @aopYear = :aopYear";

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

	private static String getStringCellValue(Cell cell, MCUNormsValueDTO dto) {
		try {

			if (cell == null || cell.getCellType() == CellType.BLANK) {
				return null;
			}

			String value;
			if (cell.getCellType() == CellType.STRING) {
				value = cell.getStringCellValue();
			} else {

				cell.setCellType(CellType.STRING);
				value = cell.getStringCellValue();
			}

			if (value == null || value.trim().isEmpty()) {
				return null;
			}

			return value.trim();

		} catch (Exception e) {
			dto.setSaveStatus("Failed");
			dto.setErrDescription("Error reading string value");
			e.printStackTrace();
		}
		return null;
	}

	private static Double getNumericCellValue(Cell cell, MCUNormsValueDTO dto) {

		if (cell == null || cell.getCellType() == CellType.BLANK) {
			return null;
		}

		if (cell.getCellType() == CellType.NUMERIC) {
			return cell.getNumericCellValue();
		}

		if (cell.getCellType() == CellType.STRING) {
			String cellValue = cell.getStringCellValue().trim();
			if (cellValue.isEmpty()) {
				return null;
			}

			try {
				return Double.parseDouble(cellValue);
			} catch (NumberFormatException e) {
				dto.setSaveStatus("Failed");
				dto.setErrDescription("Invalid number format: " + cellValue);
				return null;
			}
		}
		if (cell.getCellType() == CellType.FORMULA) {
			try {

				return cell.getNumericCellValue();
			} catch (Exception e) {
				return null;
			}
		}

		return null;
	}

	public static Boolean getBooleanCellValue(Cell cell, MCUNormsValueDTO dto) {
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

	public List<Map<String, String>> extractGradeInfo(AOPMessageVM grades) {
		List<Map<String, String>> gradeInfoList = new ArrayList<>();

		Object data = grades.getData();

		if (data instanceof List) {
			try {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> gradeList = (List<Map<String, Object>>) data;

				for (Map<String, Object> gradeMap : gradeList) {
					Object gradeIdObj = gradeMap.get("gradeId");
					Object displayNameObj = gradeMap.get("displayName");

					if (gradeIdObj != null && displayNameObj != null) {
						Map<String, String> infoMap = new HashMap<>();
						infoMap.put("gradeId", gradeIdObj.toString());
						infoMap.put("displayName", displayNameObj.toString());
						gradeInfoList.add(infoMap);
					}
				}
			} catch (ClassCastException e) {
				System.err.println("Error casting data to List<Map<String, Object>>: " + e.getMessage());
			}
		}

		return gradeInfoList;
	}

	public byte[] exportSteadyStateNorms(String year, UUID plantFKId, boolean isAfterSave,
			List<MCUNormsValueDTO> dtoList, String mode) {
		try {
			AOPMessageVM gradesVM = getNormalOperationNormsGrades(year, plantFKId.toString());
			List<Map<String, String>> gradeInfoList = extractGradeInfo(gradesVM);
			Workbook workbook = new XSSFWorkbook();
			CellStyle lockedStyle = Utility.createLockedStyle(workbook);
			CellStyle unlockedStyle = Utility.createUnlockedStyle(workbook);

			CellStyle lockedWrappedStyle = workbook.createCellStyle();
			lockedWrappedStyle.cloneStyleFrom(lockedStyle);
			lockedWrappedStyle.setWrapText(true);

			CellStyle unlockedWrappedStyle = workbook.createCellStyle();
			unlockedWrappedStyle.cloneStyleFrom(unlockedStyle);
			unlockedWrappedStyle.setWrapText(true);

			for (Map<String, String> gradeInfo : gradeInfoList) {

				String currentGradeId = gradeInfo.get("gradeId");
				String sheetName = Utility.sanitizeSheetName(gradeInfo.get("displayName"));

				AOPMessageVM aopMessageVM = null;
				List<MCUNormsValueDTO> currentDtoList = new ArrayList<>();
				List<Boolean> isEditable = new ArrayList<>();
				if (!isAfterSave) {
					aopMessageVM = getNormalOperationNormsData(year, plantFKId.toString(), currentGradeId, mode);
				}
				if (aopMessageVM != null && aopMessageVM.getData() != null) {

					Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
					currentDtoList = (List<MCUNormsValueDTO>) responseMap.get("mcuNormsValueDTOList");
				} else if (isAfterSave) {
					currentDtoList = dtoList.stream().filter(dto -> currentGradeId.equals(dto.getGradeId()))
							.collect(Collectors.toList());
				} else {
					continue;
				}

			Sheet sheet = workbook.createSheet(sheetName);
			// CRITICAL: Sheet protection must be enabled for cell-level locking to take effect in Excel
			sheet.protectSheet("secret_password");
			int currentRow = 0;

			List<List<Object>> rows = new ArrayList<>();
			for (MCUNormsValueDTO dto : currentDtoList) {
					List<Object> list = new ArrayList<>();
					list.add(dto.getNormParameterTypeDisplayName());
					list.add(dto.getProductName());
					list.add(dto.getUOM());
					list.add(dto.getApril());
					list.add(dto.getMay());
					list.add(dto.getJune());
					list.add(dto.getJuly());
					list.add(dto.getAugust());
					list.add(dto.getSeptember());
					list.add(dto.getOctober());
					list.add(dto.getNovember());
					list.add(dto.getDecember());
					list.add(dto.getJanuary());
					list.add(dto.getFebruary());
					list.add(dto.getMarch());
					list.add(dto.getRemarks());
					list.add(dto.getId());
					isEditable.add(dto.getIsEditable());

					if (isAfterSave) {
						list.add(dto.getSaveStatus());
						list.add(dto.getErrDescription());
					}
					rows.add(list);
				}

				List<String> innerHeaders = new ArrayList<>();
				innerHeaders.add("Type");
				innerHeaders.add("Particulars");
				innerHeaders.add("UOM");
				List<String> monthsList = getAcademicYearMonths(year);
				innerHeaders.addAll(monthsList);
				innerHeaders.add("Remarks");
				innerHeaders.add("Id");
				if (isAfterSave) {
					innerHeaders.add("Status");
					innerHeaders.add("Error Description");
				}
				List<List<String>> headers = new ArrayList<>();
				headers.add(innerHeaders);

				int remarksColIndex = innerHeaders.indexOf("Remarks");
				int idColIndex = innerHeaders.indexOf("Id");

				for (List<String> headerRowData : headers) {
					Row headerRow = sheet.createRow(currentRow++);
					for (int col = 0; col < headerRowData.size(); col++) {
						Cell cell = headerRow.createCell(col);
						cell.setCellValue(headerRowData.get(col));
						cell.setCellStyle(Utility.createBoldBorderedStyle(workbook));
					}
				}

				for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
					List<Object> rowData = rows.get(rowIndex);
					boolean isRowEditable = true;

					if (rowIndex < isEditable.size() && isEditable.get(rowIndex) != null) {
						isRowEditable = isEditable.get(rowIndex);
					}

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

					if (col == remarksColIndex) {
						cell.setCellStyle(isRowEditable ? unlockedWrappedStyle : lockedWrappedStyle);
					} else if (isRowEditable) {
						cell.setCellStyle(unlockedStyle);
					} else {
						cell.setCellStyle(lockedStyle);
					}
				}

				// Auto-adjust row height to accommodate wrapped Remarks text
				if (remarksColIndex >= 0 && remarksColIndex < rowData.size()) {
					Object remarksValue = rowData.get(remarksColIndex);
					if (remarksValue != null && !remarksValue.toString().isEmpty()) {
						String remarksText = remarksValue.toString();
						int charsPerLine = 55; // approximate characters fitting the fixed Remarks column width
						int lines = (int) Math.ceil((double) remarksText.length() / charsPerLine);
						lines = Math.max(1, lines);
						row.setHeight((short) (lines * 300)); // 300 twips ≈ 15 pt per line
					}
				}
			}

			// Auto-size content columns; give Remarks a fixed wide width with wrapping
			int totalCols = innerHeaders.size();
			for (int col = 0; col < totalCols; col++) {
				if (col == remarksColIndex) {
					sheet.setColumnWidth(col, 15000); // ~55 characters wide
				} else if (col == idColIndex) {
					sheet.setColumnHidden(col, true);
				} else {
					sheet.autoSizeColumn(col);
				}
			}

			}

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

	public byte[] createExcel(String year, UUID plantFKId, boolean isAfterSave, List<MCUNormsValueDTO> dtoList,
			String mode, String gradeId) {
		try {
			AOPMessageVM aopMessageVM = getNormalOperationNormsData(year, plantFKId.toString(), gradeId, mode);
			List<Boolean> isEditable = new ArrayList<>();
			Plants plant = plantsRepository.findById(plantFKId).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();

			boolean ptaPmdPia = vertical.getName().equalsIgnoreCase("PTA") && site.getName().equalsIgnoreCase("PMD") && plant.getName().equalsIgnoreCase("PIA");

			if(ptaPmdPia){ 
				// seperate method to include sap code
				return createExcelWithSapCode(year, plantFKId, isAfterSave, dtoList, mode, gradeId);
			}

			if (!isAfterSave) {
				Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
				dtoList = (List<MCUNormsValueDTO>) responseMap.get("mcuNormsValueDTOList");
			}

			Workbook workbook = new XSSFWorkbook();

			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;

			sheet.protectSheet("secret_password");
			// List<List<Object>> rows = new ArrayList<>();

			List<List<Object>> rows = new ArrayList<>();

		// Create styles for locking/unlocking cells
		CellStyle lockedStyle = workbook.createCellStyle();
		lockedStyle.setLocked(true);
		lockedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		lockedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		lockedStyle.setBorderTop(BorderStyle.THIN);
		lockedStyle.setBorderBottom(BorderStyle.THIN);
		lockedStyle.setBorderLeft(BorderStyle.THIN);
		lockedStyle.setBorderRight(BorderStyle.THIN);

		CellStyle unlockedStyle = workbook.createCellStyle();
		unlockedStyle.setLocked(false);
		unlockedStyle.setBorderTop(BorderStyle.THIN);
		unlockedStyle.setBorderBottom(BorderStyle.THIN);
		unlockedStyle.setBorderLeft(BorderStyle.THIN);
		unlockedStyle.setBorderRight(BorderStyle.THIN);

		CellStyle remarksLockedStyle = workbook.createCellStyle();
		remarksLockedStyle.setLocked(true);
		remarksLockedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		remarksLockedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		remarksLockedStyle.setBorderTop(BorderStyle.THIN);
		remarksLockedStyle.setBorderBottom(BorderStyle.THIN);
		remarksLockedStyle.setBorderLeft(BorderStyle.THIN);
		remarksLockedStyle.setBorderRight(BorderStyle.THIN);
		remarksLockedStyle.setWrapText(true);

		CellStyle remarksUnlockedStyle = workbook.createCellStyle();
		remarksUnlockedStyle.setLocked(false);
		remarksUnlockedStyle.setBorderTop(BorderStyle.THIN);
		remarksUnlockedStyle.setBorderBottom(BorderStyle.THIN);
		remarksUnlockedStyle.setBorderLeft(BorderStyle.THIN);
		remarksUnlockedStyle.setBorderRight(BorderStyle.THIN);
		remarksUnlockedStyle.setWrapText(true);
			// Data rows
			for (MCUNormsValueDTO dto : dtoList) {
				// if (isAfterSave) {
				List<Object> list = new ArrayList<>();
				list.add(dto.getNormParameterTypeDisplayName());
				list.add(dto.getProductName());
				list.add(dto.getUOM());
				list.add(dto.getApril());
				list.add(dto.getMay());
				list.add(dto.getJune());
				list.add(dto.getJuly());
				list.add(dto.getAugust());
				list.add(dto.getSeptember());
				list.add(dto.getOctober());
				list.add(dto.getNovember());
				list.add(dto.getDecember());
				list.add(dto.getJanuary());
				list.add(dto.getFebruary());
				list.add(dto.getMarch());
				if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("Chemical") || vertical.getName().equalsIgnoreCase("PTA")) {
					list.add(dto.getWtAverage());
				}

				list.add(dto.getRemarks());
				list.add(dto.getId());
				isEditable.add(dto.getIsEditable());
				// list.add(dto.getMaterialFkId());
				// list.add(dto.getIsEditable());
				if (isAfterSave) {
					list.add(dto.getSaveStatus());
					list.add(dto.getErrDescription());
				}
				rows.add(list);
				// }
			}

			List<String> innerHeaders = new ArrayList<>();
			innerHeaders.add("Type");
			innerHeaders.add("Particulars");
			innerHeaders.add("UOM");
			List<String> monthsList = getAcademicYearMonths(year);
			innerHeaders.addAll(monthsList);
			if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("Chemical") || vertical.getName().equalsIgnoreCase("PTA")) {
				innerHeaders.add("Weighted Avg");
			}
			innerHeaders.add("Remarks");
			innerHeaders.add("Id");
			// innerHeaders.add("NormParamterId");
			// innerHeaders.add("IsEditable");
			if (isAfterSave) {
				innerHeaders.add("Status");
				innerHeaders.add("Error Description");
			}

			int remarksColIndex = innerHeaders.indexOf("Remarks");
			int idColIndex = innerHeaders.indexOf("Id");

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
				boolean isRowEditable = true;
				if (isEditable.get(currentRow - 1) != null) {
					isRowEditable = isEditable.get(currentRow - 1);
				}

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
					if (col == remarksColIndex) {
						cell.setCellStyle(isRowEditable ? remarksUnlockedStyle : remarksLockedStyle);
					} else if (isRowEditable) {
						cell.setCellStyle(unlockedStyle);
					} else {
						cell.setCellStyle(lockedStyle);
					}

				}
			}
			if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("Chemical") || vertical.getName().equalsIgnoreCase("PTA")) {
				sheet.setColumnHidden(17, true);
			} else {
				sheet.setColumnHidden(16, true);
			}

			// Auto-size all columns; give Remarks a fixed wide width with text wrapping
			int totalCols = innerHeaders.size();
			for (int col = 0; col < totalCols; col++) {
				if (col == idColIndex) {
					// already hidden above; skip width adjustment
				} else if (col == remarksColIndex) {
					sheet.setColumnWidth(col, 15000); // ~55 characters wide
				} else {
					sheet.autoSizeColumn(col);
				}
			}

			// Adjust row heights so wrapped Remarks content is fully visible
			for (int r = 1; r < currentRow; r++) {
				Row row = sheet.getRow(r);
				if (row == null) continue;
				Cell remarksCell = (remarksColIndex >= 0) ? row.getCell(remarksColIndex) : null;
				if (remarksCell != null) {
					String remarksText = remarksCell.getStringCellValue();
					if (remarksText != null && !remarksText.isEmpty()) {
						int charsPerLine = 55;
						int lines = (int) Math.ceil((double) remarksText.length() / charsPerLine);
						row.setHeight((short) (Math.max(1, lines) * 300)); // 300 twips ≈ 15 pt per line
					}
				}
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

	public byte[] createExcelSAP(String year, UUID plantFKId, boolean isAfterSave, List<MCUNormsValueDTO> dtoList,
			String mode, String gradeId) {
		try {
			AOPMessageVM aopMessageVM = getNormalOperationNormsData(year, plantFKId.toString(), gradeId, mode);
			List<Boolean> isEditable = new ArrayList<>();
			Plants plant = plantsRepository.findById(plantFKId).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			boolean polyester = vertical.getName().equalsIgnoreCase("Filament") || vertical.getName().equalsIgnoreCase("Staple");
			boolean merox = vertical.getName().equalsIgnoreCase("MEROX");
			if (!isAfterSave) {
				Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
				dtoList = (List<MCUNormsValueDTO>) responseMap.get("mcuNormsValueDTOList");
			}

			Workbook workbook = new XSSFWorkbook();

			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;

			sheet.protectSheet("secret_password");
			// List<List<Object>> rows = new ArrayList<>();

			List<List<Object>> rows = new ArrayList<>();

		// Create styles for locking/unlocking cells
		CellStyle lockedStyle = workbook.createCellStyle();
		lockedStyle.setLocked(true);
		lockedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		lockedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		lockedStyle.setBorderTop(BorderStyle.THIN);
		lockedStyle.setBorderBottom(BorderStyle.THIN);
		lockedStyle.setBorderLeft(BorderStyle.THIN);
		lockedStyle.setBorderRight(BorderStyle.THIN);

		CellStyle unlockedStyle = workbook.createCellStyle();
		unlockedStyle.setLocked(false);
		unlockedStyle.setBorderTop(BorderStyle.THIN);
		unlockedStyle.setBorderBottom(BorderStyle.THIN);
		unlockedStyle.setBorderLeft(BorderStyle.THIN);
		unlockedStyle.setBorderRight(BorderStyle.THIN);

		CellStyle remarksLockedStyle = workbook.createCellStyle();
		remarksLockedStyle.setLocked(true);
		remarksLockedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		remarksLockedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		remarksLockedStyle.setBorderTop(BorderStyle.THIN);
		remarksLockedStyle.setBorderBottom(BorderStyle.THIN);
		remarksLockedStyle.setBorderLeft(BorderStyle.THIN);
		remarksLockedStyle.setBorderRight(BorderStyle.THIN);
		remarksLockedStyle.setWrapText(true);

		CellStyle remarksUnlockedStyle = workbook.createCellStyle();
		remarksUnlockedStyle.setLocked(false);
		remarksUnlockedStyle.setBorderTop(BorderStyle.THIN);
		remarksUnlockedStyle.setBorderBottom(BorderStyle.THIN);
		remarksUnlockedStyle.setBorderLeft(BorderStyle.THIN);
		remarksUnlockedStyle.setBorderRight(BorderStyle.THIN);
		remarksUnlockedStyle.setWrapText(true);

		CellStyle lastFyLockedStyle = workbook.createCellStyle();
		lastFyLockedStyle.setLocked(true);
		lastFyLockedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		lastFyLockedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		lastFyLockedStyle.setBorderTop(BorderStyle.THIN);
		lastFyLockedStyle.setBorderBottom(BorderStyle.THIN);
		lastFyLockedStyle.setBorderLeft(BorderStyle.THIN);
		lastFyLockedStyle.setBorderRight(BorderStyle.THIN);

			// Data rows
			for (MCUNormsValueDTO dto : dtoList) {
				// if (isAfterSave) {
				List<Object> list = new ArrayList<>();
				list.add(dto.getNormParameterTypeDisplayName());
				list.add(dto.getSapCode());
				list.add(dto.getProductName());
				list.add(dto.getUOM());
				if (merox) {
					list.add(dto.getLastFy());
				}
				list.add(dto.getApril());
				list.add(dto.getMay());
				list.add(dto.getJune());
				list.add(dto.getJuly());
				list.add(dto.getAugust());
				list.add(dto.getSeptember());
				list.add(dto.getOctober());
				list.add(dto.getNovember());
				list.add(dto.getDecember());
				list.add(dto.getJanuary());
				list.add(dto.getFebruary());
				list.add(dto.getMarch());
				list.add(dto.getRemarks());
				list.add(dto.getId());
				isEditable.add(dto.getIsEditable());
				if (isAfterSave) {
					list.add(dto.getSaveStatus());
					list.add(dto.getErrDescription());
				}
				rows.add(list);
			}

			List<String> innerHeaders = new ArrayList<>();
			innerHeaders.add("Type");
			innerHeaders.add("SAP MAT Code");
			innerHeaders.add("Particulars");
			if(polyester) {
				innerHeaders.add("UOM / MT");
			} else {
				innerHeaders.add("UOM");
			}
			if (merox) {
				innerHeaders.add("Last FY " + getPreviousFinancialYear(year));
			}
			List<String> monthsList = getAcademicYearMonths(year);
			innerHeaders.addAll(monthsList);
			
			innerHeaders.add("Remarks");
			innerHeaders.add("Id");
			if (isAfterSave) {
				innerHeaders.add("Status");
				innerHeaders.add("Error Description");
			}

			int remarksColIndex = innerHeaders.indexOf("Remarks");
			int idColIndex = innerHeaders.indexOf("Id");
			int lastFyColIndex = merox ? innerHeaders.indexOf("Last FY " + getPreviousFinancialYear(year)) : -1;

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
				boolean isRowEditable = true;
				if (isEditable.get(currentRow - 1) != null) {
					isRowEditable = isEditable.get(currentRow - 1);
				}

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
					if (col == remarksColIndex) {
						cell.setCellStyle(isRowEditable ? remarksUnlockedStyle : remarksLockedStyle);
					} else if (lastFyColIndex >= 0 && col == lastFyColIndex) {
						cell.setCellStyle(lastFyLockedStyle);
					} else if (isRowEditable) {
						cell.setCellStyle(unlockedStyle);
					} else {
						cell.setCellStyle(lockedStyle);
					}

				}
			}
			
				sheet.setColumnHidden(idColIndex, true);

			int totalCols = innerHeaders.size();
			for (int col = 0; col < totalCols; col++) {
				if (col == idColIndex) {
					
				} else if (col == remarksColIndex) {
					sheet.setColumnWidth(col, 15000); 
				} else {
					sheet.autoSizeColumn(col);
				}
			}

			// Adjust row heights so wrapped Remarks content is fully visible
			for (int r = 1; r < currentRow; r++) {
				Row row = sheet.getRow(r);
				if (row == null) continue;
				Cell remarksCell = (remarksColIndex >= 0) ? row.getCell(remarksColIndex) : null;
				if (remarksCell != null) {
					String remarksText = remarksCell.getStringCellValue();
					if (remarksText != null && !remarksText.isEmpty()) {
						int charsPerLine = 55;
						int lines = (int) Math.ceil((double) remarksText.length() / charsPerLine);
						row.setHeight((short) (Math.max(1, lines) * 300)); // 300 twips ≈ 15 pt per line
					}
				}
			}

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

	// ref: createExcel | separate method to include sap code
	@Override
	public byte[] createExcelWithSapCode(String year, UUID plantFKId, boolean isAfterSave, List<MCUNormsValueDTO> dtoList,
			String mode, String gradeId) {
		try {
			AOPMessageVM aopMessageVM = getNormalOperationNormsData(year, plantFKId.toString(), gradeId, mode);
			List<Boolean> isEditable = new ArrayList<>();
			Plants plant = plantsRepository.findById(plantFKId).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			if (!isAfterSave) {
				Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
				dtoList = (List<MCUNormsValueDTO>) responseMap.get("mcuNormsValueDTOList");
			}

			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;

			sheet.protectSheet("secret_password");

			List<List<Object>> rows = new ArrayList<>();

			CellStyle lockedStyle = workbook.createCellStyle();
			lockedStyle.setLocked(true);
			lockedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			lockedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			lockedStyle.setBorderTop(BorderStyle.THIN);
			lockedStyle.setBorderBottom(BorderStyle.THIN);
			lockedStyle.setBorderLeft(BorderStyle.THIN);
			lockedStyle.setBorderRight(BorderStyle.THIN);

			CellStyle unlockedStyle = workbook.createCellStyle();
			unlockedStyle.setLocked(false);
			unlockedStyle.setBorderTop(BorderStyle.THIN);
			unlockedStyle.setBorderBottom(BorderStyle.THIN);
			unlockedStyle.setBorderLeft(BorderStyle.THIN);
			unlockedStyle.setBorderRight(BorderStyle.THIN);

			CellStyle remarksLockedStyle = workbook.createCellStyle();
			remarksLockedStyle.setLocked(true);
			remarksLockedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			remarksLockedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			remarksLockedStyle.setBorderTop(BorderStyle.THIN);
			remarksLockedStyle.setBorderBottom(BorderStyle.THIN);
			remarksLockedStyle.setBorderLeft(BorderStyle.THIN);
			remarksLockedStyle.setBorderRight(BorderStyle.THIN);
			remarksLockedStyle.setWrapText(true);

			CellStyle remarksUnlockedStyle = workbook.createCellStyle();
			remarksUnlockedStyle.setLocked(false);
			remarksUnlockedStyle.setBorderTop(BorderStyle.THIN);
			remarksUnlockedStyle.setBorderBottom(BorderStyle.THIN);
			remarksUnlockedStyle.setBorderLeft(BorderStyle.THIN);
			remarksUnlockedStyle.setBorderRight(BorderStyle.THIN);
			remarksUnlockedStyle.setWrapText(true);

		for (MCUNormsValueDTO dto : dtoList) {
			List<Object> list = new ArrayList<>();
			list.add(dto.getNormParameterTypeDisplayName());
			list.add(dto.getProductName());
			list.add(dto.getUOM());
			list.add(dto.getSapCode());
			list.add(dto.getApril());
				list.add(dto.getMay());
				list.add(dto.getJune());
				list.add(dto.getJuly());
				list.add(dto.getAugust());
				list.add(dto.getSeptember());
				list.add(dto.getOctober());
				list.add(dto.getNovember());
				list.add(dto.getDecember());
				list.add(dto.getJanuary());
				list.add(dto.getFebruary());
				list.add(dto.getMarch());
				if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("Chemical") || vertical.getName().equalsIgnoreCase("PTA")) {
					list.add(dto.getWtAverage());
				}
				list.add(dto.getRemarks());
				list.add(dto.getId());
				isEditable.add(dto.getIsEditable());
				if (isAfterSave) {
					list.add(dto.getSaveStatus());
					list.add(dto.getErrDescription());
				}
				rows.add(list);
			}

		List<String> innerHeaders = new ArrayList<>();
		innerHeaders.add("Type");
		innerHeaders.add("Particulars");
		innerHeaders.add("UOM");
		innerHeaders.add("Sap Code");
			List<String> monthsList = getAcademicYearMonths(year);
			innerHeaders.addAll(monthsList);
			if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("Chemical") || vertical.getName().equalsIgnoreCase("PTA")) {
				innerHeaders.add("Weighted Avg");
			}
			innerHeaders.add("Remarks");
			innerHeaders.add("Id");
			if (isAfterSave) {
				innerHeaders.add("Status");
				innerHeaders.add("Error Description");
			}

		int remarksColIndex = innerHeaders.indexOf("Remarks");
		int idColIndex = innerHeaders.indexOf("Id");
		int sapCodeColIndex = innerHeaders.indexOf("Sap Code");

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
			boolean isRowEditable = true;
			if (isEditable.get(currentRow - 1) != null) {
				isRowEditable = isEditable.get(currentRow - 1);
			}

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
				if (col == remarksColIndex) {
					cell.setCellStyle(isRowEditable ? remarksUnlockedStyle : remarksLockedStyle);
				} else if (col == sapCodeColIndex) {
					cell.setCellStyle(lockedStyle);
				} else if (isRowEditable) {
					cell.setCellStyle(unlockedStyle);
				} else {
					cell.setCellStyle(lockedStyle);
				}
			}
		}

			if (vertical.getName().equalsIgnoreCase("VCM") || vertical.getName().equalsIgnoreCase("Chemical") || vertical.getName().equalsIgnoreCase("PTA")) {
				sheet.setColumnHidden(18, true);
			} else {
				sheet.setColumnHidden(17, true);
			}

			int totalCols = innerHeaders.size();
			for (int col = 0; col < totalCols; col++) {
				if (col == idColIndex) {
					// already hidden; skip width adjustment
				} else if (col == remarksColIndex) {
					sheet.setColumnWidth(col, 15000);
				} else {
					sheet.autoSizeColumn(col);
				}
			}

			for (int r = 1; r < currentRow; r++) {
				Row row = sheet.getRow(r);
				if (row == null) continue;
				Cell remarksCell = (remarksColIndex >= 0) ? row.getCell(remarksColIndex) : null;
				if (remarksCell != null) {
					String remarksText = remarksCell.getStringCellValue();
					if (remarksText != null && !remarksText.isEmpty()) {
						int charsPerLine = 55;
						int lines = (int) Math.ceil((double) remarksText.length() / charsPerLine);
						row.setHeight((short) (Math.max(1, lines) * 300));
					}
				}
			}

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

	public byte[] exportSteadyStateNormsChemical(String year, UUID plantFKId, boolean isAfterSave, List<MCUNormsValueDTO> dtoList) {
		try {
			AOPMessageVM aopMessageVM = getNormalOperationNormsData(year, plantFKId.toString(), null, null);
			List<Boolean> isEditable = new ArrayList<>();

			if (!isAfterSave) {
				Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
				dtoList = (List<MCUNormsValueDTO>) responseMap.get("mcuNormsValueDTOList");
			}

			Workbook workbook = new XSSFWorkbook();

			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;

			List<List<Object>> rows = new ArrayList<>();
			CellStyle lockedStyle = workbook.createCellStyle();
			lockedStyle.setLocked(true);
			lockedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			lockedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle unlockedStyle = workbook.createCellStyle();
			unlockedStyle.setLocked(false);
			
			for (MCUNormsValueDTO dto : dtoList) {
				
				List<Object> list = new ArrayList<>();
				list.add(dto.getSapCode());
				list.add(dto.getProductName());
				list.add(dto.getUOM());
				list.add(dto.getApril());
				list.add(dto.getMay());
				list.add(dto.getJune());
				list.add(dto.getJuly());
				list.add(dto.getAugust());
				list.add(dto.getSeptember());
				list.add(dto.getOctober());
				list.add(dto.getNovember());
				list.add(dto.getDecember());
				list.add(dto.getJanuary());
				list.add(dto.getFebruary());
				list.add(dto.getMarch());
				list.add(dto.getWtAverage());

				list.add(dto.getRemarks());
				list.add(dto.getId());
				isEditable.add(dto.getIsEditable());
				
				if (isAfterSave) {
					list.add(dto.getSaveStatus());
					list.add(dto.getErrDescription());
				}
				rows.add(list);
			}

			List<String> innerHeaders = new ArrayList<>();
			innerHeaders.add("Sap Mat Code");
			innerHeaders.add("Particulars");
			innerHeaders.add("UOM");
			List<String> monthsList = getAcademicYearMonths(year);
			innerHeaders.addAll(monthsList);
			innerHeaders.add("Weighted Avg");
			innerHeaders.add("Remarks");
			innerHeaders.add("Id");
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
				boolean isRowEditable = true;
				if (isEditable.get(currentRow - 1) != null) {
					isRowEditable = isEditable.get(currentRow - 1);
				}

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
					if (isRowEditable) {
						cell.setCellStyle(unlockedStyle);
					} else {
						cell.setCellStyle(lockedStyle);
					}

				}
			}
				sheet.setColumnHidden(17, true);
			
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

	private static String formatMonthYear(int month, int year) {
		LocalDate date = LocalDate.of(year, month, 1);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH);
		return date.format(formatter);
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

	@Override
	public AOPMessageVM calculateNormalOpsNorms(String aopYear, String plantId, String siteId, String verticalId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		String storedProcedure = vertical.getName() + "_" + site.getName() + "_GetNormsValue";
		String callSql = "{call " + storedProcedure + "(?, ?, ?, ?)}";

		try (Connection connection = dataSource.getConnection();
				CallableStatement stmt = connection.prepareCall(callSql)) {

			// Set parameters
			stmt.setString(1, plantId);
			stmt.setString(2, siteId);
			stmt.setString(3, verticalId);
			stmt.setString(4, aopYear);

			// Execute the stored procedure
			stmt.executeUpdate();

			// Optional: commit if auto-commit is off
			if (!connection.getAutoCommit()) {
				connection.commit();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), aopYear,
				"normal-op-norms");

		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("normal-op-norms");
		for (ScreenMapping screenMapping : screenMappingList) {
			if (!screenMapping.getCalculationScreen().equalsIgnoreCase(screenMapping.getDependentScreen())) {

				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(aopYear);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("SP Executed successfully");
		// aopMessageVM.setData(rowsAffected);
		return aopMessageVM;

	}

	@Override
	public AOPMessageVM calculateNormalOpsNormsPolyester(String aopYear, String plantId, String siteId, String verticalId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		String storedProcedure = vertical.getName() + "_" + site.getName() + "_GradeNormCalculation";
		String callSql = "{call " + "[" + storedProcedure + "]" + "(?, ?, ?, ?)}";

		try (Connection connection = dataSource.getConnection();
				CallableStatement stmt = connection.prepareCall(callSql)) {

			// Set parameters
			stmt.setString(1, plantId);
			stmt.setString(2, siteId);
			stmt.setString(3, verticalId);
			stmt.setString(4, aopYear);

			// Execute the stored procedure
			stmt.executeUpdate();

			// Optional: commit if auto-commit is off
			if (!connection.getAutoCommit()) {
				connection.commit();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), aopYear,
				"normal-op-norms");

		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("normal-op-norms");
		for (ScreenMapping screenMapping : screenMappingList) {
			if (!screenMapping.getCalculationScreen().equalsIgnoreCase(screenMapping.getDependentScreen())) {

				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(aopYear);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("SP Executed successfully");
		// aopMessageVM.setData(rowsAffected);
		return aopMessageVM;

	}

	@Override
	public AOPMessageVM getNormalOperationNormsGrades(String financialYear, String plantId) {
		List<Map<String, Object>> gradeList = new ArrayList<>();
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

			String viewName = "vwScrn" + vertical.getName() + "Grade";
			// Validate or sanitize viewName before using it directly in the query to
			// prevent SQL injection
			String sql = "SELECT * FROM " + "[" + viewName + "]" 
					+ " WHERE FinancialYear = :financialYear AND Plant_FK_Id = :plantId";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("financialYear", financialYear);
			query.setParameter("plantId", plantId);

			List<Object[]> obj = query.getResultList(); // You can cast this to a DTO later

			for (Object[] result : obj) {
				Map<String, Object> map = new HashMap<>();
				map.put("gradeId", result[0].toString());
				map.put("displayName", result[1].toString());
				map.put("name", result[2].toString());
				map.put("plantId", result[3].toString());
				map.put("financialYear", result[4].toString());
				gradeList.add(map);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(gradeList);
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getNormsTransactionFinalNormsModeWise(String plantId, String aopYear) {
		try {
			List<Map<String, Object>> result = new ArrayList<>();
			AOPMessageVM aopMessageVM = getNormsTransaction(plantId, aopYear);
			List<Map<String, Object>> normsTransactions = (List<Map<String, Object>>) aopMessageVM.getData();

			AOPMessageVM finalNorms = finalNormsService.getFinalNorms(aopYear, plantId, null, null);
			Map<String, Object> dataMap = (Map<String, Object>) finalNorms.getData();
			List<ModeWiseNormsDTO> finalNormsDTOList = (List<ModeWiseNormsDTO>) dataMap.get("mcuNormsValueDTOList");
			Map<String, String> sapMaterialCodeToIdMap = finalNormsDTOList.stream()
					.collect(Collectors.toMap(ModeWiseNormsDTO::getSapMaterialCode, ModeWiseNormsDTO::getMaterialFKId,
							(existing, replacement) -> existing));

			for (Map<String, Object> map : normsTransactions) {
				Object normParameterFKId = map.get("normParameterFKId");
				if (normParameterFKId != null) {
					UUID normParameterId = UUID.fromString(normParameterFKId.toString());
					Optional<NormParameters> normParametersOpt = normParametersRepository.findById(normParameterId);
					if (normParametersOpt.isPresent()) {
						NormParameters normParameters = normParametersOpt.get();
						if (!normParameters.getType().equalsIgnoreCase("Monthly")) {
							String sapMaterialCode = normParameters.getSapMaterialCode();
							if (sapMaterialCodeToIdMap.containsKey(sapMaterialCode)) {
								Map<String, Object> finalMap = new HashMap<>();
								finalMap.put("month", map.get("month"));
								finalMap.put("normParameterId", sapMaterialCodeToIdMap.get(sapMaterialCode));
								result.add(finalMap);
							}
						}
					}
				}
			}

			AOPMessageVM finalResult = new AOPMessageVM();
			finalResult.setCode(200);
			finalResult.setData(result);
			finalResult.setMessage("Data fetched successfully");
			return finalResult;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public AOPMessageVM getNormsTransactionFinalNorms(String plantId, String aopYear) {
		List<Map<String, Object>> result = new ArrayList<>();

		AOPMessageVM aopMessageVM = getNormsTransaction(plantId, aopYear);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> normsTransactions = (List<Map<String, Object>>) aopMessageVM.getData();

		AOPMessageVM finalNorms = finalNormsService.getFinalNorms(aopYear, plantId, null, null);
		@SuppressWarnings("unchecked")
		Map<String, Object> dataMap = (Map<String, Object>) finalNorms.getData();
		@SuppressWarnings("unchecked")
		List<ModeWiseNormsDTO> finalNormsDTOList = (List<ModeWiseNormsDTO>) dataMap.get("mcuNormsValueDTOList");

		Map<String, String> materialNameToFKId = finalNormsDTOList.stream()
				.collect(Collectors.toMap(ModeWiseNormsDTO::getSapMaterialCode, ModeWiseNormsDTO::getMaterialFKId,
						(existing, replacement) -> existing));

		for (Map<String, Object> map : normsTransactions) {
			Object normParameterFKIdObj = map.get("normParameterFKId");
			if (normParameterFKIdObj != null) {
				UUID normParameterId = UUID.fromString(normParameterFKIdObj.toString());
				Optional<NormParameters> normParametersOpt = normParametersRepository.findById(normParameterId);
				if (normParametersOpt.isPresent()) {
					NormParameters normParameters = normParametersOpt.get();
					if (normParameters.getType().equalsIgnoreCase("Monthly")) {
						UUID normParameterTypeFkId = normParameters.getNormParameterTypeFkId();
						Optional<NormParameterType> normParameterTypeOpt = normParameterTypeRepository
								.findById(normParameterTypeFkId);
						if (normParameterTypeOpt.isPresent()) {
							NormParameterType normParameterType = normParameterTypeOpt.get();
							String typeName = normParameterType.getName();
							if (typeName.equalsIgnoreCase("RawMaterial") || typeName.equalsIgnoreCase("ByProducts")) {

								String materialName = normParameters.getSapMaterialCode();
								if (materialNameToFKId.containsKey(materialName)) {
									Map<String, Object> finalMap = new HashMap<>();
									finalMap.put("month", map.get("month"));
									finalMap.put("normParameterId", materialNameToFKId.get(materialName));

									result.add(finalMap);
								}
							} else {

								List<MCUNormsValue> mcuNormsValues = mcuNormsValueRepository
										.findCheckedNormsByMaterialFkIdNative(normParameterId);
								if (!mcuNormsValues.isEmpty()) {
									String materialName = normParameters.getSapMaterialCode();
									if (materialNameToFKId.containsKey(materialName)) {
										Map<String, Object> finalMap = new HashMap<>();
										finalMap.put("month", map.get("month"));
										finalMap.put("normParameterId", materialNameToFKId.get(materialName));
										result.add(finalMap);
									}
								}
							}
						}
					}
				}
			}
		}

		AOPMessageVM finalResult = new AOPMessageVM();
		finalResult.setCode(200);
		finalResult.setData(result);
		finalResult.setMessage("Data fetched successfully");
		return finalResult;
	}

	public int executeProcedure(String procedureName, String plantId,
			String aopYear) {
		try {

			String callSql = "{call " + "[" + procedureName + "]" + "(?, ?)}";

			try (Connection connection = dataSource.getConnection();
					CallableStatement stmt = connection.prepareCall(callSql)) {
				stmt.setString(1, plantId);
				stmt.setString(2, aopYear);
				int rowsAffected = stmt.executeUpdate();
				if (!connection.getAutoCommit()) {
					connection.commit();
				}

				return rowsAffected;

			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getCatChemCalculationData(String plantId, String year) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_GetMakeupBatchRecipe";
			String sql = "EXEC [dbo].[" + storedProcedure + "] @plantId = :plantId, @aopYear = :year";
			Map<String, Object> params = new HashMap<>();
			params.put("plantId", plantId);
			params.put("year", year);

			List<Map<String, Object>> resultList = namedParameterJdbcTemplate.queryForList(sql, params);

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Recipe data fetched successfully");
			aopMessageVM.setData(resultList);
			return aopMessageVM;
		} catch (Exception e) {
			e.printStackTrace();
			aopMessageVM.setCode(500);
			aopMessageVM.setMessage("Failed to fetch Recipe data: " + e.getMessage());
			return aopMessageVM;
		}
	}

	@Override
	@Transactional
	public AOPMessageVM saveCatChemCalculationData(String plantId, String year, List<Map<String, Object>> payload) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			String userName = com.wks.caseengine.utility.Utility.getUserName();

			String mergeSql = "MERGE INTO NormAttributeTransactions AS target " +
					"USING (SELECT :normParameterId AS NormParameter_FK_Id, :year AS AuditYear) AS source " +
					"ON target.NormParameter_FK_Id = source.NormParameter_FK_Id " +
					"AND target.AuditYear = source.AuditYear " +
					"AND target.AOPMonth = 4 " +
					"WHEN MATCHED THEN " +
					"    UPDATE SET AttributeValue = :value, " +
					"            UserName = :userName, " +
					"            ModifiedOn = GETDATE() " +
					"WHEN NOT MATCHED THEN " +
					"    INSERT (Id, NormParameter_FK_Id, AuditYear, AOPMonth, AttributeValue, CreatedOn, UserName, AttributeValueVersion) " +
					"    VALUES (NEWID(), :normParameterId, :year, 4, :value, GETDATE(), :userName, 'V1');";

			String[][] keyMappings = {
				{"Sod Bi Carb", "DM_Water_Sodi_Bi_Carb_Id"},
				{"SodBiCarb", "DM_Water_Sodi_Bi_Carb_Id"},
				{"Polystat", "DM_Water_Polystat_Id"},
				{"Evicas", "DM_Water_Evicas_Id"},
				{"PVA88", "DM_Water_PVA88_Id"},
				{"PVA-55", "DM_Water_PVA55_Id"},
				{"PVA55", "DM_Water_PVA55_Id"},
				{"B72", "DM_Water_B72_Id"},
				{"L9P", "DM_Water_L9P_Id"},
				{"Versene", "DM_Water_Versene_Id"},
				{"Nonyl Phe", "DM_Water_Nonyl_Phe_Id"},
				{"IRGASTAB", "DM_Water_IRGASTAB_Id"},
				{"ATSC", "DM_Water_ATSC_Id"},
				{"Antiswelling", "DM_Water_Antiswelling_Id"},
				{"Antifoam", "DM_Water_Antifoam_Id"},
				{"K57 Catalyst", "DM_Water_K57_Catalyst_Id"},
				{"K67 Catalyst", "DM_Water_K67_Catalyst_Id"}
			};

			for (Map<String, Object> row : payload) {
				for (String[] mapping : keyMappings) {
					String valueKey = mapping[0];
					String idKey = mapping[1];

					if (row.containsKey(idKey) && row.containsKey(valueKey)) {
						Object idObj = row.get(idKey);
						Object valObj = row.get(valueKey);

						if (idObj != null && !idObj.toString().trim().isEmpty()) {
							String normParameterId = idObj.toString();
							String valueStr = valObj != null ? valObj.toString() : "0";

							Map<String, Object> params = new HashMap<>();
							params.put("normParameterId", normParameterId);
							params.put("year", year);
							params.put("value", valueStr);
							params.put("userName", userName);

							namedParameterJdbcTemplate.update(mergeSql, params);
						}
					}
				}
			}

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Recipe data saved successfully");
			aopMessageVM.setData(true);
			return aopMessageVM;
		} catch (Exception e) {
			e.printStackTrace();
			aopMessageVM.setCode(500);
			aopMessageVM.setMessage("Failed to save Recipe data: " + e.getMessage());
			aopMessageVM.setData(false);
			return aopMessageVM;
		}
	}

}
