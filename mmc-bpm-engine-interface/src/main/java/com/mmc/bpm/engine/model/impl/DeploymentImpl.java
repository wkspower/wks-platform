package com.mmc.bpm.engine.model.impl;

import com.mmc.bpm.engine.model.spi.Deployment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author victor.franca
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentImpl implements Deployment {

	private String id;

}
