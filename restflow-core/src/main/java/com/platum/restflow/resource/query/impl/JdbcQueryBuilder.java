package com.platum.restflow.resource.query.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableMap;
import com.platum.restflow.RestflowHttpMethod;
import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.exceptions.RestflowInvalidRequestException;
import com.platum.restflow.resource.Params;
import com.platum.restflow.resource.Resource;
import com.platum.restflow.resource.ResourceMethod;
import com.platum.restflow.resource.property.ResourceProperty;
import com.platum.restflow.resource.property.ResourcePropertyValidator;
import com.platum.restflow.resource.query.QueryBuilder;
import com.platum.restflow.resource.query.QueryField;
import com.platum.restflow.resource.query.QueryFilter;
import com.platum.restflow.resource.query.QueryModifier;
import com.platum.restflow.resource.query.QueryOperation;
import com.platum.restflow.resource.query.SortModifier;

public class JdbcQueryBuilder implements QueryBuilder {

	private ResourceMethod method;

	private boolean generated;

	private Resource resource;

	private Params params;

	private boolean containsAggr = false;

	private Pattern sortFieldPattern = Pattern.compile("[a-zA-Z0-9_]+");
	
	private static final Map<QueryOperation, String> sqlOperations = new ImmutableMap.Builder<QueryOperation, String>()
			.put(QueryOperation.AND, "AND").put(QueryOperation.OR, "OR").put(QueryOperation.EQUAL, "=")
			.put(QueryOperation.NOT_EQUAL, "!=").put(QueryOperation.GREATER_THAN, ">")
			.put(QueryOperation.GREATER_THAN_OR_EQUAL, ">=").put(QueryOperation.LESS_THAN, "<")
			.put(QueryOperation.LESS_THAN_OR_EQUAL, "<=").put(QueryOperation.LIKE, " like ")
			.put(QueryOperation.NOT_LIKE, " not like ").put(QueryOperation.IN, " in ")
			.put(QueryOperation.NOT_IN, " not in ").put(QueryOperation.SUM, "sum").put(QueryOperation.MIN, "min")
			.put(QueryOperation.MAX, "max").put(QueryOperation.AVG, "avg").put(QueryOperation.COUNT, "count").build();

	@Override
	public Params params() {
		return params;
	}

	@Override
	public QueryBuilder params(Params params) {
		this.params = params;
		return this;
	}

	@Override
	public ResourceMethod method() {
		return method;
	}

	@Override
	public QueryBuilder method(ResourceMethod method) {
		this.method = method;
		return this;
	}

	@Override
	public Resource resource() {
		return resource;
	}

	@Override
	public QueryBuilder resource(Resource resource) {
		this.resource = resource;
		return this;
	}

	@Override
	public boolean supportsCrudAutoGen() {
		return resource.generateCrud;
	}

	@Override
	public QueryBuilder generate(RestflowHttpMethod httpMethod) {
		if (httpMethod == null) {
			throw new RestflowException("Null http method provided.");
		}
		if (httpMethod.equals(RestflowHttpMethod.GET)) {
			generateGet();
		} else if (httpMethod.equals(RestflowHttpMethod.GET_WITH_ID)) {
			generateGetById();
		} else if (httpMethod.equals(RestflowHttpMethod.POST)) {
			generateInsert();
		} else if (httpMethod.equals(RestflowHttpMethod.PUT_WITH_ID)) {
			generateUpdate();
		} else if (httpMethod.equals(RestflowHttpMethod.DELETE_WITH_ID)) {
			generateDelete();
		}
		return this;
	}

	@Override
	public QueryBuilder generateInsert() {
		method = new ResourceMethod().setUrl(RestflowHttpMethod.POST.value());
		if (resource.hasProperties()) {
			List<ResourceProperty> properties = getProperties(false);
			StringBuilder query = new StringBuilder();
			StringBuilder values = new StringBuilder();
			int size = properties.size();
			String[] params = new String[size];
			IntStream.range(0, size).forEach(i -> {
				ResourceProperty property = properties.get(i);
				if (i == 0) {
					query.append("INSERT INTO " + resource.getTable() + " (");
					values.append("VALUES (");
				} else {
					query.append(",");
					values.append(",");
				}
				query.append(property.getColumn());
				values.append(":" + property.getName());
				params[i] = property.getName();
			});
			query.append(") " + values.toString() + ")");
			method.setQuery(query.toString()).setParams(params);
		}
		generated = true;
		return this;
	}

