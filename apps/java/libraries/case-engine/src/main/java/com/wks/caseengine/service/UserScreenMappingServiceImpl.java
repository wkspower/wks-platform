package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
	    List<String> permissions = userScreenMappingRepository.findPermissionsByVerticalFKIdAndPlantFKIdandUserId(verticalId, plantId, userId);
	    Map<String, Object> verticalData = new HashMap<>();
	    List<Map<String, Object>> children = new ArrayList<>();

	    try {
	        List<VerticalScreenMapping> screenMappingsWithDuplicates =
	                verticalScreenMappingRepository.findByScreenDisplayNameInAndVerticalFKIdOrderBySequence(userScreens, UUID.fromString(verticalId));

	        Map<String, VerticalScreenMapping> screenCodeMap = new LinkedHashMap<>();
	        for (VerticalScreenMapping mapping : screenMappingsWithDuplicates) {
	            screenCodeMap.putIfAbsent(mapping.getScreenCode(), mapping);
	        }
	        List<VerticalScreenMapping> uniqueByScreenCode = new ArrayList<>(screenCodeMap.values());

	        uniqueByScreenCode.sort(Comparator.comparing(VerticalScreenMapping::getSequence, Comparator.nullsLast(Integer::compareTo)));

	        Map<String, VerticalScreenMapping> urlMap = new LinkedHashMap<>();
	        for (VerticalScreenMapping mapping : uniqueByScreenCode) {
	            String url = mapping.getRoute();
	            if (!urlMap.containsKey(url)) {
	                urlMap.put(url, mapping);
	            }
	        }
	        List<VerticalScreenMapping> finalResult = new ArrayList<>(urlMap.values());

	        Set<UUID> groupIds = finalResult.stream()
	                .map(VerticalScreenMapping::getGroupId)
	                .filter(Objects::nonNull)
	                .collect(Collectors.toSet());

	        List<GroupMaster> groups = groupMasterRepository.findAllByIdInOrderBySequenceAsc(groupIds);

	        Map<UUID, GroupMaster> groupMap = groups.stream()
	                .collect(Collectors.toMap(
	                        GroupMaster::getId,
	                        Function.identity(),
	                        (existing, replacement) -> existing,
	                        LinkedHashMap::new
	                ));

	        String verticalCode = "utilities"; 
	        String verticalTitle = "";        

	        verticalData.put("id", verticalCode.toLowerCase().replace(" ", "-"));
	        verticalData.put("title", verticalTitle);
	        verticalData.put("type", "group");

	        Map<UUID, Map<String, Object>> groupWiseScreens = new LinkedHashMap<>(); // Use LinkedHashMap for order
	        Map<UUID, Map<String, Object>> childScreenItems = new LinkedHashMap<>(); // Use LinkedHashMap for order

	        for (GroupMaster group : groups) {
	            if (group.getParentId() == null) {
	                Map<String, Object> gData = new HashMap<>();
	                gData.put("id", group.getGroupCode());
	                gData.put("title", group.getGroupName());
	                gData.put("type", "collapse");
	                gData.put("icon", group.getIcon());
	                gData.put("children", new ArrayList<>());
	                
	                groupWiseScreens.put(group.getId(), gData);
	                children.add(gData);
	            }
	        }

	        for (GroupMaster group : groups) {
	            if (group.getParentId() != null) {
	                Map<String, Object> parentData = groupWiseScreens.get(group.getParentId());
	                if (parentData != null) {
	                    List<Map<String, Object>> parentChildren = (List<Map<String, Object>>) parentData.get("children");
	                    
	                    Map<String, Object> cData = new HashMap<>();
	                    cData.put("id", group.getGroupCode());
	                    cData.put("title", group.getGroupName());
	                    cData.put("type", "collapse");
	                    cData.put("icon", group.getIcon());
	                    cData.put("children", new ArrayList<>());
	                    
	                    childScreenItems.put(group.getId(), cData);
	                    parentChildren.add(cData);
	                }
	            }
	        }

	        finalResult.forEach(mapping -> {
	            Map<String, Object> screenItem = new HashMap<>();
	            screenItem.put("id", mapping.getScreenCode());
	            screenItem.put("title", mapping.getScreenDisplayName());
	            screenItem.put("type", mapping.getType());
	            screenItem.put("url", mapping.getRoute());
	            screenItem.put("icon", mapping.getIcon());
	            screenItem.put("breadcrumbs", mapping.getBreadCrumbs());

	            if (mapping.getGroupId() != null && groupMap.containsKey(mapping.getGroupId())) {
	                UUID groupId = mapping.getGroupId();
	                
	                if (childScreenItems.containsKey(groupId)) {
	                    List<Map<String, Object>> target = (List<Map<String, Object>>) childScreenItems.get(groupId).get("children");
	                    target.add(screenItem);
	                } 
	                else if (groupWiseScreens.containsKey(groupId)) {
	                    List<Map<String, Object>> target = (List<Map<String, Object>>) groupWiseScreens.get(groupId).get("children");
	                    target.add(screenItem);
	                }
	            } else {
	                children.add(screenItem);
	            }
	        });

	        if (!children.isEmpty()) {
	            verticalData.put("children", children);
	        }

	        result.put("status", 200);
	        result.put("message", "Screens list by verticalId " + verticalId + ".");
	        result.put("data", Arrays.asList(verticalData));
	        if (!permissions.isEmpty()) {
	            result.put("permission", permissions.get(0));
	        }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new Exception("Failed to fetch screens by vertical: " + ex.getMessage(), ex);
	    }
	    return result;
	}
}
