package com.wks.caseengine.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.wks.caseengine.dto.SpyroOutputDTO;
import com.wks.caseengine.dto.YieldDMDDTO;
import com.wks.caseengine.dto.YieldDTO;
import com.wks.caseengine.dto.YieldParticularDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.ExcelConstants;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class SpyroOutputServiceImpl implements SpyroOutputService{
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;
	
	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;
	
	@Autowired
	private NormParametersRepository normParametersRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private ExcelUtilityService excelUtilityService;


	@Override
	public AOPMessageVM getSpyroOutputData(String year, String plantId,String Mode,String type) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> spyroOutputDataList = new ArrayList<>();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
        Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

        String siteId = site.getId().toString();
        String verticalId = vertical.getId().toString();
        String procedureName=vertical.getName()+"_"+site.getName()+"_GetSpyroOutput";
		try {
			List<Object[]> results = getData(plantId, year,siteId,verticalId,Mode,procedureName);

			for (Object[] row : results) {
				Map<String, Object> map = new HashMap<>(); // Create a new map for each row
				
				if(row[4].toString().contains(type)) {	
					
					map.put("normParameterFKID", row[2]);
					map.put("particulars", row[3]);
					map.put("normParameterDisplayName", row[4]);
					map.put("uom", row[7]);
					map.put("remarks", row[9]);
					map.put("jan", (row[10] == null || row[10].toString().isEmpty()) ? 0.0 : Double.parseDouble(row[10].toString()));
					map.put("feb", (row[11] == null || row[11].toString().isEmpty()) ? 0.0 : Double.parseDouble(row[11].toString()));
					map.put("mar", (row[12] == null || row[12].toString().isEmpty()) ? 0.0 : Double.parseDouble(row[12].toString()));
					map.put("apr", (row[13] == null || row[13].toString().isEmpty()) ? 0.0 : Double.parseDouble(row[13].toString()));
					map.put("may", (row[14] == null || row[14].toString().isEmpty()) ? 0.0 : Double.parseDouble(row[14].toString()));
					map.put("jun", (row[15] == null || row[15].toString().isEmpty()) ? 0.0 : Double.parseDouble(row[15].toString()));
					map.put("jul", (row[16] == null || row[16].toString().isEmpty()) ? 0.0 : Double.parseDouble(row[16].toString()));
					map.put("aug", (row[17] == null || row[17].toString().isEmpty()) ? 0.0 : Double.parseDouble(row[17].toString()));
					map.put("sep", (row[18] == null || row[18].toString().isEmpty()) ? 0.0 : Double.parseDouble(row[18].toString()));
					map.put("oct", (row[19] == null || row[19].toString().isEmpty()) ? 0.0 : Double.parseDouble(row[19].toString()));
					map.put("nov", (row[20] == null || row[20].toString().isEmpty()) ? 0.0 : Double.parseDouble(row[20].toString()));
					map.put("dec", (row[21] == null || row[21].toString().isEmpty()) ? 0.0 : Double.parseDouble(row[21].toString()));
					map.put("isEditable", row[22]);
					spyroOutputDataList.add(map); // Add the map to the list here
				}
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(spyroOutputDataList);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}
	
	public List<Object[]> getData(String plantId, String AopYear, String siteId,
			String verticalId,String Mode,String procedureName) {
		try {
			
			String sql = "EXEC " + procedureName +
					" @plantId = :plantId,@siteId = :siteId,@verticalId = :verticalId, @AopYear = :AopYear, @Mode = :Mode";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("AopYear", AopYear);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("Mode", Mode);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public List<Object[]> getYieldData(String plantId, String aopYear,
			String procedureName) {
		try {
			
			String sql = "EXEC " + procedureName +
					" @plantId = :plantId, @aopYear = :aopYear";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);
			

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM updateSpyroOutputData(String year,String plantId,List<SpyroOutputDTO> spyroOutputDTOList) {
		AOPMessageVM aopMessageVM=new AOPMessageVM();
		List<SpyroOutputDTO> failedList = new ArrayList<>();
		try {
			for (SpyroOutputDTO spyroOutputDTO : spyroOutputDTOList) {
				if (spyroOutputDTO.getSaveStatus() != null
						&& spyroOutputDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(spyroOutputDTO);
					continue;
				}
				UUID normParameterFKId = UUID.fromString(spyroOutputDTO.getNormParameterFKID());
				Optional<NormParameters> optionNormParameters = normParametersRepository.findById(normParameterFKId);
				if (!optionNormParameters.isPresent()) {
					spyroOutputDTO.setSaveStatus("Failed");
					spyroOutputDTO.setErrDescription("Norm Paramter not found");
					failedList.add(spyroOutputDTO);
					continue;
				}
				if (optionNormParameters.isPresent() && !optionNormParameters.get().getIsEditable()) {
					continue;
				}
				for (int i = 1; i <= 12; i++) {
					Double attributeValue = getAttributeValue(spyroOutputDTO, i);
		
					saveData(normParameterFKId, i, attributeValue, spyroOutputDTO,year);
				}
			}
			List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("spyro-output");
			for (ScreenMapping screenMapping : screenMappingList) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			// Filter only failed records using Stream API
	         failedList = spyroOutputDTOList.stream()
	            .filter(dto -> "Failed".equalsIgnoreCase(dto.getSaveStatus()))
	            .collect(Collectors.toList());
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data updated successfully");
			aopMessageVM.setData(failedList);
			return aopMessageVM;

	} catch (IllegalArgumentException e) {
		throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	} catch (Exception ex) {
		throw new RuntimeException("Failed to fetch data", ex);
	}
		
	}
	
	public Double getAttributeValue(SpyroOutputDTO spyroOutputDTO, Integer i) {
		switch (i) {
			case 1:
				return spyroOutputDTO.getJan();
			case 2:
				return spyroOutputDTO.getFeb();
			case 3:
				return spyroOutputDTO.getMar();
			case 4:
				return spyroOutputDTO.getApr();
			case 5:
				return spyroOutputDTO.getMay();
			case 6:
				return spyroOutputDTO.getJun();
			case 7:
				return spyroOutputDTO.getJul();
			case 8:
				return spyroOutputDTO.getAug();
			case 9:
				return spyroOutputDTO.getSep();
			case 10:
				return spyroOutputDTO.getOct();
			case 11:
				return spyroOutputDTO.getNov();
			case 12:
				return spyroOutputDTO.getDec();

		}
		return spyroOutputDTO.getJan();
	}
	
	public void saveData(UUID normParameterFKId, Integer i, Double attributeValue, SpyroOutputDTO spyroOutputDTO, String year) {
	    if (spyroOutputDTO == null) {
	        throw new IllegalArgumentException("SpyroOutputDTO cannot be null");
	    }

	    String newRemarks = Optional.ofNullable(spyroOutputDTO.getRemarks()).orElse("").trim();
	    Double newDouble = attributeValue != null ? attributeValue : 0.0;
	    String newValue = newDouble.toString(); 

	    Optional<NormAttributeTransactions> existingRecord = normAttributeTransactionsRepository
	        .findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameterFKId, i, year);

	    if (existingRecord.isPresent()) {
	        NormAttributeTransactions normAttributeTransactions = existingRecord.get();
	        String existingRemarks = Optional.ofNullable(normAttributeTransactions.getRemarks()).orElse("").trim();
	        String existingValueStr = Optional.ofNullable(normAttributeTransactions.getAttributeValue()).orElse("0.0").trim();
	        
	        Double existingDouble = null;
	        try {
	            existingDouble = Double.parseDouble(existingValueStr);
	        } catch (NumberFormatException e) {
	        }

	        boolean remarksMatch = existingRemarks.equalsIgnoreCase(newRemarks);
	        boolean valuesDiffer = false;
	        if (existingDouble != null) {
	            valuesDiffer = Double.compare(existingDouble, newDouble) != 0;
	        } else {
	            valuesDiffer = !existingValueStr.equalsIgnoreCase(newValue);
	        }
	        
	        if (remarksMatch && valuesDiffer) {
	            spyroOutputDTO.setSaveStatus("Failed");
	            spyroOutputDTO.setErrDescription("Please add/update remark");
	            return;
	        }

	        normAttributeTransactions.setRemarks(newRemarks);
	        normAttributeTransactions.setAttributeValue(newValue);
	        normAttributeTransactions.setModifiedOn(new Date());
	        normAttributeTransactions.setUserName(Utility.getUserName());
	        
	        normAttributeTransactionsRepository.save(normAttributeTransactions);

	    } else {
	        
	        if (newRemarks.isEmpty() && newDouble != 0.0) {
	            spyroOutputDTO.setSaveStatus("Failed");
	            spyroOutputDTO.setErrDescription("Please add/update remark");
	            return;
	        }

	        NormAttributeTransactions newRecord = new NormAttributeTransactions();
	        newRecord.setCreatedOn(new Date());
	        newRecord.setAttributeValueVersion("V1");
	        newRecord.setUserName(Utility.getUserName());
	        newRecord.setNormParameterFKId(normParameterFKId);
	        newRecord.setAopMonth(i);
	        newRecord.setAuditYear(year);
	        newRecord.setRemarks(newRemarks);
	        newRecord.setAttributeValue(newValue);

	        normAttributeTransactionsRepository.save(newRecord);
	    }
	}
	
	@Override
	public AOPMessageVM getSpyroOutputYieldData(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<YieldDTO> spyroOutputYieldDataList = new ArrayList<>();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
        Sites site = siteRepository.findById(plant.getSiteFkId())
	            .orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
        String procedureName=vertical.getName()+"_"+site.getName()+"_GetYield";
		try {
			List<Object[]> results = getYieldData(plantId, year,procedureName);

			double totalFiveFC2C3 = 0;
			double totalFiveFPropane = 0;
			double totalFiveFEthane = 0;
			double totalFourFC2C3 = 0;
			double totalFourFPropane = 0;
			double totalFourFEthane = 0;
			double totalFourFDC2C3 = 0;
			double totalFourFDPropane = 0;
			double totalFourFDEthane = 0;

			for (Object[] row : results) {
			    YieldDTO yieldDTO = new YieldDTO();
			    yieldDTO.setParticulars(row[0] != null ? row[0].toString() : " ");

			    double v1 = (row[1] != null && !row[1].toString().trim().isEmpty()) 
			                ? Double.parseDouble(row[1].toString()) : 0.0;
			    double v2 = (row[2] != null && !row[2].toString().trim().isEmpty()) 
			                ? Double.parseDouble(row[2].toString()) : 0.0;
			    double v3 = (row[3] != null && !row[3].toString().trim().isEmpty()) 
			                ? Double.parseDouble(row[3].toString()) : 0.0;
			    double v4 = (row[4] != null && !row[4].toString().trim().isEmpty()) 
			                ? Double.parseDouble(row[4].toString()) : 0.0;
			    double v5 = (row[5] != null && !row[5].toString().trim().isEmpty()) 
			                ? Double.parseDouble(row[5].toString()) : 0.0;
			    double v6 = (row[6] != null && !row[6].toString().trim().isEmpty()) 
			                ? Double.parseDouble(row[6].toString()) : 0.0;
			    double v7 = (row[7] != null && !row[7].toString().trim().isEmpty()) 
			                ? Double.parseDouble(row[7].toString()) : 0.0;
			    double v8 = (row[8] != null && !row[8].toString().trim().isEmpty()) 
			                ? Double.parseDouble(row[8].toString()) : 0.0;
			    double v9 = (row[9] != null && !row[9].toString().trim().isEmpty()) 
			                ? Double.parseDouble(row[9].toString()) : 0.0;

			    yieldDTO.setFiveFC2C3(v1);
			    yieldDTO.setFiveFPropane(v2);
			    yieldDTO.setFiveFEthane(v3);
			    yieldDTO.setFourFC2C3(v4);
			    yieldDTO.setFourFPropane(v5);
			    yieldDTO.setFourFEthane(v6);
			    yieldDTO.setFourFDC2C3(v7);
			    yieldDTO.setFourFDPropane(v8);
			    yieldDTO.setFourFDEthane(v9);

			    // add to totals
			    totalFiveFC2C3 += v1;
			    totalFiveFPropane += v2;
			    totalFiveFEthane += v3;
			    totalFourFC2C3 += v4;
			    totalFourFPropane += v5;
			    totalFourFEthane += v6;
			    totalFourFDC2C3 += v7;
			    totalFourFDPropane += v8;
			    totalFourFDEthane += v9;

			    spyroOutputYieldDataList.add(yieldDTO);
			}

			// Now add the Total row
			YieldDTO totalRow = new YieldDTO();
			totalRow.setParticulars("Total");
			totalRow.setFiveFC2C3(totalFiveFC2C3);
			totalRow.setFiveFPropane(totalFiveFPropane);
			totalRow.setFiveFEthane(totalFiveFEthane);
			totalRow.setFourFC2C3(totalFourFC2C3);
			totalRow.setFourFPropane(totalFourFPropane);
			totalRow.setFourFEthane(totalFourFEthane);
			totalRow.setFourFDC2C3(totalFourFDC2C3);
			totalRow.setFourFDPropane(totalFourFDPropane);
			totalRow.setFourFDEthane(totalFourFDEthane);

			spyroOutputYieldDataList.add(totalRow);

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(spyroOutputYieldDataList);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getSpyroOutputYieldDMD(String year, String plantId) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    List<YieldDMDDTO> spyroOutputYieldDataList = new ArrayList<>();
	    
	    Plants plant = plantsRepository.findById(UUID.fromString(plantId))
	            .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
	    Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
	            .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
	    Sites site = siteRepository.findById(plant.getSiteFkId())
	            .orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));

	    String procedureName = vertical.getName() + "_" + site.getName() + "_GetYield";

	    try {
	        List<Object[]> results = getYieldData(plantId, year, procedureName);
	        
	        double[] totals = new double[24];

	        for (Object[] row : results) {
	        	YieldDMDDTO yieldDTO = new YieldDMDDTO();
	            yieldDTO.setParticulars(row[0] != null ? row[0].toString() : " ");
	            double[] vals = new double[24];
	            for (int i = 0; i < 24; i++) {
	                vals[i] = parseDoubleSafe(row[i + 1]);
	                totals[i] += vals[i]; 
	            }

	            mapValuesToDTO(yieldDTO, vals);
	            spyroOutputYieldDataList.add(yieldDTO);
	        }

	        YieldDMDDTO totalRow = new YieldDMDDTO();
	        totalRow.setParticulars("Total");
	        mapValuesToDTO(totalRow, totals);
	        spyroOutputYieldDataList.add(totalRow);

	        aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("Data fetched successfully");
	        aopMessageVM.setData(spyroOutputYieldDataList);
	        return aopMessageVM;

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}

	/**
	 * Helper to safely parse objects to double
	 */
	private double parseDoubleSafe(Object value) {
	    if (value == null || value.toString().trim().isEmpty()) {
	        return 0.0;
	    }
	    try {
	        return Double.parseDouble(value.toString());
	    } catch (NumberFormatException e) {
	        return 0.0;
	    }
	}

	private void mapValuesToDTO(YieldDMDDTO dto, double[] v) {
	    dto.setFiveFC2C3(v[0]);
	    dto.setFiveFPropane(v[1]);
	    dto.setFiveFEthane(v[2]);
	    
	    dto.setFiveFDSC2C3(v[3]);
	    dto.setFiveFDSPropane(v[4]);
	    dto.setFiveFDSEthane(v[5]);
	    dto.setSixFSFDC2C3(v[6]);
	    dto.setSixFSFDPropane(v[7]);
	    dto.setSixFSFDEthane(v[8]);
	    dto.setSixFBFDC2C3(v[9]);
	    dto.setSixFBFDPropane(v[10]);
	    dto.setSixFBFDEthane(v[11]);
	    dto.setFourFC2C3(v[12]);
	    dto.setFourFPropane(v[13]);
	    dto.setFourFEthane(v[14]);
	    dto.setSevenFC2C3(v[15]);
	    dto.setSevenFPropane(v[16]);
	    dto.setSevenFEthane(v[17]);
	    dto.setThreeFC2C3(v[18]);
	    dto.setThreeFPropane(v[19]);
	    dto.setThreeFEthane(v[20]);
	    dto.setFourF2SC2C3(v[21]);
	    dto.setFourF2SPropane(v[22]);
	    dto.setFourF2SEthane(v[23]);
	}	
	public byte[] exportYieldReport(String year, String plantId, boolean isAfterSave, List<YieldDTO> dtoList) {
	    try {
	        AOPMessageVM aopMessageVM = getSpyroOutputYieldData(year,plantId);
	        if (!isAfterSave) {
	            dtoList = (List<YieldDTO>) aopMessageVM.getData();
	        }

	        Workbook workbook = new XSSFWorkbook();
	        Sheet sheet = workbook.createSheet("Sheet1");

	        CellStyle normalStyle = workbook.createCellStyle();
	        CellStyle totalRowStyle = workbook.createCellStyle();
	        totalRowStyle.cloneStyleFrom(normalStyle);
	        totalRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	        totalRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

	        int currentRow = 0;

	        // (Ensure your header writing is here)
	        List<String> innerHeaders = new ArrayList<>();
	        innerHeaders.add("Particulars");
	        innerHeaders.add("5F-C2C3");
	        innerHeaders.add("5F-Propane");
	        innerHeaders.add("5F-Ethane");
	        innerHeaders.add("4F-C2C3");
	        innerHeaders.add("4F-Propane");
	        innerHeaders.add("4F-Ethane");
	        innerHeaders.add("4FD-C2C3");
	        innerHeaders.add("4FD-Propane");
	        innerHeaders.add("4FD-Ethane");
	        if (isAfterSave) {
	            innerHeaders.add("Status");
	            innerHeaders.add("Error Description");
	        }
	        Row headerRow = sheet.createRow(currentRow++);
	        for (int col = 0; col < innerHeaders.size(); col++) {
	            Cell cell = headerRow.createCell(col);
	            cell.setCellValue(innerHeaders.get(col));
	            cell.setCellStyle(normalStyle);
	        }

	        int dataRowCount = dtoList.size();
	        for (int i = 0; i < dataRowCount; i++) {
	            YieldDTO dto = dtoList.get(i);
	            Row row = sheet.createRow(currentRow++);
	            List<Object> rowData = new ArrayList<>();
	            rowData.add(dto.getParticulars());
	            rowData.add(dto.getFiveFC2C3());
	            rowData.add(dto.getFiveFPropane());
	            rowData.add(dto.getFiveFEthane());
	            rowData.add(dto.getFourFC2C3());
	            rowData.add(dto.getFourFPropane());
	            rowData.add(dto.getFourFEthane());
	            rowData.add(dto.getFourFDC2C3());
	            rowData.add(dto.getFourFDPropane());
	            rowData.add(dto.getFourFDEthane());
	            if (isAfterSave) {
	                rowData.add(dto.getSaveStatus());
	                rowData.add(dto.getErrDescription());
	            }

	            boolean isLastRow = (i == dataRowCount - 1);
	            CellStyle styleToUse = isLastRow ? totalRowStyle : normalStyle;

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
	                cell.setCellStyle(styleToUse);
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

	public byte[] exportYieldDMD(String year, String plantId, boolean isAfterSave, List<YieldDMDDTO> dtoList) {
	    try {
	        AOPMessageVM aopMessageVM = getSpyroOutputYieldDMD(year,plantId);
	        if (!isAfterSave) {
	            dtoList = (List<YieldDMDDTO>) aopMessageVM.getData();
	        }

	        Workbook workbook = new XSSFWorkbook();
	        Sheet sheet = workbook.createSheet("Sheet1");

	        CellStyle normalStyle = workbook.createCellStyle();
	        CellStyle totalRowStyle = workbook.createCellStyle();
	        totalRowStyle.cloneStyleFrom(normalStyle);
	        totalRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	        totalRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

	        int currentRow = 0;

	        // (Ensure your header writing is here)
	        List<String> innerHeaders = new ArrayList<>();
	        innerHeaders.add("Particulars");
	        innerHeaders.add("5F-C2C3");
	        innerHeaders.add("5F-Propane");
	        innerHeaders.add("5F-Ethane");
	        innerHeaders.add("5F-D/S-C2C3");
	        innerHeaders.add("5F-D/S-Propane");
	        innerHeaders.add("5F-D/S-Ethane");
	        innerHeaders.add("4F-C2C3");
	        innerHeaders.add("4F-Propane");
	        innerHeaders.add("4F-Ethane");
	        innerHeaders.add("7F-C2C3");
	        innerHeaders.add("7F-Propane");
	        innerHeaders.add("7F-Ethane");
	        innerHeaders.add("6F+SFD-C2C3");
	        innerHeaders.add("6F+SFD-Propane");
	        innerHeaders.add("6F+SFD-Ethane");
	        innerHeaders.add("6F+BFD-C2C3");
	        innerHeaders.add("6F+BFD-Propane");
	        innerHeaders.add("6F+BFD-Ethane");
	        innerHeaders.add("3F-C2C3");
	        innerHeaders.add("3F-Propane");
	        innerHeaders.add("3F-Ethane");
	        innerHeaders.add("4F+2SFD1BFD-C2C3");
	        innerHeaders.add("4F+2SFD1BFD-Propane");
	        innerHeaders.add("4F+2SFD1BFD-Ethane");
	        if (isAfterSave) {
	            innerHeaders.add("Status");
	            innerHeaders.add("Error Description");
	        }
	        Row headerRow = sheet.createRow(currentRow++);
	        for (int col = 0; col < innerHeaders.size(); col++) {
	            Cell cell = headerRow.createCell(col);
	            cell.setCellValue(innerHeaders.get(col));
	            cell.setCellStyle(normalStyle);
	        }

	        int dataRowCount = dtoList.size();
	        for (int i = 0; i < dataRowCount; i++) {
	            YieldDMDDTO dto = dtoList.get(i);
	            Row row = sheet.createRow(currentRow++);
	            List<Object> rowData = new ArrayList<>();
	            rowData.add(dto.getParticulars());

	            rowData.add(dto.getFiveFC2C3());
	            rowData.add(dto.getFiveFPropane());
	            rowData.add(dto.getFiveFEthane());
	            rowData.add(dto.getFiveFDSC2C3());
	            rowData.add(dto.getFiveFDSPropane());
	            rowData.add(dto.getFiveFDSEthane());
	            rowData.add(dto.getFourFC2C3());
	            rowData.add(dto.getFourFPropane());
	            rowData.add(dto.getFourFEthane());
	            rowData.add(dto.getSevenFC2C3());
	            rowData.add(dto.getSevenFPropane());
	            rowData.add(dto.getSevenFEthane());
	            rowData.add(dto.getSixFSFDC2C3());
	            rowData.add(dto.getSixFSFDPropane());
	            rowData.add(dto.getSixFSFDEthane());
	            rowData.add(dto.getSixFBFDC2C3());
	            rowData.add(dto.getSixFBFDPropane());
	            rowData.add(dto.getSixFBFDEthane());
	            rowData.add(dto.getThreeFC2C3());
	            rowData.add(dto.getThreeFPropane());
	            rowData.add(dto.getThreeFEthane());
	    	    rowData.add(dto.getFourF2SC2C3());
	    	    rowData.add(dto.getFourF2SPropane());
	    	    rowData.add(dto.getFourF2SEthane());
	    	    
	            if (isAfterSave) {
	                rowData.add(dto.getSaveStatus());
	                rowData.add(dto.getErrDescription());
	            }

	            boolean isLastRow = (i == dataRowCount - 1);
	            CellStyle styleToUse = isLastRow ? totalRowStyle : normalStyle;

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
	                cell.setCellStyle(styleToUse);
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

	@Override
	public AOPMessageVM importYieldExcel(String year,UUID plantId,MultipartFile file) {
		// TODO Auto-generated method stub
		try {
			List<YieldDTO> data = readYieldData(file.getInputStream(), plantId, year);
			 AOPMessageVM aopMessageVM = updateSpyroOutputYieldData( plantId.toString(),  year, data);
			 Map<String, Object> map = (Map<String, Object>) aopMessageVM.getData();

			List<YieldDTO> failedList = (List<YieldDTO>) map.get("Failed");
			if (failedList != null && failedList.size() > 0) {
				byte[] fileByteArray = exportYieldReport(year, plantId.toString(), true, failedList);
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
	public AOPMessageVM importYieldDMD(String year,UUID plantId,MultipartFile file) {
		// TODO Auto-generated method stub
		try {
			List<YieldDMDDTO> data = readYieldDMD(file.getInputStream(), plantId, year);
			 AOPMessageVM aopMessageVM = updateSpyroOutputYieldDMD( plantId.toString(),  year, data);
			 Map<String, Object> map = (Map<String, Object>) aopMessageVM.getData();

			List<YieldDMDDTO> failedList = (List<YieldDMDDTO>) map.get("Failed");
			if (failedList != null && failedList.size() > 0) {
				byte[] fileByteArray = exportYieldDMD(year, plantId.toString(), true, failedList);
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

	public List<YieldDTO> readYieldData(InputStream inputStream, UUID plantFKId, String year) {
	    List<YieldDTO> yieldList = new ArrayList<>();

	    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
	        Sheet sheet = workbook.getSheetAt(0);

	        int lastRowNum = sheet.getLastRowNum();  // highest row index (0-based)
	        Iterator<Row> rowIterator = sheet.iterator();

	        if (rowIterator.hasNext())
	            rowIterator.next();  // skip header

	        while (rowIterator.hasNext()) {
	            Row row = rowIterator.next();
	            if (row.getRowNum() == lastRowNum) {
	                // skip the last row
	                break;
	            }
	            YieldDTO dto = new YieldDTO();
	            try {
	                dto.setParticulars(getStringCellValue(row.getCell(0), dto));
	                dto.setFiveFC2C3(getNumericCellValue(row.getCell(1), dto));
	                dto.setFiveFPropane(getNumericCellValue(row.getCell(2), dto));
	                dto.setFiveFEthane(getNumericCellValue(row.getCell(3), dto));
	                dto.setFourFC2C3(getNumericCellValue(row.getCell(4), dto));
	                dto.setFourFPropane(getNumericCellValue(row.getCell(5), dto));
	                dto.setFourFEthane(getNumericCellValue(row.getCell(6), dto));
	                dto.setFourFDC2C3(getNumericCellValue(row.getCell(7), dto));
	                dto.setFourFDPropane(getNumericCellValue(row.getCell(8), dto));
	                dto.setFourFDEthane(getNumericCellValue(row.getCell(9), dto));
	            } catch (Exception e) {
	                e.printStackTrace();
	                dto.setErrDescription(e.getMessage());
	                dto.setSaveStatus("Failed");
	            }
	            yieldList.add(dto);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return yieldList;
	}

	public List<YieldDMDDTO> readYieldDMD(InputStream inputStream, UUID plantFKId, String year) {
	    List<YieldDMDDTO> yieldList = new ArrayList<>();

	    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
	        Sheet sheet = workbook.getSheetAt(0);

	        int lastRowNum = sheet.getLastRowNum();  
	        Iterator<Row> rowIterator = sheet.iterator();

	        if (rowIterator.hasNext())
	            rowIterator.next();  

	        while (rowIterator.hasNext()) {
	            Row row = rowIterator.next();
	            if (row.getRowNum() == lastRowNum) {
	                
	                break;
	            }
	            YieldDMDDTO dto = new YieldDMDDTO();
	            try {
	                dto.setParticulars(getStringCellValue(row.getCell(0), dto));
	                dto.setFiveFC2C3(getNumericCellValue(row.getCell(1), dto));
	                dto.setFiveFPropane(getNumericCellValue(row.getCell(2), dto));
	                dto.setFiveFEthane(getNumericCellValue(row.getCell(3), dto));
	                dto.setFiveFDSC2C3(getNumericCellValue(row.getCell(4), dto));
	                dto.setFiveFDSPropane(getNumericCellValue(row.getCell(5), dto));
	                dto.setFiveFDSEthane(getNumericCellValue(row.getCell(6), dto));
	                dto.setFourFC2C3(getNumericCellValue(row.getCell(7), dto));
	                dto.setFourFPropane(getNumericCellValue(row.getCell(8), dto));
	                dto.setFourFEthane(getNumericCellValue(row.getCell(9), dto));
	                dto.setSevenFC2C3(getNumericCellValue(row.getCell(10), dto));
	                dto.setSevenFPropane(getNumericCellValue(row.getCell(11), dto));
	                dto.setSevenFEthane(getNumericCellValue(row.getCell(12), dto));
	                dto.setSixFSFDC2C3(getNumericCellValue(row.getCell(13), dto));
	                dto.setSixFSFDPropane(getNumericCellValue(row.getCell(14), dto));
	                dto.setSixFSFDEthane(getNumericCellValue(row.getCell(15), dto));
	               dto.setSixFBFDC2C3(getNumericCellValue(row.getCell(16), dto));
	               dto.setSixFBFDPropane(getNumericCellValue(row.getCell(17), dto));
	               dto.setSixFBFDEthane(getNumericCellValue(row.getCell(18), dto));
	               dto.setThreeFC2C3(getNumericCellValue(row.getCell(19), dto));
	               dto.setThreeFPropane(getNumericCellValue(row.getCell(20), dto));
	               dto.setThreeFEthane(getNumericCellValue(row.getCell(21), dto));
	               dto.setFourF2SC2C3(getNumericCellValue(row.getCell(22), dto));
	               dto.setFourF2SPropane(getNumericCellValue(row.getCell(23), dto));
	               dto.setFourF2SEthane(getNumericCellValue(row.getCell(24), dto));
	            } catch (Exception e) {
	                e.printStackTrace();
	                dto.setErrDescription(e.getMessage());
	                dto.setSaveStatus("Failed");
	            }
	            yieldList.add(dto);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return yieldList;
	}

	private static String getStringCellValue(Cell cell, YieldDTO dto) {
	    try {
	        if (cell == null || cell.getCellType() == CellType.BLANK) {
	            return null;
	        }
	        
	        cell.setCellType(CellType.STRING);
	        String val = cell.getStringCellValue().trim();
	        
	        // Return null if the string is empty after trimming
	        return val.isEmpty() ? null : val;
	        
	    } catch (Exception e) {
	        dto.setSaveStatus("Failed");
	        dto.setErrDescription("Please enter correct values");
	        e.printStackTrace();
	    }
	    return null;
	}
	private static Double getNumericCellValue(Cell cell, YieldDTO dto) {
	    if (cell == null || cell.getCellType() == CellType.BLANK) {
	        return null;
	    }

	    if (cell.getCellType() == CellType.NUMERIC) {
	        return cell.getNumericCellValue();
	    } 
	    
	    if (cell.getCellType() == CellType.STRING) {
	        String val = cell.getStringCellValue().trim();
	        if (val.isEmpty()) {
	            return null; // Return null for blank strings
	        }
	        try {
	            return Double.parseDouble(val);
	        } catch (NumberFormatException e) {
	            dto.setSaveStatus("Failed");
	            dto.setErrDescription("Please enter numeric values");
	        }
	    }
	    return null;
	}
	private static String getStringCellValue(Cell cell, YieldDMDDTO dto) {
	    try {
	        if (cell == null || cell.getCellType() == CellType.BLANK) {
	            return null;
	        }
	        
	        cell.setCellType(CellType.STRING);
	        String val = cell.getStringCellValue().trim();
	        
	        // Return null if the string is empty after trimming
	        return val.isEmpty() ? null : val;
	        
	    } catch (Exception e) {
	        dto.setSaveStatus("Failed");
	        dto.setErrDescription("Please enter correct values");
	        e.printStackTrace();
	    }
	    return null;
	}
	private static Double getNumericCellValue(Cell cell, YieldDMDDTO dto) {
	    if (cell == null || cell.getCellType() == CellType.BLANK) {
	        return null;
	    }

	    if (cell.getCellType() == CellType.NUMERIC) {
	        return cell.getNumericCellValue();
	    } 
	    
	    if (cell.getCellType() == CellType.STRING) {
	        String val = cell.getStringCellValue().trim();
	        if (val.isEmpty()) {
	            return null; // Return null for blank strings
	        }
	        try {
	            return Double.parseDouble(val);
	        } catch (NumberFormatException e) {
	            dto.setSaveStatus("Failed");
	            dto.setErrDescription("Please enter numeric values");
	        }
	    }
	    return null;
	}

	public static Boolean getBooleanCellValue(Cell cell, YieldDTO dto) {
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


	
	private CellStyle createBoldBorderedStyle(Workbook workbook) {
		CellStyle style = createBorderedStyle(workbook);
		Font font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);
		return style;
	}
	
	private CellStyle createBorderedStyle(Workbook wb) {
		CellStyle style = wb.createCellStyle();
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		return style;
	}


	@Override
	public AOPMessageVM updateSpyroOutputYieldData(String plantId, String year,
			List<YieldDTO> yieldDTOs) {
		
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<NormAttributeTransactions> normAttributeTransactionsList = new ArrayList<>();
		List<YieldDTO> failedList = new ArrayList<>();
		List<YieldParticularDTO> yieldParticularDTOs = makeNormParameterName(yieldDTOs);
		for(YieldDTO yieldDTO:yieldDTOs) {
			if (yieldDTO.getSaveStatus() != null
					&& yieldDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
				failedList.add(yieldDTO);
				continue;
			}
		}
		
		try {
			for(YieldParticularDTO yieldParticularDTO:yieldParticularDTOs) {
				
				String normParameterName=yieldParticularDTO.getNormParameterName();
				Optional<NormParameters> normParameterOpt=normParametersRepository.findFirstOneByNameAndPlantFkId(normParameterName, UUID.fromString(plantId));
				if(normParameterOpt.isPresent()) {
					NormParameters normParameters = normParameterOpt.get();
					NormAttributeTransactions normAttributeTransactions=normAttributeTransactionsRepository.findByNormParameterFKIdAndAuditYear(normParameters.getId(),year);
					if(normAttributeTransactions==null) {
						normAttributeTransactions=new NormAttributeTransactions();
						normAttributeTransactions.setAopMonth(4);
						normAttributeTransactions.setNormParameterFKId(normParameters.getId());
						if(yieldParticularDTO.getValue()!=null) {
							normAttributeTransactions.setAttributeValue(yieldParticularDTO.getValue().toString());
						}
						
						normAttributeTransactions.setAuditYear(year);
						normAttributeTransactions.setCreatedOn(new Date());
						normAttributeTransactions.setUserName(Utility.getUserName());
						normAttributeTransactionsList.add(normAttributeTransactionsRepository.save(normAttributeTransactions));
					}else {
						if(yieldParticularDTO.getValue()!=null) {
							normAttributeTransactions.setAttributeValue(yieldParticularDTO.getValue().toString());
						}
						normAttributeTransactionsList.add(normAttributeTransactionsRepository.save(normAttributeTransactions));
					}
				}
			
			}
			List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("spyro-output");
			for (ScreenMapping screenMapping : screenMappingList) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data updated successfully");
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("Success", normAttributeTransactionsList);
			map.put("Failed", failedList);
			aopMessageVM.setData(map);
			return aopMessageVM;
		}catch (Exception ex) {
			throw new RuntimeException("Failed to update data", ex);
		}
	}
	
	@Override
	public AOPMessageVM updateSpyroOutputYieldDMD(String plantId, String year,
			List<YieldDMDDTO> yieldDTOs) {
		
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<NormAttributeTransactions> normAttributeTransactionsList = new ArrayList<>();
		List<YieldDMDDTO> failedList = new ArrayList<>();
		List<YieldParticularDTO> yieldParticularDTOs = makeNormParameterNameDMD(yieldDTOs);
		for(YieldDMDDTO yieldDTO:yieldDTOs) {
			if (yieldDTO.getSaveStatus() != null
					&& yieldDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
				failedList.add(yieldDTO);
				continue;
			}
		}
		
		try {
			for(YieldParticularDTO yieldParticularDTO:yieldParticularDTOs) {
				
				String normParameterName=yieldParticularDTO.getNormParameterName();
				Optional<NormParameters> normParameterOpt=normParametersRepository.findFirstOneByNameAndPlantFkId(normParameterName, UUID.fromString(plantId));
				if(normParameterOpt.isPresent()) {
					NormParameters normParameters = normParameterOpt.get();
					NormAttributeTransactions normAttributeTransactions=normAttributeTransactionsRepository.findByNormParameterFKIdAndAuditYear(normParameters.getId(),year);
					if(normAttributeTransactions==null) {
						normAttributeTransactions=new NormAttributeTransactions();
						normAttributeTransactions.setAopMonth(4);
						normAttributeTransactions.setNormParameterFKId(normParameters.getId());
						if(yieldParticularDTO.getValue()!=null) {
							normAttributeTransactions.setAttributeValue(yieldParticularDTO.getValue().toString());
						}
						
						normAttributeTransactions.setAuditYear(year);
						normAttributeTransactions.setCreatedOn(new Date());
						normAttributeTransactions.setUserName(Utility.getUserName());
						normAttributeTransactionsList.add(normAttributeTransactionsRepository.save(normAttributeTransactions));
					}else {
						if(yieldParticularDTO.getValue()!=null) {
							normAttributeTransactions.setAttributeValue(yieldParticularDTO.getValue().toString());
						}
						normAttributeTransactionsList.add(normAttributeTransactionsRepository.save(normAttributeTransactions));
					}
				}
			
			}
			List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("spyro-output");
			for (ScreenMapping screenMapping : screenMappingList) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data updated successfully");
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("Success", normAttributeTransactionsList);
			map.put("Failed", failedList);
			aopMessageVM.setData(map);
			return aopMessageVM;
		}catch (Exception ex) {
			throw new RuntimeException("Failed to update data", ex);
		}
	}
	
	private List<YieldParticularDTO> makeNormParameterName(List<YieldDTO> yieldDTOs) {
		List<YieldParticularDTO> yieldParticularDTOs = new ArrayList<YieldParticularDTO>();
		for(YieldDTO dto:yieldDTOs) {
			String particulars = dto.getParticulars();
		    if (particulars == null) {
		        return null;
		    }
		    
		    // priority order of checking fields
		    if (dto.getFourFPropane() != null) {
		        String fourFPropane ="4F_" + particulars + "_Propane";
		        YieldParticularDTO yieldParticularDTO = new YieldParticularDTO();
		        yieldParticularDTO.setNormParameterName(fourFPropane);
		        yieldParticularDTO.setValue(dto.getFourFPropane());
		        yieldParticularDTOs.add(yieldParticularDTO);
		    }
		    if (dto.getFourFEthane() != null) {
		    	String fourFEthane = "4F_" + particulars + "_Ethane";
		    	YieldParticularDTO yieldParticularDTO = new YieldParticularDTO();
		    	yieldParticularDTO.setNormParameterName(fourFEthane);
		    	yieldParticularDTO.setValue(dto.getFourFEthane());
		    	 yieldParticularDTOs.add(yieldParticularDTO);
		    }
		    if (dto.getFourFC2C3() != null) {
		    	YieldParticularDTO yieldParticularDTO = new YieldParticularDTO();
		        String fourFC2C3= "4F_" + particulars + "_C2C3";
		        yieldParticularDTO.setNormParameterName(fourFC2C3);
		        yieldParticularDTO.setValue(dto.getFourFC2C3());
		        yieldParticularDTOs.add(yieldParticularDTO);
		    }
		    if (dto.getFourFDC2C3() != null) {
		    	YieldParticularDTO yieldParticularDTO = new YieldParticularDTO();
		        String fourFDC2C3= "4FD_" + particulars + "_C2C3";
		        yieldParticularDTO.setNormParameterName(fourFDC2C3);
		        yieldParticularDTO.setValue(dto.getFourFDC2C3());
		        yieldParticularDTOs.add(yieldParticularDTO);
		    }
		    if (dto.getFourFDPropane() != null) {
		    	YieldParticularDTO yieldParticularDTO = new YieldParticularDTO();
		        String fourFDPropane= "4FD_" + particulars + "_Propane";
		        yieldParticularDTO.setNormParameterName(fourFDPropane);
		        yieldParticularDTO.setValue(dto.getFourFDPropane());
		        yieldParticularDTOs.add(yieldParticularDTO);
		    }
		    if (dto.getFourFDEthane() != null) {
		    	YieldParticularDTO yieldParticularDTO = new YieldParticularDTO();
		        String fourFDEthane= "4FD_" + particulars + "_Ethane";
		        yieldParticularDTO.setNormParameterName(fourFDEthane);
		        yieldParticularDTO.setValue(dto.getFourFDEthane());
		        yieldParticularDTOs.add(yieldParticularDTO);
		    }
		    if (dto.getFiveFPropane() != null) {
		    	YieldParticularDTO yieldParticularDTO = new YieldParticularDTO();
		        String  fiveFPropane="5F_" + particulars + "_Propane";
		        yieldParticularDTO.setNormParameterName(fiveFPropane);
		        yieldParticularDTO.setValue(dto.getFiveFPropane());
		        yieldParticularDTOs.add(yieldParticularDTO);
		    }
		    if (dto.getFiveFEthane() != null) {
		    	YieldParticularDTO yieldParticularDTO = new YieldParticularDTO();
		        String fiveFEthane= "5F_" + particulars + "_Ethane";
		        yieldParticularDTO.setNormParameterName(fiveFEthane);
		        yieldParticularDTO.setValue(dto.getFiveFEthane());
		        yieldParticularDTOs.add(yieldParticularDTO);
		    }
		    if (dto.getFiveFC2C3() != null) {
		    	YieldParticularDTO yieldParticularDTO = new YieldParticularDTO();
		        String fiveFC2C3= "5F_" + particulars + "_C2C3";
		        yieldParticularDTO.setNormParameterName(fiveFC2C3);
		        yieldParticularDTO.setValue(dto.getFiveFC2C3());
		        yieldParticularDTOs.add(yieldParticularDTO);
		    }
		}
		return yieldParticularDTOs;
	 }

	private List<YieldParticularDTO> makeNormParameterNameDMD(List<YieldDMDDTO> yieldDTOs) {
	    List<YieldParticularDTO> yieldParticularDTOs = new ArrayList<>();
	    
	    for (YieldDMDDTO dto : yieldDTOs) {
	        String part = dto.getParticulars();
	        if (part == null) continue; 

	        addToList(yieldParticularDTOs, "5F_" + part + "_C2C3", dto.getFiveFC2C3());
	        addToList(yieldParticularDTOs, "5F_" + part + "_Propane", dto.getFiveFPropane());
	        addToList(yieldParticularDTOs, "5F_" + part + "_Ethane", dto.getFiveFEthane());
	        addToList(yieldParticularDTOs, "5F_D_S_" + part + "_C2C3", dto.getFiveFDSC2C3());
	        addToList(yieldParticularDTOs, "5F_D_S_" + part + "_Propane", dto.getFiveFDSPropane());
	        addToList(yieldParticularDTOs, "5F_D_S_" + part + "_Ethane", dto.getFiveFDSEthane());
	        addToList(yieldParticularDTOs, "6F+SFD_" + part + "_C2C3", dto.getSixFSFDC2C3());
	        addToList(yieldParticularDTOs, "6F+SFD_" + part + "_Propane", dto.getSixFSFDPropane());
	        addToList(yieldParticularDTOs, "6F+SFD_" + part + "_Ethane", dto.getSixFSFDEthane());
	        addToList(yieldParticularDTOs, "6F+BFD_" + part + "_C2C3", dto.getSixFBFDC2C3());
	        addToList(yieldParticularDTOs, "6F+BFD_" + part + "_Propane", dto.getSixFBFDPropane());
	        addToList(yieldParticularDTOs, "6F+BFD_" + part + "_Ethane", dto.getSixFBFDEthane());
	        addToList(yieldParticularDTOs, "4F_" + part + "_C2C3", dto.getFourFC2C3());
	        addToList(yieldParticularDTOs, "4F_" + part + "_Propane", dto.getFourFPropane());
	        addToList(yieldParticularDTOs, "4F_" + part + "_Ethane", dto.getFourFEthane());
	        addToList(yieldParticularDTOs, "7F_" + part + "_C2C3", dto.getSevenFC2C3());
	        addToList(yieldParticularDTOs, "7F_" + part + "_Propane", dto.getSevenFPropane());
	        addToList(yieldParticularDTOs, "7F_" + part + "_Ethane", dto.getSevenFEthane());

	      	addToList(yieldParticularDTOs, "3F_" + part + "_C2C3", dto.getThreeFC2C3());
	        addToList(yieldParticularDTOs, "3F_" + part + "_Propane", dto.getThreeFPropane());
	        addToList(yieldParticularDTOs, "3F_" + part + "_Ethane", dto.getThreeFEthane());

	        addToList(yieldParticularDTOs, "4F+2SFD1BFD_" + part + "_C2C3", dto.getFourF2SC2C3());
	        addToList(yieldParticularDTOs, "4F+2SFD1BFD_" + part + "_Propane", dto.getFourF2SPropane());
	        addToList(yieldParticularDTOs, "4F+2SFD1BFD_" + part + "_Ethane", dto.getFourF2SEthane());
	    }
	    
	    return yieldParticularDTOs;
	}
	
	private void addToList(List<YieldParticularDTO> list, String name, Double value) {
	    if (value != null) {
	        YieldParticularDTO item = new YieldParticularDTO();
	        item.setNormParameterName(name);
	        item.setValue(value);
	        list.add(item);
	    }
	}
	
	public byte[] createExcel(String year, String plantId, String mode, boolean isAfterSave,
			Map<String, List<SpyroOutputDTO>> mapForExcel) {
		try {
			String structureJson = getJson();
			ObjectMapper mapper = new ObjectMapper();
			Map<String, List<List<Object>>> data = new HashMap<>();
			Map<String, Object> structure = mapper.readValue(structureJson, Map.class);
			Map<String, List<Map<String, Object>>> spyroOutputDataListMap = new HashMap<>();
			if (!isAfterSave) {
				AOPMessageVM vm = getSpyroOutputData(year, plantId, mode, "Composition");
				List<Map<String, Object>> spyroOutputDataList = (List<Map<String, Object>>) vm.getData();
				spyroOutputDataListMap = Utility.groupByNormParameterTypeName(spyroOutputDataList);
			}

			for (String sheetName : structure.keySet()) {
				Map<String, Object> sheetData = (Map<String, Object>) structure.get(sheetName);
				List<Map<String, Object>> tables = (List<Map<String, Object>>) sheetData.get(ExcelConstants.TABLES);

				for (Map<String, Object> table : tables) {
					String title = (String) table.get(ExcelConstants.TITLE);
					String tableId = (String) table.get(ExcelConstants.TABLEID);
					String dataInput = (String) table.get(ExcelConstants.DATA_INPUT);
					List<String> headers = (List<String>) table.get(ExcelConstants.HEADERS);
					boolean hideTable = (boolean) table.get(ExcelConstants.HIDE_TABLE);
					Integer startingIndexofMonths = (Integer) table.get(ExcelConstants.STARTING_INDEX_OF_MONTHS);
					List<List<String>> headersOuterTitles = (List<List<String>>) table
							.get(ExcelConstants.HEADERSTITLES);
					headersOuterTitles.get(0).addAll(startingIndexofMonths,
							excelUtilityService.getAcademicYearMonths(year));
					List<List<Object>> dataList = new ArrayList<>();
					if (isAfterSave) {
						if(!mapForExcel.containsKey(tableId)){
							hideTable = true;
							continue;
						}
						headers.add("saveStatus");
						headers.add("errDescription");
						headersOuterTitles.get(0).add("SaveStatus");
						headersOuterTitles.get(0).add("ErrDescription");


						for (SpyroOutputDTO dto : mapForExcel.get(tableId)) {

							List<Object> list = new ArrayList<>();
							for (String fieldName : headers) {
								String methodName = "get" + capitalize(fieldName);
								Method method = dto.getClass().getMethod(methodName);
								Object value = method.invoke(dto);
								list.add(value);
							}
							list.add(tableId);
							UUID normParameterFKId = UUID.fromString(dto.getNormParameterFKID());
							Optional<NormParameters> optionNormParameters = normParametersRepository.findById(normParameterFKId);
							if(optionNormParameters.isPresent()) {
								list.add(optionNormParameters.get().getIsEditable());
							}
							dataList.add(list);
						}

					} else {

						List<Map<String, Object>> spyroOutputDataList = new ArrayList<>();
						if (dataInput.equalsIgnoreCase("Composition")) {
							if(spyroOutputDataListMap.containsKey(title)){
								spyroOutputDataList = spyroOutputDataListMap.get(title);
							}else{
								hideTable = true;
								continue;
							}
						} else {
							AOPMessageVM vm = getSpyroOutputData(year, plantId, mode, dataInput);
							spyroOutputDataList = (List<Map<String, Object>>) vm.getData();
							System.out.println("sheetName " + sheetName + " " + spyroOutputDataList);
						}

						if(spyroOutputDataList==null ||spyroOutputDataList.isEmpty()){
							hideTable = true;
							continue;
						}
						// Data rows
						for (Map<String, Object> map : spyroOutputDataList) {
							List<Object> list = new ArrayList<>();
							for (String header : headers) {
								System.out.println("header " + header);
								list.add(map.get(header));
							}
							list.add(tableId);
							list.add(map.get("isEditable"));
							dataList.add(list);
						}

					}

					System.out.println("datalist " + dataList);
					data.put(tableId, dataList);
				}
			}
			System.out.println("data in calling method " + data);
			return excelUtilityService.generateFlexibleExcel(structure, data);

		} catch (Exception e) {
			e.printStackTrace();

		}
		return null;
	}
	
	@Override
	public AOPMessageVM importExcel(String year, String plantFKId, String mode, MultipartFile file) {
		// TODO Auto-generated method stub
		if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
			throw new IllegalArgumentException("Invalid or empty Excel file.");
		}

		try {

			System.out.println("started Read spyroOutput in importExcel");
			Map<String, List<SpyroOutputDTO>> map = readSpyroOutputExcel(file.getInputStream(), year);
			System.out.println("Ended Read spyroOutput in importExcel");
			System.out.println("Started Save spyroOutput in importExcel");
			Map<String, List<SpyroOutputDTO>> mapForExcel = new HashMap<>();
			List<SpyroOutputDTO> failedRecords = new ArrayList<>();
			for (String key : map.keySet()) {
				AOPMessageVM vm = updateSpyroOutputData(year,plantFKId,map.get(key));
				List<SpyroOutputDTO> failedList = (List<SpyroOutputDTO>) vm.getData();
				failedRecords.addAll(failedList);
				mapForExcel.put(key, failedList);
			}

			System.out.println("Ended Save spyroOutput in importExcel");
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = createExcel(year, plantFKId, mode, true, mapForExcel);
				String base64File = Base64.getEncoder().encodeToString(fileByteArray);
				aopMessageVM.setData(base64File);
				aopMessageVM.setCode(400);
				aopMessageVM.setMessage("Partial data has been saved");
			} else {
				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("All data has been saved");
			}

			return aopMessageVM;
			// return ResponseEntity.ok(data);
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public Map<String, List<SpyroOutputDTO>> readSpyroOutputExcel(InputStream inputStream, String year) {

		Map<String, List<SpyroOutputDTO>> map = new HashMap<>();
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {

			
				Sheet sheet = workbook.getSheetAt(0);
				Iterator<Row> rowIterator = sheet.iterator();
				List<SpyroOutputDTO> spyroOutputDTOs = new ArrayList<>();
				if (rowIterator.hasNext())
					rowIterator.next(); // Skip header

				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					Cell tableIdCell = row.getCell(16, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                	if (tableIdCell == null || tableIdCell.getCellType() != CellType.STRING) {
                    	continue;
                	}

					SpyroOutputDTO dto = new SpyroOutputDTO();

					try {
						
						dto.setParticulars(getStringCellValue(row.getCell(0), dto));
						dto.setUom(getStringCellValue(row.getCell(1), dto));
						dto.setAuditYear(year);
						dto.setApr(getNumericCellValue(row.getCell(2), dto));
						dto.setMay(getNumericCellValue(row.getCell(3), dto));
						dto.setJun(getNumericCellValue(row.getCell(4), dto));
						dto.setJul(getNumericCellValue(row.getCell(5), dto));
						dto.setAug(getNumericCellValue(row.getCell(6), dto));
						dto.setSep(getNumericCellValue(row.getCell(7), dto));
						dto.setOct(getNumericCellValue(row.getCell(8), dto));
						dto.setNov(getNumericCellValue(row.getCell(9), dto));
						dto.setDec(getNumericCellValue(row.getCell(10), dto));
						dto.setJan(getNumericCellValue(row.getCell(11), dto));
						dto.setFeb(getNumericCellValue(row.getCell(12), dto));
						dto.setMar(getNumericCellValue(row.getCell(13), dto));
						dto.setRemarks(getStringCellValue(row.getCell(14), dto));
						dto.setNormParameterFKID(getStringCellValue(row.getCell(15), dto));
						dto.setTableId(getStringCellValue(row.getCell(16), dto));

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
	
	private static String getStringCellValue(Cell cell, SpyroOutputDTO dto) {
		try {
			if (cell == null)
				return null;
			cell.setCellType(CellType.STRING);
			return cell.getStringCellValue().trim();
		} catch (Exception e) {
			dto.setSaveStatus("Failed");
			dto.setErrDescription("Please enter correct values");
			e.printStackTrace();
		}
		return null;

	}

	private static Double getNumericCellValue(Cell cell, SpyroOutputDTO dto) {
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

	public static Boolean getBooleanCellValue(Cell cell) {
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

	
	String getJson() {
		return "{\r\n" + //
						"    \"SpyroOutput\": {\r\n" + //
						"        \"columnCount\":13,\r\n" + //
						"        \"tables\": [\r\n" + //
						"            {\r\n" + //
						"                \"startRow\": 0,\r\n" + //
						"                \"headers\": [\r\n" + //
						"\t\t\t\t\t \r\n" + //
						"\t\t\t\t\t\"particulars\", \r\n" + //
						"\t\t\t\t\t\"uom\", \r\n" + //
						"\t\t\t\t\t\"apr\", \r\n" + //
						"\t\t\t\t\t\"may\", \r\n" + //
						"\t\t\t\t\t\"jun\", \r\n" + //
						"\t\t\t\t\t\"jul\", \r\n" + //
						"\t\t\t\t\t\"aug\", \r\n" + //
						"\t\t\t\t\t\"sep\", \r\n" + //
						"\t\t\t\t\t\"oct\", \r\n" + //
						"\t\t\t\t\t\"nov\", \r\n" + //
						"\t\t\t\t\t\"dec\",\r\n" + //
						"                    \"jan\", \r\n" + //
						"\t\t\t\t\t\"feb\", \r\n" + //
						"\t\t\t\t\t\"mar\", \r\n" + //
						"\t\t\t\t\t\"remarks\",\r\n" + //
						"                    \"normParameterFKID\"\r\n" + //
						"                ],\r\n" + //
						"                \"startingIndexOfMonths\":2,\r\n" + //
						"                \"hideTable\":false,\r\n" + //
						"                \"textBeforeTitle\":\"\",\r\n" + //
						"                \"title\":\"TotalFeed\",\r\n" + //
						"                \"tableId\":\"TotalFeed\",\r\n" + //
						"                \"dataInput\":\"Total Feed\",\r\n" + //
						"                \"isColumnMergeRequired\":false,\r\n" + //
						"                \"isRowMergeRequired\":false,\r\n" + //
						"                \"headersTitles\":[[\r\n" + //
						"                    \"Particulars\",\r\n" + //
						"                    \"UOM\",\r\n" + //
						"                    \"Remark\",\"NormParameterFKID\"]],\r\n" + //
						"                \"rows\": [],\r\n" + //
						"                \"hiddenColumns\":[15,16,18],\r\n" + //
						"                \"styles\": {\r\n" + //
						"                    \"boldColumns\": [\r\n" + //
						"                        0\r\n" + //
						"                    ],\r\n" + //
						"                    \"borders\": true\r\n" + //
						"                },\r\n" + //
						"                \"autoMerge\": {\r\n" + //
						"                    \"columns\": [],\r\n" + //
						"                    \"rows\": []\r\n" + //
						"                }\r\n" + //
						"            },\r\n" + //
						"            {\r\n" + //
						"                \"startRow\": 0,\r\n" + //
						"                \"headers\": [\r\n" + //
						"\t\t\t\t\t\"particulars\", \r\n" + //
						"\t\t\t\t\t\"uom\", \r\n" + //
						"\t\t\t\t\t\"apr\", \r\n" + //
						"\t\t\t\t\t\"may\", \r\n" + //
						"\t\t\t\t\t\"jun\", \r\n" + //
						"\t\t\t\t\t\"jul\", \r\n" + //
						"\t\t\t\t\t\"aug\", \r\n" + //
						"\t\t\t\t\t\"sep\", \r\n" + //
						"\t\t\t\t\t\"oct\", \r\n" + //
						"\t\t\t\t\t\"nov\", \r\n" + //
						"\t\t\t\t\t\"dec\",\r\n" + //
						"                    \"jan\", \r\n" + //
						"\t\t\t\t\t\"feb\", \r\n" + //
						"\t\t\t\t\t\"mar\", \r\n" + //
						"\t\t\t\t\t\"remarks\",\r\n" + //
						"                    \"normParameterFKID\"\r\n" + //
						"                ],\r\n" + //
						"                \"startingIndexOfMonths\":2,\r\n" + //
						"                \"hideTable\":false,\r\n" + //
						"                \"textBeforeTitle\":\"\",\r\n" + //
						"                \"title\":\"Total Products\",\r\n" + //
						"                \"tableId\":\"Total_Products\",\r\n" + //
						"                \"dataInput\":\"Total Products\",\r\n" + //
						"                \"isColumnMergeRequired\":false,\r\n" + //
						"                \"isRowMergeRequired\":false,\r\n" + //
						"                \"headersTitles\":[[\r\n" + //
						"                    \"Particulars\",\r\n" + //
						"                    \"UOM\",\r\n" + //
						"                    \"Remark\",\"NormParameterFKID\"]],\r\n" + //
						"                \"rows\": [],\r\n" + //
						"                \"hiddenColumns\":[15,16,18],\r\n" + //
						"                \"styles\": {\r\n" + //
						"                    \"boldColumns\": [\r\n" + //
						"                        0\r\n" + //
						"                    ],\r\n" + //
						"                    \"borders\": true\r\n" + //
						"                },\r\n" + //
						"                \"autoMerge\": {\r\n" + //
						"                    \"columns\": [],\r\n" + //
						"                    \"rows\": []\r\n" + //
						"                }\r\n" + //
						"            },\r\n" + //
						"            {\r\n" + //
						"                \"startRow\": 0,\r\n" + //
						"                \"headers\": [\r\n" + //
						"\t\t\t\t\t\"particulars\", \r\n" + //
						"\t\t\t\t\t\"uom\", \r\n" + //
						"\t\t\t\t\t\"apr\", \r\n" + //
						"\t\t\t\t\t\"may\", \r\n" + //
						"\t\t\t\t\t\"jun\", \r\n" + //
						"\t\t\t\t\t\"jul\", \r\n" + //
						"\t\t\t\t\t\"aug\", \r\n" + //
						"\t\t\t\t\t\"sep\", \r\n" + //
						"\t\t\t\t\t\"oct\", \r\n" + //
						"\t\t\t\t\t\"nov\", \r\n" + //
						"\t\t\t\t\t\"dec\",\r\n" + //
						"                    \"jan\", \r\n" + //
						"\t\t\t\t\t\"feb\", \r\n" + //
						"\t\t\t\t\t\"mar\", \r\n" + //
						"\t\t\t\t\t\"remarks\",\r\n" + //
						"                    \"normParameterFKID\"\r\n" + //
						"                ],\r\n" + //
						"                \"startingIndexOfMonths\":2,\r\n" + //
						"                \"hideTable\":false,\r\n" + //
						"                \"textBeforeTitle\":\"\",\r\n" + //
						"                \"title\":\"Miscellaneous Parameters\",\r\n" + //
						"                \"tableId\":\"Miscellaneous_Parameters\",\r\n" + //
						"                \"dataInput\":\"Miscellaneous Parameters\",\r\n" + //
						"                \"isColumnMergeRequired\":false,\r\n" + //
						"                \"isRowMergeRequired\":false,\r\n" + //
						"                \"headersTitles\":[[\r\n" + //
						"                    \"Particulars\",\r\n" + //
						"                    \"UOM\",\r\n" + //
						"                    \"Remark\",\"NormParameterFKID\"]],\r\n" + //
						"                \"rows\": [],\r\n" + //
						"                \"hiddenColumns\":[15,16,18],\r\n" + //
						"                \"styles\": {\r\n" + //
						"                    \"boldColumns\": [\r\n" + //
						"                        0\r\n" + //
						"                    ],\r\n" + //
						"                    \"borders\": true\r\n" + //
						"                },\r\n" + //
						"                \"autoMerge\": {\r\n" + //
						"                    \"columns\": [],\r\n" + //
						"                    \"rows\": []\r\n" + //
						"                }\r\n" + //
						"            }\r\n" + // // Removed the extra malformed object here
						"\r\n" + //
						"        ]\r\n" + //
						"    }\r\n" + //
						"    \r\n" + //
						"}";
	}

	private static String capitalize(String str) {
		if (str == null || str.isEmpty())
			return str;
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}


}
