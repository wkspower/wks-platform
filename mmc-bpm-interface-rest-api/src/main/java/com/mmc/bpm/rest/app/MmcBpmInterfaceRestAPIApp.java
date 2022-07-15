package com.mmc.bpm.rest.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.mmc.bpm"})
public class MmcBpmInterfaceRestAPIApp {

	public static void main(final String[] args) {
		SpringApplication.run(MmcBpmInterfaceRestAPIApp.class, args);
	}

}
