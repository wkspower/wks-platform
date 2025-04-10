package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AOPDTO;
import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;
import com.wks.caseengine.entity.AOP;
import com.wks.caseengine.entity.AOPMCCalculatedData;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AOPRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.rest.entity.Vertical;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.persistence.Query;

@Service
public class AOPServiceImpl implements AOPService {

	@Autowired
	private AOPRepository aOPRepository;
	@Autowired
	private PlantsRepository plantsRepository;
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private VerticalsRepository verticalRepository;

	@Autowired
	private NormParametersService normParametersService;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<AOPDTO> getAOP() {

		List<AOP> listAOP = aOPRepository.findAll();
		List<AOPDTO> aOPList = new ArrayList<>();

		for (AOP aOP : listAOP) {
			AOPDTO aOPDTO = new AOPDTO();
			aOPDTO.setId(aOP.getId().toString());
			aOPDTO.setAopCaseId(aOP.getAopCaseId());
			aOPDTO.setAopRemarks(aOP.getAopRemarks());
			aOPDTO.setAopStatus(aOP.getAopStatus());
			aOPDTO.setAopType(aOP.getAopType());
			aOPDTO.setAopYear(aOP.getAopYear());
			aOPDTO.setApril(aOP.getApril());
			aOPDTO.setAug(aOP.getAug());
			aOPDTO.setAvgTPH(aOP.getAvgTPH());
			aOPDTO.setDec(aOP.getDec());
			aOPDTO.setFeb(aOP.getFeb());
			aOPDTO.setJan(aOP.getJan());
			aOPDTO.setJuly(aOP.getJuly());
			aOPDTO.setJune(aOP.getJune());
			aOPDTO.setMarch(aOP.getMarch());
			aOPDTO.setMay(aOP.getMay());
			// aOPDTO.setNormItem(aOP.getNormItem());
			aOPDTO.setNov(aOP.getNov());
			aOPDTO.setOct(aOP.getOct());
			aOPDTO.setPlantFKId(aOP.getPlantFkId().toString());
			aOPDTO.setSep(aOP.getSep());
			aOPList.add(aOPDTO);
		}

		// TODO Auto-generated method stub
		return aOPList;
	}

