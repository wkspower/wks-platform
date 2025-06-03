package com.wks.caseengine.service;

 import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AOPDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

@Service
public class ExcelDataServiceImpl implements ExcelDataService{

    @Autowired
    ProductionVolumeDataReportService  productionVolumeDataReportService;

    
    @Autowired
	private AOPService aopService;


    @Autowired
	private AOPReportService aopReportService;


    @Autowired
	private TurnAroundDataReportService turnAroundDataReportService;

    @Override
    public List<List<Object>> getDataForProductionVolumeReport(String plantId, String year) {
       
        
     AOPMessageVM aopMessageVM = productionVolumeDataReportService.getReportForProductionVolumnData(plantId,year);

      List<Map<String, Object>> productionVolumeReportList = (List<Map<String, Object>>)aopMessageVM.getData();

      String[] headers = {"RowNo", "Particulates", "UOM", "BudgetPrevYear", "ActualPrevYear",
                        "BudgetCurrentYear", "VarBudgetMT", "VarBudgetPer", "VarActualMT", "VarActualPer", "Remark"};

    

	List<List<Object>> dataList = new ArrayList<>();
    // Data rows
    for (Map<String, Object> map : productionVolumeReportList) {
    	List<Object> list = new ArrayList<>();
    	for(String header :headers) {
    		list.add(map.get(header));
    	}
    	dataList.add(list);     
    }
				
    return dataList;
 
    }


    @Override
    public List<List<Object>> getReportForMonthWiseProductionData(String plantId, String year) {
       
        
     AOPMessageVM aopMessageVM = productionVolumeDataReportService.getReportForMonthWiseProductionData(plantId,year);

     Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
      List<Map<String, Object>> productionVolumeReportList = (List<Map<String, Object>>)responseMap.get("data");

      String[] headers = {
    "RowNo",
    "Month",
    "EOEProdBudget",
    "EOEProdActual",
    "OpHrsBudget",
    "OpHrsActual",
    "ThroughputBudget",
    "ThroughputActual",
    "OperatingHours",
    "MEGThroughput",
    "EOThroughput",
    "EOEThroughput",
    "TotalEOE",
    "Remark"
};

    

	List<List<Object>> dataList = new ArrayList<>();
    // Data rows
    for (Map<String, Object> map : productionVolumeReportList) {
    	List<Object> list = new ArrayList<>();
    	for(String header :headers) {
    		list.add(map.get(header));
    	}
    	dataList.add(list);     
    }
				
    return dataList;
 
    }

    @Override
    public List<List<Object>> getReportForMonthWiseConsumptionForSelectivityData(String plantId, String year) {
       
        
     AOPMessageVM aopMessageVM = productionVolumeDataReportService.getReportForMonthWiseConsumptionSummaryData(plantId,year,"Selectivity");

     Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
      List<Map<String, Object>> productionVolumeReportList = (List<Map<String, Object>>)responseMap.get("consumptionSummary");

      System.out.println("getReportForMonthWiseConsumptionForSelectivityData "+productionVolumeReportList);
      String[] headers = {
    "material",
    "april",
    "may",
    "june",
    "july",
    "aug",
    "sep",
    "oct",
    "nov",
    "dec",
    "jan",
    "feb",
    "march"
};

System.out.println("Selectivity data " + headers.length);
	List<List<Object>> dataList = new ArrayList<>();
    // Data rows
    for (Map<String, Object> map : productionVolumeReportList) {
    	List<Object> list = new ArrayList<>();
    	for(String header :headers) {
            System.out.println("Selectivity data"+ header+ " "+map.get(header));
    		list.add(map.get(header));
    	}
    	dataList.add(list);     
    }
	
    System.out.println("getReportForMonthWiseConsumptionForSelectivityData2 "+dataList);
    return dataList;

 
    }


