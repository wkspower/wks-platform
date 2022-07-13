package com.mmc.bpm.client.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.mmc.bpm.client.process.instance.ProcessInstanceService;

@ShellComponent
public class ProcessInstanceCommand {

	@Autowired
	private ProcessInstanceService processInstanceCreator;

	@ShellMethod(value = "Create a Process Instance.")
	public String createProcessInstance(final String procDefId) {
		return "Process Instance Created: " + processInstanceCreator.create(procDefId).toString();
	}

	@ShellMethod(value = "Delete a Process Instance.")
	public String deleteProcessInstance(final String processInstanceId) {
		processInstanceCreator.delete(processInstanceId);
		return "Process Instance Deleted";
	}
	
}
