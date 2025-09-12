package com.wks.caseengine.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

import com.wks.caseengine.dto.BudgetMaintenanceDto;
import com.wks.caseengine.dto.DecokePlanningDTO;
import com.wks.caseengine.dto.MaintenanceDetailsDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.BudgetMaintenance;
import com.wks.caseengine.entity.DecokeMaintenance;
import com.wks.caseengine.entity.DecokePlanning;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.BudgetMaintenanceRepository;
import com.wks.caseengine.repository.DecokeMaintenanceRepository;
import com.wks.caseengine.repository.DecokePlanningRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

@Service
public class MaintenanceCalculatedDataServiceImpl implements MaintenanceCalculatedDataService {

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private DecokePlanningRepository decokePlanningRepository;
	
	@Autowired
	private DecokeMaintenanceRepository decokeMaintenanceRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private BudgetMaintenanceRepository budgetMaintenanceRepository;

	@Override
	public List<MaintenanceDetailsDTO> getMaintenanceCalculatedData(String plantId, String year) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_GETMaintenance";
			List<Object[]> list = executeDynamicStoredProcedure(storedProcedure, plantId, site.getId().toString(),
					vertical.getId().toString(), year);
			List<MaintenanceDetailsDTO> maintenanceDetailsDTOList = new ArrayList<>();
			for (Object[] row : list) {
				MaintenanceDetailsDTO dto = new MaintenanceDetailsDTO();
				dto.setName(row[2] != null ? row[2].toString() : null);
				dto.setJan(row[3] != null ? Double.valueOf(row[3].toString()) : null);
				dto.setFeb(row[4] != null ? Double.valueOf(row[4].toString()) : null);
				dto.setMar(row[5] != null ? Double.valueOf(row[5].toString()) : null);
				dto.setApril(row[6] != null ? Double.valueOf(row[6].toString()) : null);
				dto.setMay(row[7] != null ? Double.valueOf(row[7].toString()) : null);
				dto.setJune(row[8] != null ? Double.valueOf(row[8].toString()) : null);
				dto.setJuly(row[9] != null ? Double.valueOf(row[9].toString()) : null);
				dto.setAug(row[10] != null ? Double.valueOf(row[10].toString()) : null);
				dto.setSep(row[11] != null ? Double.valueOf(row[11].toString()) : null);
				dto.setOct(row[12] != null ? Double.valueOf(row[12].toString()) : null);
				dto.setNov(row[13] != null ? Double.valueOf(row[13].toString()) : null);
				dto.setDec(row[14] != null ? Double.valueOf(row[14].toString()) : null);
				maintenanceDetailsDTOList.add(dto);
			}

