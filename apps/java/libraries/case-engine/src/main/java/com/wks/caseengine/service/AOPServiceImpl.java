package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AOPDTO;
import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;
import com.wks.caseengine.entity.AOP;
import com.wks.caseengine.entity.AOPMCCalculatedData;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.repository.AOPRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.rest.entity.Vertical;

import java.util.UUID;

@Service
public class AOPServiceImpl implements  AOPService{
	
	@Autowired
	private AOPRepository aOPRepository;
	@Autowired
	private PlantsRepository plantsRepository;
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private VerticalsRepository verticalRepository;
	
	@Override
	public List<AOPDTO> getAOP() {
		
		List<AOP> listAOP=aOPRepository.findAll();
		List<AOPDTO> aOPList=new ArrayList<>();
		
		for(AOP aOP: listAOP) {
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
			aOPDTO.setNormItem(aOP.getNormItem());
			aOPDTO.setNov(aOP.getNov());
			aOPDTO.setOct(aOP.getOct());
			aOPDTO.setPlantFkId(aOP.getPlantFkId().toString());
			aOPDTO.setSep(aOP.getSep());
			aOPList.add(aOPDTO);
		}
		
		// TODO Auto-generated method stub
		return aOPList;
	}

	@Override
	public List<AOPDTO> getAOPData(String plantId, String year) {
	    List<AOPDTO> aOPDTOList = new ArrayList<>();
	    List<AOP> objList = aOPRepository.findAllByAopYearAndPlantFkId(year, UUID.fromString(plantId));;

	    for (AOP aOPData : objList) {
	       // AOP aOPData = (AOP) obj[0]; // First element (AOPMCCalculatedData)
	       // UUID bdNormParametersFKId = obj[1] != null ? UUID.fromString(obj[1].toString()) : null; // Second element (BDNormParametersFKId)

	        AOPDTO aOPDTO = new AOPDTO();
	        aOPDTO.setId(aOPData.getId().toString());
			aOPDTO.setAopCaseId(aOPData.getAopCaseId());
			aOPDTO.setAopRemarks(aOPData.getAopRemarks());
			aOPDTO.setAopStatus(aOPData.getAopStatus());
			aOPDTO.setAopType(aOPData.getAopType());
			aOPDTO.setAopYear(aOPData.getAopYear());
			aOPDTO.setApril(aOPData.getApril());
			aOPDTO.setAug(aOPData.getAug());
			aOPDTO.setAvgTPH(aOPData.getAvgTPH());
			aOPDTO.setDec(aOPData.getDec());
			aOPDTO.setFeb(aOPData.getFeb());
			aOPDTO.setJan(aOPData.getJan());
			aOPDTO.setJuly(aOPData.getJuly());
			aOPDTO.setJune(aOPData.getJune());
			aOPDTO.setMarch(aOPData.getMarch());
			aOPDTO.setMay(aOPData.getMay());
			aOPDTO.setNormItem(aOPData.getNormItem());
			aOPDTO.setNov(aOPData.getNov());
			aOPDTO.setOct(aOPData.getOct());
			aOPDTO.setPlantFkId(aOPData.getPlantFkId().toString());
			aOPDTO.setSep(aOPData.getSep());

			aOPDTO.setPlantFkId(aOPData.getPlantFkId()!=null ? aOPData.getPlantFkId().toString():null);
			aOPDTO.setNormParametersFKId(aOPData.getNormParametersFKId()!=null ?aOPData.getNormParametersFKId().toString():null);

	        // Set BDNormParametersFKId in DTO
			//aOPDTO.setBDNormParametersFKId(bdNormParametersFKId.toString());
			aOPDTOList.add(aOPDTO);
	    }

		List<Object[]> list = aOPRepository.getDataBusinessAllData(plantId,year);
		int i=1;
				for(Object[] obj :list){
					   
					AOPDTO aOPDTO = new AOPDTO();
		
					aOPDTO.setNormParametersFKId(obj[0]!=null? obj[0].toString():null);
					aOPDTO.setId(i+"#");
					aOPDTOList.add(aOPDTO);
		i++;
				}

	    return aOPDTOList;
	}

	@Override
	public List<AOPDTO> updateAOP(List<AOPDTO> aOPDTOList) {
		for(AOPDTO aOPDTO:aOPDTOList) {
			AOP aOP= null;
			if(aOPDTO.getId()==null || aOPDTO.getId().contains("#")){
				aOP=new AOP();
				String caseId = aOPDTO.getAopYear() + "-AOP-"+aOPDTO.getNormItem()+"-V1";
				aOP.setAopStatus("draft");
				aOP.setAopType("production");
			    aOP.setAopCaseId(caseId);
			}else{
                aOP=aOPRepository.findById(UUID.fromString(aOPDTO.getId())).get();
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
			aOP.setNormItem(aOPDTO.getNormItem());
			aOP.setNov(aOPDTO.getNov());
			aOP.setOct(aOPDTO.getOct());
			aOP.setPlantFkId(UUID.fromString(aOPDTO.getPlantFkId()));
			aOP.setSep(aOPDTO.getSep());

			aOP.setPlantFkId(UUID.fromString(aOPDTO.getPlantFkId()));
			aOP.setAopYear(aOPDTO.getAopYear());
			aOP.setNormParametersFKId(UUID.fromString(aOPDTO.getNormParametersFKId()));
			aOPRepository.save(aOP);
		}
		return aOPDTOList;
	}

	@Override
	public List<AOPDTO> calculateData(String plantId, String year) {

         Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		 Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		 Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		List<Object[]> list =aOPRepository.HMD_MaintenanceCalculation(plant.getName(),site.getName(),vertical.getName(), year);
		
		List<AOP> objList = aOPRepository.findAllByAopYearAndPlantFkId(year, UUID.fromString(plantId));;

		for(AOP aOP:objList){
		    for(Object[] obj :list){
                if(aOP.getNormParametersFKId().toString().equalsIgnoreCase(obj[0].toString()))	{
					System.out.println("obj[0]"+obj[0]);
					aOP.setJan(obj[3]!=null? (Float.parseFloat(obj[3].toString())) : null);
					aOP.setFeb(obj[4]!=null? (Float.parseFloat(obj[4].toString())) : null);
					aOP.setMarch(obj[5]!=null?(Float.parseFloat(obj[5].toString())) : null);
					aOP.setApril(obj[6]!=null?(Float.parseFloat(obj[6].toString())) : null);
					aOP.setMay(obj[7]!=null? (Float.parseFloat(obj[7].toString())) : null);
					aOP.setJune(obj[8]!=null?(Float.parseFloat(obj[8].toString())) : null);
					aOP.setJuly(obj[9]!=null? (Float.parseFloat(obj[9].toString())) : null);
					aOP.setAug(obj[10]!=null? (Float.parseFloat(obj[10].toString())) : null);
					aOP.setSep(obj[11]!=null? (Float.parseFloat(obj[11].toString())) : null);
					aOP.setOct(obj[12]!=null? (Float.parseFloat(obj[12].toString())) : null);
					aOP.setNov(obj[13]!=null? (Float.parseFloat(obj[13].toString())) : null);
					aOP.setDec(obj[14]!=null? (Float.parseFloat(obj[14].toString())) : null);
					//aOP.setAvgTPH(obj[14]!=null? (Float.parseFloat(obj[14].toString())) : null);
				}			
			}	
			aOPRepository.save(aOP);
		}
		return getAOPData(plantId,year);
	}


	

}
