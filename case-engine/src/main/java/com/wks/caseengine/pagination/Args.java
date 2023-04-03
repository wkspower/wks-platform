package com.wks.caseengine.pagination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;

public final class Args {

	private int limit;
	private Direction dir;
	private String key;
	private Cursor cursor;
	private List<Criteria> criterias;
	private List<String> fields;
	private String sort;

	private Args(int limit, Direction dir) {
		this.limit = limit;
		this.dir = dir;
		this.criterias = new ArrayList<>();
		this.fields = new ArrayList<>();
		this.cursor = Cursor.empty();
	}

	public static Args of(int limit) {
		return new Args(limit, Direction.ASC);
	}
	
	public static Args of(Direction dir) {
		return new Args(5, dir);
	}
	
	public static Args of(Args copy, Cursor.Order order, String value) {
		return Args.of(copy.limit).cursor(order, value, copy.dir);
	}

	public Args fields(String field, String ...fields) {
		if (fields != null && !field.isBlank()) {
			this.fields.add(field);
		}
		
		if (fields != null) {
			this.fields.addAll(Arrays.asList(fields));
		}
		
		return this;
	}
	
	public Args key(String key) {
		this.key = key;
		return this;
	}
	
	public Args sort(String sort) {
		this.sort = sort;
		return this;
	}
	
	public Args cursor(Cursor cursor, Direction dir) {
		this.cursor = cursor == null ? Cursor.empty() : cursor;
		this.dir = dir;
		return this;
	}
	
	public Args cursor(Cursor.Order order, String value, Direction dir) {
		this.cursor = order.isAfter() ? cursor.next(value) : cursor.previous(value);
		this.dir = dir;
		return this;
	}

	public Args criteria(Consumer<Expr> expr) {
		expr.accept(new Expr() {
			@Override
			public void add(Criteria c) {
				criterias.add(c);
			}
		});
		return this;
	}
	
	public String key() {
		return Optional.ofNullable(key).orElse("_id");
	}
	
	public int limit() {
		return limit;
	}
	
	public Direction dir() {
		return dir;
	}
	
	public List<Criteria> criterias() {
		return criterias;
	}
	
	public String sort() {
		return sort;
	}

	public List<String> fields() {
		return fields;
	}

	public Cursor cursors() {
		return cursor;
	}
	
	public interface Expr {
		void add(Criteria c);
	}

	public void validate() {
		if (!fields.isEmpty()) {
			if (key != null && !fields.contains(key)) {
				throw new PaginationException("PersistentProperty '"+key+"' wasn't declared in fields");
			}
			
			if (sort != null && !fields.contains(sort)) {
				throw new PaginationException("PersistentProperty '"+sort+"' wasn't declared in fields");
			}
		}
	}
	
}
