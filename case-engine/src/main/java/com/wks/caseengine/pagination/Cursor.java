package com.wks.caseengine.pagination;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import lombok.ToString;

@ToString
public class Cursor {
	
	private static final String DEFAULT_LIMIT = "5";
	private static final String DEFAULT_DIR = "desc";

	private String token;
	private String sort;
	private String dir;
	private int limit;
	
	private Cursor(String token, String sort, String dir, String limit) {
		this(token, sort, dir,  toInteger(limit, 5));
	}
	
	private Cursor(String token, String sort, String dir, int limit) {
		this.token = token;
		this.sort = sort;
		this.dir = dir;
		this.limit = limit;
	}

	public static Cursor of(String token) {
		return new Cursor(token, null, DEFAULT_DIR, DEFAULT_LIMIT);
	}
	
	public static Cursor of(String token, String sort, String dir) {
		return new Cursor(token, sort, dir, DEFAULT_LIMIT);
	}
	
	public static Cursor of(String token, String sort, String dir, String limit) {
		return new Cursor(token, sort, dir, limit);
	}
	
	public static Cursor of(Cursor cursor, String token) {
		return new Cursor(token, cursor.sort(), cursor.dir(), cursor.limit());
	}
	
	public static Cursor of(Cursor cursor, int limit) {
		return new Cursor(cursor.token(), cursor.sort(), cursor.dir(), limit);
	}
	
	public static Cursor of(int limit) {
		return new Cursor(null, null, DEFAULT_DIR, limit);
	}
	
	public static Cursor of(String sort, Sort.Direction dir, int limit) {
		String sortDir = "";
		
		if (dir.isAscending()) {
			sortDir =  "asc";
		} else	if (dir.isDescending()) {
			sortDir =  "desc";
		}
		
		return new Cursor(null, sort, sortDir, limit);
	}
	
	public static Cursor empty() {
		return new Cursor(null, null, DEFAULT_DIR, DEFAULT_LIMIT);
	}

	public boolean isSorted() {
		return !Optional.ofNullable(sort).isEmpty();
	}

	public boolean hasSortedWith(List<String> sortableFields) {
		return isSorted() && 
						Optional.ofNullable(sortableFields)
									  .orElse(Collections.emptyList())
									  .stream()
									  .filter(s -> s.equals(sort))
									  .count() > 0;
	}
	
	public int limit() {
		return limit;
	}
	
	public String dir() {
		return dir;
	}
	
	public String sort() {
		return sort;
	}
	
	public String token() {
		return token;
	}

	public boolean hasToken() {
		return token != null && !token.isBlank();
	}

	public Sort toSort() {
		Direction sortDir = strToDir();
		
		if (isSorted()) {
			return Sort.by(sortDir, sort).and(Sort.by(sortDir, "_id"));
		}
		
		return Sort.by(sortDir, "_id");
	}

	public boolean isDirectionDesc() {
		return  Sort.Direction.DESC.equals(strToDir());
	}
	
	public boolean isDirectionAsc() {
		return  Sort.Direction.ASC.equals(strToDir());
	}

	private static int toInteger(String src, int defaultValue) {
		if (src == null || src.isBlank()) {
			return defaultValue;
		}
		
		try {
			return Integer.valueOf(src);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	private Direction strToDir() {
		if (dir == null || dir.isBlank()) {
			return Direction.fromString(DEFAULT_DIR);
		}
		
		return Direction.fromString(dir);
	}

}
