package com.platum.restflow.resource.impl.jdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.utils.ClassUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class JdbcClient {

	private static final Map<String, DatasourceConf> datasourceMap = new HashMap<>();
	
	private static final JdbcExceptionMapping errorMapping = JdbcExceptionMapping.newInstance();
	
	private final  Logger logger = LoggerFactory.getLogger(getClass());
	
	private Sql2o template;
	
	private DatabaseSqlErrors dbErrorMap;
	
	private String reference;
	
	public static JdbcClient createInstance(String reference, Properties properties) {
		JdbcClient client = new JdbcClient();
		DatasourceConf conf = getOrCreateTemplate(reference, properties);
		client.template = conf.sql2o;
		client.dbErrorMap = conf.errorMap;
		client.reference = reference;
		return client;
	}
	
	public Sql2o template() {
		return template;
	}
	
	public void close() {
		synchronized (datasourceMap) {
			HikariDataSource ds = (HikariDataSource) template.getDataSource();
			if(!ds.isClosed()) {
				ds.close();
			}
			datasourceMap.remove(reference);
		}	
	}
	
	public RestflowException translateException(Throwable e) {
		RestflowException exceptionToReturn = null;
		if(e instanceof RestflowException) {
			exceptionToReturn = (RestflowException) e;
		} else if(e instanceof Sql2oException && dbErrorMap != null) {
			SQLException originalException = (SQLException) e.getCause();
			if(dbErrorMap != null && originalException != null) {
				List<SqlErrorCodeMap> errorCodes = dbErrorMap.getErrorCodeMap();
				if(errorCodes != null && !errorCodes.isEmpty()) {
					int code = originalException.getErrorCode();
					SqlErrorCodeMap errorMap = errorCodes.stream()
					.filter(error -> error.getCode().contains(code))
					.findAny()
					.orElse(null);
					if(errorMap != null && errorMap.getException() != null) {
						Class<? extends RestflowException> eClass = errorMap.getException();
						exceptionToReturn = ClassUtils.newInstance(eClass, errorMap.getMessage());
					}
				}
			}
		} 
		String error = "Jdbc error when executing operation.";
		if(exceptionToReturn == null) {
			exceptionToReturn = new RestflowException(error, e);
		}
		logger.error(error, e);
		return exceptionToReturn;
	}
	
	private static DatasourceConf getOrCreateTemplate(String connectionReference, Properties properties) {
		Validate.notNull(connectionReference, "Connection reference cannot be null");
		Validate.notNull(properties, "Connection properties is empty");
		synchronized (datasourceMap) {
			DatasourceConf dsConf = datasourceMap.get(connectionReference);
			if(dsConf == null) {
				HikariConfig config = new HikariConfig(properties);
				HikariDataSource ds = new HikariDataSource(config);		
				dsConf = new DatasourceConf();
				dsConf.sql2o = new Sql2o(ds);
				try(Connection con = dsConf.sql2o.open()) {
					dsConf.errorMap = errorMapping.getErrorMapForDatasource(con.getJdbcConnection());
				}
				datasourceMap.put(connectionReference, dsConf);
			}
			return dsConf;
		}
	}

	private static class DatasourceConf {
		Sql2o sql2o;
		DatabaseSqlErrors errorMap;
	}
}
