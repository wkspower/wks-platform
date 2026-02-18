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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.dto.BusinessDemandDataDTO;
import com.wks.caseengine.dto.ShutdownHistoryConfigDTO;
import com.wks.caseengine.dto.SlowdownHistoryConfigDTO;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ShutdownHistoryConfig;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.SlowdownHistoryConfig;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ShutdownHistoryConfigRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.SlowdownHistoryConfigRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class ShutdownHistoryServiceImpl implements ShutdownHistoryService{
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private PlantsRepository plantsRepository;
	
	@Autowired
	private SiteRepository siteRepository;
	
	
	@Autowired
	private ShutdownHistoryConfigRepository shutdownHistoryConfigRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@Autowired
	private SlowdownHistoryConfigRepository slowdownHistoryConfigRepository;

	@Override
	public AOPMessageVM getShutdownHistory(String plantId, String year) {
		List<ShutdownHistoryConfigDTO> shutdownHistoryConfigDTOs=new ArrayList<ShutdownHistoryConfigDTO>();
		try {
			List<ShutdownHistoryConfig> shutdownHistoryConfigList=shutdownHistoryConfigRepository.findByAopYear(year,UUID.fromString(plantId));
			for(ShutdownHistoryConfig shutdownHistoryConfig:shutdownHistoryConfigList) {
				ShutdownHistoryConfigDTO shutdownHistoryConfigDTO= new ShutdownHistoryConfigDTO();
				shutdownHistoryConfigDTO.setId(shutdownHistoryConfig.getId());
				shutdownHistoryConfigDTO.setMonth(shutdownHistoryConfig.getMonth());
				shutdownHistoryConfigDTO.setRemark(shutdownHistoryConfig.getRemark());
				shutdownHistoryConfigDTO.setAopYear(shutdownHistoryConfig.getAopYear());
				shutdownHistoryConfigDTO.setYear(shutdownHistoryConfig.getYear());
				shutdownHistoryConfigDTO.setPlantId(shutdownHistoryConfig.getPlantFKId().toString());
				shutdownHistoryConfigDTO.setTypeOfSD(shutdownHistoryConfig.getTypeOfSD());
				shutdownHistoryConfigDTOs.add(shutdownHistoryConfigDTO);
			}
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(shutdownHistoryConfigDTOs);
		aopMessageVM.setMessage("Data Fetched successfully");
		return aopMessageVM;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public AOPMessageVM saveShutdownHistory(String year, String plantFKId,
			List<ShutdownHistoryConfigDTO> shutdownHistoryConfigDTOs) {
		try {
			List<ShutdownHistoryConfig> list = new ArrayList<ShutdownHistoryConfig>();
			UUID plantId = UUID.fromString(plantFKId);
			String verticalName = plantsRepository.findVerticalNameByPlantId(plantId);
			Plants plant = plantsRepository.findById(plantId).orElseThrow();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();

			for (ShutdownHistoryConfigDTO shutdownHistoryConfigDTO : shutdownHistoryConfigDTOs) {
				ShutdownHistoryConfig shutdownHistoryConfig=null;
				if(shutdownHistoryConfigDTO.getId()!=null) {
					Optional<ShutdownHistoryConfig> shutdownHistoryConfigOpt=shutdownHistoryConfigRepository.findById(shutdownHistoryConfigDTO.getId());
					if(shutdownHistoryConfigOpt.isPresent()) {
						shutdownHistoryConfig=shutdownHistoryConfigOpt.get();
						shutdownHistoryConfig.setModifiedOn(new Date());
					}
				}else {
					shutdownHistoryConfig = new ShutdownHistoryConfig();
					shutdownHistoryConfig.setCreatedOn(new Date());
				}
				shutdownHistoryConfig.setAopYear(shutdownHistoryConfigDTO.getAopYear());
				shutdownHistoryConfig.setModifiedBy(Utility.getUserName());
				shutdownHistoryConfig.setMonth(shutdownHistoryConfigDTO.getMonth());
				shutdownHistoryConfig.setRemark(shutdownHistoryConfigDTO.getRemark());
				shutdownHistoryConfig.setYear(shutdownHistoryConfigDTO.getYear());
				shutdownHistoryConfig.setPlantFKId(plantId);
				shutdownHistoryConfig.setTypeOfSD(shutdownHistoryConfigDTO.getTypeOfSD());
				list.add(shutdownHistoryConfigRepository.save(shutdownHistoryConfig));
				
			}
			
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(list);
			aopMessageVM.setMessage("Data updated successfully");
			return aopMessageVM;
		} catch (Exception ex) {
			ex.printStackTrace();
			
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	@Override
	public AOPMessageVM deleteShutdownHistory(UUID id) {
		Optional<ShutdownHistoryConfig> shutdownHistoryConfigOpt=shutdownHistoryConfigRepository.findById(id);
		if(shutdownHistoryConfigOpt.isPresent()) {
			ShutdownHistoryConfig shutdownHistoryConfig= shutdownHistoryConfigOpt.get();
			shutdownHistoryConfigRepository.delete(shutdownHistoryConfig);
		}
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(id);
		aopMessageVM.setMessage("Data deleted successfully");
		return aopMessageVM;
	}
	
	@Override
	public AOPMessageVM getTypeOfSD(String plantId, String year) {
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			String view="vwScrn"+verticalName+"TypeOfSD";
			List<Object[]> obj=getTypeOfSDData(view);
			List<Map<String,Object>> maps=new ArrayList<>();
			for (Object[] row : obj) {
				
				Map<String,Object> map=new HashMap<>();
				map.put("name", row[0] != null ? row[0].toString() : null);
				map.put("value", row[1] != null ? Integer.parseInt(row[1].toString()) : null);
				maps.add(map);
				
			}
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(maps);
			aopMessageVM.setMessage("Data fetched successfully");
			
			// TODO Auto-generated method stub
			return aopMessageVM;
		}catch (Exception ex) {
			ex.printStackTrace();
			
			throw new RuntimeException("Failed to save data", ex);
		}
		
	}

	@Override
	public AOPMessageVM getLineDetails(String plantId, String year) {
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			String view="vwScrn"+verticalName+"GetLineDetails";
			List<Object[]> obj=getLineDetailsData(view,plantId);
			List<Map<String,Object>> maps=new ArrayList<>();
			for (Object[] row : obj) {
				
				Map<String,Object> map=new HashMap<>();
				map.put("id", row[0] != null ? row[0].toString() : null);
				map.put("name", row[1] != null ? row[1].toString() : null);
				map.put("displayName", row[2] != null ? row[2].toString() : null);
				map.put("plantId", row[3] != null ? (row[3].toString()) : null);
				maps.add(map);
			}
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(maps);
			aopMessageVM.setMessage("Data fetched successfully");
			
			// TODO Auto-generated method stub
			return aopMessageVM;
		}catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to save data", ex);
		}
		
	}

	public List<Object[]> getTypeOfSDData(String viewName) {
		try {
			String sql = "SELECT * from "+ viewName;

			Query query = entityManager.createNativeQuery(sql);
			
			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public List<Object[]> getLineDetailsData(String viewName,String plantId) {
		try {
			String sql = "SELECT * from "+ viewName+" where PlantId= :plantId";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional
	@Override
	public AOPMessageVM saveSlowdownHistory(String year, String plantFKId,
			List<SlowdownHistoryConfigDTO> dtos) {

		try {
			UUID plantId = UUID.fromString(plantFKId);
			List<SlowdownHistoryConfig> list = new ArrayList<>();

			for (SlowdownHistoryConfigDTO dto : dtos) {

				SlowdownHistoryConfig entity;

				// UPDATE
				Optional<SlowdownHistoryConfig> opt = slowdownHistoryConfigRepository.findById(dto.getId());
				if (opt.isPresent()) {
					entity = opt.get();
				} else {
					entity = new SlowdownHistoryConfig();
					entity.setId(UUID.randomUUID());
				}

				entity.setDescription(dto.getDescription());
				entity.setMaintStartDateTime(dto.getMaintStartDateTime());
				entity.setMaintEndDateTime(dto.getMaintEndDateTime());
				entity.setDurationInMins(dto.getDurationInMins());
				entity.setMaintForMonth(dto.getMaintForMonth());
				entity.setAuditYear(year);
				entity.setRate(dto.getRate());
				entity.setRemarks(dto.getRemarks());
				entity.setUpdatedOn(new Date());
				entity.setUpdatedBy(Utility.getUserName());
				entity.setPlantFkId(plantId);

				list.add(slowdownHistoryConfigRepository.save(entity));
			}

			return new AOPMessageVM(200, "Saved Successfully", list);

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Save failed", e);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public AOPMessageVM getSlowdownHistory(String plantId, String year) {

		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> slowdownList = new ArrayList<>();

		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

			Sites site = siteRepository.findById(plant.getSiteFkId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));

			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

			String procedureName = vertical.getName() + "_GetSlowdownHistoryConfig";

			List<Object[]> results = getData(plantId, year, procedureName);

			for (Object[] row : results) {
				Map<String, Object> map = new HashMap<>();
				map.put("id", row[0] != null ? row[0].toString() : null);
				map.put("description", row[1] != null ? row[1].toString() : null);
				map.put("maintStartDateTime", row[2]);
				map.put("maintEndDateTime", row[3]);
				map.put("durationInMins", row[4]);
				map.put("maintForMonth", row[5]);
				map.put("auditYear", row[6]);
				map.put("rate", row[7]);
				map.put("remarks", row[8]);
				map.put("updatedOn", row[9]);
				map.put("updatedBy", row[10]);
				map.put("plantFkId", row[11] != null ? row[11].toString() : null);

				slowdownList.add(map);
			}

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(slowdownList);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getData(String plantId, String year, String procedureName) {

		String sql = "EXEC " + procedureName + " @plantId = :plantId, @aopyear = :aopyear";

		return (List<Object[]>) entityManager.createNativeQuery(sql)
				.setParameter("plantId", plantId)
				.setParameter("aopyear", year)
				.getResultList();
	}

	
	@Override
	public AOPMessageVM deleteSlowdownHistory(UUID id) {
		Optional<SlowdownHistoryConfig> slowdownHistoryConfigOpt = slowdownHistoryConfigRepository.findById(id);
		if (slowdownHistoryConfigOpt.isPresent()) {
			SlowdownHistoryConfig slowdownHistoryConfig = slowdownHistoryConfigOpt.get();
			slowdownHistoryConfigRepository.delete(slowdownHistoryConfig);
		}
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(id);
		aopMessageVM.setMessage("Data deleted successfully");
		return aopMessageVM;
	}

}
