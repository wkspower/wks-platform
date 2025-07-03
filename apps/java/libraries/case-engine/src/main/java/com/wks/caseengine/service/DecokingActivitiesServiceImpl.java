package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.DecokePlanningIBRDTO;
import com.wks.caseengine.dto.DecokeRunLengthDTO;
import com.wks.caseengine.dto.DecokingActivitiesDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.DecokeRunLength;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.DecokeRunLengthRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class DecokingActivitiesServiceImpl implements DecokingActivitiesService {

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;

	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private DecokeRunLengthRepository decokeRunLengthRepository;

	@Override
	public AOPMessageVM getDecokingActivitiesData(String year, String plantId, String reportType) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> decokingActivitiesList = new ArrayList<>();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			String procedureName=null;
			List<Object[]> results=null;
			if(reportType.equalsIgnoreCase("RunningDuration")) {
				 procedureName = "vwScrn"+vertical.getName() + "_" + site.getName() + "_DecokingPlanning";
				 results = getData(plantId, procedureName);
			}else if(reportType.equalsIgnoreCase("ibr")){
				procedureName = "vwScrn"+vertical.getName() + "_" + site.getName() + "_DecokePlanningDates";
				 results = getIBRData(plantId, procedureName);
			}else if(reportType.equalsIgnoreCase("RunLength")){
				procedureName = "vwScrn"+vertical.getName() + "_" + site.getName() + "_Decoke_RunLength";
				 results = getRunLengthData(plantId,year, procedureName);
			}

			for (Object[] row : results) {
				Map<String, Object> map = new HashMap<>(); // Create a new map for each row
				if (reportType.equalsIgnoreCase("RunningDuration")) {
					map.put("normParameterId", row[0]);
					map.put("name", row[1]);
					map.put("displayName", row[2]);
					map.put("isEditable", row[13]);
					map.put("isMonthAdd", row[16]);
					Object raw = row[0];
					UUID id=UUID.fromString(raw.toString());
					Optional<NormAttributeTransactions> normAttributeTransactionsopt=normAttributeTransactionsRepository.findByNormParameterFKId(id);
					if(normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions=normAttributeTransactionsopt.get();
						map.put("attributeValue", normAttributeTransactions.getAttributeValue());
						map.put("remarks", normAttributeTransactions.getRemarks());
						map.put("id", normAttributeTransactions.getId());
						map.put("month", getMonth(normAttributeTransactions.getAopMonth()));
					}else {
						map.put("remarks", "");
						map.put("id", "");
					}
				}
				else if(reportType.equalsIgnoreCase("ibr")) {
					map.put("furnace", row[0] != null ? row[0] : "");
					map.put("plantId", row[1] != null ? row[1] : "");
					map.put("ibrSDId", row[2] != null ? row[2] : "");
					map.put("ibrEDId", row[3] != null ? row[3] : "");
					map.put("taSDId",  row[4] != null ? row[4] : "");
					map.put("taEDId",  row[5] != null ? row[5] : "");
					map.put("sdSDId",  row[6] != null ? row[6] : "");
					map.put("sdEDId",  row[7] != null ? row[7] : "");
					map.put("ibrSD",   row[8] != null ? row[8] : "");
					map.put("ibrED",   row[9] != null ? row[9] : "");
					map.put("taSD",    row[10] != null ? row[10] : "");
					map.put("taED",    row[11] != null ? row[11] : "");
					map.put("sdSD",    row[12] != null ? row[12] : "");
					map.put("sdED",    row[13] != null ? row[13] : "");
					map.put("remarks", "");
				}
				else if(reportType.equalsIgnoreCase("activity")) {
					map.put("furnace", row[0]);
					map.put("startDateIBR", row[1]);
					map.put("endDateIBR", row[2]);
					map.put("startDateSD", row[3]);
					map.put("endDateSD", row[4]);
					map.put("startDateTA", row[5]);
					map.put("endDateTA", row[6]);
					map.put("remarks", row[7]);
				}
				else if(reportType.equalsIgnoreCase("RunLength")) {
					map.put("id", row[0]!= null ? row[0] : "");
					map.put("date", row[1]!= null ? row[1] : "");
					map.put("month", row[2]!= null ? row[2] : "");
					map.put("hTenActual", row[3]!= null ? row[3] : "");
					map.put("hTenProposed", row[4]!= null ? row[4] : "");
					map.put("hElevenActual", row[5]!= null ? row[5] : "");
					map.put("hElevenProposed", row[6]!= null ? row[6] : "");
					map.put("hTwelveActual", row[7]!= null ? row[7] : "");
					map.put("hTwelveProposed", row[8]!= null ? row[8] : "");
					map.put("hThirteenActual", row[9]!= null ? row[9] : "");
					map.put("hThirteenProposed", row[10]!= null ? row[10] : "");
					map.put("hFourteenActual", row[11]!= null ? row[11] : "");
					map.put("hFourteenProposed", row[12]!= null ? row[12] : "");
					map.put("demo", row[13]!= null ? row[13] : "");
					map.put("aopYear", row[14]!= null ? row[14] : "");
					map.put("plantId", row[15]!= null ? row[15] : "");
					map.put("remark", row[16]!= null ? row[16] : "");
				}
				decokingActivitiesList.add(map); // Add the map to the list here
			}
			Map<String, Object> aopCalculationMap = new HashMap<>();
			if(reportType.equalsIgnoreCase("RunLength")) {
				List<AopCalculation> aopCalculation = aopCalculationRepository.findByPlantIdAndAopYearAndCalculationScreen(
						UUID.fromString(plantId), year, "Furnace-run-length");
				
				aopCalculationMap.put("aopCalculation", aopCalculation);
				aopCalculationMap.put("decokingActivitiesList", decokingActivitiesList);
				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("Data fetched successfully");
				aopMessageVM.setData(aopCalculationMap);
				return aopMessageVM;
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(decokingActivitiesList);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> getData(String plantId, String aopYear, String reportType, String procedureName) {
		try {
			
			String sql = "EXEC " + procedureName
					+ " @PlantFKId = :plantId, @AuditYear = :aopYear, @ConfigTypeName = :reportType";

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
	
	public List<Object[]> getData(String plantId, String viewName) {
	    try {
	        
	        // 2. Construct SQL with dynamic view name
	        String sql = 
	            "SELECT * FROM " + viewName + 
	            " WHERE Plant_FK_Id = :plantId";

	        // 3. Create and parameterize the native query
	        Query query = entityManager.createNativeQuery(sql);
	        query.setParameter("plantId", plantId);

	        // 4. Execute
	        return query.getResultList();

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid argument: " + e.getMessage(), e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data from view " + viewName, ex);
	    }
	}
	
	public List<Object[]> getRunLengthData(String plantId,String aopYear, String viewName) {
	    try {
	        
	        // 2. Construct SQL with dynamic view name
	        String sql = 
	            "SELECT * FROM " + viewName + 
	            " WHERE Plant_FK_Id = :plantId AND AOPYear = :aopYear";

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


	public List<Object[]> getIBRData(String plantId, String viewName) {
	    try {
	        
	        // 2. Construct SQL with dynamic view name
	        String sql = 
	            "SELECT * FROM " + viewName + 
	            " WHERE PlantId = :plantId";

	        // 3. Create and parameterize the native query
	        Query query = entityManager.createNativeQuery(sql);
	        query.setParameter("plantId", plantId);

	        // 4. Execute
	        return query.getResultList();

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid argument: " + e.getMessage(), e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data from view " + viewName, ex);
	    }
	}

	public static String getMonth(Integer month) {
	    if (month == null) {
	        return "Invalid month";
	    }
	    switch (month) {
	        case 1:  return "January";
	        case 2:  return "February";
	        case 3:  return "March";
	        case 4:  return "April";
	        case 5:  return "May";
	        case 6:  return "June";
	        case 7:  return "July";
	        case 8:  return "August";
	        case 9:  return "September";
	        case 10: return "October";
	        case 11: return "November";
	        case 12: return "December";
	        default: return "Invalid month";
	    }
	}

	@Override
	public AOPMessageVM updateDecokingActivitiesData(String year, String plantId, String reportType,
			List<DecokingActivitiesDTO> decokingActivitiesDTOList) {
		List<NormAttributeTransactions> normAttributeTransactionsList=new ArrayList<>();
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			for(DecokingActivitiesDTO decokingActivitiesDTO:decokingActivitiesDTOList) {
				if(decokingActivitiesDTO.getId()!=null) {
					Optional<NormAttributeTransactions> normAttributeTransactionsopt=normAttributeTransactionsRepository.findById(UUID.fromString(decokingActivitiesDTO.getId()));
					if(normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions=normAttributeTransactionsopt.get();
						normAttributeTransactions.setAttributeValue(decokingActivitiesDTO.getDays());
						if(decokingActivitiesDTO.getAopMonth()!=null) {
							normAttributeTransactions.setAopMonth(decokingActivitiesDTO.getAopMonth());
						}else {
							normAttributeTransactions.setAopMonth(0);
						}
						
						normAttributeTransactions.setRemarks(decokingActivitiesDTO.getRemarks());
						normAttributeTransactionsList.add(normAttributeTransactionsRepository.save(normAttributeTransactions));
					}
				}else {
					NormAttributeTransactions normAttributeTransactions = new NormAttributeTransactions();
					normAttributeTransactions.setAttributeValue(decokingActivitiesDTO.getDays());
					if(decokingActivitiesDTO.getAopMonth()!=null) {
						normAttributeTransactions.setAopMonth(decokingActivitiesDTO.getAopMonth());
					}else {
						normAttributeTransactions.setAopMonth(0);
					}
					normAttributeTransactions.setRemarks(decokingActivitiesDTO.getRemarks());
					normAttributeTransactions.setAuditYear(year);
					normAttributeTransactions.setCreatedOn(new Date());
					normAttributeTransactions.setAttributeValueVersion("V1");
					normAttributeTransactions.setNormParameterFKId(UUID.fromString(decokingActivitiesDTO.getNormParameterId()));
					normAttributeTransactions.setUserName("System");
					normAttributeTransactionsList.add(normAttributeTransactionsRepository.save(normAttributeTransactions));
				}
			}
		} catch (Exception ex) {
	        throw new RuntimeException("Failed to update data");
	    }
		
		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("ibr");
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
		aopMessageVM.setMessage("Data Updated successfully");
		aopMessageVM.setData(normAttributeTransactionsList);
		return aopMessageVM;
	}

	@Override
	public AOPMessageVM updateDecokingActivitiesIBRData(String year, String plantId, String reportType,
			List<DecokePlanningIBRDTO> decokePlanningIBRDTOList) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			for(DecokePlanningIBRDTO decokePlanningIBRDTO:decokePlanningIBRDTOList) {
				if(decokePlanningIBRDTO.getIbrEDId()!=null && decokePlanningIBRDTO.getIbrEDId()!="") {
					Optional<NormAttributeTransactions> normAttributeTransactionsopt=normAttributeTransactionsRepository.findByNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getIbrEDId()));
					if(normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions=normAttributeTransactionsopt.get();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getIbrED());
						normAttributeTransactions.setModifiedOn(new Date());
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}else {
						NormAttributeTransactions normAttributeTransactions=new NormAttributeTransactions();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getIbrED());
						normAttributeTransactions.setAttributeValueVersion("V1");
						normAttributeTransactions.setAuditYear(year);
						normAttributeTransactions.setCreatedOn(new Date());
						normAttributeTransactions.setNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getIbrEDId()));
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactions.setUserName("System");
						normAttributeTransactions.setAopMonth(0);
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}
				}
				if(decokePlanningIBRDTO.getIbrSDId()!=null && decokePlanningIBRDTO.getIbrSDId()!="") {
					Optional<NormAttributeTransactions> normAttributeTransactionsopt=normAttributeTransactionsRepository.findByNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getIbrSDId()));
					if(normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions=normAttributeTransactionsopt.get();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getIbrSD());
						normAttributeTransactions.setModifiedOn(new Date());
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}else {
						NormAttributeTransactions normAttributeTransactions=new NormAttributeTransactions();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getIbrSD());
						normAttributeTransactions.setAttributeValueVersion("V1");
						normAttributeTransactions.setAuditYear(year);
						normAttributeTransactions.setCreatedOn(new Date());
						normAttributeTransactions.setNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getIbrSDId()));
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactions.setUserName("System");
						normAttributeTransactions.setAopMonth(0);
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}
				}
				if(decokePlanningIBRDTO.getTaSDId()!=null && decokePlanningIBRDTO.getTaSDId()!="") {
					Optional<NormAttributeTransactions> normAttributeTransactionsopt=normAttributeTransactionsRepository.findByNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getTaSDId()));
					if(normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions=normAttributeTransactionsopt.get();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getTaSD());
						normAttributeTransactions.setModifiedOn(new Date());
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}else {
						NormAttributeTransactions normAttributeTransactions=new NormAttributeTransactions();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getTaSD());
						normAttributeTransactions.setAttributeValueVersion("V1");
						normAttributeTransactions.setAuditYear(year);
						normAttributeTransactions.setCreatedOn(new Date());
						normAttributeTransactions.setNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getTaSDId()));
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactions.setUserName("System");
						normAttributeTransactions.setAopMonth(0);
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}
				}
				if(decokePlanningIBRDTO.getTaEDId()!=null && decokePlanningIBRDTO.getTaEDId()!="") {
					Optional<NormAttributeTransactions> normAttributeTransactionsopt=normAttributeTransactionsRepository.findByNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getTaEDId()));
					if(normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions=normAttributeTransactionsopt.get();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getTaED());
						normAttributeTransactions.setModifiedOn(new Date());
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}else {
						NormAttributeTransactions normAttributeTransactions=new NormAttributeTransactions();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getTaED());
						normAttributeTransactions.setAttributeValueVersion("V1");
						normAttributeTransactions.setAuditYear(year);
						normAttributeTransactions.setCreatedOn(new Date());
						normAttributeTransactions.setNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getTaEDId()));
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactions.setUserName("System");
						normAttributeTransactions.setAopMonth(0);
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}
				}
				if(decokePlanningIBRDTO.getSdSDId()!=null && decokePlanningIBRDTO.getSdSDId()!="") {
					Optional<NormAttributeTransactions> normAttributeTransactionsopt=normAttributeTransactionsRepository.findByNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getSdSDId()));
					if(normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions=normAttributeTransactionsopt.get();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getSdSD());
						normAttributeTransactions.setModifiedOn(new Date());
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}else {
						NormAttributeTransactions normAttributeTransactions=new NormAttributeTransactions();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getSdSD());
						normAttributeTransactions.setAttributeValueVersion("V1");
						normAttributeTransactions.setAuditYear(year);
						normAttributeTransactions.setCreatedOn(new Date());
						normAttributeTransactions.setNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getSdSDId()));
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactions.setUserName("System");
						normAttributeTransactions.setAopMonth(0);
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}
				}
				if(decokePlanningIBRDTO.getSdEDId()!=null && decokePlanningIBRDTO.getSdEDId()!="") {
					Optional<NormAttributeTransactions> normAttributeTransactionsopt=normAttributeTransactionsRepository.findByNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getSdEDId()));
					if(normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions=normAttributeTransactionsopt.get();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getSdED());
						normAttributeTransactions.setModifiedOn(new Date());
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}else {
						NormAttributeTransactions normAttributeTransactions=new NormAttributeTransactions();
						normAttributeTransactions.setAttributeValue(decokePlanningIBRDTO.getSdED());
						normAttributeTransactions.setAttributeValueVersion("V1");
						normAttributeTransactions.setAuditYear(year);
						normAttributeTransactions.setCreatedOn(new Date());
						normAttributeTransactions.setNormParameterFKId(UUID.fromString(decokePlanningIBRDTO.getSdEDId()));
						normAttributeTransactions.setRemarks(decokePlanningIBRDTO.getRemarks());
						normAttributeTransactions.setUserName("System");
						normAttributeTransactions.setAopMonth(0);
						normAttributeTransactionsRepository.save(normAttributeTransactions);
					}
				}

			}
		}
		catch (Exception ex) {
	        throw new RuntimeException("Failed to update data");
	    }
		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("sd-ta-activity");
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
		aopMessageVM.setMessage("Data Updated successfully");
		aopMessageVM.setData(decokePlanningIBRDTOList);
		return aopMessageVM;
	}

	@Override
	public AOPMessageVM updateDecokingActivitiesRunLengthData(String year, String plantId, String reportType,
			List<DecokeRunLengthDTO> decokeRunLengthDTOList) {
		List<DecokeRunLength> decokeRunLengthList=new ArrayList<>();
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			for(DecokeRunLengthDTO decokeRunLengthDTO:decokeRunLengthDTOList) {
				Optional<DecokeRunLength> decokeRunLengthopt = decokeRunLengthRepository.findById(decokeRunLengthDTO.getId());
				if(decokeRunLengthopt.isPresent()) {
					DecokeRunLength decokeRunLength = decokeRunLengthopt.get();
					decokeRunLength.setH10Proposed(decokeRunLengthDTO.getHTenProposed());
					decokeRunLength.setH11Proposed(decokeRunLengthDTO.getHElevenProposed());
					decokeRunLength.setH12Proposed(decokeRunLengthDTO.getHTwelveProposed());
					decokeRunLength.setH13Proposed(decokeRunLengthDTO.getHThirteenProposed());
					decokeRunLength.setH14Proposed(decokeRunLengthDTO.getHFourteenProposed());
					decokeRunLength.setDemo(decokeRunLengthDTO.getDemo());
					decokeRunLengthList.add(decokeRunLengthRepository.save(decokeRunLength));
				}
			}
		}catch (Exception ex) {
	        throw new RuntimeException("Failed to update data");
	    }
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Data Updated successfully");
		aopMessageVM.setData(decokeRunLengthList);
		return aopMessageVM;
	}
}
