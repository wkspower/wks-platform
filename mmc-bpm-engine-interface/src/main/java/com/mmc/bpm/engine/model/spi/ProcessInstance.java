package com.mmc.bpm.engine.model.spi;

/**
 * @author victor.franca
 *
 */
public interface ProcessInstance {

	public String getId();

	public String getBusinessKey();

	public String getDefinitionId();

	public String getCaseInstanceId();

	public Boolean getEnded();

	public Boolean getSuspended();

	public String getTenantId();

}
