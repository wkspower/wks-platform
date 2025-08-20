package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;

import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;


@Service
public class NormHistorianBasisServiceImpl  implements NormHistorianBasisService{

    
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private DataSource dataSource;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;


    @Override
    public AOPMessageVM getNormHistorianBasisData(String plantId, String year, String reportType,String uom) {
        // TODO Auto-generated method stub
    
            try {
                AOPMessageVM aopMessageVM = new AOPMessageVM();
                List<Map<String, Object>> normHistoricBasisData = new ArrayList<>();
    
                List<Object[]> obj = getHistorianBasisDatafromDB(plantId, year,reportType,uom);
                if(reportType.equalsIgnoreCase("ProductionVolumeData")) {
                    for (Object[] row : obj) {
                    Map<String, Object> map = new HashMap<>();
	                 

	                 map.put("material", row[0]);
	                 map.put("april", row[1]);
	                 map.put("may", row[2]);
	                 map.put("june", row[3]);
	                 map.put("july", row[4]);
	                 map.put("august", row[5]);
	                 map.put("september", row[6]);
	                 map.put("october", row[7]);
	                 map.put("november", row[8]);
	                 map.put("december", row[9]);
	                 map.put("january", row[10]);
	                 map.put("february", row[11]);
	                 map.put("march", row[12]);
                     map.put("total", row[13]);  
                    normHistoricBasisData.add(map);
                    }
                }

                if(reportType.equalsIgnoreCase("McuAndNormGrid")) {
                    for (Object[] row : obj) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("srNo", row[0]);
                         map.put("norms", row[1]);
                        map.put("materialName", row[2]);
                        map.put("aopMonth", row[3]);
                        map.put("siteName", row[4]);
                        map.put("plantName", row[5]);
                        map.put("normValue", row[6]);
                        map.put("actualQuantity", row[7]);
                        map.put("normDateTime", row[8]);
                       map.put("dateTime", row[9]);
                       map.put("normsUom", row[10]);
                        normHistoricBasisData.add(map);
                    }
                }

                if(reportType.equalsIgnoreCase("HistorianValues")) {
                    for (Object[] row : obj) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", row[0]);
                        map.put("particulars", row[1]);
                        map.put("april", row[2]);
                        map.put("may", row[3]);
                        map.put("june", row[4]);
                        map.put("july", row[5]);
                        map.put("august", row[6]);
                        map.put("september", row[7]);
                        map.put("october", row[8]);
                        map.put("november", row[9]);
                        map.put("december", row[10]);
                        map.put("january", row[11]);
                        map.put("february", row[12]);
                        map.put("march", row[13]);
                       
                        normHistoricBasisData.add(map);
                    }
                }
                
                // Combine both into a result map
                Map<String, Object> finalResult = new HashMap<>();
                finalResult.put("normHistoricBasisData", normHistoricBasisData);
    
                // Set in response
                aopMessageVM.setCode(200);
                aopMessageVM.setMessage("Data fetched successfully");
                aopMessageVM.setData(finalResult);
                return aopMessageVM;
            } catch (IllegalArgumentException e) {
                throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to fetch data", ex);
            }
        }
    
        
    

    public List<Object[]> getHistorianBasisDatafromDB(String plantId, String aopYear,String reportType,String uom) {
        try {
            String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
            String storedProcedure = "NormsHistorianBasis";
            String sql = "EXEC " + storedProcedure
                    + " @plantId = :plantId, @aopYear = :aopYear, @reportType = :reportType,@UOM = :uom";

            Query query = entityManager.createNativeQuery(sql);

            query.setParameter("plantId", plantId);
            query.setParameter("aopYear", aopYear);
            query.setParameter("reportType", reportType);
            query.setParameter("uom", uom);

            return query.getResultList();
        } catch (IllegalArgumentException e) {
            throw new RestInvalidArgumentException("Invalid UUID format ", e);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to fetch data", ex);
        }
    }

	@Override
	public AOPMessageVM calculateNormsHistorianValues(String plantId, String year, String periodFrom, String periodTo) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_GetNormsHistorianValue";
			System.out.println(storedProcedure);
			Integer result=  executeDynamicUpdateProcedure(storedProcedure, plantId, year,periodFrom,periodTo);
			
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
			String aopYear,String PeriodFrom, String PeriodTo) {
		System.out.println("PeriodFrom="+PeriodFrom);
		System.out.println("PeriodTo="+PeriodTo);
		try {
			
			String callSql = "{call " + procedureName + "(?, ?, ?, ?)}";

	        try (Connection connection = dataSource.getConnection();
	             CallableStatement stmt = connection.prepareCall(callSql)) {

	            // Set parameters in the correct order
	            stmt.setString(1, plantId); // @finYear
	            stmt.setString(2, aopYear); // @siteId
	            stmt.setString(3, PeriodFrom);
	            stmt.setString(4, PeriodTo);

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

}
