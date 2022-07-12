package com.mmc.bpm.engine.app;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.shell.jline.PromptProvider;

@SpringBootApplication
@ComponentScan(basePackages = { "com.mmc.bpm" })
public class CaseManagementApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(CaseManagementApplication.class, args);
	}

	@Bean
	public PromptProvider myPromptProvider() {
		return () -> new AttributedString("mmc-auto-cli:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
	}

}