			return maintenanceDetailsDTOList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional
	public List<Object[]> executeDynamicStoredProcedure(String procedureName, String plantId, String siteId,
			String verticalId, String aopYear) {
		try {
			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @aopYear = :aopYear";
			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getMaintenanceDataForCracker(String plantId, String year) {

		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> data = new ArrayList<>();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			String procedureName = "vwScrn" + vertical.getName() + "_" + site.getName() + "_Decoke_Maintenance";
			List<Object[]> results = getData(plantId, year, procedureName);

			for (Object[] row : results) {
				Map<String, Object> map = new HashMap<>(); // Create a new map for each row

				map.put("id", row[0]);
				map.put("monthName", row[1]);
				map.put("coilReplacement", row[2]);
				map.put("mnt", row[3]);
				map.put("shutdown", row[4]);
				map.put("slowdown", row[5]);
				map.put("sad", row[6]);
				map.put("bbd", row[7]);
				map.put("bbu", row[8]);
				map.put("demoHSS", row[9]);
				map.put("demoBBU", row[10]);
				map.put("demoSAD", row[11]);
				map.put("demoSD", row[12]);
				map.put("fourFD", row[13]);
				map.put("fourF", row[14]);
				map.put("fiveF", row[15]);
				map.put("total", row[16]);
				map.put("fourFHours", row[17]);
				map.put("aopYear", row[18]);
				map.put("plantId", row[19]);
				String remarks = row[20] == null ? "" : row[20].toString();
				map.put("remarks", remarks);
				map.put("totalSAD", row[21]);
				map.put("numberOfDays", row[22]);

				data.add(map); // Add the map to the list here
			}

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(data);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}

	public List<Object[]> getData(String plantId, String aopYear, String viewName) {
		try {

			// 2. Construct SQL with dynamic view name
			String sql = "SELECT * FROM " + viewName +
					" WHERE PlantId = :plantId AND AOPYear = :aopYear " +
					"ORDER BY CASE MonthName " +
					"    WHEN 'April' THEN 1 " +
					"    WHEN 'May' THEN 2 " +
					"    WHEN 'June' THEN 3 " +
					"    WHEN 'July' THEN 4 " +
					"    WHEN 'August' THEN 5 " +
					"    WHEN 'September' THEN 6 " +
					"    WHEN 'October' THEN 7 " +
					"    WHEN 'November' THEN 8 " +
					"    WHEN 'December' THEN 9 " +
					"    WHEN 'January' THEN 10 " +
					"    WHEN 'February' THEN 11 " +
					"    WHEN 'March' THEN 12 " +
					"    ELSE 13 " +
					"END";

			// 3. Create and parameterize the native query
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);

			// 4. Execute
			return query.getResultList();

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid argument: " + e.getMessage(), e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data from view " + viewName, ex);
		}
	}

	@Override
	public AOPMessageVM updateMaintenanceDataForCracker(String plantId, String year,
			List<DecokePlanningDTO> decokePlanningDTOList) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<DecokeMaintenance> decokeMaintenanceList = new ArrayList<>();
		try {
			for (DecokePlanningDTO decokePlanningDTO : decokePlanningDTOList) {
				Optional<DecokeMaintenance> decokePlanningop = decokeMaintenanceRepository
						.findById(decokePlanningDTO.getId());
				if (decokePlanningop.isPresent()) {
					DecokeMaintenance decokeMaintenance = decokePlanningop.get();
					decokeMaintenance.setMnt(decokePlanningDTO.getMnt());
					decokeMaintenance.setRemarks(decokePlanningDTO.getRemarks());
					decokeMaintenance.setBbd(decokePlanningDTO.getBbd());
					decokeMaintenance.setBbu(decokePlanningDTO.getBbu());
					decokeMaintenance.setDemoBbu(decokePlanningDTO.getDemoBBU());
					decokeMaintenance.setDemoHss(decokePlanningDTO.getDemoHSS());
					decokeMaintenance.setDemoSad(decokePlanningDTO.getDemoSAD());
					decokeMaintenance.setDemoSd(decokePlanningDTO.getDemoSD());
					decokeMaintenance.setFiveF(decokePlanningDTO.getFiveF());
					decokeMaintenance.setFourF(decokePlanningDTO.getFourF());
					decokeMaintenance.setFourFd(decokePlanningDTO.getFourFD());
					decokeMaintenance.setFourFHours(decokePlanningDTO.getFourFHours());
					decokeMaintenance.setIbr(decokePlanningDTO.getIbr());
					decokeMaintenance.setShoutdown(decokePlanningDTO.getShutdown());
					decokeMaintenance.setSad(decokePlanningDTO.getSad());
					decokeMaintenance.setSlowdown(decokePlanningDTO.getSlowdown());
					decokeMaintenance.setTotal(decokePlanningDTO.getTotal());
					decokeMaintenanceList.add(decokeMaintenanceRepository.save(decokeMaintenance));
				}
			}
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to update data", ex);
		}
		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("maintenance-details");
		for (ScreenMapping screenMapping : screenMappingList) {
			AopCalculation aopCalculation = new AopCalculation();
			aopCalculation.setAopYear(year);
			aopCalculation.setIsChanged(true);
			aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
			aopCalculation.setPlantId(UUID.fromString(plantId));
			aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
			aopCalculationRepository.save(aopCalculation);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Data updated successfully");
		aopMessageVM.setData(decokeMaintenanceList);
		return aopMessageVM;

	}

