package com.mmc.bpm.engine.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.mmc.bpm.engine.cases.instance.CaseInstanceCreator;
import com.mmc.bpm.engine.cases.instance.CaseInstanceRetriever;

@ShellComponent
public class CaseInstanceCommand {

	@Autowired
	private CaseInstanceCreator caseInstanceCreator;

	@Autowired
	private CaseInstanceRetriever caseInstanceRetriever;

	@ShellMethod(value = "Creates a Case Instance.")
	public String createCase(String attributes) {
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
