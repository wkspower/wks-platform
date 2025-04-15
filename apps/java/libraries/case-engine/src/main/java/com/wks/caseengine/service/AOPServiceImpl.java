package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AOPDTO;
import com.wks.caseengine.entity.AOP;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AOPRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import jakarta.persistence.Query;

@Service
public class AOPServiceImpl implements AOPService {

	@Autowired
	private AOPRepository aopRepository;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<AOPDTO> getAOP() {

		List<AOP> listAOP = aopRepository.findAll();
		List<AOPDTO> aopList = new ArrayList<>();
		try {
			for (AOP aop : listAOP) {
				AOPDTO aopDTO = new AOPDTO();
				aopDTO.setId(aop.getId().toString());
				aopDTO.setAopCaseId(aop.getAopCaseId());
				aopDTO.setAopRemarks(aop.getAopRemarks());
				aopDTO.setAopStatus(aop.getAopStatus());
				aopDTO.setAopType(aop.getAopType());
				aopDTO.setAopYear(aop.getAopYear());
				aopDTO.setApril(aop.getApril());
				aopDTO.setAug(aop.getAug());
				aopDTO.setAvgTPH(aop.getAvgTPH());
				aopDTO.setDec(aop.getDec());
				aopDTO.setFeb(aop.getFeb());
				aopDTO.setJan(aop.getJan());
				aopDTO.setJuly(aop.getJuly());
				aopDTO.setJune(aop.getJune());
				aopDTO.setMarch(aop.getMarch());
				aopDTO.setMay(aop.getMay());
				// aOPDTO.setNormItem(aOP.getNormItem());
				aopDTO.setNov(aop.getNov());
				aopDTO.setOct(aop.getOct());
				aopDTO.setPlantFKId(aop.getPlantFkId().toString());
				aopDTO.setSep(aop.getSep());
				aopList.add(aopDTO);
			}

			// TODO Auto-generated method stub
			return aopList;
		} catch (Exception e) {
			System.err.println("Error occurred while getting AOP data: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Failed to get AOP data: ", e);
		}
	}

