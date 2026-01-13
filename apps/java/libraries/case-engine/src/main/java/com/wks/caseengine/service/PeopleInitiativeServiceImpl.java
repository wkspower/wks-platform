package com.wks.caseengine.service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.PeopleInitiativeDTO;
import com.wks.caseengine.dto.PlantTeamDTO;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.PeopleInitiative;
import com.wks.caseengine.entity.PlantTeam;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.PeopleInitiativeRepository;
import com.wks.caseengine.repository.PlantTeamRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class PeopleInitiativeServiceImpl implements PeopleInitiativeService{
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private PlantsRepository plantsRepository;
	
	@Autowired
	private SiteRepository siteRepository;
	
	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;
	
	@Autowired
	private PlantTeamRepository plantTeamRepository;
	
	@Autowired
	private PeopleInitiativeRepository peopleInitiativeRepository;

	@Override
	public AOPMessageVM getPlantTeam(String plantId, String year) {
		
		try {
			List<Object[]> obj = new ArrayList<>();
			
				String procedureName = "GetPlantTeam";
				obj = findByYearAndPlantId(year, UUID.fromString(plantId), procedureName);
			
			List<PlantTeamDTO> plantTeamDTOs = new ArrayList<>();
			int i = 0;
			for (Object[] row : obj) {
				PlantTeamDTO plantTeamDTO = new PlantTeamDTO();
				plantTeamDTO.setId(row[0] != null ? row[0].toString() : "");

				plantTeamDTO.setSNo(
						(row[1] != null && !row[1].toString().trim().isEmpty())
								? Integer.parseInt(row[1].toString().trim())
								: 0);
				plantTeamDTO.setFunctions(row[2] != null ? row[2].toString() : "");
				plantTeamDTO.setJobRole(row[3] != null ? row[3].toString() : "");
				plantTeamDTO.setName(row[4] != null ? row[4].toString() : "");
				plantTeamDTO.setAge(
						(row[5] != null && !row[5].toString().trim().isEmpty())
								? Integer.parseInt(row[5].toString().trim())
								: 0);
				plantTeamDTO.setTeamSize(
						(row[6] != null && !row[6].toString().trim().isEmpty())
								? Integer.parseInt(row[6].toString().trim())
								: 0);
				plantTeamDTO.setPlantId(row[7] != null ? row[7].toString() : "");
				plantTeamDTO.setAopYear(row[8] != null ? row[8].toString() : "");
				plantTeamDTO.setRemark(row[9] != null ? row[9].toString() : "");
				plantTeamDTOs.add(plantTeamDTO);
				
			}
			Map<String, Object> map = new HashMap<>(); 
			
			map.put("Data", plantTeamDTOs);
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(map);
			aopMessageVM.setMessage("Data fetched successfully");

			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getPeopleInitiative(String plantId, String year) {
		
		try {
			List<Object[]> obj = new ArrayList<>();
			
				String procedureName = "GetPeopleInitiative";
				obj = findByYearAndPlantId(year, UUID.fromString(plantId), procedureName);
			
			List<PeopleInitiativeDTO> peopleInitiativeDTOs = new ArrayList<>();
			
			for (Object[] row : obj) {
				PeopleInitiativeDTO peopleInitiativeDTO = new PeopleInitiativeDTO();
				peopleInitiativeDTO.setId(row[0] != null ? row[0].toString() : "");

				peopleInitiativeDTO.setSNo(
						(row[1] != null && !row[1].toString().trim().isEmpty())
								? Integer.parseInt(row[1].toString().trim())
								: 0);
				peopleInitiativeDTO.setInitiative(row[2] != null ? row[2].toString() : "");
				peopleInitiativeDTO.setOutcome(row[3] != null ? row[3].toString() : "");
				peopleInitiativeDTO.setRecommendation(row[4] != null ? row[4].toString() : "");
				if (row[5] != null) {
				    java.util.Date dateValue = (java.util.Date) row[5];
				    peopleInitiativeDTO.setTargetDate(
				    		dateValue
				    );
				} else {
				    peopleInitiativeDTO.setTargetDate(null);
				}
				peopleInitiativeDTO.setResponsible(row[6] != null ? row[6].toString() : "");
				peopleInitiativeDTO.setPlantId(row[7] != null ? row[7].toString() : "");
				peopleInitiativeDTO.setAopYear(row[8] != null ? row[8].toString() : "");
				peopleInitiativeDTO.setRemark(row[9] != null ? row[9].toString() : "");
				peopleInitiativeDTOs.add(peopleInitiativeDTO);
				
			}
			Map<String, Object> map = new HashMap<>(); 
			
			map.put("Data", peopleInitiativeDTOs);
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(map);
			aopMessageVM.setMessage("Data fetched successfully");

			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> findByYearAndPlantId(String aopYear, UUID plantId, String procedureName) {
		try {

			String sql = "EXEC " + procedureName
					+ " @PlantId = :plantId, @AOPYear = :aopYear";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	@Override
	public AOPMessageVM deletePlantTeam(String id) {
		Optional<PlantTeam> plantTeam =plantTeamRepository.findById(UUID.fromString(id));
		if(plantTeam.isPresent()) {
			plantTeamRepository.delete(plantTeam.get()); 
		}
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(aopMessageVM);
		aopMessageVM.setMessage("Record deleted successfully");
		// TODO Auto-generated method stub
		return aopMessageVM;
	}

	@Override
	public AOPMessageVM deletePeopleInitiative(String id) {
		Optional<PeopleInitiative> peopleInitiativeOpt =peopleInitiativeRepository.findById(UUID.fromString(id));
		if(peopleInitiativeOpt.isPresent()) {
			peopleInitiativeRepository.delete(peopleInitiativeOpt.get()); 
		}
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(aopMessageVM);
		aopMessageVM.setMessage("Record deleted successfully");
		return aopMessageVM;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public AOPMessageVM savePlantTeam(String year, String plantFKId,
			List<PlantTeamDTO> plantTeamDTOs) {
		try {
			List<PlantTeamDTO> failedList = new ArrayList<>();
			UUID plantId = UUID.fromString(plantFKId);

			for (PlantTeamDTO plantTeamDTO : plantTeamDTOs) {
				if (plantTeamDTO.getSaveStatus() != null
						&& plantTeamDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(plantTeamDTO);
					continue;
				}
				PlantTeam plantTeam =null;
				if(plantTeamDTO.getId()!=null) {
					Optional<PlantTeam> plantTeamOpt=plantTeamRepository.findById(UUID.fromString(plantTeamDTO.getId()));
					if(plantTeamOpt.isPresent()) {
						plantTeam=plantTeamOpt.get();
					}
				}else {
					plantTeam=new PlantTeam();
				}
				plantTeam.setAge(plantTeamDTO.getAge());
				plantTeam.setAopYear(year);
				plantTeam.setFunctions(plantTeamDTO.getFunctions());
				plantTeam.setJobRole(plantTeamDTO.getJobRole());
				plantTeam.setName(plantTeamDTO.getName());
				plantTeam.setPlantId(plantId);
				plantTeam.setRemark(plantTeamDTO.getRemark());
				plantTeam.setSrNo(plantTeamDTO.getSNo());
				plantTeam.setTeamSize(plantTeamDTO.getTeamSize());
				plantTeamRepository.save(plantTeam);
			}
			
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(failedList);
			aopMessageVM.setMessage("Data updated successfully");
			return aopMessageVM;
		} catch (Exception ex) {
			ex.printStackTrace();
			
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public AOPMessageVM savePeopleInitiative(String year, String plantFKId,
			List<PeopleInitiativeDTO> peopleInitiativeDTOs) {
		try {
			List<PeopleInitiativeDTO> failedList = new ArrayList<>();
			UUID plantId = UUID.fromString(plantFKId);

			for (PeopleInitiativeDTO peopleInitiativeDTO : peopleInitiativeDTOs) {
				if (peopleInitiativeDTO.getSaveStatus() != null
						&& peopleInitiativeDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(peopleInitiativeDTO);
					continue;
				}
				PeopleInitiative peopleInitiative =null;
				if(peopleInitiativeDTO.getId()!=null) {
					Optional<PeopleInitiative> peopleInitiativeOpt=peopleInitiativeRepository.findById(UUID.fromString(peopleInitiativeDTO.getId()));
					if(peopleInitiativeOpt.isPresent()) {
						peopleInitiative=peopleInitiativeOpt.get();
					}
				}else {
					peopleInitiative=new PeopleInitiative();
				}
				peopleInitiative.setInitiative(peopleInitiativeDTO.getInitiative());
				peopleInitiative.setAopYear(year);
				peopleInitiative.setOutcome(peopleInitiativeDTO.getOutcome());
				peopleInitiative.setRecommendation(peopleInitiativeDTO.getRecommendation());
				peopleInitiative.setResponsible(peopleInitiativeDTO.getResponsible());
				peopleInitiative.setPlantId(plantId);
				peopleInitiative.setRemark(peopleInitiativeDTO.getRemark());
				peopleInitiative.setSrNo(peopleInitiativeDTO.getSNo());
				peopleInitiative.setTargetDate(peopleInitiativeDTO.getTargetDate());
				peopleInitiativeRepository.save(peopleInitiative);
			}
			
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(failedList);
			aopMessageVM.setMessage("Data updated successfully");
			return aopMessageVM;
		} catch (Exception ex) {
			ex.printStackTrace();
			
			throw new RuntimeException("Failed to save data", ex);
		}
	}

}
