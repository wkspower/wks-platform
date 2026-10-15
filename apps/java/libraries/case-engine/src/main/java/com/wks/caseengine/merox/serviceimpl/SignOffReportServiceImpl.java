package com.wks.caseengine.merox.serviceimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.merox.service.SignOffReportService;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class SignOffReportServiceImpl implements SignOffReportService {


   // @PersistenceContext(unitName = "db2")
    private EntityManager entityManagerdb2;

    @Autowired 
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

    @Override
    public AOPMessageVM getSignOffReport(String plantId, String aopYear, String periodFrom, String periodTo) {

        AOPMessageVM aopMessageVM = new AOPMessageVM();

        try {

             
	    Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
        Sites site = siteRepository.findById(plant.getSiteFkId()).get();
       Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

       String storedProcedure = vertical.getName() + "_" + site.getName() + "_GetSignOffReport";

       storedProcedure = "[RIL.AOP.Report].[dbo].[" + storedProcedure + "]";

            // 1. Fetch ALL column metadata
            List<List<Map<String, Object>>> allColMetadata =
                    getAllColumnMetadata(plantId, aopYear, periodFrom, periodTo, storedProcedure);

            // 2. Fetch ALL grid data
            List<List<Object[]>> allGridData =
                    getAllGridData(plantId, aopYear, periodFrom, periodTo, storedProcedure);

            if (allColMetadata.size() != allGridData.size()) {
                throw new RuntimeException("Mismatch: Stored procedure returned " + allColMetadata.size()
                        + " metadata lists but " + allGridData.size() + " data grids.");
            }

            // 3. Build combined list for frontend
            List<Map<String, Object>> combined = new ArrayList<>();

            for (int i = 0; i < allGridData.size(); i++) {
                List<Map<String, Object>> colMetadata = allColMetadata.get(i);
                List<Object[]> rawRows = allGridData.get(i);

                List<String> colNames = colMetadata.stream()
                        .map(m -> (String) m.get("field"))
                        .collect(Collectors.toList());

                // Grid name logic – uses GRID_TYPE column value if present
                String gridName = "UNKNOWN_GRID_" + (i + 1);
                if (!colNames.isEmpty()) {
                    int lastColIdx = colNames.size() - 1;
                    if (colNames.get(lastColIdx).equalsIgnoreCase("GRID_TYPE") && !rawRows.isEmpty()) {
                        Object gridTypeVal = rawRows.get(0)[lastColIdx];
                        if (gridTypeVal != null) {
                            gridName = gridTypeVal.toString();
                        }
                    } else {
                        gridName = colNames.get(0);
                    }
                }

                // Convert Object[] rows to List<Map<String, Object>>
                List<Map<String, Object>> gridDataMap = new ArrayList<>();
                for (Object[] row : rawRows) {
                    Map<String, Object> rowMap = new LinkedHashMap<>();
                    for (int j = 0; j < colNames.size(); j++) {
                        rowMap.put(colNames.get(j), row[j]);
                    }
                    gridDataMap.add(rowMap);
                }

                Map<String, Object> part = new LinkedHashMap<>();
                part.put("gridName", gridName);
                part.put("data", gridDataMap);
                part.put("columns", colMetadata);
                combined.add(part);
            }

            aopMessageVM.setCode(200);
            aopMessageVM.setMessage("SP Executed successfully");
            aopMessageVM.setData(combined);
            return aopMessageVM;

        } catch (Exception e) {
            e.printStackTrace();
            aopMessageVM.setCode(500);
            aopMessageVM.setMessage("Error processing sign-off report data: " + e.getMessage());
            aopMessageVM.setData(null);
            return aopMessageVM;
        }
    }

    @Transactional(readOnly = true)
    private List<List<Map<String, Object>>> getAllColumnMetadata(
            String plantId, String aopYear, String periodFrom, String periodTo, String storedProcedure) {

        String storedProcedureCall = "{ call " + storedProcedure + "(?, ?, ?, ?) }";

        Session session = entityManagerdb2.unwrap(Session.class);

        return session.doReturningWork(connection -> {
            List<List<Map<String, Object>>> allMetadataGrids = new ArrayList<>();

            try (java.sql.CallableStatement cs = connection.prepareCall(storedProcedureCall)) {

                cs.setString(1, plantId);
                cs.setString(2, aopYear);
                cs.setString(3, periodFrom);
                cs.setString(4, periodTo);

                boolean results = cs.execute();

                while (results || cs.getUpdateCount() != -1) {
                    if (results) {
                        try (java.sql.ResultSet rs = cs.getResultSet()) {
                            java.sql.ResultSetMetaData rsMetaData = rs.getMetaData();
                            List<Map<String, Object>> currentMetadata = new ArrayList<>();

                            for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                                Map<String, Object> columnInfo = new HashMap<>();
                                String columnName = rsMetaData.getColumnLabel(i);
                                String columnType = rsMetaData.getColumnTypeName(i);

                                columnInfo.put("field", columnName);
                                columnInfo.put("title", formatTitle(columnName));
                                columnInfo.put("editable", false);
                                columnInfo.put("type", getFrontendType(columnType));
                                currentMetadata.add(columnInfo);
                            }
                            allMetadataGrids.add(currentMetadata);
                        }
                    }
                    results = cs.getMoreResults();
                }

                return allMetadataGrids;

            } catch (java.sql.SQLException e) {
                throw new RuntimeException(
                        "Error fetching metadata for stored procedure: " + storedProcedure
                                + ". SQL Error: " + e.getMessage(), e);
            }
        });
    }

    @Transactional(readOnly = true)
    private List<List<Object[]>> getAllGridData(
            String plantId, String aopYear, String periodFrom, String periodTo, String storedProcedure) {

        String storedProcedureCall = "{ call " + storedProcedure + "(?, ?, ?, ?) }";

        Session session = entityManagerdb2.unwrap(Session.class);

        return session.doReturningWork(connection -> {
            List<List<Object[]>> allGrids = new ArrayList<>();

            try (java.sql.CallableStatement cs = connection.prepareCall(storedProcedureCall)) {

                cs.setString(1, plantId);
                cs.setString(2, aopYear);
                cs.setString(3, periodFrom);
                cs.setString(4, periodTo);

                boolean results = cs.execute();

                while (results || cs.getUpdateCount() != -1) {
                    if (results) {
                        try (java.sql.ResultSet rs = cs.getResultSet()) {
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
                    results = cs.getMoreResults();
                }

                return allGrids;

            } catch (java.sql.SQLException e) {
                throw new RuntimeException(
                        "Error fetching data for stored procedure: " + storedProcedure
                                + ". SQL Error: " + e.getMessage(), e);
            }
        });
    }

    // ── Utility helpers ──────────────────────────────────────────────────────

    private String formatTitle(String columnName) {
        return columnName.replace("_", " ");
    }

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
}
