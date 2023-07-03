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
public class Attachment {

	private String id;
	private String storage;
	private String dir;
	private String name;
	private String url;
	private int size;
	private String type;
	private String originalName;
	private String hash;
	private String userId;
	private String userName;
	private Date createdAt;

}