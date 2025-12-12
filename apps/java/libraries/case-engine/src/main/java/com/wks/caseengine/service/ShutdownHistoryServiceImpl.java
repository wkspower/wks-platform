package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.dto.ShutdownHistoryConfigDTO;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ShutdownHistoryConfig;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.ShutdownHistoryConfigRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class ShutdownHistoryServiceImpl implements ShutdownHistoryService{
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private PlantsRepository plantsRepository;
	
	@Autowired
	private SiteRepository siteRepository;
	
	@Autowired
	private NormParametersRepository normParametersRepository;
	
	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private ShutdownHistoryConfigRepository shutdownHistoryConfigRepository;

	@Override
	public AOPMessageVM getShutdownHistory(String plantId, String year) {
		List<ShutdownHistoryConfigDTO> shutdownHistoryConfigDTOs=new ArrayList<ShutdownHistoryConfigDTO>();
		try {
			List<ShutdownHistoryConfig> shutdownHistoryConfigList=shutdownHistoryConfigRepository.findByAopYear(year);
			for(ShutdownHistoryConfig shutdownHistoryConfig:shutdownHistoryConfigList) {
				ShutdownHistoryConfigDTO shutdownHistoryConfigDTO= new ShutdownHistoryConfigDTO();
				shutdownHistoryConfigDTO.setId(shutdownHistoryConfig.getId());
				shutdownHistoryConfigDTO.setMonth(shutdownHistoryConfig.getMonth());
				shutdownHistoryConfigDTO.setRemark(shutdownHistoryConfig.getRemark());
				shutdownHistoryConfigDTO.setAopYear(shutdownHistoryConfig.getAopYear());
				shutdownHistoryConfigDTO.setYear(shutdownHistoryConfig.getYear());
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
	
}
