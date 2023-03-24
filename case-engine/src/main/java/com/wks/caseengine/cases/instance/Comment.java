package com.wks.caseengine.cases.instance;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

	private String id;
	
	private String body;
	
	private String userName;
	
	private String userId;
	
	private String parentId;
	
	private Date createdAt;
	
	private String caseId;
	
}