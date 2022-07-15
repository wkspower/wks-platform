package com.mmc.bpm.client.cases.businesskey;

import com.mmc.bpm.engine.model.spi.BusinessKey;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class GenericBusinessKey implements BusinessKey {

	private String businessKey;

}
