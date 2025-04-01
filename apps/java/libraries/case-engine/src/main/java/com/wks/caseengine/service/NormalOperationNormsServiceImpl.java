package com.wks.caseengine.service;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.MCUNormsValueDTO;
import com.wks.caseengine.entity.MCUNormsValue;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.repository.NormalOperationNormsRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

@Service
public class NormalOperationNormsServiceImpl implements NormalOperationNormsService{
	
	@Autowired
	private NormalOperationNormsRepository normalOperationNormsRepository;
	
	@PersistenceContext
    private EntityManager entityManager;
	@Autowired
	PlantsRepository plantsRepository;
	@Autowired
	SiteRepository siteRepository;
	@Autowired
	VerticalsRepository verticalRepository;


	@Override
	public List<MCUNormsValueDTO> getNormalOperationNormsData(String year, String plantId) {
		List<Object[]> obj = normalOperationNormsRepository.findByYearAndPlantFkId(year, UUID.fromString(plantId));
		List<MCUNormsValueDTO> mCUNormsValueDTOList = new ArrayList<>();

		for (Object[] row : obj) {
		    MCUNormsValueDTO mCUNormsValueDTO = new MCUNormsValueDTO();
		    mCUNormsValueDTO.setId(row[0].toString());
		    mCUNormsValueDTO.setSiteFkId(row[1].toString());
		    mCUNormsValueDTO.setPlantFkId(row[2].toString());
		    mCUNormsValueDTO.setVerticalFkId(row[3].toString());
		    mCUNormsValueDTO.setMaterialFkId(row[4].toString());
		    
		    mCUNormsValueDTO.setApril(row[5] != null ? Float.parseFloat(row[5].toString()) : null);
		    mCUNormsValueDTO.setMay(row[6] != null ? Float.parseFloat(row[6].toString()) : null);
		    mCUNormsValueDTO.setJune(row[7] != null ? Float.parseFloat(row[7].toString()) : null);
		    mCUNormsValueDTO.setJuly(row[8] != null ? Float.parseFloat(row[8].toString()) : null);
		    mCUNormsValueDTO.setAugust(row[9] != null ? Float.parseFloat(row[9].toString()) : null);
		    mCUNormsValueDTO.setSeptember(row[10] != null ? Float.parseFloat(row[10].toString()) : null);
		    mCUNormsValueDTO.setOctober(row[11] != null ? Float.parseFloat(row[11].toString()) : null);
		    mCUNormsValueDTO.setNovember(row[12] != null ? Float.parseFloat(row[12].toString()) : null);
		    mCUNormsValueDTO.setDecember(row[13] != null ? Float.parseFloat(row[13].toString()) : null);
		    mCUNormsValueDTO.setJanuary(row[14] != null ? Float.parseFloat(row[14].toString()) : null);
		    mCUNormsValueDTO.setFebruary(row[15] != null ? Float.parseFloat(row[15].toString()) : null);
		    mCUNormsValueDTO.setMarch(row[16] != null ? Float.parseFloat(row[16].toString()) : null);
		    
		    mCUNormsValueDTO.setFinancialYear(row[17].toString());
		    mCUNormsValueDTO.setRemarks(row[18] != null ? row[18].toString() : " ");
		    mCUNormsValueDTO.setCreatedOn(row[19] != null ? (Date) row[19] : null);
		    mCUNormsValueDTO.setModifiedOn(row[20] != null ? (Date) row[20] : null);
		    mCUNormsValueDTO.setMcuVersion(row[21] != null ? row[21].toString() : null);
		    mCUNormsValueDTO.setUpdatedBy(row[22] != null ? row[22].toString() : null);
		    mCUNormsValueDTO.setNormParameterTypeId(row[23] != null ? row[23].toString() : null);
		    mCUNormsValueDTO.setNormParameterTypeName(row[24] != null ? row[24].toString() : null);
		    mCUNormsValueDTO.setNormParameterTypeDisplayName(row[25] != null ? row[25].toString() : null);
		    mCUNormsValueDTO.setUOM(row[26] != null ? row[26].toString() : null);
		    mCUNormsValueDTOList.add(mCUNormsValueDTO);
		}

		return mCUNormsValueDTOList;
	}

