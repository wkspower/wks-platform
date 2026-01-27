package com.wks.caseengine.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
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
import com.wks.caseengine.dto.PeopleInitiativeDTO;
import com.wks.caseengine.dto.PlantTeamDTO;
import com.wks.caseengine.entity.ExclusionDate;
import com.wks.caseengine.entity.PeopleInitiative;
import com.wks.caseengine.entity.PlantTeam;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.ExclusionDateRepository;
import com.wks.caseengine.repository.PeopleInitiativeRepository;
import com.wks.caseengine.repository.PlantTeamRepository;
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
	private PeopleInitiativeRepository peopleInitiativeRepository;
	
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
			    exclusionDTO.setStartDate(row[1] != null ? ((java.sql.Date) row[1]).toLocalDate() : null);
			    exclusionDTO.setEndDate(row[2] != null ? ((java.sql.Date) row[2]).toLocalDate() : null);
			    
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
				exclusionDate.setModifiedOn(LocalDateTime.now());
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
	        	AOPMessageVM aopMessageVM = getExclusionDate(plantId,year);
	        	Map<String, Object> innerMap = (Map<String, Object>) aopMessageVM.getData();

		        if (innerMap != null) {
		             dtoList = (List<ExclusionDTO>) innerMap.get("Data");
		        }
	        }

	        Workbook workbook = new XSSFWorkbook();
	        Sheet sheet = workbook.createSheet("Sheet1");
	        int currentRow = 0;

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

	        int dataRowCount = dtoList.size();
	        for (int i = 0; i < dataRowCount; i++) {
	        	ExclusionDTO dto = dtoList.get(i);
	            Row row = sheet.createRow(currentRow++);
	            List<Object> rowData = new ArrayList<>();
	            rowData.add(dto.getStartDate());
	            rowData.add(dto.getEndDate());
	            rowData.add(dto.getRemark());
	            rowData.add(dto.getId());
	            if (isAfterSave) {
	                rowData.add(dto.getSaveStatus());
	                rowData.add(dto.getErrDescription());
	            }

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
	            }
	        }
	        sheet.setColumnHidden(3, true);
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

	    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
	        Sheet sheet = workbook.getSheetAt(0);
	        Iterator<Row> rowIterator = sheet.iterator();

	        if (rowIterator.hasNext())
	            rowIterator.next();  

	        while (rowIterator.hasNext()) {
	            Row row = rowIterator.next();
	            
	            ExclusionDTO dto = new ExclusionDTO();
	            try {
	                dto.setStartDate(getLocalDateCellValue(row.getCell(0), dto));
	                dto.setEndDate(getLocalDateCellValue(row.getCell(1), dto));
	                dto.setRemark(getStringCellValue(row.getCell(2), dto));
	                
	                dto.setId(getStringCellValue(row.getCell(3), dto));
	              } 
	              catch (Exception e) {
	                e.printStackTrace();
	                dto.setErrDescription(e.getMessage());
	                dto.setSaveStatus("Failed");
	            }
	            exclusionDTOs.add(dto);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return exclusionDTOs;
	}

	private static String getStringCellValue(Cell cell, ExclusionDTO dto) {
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
	
	private static LocalDate getLocalDateCellValue(Cell cell, ExclusionDTO dto) {
	    try {
	        if (cell == null || cell.getCellType() == CellType.BLANK) {
	            return null;
	        }

	        if (cell.getCellType() == CellType.NUMERIC) {
	            if (DateUtil.isCellDateFormatted(cell)) {
	                return cell.getLocalDateTimeCellValue().toLocalDate();
	            } else {
	                throw new Exception("Cell is numeric but not a valid date format");
	            }
	        } 
	        if (cell.getCellType() == CellType.STRING) {
	            String val = cell.getStringCellValue().trim();
	            if (val.isEmpty()) return null;
	            return LocalDate.parse(val, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	        }

	    } catch (Exception e) {
	        dto.setSaveStatus("Failed");
	        dto.setErrDescription("Invalid Date format in Excel. Please use YYYY-MM-DD or Excel Date format.");
	        e.printStackTrace();
	    }
	    return null;
	}
	
}
