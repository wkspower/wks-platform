package com.mmc.bpm.shell.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.mmc.bpm.client.cases.businesskey.GenericBusinessKey;
import com.mmc.bpm.client.cases.instance.CaseInstanceService;

@ShellComponent
public class CaseCommand {

	@Autowired
	private CaseInstanceService caseInstanceService;

	@ShellMethod(value = "Create a Case Instance.")
	public String createCase(final String attributes) {
		return "Case Instance Created: " + caseInstanceService.create(attributes);

	}

	@ShellMethod(value = "Delete a Case Instance")
	public String deleteCase(final String businessKey) {
		caseInstanceService.delete(GenericBusinessKey.builder().businessKey(businessKey).build());
		return "Case Deleted";
	}

	@ShellMethod(value = "Search Case Instances.")
	public String findCase() {

		StringBuffer caseInstancesStrings = new StringBuffer();

		caseInstanceService.find()
				.forEach(o -> caseInstancesStrings.append(o).append(System.getProperty("line.separator")));

		return caseInstancesStrings.toString();
	}
}
