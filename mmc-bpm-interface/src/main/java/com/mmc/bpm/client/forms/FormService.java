package com.mmc.bpm.client.forms;

import com.mmc.bpm.engine.model.spi.CamundaForm;

public interface FormService {

	public CamundaForm getTaskForm(String taskId);

}