	@Override
	public List<MCUNormsValueDTO> saveNormalOperationNormsData(List<MCUNormsValueDTO> mCUNormsValueDTOList) {
		
		for(MCUNormsValueDTO mCUNormsValueDTO:mCUNormsValueDTOList) {
			MCUNormsValue mCUNormsValue= new MCUNormsValue();
			if(mCUNormsValueDTO.getId()!=null || !mCUNormsValueDTO.getId().isEmpty()) {
				mCUNormsValue.setId(UUID.fromString(mCUNormsValueDTO.getId()));
				mCUNormsValue.setModifiedOn(new Date());
			}else {
				mCUNormsValue.setCreatedOn(new Date());
			}
			mCUNormsValue.setApril(Optional.ofNullable(mCUNormsValueDTO.getApril()).orElse(0.0f));
			mCUNormsValue.setMay(Optional.ofNullable(mCUNormsValueDTO.getMay()).orElse(0.0f));
			mCUNormsValue.setJune(Optional.ofNullable(mCUNormsValueDTO.getJune()).orElse(0.0f));
			mCUNormsValue.setJuly(Optional.ofNullable(mCUNormsValueDTO.getJuly()).orElse(0.0f));
			mCUNormsValue.setAugust(Optional.ofNullable(mCUNormsValueDTO.getAugust()).orElse(0.0f));
			mCUNormsValue.setSeptember(Optional.ofNullable(mCUNormsValueDTO.getSeptember()).orElse(0.0f));
			mCUNormsValue.setOctober(Optional.ofNullable(mCUNormsValueDTO.getOctober()).orElse(0.0f));
			mCUNormsValue.setNovember(Optional.ofNullable(mCUNormsValueDTO.getNovember()).orElse(0.0f));
			mCUNormsValue.setDecember(Optional.ofNullable(mCUNormsValueDTO.getDecember()).orElse(0.0f));
			mCUNormsValue.setJanuary(Optional.ofNullable(mCUNormsValueDTO.getJanuary()).orElse(0.0f));
			mCUNormsValue.setFebruary(Optional.ofNullable(mCUNormsValueDTO.getFebruary()).orElse(0.0f));
			mCUNormsValue.setMarch(Optional.ofNullable(mCUNormsValueDTO.getMarch()).orElse(0.0f));
			if(mCUNormsValueDTO.getSiteFkId()!=null) {
				mCUNormsValue.setSiteFkId(UUID.fromString(mCUNormsValueDTO.getSiteFkId()));
			}
			if(mCUNormsValueDTO.getPlantFkId()!=null) {
				mCUNormsValue.setPlantFkId(UUID.fromString(mCUNormsValueDTO.getPlantFkId()));
			}
			if(mCUNormsValueDTO.getVerticalFkId()!=null) {
				mCUNormsValue.setVerticalFkId(UUID.fromString(mCUNormsValueDTO.getVerticalFkId()));
			}
			if(mCUNormsValueDTO.getMaterialFkId()!=null) {
				mCUNormsValue.setMaterialFkId(UUID.fromString(mCUNormsValueDTO.getMaterialFkId()));
			}
			if(mCUNormsValueDTO.getNormParameterTypeId()!=null) {
				mCUNormsValue.setNormParameterTypeFkId(UUID.fromString(mCUNormsValueDTO.getNormParameterTypeId()));
			}
			
			mCUNormsValue.setFinancialYear(mCUNormsValueDTO.getFinancialYear());
			mCUNormsValue.setRemarks(mCUNormsValueDTO.getRemarks());
			mCUNormsValue.setMcuVersion("V1");
			mCUNormsValue.setUpdatedBy("System");
			
			System.out.println("Data Saved Succussfully");
			normalOperationNormsRepository.save(mCUNormsValue);
		}
		// TODO Auto-generated method stub
		return mCUNormsValueDTOList;
	}

	@Override
	@Transactional
	public int calculateExpressionConsumptionNorms(String year,String plantId) {
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		String storedProcedure=vertical.getName()+"_HMD_CalculateExpressionConsumptionNorms";
		System.out.println("storedProcedure"+storedProcedure);
		return executeDynamicUpdateProcedure(storedProcedure,year);
	}
	
	@Transactional
    public int executeDynamicUpdateProcedure(String procedureName, String finYear) {
        try {
            String sql = "EXEC " + procedureName + " @finYear = :finYear"; // Fixed syntax for SQL Server
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("finYear", finYear);

            return query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace(); // Log detailed exception for debugging
            return 0; // Return 0 if execution fails
        }
    }
	 

}
