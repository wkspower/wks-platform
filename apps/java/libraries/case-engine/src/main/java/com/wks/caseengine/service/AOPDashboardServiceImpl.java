package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.jdbc.ReturningWork;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.hibernate.Session;
import com.wks.caseengine.message.vm.AOPMessageVM;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class AOPDashboardServiceImpl implements AOPDashboardService {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Override
	public AOPMessageVM getAOPDashboard(final String year) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    try {
	        String procedureName = "sp_GetAOPDashboardUtility";

	        // Fetch both data and column metadata dynamically
	        Map<String, Object> dynamicResult = getDynamicData(year, procedureName);

	        aopMessageVM.setCode(200);
	        aopMessageVM.setData(dynamicResult);
	        aopMessageVM.setMessage("Data fetched successfully");
	        return aopMessageVM;

	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch dashboard data", ex);
	    }
	}

	public Map<String, Object> getDynamicData(final String aopYear, final String procedureName) {
	    return entityManager.unwrap(Session.class).doReturningWork(new ReturningWork<Map<String, Object>>() {
	        @Override
	        public Map<String, Object> execute(Connection connection) throws SQLException {
	            Map<String, Object> result = new HashMap<>();
	            List<Map<String, Object>> dataList = new ArrayList<>();
	            List<Map<String, Object>> columnMetadata = new ArrayList<>();

	            String sql = "{call " + procedureName + "(?)}"; // Using standard call syntax

	            try (PreparedStatement ps = connection.prepareStatement(sql)) {
	                ps.setString(1, aopYear);

	                try (ResultSet rs = ps.executeQuery()) {
	                    ResultSetMetaData rsmd = rs.getMetaData();
	                    int columnCount = rsmd.getColumnCount();

	                    // 1. Generate Column Metadata dynamically
	                    for (int i = 1; i <= columnCount; i++) {
	                        Map<String, Object> meta = new HashMap<>();
	                        String columnName = rsmd.getColumnLabel(i);
	                        String columnType = rsmd.getColumnTypeName(i);

	                        meta.put("field", columnName);
	                        meta.put("title", formatTitle(columnName));
	                        meta.put("type", getFrontendType(columnType));
	                        columnMetadata.add(meta);
	                    }

	                    // 2. Generate Data Rows dynamically
	                    while (rs.next()) {
	                        Map<String, Object> row = new LinkedHashMap<>();
	                        for (int i = 1; i <= columnCount; i++) {
	                            String columnName = rsmd.getColumnLabel(i);
	                            Object value = rs.getObject(i);
	                            
	                            // Null-free check: Use empty string for nulls
	                            row.put(columnName, value != null ? value : "");
	                        }
	                        dataList.add(row);
	                    }
	                }
	            }
	            result.put("data", dataList);
	            result.put("columns", columnMetadata);
	            return result;
	        }
	    });
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
	        case "SMALLDATETIME": 
	        case "TIME": 
	            return "date";

	        case "BIT": 
	            return "boolean";

	        case "UNIQUEIDENTIFIER": 
	            return "string"; 
	            
	        default:
	            return "string"; 
	    }
	}

}
