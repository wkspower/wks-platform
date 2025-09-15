package com.wks.caseengine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.ReliabilityPerformanceDto;
import com.wks.caseengine.dto.ReliabilityRecordDto;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;

@Service
public class ReliabilityServiceImpl implements ReliabilityService{
	
	 @PersistenceContext
	 private EntityManager entityManager;
	 
	 @Autowired
	 private PlantsRepository plantsRepository;

	 @Override
	 public AOPMessageVM getReliabilityPerformance(String plantId, String year, String type) {
	     List<ReliabilityPerformanceDto> reliabilityPerformanceDtos = new ArrayList<>();
	     try {
	         String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
	         String viewName = "vwScrn" + verticalName + "ReliabilityPerformance";

	         List<Object[]> rows = findByViewNameAopYearPlantId(viewName, year, UUID.fromString(plantId), type);
	         for (Object[] row : rows) {
	             ReliabilityPerformanceDto dto = new ReliabilityPerformanceDto();

	             int idx = 0;
	             // id
	             dto.setId(row[idx] != null ? UUID.fromString(row[idx].toString()) : null);
	             idx++;
	             // row_no
	             dto.setRowNo(row[idx] != null ? ((Number) row[idx]).intValue() : null);
	             idx++;
	             // parameter
	             dto.setParameter(row[idx] != null ? row[idx].toString() : null);
	             idx++;
	             // uom
	             dto.setUom(row[idx] != null ? row[idx].toString() : null);
	             idx++;
	             // best_achieved as double
	             if (row[idx] != null) {
	                 dto.setBestAchieved(Double.parseDouble(row[idx].toString()));
	             } else {
	                 dto.setBestAchieved(null);
	             }
	             idx++;
	             // aop
	             if (row[idx] != null) {
	                 dto.setAop(Double.parseDouble(row[idx].toString()));
	             } else {
	                 dto.setAop(null);
	             }
	             idx++;
	             // actual
	             if (row[idx] != null) {
	                 dto.setActual(Double.parseDouble(row[idx].toString()));
	             } else {
	                 dto.setActual(null);
	             }
	             idx++;
	             // plann
	             if (row[idx] != null) {
	                 dto.setPlann(Double.parseDouble(row[idx].toString()));
	             } else {
	                 dto.setPlann(null);
	             }
	             idx++;
	             // limit
	             dto.setLimit(row[idx] != null ? row[idx].toString() : null);
	             idx++;
	             // rationale
	             dto.setRationale(row[idx] != null ? row[idx].toString() : null);
	             idx++;
	             // created_at → java.util.Date
	             if (row[idx] != null) {
	                 // assuming row[idx] is java.sql.Timestamp
	                 java.sql.Timestamp ts = (java.sql.Timestamp) row[idx];
	                 dto.setCreatedAt(new Date(ts.getTime()));
	             } else {
	                 dto.setCreatedAt(null);
	             }
	             idx++;
	             // updated_at
	             if (row[idx] != null) {
	                 java.sql.Timestamp ts = (java.sql.Timestamp) row[idx];
	                 dto.setUpdatedAt(new Date(ts.getTime()));
	             } else {
	                 dto.setUpdatedAt(null);
	             }
	             idx++;
	             // updated_by
	             dto.setUpdatedBy(row[idx] != null ? row[idx].toString() : null);
	             idx++;
	             // remarks
	             dto.setRemarks(row[idx] != null ? row[idx].toString() : null);
	             idx++;
	             // aopYear
	             dto.setAopYear(row[idx] != null ? row[idx].toString() : null);
	             idx++;
	             // plantId
	             dto.setPlantId(row[idx] != null ? UUID.fromString(row[idx].toString()) : null);
	             idx++;
	             // reportType
	             dto.setReportType(row[idx] != null ? row[idx].toString() : null);
	             idx++;

	             reliabilityPerformanceDtos.add(dto);
	         }

	         AOPMessageVM vm = new AOPMessageVM();
	         vm.setCode(200);
	         vm.setMessage("Data fetched successfully");
	         vm.setData(reliabilityPerformanceDtos);
	         return vm;

	     } catch (IllegalArgumentException e) {
	         throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	     } catch (Exception ex) {
	         throw new RuntimeException("Failed to fetch data", ex);
	     }
	 }
	 
