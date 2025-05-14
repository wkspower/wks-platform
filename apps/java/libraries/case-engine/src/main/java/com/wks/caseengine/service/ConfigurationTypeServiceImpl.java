package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;

import com.wks.caseengine.repository.ConfigurationTypeRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import com.wks.caseengine.entity.ConfigurationType;
import com.wks.caseengine.dto.ConfigurationTypeDTO;


@Service
public class ConfigurationTypeServiceImpl  implements ConfigurationTypeService{

    
	

	@Autowired
	private ConfigurationTypeRepository configurationTypeRepository;

    @Override
    public AOPMessageVM getConfigurationTypeData() {
       
         List<ConfigurationType> list =  configurationTypeRepository.findAllByOrderByDisplaySequenceAsc();
         List<ConfigurationTypeDTO> dtList = new ArrayList<>();
         for(ConfigurationType obj:list){
            ConfigurationTypeDTO dto = new ConfigurationTypeDTO();
            dto.setId(obj.getId().toString());
            dto.setName(obj.getName());
            dto.setDisplayName(obj.getDisplayName());
            dto.setDisplaySequence(obj.getDisplaySequence());
            dtList.add(dto);

         }

         AOPMessageVM aopMessageVM = new AOPMessageVM();
         Map<String, Object> finalResult = new HashMap<>();
			finalResult.put("configurationTypeList", list);

			// Set in response
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(finalResult);
			return aopMessageVM;




    }
}