package com.wks.caseengine.form;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.repository.DataRepository;

@Component
public class FormServiceImpl implements FormService {
	
	@Autowired
	private DataRepository dataRepository;

	@Override
	public void save(Form form) throws Exception {
		dataRepository.saveForm(form);
	}

	@Override
	public Form getForm(String formKey) throws Exception {
		return dataRepository.getForm(formKey);
	}

	@Override
	public List<Form> find() throws Exception {
		return dataRepository.findForms();
	}

}
