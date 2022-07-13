package com.mmc.bpm.client.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.mmc.bpm.client.cases.instance.CaseInstanceCreator;
import com.mmc.bpm.client.cases.instance.CaseInstanceRetriever;

@ShellComponent
public class CaseInstanceCommand {

	@Autowired
	private CaseInstanceCreator caseInstanceCreator;

	@Autowired
	private CaseInstanceRetriever caseInstanceRetriever;

	@ShellMethod(value = "Create a Case Instance.")
	public String createCase(final String attributes) {
		return "Case Instance Created: " + caseInstanceCreator.create(attributes);

	}

	@ShellMethod(value = "Search Case Instances.")
	public String findCase() {

		StringBuffer caseInstancesStrings = new StringBuffer();

		caseInstanceRetriever.find()
				.forEach(o -> caseInstancesStrings.append(o).append(System.getProperty("line.separator")));

		return caseInstancesStrings.toString();
	}
}
