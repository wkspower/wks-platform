package com.wks.caseengine.pagination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.wks.caseengine.pagination.mongo.CoreUtils;
import com.wks.caseengine.pagination.mongo.MongoCursorPagination;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class MongoCursorPaginationIT {

	@Autowired
	private MongoOperations operations;

	@BeforeEach
	public void setup() {
		operations.insert(Post.fixtures(), Post.class);
		operations.insert(Case.fixtures(), Case.class);
	}

	@AfterEach
	public void teadown() {
		operations.remove(new Query(), Post.class);
		operations.remove(new Query(), Case.class);
	}

	@Test
	void shouldGetResultsSortedByIdDescWithNextCursorUsingTwo() {
		String cursor = CoreUtils.encode("236UVhAGEKHSHAt3HekgSuW7zNw|a");

		MongoCursorPagination pagination = new MongoCursorPagination(operations);

		Args args = Args.of(1).key("_id").sort("title").cursor(Cursor.Order.BEFORE, cursor, Sort.Direction.DESC);

		PageResult<Post> results = pagination.executeQuery(args, Post.class);

		assertNotNull(results);
		assertEquals(1, results.size());
		assertTrue(results.hasNext());
	}

	@Test
	void shouldGetResultsSortedByIdAscWithNextCursorUsingTwo() {
		String cursor = CoreUtils.encode("236UV30CwhgaMiGKYbC4xm4KkUg|a");

		MongoCursorPagination pagination = new MongoCursorPagination(operations);

		Args args = Args.of(3).key("_id").sort("title").cursor(Cursor.Order.AFTER, cursor, Sort.Direction.ASC);

		PageResult<Post> results = pagination.executeQuery(args, Post.class);

		assertNotNull(results);
		assertEquals(3, results.size());
		assertTrue(results.hasNext());
	}

	@Test
	void shouldGetResultsSortedByIdAsc() {
		MongoCursorPagination pagination = new MongoCursorPagination(operations);

		Args args = Args.of(3).key("_id");

		PageResult<Post> results = pagination.executeQuery(args, Post.class);

		assertNotNull(results);
		assertEquals(3, results.size());
		assertTrue(results.hasNext());
	}

	@Test
	void shouldGetResultsSortedByIdAscWithNextCursor() {
		String cursor = CoreUtils.encode("236UWIrPdkjY2FQ1pluzGm6amXs");

		MongoCursorPagination pagination = new MongoCursorPagination(operations);

		Args args = Args.of(3).key("_id").cursor(Cursor.Order.AFTER, cursor, Sort.Direction.ASC);

		PageResult<Post> results = pagination.executeQuery(args, Post.class);

		assertNotNull(results);
		assertEquals(3, results.size());
		assertFalse(results.hasNext());
	}

	@Test
	void shouldGetResultsSortedByIdDesc() {
		MongoCursorPagination pagination = new MongoCursorPagination(operations);

		Args args = Args.of(3).key("_id").cursor(null, Sort.Direction.DESC);

		PageResult<Post> results = pagination.executeQuery(args, Post.class);

		assertNotNull(results);
		assertEquals(3, results.size());
		assertTrue(results.hasNext());
		assertFalse(results.hasPrevious());
	}

	@Test
	void shouldGetResultsSortedByIdDescWithNextCursor() {
		String cursor = CoreUtils.encode("236UWqgz6Hili6vAC3DE0Gh4Ihe");

		MongoCursorPagination pagination = new MongoCursorPagination(operations);

		Args args = Args.of(3).key("_id").cursor(Cursor.Order.AFTER, cursor, Sort.Direction.DESC);

		PageResult<Post> results = pagination.executeQuery(args, Post.class);

		assertNotNull(results);
		assertEquals(3, results.size());
		assertFalse(results.hasNext());
		assertTrue(results.hasPrevious());
	}

	@Test
	void shouldGetResultsSortedByIdDescWithManyNextCursor() {
		String cursor = CoreUtils.encode("236UYXcEANLN2F8K5A0d45k2DQo");

		MongoCursorPagination pagination = new MongoCursorPagination(operations);

		PageResult<Post> results = pagination.executeQuery(
				Args.of(1).key("_id").cursor(Cursor.Order.AFTER, cursor, Sort.Direction.DESC), Post.class);
		results = pagination.executeQuery(
				Args.of(1).key("_id").cursor(Cursor.Order.AFTER, results.next(), Sort.Direction.DESC), Post.class);
		results = pagination.executeQuery(
				Args.of(1).key("_id").cursor(Cursor.Order.AFTER, results.next(), Sort.Direction.DESC), Post.class);
		results = pagination.executeQuery(
				Args.of(1).key("_id").cursor(Cursor.Order.AFTER, results.next(), Sort.Direction.DESC), Post.class);
		results = pagination.executeQuery(
				Args.of(1).key("_id").cursor(Cursor.Order.AFTER, results.next(), Sort.Direction.DESC), Post.class);

		assertNotNull(results);
		assertEquals(1, results.size());
		assertFalse(results.hasNext());
		assertTrue(results.hasPrevious());
	}

	@Test
	void shouldGetEmptyResultsSortedByIdDescWithNextCursor() {
		String cursor = CoreUtils.encode("236UV30CwhgaMiGKYbC4xm4KkUg");

		MongoCursorPagination pagination = new MongoCursorPagination(operations);

		PageResult<Post> results = pagination.executeQuery(
				Args.of(1).key("_id").cursor(Cursor.Order.AFTER, cursor, Sort.Direction.DESC), Post.class);

		assertNotNull(results);
		assertEquals(0, results.size());
		assertFalse(results.hasNext());
		assertFalse(results.hasPrevious());
	}

	@Test
	void shouldGetTotalResultsSortedByIdAsc() {
		MongoCursorPagination pagination = new MongoCursorPagination(operations);

		PageResult<Post> results = pagination.executeQuery(Args.of(6).key("_id"), Post.class);

		assertNotNull(results);
		assertEquals(6, results.size());
		assertFalse(results.hasNext());
		assertFalse(results.hasPrevious());
		assertEquals("236UV30CwhgaMiGKYbC4xm4KkUg", results.first().getId());
		assertEquals("236UYXcEANLN2F8K5A0d45k2DQo", results.last().getId());
	}

	@Test
	void shouldGetTotalResultsSortedByIdAndTitleAsc() {
		MongoCursorPagination pagination = new MongoCursorPagination(operations);

		Args args = Args.of(6).key("_id").sort("title");

		PageResult<Post> results = pagination.executeQuery(args, Post.class);

		assertNotNull(results);
		assertEquals(6, results.size());
		assertFalse(results.hasNext());
		assertTrue(results.hasPrevious());
		assertEquals("236UV30CwhgaMiGKYbC4xm4KkUg", results.first().getId());
		assertEquals("236UYXcEANLN2F8K5A0d45k2DQo", results.last().getId());
	}

	@Test
	void shouldThrowExceptionWhenKeyIsNotDeclaredInFields() {
		MongoCursorPagination pagination = new MongoCursorPagination(operations);

		Args args = Args.of(6).key("unknown").fields("id").sort("title").criteria(c -> {
			Criteria or = new Criteria();
			or.orOperator(where("title").is("e"), where("title").is("d"));
			c.add(or);
		});

		PaginationException thrown = assertThrows(PaginationException.class, () -> {
			pagination.executeQuery(args, Post.class);
		});

		assertEquals("PersistentProperty 'unknown' wasn't declared in fields", thrown.getMessage());
	}

	@Test
	void shouldGetTotalResultsSortedByIdAscWithDefaultValues() {
		MongoCursorPagination pagination = new MongoCursorPagination(operations);

		Args args = Args.of(6);

		PageResult<Post> results = pagination.executeQuery(args, Post.class);

		assertNotNull(results);
		assertEquals(6, results.size());
		assertFalse(results.hasNext());
		assertFalse(results.hasPrevious());
	}

	@Test
	void shouldThrowExceptionIfSortIsNull() {
		MongoCursorPagination pagination = new MongoCursorPagination(operations);

		Args args = Args.of(6).key("_id").sort("unkonw").criteria(c -> {
			Criteria or = new Criteria();
			or.orOperator(where("title").is("e"), where("title").is("d"));
			c.add(or);
		});

		PaginationException thrown = assertThrows(PaginationException.class, () -> {
			pagination.executeQuery(args, Post.class);
		});

		assertEquals("PersistentProperty is null", thrown.getMessage());
	}

	@Test
	void shouldGetResultsSortedByIdAscUsingNextAndBeforeCursor() {
		MongoCursorPagination pagination = new MongoCursorPagination(operations);
		Args args1 = Args.of(5).fields("_id", "businessKey").criteria(c -> c.add(where("caseDefinitionId").is("1")));
		PageResult<Case> results1 = pagination.executeQuery(args1, Case.class);

		Args args2 = Args.of(5).fields("_id", "businessKey").criteria(c -> c.add(where("caseDefinitionId").is("1")))
				.cursor(Cursor.Order.AFTER, results1.next(), Sort.Direction.ASC);
		PageResult<Case> results2 = pagination.executeQuery(args2, Case.class);

		Args args3 = Args.of(5).fields("_id", "businessKey").criteria(c -> c.add(where("caseDefinitionId").is("1")))
				.cursor(Cursor.Order.BEFORE, results2.previous(), Sort.Direction.ASC);
		PageResult<Case> results3 = pagination.executeQuery(args3, Case.class);

		assertEquals(5, results1.size());
		assertTrue(results1.hasNext());
		assertFalse(results1.hasPrevious());
		assertEquals(5, results2.size());
		assertFalse(results2.hasNext());
		assertTrue(results2.hasPrevious());
		assertEquals(5, results3.size());
		assertTrue(results3.hasNext());
		assertFalse(results3.hasPrevious());
	}

}
