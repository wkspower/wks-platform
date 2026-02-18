package com.wks.caseengine.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.wks.caseengine.dto.PeopleInitiativeDTO;
import com.wks.caseengine.dto.PlantTeamDTO;
import com.wks.caseengine.dto.SiteTeamTranscationDTO;
import com.wks.caseengine.entity.PeopleInitiative;
import com.wks.caseengine.entity.PlantTeam;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.SiteTeam;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PeopleInitiativeRepository;
import com.wks.caseengine.repository.PlantTeamRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.SiteTeamRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class SiteTeamTranscationServiceImpl implements SiteTeamTranscationService{
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private SiteTeamRepository siteTeamRepository;
	
	@Autowired
	private SiteRepository siteRepository;

	@Override
	public AOPMessageVM getSiteTeamTransaction(String siteId, String year) {
		
		try {
			List<Object[]> obj = new ArrayList<>();
			
			
			Sites site = siteRepository.findById(UUID.fromString(siteId)).orElseThrow();
				String procedureName = site.getName()+"_SiteTeamTranscation";
				obj = findByYearAndSiteId(year, site.getId(), procedureName);
			
			List<SiteTeamTranscationDTO> siteTeamTranscationDTOs = new ArrayList<>();
			
			for (Object[] row : obj) {
				SiteTeamTranscationDTO siteTeamTranscationDTO = new SiteTeamTranscationDTO();
				siteTeamTranscationDTO.setId(row[0] != null ? row[0].toString() : "");

				
				siteTeamTranscationDTO.setJobRole(row[1] != null ? row[1].toString() : "");
				siteTeamTranscationDTO.setName(row[2] != null ? row[2].toString() : "");
				siteTeamTranscationDTO.setAge(
						(row[3] != null && !row[3].toString().trim().isEmpty())
								? Integer.parseInt(row[3].toString().trim())
								: 0);
				siteTeamTranscationDTO.setTeamSize(
						(row[4] != null && !row[4].toString().trim().isEmpty())
								? Integer.parseInt(row[4].toString().trim())
								: 0);
				
				siteTeamTranscationDTO.setRemark(row[5] != null ? row[5].toString() : "");
				siteTeamTranscationDTOs.add(siteTeamTranscationDTO);
				
			}
			Map<String, Object> map = new HashMap<>(); 
			
			map.put("Data", siteTeamTranscationDTOs);
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

	
	public List<Object[]> findByYearAndSiteId(String aopYear, UUID siteId, String procedureName) {
		try {

			String sql = "EXEC " + procedureName
					+ " @SiteId = :siteId, @AOPYear = :aopYear";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("siteId", siteId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public AOPMessageVM saveSiteTeamTransaction(String year, String plantFKId,
			List<SiteTeamTranscationDTO> siteTeamTranscationDTOs) {
		try {
			List<SiteTeamTranscationDTO> failedList = new ArrayList<>();

			for (SiteTeamTranscationDTO siteTeamTranscationDTO : siteTeamTranscationDTOs) {
				if (siteTeamTranscationDTO.getSaveStatus() != null
						&& siteTeamTranscationDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(siteTeamTranscationDTO);
					continue;
				}
				SiteTeam siteTeam =null;
				if(siteTeamTranscationDTO.getId()!=null) {
					Optional<SiteTeam> siteTeamOpt=siteTeamRepository.findById(UUID.fromString(siteTeamTranscationDTO.getId()));
					if(siteTeamOpt.isPresent()) {
						siteTeam=siteTeamOpt.get();
					}else {
						siteTeamTranscationDTO.setSaveStatus("Failed");
						siteTeamTranscationDTO.setErrDescription("Data not present with given id");
						failedList.add(siteTeamTranscationDTO);
					}
				}
				siteTeam.setAge(siteTeamTranscationDTO.getAge());
				siteTeam.setName(siteTeamTranscationDTO.getName());			
				siteTeam.setRemark(siteTeamTranscationDTO.getRemark());
				siteTeam.setTeamSize(siteTeamTranscationDTO.getTeamSize());
				siteTeamRepository.save(siteTeam);
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
