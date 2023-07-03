package com.wks.caseengine.pagination;

public final class Cursor {

	private Order order;
	private Object previous;
	private Object next;

	private Cursor(Object previous, Object next) {
		this.previous = previous;
		this.next = next;
	}

	public static Cursor of(String before, String after) {
		Cursor c = Cursor.empty();

		if (before != null && !before.isBlank()) {
			return c.previous(before);
		}

		if (after != null && !after.isBlank()) {
			return c.next(after);
		}

		return null;
	}

	public static Cursor empty() {
		return new Cursor(null, null);
	}

	public Cursor previous(String value) {
		this.order = Cursor.Order.BEFORE;
		this.previous = value;
		return this;
	}

	public Cursor next(String value) {
		this.order = Cursor.Order.AFTER;
		this.next = value;
		return this;
	}

	public boolean hasPrevious() {
		return previous != null && !previous.toString().isBlank() && order.isBefore();
	}

	public boolean hasNext() {
		return next != null && !next.toString().isBlank() && order.isAfter();
	}

	public String previous() {
		return previous != null ? previous.toString() : null;
	}

	public String next() {
		return next != null ? next.toString() : null;
	}

	public enum Order {

		AFTER, BEFORE, EMPTY;

		public boolean isAfter() {
			return this.equals(AFTER);
		}

		public boolean isBefore() {
			return this.equals(BEFORE);
		}

	}

}