	@Override
	public QueryBuilder generateInsertIfNotFound() {
		if (method == null && supportsCrudAutoGen()) {
			generateInsert();
		}
		return this;
	}

	@Override
	public QueryBuilder generateUpdate() {
		method = new ResourceMethod().setUrl(RestflowHttpMethod.PUT_WITH_ID.value());
		if (resource.hasProperties()) {
			List<ResourceProperty> properties = getProperties(false);
			StringBuilder query = new StringBuilder();
			int size = properties.size();
			String[] params = new String[size + 1];
			IntStream.range(0, size).forEach(i -> {
				ResourceProperty property = properties.get(i);
				if (i == 0) {
					query.append("UPDATE " + resource.getTable() + " SET ");
				} else {
					query.append(",");
				}
				query.append(property.getColumn() + "=" + ":" + property.getName());
				params[i] = property.getName();
			});
			ResourceProperty idProp = resource.getIdPropertyAsObject();
			query.append(" WHERE " + idProp.getColumn() + "=:" + idProp.getName());
			params[size] = idProp.getName();
			method.setQuery(query.toString()).setParams(params);
		}
		generated = true;
		return this;
	}

	@Override
	public QueryBuilder generateUpdateIfNotFound() {
		if (method == null && supportsCrudAutoGen()) {
			generateUpdate();
		}
		return this;
	}

	@Override
	public QueryBuilder generateDelete() {
		method = new ResourceMethod()
				.setUrl(RestflowHttpMethod.DELETE_WITH_ID.value());
		ResourceProperty idProp = resource.getIdPropertyAsObject();
		if (idProp != null) {
			String query = "DELETE FROM " + resource.getTable() + " WHERE " + idProp.getColumn() + "=:"
					+ idProp.getName();
			method.setQuery(query).setParams(new String[] { idProp.getName() });
		}
		generated = true;
		return this;
	}

	@Override
	public QueryBuilder generateDeleteIfNotFound() {
		if (method == null && supportsCrudAutoGen()) {
			generateDelete();
		}
		return this;
	}

	@Override
	public QueryBuilder generateGet() {
		method = new ResourceMethod().setUrl(RestflowHttpMethod.GET.value());
		StringBuilder query = new StringBuilder();
		if (resource.hasProperties()) {
			List<ResourceProperty> properties = getProperties();
			properties.stream().forEach(property -> {
				if (query.length() == 0) {
					query.append("SELECT ");
				} else {
					query.append(",");
				}
				query.append(property.getColumn());
			});
		} else {
			query.append("SELECT *");
		}
		query.append(" FROM " + resource.getTable());
		method.setQuery(query.toString());
		generated = true;
		return this;
	}

	@Override
	public QueryBuilder generateGetIfNotFound() {
		if (method == null && supportsCrudAutoGen()) {
			generateGet();
		}
		return this;
	}

	@Override
	public QueryBuilder generateGetById() {
		generateGet();
		method.setUrl(RestflowHttpMethod.GET_WITH_ID.value());
		ResourceProperty idProp = resource.getIdPropertyAsObject();
		String query = method.getQuery();
		if (StringUtils.isNotEmpty(query) && idProp != null) {
			query += " WHERE " + idProp.getColumn() + "=:" + idProp.getName();
			method.setQuery(query).setParams(new String[] { idProp.getName() });
		}
		generated = true;
		return this;
	}

	@Override
	public QueryBuilder generateGetByIdIfNotFound() {
		if (method == null && supportsCrudAutoGen()) {
			generateGetById();
		}
		return this;
	}

	@Override
	public QueryBuilder generateCount() {
		method = new ResourceMethod();
		String query = "SELECT COUNT(*) FROM " + resource.getTable();
		method.setQuery(query);
		generated = true;
		return this;
	}

	@Override
	public QueryBuilder generateCountIfNotFound() {
		if (method == null && supportsCrudAutoGen()) {
			generateCount();
		}
		return this;
	}

	@Override
	public QueryBuilder wrap() {
		assertMethodNotNull();
		if (!generated) {
			method = method.clone();
			generated = true;
		}
		method.setWrap(true).setQuery("SELECT * FROM (" + method.getQuery() + ") AS WRAP");
		return this;
	}