	@Override
	public AOPMessageVM getBudgetMaintenance(String plantId, String year,String budgetCategory) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<BudgetMaintenance> budgetMaintenanceList=null;
		
		UUID plant=null;
		if(plantId!=null && (!plantId.equalsIgnoreCase("ALL"))) {
			plant=UUID.fromString(plantId);
			 budgetMaintenanceList	= budgetMaintenanceRepository.findByPlantIdAndAOPYear(plant,year,budgetCategory);
		}else {
			 budgetMaintenanceList	= budgetMaintenanceRepository.findByAOPYear(year,budgetCategory);
			
		}
		List<BudgetMaintenanceDto> budgetMaintenanceDtoList = new ArrayList<BudgetMaintenanceDto>();
		try {
			
			for(BudgetMaintenance budgetMaintenance:budgetMaintenanceList) {
				BudgetMaintenanceDto budgetMaintenanceDto = new BudgetMaintenanceDto();
				budgetMaintenanceDto.setAopYear(budgetMaintenance.getAopYear());
				budgetMaintenanceDto.setApr(budgetMaintenance.getApr());
				budgetMaintenanceDto.setMay(budgetMaintenance.getMay());
				budgetMaintenanceDto.setJun(budgetMaintenance.getJun());
				budgetMaintenanceDto.setJul(budgetMaintenance.getJul());
				budgetMaintenanceDto.setAug(budgetMaintenance.getAug());
				budgetMaintenanceDto.setSep(budgetMaintenance.getSep());
				budgetMaintenanceDto.setOct(budgetMaintenance.getOct());
				budgetMaintenanceDto.setNov(budgetMaintenance.getNov());
				budgetMaintenanceDto.setDec(budgetMaintenance.getDec());
				budgetMaintenanceDto.setJan(budgetMaintenance.getJan());
				budgetMaintenanceDto.setFeb(budgetMaintenance.getFeb());
				budgetMaintenanceDto.setMar(budgetMaintenance.getMar());
				budgetMaintenanceDto.setBudgetCategory(budgetMaintenance.getBudgetCategory());
				budgetMaintenanceDto.setBudgetType(budgetMaintenance.getBudgetType());
				budgetMaintenanceDto.setCostName(budgetMaintenance.getCostName());
				budgetMaintenanceDto.setId(budgetMaintenance.getId());
				budgetMaintenanceDto.setPlantId(plant);
				budgetMaintenanceDto.setPlantName(budgetMaintenance.getPlantName());
				budgetMaintenanceDto.setRemark(budgetMaintenance.getRemark());
				budgetMaintenanceDto.setIsEditable(budgetMaintenance.getIsEditable());
				budgetMaintenanceDtoList.add(budgetMaintenanceDto);
			}
		}catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to get data", ex);
		}
		
		aopMessageVM.setCode(200);
		aopMessageVM.setData(budgetMaintenanceDtoList);
		aopMessageVM.setMessage("Data fetched successfully");
		return aopMessageVM;
	}

	@Override
	public AOPMessageVM updateBudgetMaintenance(List<BudgetMaintenanceDto> budgetMaintenanceDtos) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<BudgetMaintenanceDto> failedList= new ArrayList<BudgetMaintenanceDto>();
		List<BudgetMaintenance> budgetMaintenanceList=new ArrayList<BudgetMaintenance>();
		try {
			for(BudgetMaintenanceDto budgetMaintenanceDto:budgetMaintenanceDtos) {
				BudgetMaintenance budgetMaintenance=null;
				if(budgetMaintenanceDto.getId()==null) {
					budgetMaintenance=new BudgetMaintenance();
					budgetMaintenanceList.add(saveData(budgetMaintenance,budgetMaintenanceDto));
				}else {
					Optional<BudgetMaintenance> budgetMaintenanceOpt=budgetMaintenanceRepository.findById(budgetMaintenanceDto.getId());
					if(budgetMaintenanceOpt.isPresent()) {
						budgetMaintenance=budgetMaintenanceOpt.get();
						budgetMaintenanceList.add(saveData(budgetMaintenance,budgetMaintenanceDto));
					}else {
						failedList.add(budgetMaintenanceDto);
					}
				}
					
			}
		}catch(Exception e) {
			throw new RuntimeException("Failed to update data", e);
		}
		Map<String,Object> map=new HashMap<>();
		map.put("Success", budgetMaintenanceList);
		map.put("Failed", failedList);
		aopMessageVM.setCode(200);
		aopMessageVM.setData(map);
		aopMessageVM.setMessage("Data updated successfully");
		// TODO Auto-generated method stub
		return aopMessageVM;
	}
	
	public BudgetMaintenance saveData(BudgetMaintenance budgetMaintenance,BudgetMaintenanceDto budgetMaintenanceDto) {
		budgetMaintenance.setApr(budgetMaintenanceDto.getApr());
		budgetMaintenance.setMay(budgetMaintenanceDto.getMay());
		budgetMaintenance.setJun(budgetMaintenanceDto.getJun());
		budgetMaintenance.setJul(budgetMaintenanceDto.getJul());
		budgetMaintenance.setAug(budgetMaintenanceDto.getAug());
		budgetMaintenance.setSep(budgetMaintenanceDto.getSep());
		budgetMaintenance.setOct(budgetMaintenanceDto.getOct());
		budgetMaintenance.setNov(budgetMaintenanceDto.getNov());
		budgetMaintenance.setDec(budgetMaintenanceDto.getDec());
		budgetMaintenance.setJan(budgetMaintenanceDto.getJan());
		budgetMaintenance.setFeb(budgetMaintenanceDto.getFeb());
		budgetMaintenance.setMar(budgetMaintenanceDto.getMar());
		budgetMaintenance.setBudgetCategory(budgetMaintenanceDto.getBudgetCategory());
		budgetMaintenance.setBudgetType(budgetMaintenanceDto.getBudgetType());
		budgetMaintenance.setCostName(budgetMaintenanceDto.getCostName());
		budgetMaintenance.setPlantId(budgetMaintenanceDto.getPlantId());
		budgetMaintenance.setPlantName(budgetMaintenanceDto.getPlantName());
		budgetMaintenance.setAopYear(budgetMaintenanceDto.getAopYear());
		budgetMaintenance.setRemark(budgetMaintenanceDto.getRemark());
		budgetMaintenance.setModifiedOn(new Date());
		budgetMaintenance.setUpdatedBy(Utility.getUserName());
		return budgetMaintenanceRepository.save(budgetMaintenance);
	}

	@Override
	public AOPMessageVM getMacroData(Double value, String year,String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		
		Map<String,Object> map=new HashMap<String,Object>();
		try {
			Double obj=getData( value,  year, plantId);
				map.put("macroValue",obj);
		}catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setData(map);
		aopMessageVM.setMessage(plantId);
		// TODO Auto-generated method stub
		return aopMessageVM;
	}
	
	public Double getData(Double value, String aopYear, String plantId) {
	    try {
	    	String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
	        
	        String storedProcedure = "MacroTest";
	        if (!"MEG".equalsIgnoreCase(verticalName)) {
	            storedProcedure = verticalName + "_" + site.getName() + "_MacroTest";
	        }
	        
	        String sql = "EXEC " + storedProcedure
	                     + " @value = :value, @aopYear = :aopYear";
	        
	        Query query = entityManager.createNativeQuery(sql);
	        query.setParameter("value", value);
	        query.setParameter("aopYear", aopYear);
	        
	        Object singleResult = query.getSingleResult();  // expect exactly one result
	        
	        if (singleResult == null) {
	            return null;
	        }
	        
	        // Depending on what your database/stored proc returns, it may be a BigDecimal, Double, Number etc.
	        if (singleResult instanceof Number) {
	            return ((Number) singleResult).doubleValue();
	        } else {
	            // Unexpected type; try converting
	            return Double.parseDouble(singleResult.toString());
	        }
	        
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}

	
	
	
	

}
