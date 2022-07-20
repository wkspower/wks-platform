package com.mmc.bpm.shell.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.mmc.bpm.client.cases.instance.CaseInstanceService;

@ShellComponent
public class CaseCommand {

	@Autowired
	private CaseInstanceService caseInstanceService;

	@ShellMethod(value = "Create a Case Instance.")
	public String createCase(final String attributes) throws Exception {
		// TODO parse json string to List
		return "Case Instance Created: " + caseInstanceService.create(null);

	}

	@ShellMethod(value = "Update Case Instance Status")
	public String updateCaseStatus(final String businessKey, String status) throws Exception {
		caseInstanceService.updateStatus(businessKey, status);
		return "Case Status Updated";
	}

	@ShellMethod(value = "Delete a Case Instance")
	public String deleteCase(final String businessKey) throws Exception {
		caseInstanceService.delete(businessKey);
		return "Case Deleted";
	}

	@ShellMethod(value = "Search Case Instances.")
	public String findCase() throws Exception {

		StringBuffer caseInstancesStrings = new StringBuffer();

		caseInstanceService.find()
				.forEach(o -> caseInstancesStrings.append(o).append(System.getProperty("line.separator")));

		return caseInstancesStrings.toString();
	}
}