	 @Override
	 public AOPMessageVM getReliabilityRecords(String plantId, String year, String type) {
	     List<ReliabilityRecordDto> reliabilityRecordDtos = new ArrayList<ReliabilityRecordDto>();
	     try {
	         String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
	         String viewName = "vwScrn" + verticalName + "ReliabilityRecords";

	         List<Object[]> rows = findByAopYearPlantId(viewName, year, UUID.fromString(plantId), type);
	         for (Object[] row : rows) {
	        	 ReliabilityRecordDto dto = new ReliabilityRecordDto();
	             
	             dto.setId(row[0] != null ? UUID.fromString(row[0].toString()) : null);
	        	 dto.setReportType(row[1] != null ? row[1].toString() : null);
	        	 dto.setIncidentDescription(row[2] != null ? row[2].toString() : null);
	        	 dto.setRootCauseAnalysis(row[3] != null ? row[3].toString() : null);
	        	 dto.setInitiative(row[4] != null ? row[4].toString() : null);
	        	 dto.setOutcome(row[5] != null ? row[5].toString() : null);
	        	 dto.setRecommendation(row[6] != null ? row[6].toString() : null);
	        	 if (row[7] != null) {
	        		    Object dbValue = row[7];
	        		    if (dbValue instanceof java.sql.Timestamp) {
	        		        // If it's a Timestamp, convert it to a util.Date
	        		        java.sql.Timestamp sqlTimestamp = (java.sql.Timestamp) dbValue;
	        		        java.util.Date utilDate = new java.util.Date(sqlTimestamp.getTime());
	        		        dto.setTargetDate(utilDate);
	        		    } else if (dbValue instanceof java.sql.Date) {
	        		        // If it's a Date, handle it as originally intended
	        		        java.sql.Date sqlDate = (java.sql.Date) dbValue;
	        		        java.util.Date utilDate = new java.util.Date(sqlDate.getTime());
	        		        dto.setTargetDate(utilDate);
	        		    } else {
	        		        // Handle other possible types or throw an exception if necessary
	        		        dto.setTargetDate(null);
	        		    }
	        		} else {
	        		    dto.setTargetDate(null);
	        		}
	        	 dto.setResponsible(row[8] != null ? row[8].toString() : null);
	        	 if (row[9] != null) {
	        		    Object createdAtValue = row[9];
	        		    if (createdAtValue instanceof java.sql.Timestamp) {
	        		        java.sql.Timestamp sqlTimestamp = (java.sql.Timestamp) createdAtValue;
	        		        dto.setCreatedAt(new java.util.Date(sqlTimestamp.getTime()));
	        		    } else if (createdAtValue instanceof java.sql.Date) {
	        		        java.sql.Date sqlDate = (java.sql.Date) createdAtValue;
	        		        dto.setCreatedAt(new java.util.Date(sqlDate.getTime()));
	        		    } else {
	        		        dto.setCreatedAt(null);
	        		    }
	        		} else {
	        		    dto.setCreatedAt(null);
	        		}

	        		if (row[10] != null) {
	        		    Object updatedAtValue = row[10];
	        		    if (updatedAtValue instanceof java.sql.Timestamp) {
	        		        java.sql.Timestamp sqlTimestamp = (java.sql.Timestamp) updatedAtValue;
	        		        dto.setUpdatedAt(new java.util.Date(sqlTimestamp.getTime()));
	        		    } else if (updatedAtValue instanceof java.sql.Date) {
	        		        java.sql.Date sqlDate = (java.sql.Date) updatedAtValue;
	        		        dto.setUpdatedAt(new java.util.Date(sqlDate.getTime()));
	        		    } else {
	        		        dto.setUpdatedAt(null);
	        		    }
	        		} else {
	        		    dto.setUpdatedAt(null);
	        		}
	        	 dto.setUpdatedBy(row[11] != null ? row[11].toString() : null);
	        	 dto.setRemarks(row[12] != null ? row[12].toString() : null);
	        	 dto.setAopYear(row[13] != null ? row[13].toString() : null);
	        	 
	        	 reliabilityRecordDtos.add(dto);
	         }

	         AOPMessageVM vm = new AOPMessageVM();
	         vm.setCode(200);
	         vm.setMessage("Data fetched successfully");
	         vm.setData(reliabilityRecordDtos);
	         return vm;

	     } catch (IllegalArgumentException e) {
	         throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	     } catch (Exception ex) {
	         throw new RuntimeException("Failed to fetch data", ex);
	     }
	 }


	
	public List<Object[]> findByViewNameAopYearPlantId(
            String viewName, String aopYear, UUID plantId, String reportType) {

       
        // explicit column list
        String sql = "SELECT id, row_no, parameter, uom, best_achieved, aop, actual, plann, [limit], rationale, " +
                     "created_at, updated_at, updated_by, remarks, aopYear, plantId, reportType " +
                     "FROM " + viewName + " " +
                     "WHERE aopYear = :aopYear AND plantId = :plantId and reportType = :reportType";

        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("aopYear", aopYear);
        q.setParameter("plantId", plantId);
        q.setParameter("reportType", reportType);
        @SuppressWarnings("unchecked")
        List<Object[]> resultList = q.getResultList();
        return resultList;
    }
	
	public List<Object[]> findByAopYearPlantId(
            String viewName, String aopYear, UUID plantId, String reportType) {

       
		String sql = "SELECT id, reportType, IncidentDescription, RootCauseAnalysis, Initiative, Outcome, Recommendation, " +
	             "TargetDate, Responsible, created_at, updated_at, updated_by, remarks, aopYear, plantId " +
	             "FROM " + viewName + " " +
	             "WHERE aopYear = :aopYear AND plantId = :plantId AND reportType = :reportType";


        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("aopYear", aopYear);
        q.setParameter("plantId", plantId);
        q.setParameter("reportType", reportType);
        @SuppressWarnings("unchecked")
        List<Object[]> resultList = q.getResultList();
        return resultList;
    }

}
