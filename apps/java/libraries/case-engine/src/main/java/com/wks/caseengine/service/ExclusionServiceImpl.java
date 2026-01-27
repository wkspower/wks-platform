package com.wks.caseengine.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.ConfigurationVersionDTO;
import com.wks.caseengine.dto.ExclusionDTO;

import com.wks.caseengine.entity.ExclusionDate;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.ExclusionDateRepository;

import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class ExclusionServiceImpl implements ExclusionService{
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private ExclusionDateRepository exclusionDateRepository;
	
	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;
	
	@Autowired
	private PlantsRepository plantsRepository;
	
	@Autowired
	private ConfigurationService configurationService;

	@Override
	public AOPMessageVM getExclusionDate(String plantId, String year) {
		
		try {
			List<Object[]> obj = new ArrayList<>();
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_GetExclusionDate";
				obj = findByYearAndPlantId(year, UUID.fromString(plantId), storedProcedure);
			
			List<ExclusionDTO> exclusionDTOs = new ArrayList<>();
			
			for (Object[] row : obj) {
			    ExclusionDTO exclusionDTO = new ExclusionDTO();
			    exclusionDTO.setId(row[0] != null ? row[0].toString() : "");
			    exclusionDTO.setStartDate(row[1] != null ? new java.util.Date(((java.sql.Date) row[1]).getTime()) : null);
			    exclusionDTO.setEndDate(row[2] != null ? new java.util.Date(((java.sql.Date) row[2]).getTime()) : null);
			    
			    exclusionDTO.setRemark(row[3] != null ? row[3].toString() : "");
			    exclusionDTOs.add(exclusionDTO);
			}
			Map<String, Object> map = new HashMap<>(); 
			
			map.put("Data", exclusionDTOs);
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(map);
			aopMessageVM.setMessage("Data fetched successfully");

			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
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
	
	@Override
	public AOPMessageVM deleteExclusionDate(String id) {
		Optional<ExclusionDate> exclusionDate =exclusionDateRepository.findById(UUID.fromString(id));
		if(exclusionDate.isPresent()) {
			exclusionDateRepository.delete(exclusionDate.get()); 
		}
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(aopMessageVM);
		aopMessageVM.setMessage("Record deleted successfully");
		// TODO Auto-generated method stub
		return aopMessageVM;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public AOPMessageVM saveExclusionDate(String year, String plantFKId,
			List<ExclusionDTO> exclusionDTOs) {
		try {
			List<ExclusionDTO> failedList = new ArrayList<>();
			UUID plantId = UUID.fromString(plantFKId);

			for (ExclusionDTO exclusionDTO : exclusionDTOs) {
				if (exclusionDTO.getSaveStatus() != null
						&& exclusionDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(exclusionDTO);
					continue;
				}
				ExclusionDate exclusionDate =null;
				if(exclusionDTO.getId()!=null) {
					Optional<ExclusionDate> exclusionDTOOpt=exclusionDateRepository.findById(UUID.fromString(exclusionDTO.getId()));
					if(exclusionDTOOpt.isPresent()) {
						exclusionDate=exclusionDTOOpt.get();
					}
				}else {
					exclusionDate=new ExclusionDate();
					exclusionDate.setAopYear(year);
					exclusionDate.setPlantId(plantId);
				}
				AOPMessageVM response = configurationService.getConfigurationVersion(year, plantId.toString());

				if (response != null && response.getData() != null && !((List<?>) response.getData()).isEmpty()) {
				    
				    List<ConfigurationVersionDTO> dataList = (List<ConfigurationVersionDTO>) response.getData();
				    
				    String attributeValue = dataList.get(0).getAttributeValue();
				    
				    exclusionDate.setRevision(Integer.parseInt(attributeValue));	
				}
				exclusionDate.setStartDate(exclusionDTO.getStartDate());
				exclusionDate.setEndDate(exclusionDTO.getEndDate());
				exclusionDate.setRemarks(exclusionDTO.getRemark());
				exclusionDate.setModifiedBy(Utility.getUserName());
				exclusionDate.setModifiedOn(new Date());
				exclusionDateRepository.save(exclusionDate);
			}
			
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(failedList);
			aopMessageVM.setMessage("Data updated successfully");
			return aopMessageVM;
		} catch (Exception ex) {
			ex.printStackTrace();
			
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	public byte[] exportExclusionDate(String year, String plantId, boolean isAfterSave, List<ExclusionDTO> dtoList) {
	    try {   
	      
	        if (!isAfterSave) {
	            AOPMessageVM aopMessageVM = getExclusionDate(plantId, year);
	            if (aopMessageVM != null && aopMessageVM.getData() != null) {
	                
	                Map<String, Object> innerMap = (Map<String, Object>) aopMessageVM.getData();
	                if (innerMap != null && innerMap.get("Data") != null) {
	                    dtoList = (List<ExclusionDTO>) innerMap.get("Data");
	                }
	            }
	        }

	       
	        if (dtoList == null) dtoList = new ArrayList<>();

	        Workbook workbook = new XSSFWorkbook();
	        Sheet sheet = workbook.createSheet("Exclusion Dates");
	        int currentRow = 0;

	      
	        CellStyle dateCellStyle = workbook.createCellStyle();
	        CreationHelper createHelper = workbook.getCreationHelper();
	        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
	       
	        dateCellStyle.setBorderBottom(BorderStyle.THIN);
	        dateCellStyle.setBorderTop(BorderStyle.THIN);
	        dateCellStyle.setBorderLeft(BorderStyle.THIN);
	        dateCellStyle.setBorderRight(BorderStyle.THIN);

	       
	        List<String> innerHeaders = new ArrayList<>();
	        innerHeaders.add("From Date");
	        innerHeaders.add("To Date");
	        innerHeaders.add("Reason");
	        innerHeaders.add("Id");
	        if (isAfterSave) {
	            innerHeaders.add("Status");
	            innerHeaders.add("Error Description");
	        }

	       
	        Row headerRow = sheet.createRow(currentRow++);
	        for (int col = 0; col < innerHeaders.size(); col++) {
	            Cell cell = headerRow.createCell(col);
	            cell.setCellValue(innerHeaders.get(col));
	           
	            cell.setCellStyle(Utility.createBoldBorderedStyle(workbook));
	        }

	      
	        for (ExclusionDTO dto : dtoList) {
	            Row row = sheet.createRow(currentRow++);
	            
	         
	            Object[] dataValues = new Object[innerHeaders.size()];
	            dataValues[0] = dto.getStartDate();
	            dataValues[1] = dto.getEndDate();
	            dataValues[2] = dto.getRemark();
	            dataValues[3] = dto.getId();
	            
	            if (isAfterSave) {
	                dataValues[4] = dto.getSaveStatus();
	                dataValues[5] = dto.getErrDescription();
	            }

	            for (int col = 0; col < dataValues.length; col++) {
	                Cell cell = row.createCell(col);
	                Object value = dataValues[col];

	                if (value instanceof java.util.Date) {
	                   
	                    cell.setCellValue((java.util.Date) value);
	                    cell.setCellStyle(dateCellStyle);
	                } else if (value instanceof Number) {
	                    cell.setCellValue(((Number) value).doubleValue());
	                } else if (value instanceof Boolean) {
	                    cell.setCellValue((Boolean) value);
	                } else if (value != null) {
	                    cell.setCellValue(value.toString());
	                } else {
	                    cell.setCellValue("");
	                }
	            }
	        }

	      
	        sheet.setColumnHidden(3, true);
	        
	        
	        for (int i = 0; i < innerHeaders.size(); i++) {
	            sheet.autoSizeColumn(i);
	        }

	     
	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        workbook.write(outputStream);
	        workbook.close();
	        
	        return outputStream.toByteArray();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return new byte[0]; 
	}
	
	@Override
	public AOPMessageVM importExclusionDate(String year,UUID plantId,MultipartFile file) {
		try {
			List<ExclusionDTO> data = readExclusionDate(file.getInputStream(), plantId, year);
			 AOPMessageVM aopMessageVM = saveExclusionDate(year, plantId.toString(),data);
			 List<ExclusionDTO> failedList = (List<ExclusionDTO>) aopMessageVM.getData();

			
			if (failedList != null && failedList.size() > 0) {
				byte[] fileByteArray = exportExclusionDate(year, plantId.toString(), true, failedList);
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
	
	public List<ExclusionDTO> readExclusionDate(InputStream inputStream, UUID plantFKId, String year) {
	    List<ExclusionDTO> exclusionDTOs = new ArrayList<>();
	    SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy");
	    AOPMessageVM configResp = configurationService.getConfigurationExecution(year, plantFKId.toString());
	    Date boundaryStart = null;
	    Date boundaryEnd = null;

	    if (configResp != null && configResp.getData() != null) {
	        List<Map<String, Object>> configData = (List<Map<String, Object>>) configResp.getData();
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        
	        for (Map<String, Object> map : configData) {
	            try {
	                String name = String.valueOf(map.get("Name"));
	                String val = String.valueOf(map.get("AttributeValue"));
	                if ("StartDate".equalsIgnoreCase(name)) {
	                    boundaryStart = sdf.parse(val);
	                } else if ("EndDate".equalsIgnoreCase(name)) {
	                    boundaryEnd = sdf.parse(val);
	                }
	            } catch (Exception e) {
	            }
	        }
	    }

	    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
	        Sheet sheet = workbook.getSheetAt(0);
	        Iterator<Row> rowIterator = sheet.iterator();
	        if (rowIterator.hasNext()) rowIterator.next(); 

	        while (rowIterator.hasNext()) {
	            Row row = rowIterator.next();
	            ExclusionDTO dto = new ExclusionDTO();
	            Date start = getDateCellValue(row.getCell(0), dto);
	            Date end = getDateCellValue(row.getCell(1), dto);
	            String remark = getStringCellValue(row.getCell(2), dto);
	            String id = getStringCellValue(row.getCell(3), dto);

	            dto.setStartDate(start);
	            dto.setEndDate(end);
	            dto.setRemark(remark);
	            dto.setId(id);
	            dto.setSaveStatus("Success");

	            try {
	                if (start == null || end == null) {
	                    dto.setSaveStatus("Failed");
	                    dto.setErrDescription("Invalid or missing Date. Use format: dd-MM-yyyy.");
	                } 
	                else if (end.before(start)) {
	                    dto.setSaveStatus("Failed");
	                    dto.setErrDescription("End date (" + displayFormat.format(end) + ") cannot be before Start date (" + displayFormat.format(start) + ").");
	                } 
	                else if (boundaryStart != null && boundaryEnd != null) {
	                    if (start.before(boundaryStart) || end.after(boundaryEnd)) {
	                        dto.setSaveStatus("Failed");
	                        dto.setErrDescription("Dates must be within boundary: " + 
	                            displayFormat.format(boundaryStart) + " to " + displayFormat.format(boundaryEnd));
	                    }
	                }
	            } catch (Exception valEx) {
	                dto.setSaveStatus("Failed");
	                dto.setErrDescription("Validation error: " + valEx.getMessage());
	            }

	            exclusionDTOs.add(dto);
	        }

	        validateOverlaps(exclusionDTOs);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    return exclusionDTOs;
	}

	private void validateOverlaps(List<ExclusionDTO> list) {
	    for (int i = 0; i < list.size(); i++) {
	        ExclusionDTO current = list.get(i);
	        if ("Failed".equals(current.getSaveStatus()) || current.getStartDate() == null || current.getEndDate() == null) continue;

	        for (int j = i + 1; j < list.size(); j++) {
	            ExclusionDTO other = list.get(j);
	            
	            if ("Failed".equals(other.getSaveStatus()) || other.getStartDate() == null || other.getEndDate() == null) continue;
	            boolean isOverlapping = !current.getStartDate().after(other.getEndDate()) 
	                                 && !current.getEndDate().before(other.getStartDate());

	            if (isOverlapping) {
	                current.setSaveStatus("Failed");
	                current.setErrDescription("This date range overlaps with another row in the file.");
	                
	                other.setSaveStatus("Failed");
	                other.setErrDescription("This date range overlaps with another row in the file.");
	            }
	        }
	    }
	}
	private static String getStringCellValue(Cell cell, ExclusionDTO dto) {
	    try {
	        if (cell == null || cell.getCellType() == CellType.BLANK) {
	            return null;
	        }
	        
	        cell.setCellType(CellType.STRING);
	        String val = cell.getStringCellValue().trim();
	        
	        
	        return val.isEmpty() ? null : val;
	        
	    } catch (Exception e) {
	        dto.setSaveStatus("Failed");
	        dto.setErrDescription("Please enter correct values");
	        e.printStackTrace();
	    }
	    return null;
	}
	
	private Date getDateCellValue(Cell cell, ExclusionDTO dto) {
	    try {
	        if (cell == null || cell.getCellType() == CellType.BLANK) {
	            return null;
	        }

	        if (cell.getCellType() == CellType.NUMERIC) {
	            if (DateUtil.isCellDateFormatted(cell)) {
	                return cell.getDateCellValue();
	            }
	        }
	        if (cell.getCellType() == CellType.STRING) {
	            String val = cell.getStringCellValue().trim();
	            if (val.isEmpty()) return null;
	            
	            SimpleDateFormat excelSdf = new SimpleDateFormat("dd-MM-yyyy");
	            excelSdf.setLenient(false); // Strict parsing
	            return excelSdf.parse(val);
	        }
	    } catch (Exception e) {
	    }
	    return null;
	}	
}
