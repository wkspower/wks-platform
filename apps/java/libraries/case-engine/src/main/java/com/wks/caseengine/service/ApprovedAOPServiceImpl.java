package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AOPDTO;
import com.wks.caseengine.dto.AOPDashboardDTO;
import com.wks.caseengine.dto.AOPProposedNormsDTO;
import com.wks.caseengine.entity.AOP;
import com.wks.caseengine.entity.AOPProposedNormsGradeWise;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.ApprovedAOP;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AOPProposedNormsGradeWiseRepository;
import com.wks.caseengine.repository.AOPRepository;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.ApprovedAOPRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.UUID;


import javax.sql.DataSource;

import java.io.ByteArrayOutputStream;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Connection;
import jakarta.persistence.Query;

@Service
public class ApprovedAOPServiceImpl implements ApprovedAOPService {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;
	
	@Autowired
	private ApprovedAOPRepository approvedAOPRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;

	@Autowired
	private AopCalculationRepository aopCalculationRepository;

	@Override
	@Transactional
	public AOPMessageVM updateApprovedAOP( String plantId,String year) {
	    try {
	        
	    	List<ApprovedAOP>  approvedAOPs=approvedAOPRepository.findByYearAndPlant( year,  UUID.fromString(plantId));

	            if (approvedAOPs.isEmpty()) {
	            	ApprovedAOP entity = new ApprovedAOP();
	            	entity.setAopYear(year);
	            	entity.setPlantFkId(UUID.fromString(plantId));
	            	approvedAOPRepository.save(entity);
	       }
	        
	        AOPMessageVM aopMessageVM = new AOPMessageVM();
	        aopMessageVM.setCode(200);
	        aopMessageVM.setData(null);
	        aopMessageVM.setMessage("Data saved successfully");
	        return aopMessageVM;
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to update proposed norms", ex);
	    }
	}
	
	@Override
	public AOPMessageVM getApprovedAOP( String plantId,String year) {
	    try {
	    	 	AOPMessageVM aopMessageVM = new AOPMessageVM();
		        aopMessageVM.setCode(200);
		        List<ApprovedAOP>  approvedAOPs=approvedAOPRepository.findByYearAndPlant( year,  UUID.fromString(plantId));

	            if (approvedAOPs!=null && approvedAOPs.size()>0) {
	            	aopMessageVM.setMessage("Data fetched successfully");
	            	aopMessageVM.setData(approvedAOPs);
	            }else {
	            	aopMessageVM.setMessage("No record found");
	            	aopMessageVM.setData(null);
	            }
	        return aopMessageVM;
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to update proposed norms", ex);
	    }
	}

	@Override
	public AOPMessageVM deleteApprovedAOP(String id) {
		Optional<ApprovedAOP> approvedAOPOPT=approvedAOPRepository.findById(UUID.fromString(id));
		AOPMessageVM aopMessageVM = new AOPMessageVM();
        aopMessageVM.setCode(200);
		if(approvedAOPOPT.isPresent()) {
			ApprovedAOP approvedAOP= approvedAOPOPT.get();
			approvedAOPRepository.delete(approvedAOP);
			aopMessageVM.setData(approvedAOP);
			aopMessageVM.setMessage("Data deleted successfully");
		}else {
			aopMessageVM.setData(null);
			aopMessageVM.setMessage("No record found with Id="+id);
		}
		return aopMessageVM;
	}

}
