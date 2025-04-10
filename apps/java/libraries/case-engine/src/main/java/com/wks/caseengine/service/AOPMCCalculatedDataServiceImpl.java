package com.wks.caseengine.service;

import java.util.ArrayList;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ShutdownNormsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;
import com.wks.caseengine.entity.AOPMCCalculatedData;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AOPMCCalculatedDataRepository;

@Service
public class AOPMCCalculatedDataServiceImpl implements AOPMCCalculatedDataService {

	@Autowired
	private AOPMCCalculatedDataRepository aOPMCCalculatedDataRepository;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public AOPMessageVM getAOPMCCalculatedData(String plantId, String year) {

		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			List<Object[]> obj = aOPMCCalculatedDataRepository.getDataMCUValuesAllData(year, plantId);
			List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList = new ArrayList<>();

			for (Object[] row : obj) {
				AOPMCCalculatedDataDTO aOPMCCalculatedDataDTO = new AOPMCCalculatedDataDTO();
				aOPMCCalculatedDataDTO.setId(row[0] != null ? row[0].toString() : null);
				aOPMCCalculatedDataDTO.setSiteFKId(row[1] != null ? row[1].toString() : null);
				aOPMCCalculatedDataDTO.setPlantFKId(row[2] != null ? row[2].toString() : null);
				aOPMCCalculatedDataDTO.setMaterialFKId(row[3] != null ? row[3].toString() : null);
				aOPMCCalculatedDataDTO.setApril(row[4] != null ? Float.parseFloat(row[4].toString()) : null);
				aOPMCCalculatedDataDTO.setMay(row[5] != null ? Float.parseFloat(row[5].toString()) : null);
				aOPMCCalculatedDataDTO.setJune(row[6] != null ? Float.parseFloat(row[6].toString()) : null);
				aOPMCCalculatedDataDTO.setJuly(row[7] != null ? Float.parseFloat(row[7].toString()) : null);
				aOPMCCalculatedDataDTO.setAugust(row[8] != null ? Float.parseFloat(row[8].toString()) : null);
				aOPMCCalculatedDataDTO.setSeptember(row[9] != null ? Float.parseFloat(row[9].toString()) : null);
				aOPMCCalculatedDataDTO.setOctober(row[10] != null ? Float.parseFloat(row[10].toString()) : null);
				aOPMCCalculatedDataDTO.setNovember(row[11] != null ? Float.parseFloat(row[11].toString()) : null);
				aOPMCCalculatedDataDTO.setDecember(row[12] != null ? Float.parseFloat(row[12].toString()) : null);
				aOPMCCalculatedDataDTO.setJanuary(row[13] != null ? Float.parseFloat(row[13].toString()) : null);
				aOPMCCalculatedDataDTO.setFebruary(row[14] != null ? Float.parseFloat(row[14].toString()) : null);
				aOPMCCalculatedDataDTO.setMarch(row[15] != null ? Float.parseFloat(row[15].toString()) : null);
				aOPMCCalculatedDataDTO.setFinancialYear(row[16] != null ? row[16].toString() : null);
				aOPMCCalculatedDataDTO.setRemarks(row[17] != null ? row[17].toString() : " ");
				aOPMCCalculatedDataDTO.setVerticalFKId(row[22] != null ? row[22].toString() : null);
				aOPMCCalculatedDataDTOList.add(aOPMCCalculatedDataDTO);
			}

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(aOPMCCalculatedDataDTOList);
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
		return aopMessageVM;
	}

	@Override
	public AOPMessageVM editAOPMCCalculatedData(List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTOList) {

		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<AOPMCCalculatedData> aopMCCalculatedDataList = new ArrayList<>();
		try {
			for (AOPMCCalculatedDataDTO aOPMCCalculatedDataDTO : aOPMCCalculatedDataDTOList) {

				AOPMCCalculatedData aOPMCCalculatedData = new AOPMCCalculatedData();
				if (aOPMCCalculatedDataDTO.getId() == null || aOPMCCalculatedDataDTO.getId().contains("#")) {
					aOPMCCalculatedData.setId(null);
				} else {
					aOPMCCalculatedData.setId(UUID.fromString(aOPMCCalculatedDataDTO.getId()));
				}
				aOPMCCalculatedData.setPlantFKId(UUID.fromString(aOPMCCalculatedDataDTO.getPlantFKId()));
				aOPMCCalculatedData.setSiteFKId(UUID.fromString(aOPMCCalculatedDataDTO.getSiteFKId()));
				aOPMCCalculatedData.setVerticalFKId(UUID.fromString(aOPMCCalculatedDataDTO.getVerticalFKId()));
				aOPMCCalculatedData.setMaterialFKId(UUID.fromString(aOPMCCalculatedDataDTO.getMaterialFKId()));

				aOPMCCalculatedData.setJanuary(aOPMCCalculatedDataDTO.getJanuary());
				aOPMCCalculatedData.setFebruary(aOPMCCalculatedDataDTO.getFebruary());
				aOPMCCalculatedData.setMarch(aOPMCCalculatedDataDTO.getMarch());
				aOPMCCalculatedData.setApril(aOPMCCalculatedDataDTO.getApril());
				aOPMCCalculatedData.setMay(aOPMCCalculatedDataDTO.getMay());
				aOPMCCalculatedData.setJune(aOPMCCalculatedDataDTO.getJune());
				aOPMCCalculatedData.setJuly(aOPMCCalculatedDataDTO.getJuly());
				aOPMCCalculatedData.setAugust(aOPMCCalculatedDataDTO.getAugust());
				aOPMCCalculatedData.setSeptember(aOPMCCalculatedDataDTO.getSeptember());
				aOPMCCalculatedData.setOctober(aOPMCCalculatedDataDTO.getOctober());
				aOPMCCalculatedData.setNovember(aOPMCCalculatedDataDTO.getNovember());
				aOPMCCalculatedData.setDecember(aOPMCCalculatedDataDTO.getDecember());
				aOPMCCalculatedData.setJanuary(aOPMCCalculatedDataDTO.getJanuary());

				aOPMCCalculatedData.setFinancialYear(aOPMCCalculatedDataDTO.getFinancialYear());
				aOPMCCalculatedData.setRemarks(aOPMCCalculatedDataDTO.getRemarks());

				aOPMCCalculatedDataRepository.save(aOPMCCalculatedData);
				aopMCCalculatedDataList.add(aOPMCCalculatedData);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data saved successfully");
			aopMessageVM.setData(aopMCCalculatedDataList);
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed saved to  data", ex);
		}
		return aopMessageVM;
	}

	@Transactional
	@Override
	public int getAOPMCCalculatedDataSP(String plantId, String finYear) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			UUID siteId = site.getId();
			UUID verticalId = vertical.getId();
			String storedProcedure = "MEG_LoadMCValues";
			String sql = "EXEC " + storedProcedure
					+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @finYear = :finYear";
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("finYear", finYear);

			return query.executeUpdate();
		} catch (Exception ex) {
			throw new RuntimeException("Failed execute sp", ex);
		}
	}

}
