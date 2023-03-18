package com.wks.caseengine.cases.instance;

import java.util.Date;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class Comment {

	private String id;
	
	private String body;
	
	private String userName;
	
	private String userId;
	
	private String parentId;
	
	private Date createdAt;
	
	private String caseId;
	
}