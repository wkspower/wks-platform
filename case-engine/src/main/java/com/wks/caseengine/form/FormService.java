package com.wks.caseengine.form;

import java.util.List;

public interface FormService {

	public void save(final Form form) throws Exception;

	public Form getForm(final String formKey) throws Exception;

	public List<Form> find() throws Exception;

}
