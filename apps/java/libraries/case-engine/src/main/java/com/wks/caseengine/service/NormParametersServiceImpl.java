 package com.wks.caseengine.service;

 import java.util.List;

 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;

 import com.wks.caseengine.entity.NormParameters;
 import com.wks.caseengine.repository.NormParametersRepository;

 @Service
 public class NormParametersServiceImpl implements NormParametersService {
	
 	@Autowired
 	private NormParametersRepository normParametersRepository;

 	@Override
 	public List<NormParameters> findAllByType(String type) {
 		return	normParametersRepository.findAllByType(type);
 	}

 }
