package com.wks.caseengine.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.PIOImpactDTO;
import com.wks.caseengine.entity.PIOImpact;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PIOImpactRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

@Service
public class PIOImpactServiceImpl implements PIOImpactService {
	
	@Autowired
	private PIOImpactRepository pioImpactRepository;
	
	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;
	
	@Autowired
	private PlantsRepository plantsRepository;

	@Override
	public AOPMessageVM getPIOImpact(String year, String plantId) {
		
		List<PIOImpactDTO> pioImpactDTOs=new ArrayList<PIOImpactDTO>();
		List<PIOImpact> pioImpacts=	pioImpactRepository.findByPlantIdAndAopYear(UUID.fromString(plantId), year);
		try {
			for(PIOImpact pioImpact:pioImpacts) {
				PIOImpactDTO pioImpactDTO = new PIOImpactDTO();
				pioImpactDTO.setDescription(pioImpact.getDescription());
				pioImpactDTO.setEndMonth(pioImpact.getEndMonth());
				pioImpactDTO.setId(pioImpact.getId().toString());
				if(pioImpact.getRemarks()!=null) {
					pioImpactDTO.setRemarks(pioImpact.getRemarks());
				}else {
					pioImpactDTO.setRemarks("");
				}
				pioImpactDTO.setStartMonth(pioImpact.getStartMonth());
				pioImpactDTO.setValue(pioImpact.getValue());
				pioImpactDTOs.add(pioImpactDTO);
			}
		}catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
		
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(pioImpactDTOs);
		aopMessageVM.setMessage("Data Fetched successfully");
		return aopMessageVM;
	}

	@Override
	public AOPMessageVM updatePIOImpact(String year, String plantId, List<PIOImpactDTO> pioImpactDTOs) {
		List<PIOImpact> pioImpacts = new ArrayList<PIOImpact>();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		Sites site = siteRepository.findById(plant.getSiteFkId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
		try {
			for(PIOImpactDTO pioImpactDTO :pioImpactDTOs) {
				PIOImpact pioImpact = null;
				if(pioImpactDTO.getId()!=null) {
					Optional<PIOImpact> pioImpactOpt=	pioImpactRepository.findById(UUID.fromString(pioImpactDTO.getId()));
					if(pioImpactOpt.isPresent()) {
						pioImpact=pioImpactOpt.get();
					}
				}else {
					pioImpact = new PIOImpact();
				}
				pioImpact.setDescription(pioImpactDTO.getDescription());
				pioImpact.setEndMonth(pioImpactDTO.getEndMonth());
				pioImpact.setRemarks(pioImpactDTO.getRemarks());
				pioImpact.setStartMonth(pioImpactDTO.getStartMonth());
				pioImpact.setValue(pioImpactDTO.getValue());
				pioImpact.setPlantId(UUID.fromString(plantId));
				pioImpact.setAopYear(year);
				pioImpact.setSiteId(site.getId());
				pioImpact.setVerticalId(vertical.getId());
				pioImpacts.add(pioImpactRepository.save(pioImpact));
			}
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to update data", e);
		}
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(pioImpacts);
		aopMessageVM.setMessage("Data updated successfully");
		// TODO Auto-generated method stub
		return aopMessageVM;
	}

	@Override
	public AOPMessageVM deletePIOImpact(UUID id) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Optional<PIOImpact> pioImpactDTO=pioImpactRepository.findById(id);
			if(pioImpactDTO.isPresent()) {
				pioImpactRepository.delete(pioImpactDTO.get());
			}
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to delete data", e);
		}
		
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Deleted successfully");
		// TODO Auto-generated method stub
		return aopMessageVM;
	}

			
}
