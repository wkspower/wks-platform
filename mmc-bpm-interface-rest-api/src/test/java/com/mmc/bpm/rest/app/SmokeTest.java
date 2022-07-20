package com.mmc.bpm.rest.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mmc.bpm.rest.server.CaseController;
import com.mmc.bpm.rest.server.ProcessController;

@SpringBootTest
public class SmokeTest {

	@Autowired
	private CaseController caseController;

	@Autowired
	private ProcessController processController;

	@Test
	public void contextLoads() throws Exception {
		assertThat(caseController).isNotNull();
		assertThat(processController).isNotNull();
	}
}
