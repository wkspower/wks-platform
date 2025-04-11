package com.wks.caseengine.rest.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.dto.product.SiteAndPlantDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
//import com.wks.caseengine.service.NormParameterMonthlyTransactionService;
import com.wks.caseengine.service.PlantService;

@RestController
@RequestMapping("task")
public class SiteAndPlantController {

	private final PlantService plantService;

	public SiteAndPlantController(PlantService plantService) {
		this.plantService = plantService;
	}

	@GetMapping("/shutdown-months")
	public ResponseEntity<List> getShutdownMonths(@RequestParam UUID plantId, @RequestParam String maintenanceName) {
		List data = plantService.getShutdownMonths(plantId, maintenanceName);
		return ResponseEntity.ok(data);
	}

	@GetMapping(value = "/plant-site")
	public ResponseEntity<AOPMessageVM> getPlantAndSite() {
		AOPMessageVM aopMessageVM=new AOPMessageVM();
		try {
		List<Object[]> listOfSite = plantService.getPlantAndSite();

		// Group plants by site ID
		Map<UUID, List<Object[]>> groupedBySite = listOfSite.stream()
				.collect(Collectors.groupingBy(result -> UUID.fromString((String) result[0])));

		List<Object> siteData = new ArrayList<>();

		// Iterate through grouped sites and build the response
		for (Map.Entry<UUID, List<Object[]>> entry : groupedBySite.entrySet()) {
			UUID siteId = entry.getKey();
			List<Object[]> plants = entry.getValue();

			// Create the Site object
			SiteResponse siteResponse = new SiteResponse();
			siteResponse.setId(siteId);
			siteResponse.setName((String) plants.get(0)[1]);

			List<PlantResponse> plantResponses = new ArrayList<>();
			for (Object[] plant : plants) {
				PlantResponse plantResponse = new PlantResponse();
				plantResponse.setId(UUID.fromString((String) plant[3]));
				plantResponse.setName((String) plant[4]);
				plantResponse.setDisplayName((String) plant[5]);
				plantResponses.add(plantResponse);
			}

			siteResponse.setPlants(plantResponses);
			siteData.add(siteResponse);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Data fetched successfully");
		aopMessageVM.setData(siteData);
		return ResponseEntity.status(aopMessageVM.getCode()).body(aopMessageVM);
		}  catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}

	// Inner DTOs for the response format
	public static class SiteResponse {
		private UUID id;
		private String name;
		private List<PlantResponse> plants;

		// Getters and setters
		public UUID getId() {
			return id;
		}

		public void setId(UUID id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<PlantResponse> getPlants() {
			return plants;
		}

		public void setPlants(List<PlantResponse> plants) {
			this.plants = plants;
		}
	}

	public static class PlantResponse {
		private UUID id;
		private String name;
		private String displayName;

		// Getters and setters
		public UUID getId() {
			return id;
		}

		public void setId(UUID id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}
	}
}
