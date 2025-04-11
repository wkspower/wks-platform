package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.VerticalScreenMappingDTO;
import com.wks.caseengine.entity.VerticalScreenMapping;
import com.wks.caseengine.repository.ScreenMappingRepository;

@Service
public class ScreenMappingServiceImpl implements ScreenMappingService {

	@Autowired
	private ScreenMappingRepository screenMappingRepository;

	@Override
	public List<VerticalScreenMappingDTO> getScreenMapping(String verticalId) {

		List<VerticalScreenMappingDTO> verticalScreenMappingDTOList = new ArrayList<>();
		try {
			List<VerticalScreenMapping> list = screenMappingRepository
					.findAllByVerticalIdOrderBySequence(UUID.fromString(verticalId));
			for (VerticalScreenMapping verticalScreenMapping : list) {
				VerticalScreenMappingDTO verticalScreenMappingDTO = new VerticalScreenMappingDTO();
				verticalScreenMappingDTO.setScreenDisplayName(verticalScreenMapping.getScreenDisplayName());
				verticalScreenMappingDTO.setScreenCode(verticalScreenMapping.getScreenCode());
				verticalScreenMappingDTOList.add(verticalScreenMappingDTO);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return verticalScreenMappingDTOList;
	}

}
