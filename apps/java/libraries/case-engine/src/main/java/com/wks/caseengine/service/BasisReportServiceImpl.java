package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.PersistenceContext;

import org.hibernate.Session;

import java.sql.*;

import com.wks.caseengine.dto.BasisReportDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

@Service
public class BasisReportServiceImpl implements BasisReportService {

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private ModeWiseNormsService modeWiseNormsService;
	
	private DataSource dataSource;
	public BasisReportServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

		
	@SuppressWarnings("unchecked")
	public List<String> extractTypes(Map<String, Object> typeMap) {
	    List<String> types = new ArrayList<>();
	    Object dataObj = typeMap.get("data");
	    if (dataObj instanceof List<?>) {
	        List<?> dataList = (List<?>) dataObj;
	        for (Object elem : dataList) {
	            if (elem instanceof Map<?, ?>) {
	                Map<?, ?> row = (Map<?, ?>) elem;
	                Object typeObj = row.get("TYPE");
	                if (typeObj != null) {
	                    types.add(typeObj.toString());
	                }
	            }
	        }
	    }
	    return types;
	}
	@Override
	public AOPMessageVM getNormhistorian(
	    String plantId, String aopYear, String periodFrom, String periodTo,String type) {

	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    
	    // 1. Fetching SP Name (Unchanged)
	    Plants plant = plantsRepository.findById(UUID.fromString(plantId))
	                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
	    Sites site = siteRepository.findById(plant.getSiteFkId())
	                .orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
	    Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
	                .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
	    String storedProcedure=null;
	    if(type.equalsIgnoreCase("NormsHistorian")) {
	        storedProcedure = vertical.getName() + "_" + site.getName() + "_NormsBasisReport";
	    }else if(type.equalsIgnoreCase("ProductionTarget")) {
	        storedProcedure = vertical.getName() + "_" + site.getName() + "_ProductionBasisReport";
	    }else if(type.equalsIgnoreCase("OverallConsumption")) {
	        storedProcedure = vertical.getName() + "_" + site.getName() + "_ProductionBasisReport";
	    }
	    
	    try {
	        // 1. Fetch ALL column metadata (List of Lists of Maps) - NEW
	        List<List<Map<String, Object>>> allColMetadata = getAllColumnMetadataForPEE(
	                plantId, aopYear, periodFrom, periodTo, type, storedProcedure);

	        // 2. Fetch ALL grid data (List of Lists of Object[]) - Unchanged
	        List<List<Object[]>> allGridData = getReportDataForPEE(
	                plantId, aopYear, periodFrom, periodTo, type, storedProcedure);

	       
	        if (allColMetadata.size() != allGridData.size()) {
	             throw new RuntimeException("Mismatch: Stored procedure returned " + allColMetadata.size()
	                        + " metadata lists but " + allGridData.size() + " data grids.");
	        }

	        // 3. Build combined list for frontend (List of Maps)
	        List<Map<String, Object>> combined = new ArrayList<>();
	        
	        // Loop through each grid's data and its corresponding metadata
	        for (int i = 0; i < allGridData.size(); i++) {
	            List<Map<String, Object>> colMetadata = allColMetadata.get(i);
	            List<Object[]> rawRows = allGridData.get(i);
	            
	            // Extract column names from metadata list
	            List<String> colNames = colMetadata.stream()
	                                              .map(m -> (String)m.get("field"))
	                                              .collect(Collectors.toList());

	            // --- Grid Name Logic (Copied from original, using 'colNames' derived from metadata) ---
	            String gridName = "UNKNOWN_GRID_" + (i + 1); // Default name
	            if (!colNames.isEmpty()) {
	                int lastColIdx = colNames.size() - 1;
	                // Check if the last column is actually GRID_TYPE (as in your SP)
	                if (colNames.get(lastColIdx).equalsIgnoreCase("GRID_TYPE") && !rawRows.isEmpty()) {
	                    // Use the value from the first row as the grid name
	                    Object gridTypeVal = rawRows.get(0)[lastColIdx];
	                    if (gridTypeVal != null) {
	                        gridName = gridTypeVal.toString();
	                    }
	                } else {
	                    // Fallback to the column name of the first column if no GRID_TYPE is found
	                    gridName = colNames.get(0); 
	                }
	            }
	            // ---------------------------------------------------------------------------------

	            // Convert Object[] rows to List<Map<String, Object>>
	            List<Map<String, Object>> gridDataMap = new ArrayList<>();
	            for (Object[] row : rawRows) {
	                Map<String, Object> rowMap = new LinkedHashMap<>();
	                for (int j = 0; j < colNames.size(); j++) {
	                    rowMap.put(colNames.get(j), row[j]);
	                }
	                gridDataMap.add(rowMap);
	            }

	            // Assemble the final map structure for the grid
	            Map<String, Object> part = new LinkedHashMap<>();
	            part.put("gridName", gridName);
	            part.put("data", gridDataMap);
	            // ADD THE COLUMN METADATA HERE
	            part.put("columns", colMetadata); 
	            combined.add(part);
	        }

	        aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("SP Executed successfully");
	        aopMessageVM.setData(combined); 
	        return aopMessageVM;

	    } catch (Exception e) {
	        e.printStackTrace();
	        aopMessageVM.setCode(500); // Set an error code
	        aopMessageVM.setMessage("Error processing report data: " + e.getMessage());
	        aopMessageVM.setData(null);
	        return aopMessageVM;
	    }
	}

	
	
