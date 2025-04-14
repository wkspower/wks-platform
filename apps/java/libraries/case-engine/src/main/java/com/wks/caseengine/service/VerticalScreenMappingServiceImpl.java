package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.VerticalScreenMappingDTO;
import com.wks.caseengine.entity.GroupMaster;
import com.wks.caseengine.entity.VerticalScreenMapping;
import com.wks.caseengine.repository.GroupMasterRepository;
import com.wks.caseengine.repository.VerticalScreenMappingRepository;

@Service
public class VerticalScreenMappingServiceImpl implements VerticalScreenMappingService {

	@Autowired
	private VerticalScreenMappingRepository verticalScreenMappingRepository;

	@Autowired
	private GroupMasterRepository groupMasterRepository;
	
	@Override
	public Map<String, Object> getVerticalScreenMapping(String verticalId) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();

		List<VerticalScreenMappingDTO> verticalScreenMappingDTOList = new ArrayList<>();
		try {
			List<VerticalScreenMapping> list = verticalScreenMappingRepository
				    .findAllByVerticalFKIdOrderBySequence(UUID.fromString(verticalId));

				// Extract all unique group IDs to fetch in batch
				Set<UUID> groupIds = list.stream()
				    .map(VerticalScreenMapping::getGroupId)
				    .filter(Objects::nonNull)
				    .collect(Collectors.toSet());

				// Fetch all groups in one query
				Map<UUID, GroupMaster> groupMap = groupMasterRepository.findAllById(groupIds)
				    .stream()
				    .collect(Collectors.toMap(GroupMaster::getId, Function.identity()));

				verticalScreenMappingDTOList = list.stream()
				    .map(mapping -> {
				        VerticalScreenMappingDTO dto = new VerticalScreenMappingDTO();
				        dto.setScreenDisplayName(mapping.getScreenDisplayName());
				        dto.setScreenCode(mapping.getScreenCode());
				        dto.setType(mapping.getType());
				        dto.setRoute(mapping.getRoute());
				        dto.setIcon(mapping.getIcon());
				        dto.setBreadCrumbs(mapping.getBreadCrumbs());

				        if (mapping.getGroupId() != null) {
				            dto.setGroup(groupMap.get(mapping.getGroupId()));
				        }

				        return dto;
				    })
				    .collect(Collectors.toList());
				
				result.put("status", 200);
				result.put("message", "Screens list by verticalId " + verticalId + ".");
				result.put("data", verticalScreenMappingDTOList);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new Exception("Failed to fetch screens by vertical: " + ex.getMessage(), ex);
		}
		return result;
	}

}
