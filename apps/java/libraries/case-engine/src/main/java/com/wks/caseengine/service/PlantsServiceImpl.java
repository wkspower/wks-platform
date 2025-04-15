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
public class PlantsServiceImpl implements PlantsService {

	@Autowired
	private PlantsRepository plantsRepository;

	@Override
	public List<PlantsDTO> getAllPlants() {
		try {
			List<PlantsDTO> plantsDTOList = new ArrayList<>();
			List<Plants> plantsList = plantsRepository.findAll();
			for (Plants Plants : plantsList) {
				PlantsDTO plantsDTO = new PlantsDTO();
				plantsDTO.setDisplayName(Plants.getDisplayName());
				plantsDTO.setDisplayOrder(Plants.getDisplayOrder());
				plantsDTO.setId(Plants.getId().toString());
				plantsDTO.setIsActive(Plants.getIsActive());
				plantsDTO.setName(Plants.getName());
				plantsDTO.setSiteFkId(Plants.getSiteFkId().toString());
				plantsDTO.setVerticalFKId(Plants.getVerticalFKId().toString());
				plantsDTOList.add(plantsDTO);
			}
			// TODO Auto-generated method stub
			return plantsDTOList;
		} catch (Exception e) {
			// Log error and rethrow a runtime exception
			System.err.println("Exception occurred while fetching plant data: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Unable to fetch plant data from database", e);
		}
	}

	@Override
	public String findVerticalNameByPlantId(UUID plantId) {
		return plantsRepository.findVerticalNameByPlantId(plantId);
		// TODO Auto-generated method stub
	}

}
