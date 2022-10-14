package com.wks.caseengine.db;

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
public class DatabaseConfig {

	private String databaseURL = "jdbc:h2:~/wks_bpm_interface;AUTO_SERVER=TRUE";
	private String mongoDatabase = "wks-mongo-database";

}
