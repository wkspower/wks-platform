package com.wks.caseengine.pagination;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.data.mongodb.core.query.Criteria.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class MongoQueryPaginatorIntegrationTest {

	@Autowired
	private MongoOperations operations;
	
	@BeforeEach
	public void setUp() {
		List<Person> p = createPeople();
		operations.insert(Arrays.asList(p.get(0), p.get(1), p.get(2), p.get(3)), Person.class);
	}
	
	@AfterEach
	public void teadown() {
		operations.remove(new Query(), Person.class);
	}
	
	@Test
	public void shouldResultPaginationUsingNextAndPrior() {
		CursorPage<Person> data1 = MongoQueryPaginator
																					 .with(operations, asList("id"))
																					 .cursor(Sort.Direction.ASC, 3)
																					 .executeQuery(Person.class);

		CursorPage<Person>  data2 = MongoQueryPaginator
																					.with(operations, asList("id"))
																					.cursor(data1.currentToken())
																					.executeQuery(Person.class);

		
		assertNotNull(data1);
		assertTrue(data1.hasNext());
		assertEquals("Davide", data1.get(0).getName());
		assertEquals("Gianni", data1.get(1).getName());
		
		assertNotNull(data2);
		assertTrue(data2.hasNext());
		assertEquals("Franco", data2.get(0).getName());
	}

	@Test
	public void shouldResultPaginationWithComplexCriteria() {
		CursorPage<Person> data1 = MongoQueryPaginator
																					 .with(operations, asList("id"))
																					 .cursor(null, Sort.Direction.ASC, 2)
																					 .criteria(c -> {
																						 Criteria criteria = new Criteria();
																						 criteria.orOperator(
																							where("name").is("Mario"),
																							where("age").is(20)
																						);
																						 c.add(criteria);
																					 })
																					 .executeQuery(Person.class);
			
		assertNotNull(data1);
		assertEquals(2, data1.getSize());
		assertTrue(data1.hasNext());
		assertEquals("Gianni", data1.get(0).getName());
		assertEquals("Mario", data1.get(1).getName());
	}
	
	@Test
	public void shouldResultPaginationWithCriteria() {
		CursorPage<Person> data1 = MongoQueryPaginator
																					 .with(operations, asList("id"))
																					 .cursor(null, Sort.Direction.ASC, 3)
																					 .criteria(c -> {
																						 c.add(where("name").is("Gianni"));
																					 })
																					 .executeQuery(Person.class);
			
		assertNotNull(data1);
		assertEquals(1, data1.getSize());
		assertFalse(data1.hasNext());
		assertEquals("5ede59ba2ed62b0006871000", data1.get(0).getId());
	}
	
	@Test
	public void shouldResultPaginationWithoutSortBy() {
		CursorPage<Person> data1 = MongoQueryPaginator
																					 .with(operations, asList("id"))
																					 .cursor(null, Sort.Direction.ASC, 3)
																					 .executeQuery(Person.class);
			
		assertNotNull(data1);
		assertEquals(3, data1.getData().size());
		assertTrue(data1.hasNext());
		assertEquals("5ede59ba2ed62b0006870000", data1.get(0).getId());
		assertEquals("5ede59ba2ed62b0006871000", data1.get(1).getId());
		assertEquals("5ede59ba2ed62b0006872000", data1.get(2).getId());

		CursorPage<Person> data2 = MongoQueryPaginator
																					 .with(operations, asList("id"))
																					 .cursor(data1.currentToken(), 3)
																					 .executeQuery(Person.class);
		
		assertNotNull(data2);
		assertEquals(1, data2.getData().size());
		assertFalse(data2.hasNext());
		assertEquals("5ede59ba2ed62b0006874000", data2.get(0).getId());		
	}
	
	@Test
	public void shouldResultPaginationWithPageSize() {
		CursorPage<Person> data1 = MongoQueryPaginator
																					 .with(operations, asList("id"))
																					 .cursor(3)
																					 .executeQuery(Person.class);
			
		assertNotNull(data1);
		assertEquals(3, data1.getData().size());
		assertTrue(data1.hasNext());
		assertEquals("5ede59ba2ed62b0006874000", data1.get(0).getId());
		assertEquals("5ede59ba2ed62b0006872000", data1.get(1).getId());
		assertEquals("5ede59ba2ed62b0006871000", data1.get(2).getId());

		CursorPage<Person> data2 = MongoQueryPaginator
																					 .with(operations, asList("id"))
																					 .cursor(data1.currentToken(), 3)
																					 .executeQuery(Person.class);
		
		assertNotNull(data2);
		assertEquals(1, data2.getData().size());
		assertFalse(data2.hasNext());
		assertEquals("5ede59ba2ed62b0006870000", data2.get(0).getId());		
	}

	@Test
	void shouldResultPaginationWithSortingByIdAsc() throws Exception {
		CursorPage<Person> data1 = MongoQueryPaginator
																				 .with(operations, asList("id"))
																				 .cursor(Sort.Direction.ASC, 3)
																				 .executeQuery(Person.class);
		
		assertNotNull(data1);
		assertTrue(data1.currentToken().hasToken());
		assertEquals(3, data1.getSize());
		assertTrue(data1.hasNext());
		assertEquals("5ede59ba2ed62b0006870000", data1.get(0).getId());
		assertEquals("5ede59ba2ed62b0006871000", data1.get(1).getId());
		assertEquals("5ede59ba2ed62b0006872000", data1.get(2).getId());
		
		CursorPage<Person> data2 = MongoQueryPaginator
																				 .with(operations, asList("id"))
																				 .cursor(data1.currentToken(), 3)
																				 .executeQuery(Person.class);
		
		assertNotNull(data2);
		assertFalse(data2.currentToken().hasToken());
		assertEquals(1, data2.getSize());
		assertFalse(data2.hasNext());
		assertEquals("5ede59ba2ed62b0006874000", data2.get(0).getId());
	}
	
	@Test
	void shouldResultPaginationWithSortingByIdDesc() throws Exception {
		CursorPage<Person> data1 = MongoQueryPaginator
																				 .with(operations, asList("id"))
																				 .cursor(Sort.Direction.DESC, 3)
																				 .executeQuery(Person.class);
		
		assertNotNull(data1);
		assertTrue(data1.currentToken().hasToken());
		assertEquals(3, data1.getSize());
		assertTrue(data1.hasNext());
		assertEquals("5ede59ba2ed62b0006874000", data1.get(0).getId());
		assertEquals("5ede59ba2ed62b0006872000", data1.get(1).getId());
		assertEquals("5ede59ba2ed62b0006871000", data1.get(2).getId());
		
		CursorPage<Person> data2 = MongoQueryPaginator
																				 .with(operations, asList("id"))
																				 .cursor(data1.currentToken(), 3)
																				 .executeQuery(Person.class);
		
		assertNotNull(data2);
		assertFalse(data2.currentToken().hasToken());
		assertEquals(1, data2.getSize());
		assertFalse(data2.hasNext());
		assertEquals("5ede59ba2ed62b0006870000", data2.get(0).getId());
	}	
	
	@Test
	void shouldResultPaginationWithSortingByDateAsc() throws Exception {
		CursorPage<Person> data1 = MongoQueryPaginator
																				 .with(operations, asList("id"))
																				 .cursor("birthday", Sort.Direction.ASC, 3)
																				 .executeQuery(Person.class);
		
		assertNotNull(data1);
		assertTrue(data1.currentToken().hasToken());
		assertEquals(3, data1.getSize());
		assertTrue(data1.hasNext());
		
		CursorPage<Person> data2 = MongoQueryPaginator
																				 .with(operations, asList("id"))
																				 .cursor(data1.currentToken(), 3)
																				 .executeQuery(Person.class);
		
		assertNotNull(data2);
		assertFalse(data2.currentToken().hasToken());
		assertEquals(1, data2.getSize());
		assertFalse(data2.hasNext());
		assertEquals("5ede59ba2ed62b0006872000", data1.get(0).getId());
		assertEquals("5ede59ba2ed62b0006870000",data1.get(1).getId());
		assertEquals("5ede59ba2ed62b0006874000", data1.get(2).getId());
		assertEquals("5ede59ba2ed62b0006871000", data2.get(0).getId());		
	}
	
	@Test
	void shouldResultPaginationWithSortingByDateDesc() throws Exception {
		CursorPage<Person> data1 = MongoQueryPaginator
																				 .with(operations, asList("id"))
																				 .cursor("birthday", Sort.Direction.DESC, 3)
																				 .executeQuery(Person.class);
		
		assertNotNull(data1);
		assertTrue(data1.currentToken().hasToken());
		assertEquals(3, data1.getSize());
		assertTrue(data1.hasNext());
		assertEquals("5ede59ba2ed62b0006871000", data1.get(0).getId());
		assertEquals("5ede59ba2ed62b0006874000",data1.get(1).getId());
		assertEquals("5ede59ba2ed62b0006870000", data1.get(2).getId());
		
		CursorPage<Person> data2 = MongoQueryPaginator
																				 .with(operations, asList("id"))
																				 .cursor(data1.currentToken(), 3)
																				 .executeQuery(Person.class);
		
		assertNotNull(data2);
		assertFalse(data2.currentToken().hasToken());
		assertEquals(0, data2.getSize());
		assertFalse(data2.hasNext());
	}
	
	@Test
	public void shouldExceptionWhenFieldNotFound() {
		QueryPaginatorException thrown = assertThrows(QueryPaginatorException.class, () -> {
			MongoQueryPaginator
					.with(operations, asList("id"))
					.cursor("exceptionGetterField", Sort.Direction.DESC, 3)
					.executeQuery(Person.class);
		});
		
		assertEquals("Error invoking getter getExceptionGetterField for property exceptionGetterField",
				thrown.getMessage());
	}

	@Test
	public void shouldExceptionWhenSortIsNull() {
		QueryPaginatorException thrown = assertThrows(QueryPaginatorException.class, () -> {
			MongoQueryPaginator
						.with(operations, asList("id"))
						.cursor("nullField", Sort.Direction.ASC, 3)
						.executeQuery(Person.class);
		});
		
		assertEquals("Null value not allowed for property nullField", thrown.getMessage());
	}
	
	@Test
	public void  shouldExceptionWhenUnknownPropertyDecrypting() throws Exception {
		String token =  TokenDigestUtils.encode("4b6280e4847fc72dcae90c213a357f96_6424aeaa18390072d6dea8fb_unknownProperty_10");
		
		QueryPaginatorException thrown = assertThrows(QueryPaginatorException.class, () -> {
			MongoQueryPaginator
							.with(operations, asList("id"))
							.cursor(Cursor.of(token), 10)
							.executeQuery(Person.class);
		});
		
		assertEquals("Error getting parameter value: null", thrown.getMessage());
	}

	@Test
	public void  shouldExceptionWhenUnknownPropertyEncrypting() {
		QueryPaginatorException thrown = assertThrows(QueryPaginatorException.class, () -> {
			MongoQueryPaginator
							.with(operations, asList("id"))
							.cursor("unknown", Sort.Direction.ASC, 3)
							.executeQuery(Person.class);			
		});
		
		assertEquals("PersistentProperty is null", thrown.getMessage());
	}

	
	private List<Person> createPeople() {
		Person p1 = new Person();
		p1.setId("5ede59ba2ed62b0006870000");
		p1.setAge(10);
		p1.setBirthday(new Date(946684800000L)); // 2000/1/1
		p1.setName("Davide");
		p1.setTimestamp(1561932000000L);
		p1.setNoGetterField("randomstring");
		
		Person p3 = new Person();
		p3.setId("5ede59ba2ed62b0006871000");
		p3.setAge(20);
		p3.setBirthday(new Date(1577836800000L)); // 2020/1/1
		p3.setName("Gianni");
		p3.setNoGetterField("randomstring");
		
		Person p4 = new Person();
		p4.setId("5ede59ba2ed62b0006872000");
		p4.setName("Mario");
		p4.setNoGetterField("randomstring");

		Person p2 = new Person();
		p2.setId("5ede59ba2ed62b0006874000");
		p2.setAge(20);
		p2.setBirthday(new Date(978307200000L)); // 2001/1/1
		p2.setName("Franco");
		p2.setTimestamp(1591631632000L);
		p2.setNoGetterField("randomstring");
		
		return Arrays.asList(p1, p2, p3, p4);
	}
}
