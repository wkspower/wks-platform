package com.mmc.bpm.client.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.mmc.bpm.client.process.instance.ProcessInstanceCreator;

@ShellComponent
public class ProcessInstanceCommand {

	@Autowired
	private ProcessInstanceCreator processInstanceCreator;

	@ShellMethod(value = "Creates a Process Instance.")
	public String createProcessInstance(final String procDefId) {
		return "Process Instance Created: " + processInstanceCreator.create(procDefId).toString();
	}

}
