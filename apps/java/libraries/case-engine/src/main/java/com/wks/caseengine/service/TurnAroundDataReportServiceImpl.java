package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.MonthWiseConsumptionSummaryDTO;
import com.wks.caseengine.dto.TurnAroundPlanReportDTO;
import com.wks.caseengine.entity.MonthwiseConsumptionReport;
import com.wks.caseengine.entity.TurnAroundPlan;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;

import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.TurnAroundPlanReportRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class TurnAroundDataReportServiceImpl implements TurnAroundDataReportService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PlantsRepository plantsRepository;
    
    @Autowired
    private TurnAroundPlanReportRepository turnAroundPlanReportRepository;

    @Override
    public AOPMessageVM getReportForTurnAroundPlanData(String plantId, String year, String reportType) {
        // TODO Auto-generated method stub

        try {
            AOPMessageVM aopMessageVM = new AOPMessageVM();
            List<Map<String, Object>> plantTurnAroundData = new ArrayList<>();

            List<Object[]> obj = getPlantTurnAroundData(plantId, year, reportType);
            if (reportType.equalsIgnoreCase("currentYear")) {
                for (Object[] row : obj) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("sno", row[0]);
                    map.put("activity", row[1]);
                    map.put("fromDate", row[2]);
                    map.put("toDate", row[3]);
                    map.put("durationInHrs", row[4]);
                    // map.put("remarks", row[5]);
                    map.put("remarks", row[5] != null ? row[5] : "");

                    map.put("periodInMonths", row[6]);
                    map.put("Id", row[7]);

                    plantTurnAroundData.add(map);
                }
            }

            if (reportType.equalsIgnoreCase("previousYear")) {
                for (Object[] row : obj) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("sno", row[0]);
                    map.put("activity", row[1]);
                    map.put("fromDate", row[2]);
                    map.put("toDate", row[3]);
                    map.put("durationInHrs", row[4]);
                    // map.put("remarks", row[5]);

                    map.put("remarks", row[5] != null ? row[5] : "");

                    map.put("periodInMonths", row[6]);
                    map.put("Id", row[7]);

                    plantTurnAroundData.add(map);
                }
            }

            // Combine both into a result map
            Map<String, Object> finalResult = new HashMap<>();
            finalResult.put("plantTurnAroundReportData", plantTurnAroundData);

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

    public List<Object[]> getPlantTurnAroundData(String plantId, String aopYear, String reportType) {
        try {
            String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
            String storedProcedure = "TurnAroundPlanReport";
            String sql = "EXEC " + storedProcedure
                    + " @plantId = :plantId, @aopYear = :aopYear, @reportType = :reportType";

            Query query = entityManager.createNativeQuery(sql);

            query.setParameter("plantId", plantId);
            query.setParameter("aopYear", aopYear);
            query.setParameter("reportType", reportType);

            return query.getResultList();
        } catch (IllegalArgumentException e) {
            throw new RestInvalidArgumentException("Invalid UUID format ", e);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to fetch data", ex);
        }
    }

	@Override
	public AOPMessageVM updateReportForTurnAroundData(String plantId, String year,
			String reportType,List<TurnAroundPlanReportDTO> dataList) {
		try {
		List<TurnAroundPlan> turnAroundPlanList = new ArrayList<>();
		for (TurnAroundPlanReportDTO dto : dataList) {
			TurnAroundPlan turnAroundPlan=null;
			if(dto.getId()!=null) {
				turnAroundPlan = turnAroundPlanReportRepository
						.findById(UUID.fromString(dto.getId())).get();
			}else {
				 turnAroundPlan=new TurnAroundPlan();
				 turnAroundPlan.setPlantFkId(UUID.fromString(plantId));
				 turnAroundPlan.setAopYear(year);
				 turnAroundPlan.setReportType(reportType);
			}
			
			//optional.get().setRemark(dto.getRemark());
			if(dto.getActivity()!=null) {
				turnAroundPlan.setActivity(dto.getActivity());
			}
			if(dto.getFromDate()!=null) {
				turnAroundPlan.setFromDate(dto.getFromDate());
			}
			if(dto.getToDate()!=null) {
				turnAroundPlan.setToDate(dto.getToDate());
			}
			if(dto.getDurationInHrs()!=null) {
				turnAroundPlan.setDurationInHrs(dto.getDurationInHrs());
			}
			if(dto.getPeriodInMonths()!=null) {
				turnAroundPlan.setPeriodInMonths(dto.getPeriodInMonths());
			}
			if(dto.getRemark()!=null) {
				turnAroundPlan.setRemark(dto.getRemark());
			}
			
			turnAroundPlanList.add(turnAroundPlanReportRepository.save(turnAroundPlan));
		}
		AOPMessageVM response = new AOPMessageVM();
		response.setCode(200);
		response.setMessage("Remarks updated successfully.");
		response.setData(turnAroundPlanList);
		return response;
		}catch (Exception ex) {
            throw new RuntimeException("Failed to save data", ex);
        }
	}

}
