package com.wks.caseengine.pagination;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Document("caseInstances")
public class Case {

	@Id
	private String id;
	private String businessKey;
	private String caseDefinitionId;
	private String stage;
	private String status;

	public static List<Case> fixtures() {
		List<Case> items = new ArrayList<>();
		items.add(new Case("634d1eac797f75ecc4a10052", "92935", "1", "Data Collection", "ARCHIVED_CASE_STATUS"));
		items.add(new Case("634d1eb2797f75ecc4a10059", "1711", "1", "Data Collection", "WIP_CASE_STATUS"));
		items.add(new Case("634d1eb2797f75ecc4a1005e", "98228", "1", "Data Collection", "CLOSED_CASE_STATUS"));
		items.add(new Case("634d1eb3797f75ecc4a10063", "65422", "1", "Data Collection", "CLOSED_CASE_STATUS"));
		items.add(new Case("634d1ee0797f75ecc4a10076", "992", "1", "Data Collection", "CLOSED_CASE_STATUS"));

		items.add(new Case("634d227d797f75ecc4a10124", "40187", "1", "Data Collection", "CLOSED_CASE_STATUS"));
		items.add(new Case("634e8b48ee448937ec2a31ac", "95622", "1", "Data Collection", "CLOSED_CASE_STATUS"));
		items.add(new Case("634e8b4aee448937ec2a31b1", "88595", "1", "Data Collection", "CLOSED_CASE_STATUS"));
		items.add(new Case("634e8b5aee448937ec2a31cc", "63618", "1", "Stage 1", "CLOSED_CASE_STATUS"));
		items.add(new Case("634e8cb8ee448937ec2a32ce", "25003", "1", "Review", "CLOSED_CASE_STATUS"));
		return items;
	}

}
