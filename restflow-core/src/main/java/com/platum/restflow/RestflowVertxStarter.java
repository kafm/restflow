package com.platum.restflow;

import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;

public class RestflowVertxStarter {
	
	public static final int DEFAULT_SERVER_PORT = 8080;
	
	private static final Logger logger = LoggerFactory.getLogger(RestflowVertxStarter.class);
		
	public static void startVertx(RestflowEnvironment environment, Handler<AsyncResult<Vertx>> handler) {
		VertxOptions options = getOptions(environment);
		Future<Vertx> future = Future.future();
		future.setHandler(handler);
		if(options.isClustered()) {
			Vertx.clusteredVertx(options, res -> {	
				if (res.succeeded()) {
					if(logger.isInfoEnabled()) {
						logger.info("Vertx cluster initialized");
					}
					future.complete(res.result());
				} else {
					if(logger.isDebugEnabled()) {
						logger.debug("Unable to start Vertx cluster",res.cause());
					}
					future.fail(res.cause());
				}
			});
		} else {
			future.complete(Vertx.vertx());
		}
	}
	
	public static HttpServerOptions getHttpOptions(RestflowEnvironment environment) {
		boolean httpSslEnabled =  BooleanUtils.toBoolean(
		environment.getProperty(RestflowEnvironment.HTTP_SSL_ENABLED_PROPERTY));	
		Integer port = environment.getProperty(RestflowEnvironment.HTTP_PORT_PROPERTY, Integer.class);
		HttpServerOptions httpOptions = new HttpServerOptions()
										.setPort(port == null ? DEFAULT_SERVER_PORT : port);
		String maxHeaderSize = environment.getProperty(RestflowEnvironment.HTTP_MAX_HEADER_SIZE);
		httpOptions.setMaxHeaderSize(StringUtils.isEmpty(maxHeaderSize)?
										16384: Integer.parseInt(maxHeaderSize));
		if(httpSslEnabled) {
			boolean alpnEnabled =  BooleanUtils.toBoolean(
					environment.getProperty(RestflowEnvironment.HTTP_SSL_ALPN_PROPERTY));		
			String keyStore = environment.getProperty(RestflowEnvironment.HTTP_SSL_KEYSTORE_PROPERTY);
			String keyPemStore = environment.getProperty(RestflowEnvironment.HTTP_SSL_PEM_KEY_PROPERTY);
			String keyPfxCertStore = environment.getProperty(RestflowEnvironment.HTTP_SSL_PFX_KEY_PROPERTY);
			String password = environment.getProperty(RestflowEnvironment.HTTP_SSL_PASSWORD_PROPERTY);
			httpOptions.setSsl(httpSslEnabled)
					   .setUseAlpn(alpnEnabled);
			if(StringUtils.isNotEmpty(keyStore)) {
				httpOptions.setKeyStoreOptions(
							new JksOptions().setPath(keyStore)
											.setPassword(password));
			} else if(StringUtils.isNotEmpty(keyPemStore)) {
				String pemCertStore = environment.getProperty(RestflowEnvironment.HTTP_SSL_PEM_CERT_PROPERTY);
				httpOptions.setKeyCertOptions(
						new PemKeyCertOptions().setKeyPath(keyPemStore)
											   .setCertPath(pemCertStore));
			} else if(StringUtils.isNotEmpty(keyPfxCertStore)) {
				httpOptions.setPfxKeyCertOptions(
						new PfxOptions().setPath(keyPfxCertStore)
										.setPassword(password));
			}
		}
		return httpOptions;
	}
	
	protected static VertxOptions getOptions(RestflowEnvironment environment) {
		if(environment == null) {
			return new VertxOptions();
		}
		boolean clusterEnabled = BooleanUtils.toBoolean(
				environment.getProperty(RestflowEnvironment.CLUSTER_ENABLED_PROPERTY));
		boolean haEnabled =  BooleanUtils.toBoolean(
						environment.getProperty(RestflowEnvironment.HA_ENABLED_PROPERTY));
		boolean metricsEnabled =  BooleanUtils.toBoolean(
		environment.getProperty(RestflowEnvironment.METRICS_ENABLED_PROPERTY));		
		VertxOptions options = new VertxOptions(
									new JsonObject(
									environment.getPropertiesAsMap(environment.getPropertiesByPrefix("vertx."))
								));
		configClusterManagerProperties(environment);
		if(haEnabled) {
			Integer quorumSize = environment.getProperty(
							RestflowEnvironment.HA_QUORUM_SIZE_PROPERTY, Integer.class);
			String haGroup = environment.getProperty(
							RestflowEnvironment.HA_GROUP_PROPERTY);
			options.setHAEnabled(haEnabled);
			if(quorumSize > 0) {
			options.setQuorumSize(quorumSize);
			}
			if(StringUtils.isNotEmpty(haGroup)) {
			options.setHAGroup(haGroup);
			}
		}
		if(metricsEnabled) {
			DropwizardMetricsOptions metrics = 
			new DropwizardMetricsOptions()
				.setEnabled(metricsEnabled);		
			boolean jmxEnabled = BooleanUtils.toBoolean(
			environment.getProperty(RestflowEnvironment.METRICS_JMX_ENABLED_PROPERTY));
			if(jmxEnabled) {
				metrics.setJmxEnabled(jmxEnabled)
					.setJmxDomain(environment.getProperty(
					RestflowEnvironment.METRICS_JMX_DOMIN_PROPERTY));
			}
		}
		if(clusterEnabled) {
			options.setClustered(clusterEnabled)
			.setClusterHost(environment.getProperty(RestflowEnvironment.CLUSTER_HOST_PROPERTY))
			.setClusterPublicHost(environment.getProperty(RestflowEnvironment.CLUSTER_PUBLIC_HOST_PROPERTY))
			.setClusterPort(environment.getProperty(RestflowEnvironment.CLUSTER_PORT_PROPERTY, Integer.class))
			.setClusterPublicPort(environment.getProperty(RestflowEnvironment.CLUSTER_PUBLIC_PORT_PROPERTY, Integer.class))
			.setClusterPingInterval(environment.getProperty(RestflowEnvironment.CLUSTER_PING_INTERVAL_PROPERTY, Long.class))
			.setClusterPingReplyInterval(environment.getProperty(RestflowEnvironment.CLUSTER_PING_REPLY_INTERVAL_PROPERTY, Long.class));
			String addrsPath = environment.getProperty(RestflowEnvironment.CUSTER_ADDRESSES_PATH_PROPERTY);
			if(StringUtils.isNotEmpty(addrsPath)) {
				options.setAddressResolverOptions(
						new AddressResolverOptions().setHostsPath(addrsPath));
			}
		}
		return options;
	}
	
	protected static void configClusterManagerProperties(RestflowEnvironment environment) {
		Properties properties = environment.getPropertiesByPrefix("hazelcast.");
		if(properties.isEmpty()) {
			return;
		}
		Set<String> propertyNames = properties.stringPropertyNames();
		propertyNames.stream()
						.forEach(propertyName -> {
							String value = properties.getProperty(propertyName);
							if(!StringUtils.isEmpty(value)) {
								System.setProperty(propertyName, value); 	
							}
						});
	}
}
