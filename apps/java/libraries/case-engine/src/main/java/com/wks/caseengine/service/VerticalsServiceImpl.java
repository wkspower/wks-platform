package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.VerticalsDTO;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.repository.VerticalsRepository;

@Service
public class VerticalsServiceImpl implements VerticalsService{
	
	@Autowired
	private VerticalsRepository verticalsRepository;

	@Override
	public List<VerticalsDTO> getAllVerticals() {
		List<Verticals> verticalsList= verticalsRepository.findAll();
		List<VerticalsDTO> verticalsDTOList=new ArrayList<>();
		
		for(Verticals verticals:verticalsList) {
			VerticalsDTO verticalsDTO=new VerticalsDTO();
			verticalsDTO.setDisplayName(verticals.getDisplayName());
			verticalsDTO.setDisplayOrder(verticals.getDisplayOrder());
			verticalsDTO.setId(verticals.getId().toString());
			verticalsDTO.setIsActive(verticals.getIsActive());
			verticalsDTO.setName(verticals.getName());
			verticalsDTOList.add(verticalsDTO);
		}
		
		// TODO Auto-generated method stub
		return verticalsDTOList;
	}

}
