package com.wks.caseengine.service;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.wks.caseengine.dto.PIOImpactDTO;
import com.wks.caseengine.entity.PIOImpact;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PIOImpactRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
@Service
public class PIOImpactServiceImpl implements PIOImpactService {
	
	@Autowired
	private PIOImpactRepository pioImpactRepository;
	
	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;
	
	@Autowired
	private PlantsRepository plantsRepository;

	@Override
	public AOPMessageVM getPIOImpact(String year, String plantId) {
		
		List<PIOImpactDTO> pioImpactDTOs=new ArrayList<PIOImpactDTO>();
		List<PIOImpact> pioImpacts=	pioImpactRepository.findByPlantIdAndAopYear(UUID.fromString(plantId), year);
		try {
			for(PIOImpact pioImpact:pioImpacts) {
				PIOImpactDTO pioImpactDTO = new PIOImpactDTO();
				pioImpactDTO.setDescription(pioImpact.getDescription());
				pioImpactDTO.setEndMonth(pioImpact.getEndMonth());
				pioImpactDTO.setId(pioImpact.getId().toString());
				if(pioImpact.getRemarks()!=null) {
					pioImpactDTO.setRemarks(pioImpact.getRemarks());
				}else {
					pioImpactDTO.setRemarks("");
				}
				pioImpactDTO.setStartMonth(pioImpact.getStartMonth());
				pioImpactDTO.setValue(pioImpact.getValue());
				pioImpactDTOs.add(pioImpactDTO);
			}
		}catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
		
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(pioImpactDTOs);
		aopMessageVM.setMessage("Data Fetched successfully");
		return aopMessageVM;
	}

