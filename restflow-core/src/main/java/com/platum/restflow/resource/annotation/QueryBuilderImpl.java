package com.platum.restflow.resource.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.platum.restflow.resource.query.QueryBuilder;

@Retention(RetentionPolicy.RUNTIME)
public @interface QueryBuilderImpl {
	Class<? extends QueryBuilder> value();
}
