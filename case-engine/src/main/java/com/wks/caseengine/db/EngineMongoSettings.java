package com.wks.caseengine.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class EngineMongoSettings {

	@Value("${spring.data.mongodb.uri}")
	private String uri;

	@Value("${spring.data.mongodb.database}")
	private String dataBaseName;

	@Value("${spring.data.mongodb.database.min-pool-size:1}")
	private int minPool;

	@Value("${spring.data.mongodb.database.max-pool-size:10}")
	private int maxPool;

	@Value("${spring.data.mongodb.database.max-connection-idle-time:3000}")
	private int maxConnectionIdleTime;

	@Value("${spring.data.mongodb.database.max-connection-life-time:3000}")
	private int maxConnectionLifeTime;

}