	@Override
	public AOPMessageVM updatePIOImpact(String year, String plantId, List<PIOImpactDTO> pioImpactDTOs) {
		List<PIOImpactDTO> failedList = new ArrayList<PIOImpactDTO>();
		List<PIOImpact> pioImpacts = new ArrayList<PIOImpact>();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		Sites site = siteRepository.findById(plant.getSiteFkId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
		try {
			for(PIOImpactDTO pioImpactDTO :pioImpactDTOs) {
				if (pioImpactDTO.getSaveStatus() != null
						&& pioImpactDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(pioImpactDTO);
					continue;
				}
				PIOImpact pioImpact = null;
				if(pioImpactDTO.getId()!=null) {
					Optional<PIOImpact> pioImpactOpt=	pioImpactRepository.findById(UUID.fromString(pioImpactDTO.getId()));
					if(pioImpactOpt.isPresent()) {
						pioImpact=pioImpactOpt.get();
					}else {
						pioImpactDTO.setErrDescription("No record found with this Id");
						pioImpactDTO.setSaveStatus("Failed");
					}
				}else {
					pioImpact = new PIOImpact();
				}
				pioImpact.setDescription(pioImpactDTO.getDescription());
				pioImpact.setEndMonth(pioImpactDTO.getEndMonth());
				pioImpact.setRemarks(pioImpactDTO.getRemarks());
				pioImpact.setStartMonth(pioImpactDTO.getStartMonth());
				pioImpact.setValue(pioImpactDTO.getValue());
				pioImpact.setPlantId(UUID.fromString(plantId));
				pioImpact.setAopYear(year);
				pioImpact.setSiteId(site.getId());
				pioImpact.setVerticalId(vertical.getId());
				pioImpacts.add(pioImpactRepository.save(pioImpact));
			}
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to update data", e);
		}
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(failedList);
		aopMessageVM.setMessage("Data updated successfully");
		// TODO Auto-generated method stub
		return aopMessageVM;
	}

	@Override
	public AOPMessageVM deletePIOImpact(UUID id) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Optional<PIOImpact> pioImpactDTO=pioImpactRepository.findById(id);
			if(pioImpactDTO.isPresent()) {
				pioImpactRepository.delete(pioImpactDTO.get());
			}
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to delete data", e);
		}
		
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Deleted successfully");
		// TODO Auto-generated method stub
		return aopMessageVM;
	}
	
	public byte[] exportPIOImpact(String year, String plantId, boolean isAfterSave, List<PIOImpactDTO> dtoList) {
		try {
			
			AOPMessageVM aopMessageVM = getPIOImpact(year,plantId);
			
			List<Boolean> isEditable = new ArrayList<>();

			if (!isAfterSave) {
				 dtoList = (List<PIOImpactDTO>) aopMessageVM.getData();
			}

			Workbook workbook = new XSSFWorkbook();

			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;
			// List<List<Object>> rows = new ArrayList<>();

			List<List<Object>> rows = new ArrayList<>();
			
			// Data rows
			for (PIOImpactDTO dto : dtoList) {
				//if (isAfterSave) {
					List<Object> list = new ArrayList<>();
					
					list.add(dto.getDescription());
					list.add(getShortMonth(dto.getStartMonth()));
					list.add(getShortMonth(dto.getEndMonth()));
					list.add(dto.getValue());
					list.add(dto.getRemarks());
					list.add(dto.getId());
					
					if (isAfterSave) {
						list.add(dto.getSaveStatus());
						list.add(dto.getErrDescription());
					}
					rows.add(list);
				//}
			}

			List<String> innerHeaders = new ArrayList<>();
			
			innerHeaders.add("Description");
			innerHeaders.add("Start Month");
			innerHeaders.add("End Month");
			innerHeaders.add("Value");
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
			
			sheet.setColumnHidden(5, true);
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
	
	public  String getShortMonth(int monthNumber) {
        // monthNumber from 1 to 12
        Month month = Month.of(monthNumber);
        return month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }
	public int getMonthNumber(String shortName,PIOImpactDTO dto) {
	    for (Month m : Month.values()) {
	        String name = m.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
	        if (name.equalsIgnoreCase(shortName)) {
	            return m.getValue();  // returns 1 for Jan, 2 for Feb, etc.
	        }
	    }
	    dto.setErrDescription("No record found with this Id");
		dto.setSaveStatus("Failed");
		return 0;
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
	public AOPMessageVM importPIOImpact(String year, UUID plantId, MultipartFile file) {
		// TODO Auto-generated method stub
		try {
			List<PIOImpactDTO> data = readPIOImpact(file.getInputStream(), plantId, year);
			 AOPMessageVM aopMessageVM = updatePIOImpact(year,plantId.toString(), data);
			 List<PIOImpactDTO> failedList = (List<PIOImpactDTO>) aopMessageVM.getData();

			
			if (failedList != null && failedList.size() > 0) {
				byte[] fileByteArray = exportPIOImpact(year, plantId.toString(), true, failedList);
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
	public List<PIOImpactDTO> readPIOImpact(InputStream inputStream, UUID plantFKId, String year) {
		List<PIOImpactDTO> impactList = new ArrayList<>();

		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();

			if (rowIterator.hasNext())
				rowIterator.next(); // Skip header

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				PIOImpactDTO dto = new PIOImpactDTO();
				try {
					dto.setDescription(getStringCellValue(row.getCell(0), dto));
					int startMonth=getMonthNumber(getStringCellValue(row.getCell(1), dto),dto);
					dto.setStartMonth(startMonth);
					int endMonth =getMonthNumber(getStringCellValue(row.getCell(2), dto),dto);
					dto.setEndMonth(endMonth);
					dto.setValue(getNumericCellValue(row.getCell(3), dto));
					dto.setRemarks(getStringCellValue(row.getCell(4), dto));
					dto.setId(getStringCellValue(row.getCell(5), dto));
					
				} catch (Exception e) {
					e.printStackTrace();
					dto.setErrDescription(e.getMessage());
					dto.setSaveStatus("Failed");
				}
				impactList.add(dto);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return impactList;
	}

	private static String getStringCellValue(Cell cell, PIOImpactDTO dto) {
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

	private static Double getNumericCellValue(Cell cell, PIOImpactDTO dto) {
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

	public static Boolean getBooleanCellValue(Cell cell, PIOImpactDTO dto) {
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

			
}