	public List<Object[]> getReportDataForPE(String plantId, String aopYear, String PeriodFrom,
			String PeriodTo) {

		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		Sites site = siteRepository.findById(plant.getSiteFkId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

		
		String storedProcedure = vertical.getName() + "_" + site.getName() + "_NormsBasisReport_TEST";
		String sql = "EXEC " + storedProcedure
				+ " @plantId = :plantId, @aopYear = :aopYear, @PeriodFrom = :PeriodFrom, @PeriodTo = :PeriodTo";

		Query query = entityManager.createNativeQuery(sql);

		query.setParameter("plantId", plantId);
		query.setParameter("aopYear", aopYear);
		query.setParameter("PeriodFrom", PeriodFrom);
		query.setParameter("PeriodTo", PeriodTo);
		

		return query.getResultList();
	}
	
	@Transactional(readOnly = true) 
	public List<List<Object[]>> getReportDataForPEE(String plantId, String aopYear, String periodFrom, String periodTo,String type,String storedProcedure) {
   
	    String storedProcedureCall = "{ call " + storedProcedure + "(?, ?, ?, ?) }";
	   
	    Session session = entityManager.unwrap(Session.class);
  
	    return session.doReturningWork(connection -> {
	        
	        List<List<Object[]>> allGrids = new ArrayList<>();

	        try (java.sql.CallableStatement callableStatement = connection.prepareCall(storedProcedureCall)) {
	            
	            callableStatement.setString(1, plantId);
	            callableStatement.setString(2, aopYear);
	            callableStatement.setString(3, periodFrom);
	            callableStatement.setString(4, periodTo);

	            boolean results = callableStatement.execute();

	            while (results || callableStatement.getUpdateCount() != -1) {
	                if (results) {
	                    try (java.sql.ResultSet rs = callableStatement.getResultSet()) {
	                        java.sql.ResultSetMetaData metaData = rs.getMetaData();
	                        int columnCount = metaData.getColumnCount();

	                        List<Object[]> currentGrid = new ArrayList<>();
	                        while (rs.next()) {
	                            Object[] row = new Object[columnCount];
	                            for (int i = 1; i <= columnCount; i++) {
	                                
	                                row[i - 1] = rs.getObject(i);
	                            }
	                            currentGrid.add(row);
	                        }
	                        allGrids.add(currentGrid);
	                    }
	                }

	                
	                results = callableStatement.getMoreResults();
	            }

	            return allGrids;

	        } catch (java.sql.SQLException e) {
	            // Include the dynamic SP name in the error message for better debugging
	            throw new RuntimeException("Error executing stored procedure: " + storedProcedure + ". SQL Error: " + e.getMessage(), e);
	        }
	    });
	}
	
	// Add this new method to your service class
	// Note: You need the same parameters to resolve the SP name as in getNormhistorian
	@Transactional(readOnly = true)
	public List<List<Map<String, Object>>> getAllColumnMetadataForPEE(
	    String plantId, String aopYear, String periodFrom, String periodTo, String type, String storedProcedure) {

	    String storedProcedureCall = "{ call " + storedProcedure + "(?, ?, ?, ?) }";

	    Session session = entityManager.unwrap(Session.class);

	    return session.doReturningWork(connection -> {
	        List<List<Map<String, Object>>> allMetadataGrids = new ArrayList<>();

	        try (java.sql.CallableStatement callableStatement = connection.prepareCall(storedProcedureCall)) {

	            callableStatement.setString(1, plantId);
	            callableStatement.setString(2, aopYear);
	            callableStatement.setString(3, periodFrom);
	            callableStatement.setString(4, periodTo);

	            boolean results = callableStatement.execute();

	            while (results || callableStatement.getUpdateCount() != -1) {
	                if (results) {
	                    try (java.sql.ResultSet rs = callableStatement.getResultSet()) {
	                        java.sql.ResultSetMetaData rsMetaData = rs.getMetaData();
	                        List<Map<String, Object>> currentMetadata = new ArrayList<>();

	                        for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
	                            Map<String, Object> columnInfo = new HashMap<>();
	                            String columnName = rsMetaData.getColumnLabel(i);
	                            String columnType = rsMetaData.getColumnTypeName(i);

	                            columnInfo.put("field", columnName);
	                            columnInfo.put("title", formatTitle(columnName)); // Use your formatting method
	                            columnInfo.put("editable", false); // Example property
	                            columnInfo.put("type", getFrontendType(columnType)); // Use your type mapping method
	                            currentMetadata.add(columnInfo);
	                        }
	                        allMetadataGrids.add(currentMetadata);
	                    }
	                }
	                // Move to the next result set or update count
	                results = callableStatement.getMoreResults();
	            }

	            return allMetadataGrids;

	        } catch (java.sql.SQLException e) {
	            throw new RuntimeException("Error executing stored procedure for metadata: " + storedProcedure + ". SQL Error: " + e.getMessage(), e);
	        }
	    });
	}
	
