package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
	        List<VerticalScreenMapping> screenMappingsWithDuplicates =
	                verticalScreenMappingRepository.findByScreenDisplayNameInOrderBySequence(userScreens);

	        // Step 1: Remove duplicates by screenCode
	        Map<String, VerticalScreenMapping> screenCodeMap = new LinkedHashMap<>();
	        for (VerticalScreenMapping mapping : screenMappingsWithDuplicates) {
	            screenCodeMap.putIfAbsent(mapping.getScreenCode(), mapping);
	        }
	        List<VerticalScreenMapping> uniqueByScreenCode = new ArrayList<>(screenCodeMap.values());

	        // Step 2: Sort by sequence
	        uniqueByScreenCode.sort(Comparator.comparing(VerticalScreenMapping::getSequence, Comparator.nullsLast(Integer::compareTo)));

	        // Step 3: Enforce uniqueness again by URL after sorting
	        Map<String, VerticalScreenMapping> urlMap = new LinkedHashMap<>();
	        for (VerticalScreenMapping mapping : uniqueByScreenCode) {
	            String url = mapping.getRoute();
	            if (!urlMap.containsKey(url)) {
	                urlMap.put(url, mapping);
	            }
	        }
	        List<VerticalScreenMapping> finalResult = new ArrayList<>(urlMap.values());


	        // Extract all unique group IDs to fetch in batch
	        Set<UUID> groupIds = finalResult.stream()
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

	        Map<UUID, Map<String, Object>> groupWiseScreens = new HashMap<>();

	        finalResult.forEach(mapping -> {
	            if ("collapse".equals(mapping.getType())) {
	                // Treat as a parent group directly under the vertical
	                Map<String, Object> parentGroup = new HashMap<>();
	                parentGroup.put("id", mapping.getScreenCode()); // Assuming ScreenCode can act as a unique ID
	                parentGroup.put("title", mapping.getScreenDisplayName());
	                parentGroup.put("type",mapping.getType());
	                parentGroup.put("icon", mapping.getIcon());
					  parentGroup.put("url", mapping.getRoute());
					    parentGroup.put("breadcrumbs", mapping.getBreadCrumbs());
	                // parentGroup.put("children", new ArrayList<>());
	                children.add(parentGroup); // Add directly to the vertical's children
	            } else {
	                // Treat as a regular screen item
	                Map<String, Object> screenItem = new HashMap<>();
	                screenItem.put("id", mapping.getScreenCode());
	                screenItem.put("title", mapping.getScreenDisplayName());
	                screenItem.put("type", mapping.getType());
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
	                            groupData.put("icon", group.getIcon());
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
	            }
	        });
	        if (!children.isEmpty()) {
	            verticalData.put("children", children);
	        }

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
