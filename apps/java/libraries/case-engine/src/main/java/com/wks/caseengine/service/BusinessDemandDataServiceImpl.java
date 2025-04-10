package com.wks.caseengine.service;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import java.util.UUID;
import com.wks.caseengine.entity.BusinessDemand;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.BusinessDemandDataDTO;
import com.wks.caseengine.repository.BusinessDemandDataRepository;
import com.wks.caseengine.repository.PlantsRepository;

@Service
public class BusinessDemandDataServiceImpl implements BusinessDemandDataService {

	@Autowired
	private BusinessDemandDataRepository businessDemandDataRepository;

	@Autowired
	private NormParametersService normParametersService;

	@Autowired
	private PlantsRepository plantsRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public AOPMessageVM getBusinessDemandData(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			String viewName = "vwScrn" + verticalName + "BusinessDemand";
			List<Object[]> obj = findByYearAndPlantFkId(year, UUID.fromString(plantId), viewName);
			System.out.println("obj" + obj);
			List<BusinessDemandDataDTO> businessDemandDataDTOList = new ArrayList<>();

			for (Object[] row : obj) {
				BusinessDemandDataDTO businessDemandDataDTO = new BusinessDemandDataDTO();

				businessDemandDataDTO.setId(row[0] != null ? row[0].toString() : null);
				businessDemandDataDTO.setRemark(row[1] != null ? row[1].toString() : null);
				businessDemandDataDTO.setJan(row[2] != null ? Float.parseFloat(row[2].toString()) : null);
				businessDemandDataDTO.setFeb(row[3] != null ? Float.parseFloat(row[3].toString()) : null);
				businessDemandDataDTO.setMarch(row[4] != null ? Float.parseFloat(row[4].toString()) : null);
				businessDemandDataDTO.setApril(row[5] != null ? Float.parseFloat(row[5].toString()) : null);
				businessDemandDataDTO.setMay(row[6] != null ? Float.parseFloat(row[6].toString()) : null);
				businessDemandDataDTO.setJune(row[7] != null ? Float.parseFloat(row[7].toString()) : null);
				businessDemandDataDTO.setJuly(row[8] != null ? Float.parseFloat(row[8].toString()) : null);
				businessDemandDataDTO.setAug(row[9] != null ? Float.parseFloat(row[9].toString()) : null);
				businessDemandDataDTO.setSep(row[10] != null ? Float.parseFloat(row[10].toString()) : null);
				businessDemandDataDTO.setOct(row[11] != null ? Float.parseFloat(row[11].toString()) : null);
				businessDemandDataDTO.setNov(row[12] != null ? Float.parseFloat(row[12].toString()) : null);
				businessDemandDataDTO.setDec(row[13] != null ? Float.parseFloat(row[13].toString()) : null);
				businessDemandDataDTO.setYear(row[13] != null ? row[14].toString() : null);
				businessDemandDataDTO.setPlantId(row[15] != null ? row[15].toString().toUpperCase() : null);
				businessDemandDataDTO.setNormParameterId(row[16] != null ? row[16].toString() : null);
				businessDemandDataDTO.setAvgTph(row[17] != null ? Float.parseFloat(row[17].toString()) : null);
				businessDemandDataDTO.setDisplayOrder(row[18] != null ? Integer.parseInt(row[18].toString()) : null);
				businessDemandDataDTO.setNormParameterTypeId(row[19] != null ? row[19].toString() : null);
				businessDemandDataDTO.setNormParameterTypeName(row[20] != null ? row[20].toString() : null);
				businessDemandDataDTO.setNormParameterTypeDisplayName(row[21] != null ? row[21].toString() : null);

				businessDemandDataDTOList.add(businessDemandDataDTO);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(businessDemandDataDTOList);
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM saveBusinessDemandData(List<BusinessDemandDataDTO> businessDemandDataDTOList) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<BusinessDemand> businessDemandList = new ArrayList<>();
		try {
			for (BusinessDemandDataDTO businessDemandDataDTO : businessDemandDataDTOList) {
				BusinessDemand businessDemand = new BusinessDemand();
				businessDemand.setApril(businessDemandDataDTO.getApril());
				businessDemand.setAug(businessDemandDataDTO.getAug());
				businessDemand.setAvgTph(businessDemandDataDTO.getAvgTph());
				businessDemand.setDec(businessDemandDataDTO.getDec());
				businessDemand.setFeb(businessDemandDataDTO.getFeb());

				if (businessDemandDataDTO.getId() == null || businessDemandDataDTO.getId().contains("#")) {
					businessDemand.setId(null);
				} else {
					businessDemand.setId(UUID.fromString(businessDemandDataDTO.getId()));
				}

				businessDemand.setJan(businessDemandDataDTO.getJan());
				businessDemand.setJuly(businessDemandDataDTO.getJuly());
				businessDemand.setJune(businessDemandDataDTO.getJune());
				businessDemand.setMarch(businessDemandDataDTO.getMarch());
				businessDemand.setMay(businessDemandDataDTO.getMay());

				if (businessDemandDataDTO.getNormParameterId() != null
						&& !businessDemandDataDTO.getNormParameterId().isEmpty()) {
					businessDemand.setNormParameterId(UUID.fromString(businessDemandDataDTO.getNormParameterId()));
				}

				businessDemand.setNov(businessDemandDataDTO.getNov());
				businessDemand.setOct(businessDemandDataDTO.getOct());

				if (businessDemandDataDTO.getPlantId() != null && !businessDemandDataDTO.getPlantId().isEmpty()) {
					businessDemand.setPlantId(UUID.fromString(businessDemandDataDTO.getPlantId()));
					businessDemand.setRemark(businessDemandDataDTO.getRemark());
					businessDemand.setSep(businessDemandDataDTO.getSep());
					businessDemand.setYear(businessDemandDataDTO.getYear());
					if (businessDemandDataDTO.getSiteFKId() != null) {
						businessDemand.setSiteFKId(UUID.fromString(businessDemandDataDTO.getSiteFKId()));
					}
					if (businessDemandDataDTO.getVerticalFKId() != null) {
						businessDemand.setVerticalFKId(UUID.fromString(businessDemandDataDTO.getVerticalFKId()));
					}
					businessDemandDataRepository.save(businessDemand);
					businessDemandList.add(businessDemand);
				}
			} // TODO Auto-generated method stub

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data saved successfully");
			aopMessageVM.setData(businessDemandList);
			return aopMessageVM;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	@Override
	public List<BusinessDemandDataDTO> editBusinessDemandData(List<BusinessDemandDataDTO> businessDemandDataDTOList) {

		for (BusinessDemandDataDTO businessDemandDataDTO : businessDemandDataDTOList) {
			BusinessDemand businessDemand = new BusinessDemand();

			businessDemand.setApril(businessDemandDataDTO.getApril());
			businessDemand.setAug(businessDemandDataDTO.getAug());
			businessDemand.setAvgTph(businessDemandDataDTO.getAvgTph());
			businessDemand.setDec(businessDemandDataDTO.getDec());
			businessDemand.setFeb(businessDemandDataDTO.getFeb());
			if (businessDemandDataDTO.getId() != null) {
				businessDemand.setId(UUID.fromString(businessDemandDataDTO.getId()));
			}
			businessDemand.setJan(businessDemandDataDTO.getJan());
			businessDemand.setJuly(businessDemandDataDTO.getJuly());
			businessDemand.setJune(businessDemandDataDTO.getJune());
			businessDemand.setMarch(businessDemandDataDTO.getMarch());
			businessDemand.setMay(businessDemandDataDTO.getMay());
			businessDemand.setNormParameterId(UUID.fromString(businessDemandDataDTO.getNormParameterId()));
			businessDemand.setNov(businessDemandDataDTO.getNov());
			businessDemand.setOct(businessDemandDataDTO.getOct());
			businessDemand.setPlantId(UUID.fromString(businessDemandDataDTO.getPlantId()));
			businessDemand.setRemark(businessDemandDataDTO.getRemark());
			businessDemand.setSep(businessDemandDataDTO.getSep());
			businessDemand.setYear(businessDemandDataDTO.getYear());
			businessDemandDataRepository.save(businessDemand);
		}
		// TODO Auto-generated method stub
		return businessDemandDataDTOList;
	}

	@Override
	public BusinessDemandDataDTO deleteBusinessDemandData(UUID id) {
		// businessDemandDataRepository.softDelete(UUID.fromString(businessDemandDataDTO.getId()));

		BusinessDemand businessDemand = new BusinessDemand();
		businessDemand.setId(id);
		businessDemandDataRepository.delete(businessDemand);
		return null;
	}

	public List<Object[]> findByYearAndPlantFkId(String year, UUID plantFkId, String viewName) {
		String sql = "SELECT " + "Id, Remark, Jan, Feb, March, April, May, June, July, Aug, Sep, Oct, Nov, Dec, "
				+ "Year, Plant_FK_Id, NormParameters_FK_Id, AvgTPH, NormTypeDisplayOrder, "
				+ "NormParameterTypeId, NormParameterTypeName, NormParameterTypeDisplayName, "
				+ "CreatedOn, ModifiedOn, UpdatedBy, IsDeleted, MaterialDisplayOrder, " + "Site_FK_Id, Vertical_FK_Id "
				+ "FROM " + viewName + " " + "WHERE (Year = :year OR Year IS NULL) " + "AND Plant_FK_Id = :plantFkId "
				+ "ORDER BY NormTypeDisplayOrder, MaterialDisplayOrder";

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("year", year);
		query.setParameter("plantFkId", plantFkId);

		return query.getResultList();
	}

}
