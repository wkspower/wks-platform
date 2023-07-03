package com.wks.caseengine.pagination;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

import lombok.ToString;

@ToString
public class Post {

	@Id
	private String id;

	private String title;

	public Post() {
		super();
	}

	public Post(String id, String title) {
		this.id = id;
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public static List<Post> fixtures() {
		List<Post> items = new ArrayList<>();
		items.add(new Post("236UV30CwhgaMiGKYbC4xm4KkUg", "a"));
		items.add(new Post("236UVhAGEKHSHAt3HekgSuW7zNw", "b"));
		items.add(new Post("236UWIrPdkjY2FQ1pluzGm6amXs", "c"));
		items.add(new Post("236UWqgz6Hili6vAC3DE0Gh4Ihe", "d"));
		items.add(new Post("236UXdxv812J7t3AveqnudxG6SI", "d"));
		items.add(new Post("236UYXcEANLN2F8K5A0d45k2DQo", "e"));
		return items;
	}

}
