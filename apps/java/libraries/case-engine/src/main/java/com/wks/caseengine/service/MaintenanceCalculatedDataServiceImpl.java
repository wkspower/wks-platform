package com.wks.caseengine.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

import com.wks.caseengine.dto.DecokePlanningDTO;
import com.wks.caseengine.dto.MaintenanceDetailsDTO;
import com.wks.caseengine.entity.DecokeMaintenance;
import com.wks.caseengine.entity.DecokePlanning;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.DecokeMaintenanceRepository;
import com.wks.caseengine.repository.DecokePlanningRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

@Service
public class MaintenanceCalculatedDataServiceImpl implements MaintenanceCalculatedDataService {

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private DecokePlanningRepository decokePlanningRepository;
	
	@Autowired
	private DecokeMaintenanceRepository decokeMaintenanceRepository;

	@Override
	public List<MaintenanceDetailsDTO> getMaintenanceCalculatedData(String plantId, String year) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_GETMaintenance";
			List<Object[]> list = executeDynamicStoredProcedure(storedProcedure, plantId, site.getId().toString(),
					vertical.getId().toString(), year);
			List<MaintenanceDetailsDTO> maintenanceDetailsDTOList = new ArrayList<>();
			for (Object[] row : list) {
				MaintenanceDetailsDTO dto = new MaintenanceDetailsDTO();
				dto.setName(row[2] != null ? row[2].toString() : null);
				dto.setJan(row[3] != null ? Double.valueOf(row[3].toString()) : null);
				dto.setFeb(row[4] != null ? Double.valueOf(row[4].toString()) : null);
				dto.setMar(row[5] != null ? Double.valueOf(row[5].toString()) : null);
				dto.setApril(row[6] != null ? Double.valueOf(row[6].toString()) : null);
				dto.setMay(row[7] != null ? Double.valueOf(row[7].toString()) : null);
				dto.setJune(row[8] != null ? Double.valueOf(row[8].toString()) : null);
				dto.setJuly(row[9] != null ? Double.valueOf(row[9].toString()) : null);
				dto.setAug(row[10] != null ? Double.valueOf(row[10].toString()) : null);
				dto.setSep(row[11] != null ? Double.valueOf(row[11].toString()) : null);
				dto.setOct(row[12] != null ? Double.valueOf(row[12].toString()) : null);
				dto.setNov(row[13] != null ? Double.valueOf(row[13].toString()) : null);
				dto.setDec(row[14] != null ? Double.valueOf(row[14].toString()) : null);
				maintenanceDetailsDTOList.add(dto);
			}

			return maintenanceDetailsDTOList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional
	public List<Object[]> executeDynamicStoredProcedure(String procedureName, String plantId, String siteId,
			String verticalId, String aopYear) {
		try {
			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @aopYear = :aopYear";
			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getMaintenanceDataForCracker(String plantId, String year) {

		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> data = new ArrayList<>();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			String procedureName = "vwScrn" + vertical.getName() + "_" + site.getName() + "_Decoke_Maintenance";
			List<Object[]> results = getData(plantId, year, procedureName);

			for (Object[] row : results) {
				Map<String, Object> map = new HashMap<>(); // Create a new map for each row

				map.put("id", row[0]);
				map.put("monthName", row[1]);
				map.put("coilReplacement", row[2]);
				map.put("mnt", row[3]);
				map.put("shutdown", row[4]);
				map.put("slowdown", row[5]);
				map.put("sad", row[6]);
				map.put("bbd", row[7]);
				map.put("bbu", row[8]);
				map.put("demoHSS", row[9]);
				map.put("demoBBU", row[10]);
				map.put("demoSAD", row[11]);
				map.put("demoSD", row[12]);
				map.put("fourFD", row[13]);
				map.put("fourF", row[14]);
				map.put("fiveF", row[15]);
				map.put("total", row[16]);
				map.put("fourFHours", row[17]);
				map.put("aopYear", row[18]);
				map.put("plantId", row[19]);
				String remarks = row[20] == null ? "" : row[20].toString();
				map.put("remarks", remarks);
				map.put("totalSAD", row[21]);
				map.put("numberOfDays", row[22]);

				data.add(map); // Add the map to the list here
			}

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(data);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}

	public List<Object[]> getData(String plantId, String aopYear, String viewName) {
		try {

			// 2. Construct SQL with dynamic view name
			String sql = "SELECT * FROM " + viewName +
					" WHERE PlantId = :plantId AND AOPYear = :aopYear " +
					"ORDER BY CASE MonthName " +
					"    WHEN 'April' THEN 1 " +
					"    WHEN 'May' THEN 2 " +
					"    WHEN 'June' THEN 3 " +
					"    WHEN 'July' THEN 4 " +
					"    WHEN 'August' THEN 5 " +
					"    WHEN 'September' THEN 6 " +
					"    WHEN 'October' THEN 7 " +
					"    WHEN 'November' THEN 8 " +
					"    WHEN 'December' THEN 9 " +
					"    WHEN 'January' THEN 10 " +
					"    WHEN 'February' THEN 11 " +
					"    WHEN 'March' THEN 12 " +
					"    ELSE 13 " +
					"END";

			// 3. Create and parameterize the native query
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);

			// 4. Execute
			return query.getResultList();

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid argument: " + e.getMessage(), e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data from view " + viewName, ex);
		}
	}

	@Override
	public AOPMessageVM updateMaintenanceDataForCracker(String plantId, String year,
			List<DecokePlanningDTO> decokePlanningDTOList) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<DecokeMaintenance> decokeMaintenanceList = new ArrayList<>();
		try {
			for (DecokePlanningDTO decokePlanningDTO : decokePlanningDTOList) {
				Optional<DecokeMaintenance> decokePlanningop = decokeMaintenanceRepository
						.findById(decokePlanningDTO.getId());
				if (decokePlanningop.isPresent()) {
					DecokeMaintenance decokeMaintenance = decokePlanningop.get();
					decokeMaintenance.setMnt(decokePlanningDTO.getMnt());
					decokeMaintenance.setRemarks(decokePlanningDTO.getRemarks());
					decokeMaintenance.setBbd(decokePlanningDTO.getBbd());
					decokeMaintenance.setBbu(decokePlanningDTO.getBbu());
					decokeMaintenance.setDemoBbu(decokePlanningDTO.getDemoBBU());
					decokeMaintenance.setDemoHss(decokePlanningDTO.getDemoHSS());
					decokeMaintenance.setDemoSad(decokePlanningDTO.getDemoSAD());
					decokeMaintenance.setDemoSd(decokePlanningDTO.getDemoSD());
					decokeMaintenance.setFiveF(decokePlanningDTO.getFiveF());
					decokeMaintenance.setFourF(decokePlanningDTO.getFourF());
					decokeMaintenance.setFourFd(decokePlanningDTO.getFourFD());
					decokeMaintenance.setFourFHours(decokePlanningDTO.getFourFHours());
					decokeMaintenance.setIbr(decokePlanningDTO.getIbr());
					decokeMaintenance.setShoutdown(decokePlanningDTO.getShutdown());
					decokeMaintenance.setSad(decokePlanningDTO.getSad());
					decokeMaintenance.setSlowdown(decokePlanningDTO.getSlowdown());
					decokeMaintenance.setTotal(decokePlanningDTO.getTotal());
					decokeMaintenanceList.add(decokeMaintenanceRepository.save(decokeMaintenance));
				}
			}
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to update data", ex);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Data updated successfully");
		aopMessageVM.setData(decokeMaintenanceList);
		return aopMessageVM;

	}

}
