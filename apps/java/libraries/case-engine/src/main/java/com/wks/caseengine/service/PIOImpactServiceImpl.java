package com.wks.caseengine.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wks.caseengine.dto.PIOImpactDTO;
import com.wks.caseengine.entity.PIOImpact;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PIOImpactRepository;

@Service
public class PIOImpactServiceImpl implements PIOImpactService {
	
	@Autowired
	private PIOImpactRepository pioImpactRepository;

	@Override
	public AOPMessageVM getPIOImpact(String year, String plantId) {
		
		List<PIOImpactDTO> pioImpactDTOs=new ArrayList<PIOImpactDTO>();
		List<PIOImpact> pioImpacts=	pioImpactRepository.findByPlantIdAndAopYear(UUID.fromString(plantId), year);
		try {
			for(PIOImpact pioImpact:pioImpacts) {
				PIOImpactDTO pioImpactDTO = new PIOImpactDTO();
				pioImpactDTO.setDescription(pioImpact.getDescription());
				pioImpactDTO.setEndMonth(pioImpact.getEndMonth());
				pioImpactDTO.setId(pioImpact.getId().toString());
				if(pioImpact.getRemarks()!=null) {
					pioImpactDTO.setRemarks(pioImpact.getRemarks());
				}else {
					pioImpactDTO.setRemarks("");
				}
				pioImpactDTO.setStartMonth(pioImpact.getStartMonth());
				pioImpactDTO.setValue(pioImpact.getValue());
				pioImpactDTOs.add(pioImpactDTO);
			}
		}catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
		
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(pioImpactDTOs);
		aopMessageVM.setMessage("Data Fetched successfully");
		return aopMessageVM;
	}

	@Override
	public AOPMessageVM updatePIOImpact(String year, String plantId, List<PIOImpactDTO> pioImpactDTOs) {
		List<PIOImpact> pioImpacts = new ArrayList<PIOImpact>();
		try {
			for(PIOImpactDTO pioImpactDTO :pioImpactDTOs) {
				PIOImpact pioImpact = null;
				if(pioImpactDTO.getId()!=null) {
					Optional<PIOImpact> pioImpactOpt=	pioImpactRepository.findById(UUID.fromString(pioImpactDTO.getId()));
					if(pioImpactOpt.isPresent()) {
						pioImpact=pioImpactOpt.get();
					}
				}else {
					pioImpact = new PIOImpact();
				}
				pioImpact.setDescription(pioImpactDTO.getDescription());
				pioImpact.setEndMonth(pioImpactDTO.getEndMonth());
				pioImpact.setRemarks(pioImpactDTO.getRemarks());
				pioImpact.setStartMonth(pioImpactDTO.getStartMonth());
				pioImpact.setValue(pioImpactDTO.getValue());
				pioImpacts.add(pioImpactRepository.save(pioImpact));
			}
		}catch(Exception e) {
			throw new RuntimeException("Failed to fetch data", e);
		}
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(pioImpacts);
		aopMessageVM.setMessage("Data updated successfully");
		// TODO Auto-generated method stub
		return aopMessageVM;
	}

			
}
