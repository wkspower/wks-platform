package com.wks.caseengine.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.NormParametersRepository;

@Service
public class NormParametersServiceImpl implements NormParametersService {

	@Autowired
	private NormParametersRepository normParametersRepository;

	@Override
	public List<NormParameters> findAllByType(String type) {
		return normParametersRepository.findAllByType(type);
	}

	@Override
	public AOPMessageVM getAllGrades(String type) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			List<NormParameters> normParametersList = normParametersRepository.getAllGrades(type);
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(normParametersList);
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid type", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

}