	@Override
	public AOPMessageVM getAOPData(String plantId, String year) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<AOPDTO> aopDTOList = new ArrayList<>();
		try {
			List<Object[]> obj = aopRepository.findByAOPYearAndPlantFkId(year, UUID.fromString(plantId));

			for (Object[] row : obj) {
				AOPDTO aopDTO = new AOPDTO();

				aopDTO.setId(row[0] != null ? row[0].toString() : null);
				aopDTO.setAopCaseId(row[1] != null ? row[1].toString() : null);
				aopDTO.setAopStatus(row[2] != null ? row[2].toString() : null);
				aopDTO.setAopRemarks(row[3] != null ? row[3].toString() : null);
				aopDTO.setAopType(row[4] != null ? row[4].toString() : null);

				// Directly parsing float values
				aopDTO.setJan(row[5] != null ? Float.valueOf(row[5].toString()) : null);
				aopDTO.setFeb(row[6] != null ? Float.valueOf(row[6].toString()) : null);
				aopDTO.setMarch(row[7] != null ? Float.valueOf(row[7].toString()) : null);
				aopDTO.setApril(row[8] != null ? Float.valueOf(row[8].toString()) : null);
				aopDTO.setMay(row[9] != null ? Float.valueOf(row[9].toString()) : null);
				aopDTO.setJune(row[10] != null ? Float.valueOf(row[10].toString()) : null);
				aopDTO.setJuly(row[11] != null ? Float.valueOf(row[11].toString()) : null);
				aopDTO.setAug(row[12] != null ? Float.valueOf(row[12].toString()) : null);
				aopDTO.setSep(row[13] != null ? Float.valueOf(row[13].toString()) : null);
				aopDTO.setOct(row[14] != null ? Float.valueOf(row[14].toString()) : null);
				aopDTO.setNov(row[15] != null ? Float.valueOf(row[15].toString()) : null);
				aopDTO.setDec(row[16] != null ? Float.valueOf(row[16].toString()) : null);

				aopDTO.setAopYear(row[17] != null ? row[17].toString() : null);
				aopDTO.setPlantFKId(row[18] != null ? row[18].toString() : null);
				aopDTO.setAvgTPH(row[19] != null ? Float.valueOf(row[19].toString()) : null);
				aopDTO.setMaterialFKId(row[20] != null ? row[20].toString() : null);

				// Directly parsing integer value
				aopDTO.setDisplayOrder(row[21] != null ? Integer.valueOf(row[21].toString()) : null);

				aopDTOList.add(aopDTO);
			}

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(aopDTOList);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to data", ex);
		}
	}

	@Override
	public AOPMessageVM updateAOP(List<AOPDTO> aopDTOList) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<AOP> aopList = new ArrayList<>();
		try {
			for (AOPDTO aopDTO : aopDTOList) {

				AOP aop = null;
				Plants plant = null;
				Sites site = null;
				Verticals vertical = null;
				if (aopDTO.getSiteFKId() == null || aopDTO.getVerticalFKId() == null) {
					plant = plantsRepository.findById(UUID.fromString(aopDTO.getPlantFKId())).get();
					site = siteRepository.findById(plant.getSiteFkId()).get();
					vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
				}

				if (aopDTO.getId() == null) {
					UUID Site = null;
					UUID Vertical = null;
					UUID Material = null;
					UUID Plant = null;
					if (aopDTO.getSiteFKId() != null) {
						Site = UUID.fromString(aopDTO.getSiteFKId());
					} else {
						Site = site.getId();
					}
					if (aopDTO.getVerticalFKId() != null) {
						Vertical = UUID.fromString(aopDTO.getVerticalFKId());
					} else {
						Vertical = vertical.getId();
					}
					if (aopDTO.getMaterialFKId() != null) {
						Material = UUID.fromString(aopDTO.getMaterialFKId());
					}
					if (aopDTO.getPlantFKId() != null) {
						Plant = UUID.fromString(aopDTO.getPlantFKId());
					}
					Optional<UUID> Id = aopRepository.findAopIdByFilters(Site, Vertical, Material, Plant,
							aopDTO.getAopYear());
					aop = new AOP();
					if (Id != null && !Id.isEmpty()) {
						aop.setId(Id.get());
					}

					String caseId = aopDTO.getAopYear() + "-AOP-" + "-V1";
					aop.setAopStatus("draft");
					aop.setAopType("production");
					aop.setAopCaseId(caseId);
				} else if (aopDTO.getId().contains("#")) {
					aop = new AOP();
					aop.setId(null);
					String caseId = aopDTO.getAopYear() + "-AOP-" + "-V1";
					aop.setAopStatus("draft");
					aop.setAopType("production");
					aop.setAopCaseId(caseId);
				} else {
					aop = aopRepository.findById(UUID.fromString(aopDTO.getId())).get();
					aop.setAopCaseId(aopDTO.getAopCaseId());
					aop.setAopStatus(aopDTO.getAopStatus());
					aop.setAopType(aopDTO.getAopType());
				}
				aop.setAopRemarks(aopDTO.getAopRemarks());
				aop.setAopType(aopDTO.getAopType());
				aop.setAopYear(aopDTO.getAopYear());
				aop.setApril(aopDTO.getApril());
				aop.setAug(aopDTO.getAug());
				aop.setAvgTPH(aopDTO.getAvgTPH());
				aop.setDec(aopDTO.getDec());
				aop.setFeb(aopDTO.getFeb());
				aop.setJan(aopDTO.getJan());
				aop.setJuly(aopDTO.getJuly());
				aop.setJune(aopDTO.getJune());
				aop.setMarch(aopDTO.getMarch());
				aop.setMay(aopDTO.getMay());
				// aOP.setNormItem(aOPDTO.getNormItem());
				aop.setNov(aopDTO.getNov());
				aop.setOct(aopDTO.getOct());

				if (aopDTO.getSiteFKId() != null) {
					aop.setSiteFkId(UUID.fromString(aopDTO.getSiteFKId()));
				} else {
					aop.setSiteFkId(site.getId());
				}
				if (aopDTO.getVerticalFKId() != null) {
					aop.setVerticalFkId(UUID.fromString(aopDTO.getVerticalFKId()));
				} else {
					aop.setVerticalFkId(vertical.getId());
				}
				if (aopDTO.getMaterialFKId() != null) {
					aop.setMaterialFKId(UUID.fromString(aopDTO.getMaterialFKId()));
				}
				if (aopDTO.getPlantFKId() != null) {
					aop.setPlantFkId(UUID.fromString(aopDTO.getPlantFKId()));
				}

				aop.setSep(aopDTO.getSep());
				aop.setAopYear(aopDTO.getAopYear());

				aopRepository.save(aop);
				aopList.add(aop);
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

			List<Object[]> maintainsData = aopRepository.checkMaintainance(plantId, year);
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
						year);

				List<AOP> objList = aopRepository.findAllByAopYearAndPlantFkId(year, UUID.fromString(plantId));

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
		try {
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
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid argument passed to the query: " + e.getMessage(), e);
		} catch (Exception e) {
			throw new RuntimeException("Unexpected error occurred while executing dynamic maintenance calculation.", e);
		}

	}

}
