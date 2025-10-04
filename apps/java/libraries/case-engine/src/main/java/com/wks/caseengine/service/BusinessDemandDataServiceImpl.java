package com.wks.caseengine.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.BusinessDemand;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;

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

import com.wks.caseengine.dto.BusinessDemandDataDTO;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.BusinessDemandDataRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

@Service
public class BusinessDemandDataServiceImpl implements BusinessDemandDataService {

	@Autowired
	private BusinessDemandDataRepository businessDemandDataRepository;

	

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


	
	@Override
	public List<BusinessDemandDataDTO> getBusinessDemandData(String year, String plantId) {
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			String viewName = "vwScrn" + verticalName + "BusinessDemand";
			List<Object[]> obj = findByYearAndPlantFkId(year, UUID.fromString(plantId), viewName);
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

			return businessDemandDataDTOList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public byte[] exportBusinessDemand(String year, String plantId, boolean isAfterSave, List<BusinessDemandDataDTO> dtoList) {
		try {
			
			List<Boolean> isEditable = new ArrayList<>();

			if (!isAfterSave) {
				 dtoList = getBusinessDemandData(year,plantId);
			}

			Workbook workbook = new XSSFWorkbook();

			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;
			// List<List<Object>> rows = new ArrayList<>();

			List<List<Object>> rows = new ArrayList<>();
			
			// Data rows
			for (BusinessDemandDataDTO dto : dtoList) {
				//if (isAfterSave) {
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
					cell.setCellStyle(createBoldBorderedStyle(workbook));
				}
			}
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
				}
			}
			sheet.setColumnHidden(15, true);
			sheet.setColumnHidden(16, true);
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
	
	private CellStyle createBorderedStyle(Workbook wb) {
		CellStyle style = wb.createCellStyle();
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		return style;
	}

	private CellStyle createBoldStyle(Workbook wb) {
		Font font = wb.createFont();
		font.setBold(true);
		CellStyle style = wb.createCellStyle();
		style.setFont(font);
		return style;
	}

	private CellStyle createBoldBorderedStyle(Workbook workbook) {
		CellStyle style = createBorderedStyle(workbook);
		Font font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);
		return style;
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
	
	public List<BusinessDemandDataDTO> readBusinessDemand(InputStream inputStream, UUID plantFKId, String year) {
		List<BusinessDemandDataDTO> configList = new ArrayList<>();

		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();

			if (rowIterator.hasNext())
				rowIterator.next(); // Skip header

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				BusinessDemandDataDTO dto = new BusinessDemandDataDTO();
				try {
					dto.setDisplayName(getStringCellValue(row.getCell(0), dto));
					dto.setUOM(getStringCellValue(row.getCell(1), dto));
					dto.setApril(getNumericCellValue(row.getCell(2), dto));
					dto.setMay(getNumericCellValue(row.getCell(3), dto));
					dto.setJune(getNumericCellValue(row.getCell(4), dto));
					dto.setJuly(getNumericCellValue(row.getCell(5), dto));
					dto.setAug(getNumericCellValue(row.getCell(6), dto));
					dto.setSep(getNumericCellValue(row.getCell(7), dto));
					dto.setOct(getNumericCellValue(row.getCell(8), dto));
					dto.setNov(getNumericCellValue(row.getCell(9), dto));
					dto.setDec(getNumericCellValue(row.getCell(10), dto));
					dto.setJan(getNumericCellValue(row.getCell(11), dto));
					dto.setFeb(getNumericCellValue(row.getCell(12), dto));
					dto.setMarch(getNumericCellValue(row.getCell(13), dto));
					dto.setRemark(getStringCellValue(row.getCell(14), dto));
					dto.setId(getStringCellValue(row.getCell(15), dto));
					dto.setNormParameterId(getStringCellValue(row.getCell(16), dto));
					dto.setPlantId(plantFKId!=null ? plantFKId.toString():"");
					Plants plant = plantsRepository.findById(plantFKId)
							.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

					Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
							.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
					Sites site = siteRepository.findById(plant.getSiteFkId()).get();
					dto.setVerticalFKId(vertical.getId().toString());
					dto.setSiteFKId(site.getId().toString());
					dto.setYear(year);
					// dto.setMaterialFkId(getStringCellValue(row.getCell(17), dto));
					// dto.setIsEditable(getBooleanCellValue(row.getCell(18), dto));
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

	@Override
	public List<BusinessDemandDataDTO> saveBusinessDemandData(List<BusinessDemandDataDTO> businessDemandDataDTOList) {
		String year=null;
		UUID plantId=null;
		List<BusinessDemandDataDTO> failedList = new ArrayList<>();
		try {
			for (BusinessDemandDataDTO businessDemandDataDTO : businessDemandDataDTOList) {
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
				} else {
					businessDemand.setId(UUID.fromString(businessDemandDataDTO.getId()));
				}

				businessDemand.setJan(businessDemandDataDTO.getJan());
				businessDemand.setJuly(businessDemandDataDTO.getJuly());
				businessDemand.setJune(businessDemandDataDTO.getJune());
				businessDemand.setMarch(businessDemandDataDTO.getMarch());
				businessDemand.setMay(businessDemandDataDTO.getMay());

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
					plantId=UUID.fromString(businessDemandDataDTO.getPlantId());
					if (businessDemandDataDTO.getSiteFKId() != null) {
						businessDemand.setSiteFKId(UUID.fromString(businessDemandDataDTO.getSiteFKId()));
					}
					if (businessDemandDataDTO.getVerticalFKId() != null) {
						businessDemand.setVerticalFKId(UUID.fromString(businessDemandDataDTO.getVerticalFKId()));
					}
					businessDemandDataRepository.save(businessDemand);

				}
			} // TODO Auto-generated method stub
			
			Plants plant = plantsRepository.findById((plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
			
			if(vertical.getName().equalsIgnoreCase("Cracker")) {
				for(BusinessDemandDataDTO businessDemandDataDTO : businessDemandDataDTOList) {
					String normParameterName=normParametersRepository.findNormParameterName(UUID.fromString(businessDemandDataDTO.getNormParameterId()));
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
			throw new RuntimeException("Failed to save data", ex);
		}

	}
	
	void saveData(UUID normParameterFKId, Integer i, Double attributeValue, String remark, String plantId,
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
					+ "Site_FK_Id, Vertical_FK_Id,isEditable,isVisible,UOM,DisplayName " + "FROM " + viewName + " "
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

}
