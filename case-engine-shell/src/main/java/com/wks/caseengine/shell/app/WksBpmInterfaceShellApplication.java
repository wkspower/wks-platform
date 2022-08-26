package com.wks.caseengine.shell.app;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.shell.jline.PromptProvider;

@SpringBootApplication
@ComponentScan(basePackages = { "com.wks.bpm", "com.wks.caseengine" })
public class WksBpmInterfaceShellApplication {

	public static void main(final String[] args) throws Exception {
		SpringApplication.run(WksBpmInterfaceShellApplication.class, args);
	}

	@Bean
	public PromptProvider myPromptProvider() {
		return () -> new AttributedString("case-engine-cli:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
	}

}