	@Override
	public QueryBuilder wrapIfAllowed() {
		if(method.isWrap()) {
			wrap();
		}
		return this;
	}
	
	@Override
	public QueryBuilder completeQueryWithFilter(QueryFilter filter) {
		if (method != null) {
			method = generated ? method : method.clone();
			if (params == null) {
				params = new Params();
			}
			StringBuilder query = new StringBuilder(method.getQuery());
			containsAggr = false;
			if (filter != null) {
				Pair<StringBuilder, Params> statement = completeQueryWithFilter(filter, true);
				StringBuilder where = statement.getLeft();
				if (where != null && where.length() > 0) {
					query.append(statement.getLeft().toString());
					Params params = statement.getRight();
					if (!params.isEmpty()) {
						this.params.merge(params);
						//method.setParams(ArrayUtils.addAll(method.getParams(), params.getParamNames()));
					}
				}
			}
			method.setQuery(query.toString());
		}
		return this;
	}

	@Override
	public QueryBuilder completeQueryWithModifier(QueryModifier modifier) {
		assertMethodNotNull();
		if (modifier != null) {
			completeWithFields(modifier.fields());
			StringBuilder query = new StringBuilder(method.getQuery());
			completeWithSort(query, modifier.sort());
			if (modifier.limit() >= 0) {
				query.append(" LIMIT " + modifier.limit());
			}
			if (modifier.offset() >= 0) {
				query.append(" OFFSET " + modifier.offset());
			}
			method.setQuery(query.toString());
		}
		return this;
	}

	private void completeWithSort(StringBuilder query, SortModifier[] sort) {
		if (sort != null && sort.length > 0) {
			List<String> paramNames = new ArrayList<>();
			if (method.getParams() != null) {
				paramNames.addAll(Arrays.asList(method.getParams()));
			}
			if (params == null) {
				params = new Params();
			}
			IntStream.range(0, sort.length).forEach(i -> {
				SortModifier param = sort[i];
				if(sortFieldPattern.matcher(param.field()).matches()) {
					if (i == 0) {
						query.append(" ORDER BY ");
					} else {
						query.append(",");
					}
					query.append(param.field() + " " + param.order().name());
				}
			});
			method.setParams(paramNames.toArray(new String[paramNames.size()]));
		}
	}

	private void completeWithFields(QueryField[] fields) {
		if (fields != null && fields.length > 0) {
			StringBuilder query = new StringBuilder();
			StringBuilder groupBy = new StringBuilder();
			Stream.of(fields).forEach(field -> {
				if (query.length() == 0) {
					query.append("SELECT ");
				} else {
					query.append(",");
				}
				if (field.measure() != null) {
					query.append(sqlOperations.get(field.measure())).append("(")
							.append(getColumnFromFieldName(field.field()))
							.append(") AS " + field.toStringRepresentation());
					if (!containsAggr) {
						containsAggr = true;
					}
				} else {
					query.append(field.field());
					if (groupBy.length() > 0) {
						groupBy.append(",");
					}
					groupBy.append(field.field());
				}
			});
			int length = method.getQuery().length();
			String from = " FROM ";
			int fromIndex = method.getQuery().toUpperCase().indexOf(from) + from.length();
			query.append(from).append(method.getQuery().substring(fromIndex, length));
			if (groupBy.length() > 0 && containsAggr) {
				query.append(" GROUP BY ").append(groupBy.toString());
			}
			method.setQuery(query.toString());
		}
	}

