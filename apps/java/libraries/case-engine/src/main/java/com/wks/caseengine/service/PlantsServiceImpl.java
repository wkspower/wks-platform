package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.PlantsDTO;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.repository.PlantsRepository;

@Service
public class PlantsServiceImpl implements PlantsService{
	
	@Autowired
	private PlantsRepository plantsRepository;


	@Override
	public List<PlantsDTO> getAllPlants() {
		List<PlantsDTO> plantsDTOList=new ArrayList<>();
		List<Plants> plantsList = plantsRepository.findAll();
		for(Plants Plants:plantsList) {
			PlantsDTO plantsDTO=new PlantsDTO();
			plantsDTO.setDisplayName(Plants.getDisplayName());
			plantsDTO.setDisplayOrder(Plants.getDisplayOrder());
			plantsDTO.setId(Plants.getId());
			plantsDTO.setIsActive(Plants.getIsActive());
			plantsDTO.setName(Plants.getName());
			plantsDTO.setSiteFkId(Plants.getSiteFkId());
			plantsDTO.setVerticalFKId(Plants.getVerticalFKId());
			plantsDTOList.add(plantsDTO);
		}
		// TODO Auto-generated method stub
		return plantsDTOList;
	}

}
