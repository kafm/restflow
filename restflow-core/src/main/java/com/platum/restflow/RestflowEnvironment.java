package com.platum.restflow;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public interface RestflowEnvironment {
	
	static final String DEFAULT_CONFIG_FILE = "restflow.properties";
	
	static final String TMP_PATH_PROPERTY = "restflow.tmp.path";
	
	static final String LOG_PATH_PROPERTY = "restflow.log.path";
	
	static final String LOG_CONFIG_PATH_PROPERTY = "restflow.log.config.path";
	
	static final String MODELS_PATH_PROPERTY = "restflow.models.path";
	
	static final String MESSAGES_PATH_PROPERTY = "restflow.messages.path";
	
	static final String MESSAGES_NAME_PROPERTY = "restflow.messages.name";
	
	static final String MESSAGES_DEFAULT_LOCALE_PROPERTY = "restflow.messages.locale";
	
	static final String ASSETS_PATH_PROPERTY = "restflow.assets.path";
	
	static final String ASSETS_ROUTE_PROPERTY = "restflow.assets.route";
	
	static final String BASE_URL_PROPERTY = "restflow.baseUrl";
	
	static final String BASE_PACKAGE_PROPERTY = "restflow.basePackage";
	
	static final String CLUSTER_ENABLED_PROPERTY = "restflow.cluster.enabled";
			
	static final String CLUSTER_HOST_PROPERTY = "restflow.cluster.host";
	
	static final String CLUSTER_PUBLIC_HOST_PROPERTY = "restflow.cluster.publicHost";
	
	static final String CLUSTER_PORT_PROPERTY = "restflow.cluster.port";
	
	static final String CLUSTER_PUBLIC_PORT_PROPERTY = "restflow.cluster.publicPort";
	
	static final String CLUSTER_PING_INTERVAL_PROPERTY = "restflow.cluster.pingInterval";
	
	static final String CLUSTER_PING_REPLY_INTERVAL_PROPERTY = "restflow.cluster.pingReplyInterval";
	
	static final String CUSTER_ADDRESSES_PATH_PROPERTY = "restflow.cluster.addresses.path";
	
	static final String HA_ENABLED_PROPERTY = "restflow.ha.enabled";
	
	static final String HA_QUORUM_SIZE_PROPERTY = "restflow.ha.quorumSize";
	
	static final String HA_GROUP_PROPERTY = "restflow.ha.group";
	
	static final String METRICS_ENABLED_PROPERTY = "restflow.metrics.enabled";
	
	static final String METRICS_JMX_ENABLED_PROPERTY = "restflow.metrics.jmxEnabled";
	
	static final String METRICS_JMX_DOMIN_PROPERTY = "restflow.metrics.jmxDomain";

	static final String HTTP_PORT_PROPERTY = "restflow.http.port";
	
	static final String HTTP_SSL_ENABLED_PROPERTY = "restflow.http.ssl.enabled";
	
	static final String HTTP_SSL_KEYSTORE_PROPERTY = "restflow.http.ssl.keystore";
	
	static final String HTTP_SSL_PEM_KEY_PROPERTY = "restflow.http.ssl.pemkey";

	static final String HTTP_SSL_PFX_KEY_PROPERTY = "restflow.http.ssl.pfxkey";

	static final String HTTP_SSL_PEM_CERT_PROPERTY = "restflow.http.ssl.pemcert";
	
	static final String HTTP_SSL_PASSWORD_PROPERTY = "restflow.http.ssl.password";
	
	static final String HTTP_SSL_ALPN_PROPERTY = "restflow.http.ssl.alpn";

	static final String HTTP_MAX_HEADER_SIZE = "restflow.http.header.size";

	String getProperty(String property);
	
	<T> T getProperty(String property, Class<?> clazz);
	
	Properties getPropertiesByPrefix(String prefix);
	
	Map<String, Object> getPropertiesAsMap(Properties properties);
	
	RestflowEnvironment setProperty(String property, String value);
	
	RestflowEnvironment setProperties(Properties properties);
	
	RestflowEnvironment load(InputStream in);
	
}
