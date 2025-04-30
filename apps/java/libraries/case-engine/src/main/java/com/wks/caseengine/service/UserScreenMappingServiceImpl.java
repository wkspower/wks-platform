package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

import com.wks.caseengine.entity.GroupMaster;
import com.wks.caseengine.entity.VerticalScreenMapping;
import com.wks.caseengine.repository.GroupMasterRepository;
import com.wks.caseengine.repository.UserScreenMappingRepository;
import com.wks.caseengine.repository.VerticalScreenMappingRepository;

@Service
public class UserScreenMappingServiceImpl implements UserScreenMappingService {

	@Autowired
	private UserScreenMappingRepository userScreenMappingRepository;
	
	@Autowired
	private VerticalScreenMappingRepository verticalScreenMappingRepository;
	
	@Autowired
	private GroupMasterRepository groupMasterRepository;

	
	@Override
	public Map<String, Object> getUserScreenMapping(String verticalId, String plantId, String userId) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();

		List<String> userScreens = userScreenMappingRepository.findByVerticalFKIdAndPlantFKIdandUserId(verticalId, plantId, userId);
		
	    Map<String, Object> verticalData = new HashMap<>();
	    List<Map<String, Object>> children = new ArrayList<>();

	    try {
	    	List<VerticalScreenMapping> screenMappingsWithDuplicates = verticalScreenMappingRepository.findByScreenDisplayNameInOrderBySequence(userScreens);
	    	Map<String, VerticalScreenMapping> uniqueScreenMappingMap = new HashMap<>();

	    	for (VerticalScreenMapping mapping : screenMappingsWithDuplicates) {
	    	    uniqueScreenMappingMap.putIfAbsent(mapping.getScreenCode(), mapping);
	    	}

	    	List<VerticalScreenMapping> uniqueScreenMappings = new ArrayList<>(uniqueScreenMappingMap.values());
	    	uniqueScreenMappings.sort(Comparator.comparing(VerticalScreenMapping::getSequence, Comparator.nullsLast(Integer::compareTo)));


	        // Extract all unique group IDs to fetch in batch
	        Set<UUID> groupIds = uniqueScreenMappings.stream()
	                .map(VerticalScreenMapping::getGroupId)
	                .filter(Objects::nonNull)
	                .collect(Collectors.toSet());

	        // Fetch all groups in one query
	        Map<UUID, GroupMaster> groupMap = groupMasterRepository.findAllById(groupIds)
	                .stream()
	                .collect(Collectors.toMap(GroupMaster::getId, Function.identity()));

	        // Assuming you have a way to fetch the VerticalMaster based on verticalId
	        // Replace this with your actual logic to get the VerticalMaster
	        // For now, let's create a placeholder
	        // VerticalMaster verticalMaster = verticalMasterRepository.findById(UUID.fromString(verticalId)).orElse(null);
	        String verticalCode = "utilities"; // Placeholder - replace with actual vertical code
	        String verticalTitle = "";        // Placeholder - replace with actual vertical title

	        verticalData.put("id", verticalCode.toLowerCase().replace(" ", "-")); // Example: "Production Norms Plan" -> "production-norms-plan"
	        verticalData.put("title", verticalTitle);
	        verticalData.put("type", "group");
	        verticalData.put("children", children);

	        Map<UUID, Map<String, Object>> groupWiseScreens = new HashMap<>();

	        uniqueScreenMappings.forEach(mapping -> {
	            Map<String, Object> screenItem = new HashMap<>();
	            screenItem.put("id", mapping.getScreenCode());
	            screenItem.put("title", mapping.getScreenDisplayName());
	            screenItem.put("type", "item");
	            screenItem.put("url", mapping.getRoute());
	            screenItem.put("icon", mapping.getIcon());
	            screenItem.put("breadcrumbs", mapping.getBreadCrumbs());

	            if (mapping.getGroupId() != null) {
	                GroupMaster group = groupMap.get(mapping.getGroupId());
	                if (group != null) {
	                    UUID groupId = group.getId();
	                    if (!groupWiseScreens.containsKey(groupId)) {
	                        Map<String, Object> groupData = new HashMap<>();
	                        groupData.put("id", group.getGroupCode());
	                        groupData.put("title", group.getGroupName());
	                        groupData.put("type", "collapse");
	                        groupData.put("icon", group.getIcon()); // Assuming GroupMaster has an icon field
	                        groupData.put("children", new ArrayList<>());
	                        groupWiseScreens.put(groupId, groupData);
	                        children.add(groupData);
	                    }
	                    ((List<Map<String, Object>>) groupWiseScreens.get(groupId).get("children")).add(screenItem);
	                } else {
	                    children.add(screenItem); // If no group, add directly to the vertical's children
	                }
	            } else {
	                children.add(screenItem); // If no group ID, add directly to the vertical's children
	            }
	        });

	        result.put("status", 200);
	        result.put("message", "Screens list by verticalId " + verticalId + ".");
	        result.put("data", Arrays.asList(verticalData)); // Wrap the verticalData in a list to match the outer structure
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new Exception("Failed to fetch screens by vertical: " + ex.getMessage(), ex);
	    }
	    return result;

	}

}