    @Override
    public Map<String, List<List<Object>>> getReportForMonthWiseConsumptionSummaryData(String plantId, String year) {
        Map<String, List<List<Object>>> outerMap = new HashMap<>(); 
     AOPMessageVM aopMessageVM = productionVolumeDataReportService.getReportForMonthWiseConsumptionSummaryData(plantId,year,"NormQuantity");

     Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
      List<Map<String, Object>> annualAOPReportList = (List<Map<String, Object>>)responseMap.get("consumptionSummary");
System.out.println("getReportForMonthWiseConsumptionSummaryData "+annualAOPReportList);
String[] headers={
    
 
"material", 
"UOM", 
"spec", 
"april", 
"may", 
"june", 
"july", 
"aug", 
"sep", 
"oct", 
"nov", 
"dec", 
"jan", 
"feb", 
"march", 
"total" 
};


	for (Map<String, Object> map : annualAOPReportList) {
    	List<Object> list = new ArrayList<>();
    	for(String header :headers) {
            if(header.equals("")){
                list.add("");
            }else{
               list.add(map.get(header));
            }
    		
    	}

        if(outerMap.containsKey(map.get("normType"))){
           List<List<Object>> dataList = outerMap.get(map.get("normType"));
           dataList.add(list);
        }else{
           List<List<Object>> dataList = new ArrayList<>();
           dataList.add(list);
           outerMap.put(map.get("normType").toString(), dataList);
        }
    }
				
    System.out.println("getReportForMonthWiseConsumptionSummaryData "+outerMap);
    return outerMap;
 
    }



    @Override
    public List<List<Object>> getAOPData(String plantId, String year) {
       
     AOPMessageVM aopMessageVM = aopService.getAOPData(plantId,year);   

Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
      List<AOPDTO> aOPList = (List<AOPDTO>)responseMap.get("aopDTOList" );


    

	List<List<Object>> dataList = new ArrayList<>();
    // Data rows
    for (AOPDTO dto : aOPList) {
    	Double sum = 0.0;
        List<Object> list = new ArrayList<>();
    	list.add(dto.getNormParameterName());
        list.add(dto.getApril());
        list.add(dto.getMay());
        list.add(dto.getJune());
        list.add(dto.getJuly());
        list.add(dto.getAug());
        list.add(dto.getSep());
        list.add(dto.getOct());
        list.add(dto.getNov());
        list.add(dto.getDec());
        list.add(dto.getJan());
        list.add(dto.getFeb());
        list.add(dto.getMarch());
        list.add(calculateSum(dto.getApril(), sum)
        +calculateSum(dto.getMay(), sum)
        +calculateSum(dto.getJune(), sum)
        +calculateSum(dto.getJuly(), sum)
        +calculateSum(dto.getAug(), sum)
        +calculateSum(dto.getSep(), sum)
        +calculateSum(dto.getOct(), sum)
        +calculateSum(dto.getNov(), sum) 
        +calculateSum(dto.getDec(), sum)
        +calculateSum(dto.getJan(), sum)
        +calculateSum(dto.getFeb(), sum)
        +calculateSum(dto.getMarch(), sum));

    	dataList.add(list);     
    }
				
    return dataList;
 
    }

     
    Double calculateSum(Double value, Double sum){
         if(value!=null){
            sum = sum+value;
         }
      return sum;
    }

    public static Map<String, List<Double>> getColumnSums(Map<String, List<List<Object>>> dataMap) {
        Map<String, List<Double>> result = new HashMap<>();

        for (Map.Entry<String, List<List<Object>>> entry : dataMap.entrySet()) {
            String key = entry.getKey();
            List<List<Object>> rows = entry.getValue();

            if (rows.isEmpty()) {
                result.put(key, new ArrayList<>());
                continue;
            }

            int columnCount = rows.get(0).size();
            List<Double> columnSums = new ArrayList<>(Collections.nCopies(columnCount, 0.0));

            for (List<Object> row : rows) {
                for (int i = 0; i < row.size(); i++) {
                    Object value = row.get(i);
                    if (value instanceof Number) {
                        Double existing = columnSums.get(i);
                        columnSums.set(i, existing + ((Number) value).doubleValue());
                    }
                }
            }

            result.put(key, columnSums);
        }

        return result;
    }

