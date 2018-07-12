package com.platum.restflow.resource.impl.jdbc;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcExceptionMapping {
	
	private static final Logger logger = LoggerFactory.getLogger(JdbcExceptionMapping.class);

	private static List<DatabaseSqlErrors> errors;	
	
	private JdbcExceptionMapping(ErrorMapping mapping) {
		if(mapping != null && mapping.errors != null) {
			if(errors == null) {
				errors = new ArrayList<>();
			}
			errors.addAll(mapping.errors);
		}
	}
	
	public static JdbcExceptionMapping newInstance() {
		return newInstance(false);
	}
	
	public static JdbcExceptionMapping newInstance(boolean force) {
		if(errors == null || force) {
			return load((URL) null);
		}
		return new JdbcExceptionMapping(null);
	}
	
	public static JdbcExceptionMapping load(URL url) {
		try {
			if(url == null) {
				//URL classPath = JdbcExceptionMapping.class.getProtectionDomain().getCodeSource().getLocation();
				//url = new URL (classPath + "/sql-exception-mapping.xml");		
				InputStream in = JdbcExceptionMapping.class.getResourceAsStream("/sql-exception-mapping.xml");
				return load(in);
			}
			return load(url.openStream());
		} catch (Throwable e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Ignoring xml ["+url.getFile()+"].", e);
			}
		}
		return null;
	}
	
	public static JdbcExceptionMapping load(InputStream in) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(ErrorMapping.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			ErrorMapping mapping = (ErrorMapping) jaxbUnmarshaller.unmarshal(in);			
			return new JdbcExceptionMapping(mapping);
		} catch (Throwable e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Ignoring sql mapping xml.", e);
			}
		}
		return null;
	}
	
	public DatabaseSqlErrors getErrorMapForDatasource(Connection connection) {
		try {
			DatabaseMetaData metadata = connection.getMetaData();
			return getErrorMapForDatasource(metadata.getDatabaseProductName());
		} catch(Throwable e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Error when getting database metadata.", e);
			}
		}
		return null;
	}
	
	public DatabaseSqlErrors getErrorMapForDatasource(String dbName) {
		if(errors != null) {
			return errors.stream()
					.filter(dbErrorMap -> dbMatch(dbName, dbErrorMap))
					.findAny()
					.orElse(null);
		}
		return null;
	}
	
	private boolean dbMatch(String dbName, DatabaseSqlErrors dbErrorMap) {
		if(dbName != null && dbErrorMap != null) {
			return Stream.of(dbErrorMap.getNamesAsArray())
				  .filter(name -> (name.endsWith("*") 
						  				&& dbName.startsWith(name.substring(0, name.lastIndexOf("*"))))
						  				|| name.equals(dbName))
				  .findAny()
				  .orElse(null) != null;
		}
		return false;
	}
	
	public static void main(String[] args) {
		/*System.out.println(ClassUtils.getClassLocation(JdbcExceptionMapping.class));
		String packagePath = JdbcExceptionMapping.class.getPackage().getName().replaceAll("\\.", "/");
		String urlName = "classpath:"+packagePath+"/sql-exception-mapping.xml";
		System.out.println(urlName);
		URL url = ResourceUtils.getURL(urlName);
		System.out.println(url);
		List<URL> urls = ResourceUtils.getUrlsFromClassPath("META-INF/sql-exception-mapping.xml");
		System.out.println(urls);*/
		JdbcExceptionMapping.newInstance();
	}
}
