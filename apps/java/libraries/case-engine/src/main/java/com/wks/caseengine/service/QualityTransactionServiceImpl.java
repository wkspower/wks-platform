package com.wks.caseengine.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
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
import com.wks.caseengine.dto.PeopleInitiativeDTO;
import com.wks.caseengine.dto.QualityTransactionDTO;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.QualityTransaction;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.QualityTransactionRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class QualityTransactionServiceImpl implements QualityTransactionService{
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;
	
	@Autowired
	private QualityTransactionRepository qualityTransactionRepository;

	@Override
	public AOPMessageVM getQualityTransaction(String plantId, String year) {
		
		try {
			List<Object[]> obj = new ArrayList<>();
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

			Sites site = siteRepository.findById(plant.getSiteFkId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
			
				String procedureName = vertical.getName()+"_"+site.getName()+"_GetQualityTransaction";
				obj = findByYearAndPlantId(year, UUID.fromString(plantId), procedureName);
			
			List<QualityTransactionDTO> qualityTransactionDTOs = new ArrayList<>();
			for (Object[] row : obj) {
				QualityTransactionDTO qualityTransactionDTO = new QualityTransactionDTO();
				qualityTransactionDTO.setId(row[0] != null ? row[0].toString() : "");

				qualityTransactionDTO.setMaterialId(row[1] != null ? row[1].toString() : "");
				qualityTransactionDTO.setNormParameterTypeName(row[2] != null ? row[2].toString() : "");
				qualityTransactionDTO.setDisplayName(row[3] != null ? row[3].toString() : "");
				qualityTransactionDTO.setUom(row[4] != null ? row[4].toString() : "");
				qualityTransactionDTO.setPrevBudget(
						(row[5] != null && !row[5].toString().trim().isEmpty())
								? Double.parseDouble(row[5].toString().trim())
								: 0.0);
				qualityTransactionDTO.setPrevActual(
						(row[6] != null && !row[6].toString().trim().isEmpty())
								? Double.parseDouble(row[6].toString().trim())
								: 0.0);
				qualityTransactionDTO.setProposedNorm(
						(row[7] != null && !row[7].toString().trim().isEmpty())
								? Double.parseDouble(row[7].toString().trim())
								: 0.0);
				
				qualityTransactionDTO.setPlantId(row[8] != null ? row[8].toString() : "");
				qualityTransactionDTO.setAopYear(row[9] != null ? row[9].toString() : "");
				qualityTransactionDTO.setRemark(row[12] != null ? row[12].toString() : "");
				qualityTransactionDTOs.add(qualityTransactionDTO);
				
			}
			Map<String, Object> map = new HashMap<>(); 
			
			map.put("Data", qualityTransactionDTOs);
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
					+ " @PlantId = :plantId, @AOPYear = :aopYear";

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
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public AOPMessageVM saveQualityTransaction(String year, String plantFKId,
			List<QualityTransactionDTO> qualityTransactionDTOs) {
		try {
			List<QualityTransactionDTO> failedList = new ArrayList<>();
			UUID plantId = UUID.fromString(plantFKId);

			for (QualityTransactionDTO qualityTransactionDTO : qualityTransactionDTOs) {
				if (qualityTransactionDTO.getSaveStatus() != null
						&& qualityTransactionDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(qualityTransactionDTO);
					continue;
				}
				QualityTransaction qualityTransaction =null;
				UUID material=UUID.fromString(qualityTransactionDTO.getMaterialId());
				Optional<QualityTransaction> qualityTransactionOpt =qualityTransactionRepository.findByMaterialPlantAndYear(material,plantId,year);
				if(qualityTransactionOpt.isPresent()) {
						qualityTransaction=qualityTransactionOpt.get();
				}else {
					qualityTransaction = new QualityTransaction();
					qualityTransaction.setMaterialId(UUID.fromString(qualityTransactionDTO.getMaterialId()));
					qualityTransaction.setAopYear(year);
					qualityTransaction.setPrevActual(qualityTransactionDTO.getPrevActual());
					qualityTransaction.setPrevBudget(qualityTransactionDTO.getPrevBudget());
					qualityTransaction.setPlantId(plantId);
					qualityTransactionRepository.save(qualityTransaction);
				}
				qualityTransaction.setRemark(qualityTransactionDTO.getRemark());
				qualityTransaction.setUpdatedBy(Utility.getUserName());
				qualityTransaction.setModifiedOn(new Date());
				qualityTransaction.setProposedNorm(qualityTransactionDTO.getProposedNorm());
				qualityTransactionRepository.save(qualityTransaction);
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
	
	public byte[] exportQualityTransaction(String year, String plantId, boolean isAfterSave, List<QualityTransactionDTO> dtoList) {
	    try {   
	        if (!isAfterSave) {
	        	AOPMessageVM aopMessageVM = getQualityTransaction(plantId,year);
	        	Map<String, Object> innerMap = (Map<String, Object>) aopMessageVM.getData();

		        if (innerMap != null) {
		             dtoList = (List<QualityTransactionDTO>) innerMap.get("Data");
		        }
	        }

	        Workbook workbook = new XSSFWorkbook();
	        Sheet sheet = workbook.createSheet("Sheet1");
	        int currentRow = 0;

	        List<String> innerHeaders = new ArrayList<>();
	        innerHeaders.add("Material Id");
	        innerHeaders.add("Name");
	        innerHeaders.add("UOM");
	        innerHeaders.add("Budget");
	        innerHeaders.add("Actual");
	        innerHeaders.add("Proposed Norm");
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
	        	QualityTransactionDTO dto = dtoList.get(i);
	            Row row = sheet.createRow(currentRow++);
	            List<Object> rowData = new ArrayList<>();
	            rowData.add(dto.getMaterialId());
	            rowData.add(dto.getDisplayName());
	            rowData.add(dto.getUom());
	            rowData.add(dto.getPrevBudget());
	            rowData.add(dto.getPrevActual());
	            rowData.add(dto.getProposedNorm());
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
	        sheet.setColumnHidden(6, true);
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
	public AOPMessageVM importQualityTransaction(String year,UUID plantId,MultipartFile file) {
		try {
			List<QualityTransactionDTO> data = readQualityTransaction(file.getInputStream(), plantId, year);
			 AOPMessageVM aopMessageVM = saveQualityTransaction(year, plantId.toString(),data);
			 List<QualityTransactionDTO> failedList = (List<QualityTransactionDTO>) aopMessageVM.getData();
			
			if (failedList != null && failedList.size() > 0) {
				byte[] fileByteArray = exportQualityTransaction(year, plantId.toString(), true, failedList);
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
	
	public List<QualityTransactionDTO> readQualityTransaction(InputStream inputStream, UUID plantFKId, String year) {
	    List<QualityTransactionDTO> qualityTransactions = new ArrayList<>();

	    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
	        Sheet sheet = workbook.getSheetAt(0);
	        Iterator<Row> rowIterator = sheet.iterator();

	        if (rowIterator.hasNext())
	            rowIterator.next();  

	        while (rowIterator.hasNext()) {
	            Row row = rowIterator.next();
	            
	            QualityTransactionDTO dto = new QualityTransactionDTO();
	            try {
	            	dto.setMaterialId(getStringCellValue(row.getCell(0), dto));
	                dto.setDisplayName(getStringCellValue(row.getCell(1), dto));
	                dto.setUom(getStringCellValue(row.getCell(2), dto));
	                dto.setPrevBudget(getNumericCellValue(row.getCell(3), dto));
	                dto.setPrevActual(getNumericCellValue(row.getCell(4), dto));
	                dto.setProposedNorm(getNumericCellValue(row.getCell(5), dto));
	                dto.setId(getStringCellValue(row.getCell(6), dto));
	                dto.setPlantId(plantFKId.toString());
	                dto.setAopYear(year);
	              } 
	              catch (Exception e) {
	                e.printStackTrace();
	                dto.setErrDescription(e.getMessage());
	                dto.setSaveStatus("Failed");
	            }
	            qualityTransactions.add(dto);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return qualityTransactions;
	}

	private static java.util.Date getDateCellValue(Cell cell, QualityTransactionDTO dto) {
	    if (cell == null || cell.getCellType() == CellType.BLANK) {
	        return null;
	    }

	    if (cell.getCellType() == CellType.NUMERIC) {
	        if (DateUtil.isCellDateFormatted(cell)) {
	            return cell.getDateCellValue();
	        } else {
	            dto.setSaveStatus("Failed");
	            dto.setErrDescription("Invalid date format in cell");
	        }
	    } else if (cell.getCellType() == CellType.STRING) {
	        String val = cell.getStringCellValue().trim();
	        if (val.isEmpty()) {
	            return null; 
	        }
	        try {
	            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
	            return sdf.parse(val);
	        } catch (java.text.ParseException e) {
	            dto.setSaveStatus("Failed");
	            dto.setErrDescription("Please enter date in correct format (yyyy-MM-dd)");
	        }
	    }
	    return null;
	}
	private static Integer getIntegerCellValue(Cell cell, QualityTransactionDTO dto) {
	    if (cell == null || cell.getCellType() == CellType.BLANK) {
	        return null;
	    }

	    if (cell.getCellType() == CellType.NUMERIC) {
	        
	        return (int) cell.getNumericCellValue();
	    } 
	    
	    if (cell.getCellType() == CellType.STRING) {
	        String val = cell.getStringCellValue().trim();
	        if (val.isEmpty()) {
	            return null; 
	        }
	        try {
	            
	            return Integer.parseInt(val);
	        } catch (NumberFormatException e) {
	            dto.setSaveStatus("Failed");
	            dto.setErrDescription("Please enter valid integer values");
	        }
	    }
	    return null;
	}
	private static String getStringCellValue(Cell cell, QualityTransactionDTO dto) {
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
	private static Double getNumericCellValue(Cell cell, QualityTransactionDTO dto) {
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

}
