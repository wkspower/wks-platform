package com.wks.caseengine.form;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.repository.FormRepository;

@Component
public class FormServiceImpl implements FormService {

	@Autowired
	private FormRepository repository;

	@Override
	public void save(Form form) throws Exception {
		repository.save(form);
	}

	@Override
	public Form get(String formKey) throws Exception {
		return repository.get(formKey);
	}

	@Override
	public List<Form> find() throws Exception {
		return repository.find();
	}

	@Override
	public void delete(String formKey) throws Exception {
		repository.delete(formKey);
	}

	@Override
	public void update(final String formKey, final Form form) throws Exception {
		repository.update(formKey, form);
	}

}