	@Override
	public AOPMessageVM getAOPData(String plantId, String year) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<AOPDTO> aOPDTOList = new ArrayList<>();
		try {
			List<Object[]> obj = aOPRepository.findByAOPYearAndPlantFkId(year, UUID.fromString(plantId));

			for (Object[] row : obj) {
				AOPDTO aOPDTO = new AOPDTO();

				aOPDTO.setId(row[0] != null ? row[0].toString() : null);
				aOPDTO.setAopCaseId(row[1] != null ? row[1].toString() : null);
				aOPDTO.setAopStatus(row[2] != null ? row[2].toString() : null);
				aOPDTO.setAopRemarks(row[3] != null ? row[3].toString() : null);
				aOPDTO.setAopType(row[4] != null ? row[4].toString() : null);

				// Directly parsing float values
				aOPDTO.setJan(row[5] != null ? Float.valueOf(row[5].toString()) : null);
				aOPDTO.setFeb(row[6] != null ? Float.valueOf(row[6].toString()) : null);
				aOPDTO.setMarch(row[7] != null ? Float.valueOf(row[7].toString()) : null);
				aOPDTO.setApril(row[8] != null ? Float.valueOf(row[8].toString()) : null);
				aOPDTO.setMay(row[9] != null ? Float.valueOf(row[9].toString()) : null);
				aOPDTO.setJune(row[10] != null ? Float.valueOf(row[10].toString()) : null);
				aOPDTO.setJuly(row[11] != null ? Float.valueOf(row[11].toString()) : null);
				aOPDTO.setAug(row[12] != null ? Float.valueOf(row[12].toString()) : null);
				aOPDTO.setSep(row[13] != null ? Float.valueOf(row[13].toString()) : null);
				aOPDTO.setOct(row[14] != null ? Float.valueOf(row[14].toString()) : null);
				aOPDTO.setNov(row[15] != null ? Float.valueOf(row[15].toString()) : null);
				aOPDTO.setDec(row[16] != null ? Float.valueOf(row[16].toString()) : null);

				aOPDTO.setAopYear(row[17] != null ? row[17].toString() : null);
				aOPDTO.setPlantFKId(row[18] != null ? row[18].toString() : null);
				aOPDTO.setAvgTPH(row[19] != null ? Float.valueOf(row[19].toString()) : null);
				aOPDTO.setMaterialFKId(row[20] != null ? row[20].toString() : null);

				// Directly parsing integer value
				aOPDTO.setDisplayOrder(row[21] != null ? Integer.valueOf(row[21].toString()) : null);

				aOPDTOList.add(aOPDTO);
			}

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(aOPDTOList);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to data", ex);
		}
	}

	@Override
	public AOPMessageVM updateAOP(List<AOPDTO> aOPDTOList) {
		AOPMessageVM aopMessageVM=new AOPMessageVM();
		List<AOP> aopList=new ArrayList<>();
		try {
		for (AOPDTO aOPDTO : aOPDTOList) {

			AOP aOP = null;
			Plants plant = null;
			Sites site = null;
			Verticals vertical = null;
			if (aOPDTO.getSiteFKId() == null || aOPDTO.getVerticalFKId() == null) {
				plant = plantsRepository.findById(UUID.fromString(aOPDTO.getPlantFKId())).get();
				site = siteRepository.findById(plant.getSiteFkId()).get();
				vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			}

			if (aOPDTO.getId() == null) {
				UUID Site = null;
				UUID Vertical = null;
				UUID Material = null;
				UUID Plant = null;
				if (aOPDTO.getSiteFKId() != null) {
					Site = UUID.fromString(aOPDTO.getSiteFKId());
				} else {
					Site = site.getId();
				}
				if (aOPDTO.getVerticalFKId() != null) {
					Vertical = UUID.fromString(aOPDTO.getVerticalFKId());
				} else {
					Vertical = vertical.getId();
				}
				if (aOPDTO.getMaterialFKId() != null) {
					Material = UUID.fromString(aOPDTO.getMaterialFKId());
				}
				if (aOPDTO.getPlantFKId() != null) {
					Plant = UUID.fromString(aOPDTO.getPlantFKId());
				}
				Optional<UUID> Id = aOPRepository.findAopIdByFilters(Site, Vertical, Material, Plant,
						aOPDTO.getAopYear());
				aOP = new AOP();
				if (Id != null && !Id.isEmpty()) {
					aOP.setId(Id.get());
				}

				String caseId = aOPDTO.getAopYear() + "-AOP-" + "-V1";
				aOP.setAopStatus("draft");
				aOP.setAopType("production");
				aOP.setAopCaseId(caseId);
			} else if (aOPDTO.getId().contains("#")) {
				aOP = new AOP();
				aOP.setId(null);
				String caseId = aOPDTO.getAopYear() + "-AOP-" + "-V1";
				aOP.setAopStatus("draft");
				aOP.setAopType("production");
				aOP.setAopCaseId(caseId);
			} else {
				aOP = aOPRepository.findById(UUID.fromString(aOPDTO.getId())).get();
				aOP.setAopCaseId(aOPDTO.getAopCaseId());
				aOP.setAopStatus(aOPDTO.getAopStatus());
				aOP.setAopType(aOPDTO.getAopType());
			}
			aOP.setAopRemarks(aOPDTO.getAopRemarks());
			aOP.setAopType(aOPDTO.getAopType());
			aOP.setAopYear(aOPDTO.getAopYear());
			aOP.setApril(aOPDTO.getApril());
			aOP.setAug(aOPDTO.getAug());
			aOP.setAvgTPH(aOPDTO.getAvgTPH());
			aOP.setDec(aOPDTO.getDec());
			aOP.setFeb(aOPDTO.getFeb());
			aOP.setJan(aOPDTO.getJan());
			aOP.setJuly(aOPDTO.getJuly());
			aOP.setJune(aOPDTO.getJune());
			aOP.setMarch(aOPDTO.getMarch());
			aOP.setMay(aOPDTO.getMay());
			// aOP.setNormItem(aOPDTO.getNormItem());
			aOP.setNov(aOPDTO.getNov());
			aOP.setOct(aOPDTO.getOct());

			if (aOPDTO.getSiteFKId() != null) {
				aOP.setSiteFkId(UUID.fromString(aOPDTO.getSiteFKId()));
			} else {
				aOP.setSiteFkId(site.getId());
			}
			if (aOPDTO.getVerticalFKId() != null) {
				aOP.setVerticalFkId(UUID.fromString(aOPDTO.getVerticalFKId()));
			} else {
				aOP.setVerticalFkId(vertical.getId());
			}
			if (aOPDTO.getMaterialFKId() != null) {
				aOP.setMaterialFKId(UUID.fromString(aOPDTO.getMaterialFKId()));
			}
			if (aOPDTO.getPlantFKId() != null) {
				aOP.setPlantFkId(UUID.fromString(aOPDTO.getPlantFKId()));
			}

			aOP.setSep(aOPDTO.getSep());
			aOP.setAopYear(aOPDTO.getAopYear());
			
			aOPRepository.save(aOP);
			aopList.add(aOP);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Data saved successfully");
		aopMessageVM.setData(aopList);
		return aopMessageVM;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	@Override
	public AOPMessageVM calculateData(String plantId, String year) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			List<AOPDTO> dtoList = new ArrayList<>();

			List<Object[]> maintainsData = aOPRepository.CheckIfMaintainanceDataExists(plantId, year);
			// if(maintainsData != null && !maintainsData.isEmpty()) {
			if (1 == 1) {
				Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
				Sites site = siteRepository.findById(plant.getSiteFkId()).get();
				Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
				String verticalName = plantsRepository.findVerticalNameByPlantId(plant.getId());

				List<Object[]> list = executeDynamicMaintenanceCalculation(
					verticalName,
					plant.getId().toString(),
					site.getId().toString(),
					vertical.getId().toString(),
					year
				);

				List<AOP> objList = aOPRepository.findAllByAopYearAndPlantFkId(year, UUID.fromString(plantId));

				for (Object[] obj : list) {
					AOPDTO aopDto = new AOPDTO();
					aopDto.setAopCaseId("");
					aopDto.setAopRemarks("");
					aopDto.setId("");
					aopDto.setPlantFKId(plantId);
					aopDto.setAopStatus("Draft");
					aopDto.setAopYear(year);
					aopDto.setMaterialFKId(obj[0] != null ? obj[0].toString() : null);
					aopDto.setSiteFKId(site.getId().toString());
					aopDto.setJan(obj[3] != null ? Float.parseFloat(obj[3].toString()) : null);
					aopDto.setFeb(obj[4] != null ? Float.parseFloat(obj[4].toString()) : null);
					aopDto.setMarch(obj[5] != null ? Float.parseFloat(obj[5].toString()) : null);
					aopDto.setApril(obj[6] != null ? Float.parseFloat(obj[6].toString()) : null);
					aopDto.setMay(obj[7] != null ? Float.parseFloat(obj[7].toString()) : null);
					aopDto.setJune(obj[8] != null ? Float.parseFloat(obj[8].toString()) : null);
					aopDto.setJuly(obj[9] != null ? Float.parseFloat(obj[9].toString()) : null);
					aopDto.setAug(obj[10] != null ? Float.parseFloat(obj[10].toString()) : null);
					aopDto.setSep(obj[11] != null ? Float.parseFloat(obj[11].toString()) : null);
					aopDto.setOct(obj[12] != null ? Float.parseFloat(obj[12].toString()) : null);
					aopDto.setNov(obj[13] != null ? Float.parseFloat(obj[13].toString()) : null);
					aopDto.setDec(obj[14] != null ? Float.parseFloat(obj[14].toString()) : null);

					dtoList.add(aopDto);
				}

				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("Data fetched successfully");
				aopMessageVM.setData(dtoList);
			}
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

		return aopMessageVM;
	}

	@Override
	public List<Object[]> executeDynamicMaintenanceCalculation(String verticalName, String plantId, String siteId,
			String verticalId, String aopYear) {

		// Construct dynamic stored procedure name
		String procedureName = verticalName + "_HMD_MaintenanceCalculation";

		// Create a native query to execute the stored procedure
		String sql = "EXEC " + procedureName
				+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @aopYear = :aopYear";

		Query query = entityManager.createNativeQuery(sql);

		// Set parameters
		query.setParameter("plantId", plantId);
		query.setParameter("siteId", siteId);
		query.setParameter("verticalId", verticalId);
		query.setParameter("aopYear", aopYear);

		return query.getResultList();
	}

}
