package com.platum.restflow.resource.transaction.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;

import com.platum.restflow.resource.transaction.RepositoryTransaction;

public class JdbcTransation implements RepositoryTransaction<Connection> {
	
	private final  Logger logger = LoggerFactory.getLogger(getClass());
	
	private Connection connection;
	
	@Override
	public RepositoryTransaction<Connection> connection(Connection connection) {
		this.connection = connection;
		return this;
	}

	@Override
	public Connection connection() {
		return connection;
	}

	@Override
	public void commit() {
		try {
			if(logger.isInfoEnabled()) {
				logger.info("Commiting transaction...");
			}
			connection.commit();			
		} finally {
			connection.close();	
		}
	}

	@Override
	public void rollback() {
		try {
			if(logger.isInfoEnabled()) {
				logger.info("Rolling back transaction...");
			}
			connection.rollback();	
		} finally {
			connection.close();	
		}
	}



}
