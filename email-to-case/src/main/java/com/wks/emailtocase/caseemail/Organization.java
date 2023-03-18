package com.wks.emailtocase.caseemail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Organization {

	private Object _id;
	
	private String mailReceiveApiKey;

}