		public List<String> getColumnNames(String plantId, String aopYear, String PeriodFrom,
			String PeriodTo) {
		return entityManager.unwrap(Session.class).doReturningWork(connection -> {
			List<String> columnNames = new ArrayList<>();
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

			
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_NormsBasisReport_TEST";
			String sql = "EXEC " + storedProcedure
					+ " @plantId = ?, @aopYear = ?, @PeriodFrom = ?, @PeriodTo = ?";
			try (PreparedStatement ps = connection.prepareStatement(sql)) {
				ps.setString(1, plantId);
				ps.setString(2, aopYear);
				ps.setString(3, PeriodFrom);
				ps.setString(4, PeriodTo);
				

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
	
	// Assuming this is added to your service/utility class
	public List<List<String>> getAllColumnNames(String plantId, String aopYear, String PeriodFrom, String PeriodTo,String type,String storedProcedure) {
	   
	    return entityManager.unwrap(Session.class).doReturningWork(connection -> {
	        List<List<String>> allColumnNames = new ArrayList<>();
	        String storedProcedureCall = "{ call " + storedProcedure + "(?, ?, ?, ?) }";

	        try (java.sql.CallableStatement callableStatement = connection.prepareCall(storedProcedureCall)) {
	            callableStatement.setString(1, plantId);
	            callableStatement.setString(2, aopYear);
	            callableStatement.setString(3, PeriodFrom);
	            callableStatement.setString(4, PeriodTo);

	            boolean hasResult = callableStatement.execute();

	            while (hasResult || callableStatement.getUpdateCount() != -1) {
	                if (hasResult) {
	                    try (java.sql.ResultSet rs = callableStatement.getResultSet()) {
	                        java.sql.ResultSetMetaData rsMetaData = rs.getMetaData();
	                        List<String> currentColumnNames = new ArrayList<>();
	                        for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
	                            currentColumnNames.add(rsMetaData.getColumnLabel(i));
	                        }
	                        allColumnNames.add(currentColumnNames);
	                    }
	                }
	                hasResult = callableStatement.getMoreResults();
	            }
	            return allColumnNames;
	        }
	    });
	}

	public List<Map<String, Object>> getColumnMetadata(String plantId, String aopYear, String reportType,
			String PeriodFrom,
			String PeriodTo) {
		return entityManager.unwrap(Session.class).doReturningWork(connection -> {
			List<Map<String, Object>> columnMetadata = new ArrayList<>();
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

			UUID siteId = site.getId();
			UUID verticalId = vertical.getId();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_NormsBasisReport";
			String sql = "EXEC " + storedProcedure
					+ " @plantId = ?, @aopYear = ?, @Type = ?, @PeriodFrom = ?, @PeriodTo = ?, @siteId = ?, @verticalId = ?";
			try (PreparedStatement ps = connection.prepareStatement(sql)) {
				ps.setString(1, plantId);
				ps.setString(2, aopYear);
				ps.setString(3, reportType);
				ps.setString(4, PeriodFrom);
				ps.setString(5, PeriodTo);
				ps.setString(6, siteId.toString());
				ps.setString(7, verticalId.toString());

				try (ResultSet rs = ps.executeQuery()) {
					ResultSetMetaData rsMetaData = rs.getMetaData();
					for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
						Map<String, Object> columnInfo = new HashMap<>();
						String columnName = rsMetaData.getColumnLabel(i);
						String columnType = rsMetaData.getColumnTypeName(i);

						columnInfo.put("field", columnName);
						columnInfo.put("title", formatTitle(columnName));
						columnInfo.put("editable", false);
						columnInfo.put("type", getFrontendType(columnType));
						columnMetadata.add(columnInfo);
					}
				}
			}
			return columnMetadata;
		});
	}

	// Helper method to format column titles
	private String formatTitle(String columnName) {
		return columnName.replace("_", " ");
	}

	// Helper method to map SQL data types to frontend types
	private String getFrontendType(String sqlTypeName) {
		switch (sqlTypeName.toUpperCase()) {
			case "VARCHAR":
			case "NVARCHAR":
			case "CHAR":
				return "string";
			case "INT":
			case "TINYINT":
			case "BIGINT":
			case "SMALLINT":
			case "DECIMAL":
			case "FLOAT":
			case "DOUBLE":
			case "NUMERIC":
				return "number";
			case "DATE":
			case "DATETIME":
			case "DATETIME2":
				return "date";
			default:
				return "string";
		}
	}

	public List<Object[]> getReportDataForCracker(String plantId, String aopYear, String Type, String mode) {

		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		Sites site = siteRepository.findById(plant.getSiteFkId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

		UUID siteId = site.getId();
		UUID verticalId = vertical.getId();
		String storedProcedure = vertical.getName() + "_" + site.getName() + "_NormsBasisReport";
		String sql = "EXEC " + storedProcedure
				+ " @plantId = :plantId, @aopYear = :aopYear, @Type = :Type, @verticalId = :verticalId, @siteId = :siteId, @mode = :mode";

		Query query = entityManager.createNativeQuery(sql);

		query.setParameter("plantId", plantId);
		query.setParameter("aopYear", aopYear);
		query.setParameter("Type", Type);
		query.setParameter("mode", mode);
		query.setParameter("siteId", siteId);
		query.setParameter("verticalId", verticalId);

		return query.getResultList();
	}

	@Override
	public AOPMessageVM getNormBasisReportCracker(String plantId, String aopYear, String type, String mode) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<BasisReportDTO> basisReportDTOList = new ArrayList<>();
		try {

			List<Object[]> obj = getReportDataForCracker(plantId, aopYear, type, mode);
			for (Object[] row : obj) {
				BasisReportDTO basisReportDTO = new BasisReportDTO();
				basisReportDTO.setUom(row[0] != null ? row[0].toString() : null);
				basisReportDTO.setApril(
					    row[1] instanceof Number ? ((Number) row[1]).doubleValue()
					                            : row[1] != null ? Double.parseDouble(row[1].toString())
					                                            : null
					);
					// Repeat similarly for other months:
					basisReportDTO.setMay(row[2] instanceof Number ? ((Number) row[2]).doubleValue() : row[2] != null ? Double.parseDouble(row[2].toString()) : null);
					basisReportDTO.setJune(row[3] instanceof Number ? ((Number) row[3]).doubleValue() : row[3] != null ? Double.parseDouble(row[3].toString()) : null);
					basisReportDTO.setJuly(row[4] instanceof Number ? ((Number) row[4]).doubleValue() : row[4] != null ? Double.parseDouble(row[4].toString()) : null);
					basisReportDTO.setAugust(row[5] instanceof Number ? ((Number) row[5]).doubleValue() : row[5] != null ? Double.parseDouble(row[5].toString()) : null);
					basisReportDTO.setSeptember(row[6] instanceof Number ? ((Number) row[6]).doubleValue() : row[6] != null ? Double.parseDouble(row[6].toString()) : null);
					basisReportDTO.setOctober(row[7] instanceof Number ? ((Number) row[7]).doubleValue() : row[7] != null ? Double.parseDouble(row[7].toString()) : null);
					basisReportDTO.setNovember(row[8] instanceof Number ? ((Number) row[8]).doubleValue() : row[8] != null ? Double.parseDouble(row[8].toString()) : null);
					basisReportDTO.setDecember(row[9] instanceof Number ? ((Number) row[9]).doubleValue() : row[9] != null ? Double.parseDouble(row[9].toString()) : null);
					basisReportDTO.setJanuary(row[10] instanceof Number ? ((Number) row[10]).doubleValue() : row[10] != null ? Double.parseDouble(row[10].toString()) : null);
					basisReportDTO.setFebruary(row[11] instanceof Number ? ((Number) row[11]).doubleValue() : row[11] != null ? Double.parseDouble(row[11].toString()) : null);
					basisReportDTO.setMarch(row[12] instanceof Number ? ((Number) row[12]).doubleValue() : row[12] != null ? Double.parseDouble(row[12].toString()) : null);

				basisReportDTO.setNormParameterDisplayName(row[13] != null ? row[13].toString() : null);
				basisReportDTO.setProductName(row[14] != null ? row[14].toString() : null);
				basisReportDTOList.add(basisReportDTO);
			}
			Map<String, Object> finalResult = new HashMap<>();
			finalResult.put("normHistoricBasisData", basisReportDTOList);
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("SP Executed successfully");
			aopMessageVM.setData(finalResult);
			return aopMessageVM;
		}

		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public AOPMessageVM getBestAchievedCracker(String plantId, String aopYear, String reportType) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {

			List<Object[]> obj = getBestAchievedData(plantId, aopYear, reportType);

			// Get column names

			List<String> columnNames = getBestAchievedColumnNames(plantId, aopYear, reportType);

			List<Map<String, Object>> resultList = new ArrayList<>();

			for (Object[] row : obj) {
				Map<String, Object> rowMap = new LinkedHashMap<>();
				for (int i = 0; i < columnNames.size(); i++) {
					rowMap.put(columnNames.get(i), row[i]);
				}
				resultList.add(rowMap);
			}

			Map<String, Object> data = new HashMap<>();
			data.put("data", resultList);
			data.put("columns", getBestAchievedColumnMetadata(plantId, aopYear, reportType));

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("SP Executed successfully");
			aopMessageVM.setData(data);
			return aopMessageVM;

		} catch (Exception e) {
			e.printStackTrace();
			return aopMessageVM;
		}
	}
	
	public List<Object[]> getBestAchievedData(String plantId, String aopYear, String reportType) {

		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		Sites site = siteRepository.findById(plant.getSiteFkId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

		String storedProcedure = vertical.getName() + "_" + site.getName() + "_BestAchived_MinCC";
		String sql = "EXEC " + storedProcedure
				+ " @plantId = :plantId, @aopYear = :aopYear, @reportType = :reportType";

		Query query = entityManager.createNativeQuery(sql);

		query.setParameter("plantId", plantId);
		query.setParameter("aopYear", aopYear);
		query.setParameter("reportType", reportType);
		
		return query.getResultList();
	}

	public List<String> getBestAchievedColumnNames(String plantId, String aopYear, String reportType
			) {
		return entityManager.unwrap(Session.class).doReturningWork(connection -> {
			List<String> columnNames = new ArrayList<>();
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

			String storedProcedure = vertical.getName() + "_" + site.getName() + "_BestAchived_MinCC";
			String sql = "EXEC " + storedProcedure
					+ " @plantId = ?, @aopYear = ?, @reportType = ?";
			try (PreparedStatement ps = connection.prepareStatement(sql)) {
				ps.setString(1, plantId);
				ps.setString(2, aopYear);
				ps.setString(3, reportType);
				

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

	public List<Map<String, Object>> getBestAchievedColumnMetadata(String plantId, String aopYear, String reportType) {
		return entityManager.unwrap(Session.class).doReturningWork(connection -> {
			List<Map<String, Object>> columnMetadata = new ArrayList<>();
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

			
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_BestAchived_MinCC";
			String sql = "EXEC " + storedProcedure
					+ " @plantId = ?, @aopYear = ?, @reportType = ?";
			try (PreparedStatement ps = connection.prepareStatement(sql)) {
				ps.setString(1, plantId);
				ps.setString(2, aopYear);
				ps.setString(3, reportType);
				

				try (ResultSet rs = ps.executeQuery()) {
					ResultSetMetaData rsMetaData = rs.getMetaData();
					for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
						Map<String, Object> columnInfo = new HashMap<>();
						String columnName = rsMetaData.getColumnLabel(i);
						String columnType = rsMetaData.getColumnTypeName(i);

						columnInfo.put("field", columnName);
						columnInfo.put("title", formatTitle(columnName));
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
	public AOPMessageVM calculateBestAchieved(String year, String plantId,String periodTo, String periodFrom) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_LoadBestAchived_MinCC";
			System.out.println(storedProcedure);
			Integer result=  executeDynamicUpdateProcedure(storedProcedure, plantId, year,periodTo,periodFrom);
			aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId),year,"best-achieved-mincc");
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("best-achieved-mincc");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("SP Executed successfully");
	        aopMessageVM.setData(result);
	        return aopMessageVM;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return aopMessageVM;
	}


	@Override
	public AOPMessageVM calculateBestAchievedIndividual(String year, String plantId,String periodTo, String periodFrom) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_LoadBestAchived_Individual";
			System.out.println(storedProcedure);
			Integer result=  executeDynamicUpdateProcedure(storedProcedure, plantId, year,periodTo,periodFrom);
			aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId),year,"best-achieved-individual");
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("best-achieved-individual");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("SP Executed successfully");
	        aopMessageVM.setData(result);
	        return aopMessageVM;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return aopMessageVM;
	}
	
	public int executeDynamicUpdateProcedure(String procedureName, String plantId,
			String aopYear,String PeriodTo, String PeriodFrom) {
		try {
			
			String callSql = "{call " + procedureName + "(?, ?, ?, ?)}";

	        try (Connection connection = dataSource.getConnection();
	             CallableStatement stmt = connection.prepareCall(callSql)) {

	            // Set parameters in the correct order
	            stmt.setString(1, plantId); 
	            stmt.setString(2, aopYear); 
	            stmt.setString(3, PeriodTo);
	            stmt.setString(4, PeriodFrom);

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

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public AOPMessageVM getBestAchievedCrackerData(String plantId, String aopYear, String reportType) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {

			List<Object[]> obj = getBestAchievedData(plantId, aopYear, reportType);

			List<String> columnNames = getBestAchievedColumnNames(plantId, aopYear, reportType);

			List<Map<String, Object>> resultList = new ArrayList<>();

			for (Object[] row : obj) {
				Map<String, Object> rowMap = new LinkedHashMap<>();
				for (int i = 0; i < columnNames.size(); i++) {
				if(columnNames.get(i)!=null && columnNames.get(i).toString().equalsIgnoreCase("Id")) {
										
				}
					rowMap.put(columnNames.get(i), row[i]);
				}
				resultList.add(rowMap);
			}

			Map<String, Object> data = new HashMap<>();
			data.put("data", resultList);
			data.put("columns", getBestAchievedColumnMetadata(plantId, aopYear, reportType));

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("SP Executed successfully");
			aopMessageVM.setData(data);
			return aopMessageVM;

		} catch (Exception e) {
			e.printStackTrace();
			return aopMessageVM;
		}
	}
	
}
