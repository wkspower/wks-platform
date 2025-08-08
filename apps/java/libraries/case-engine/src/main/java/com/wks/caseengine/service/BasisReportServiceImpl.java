package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.BasisReportDTO;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class BasisReportServiceImpl implements BasisReportService {

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public AOPMessageVM getNormBasisReportForPE(String plantId, String aopYear, String type, String periodFrom,
			String periodTo) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> normBasisList = new ArrayList<>();
		try {

			List<Object[]> obj = getReportDataForPE(plantId, aopYear, type, periodFrom, periodTo);
			for (Object[] row : obj) {
				Map<String, Object> map = new HashMap<>(); // Create a new map for each row

				if (type.equalsIgnoreCase("BEST ACHIEVED NORMS")) {

					map.put("account", row[0]);
					map.put("uom", row[1]);
					map.put("material", row[2]);
					map.put("grade", row[3]);
					map.put("january", row[4]);
					map.put("february", row[5]);
					map.put("march", row[6]);
					map.put("april", row[7]);
					map.put("may", row[8]);
					map.put("june", row[9]);
					map.put("july", row[10]);
					map.put("august", row[11]);
					map.put("september", row[12]);
					map.put("october", row[13]);
					map.put("november", row[14]);
					map.put("december", row[15]);
					normBasisList.add(map); // Add the map to the list here

				}
				if (type.equalsIgnoreCase("RAW MCU")) {
					map.put("gradeCode", row[0]);
					map.put("gradeMaxCap", row[1]);
					map.put("mcuDate", row[2]);
					map.put("gradeACTProd", row[3]);
					normBasisList.add(map); // Add the map to the list here
				}
				if (type.equalsIgnoreCase("MCU RANGE")) {
					map.put("gradeCode", row[0]);
					map.put("gradeMaxCap", row[1]);
					map.put("year", row[2]);
					map.put("highRange", row[3]);
					map.put("lowRange", row[4]);
					map.put("lowerLimitPercent", row[5]);
					map.put("upperLimitPercent", row[6]);
					normBasisList.add(map); // Add the map to the list here
				}
				if (type.equalsIgnoreCase("MCU WITHIN RANGE")) {
					map.put("grade", row[0]);
					map.put("gradeMaxCap", row[1]);
					map.put("dateTime", row[2]);
					map.put("gradeACTProd", row[3]);
					map.put("highRange", row[4]);
					map.put("lowRange", row[5]);
					map.put("year", row[6]);
					normBasisList.add(map); // Add the map to the list here
				}
				if (type.equalsIgnoreCase("MIIS NORMS RAW DATA")) {
					map.put("material", row[0]);
					map.put("grade", row[1]);
					map.put("account", row[2]);
					map.put("uom", row[3]);
					map.put("actualQty", row[4]);
					map.put("dateTime", row[5]);
					map.put("contributionType", row[6]);
					normBasisList.add(map); // Add the map to the list here
				}
				if (type.equalsIgnoreCase("CONSECUTIVE DAYS")) {
					map.put("noOfConsecutiveDays", row[0]);
					map.put("grade", row[1]);
					map.put("dateTime", row[2]);
					normBasisList.add(map); // Add the map to the list here
				}
				if (type.equalsIgnoreCase("AVG ANNUAL NORMS")) {
					map.put("account", row[0]);
					map.put("grade", row[1]);
					map.put("material", row[2]);
					map.put("avgFinalNorms", row[3]);
					map.put("year", row[4]);
					normBasisList.add(map); // Add the map to the list here
				}
				if (type.equalsIgnoreCase("PRODUCTION VOLUME BASIS")) {
					map.put("productName", row[0]);
					map.put("aopYear", row[1]);
					map.put("monthNumber", row[2]);
					map.put("operatingHrs", row[3]);
					map.put("avgMCU", row[4]);
					map.put("monthlyBudgetedProduction", row[5]);
					map.put("demand", row[6]);
					map.put("productionValue", row[7]);
					normBasisList.add(map); // Add the map to the list here
				}
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("SP Executed successfully");
			aopMessageVM.setData(normBasisList);
			return aopMessageVM;

		} catch (Exception e) {
			e.printStackTrace();
			return aopMessageVM;
		}

	}

	public List<Object[]> getReportDataForPE(String plantId, String aopYear, String reportType, String PeriodFrom,
			String PeriodTo) {

		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		Sites site = siteRepository.findById(plant.getSiteFkId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

		UUID siteId = site.getId();
		UUID verticalId = vertical.getId();
		String storedProcedure = vertical.getName() + "_" + site.getName() + "_NormsBasisReport";
		String sql = "EXEC " + storedProcedure
				+ " @plantId = :plantId, @aopYear = :aopYear, @Type = :reportType, @PeriodFrom = :PeriodFrom, @PeriodTo = :PeriodTo, @verticalId = :verticalId, @siteId = :siteId";

		Query query = entityManager.createNativeQuery(sql);

		query.setParameter("plantId", plantId);
		query.setParameter("aopYear", aopYear);
		query.setParameter("reportType", reportType);
		query.setParameter("PeriodFrom", PeriodFrom);
		query.setParameter("PeriodTo", PeriodTo);
		query.setParameter("siteId", siteId);
		query.setParameter("verticalId", verticalId);

		return query.getResultList();
	}

	public List<Object[]> getReportDataForCracker(String plantId, String aopYear, String Type, String mode) {

		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		Sites site = siteRepository.findById(plant.getSiteFkId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

		UUID siteId = site.getId();
		UUID verticalId = vertical.getId();
		String storedProcedure = vertical.getName() + "_" + site.getName() + "_NormsBasisReport";
		String sql = "EXEC " + storedProcedure
				+ " @plantId = :plantId, @aopYear = :aopYear, @Type = :Type, @verticalId = :verticalId, @siteId = :siteId, @mode = :mode";

		Query query = entityManager.createNativeQuery(sql);

		query.setParameter("plantId", plantId);
		query.setParameter("aopYear", aopYear);
		query.setParameter("Type", Type);
		query.setParameter("mode", mode);
		query.setParameter("siteId", siteId);
		query.setParameter("verticalId", verticalId);

		return query.getResultList();
	}

	@Override
	public AOPMessageVM getNormBasisReportCracker(String plantId, String aopYear, String type, String mode) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<BasisReportDTO> basisReportDTOList = new ArrayList<>();
		try {

			List<Object[]> obj = getReportDataForCracker(plantId, aopYear, type, mode);
			for (Object[] row : obj) {
				BasisReportDTO basisReportDTO = new BasisReportDTO();
				basisReportDTO.setUom(row[0] != null ? row[0].toString() : null);
				basisReportDTO.setApril(row[1] != null ? row[1].toString() : null);
				basisReportDTO.setMay(row[2] != null ? row[2].toString() : null);
				basisReportDTO.setJune(row[3] != null ? row[3].toString() : null);
				basisReportDTO.setJuly(row[4] != null ? row[4].toString() : null);
				basisReportDTO.setAugust(row[5] != null ? row[5].toString() : null);
				basisReportDTO.setSeptember(row[6] != null ? row[6].toString() : null);
				basisReportDTO.setOctober(row[7] != null ? row[7].toString() : null);
				basisReportDTO.setNovember(row[8] != null ? row[8].toString() : null);
				basisReportDTO.setDecember(row[9] != null ? row[9].toString() : null);
				basisReportDTO.setJanuary(row[10] != null ? row[10].toString() : null);
				basisReportDTO.setFebruary(row[11] != null ? row[11].toString() : null);
				basisReportDTO.setMarch(row[12] != null ? row[12].toString() : null);
				basisReportDTO.setNormParameterDisplayName(row[13] != null ? row[13].toString() : null);
				basisReportDTO.setProductName(row[14] != null ? row[14].toString() : null);
				basisReportDTOList.add(basisReportDTO);
			}
			Map<String, Object> finalResult = new HashMap<>();
            finalResult.put("normHistoricBasisData", basisReportDTOList);
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("SP Executed successfully");
			aopMessageVM.setData(finalResult);
			return aopMessageVM;
		}

		catch (Exception e) {
			e.printStackTrace();
			return aopMessageVM;
		}
	}

}
