package com.wks.caseengine.repository;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataBaseConfig {

	private String databaseURL = "jdbc:h2:~/wks_bpm_interface;AUTO_SERVER=TRUE";
	private String mongoDatabase = "wks-mongo-database";

}