	private Pair<StringBuilder, Params> completeQueryWithFilter(QueryFilter filter, boolean appendWhere) {
		Object leftToken = filter.leftToken();
		Object rightToken = filter.rightToken();
		QueryOperation operation = filter.operation();
		StringBuilder builder = new StringBuilder(appendWhere ? " WHERE " : "");
		if (leftToken != null && rightToken != null) {
			if (leftToken instanceof QueryFilter) {
				if (operation == null || !operation.isAndOr()) {
					throw new RestflowInvalidRequestException(
							"Wrong Query provided. Expecting AND|OR but found " + operation + ".");
				} else if (!(rightToken instanceof QueryFilter)) {
					throw new RestflowInvalidRequestException(
							"Wrong Query provided. Expecting QueryFilter instance but found " + rightToken.getClass()
									+ ".");
				}
				Pair<StringBuilder, Params> left = completeQueryWithFilter((QueryFilter) leftToken, false);
				Pair<StringBuilder, Params> right = completeQueryWithFilter((QueryFilter) rightToken, false);
				boolean brackets = operation.equals(QueryOperation.OR);
				builder.append(brackets ? "(" + left.getLeft().toString() + ")" : left.getLeft().toString())
						.append(" " + operation.name() + " ")
						.append(brackets ? "(" + right.getLeft().toString() + ")" : right.getLeft().toString());
				Params params = left.getRight();
				params.merge(right.getRight());
				return new ImmutablePair<StringBuilder, Params>(builder, params);

			} else {
				if (operation == null) {
					operation = QueryOperation.EQUAL;
				} else if (operation.isAndOr()) {
					throw new RestflowInvalidRequestException(
							"Wrong Query provided. At this stage AND|OR operation is not allowed.");
				}
				String op = sqlOperations.get(operation);
				if (op == null) {
					throw new RestflowInvalidRequestException(
							"Wrong Query provided. Invalid operation " + operation.name() + ".");
				}
				String columnName = leftToken.toString();
				String propertyName = columnName;
				ResourceProperty property = resource.getProperty(propertyName);
				if (property != null) {
					propertyName = property.getName();
					columnName = property.getColumn();
				} else {
					propertyName = propertyName.replaceAll("[^A-Za-z0-9_-]", "");
				}
				String[] paramNames = method.getParams();
				List<String> newParams = new ArrayList<>();
				int curIndex = paramNames == null ? 0 : paramNames.length;
				if(operation.equals(QueryOperation.IN) || operation.equals(QueryOperation.NOT_IN)) {
					@SuppressWarnings("unchecked")
					final List<Object> vals = rightToken instanceof List ? (List<Object>) rightToken :Arrays.asList(rightToken);
					builder.append(columnName)
					.append(op)
					.append("(");
					for(int i = 0; i < vals.size(); i++) {
						Object val = vals.get(i);
						if(i > 0) {
							builder.append(",");
							curIndex++;
						}
						String inPropertyName = propertyName+"_"+curIndex;
						builder.append(":")
							   .append(inPropertyName);
						params.addParam(inPropertyName, 
								ResourcePropertyValidator.getRepositoryPropertyValue(property, val));
						newParams.add(inPropertyName);
					}
					builder.append(")");
				} else {
					propertyName = propertyName+"_"+curIndex;
					builder.append(columnName)
							.append(op).append(":")
							.append(propertyName);
					newParams.add(propertyName);
					params.addParam(propertyName, 
							ResourcePropertyValidator.getRepositoryPropertyValue(property, rightToken));
				}			
				method.setParams(paramNames == null ? newParams.toArray(new String[newParams.size()])
													: ArrayUtils.addAll(paramNames, 
														newParams.toArray(new String[newParams.size()])));
				return new ImmutablePair<StringBuilder, Params>(builder, params);
			}
		} else {
			throw new RestflowInvalidRequestException("Wrong Query provided. left token [" + leftToken
					+ "] or right token [" + rightToken + "] is null.");
		}
	}

	private List<ResourceProperty> getProperties() {
		return getProperties(true);
	}

	private List<ResourceProperty> getProperties(boolean includeIdAutoGen) {
		return !includeIdAutoGen && resource.isIdAutoGenerated()
				&& StringUtils
						.isNotEmpty(resource.getIdProperty())
								? resource.getProperties().stream()
										.filter(property -> !property.getName()
												.equalsIgnoreCase(resource.getIdProperty()))
								.collect(Collectors.toList()) : resource.getProperties();
	}

	private String getColumnFromFieldName(String name) {
		List<ResourceProperty> properties = getProperties();
		if (properties != null && StringUtils.isNotEmpty(name)) {
			ResourceProperty property = getProperties().stream().filter(p -> p.getName().equals(name)).findAny()
					.orElse(null);
			if (property != null) {
				return property.getColumn();
			}
		}
		return name;
	}

	private void assertMethodNotNull() {
		if (method == null)
			throw new RestflowException("Method cannot be null at this point.");
	}

}
