package com.platum.restflow.resource.query;

import org.junit.Assert;
import org.junit.Test;

import com.platum.restflow.exceptions.InvalidQueryFieldException;

public class QueryFieldTest {

	@Test(expected=InvalidQueryFieldException.class)
	public void testFieldCreationFromNullString() {
		QueryField.fromString(null);
	}
	
	@Test
	public void testFieldCreationFromString() {
		String field = "testField";
		QueryField queryField = QueryField.fromString(field);
		Assert.assertTrue(queryField.field().equals(field));
		Assert.assertTrue(queryField.measure() == null);
	}
	
	@Test
	public void testFieldCreationWithMeasureFromString() {
		String field = "$sum(testField)";
		QueryField queryField = QueryField.fromString(field);
		Assert.assertTrue(queryField.field().equals("testField"));
		Assert.assertTrue(queryField.measure().equals(QueryOperation.SUM));		
	}
	
	@Test(expected=InvalidQueryFieldException.class)
	public void testFieldCreationWrongMeasureFromString() {
		String field = "$not_exists(testField)";
		QueryField.fromString(field);		
	}	
}
