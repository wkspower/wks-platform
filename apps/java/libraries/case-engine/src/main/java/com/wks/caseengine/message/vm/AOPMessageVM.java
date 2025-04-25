package com.wks.caseengine.message.vm;

import org.springframework.context.annotation.Configuration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AOPMessageVM {
	
	private  int code;
	private String message ;
	private Object data ;

}
