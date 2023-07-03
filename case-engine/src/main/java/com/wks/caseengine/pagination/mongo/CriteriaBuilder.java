package com.wks.caseengine.pagination.mongo;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;

import com.wks.caseengine.pagination.Args;
import com.wks.caseengine.pagination.Cursor;
import com.wks.caseengine.pagination.Cursor.Order;

public final class CriteriaBuilder {

	private String sortOp;
	private Direction sortDir;
	private Args args;
	private Cursor.Order cursorOrder;

	public CriteriaBuilder(Args args) {
		this.args = args;
		this.cursorOrder = Order.EMPTY;
	}

	public void setSortDir(Direction sortDir) {
		this.sortDir = sortDir;
	}

	public void setSortOp(String sortOp) {
		this.sortOp = sortOp;
	}

	public void setCursorOrder(Order useOrder) {
		this.cursorOrder = useOrder;
	}

	public boolean hasCursor() {
		String cursor = getCursor();
		return cursor != null && !cursor.isBlank();
	}

	public String getCursor() {
		if (cursorOrder.isBefore()) {
			String previous = args.cursors().previous();
			previous = DigestUtils.decode(previous);
			return previous;
		}

		if (cursorOrder.isAfter()) {
			String next = args.cursors().next();
			next = DigestUtils.decode(next);
			return next;
		}

		return null;
	}

	public Tuple<Criteria, List<Sort>> createCriteriaData() {
		String[] values = getCursorValues();

		String cursorId = values[0];
		String cursorSort = values[1];
		List<Sort> sorted = new ArrayList<>();
		Criteria criteria = new Criteria();

		if (cursorSort != null) {
			Criteria c1 = new Criteria();
			if ("lt".equals(sortOp)) {
				c1 = where(args.sort()).lt(cursorSort);
			} else if ("gt".equals(sortOp)) {
				c1 = where(args.sort()).gt(cursorSort);
			}

			Criteria c2 = new Criteria();
			if ("lt".equals(sortOp)) {
				c2 = where(args.sort()).is(cursorSort).and(args.key()).lt(CoreUtils.toObjectId(cursorId));
			} else if ("gt".equals(sortOp)) {
				c2 = where(args.sort()).is(cursorSort).and(args.key()).gt(CoreUtils.toObjectId(cursorId));
			}

			criteria.orOperator(c1, c2);
			sorted.add(Sort.by(sortDir, args.sort()));
		} else {
			if ("lt".equals(sortOp)) {
				criteria = where(args.key()).lt(CoreUtils.toObjectId(cursorId));
			} else if ("gt".equals(sortOp)) {
				criteria = where(args.key()).gt(CoreUtils.toObjectId(cursorId));
			}
		}

		sorted.add(Sort.by(sortDir, args.key()));

		return new Tuple<>(criteria, sorted);
	}

	public CriteriaDefinition createCriteriaPrevOrNext(Object id, Object sortValue) {
		Criteria c1 = new Criteria();
		if ("lt".equals(sortOp)) {
			c1 = where(args.sort()).lt(sortValue);
		} else if ("gt".equals(sortOp)) {
			c1 = where(args.sort()).gt(sortValue);
		}

		Criteria c2 = new Criteria();
		if ("lt".equals(sortOp)) {
			c2 = where(args.sort()).is(sortValue).and(args.key()).lt(CoreUtils.toObjectId(id));
		} else if ("gt".equals(sortOp)) {
			c2 = where(args.sort()).is(sortValue).and(args.key()).gt(CoreUtils.toObjectId(id));
		}

		Criteria criteria = new Criteria();
		criteria.orOperator(c1, c2);
		return criteria;
	}

	private String[] getCursorValues() {
		String cursor = getCursor();

		String[] values = cursor.split("|");

		if (values.length != 2) {
			return new String[] { cursor, null };
		}

		return values;
	}

}
