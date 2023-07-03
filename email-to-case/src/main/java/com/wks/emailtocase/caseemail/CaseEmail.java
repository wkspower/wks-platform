package com.wks.emailtocase.caseemail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaseEmail {

	private Object _id;

	private String to;
	private String from;
	private String subject;
	private String text;
	private String html;

	private String caseInstanceBusinessKey;

	private String caseDefinitionId;

	private CaseEmailType caseEmailType;

}
