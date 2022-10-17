package com.wks.caseengine.repository;

import com.wks.caseengine.form.Form;

public interface FormRepository extends Repository<Form> {

	void updateForm(final String formKey, final Form form) throws Exception;

}
