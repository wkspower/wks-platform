package com.wks.caseengine.service;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.AOPMaintenanceDesignRemarksDTO;
import com.wks.caseengine.entity.AOPMaintenanceDesignBasis;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AOPMaintenanceDesignBasisRepository;
import com.wks.caseengine.utility.Utility;

import java.util.UUID;


@Service
public class AOPMaintenanceDesignBasisServiceImpl implements AOPMaintenanceDesignBasisService {

	@Autowired
	private AOPMaintenanceDesignBasisRepository aopMaintenanceDesignBasisRepository;
	
	
	@Override
	public AOPMessageVM getMaintenanceDesignBasis(String plantId, String year) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<AOPMaintenanceDesignRemarksDTO> aopMaintenanceDesignRemarksDTOs = new ArrayList<AOPMaintenanceDesignRemarksDTO>();
		try {
			AOPMaintenanceDesignBasis row = aopMaintenanceDesignBasisRepository.getData(UUID.fromString(plantId),year);
				if(row!=null) {
					AOPMaintenanceDesignRemarksDTO aopMaintenanceDesignRemarksDTO = new AOPMaintenanceDesignRemarksDTO();
					aopMaintenanceDesignRemarksDTO.setId(row.getId());
					aopMaintenanceDesignRemarksDTO.setPlantFkId(row.getPlantFkId());
					aopMaintenanceDesignRemarksDTO.setAopYear(row.getAopYear());
					aopMaintenanceDesignRemarksDTO.setSummary(row.getSummary());
					aopMaintenanceDesignRemarksDTO.setUpdatedBy(row.getUpdatedBy());
					aopMaintenanceDesignRemarksDTO.setUpdatedDateTime(row.getUpdatedDateTime());
					aopMaintenanceDesignRemarksDTOs.add(aopMaintenanceDesignRemarksDTO);

				}
			aopMessageVM.setCode(200);
			aopMessageVM.setData(aopMaintenanceDesignRemarksDTOs);
			aopMessageVM.setMessage("Data fetched successfully");
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM updateMaintenanceDesignBasis(String plantId, String year,
			String summary) {
		try {
			AOPMaintenanceDesignBasis aopMaintenanceDesignBasis = aopMaintenanceDesignBasisRepository.getData(UUID.fromString(plantId),year);

			if(aopMaintenanceDesignBasis==null) {
				 aopMaintenanceDesignBasis = new AOPMaintenanceDesignBasis();
				 aopMaintenanceDesignBasis.setAopYear(year);
				 aopMaintenanceDesignBasis.setPlantFkId(UUID.fromString(plantId));
			}
			aopMaintenanceDesignBasis.setSummary(summary);
			aopMaintenanceDesignBasis.setUpdatedBy(Utility.getUserName());
			aopMaintenanceDesignBasis.setUpdatedDateTime(new java.util.Date());

			aopMaintenanceDesignBasisRepository.save(aopMaintenanceDesignBasis);
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(aopMaintenanceDesignBasis);
			aopMessageVM.setMessage("Data updated successfully");
			// TODO Auto-generated method stub
			return aopMessageVM;

		}catch (Exception ex) {
			throw new RestInvalidArgumentException("summary", ex);
		}
	}

	
}
