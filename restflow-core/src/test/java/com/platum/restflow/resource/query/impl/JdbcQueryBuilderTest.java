package com.platum.restflow.resource.query.impl;

import org.junit.Assert;
import org.junit.Test;

import com.platum.restflow.Restflow;
import com.platum.restflow.resource.Resource;
import com.platum.restflow.resource.query.QueryBuilder;
import com.platum.restflow.resource.query.QueryField;
import com.platum.restflow.resource.query.QueryFilter;
import com.platum.restflow.resource.query.QueryModifier;
import com.platum.restflow.resource.query.QueryOperation;

public class JdbcQueryBuilderTest {

	private static Resource resource;
	
	@Test
	public void generateInsertTest() {
		resolveResource();
		QueryBuilder builder = new JdbcQueryBuilder()
								.resource(resource)
								.generateInsert();
		Assert.assertEquals("INSERT INTO test (name,s_name) VALUES (:name,:surname)", builder.method().getQuery());
	}
	
	@Test
	public void generateUpdateTest() {
		resolveResource();
		QueryBuilder builder = new JdbcQueryBuilder()
								.resource(resource)
								.generateUpdate();
		Assert.assertEquals("UPDATE test SET name=:name,s_name=:surname WHERE id=:id", builder.method().getQuery());	
		Assert.assertArrayEquals(new String[]{"name","surname","id"}, builder.method().getParams());
	}
	
	@Test
	public void generateDeleteTest() {
		resolveResource();
		QueryBuilder builder = new JdbcQueryBuilder()
								.resource(resource)
								.generateDelete();
		Assert.assertEquals("DELETE FROM test WHERE id=:id", builder.method().getQuery());	
		Assert.assertArrayEquals(new String[]{"id"}, builder.method().getParams());		
	}
	
	@Test
	public void generateGetTest() {
		resolveResource();
		QueryBuilder builder = new JdbcQueryBuilder()
								.resource(resource)
								.generateGet();
		Assert.assertEquals("SELECT name,s_name,id FROM test", builder.method().getQuery());		
	}
	
	@Test
	public void generateGetById() {
		resolveResource();
		QueryBuilder builder = new JdbcQueryBuilder()
								.resource(resource)
								.generateGetById();
		Assert.assertEquals("SELECT name,s_name,id FROM test WHERE id=:id", builder.method().getQuery());	
		Assert.assertArrayEquals(new String[]{"id"}, builder.method().getParams());			
	}
	
	@Test
	public void generateCountTest() {
		resolveResource();
		QueryBuilder builder = new JdbcQueryBuilder()
								.resource(resource)
								.generateCount();
		Assert.assertEquals("SELECT COUNT(*) FROM test", builder.method().getQuery());	
	}
	
	@Test
	public void completeQueryWithFilterTest() {
		resolveResource();
		QueryBuilder builder = new JdbcQueryBuilder()
								.resource(resource)
								.generateGet();
		QueryFilter filter = new QueryFilter()
								.filter("surname", QueryOperation.EQUAL, "Ana")
								.and(new QueryFilter().filter("name", QueryOperation.LIKE, "Test%"))
								.or(new QueryFilter().filter("id", QueryOperation.EQUAL, 1));
		builder.completeQueryWithFilter(filter);
		Assert.assertEquals("SELECT name,s_name,id FROM test WHERE (s_name=:surname_0 AND name like :name_1) OR (id=:id_2)",
				builder.method().getQuery());
		Assert.assertArrayEquals(new String[]{"surname_0","name_1", "id_2"}, builder.method().getParams());	
		Assert.assertEquals(3,builder.params().size());		
	}
	
	@Test
	public void completeCountQueryWithFilterTest() {
		resolveResource();
		QueryBuilder builder = new JdbcQueryBuilder()
								.resource(resource)
								.generateCount();
		QueryFilter filter = new QueryFilter()
								.filter("surname", QueryOperation.EQUAL, "Ana")
								.and(new QueryFilter().filter("name", QueryOperation.LIKE, "Test%"))
								.and(new QueryFilter().filter("id", QueryOperation.EQUAL, 1));
		builder.completeQueryWithFilter(filter);
		Assert.assertEquals("SELECT COUNT(*) FROM test WHERE s_name=:surname_0 AND name like :name_1 AND id=:id_2",
				builder.method().getQuery());
		Assert.assertArrayEquals(new String[]{"surname_0", "name_1", "id_2"}, builder.method().getParams());	
		Assert.assertEquals(3,builder.params().size());		
	}
	
	@Test
	public void completeQueryWithFilterAndFieldsTest() {
		resolveResource();
		QueryBuilder builder = new JdbcQueryBuilder()
								.resource(resource)
								.generateGet();
		QueryFilter filter = new QueryFilter()
								.filter("surname", QueryOperation.EQUAL, "Ana")
								.and(new QueryFilter().filter("name", QueryOperation.LIKE, "Test%"))
								.or(new QueryFilter().filter("id", QueryOperation.EQUAL, 1))
								.and(new QueryFilter().filter("xpto", QueryOperation.IN, new String[]{"a","b"}));
		builder.completeQueryWithFilter(filter)
			   .completeQueryWithModifier(new QueryModifier().fields(new QueryField[]{
				new QueryField("name"), new QueryField("id", QueryOperation.COUNT)}));
		Assert.assertEquals("SELECT name,count(id) AS countId FROM test WHERE (s_name=:surname_0 AND name like :name_1) OR (id=:id_2) AND xpto in (:xpto_3) GROUP BY name",
				builder.method().getQuery());
		Assert.assertArrayEquals(new String[]{"surname_0","name_1", "id_2", "xpto_3"}, builder.method().getParams());	
		Assert.assertEquals(4,builder.params().size());		
	}
	
	@Test
	public void completeQueryWithFilterAndFieldsWrapTest() {
		resolveResource();
		QueryBuilder builder = new JdbcQueryBuilder()
								.resource(resource)
								.generateGet()
								.wrap();
		QueryFilter filter = new QueryFilter()
								.filter("surname", QueryOperation.EQUAL, "Ana")
								.and(new QueryFilter().filter("name", QueryOperation.LIKE, "Test%"))
								.or(new QueryFilter().filter("id", QueryOperation.EQUAL, 1));
		builder.completeQueryWithFilter(filter)
			   .completeQueryWithModifier(new QueryModifier().fields(new QueryField[]{
					   new QueryField("name"), new QueryField("id", QueryOperation.COUNT)}));
		Assert.assertEquals("SELECT name,count(id) AS countId FROM (SELECT name,s_name,id FROM test) AS WRAP WHERE (s_name=:surname_0 AND name like :name_1) OR (id=:id_2) GROUP BY name",
				builder.method().getQuery());
		Assert.assertArrayEquals(new String[]{"surname_0","name_1", "id_2"}, builder.method().getParams());	
		Assert.assertEquals(3,builder.params().size());		
	}
	
	private void resolveResource() {
		if(resource == null) {
			Restflow restflow = new Restflow()
					.loadModels();
			resource = restflow.getResource("resource_test");
		}
	}
	
}