    @Override
    public Map<String, List<List<Object>>> getAnnualAOPReport(String plantId, String year) {
    
    Map<String, List<List<Object>>> outerMap = new HashMap<>();    
     AOPMessageVM aopMessageVM = aopReportService.getAnnualAOPReport(plantId,year,"quantity","FY "+year+" AOP");;

      List<Map<String, Object>> annualAOPReportList = (List<Map<String, Object>>)aopMessageVM.getData();

      String[] headers = {
    "particulars",
    "",
    "", 
"april", 
"may", 
"june", 
"july", 
"august", 
"september", 
"october", 
"november", 
"december", 
"january", 
"february", 
"march", 
"total",
};

    

	//
    // Data rows
    for (Map<String, Object> map : annualAOPReportList) {
    	List<Object> list = new ArrayList<>();
    	for(String header :headers) {
            if(header.equals("")){
                list.add("");
            }else{
               list.add(map.get(header));
            }
    		
    	}

        if(outerMap.containsKey(map.get("norm"))){
           List<List<Object>> dataList = outerMap.get(map.get("norm"));
           dataList.add(list);
        }else{
           List<List<Object>> dataList = new ArrayList<>();
           dataList.add(list);
           outerMap.put(map.get("norm").toString(), dataList);
        }
    }
    
    



				
    return outerMap;
 
    }

    @Override
    public List<List<Object>> getReportForTurnAroundPlanData(String plantId, String year, String reportType) {
       
        
     AOPMessageVM aopMessageVM	= turnAroundDataReportService.getReportForTurnAroundPlanData(plantId,year,reportType);

     Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
      List<Map<String, Object>> productionVolumeReportList = (List<Map<String, Object>>)responseMap.get("plantTurnAroundReportData");

      String[] headers = {
    "sno",
    "activity",
    "fromDate",
    "toDate",
    "durationInHrs",
    "remarks"
};

    

	List<List<Object>> dataList = new ArrayList<>();
    // Data rows
    for (Map<String, Object> map : productionVolumeReportList) {
    	List<Object> list = new ArrayList<>();
    	for(String header :headers) {
    		list.add(map.get(header));
    	}
    	dataList.add(list);     
    }
				
    return dataList;
 
    }


    @Override
    public List<List<Object>> getReportForPlantProductionPlanData(String plantId, String year, String reportType) {
       
        
     AOPMessageVM aopMessageVM = productionVolumeDataReportService.getReportForPlantProductionPlanData(plantId, year,
				reportType);

     Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
      List<Map<String, Object>> productionVolumeReportList = (List<Map<String, Object>>)responseMap.get("plantProductionData");

      String[] headers = {
    "sno",
    "activity",
    "fromDate",
    "toDate",
    "durationInHrs",
    "remarks"
};
      if(reportType.equalsIgnoreCase("maxRate")){
      headers = new String[] {
        "sno",
        "part1",
        "part2",
        "part3"
        };

      }else if(reportType.equalsIgnoreCase("OperatingHrs")){
        headers = new String[] {
            "sno",
            "part1",
            "part2",
            "part3"
            };

      }else if(reportType.equalsIgnoreCase("AverageHourlyRate")){
         headers = new String[] {
            "sno",
            "Throughput" , //
            "HourlyRate",
            "OperatingHrs",
            "PeriodFrom", //
             "PeriodTo" 
            };

      }else if(reportType.equalsIgnoreCase("assumptions")){
         headers = new String[] {
            "sno",
            "part1"
            };
        
      }else if(reportType.equalsIgnoreCase("ProductionPerformance")){
        headers = new String[] {
            "sno",
            "Item",
            "Budget1" ,
            "Actual1" ,
            "Budget2" , 
            "Actual2" ,
            "Budget3" , 
            "Actual3",
            "Budget4" ,
            };
      }
      

    

	List<List<Object>> dataList = new ArrayList<>();
    // Data rows
    for (Map<String, Object> map : productionVolumeReportList) {
    	List<Object> list = new ArrayList<>();
    	for(String header :headers) {
    		list.add(map.get(header));
    	}
    	dataList.add(list);     
    }
				
    return dataList;
 
    }


