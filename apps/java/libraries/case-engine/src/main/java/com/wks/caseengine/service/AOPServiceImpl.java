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
public class AOPServiceImpl implements  AOPService{
	
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
	public List<AOPDTO> getAOPData(String plantId, String year) {
	    List<AOPDTO> aOPDTOList = new ArrayList<>();
	    List<Object[]> obj= aOPRepository.findByAOPYearAndPlantFkId(year, UUID.fromString(plantId));

	    for (Object[] row : obj) {
 	    	AOPDTO aOPDTO = new AOPDTO();
 	        aOPDTO.setId(row[0].toString());
 			aOPDTO.setAopCaseId(row[1] != null ? row[1].toString() : null);
 			aOPDTO.setAopStatus(row[2] != null ? row[2].toString() : null);
 			aOPDTO.setAopRemarks(row[3] != null ? row[3].toString() : null);
 			aOPDTO.setNormItem(row[4] != null ? row[4].toString() : null);
 			aOPDTO.setAopType(row[5] != null ? row[5].toString() : null);
 			aOPDTO.setJan(row[6] != null ? Float.parseFloat(row[6].toString()) : null);
 			aOPDTO.setFeb(row[7] != null ? Float.parseFloat(row[7].toString()) : null);
 			aOPDTO.setMarch(row[8] != null ? Float.parseFloat(row[8].toString()) : null);
 			aOPDTO.setApril(row[9] != null ? Float.parseFloat(row[9].toString()) : null);
 			aOPDTO.setMay(row[10] != null ? Float.parseFloat(row[10].toString()) : null);
 			aOPDTO.setJune(row[11] != null ? Float.parseFloat(row[11].toString()) : null);
 			aOPDTO.setJuly(row[12] != null ? Float.parseFloat(row[12].toString()) : null);
 			aOPDTO.setAug(row[13] != null ? Float.parseFloat(row[13].toString()) : null);
 			aOPDTO.setSep(row[14] != null ? Float.parseFloat(row[14].toString()) : null);
 			aOPDTO.setOct(row[15] != null ? Float.parseFloat(row[15].toString()) : null);
 			aOPDTO.setNov(row[16] != null ? Float.parseFloat(row[16].toString()) : null);
 			aOPDTO.setDec(row[17] != null ? Float.parseFloat(row[17].toString()) : null);    
 			aOPDTO.setAopYear(row[18] != null ? row[18].toString() : null);
			aOPDTO.setPlantFKId(row[19] != null ? row[19].toString() : null);
 			aOPDTO.setAvgTPH(row[20] != null ? Float.parseFloat(row[20].toString()) : null);
 			aOPDTO.setMaterialFKId(row[21] != null ? row[21].toString() : null);
 			aOPDTO.setDisplayOrder(row[22] != null ? Integer.parseInt(row[22].toString()) : null);
 			aOPDTOList.add(aOPDTO);			
 	    }
 	   
		 List<Object[]> list = aOPRepository.getDataBusinessAllData(plantId,year);
		 int i=1;
		 		for(Object[] obj1 :list){
					   
		 			AOPDTO aOPDTO = new AOPDTO();
		
					aOPDTO.setMaterialFKId(obj1[0]!=null? obj1[0].toString():null);
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
			if(aOPDTO.getId()==null) {
				UUID Site=null;
				UUID Vertical=null;
				UUID Material=null;
				UUID Plant=null;
				if(aOPDTO.getSiteFKId()!=null) {
					Site=UUID.fromString(aOPDTO.getSiteFKId());
				}
				if(aOPDTO.getVerticalFKId()!=null) {
					Vertical=UUID.fromString(aOPDTO.getVerticalFKId());
				}
				if(aOPDTO.getMaterialFKId()!=null) {
					Material=UUID.fromString(aOPDTO.getMaterialFKId());
				}
				if(aOPDTO.getPlantFKId()!=null) {
					Plant=UUID.fromString(aOPDTO.getPlantFKId());
				}
				Optional<UUID> Id=aOPRepository.findAopIdByFilters(Site,Vertical,Material,Plant,aOPDTO.getAopYear());
				aOP=new AOP();
				if(Id!=null && !Id.isEmpty()) {
					aOP.setId(Id.get());
				}
				
				String caseId = aOPDTO.getAopYear() + "-AOP-"+aOPDTO.getNormItem()+"-V1";
				aOP.setAopStatus("draft");
				aOP.setAopType("production");
			    aOP.setAopCaseId(caseId);
			}
			else{
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
			// aOP.setNormItem(aOPDTO.getNormItem());
			aOP.setNov(aOPDTO.getNov());
			aOP.setOct(aOPDTO.getOct());
			aOP.setPlantFkId(UUID.fromString(aOPDTO.getPlantFKId()));
			aOP.setSep(aOPDTO.getSep());

			aOP.setSiteFkId(UUID.fromString(aOPDTO.getSiteFKId()));
			aOP.setVerticalFkId(UUID.fromString(aOPDTO.getVerticalFKId()));
			aOP.setAopYear(aOPDTO.getAopYear());
			aOP.setMaterialFKId(UUID.fromString(aOPDTO.getMaterialFKId()));
			aOPRepository.save(aOP);
		}
		return aOPDTOList;
	}

	@Override
	public List<AOPDTO> calculateData(String plantId, String year) {

        List<AOPDTO> dtoList = new ArrayList<>();
		

		List<Object[]> maintainsData =aOPRepository.CheckIfMaintainanceDataExists(plantId,year);
		// if(maintainsData!=null && maintainsData.size()>0){
		if(1==1){
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String verticalName=plantsRepository.findVerticalNameByPlantId(plant.getId());
		//    List<Object[]> list =aOPRepository.HMD_MaintenanceCalculation(plant.getName(),site.getName(),vertical.getName(), year);
			List<Object[]> list = executeDynamicMaintenanceCalculation(verticalName,plant.getId().toString(),
					site.getId().toString(), vertical.getId().toString(), year);
						System.out.println("list"+list);
						System.out.println("listTo String"+list.toString());
		   
		   List<AOP> objList = aOPRepository.findAllByAopYearAndPlantFkId(year, UUID.fromString(plantId));

		   System.out.println("objList"+objList);
		   System.out.println("objList String"+objList.toString());


//            for(AOP aop:objList){
				for(Object[] obj :list){

//				System.out.println("aop" + aop.toString());
					System.out.println("obj"+obj.toString());
				System.out.println("obj[0].toString()" + obj[0].toString());


//				if (aop.getMaterialFKId().toString().equalsIgnoreCase(obj[0].toString())) {
						System.out.println("obj[0]"+obj[0]);
						AOPDTO aopDto = new AOPDTO();
					aopDto.setAopCaseId("");
					aopDto.setAopRemarks("");
					aopDto.setId("");
                        // aopDto.setNormItem(aop.getNormItem());
					aopDto.setPlantFKId(plantId);
					aopDto.setAopStatus("Draft");
					aopDto.setAopYear(year);
					aopDto.setMaterialFKId(obj[0] != null ? (obj[0].toString()) : null);
						aopDto.setJan(obj[3]!=null? (Float.parseFloat(obj[3].toString())) : null);
						aopDto.setFeb(obj[4]!=null? (Float.parseFloat(obj[4].toString())) : null);
						aopDto.setMarch(obj[5]!=null?(Float.parseFloat(obj[5].toString())) : null);
						aopDto.setApril(obj[6]!=null?(Float.parseFloat(obj[6].toString())) : null);
						aopDto.setMay(obj[7]!=null? (Float.parseFloat(obj[7].toString())) : null);
						aopDto.setJune(obj[8]!=null?(Float.parseFloat(obj[8].toString())) : null);
						aopDto.setJuly(obj[9]!=null? (Float.parseFloat(obj[9].toString())) : null);
						aopDto.setAug(obj[10]!=null? (Float.parseFloat(obj[10].toString())) : null);
						aopDto.setSep(obj[11]!=null? (Float.parseFloat(obj[11].toString())) : null);
						aopDto.setOct(obj[12]!=null? (Float.parseFloat(obj[12].toString())) : null);
						aopDto.setNov(obj[13]!=null? (Float.parseFloat(obj[13].toString())) : null);
						aopDto.setDec(obj[14]!=null? (Float.parseFloat(obj[14].toString())) : null);
						dtoList.add(aopDto);
						//aOP.setAvgTPH(obj[14]!=null? (Float.parseFloat(obj[14].toString())) : null);
					}			
//			}
			}
//		else {
//			dtoList = getAOPData(plantId, year);
//		}
		return dtoList;
	}

	

	 @Override
	    public List<Object[]> executeDynamicMaintenanceCalculation(
	        String verticalName, String plantId, String siteId, String verticalId, String aopYear) {
	        
	        // Construct dynamic stored procedure name
	        String procedureName = verticalName + "_HMD_MaintenanceCalculation";

	        // Create a native query to execute the stored procedure
	        String sql = "EXEC " + procedureName + 
	                     " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @aopYear = :aopYear";
	        
	        Query query = entityManager.createNativeQuery(sql);
	        
	        // Set parameters
	        query.setParameter("plantId", plantId);
	        query.setParameter("siteId", siteId);
	        query.setParameter("verticalId", verticalId);
	        query.setParameter("aopYear", aopYear);

	        return query.getResultList();
	    }
	

}