     @Override
    public List<List<Object>> getReportForPlantContributionYearWise(String plantId, String year, String reportType) {
       
        
    AOPMessageVM aopMessageVM = productionVolumeDataReportService.getReportForPlantContributionYearWise(plantId, year,
				reportType);

     Map<String, Object> responseMap = (Map<String, Object>) aopMessageVM.getData();
      List<Map<String, Object>> productionVolumeReportList = (List<Map<String, Object>>)responseMap.get("plantProductionData");

      String[] headers = {
    "sno",
    "activity",
    "fromDate",
    "toDate",
    "durationInHrs",
    "remarks"
};
      if(reportType.equalsIgnoreCase("RawMaterial")){
      headers = new String[] {
            "SrNo",
            "ByProductName",
            "Unit",
            "Price",
            "PrevYearNormBudget",
            "PrevYearNormActual",
            "NextYearNormActual",
            "PrevYearCostBudget",
            "PrevYearCostActual",
            "NextYearCostActual"
            
            };
      }else if(reportType.equalsIgnoreCase("ByProducts")){
       headers = new String[] {
            "SrNo",
            "ByProductName",
            "Unit",
            "Price",
            "PrevYearNormBudget",
            "PrevYearNormActual",
            "NextYearNormActual",
            "PrevYearCostBudget",
            "PrevYearCostActual",
            "NextYearCostActual"
            
            };

      }else if(reportType.equalsIgnoreCase("ProductMixAndProduction")){
         headers = new String[] {
            "SrNo",
"ByProductName",
"Unit",
"Price",
"PrevYearNormBudget",
"PrevYearNormActual",
"NextYearCostBudget"

            };

      }else if(reportType.equalsIgnoreCase("CatChem")){
        headers = new String[] {
            "SrNo",
            "ByProductName",
            "Unit",
            "Price",
            "PrevYearNormBudget",
            "PrevYearNormActual",
            "NextYearNormActual",
            "PrevYearCostBudget",
            "PrevYearCostActual",
            "NextYearCostActual"
            
            };
        
      }else if(reportType.equalsIgnoreCase("Utilities")){
        headers = new String[] {
            "SrNo",
            "ByProductName",
            "Unit",
            "Price",
            "PrevYearNormBudget",
            "PrevYearNormActual",
            "NextYearNormActual",
            "PrevYearCostBudget",
            "PrevYearCostActual",
            "NextYearCostActual"
            
            };
      }else if(reportType.equalsIgnoreCase("OtherVariableCost")){
        headers = new String[] {
            "SrNo", 
"OtherCost", 
"Unit", 
"PrevYearBudget", 
"PrevYearActual", 
"CurrentYearBudget", 
            };
      }else if(reportType.equalsIgnoreCase("ProductionCostCalculations")){
        headers = new String[] {
           "SrNo", 
"ProductionCostCalculations", 
"PrevYearBudget", 
"PrevYearActual", 
"NextYearBudget", 
            };
      }
      

    

	List<List<Object>> dataList = new ArrayList<>();
    // Data rows
    for (Map<String, Object> map : productionVolumeReportList) {
    	List<Object> list = new ArrayList<>();
    	for(String header :headers) {
    		list.add(map.get(header));
    	}
    	dataList.add(list);     
    }
				
    return dataList;
 
    }


    
    
    
}
