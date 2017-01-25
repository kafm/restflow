package com.platum.restflow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.platum.restflow.exceptions.ResflowNotExistsException;
import com.platum.restflow.exceptions.RestflowDuplicatedRefException;
import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.impl.RestflowEnvironmentImpl;
import com.platum.restflow.mail.RestflowMail;
import com.platum.restflow.mail.impl.RestflowMailImpl;
import com.platum.restflow.resource.Resource;
import com.platum.restflow.resource.ResourceMethod;
import com.platum.restflow.resource.annotation.HookManager;
import com.platum.restflow.utils.ClassUtils;
import com.platum.restflow.utils.ResourceUtils;

import io.vertx.core.Vertx;

public class Restflow extends RestflowDefaultConfig {
	
	private static final String logPathSystemProperty = "logback.configurationFile";
	
	private static final Pattern paramPattern = Pattern.compile("\\:[\\w|\\w\\.\\w]+");

	private final  Logger logger = LoggerFactory.getLogger(getClass());
	
	private RestflowEnvironment environment;
	
	private RestflowContext context;
	
	private Map<String, DatasourceDetails> datasources;
	
	private Map<String, FileSystemDetails> fileSystems;
	
	private HookManager hookManager;
	
	private Vertx vertx;

	private Table<String, String, Resource> resources;
	
	private RestflowMail mailService;
	
	public Restflow() {
		environment = new RestflowEnvironmentImpl();
		context = new RestflowContext();
		datasources = Maps.newHashMap();
		fileSystems = Maps.newHashMap();
		resources = HashBasedTable.create();
	}

	public Restflow config() {
		return config(null);
	}
	
	public Restflow config(String configPath) {		
		List<String> configPaths = new ArrayList<>();
		if(!StringUtils.isEmpty(configPath)) {
			configPaths.add(resolvePathName(configPath)+RestflowEnvironment.DEFAULT_CONFIG_FILE);
		} else {
			configPaths = DEFAULT_CONFIG_PATHS;
		}
		configPaths.stream()
				   .forEach(path -> loadToEnvironment(path));
		if(logger.isInfoEnabled()) {
			logger.info("Configuration loaded to environment.");
		}
		FactoryLoader.load();
		configLog();
		loadMessages();
		loadModels();
		return this;
	}
			
	public Restflow run(){
		return run(false);
	}
	
	public Restflow run(boolean staticResources) {
		if(staticResources) {
			if(!datasources.isEmpty()) {
				datasources = new ImmutableMap.Builder<String, DatasourceDetails>()
											  .putAll(datasources)
											  .build();	
			}
			if(!fileSystems.isEmpty()) {
				fileSystems = new ImmutableMap.Builder<String, FileSystemDetails>()
											  .putAll(fileSystems)
											  .build();	
			}
			if(!resources.isEmpty()) {
				resources = new ImmutableTable.Builder<String, String, Resource>()
											  .putAll(resources)
											  .build();	
			}			
		}
		RestflowVertxStarter.startVertx(environment, res -> {
			if(res.succeeded()) {
				//TODO deploy verticle with num instances >=1
				vertx = res.result();
				vertx.deployVerticle(new RestflowVerticle(this)
										.baseUrl(environment.getProperty(RestflowEnvironment.BASE_URL_PROPERTY))
										.staticAssets(environment.getProperty(RestflowEnvironment.ASSETS_ROUTE_PROPERTY),
												environment.getProperty(RestflowEnvironment.ASSETS_PATH_PROPERTY)));
				mailService = new RestflowMailImpl(this)
						.config();
			} else {
				throw new RestflowException("Unable to start vertx core.", res.cause());
			}
		});
		return this;
	}
	
	public DatasourceDetails getDatasource(String name) {
		DatasourceDetails datasourceProperties = datasources.get(name);
		if(datasourceProperties == null) {
			throw new ResflowNotExistsException("Datasource ["+name+"] does not exists.");
		}
		return datasourceProperties;
	}
	
	public FileSystemDetails getFileSystem(String name) {
		FileSystemDetails fsProperties = fileSystems.get(name);
		if(fsProperties == null) {
			throw new ResflowNotExistsException("FileSystem ["+name+"] does not exists.");
		}
		return fsProperties;
	}
	
	public HookManager getHookManager() {
		return hookManager;
	}

	public Resource getResource(String name) {
		return getResource(name, null);
	}
	
	public Vertx vertx() {
		return vertx;
	}
	
	public RestflowMail mailService() {
		return mailService;
	}

	public Resource getResource(String name, String version) {
		Map<String, Resource> resourceVersions = resources.row(name);
		if(resourceVersions != null && !resourceVersions.isEmpty()) {
			Resource resource = resourceVersions.get( (StringUtils.isEmpty(version))? 
											resourceVersions.keySet().iterator().next() : version);
			if(resource != null) {
				return resource;
			}
		}
		throw new ResflowNotExistsException("Resource ["+name+version+"] does not exists.");
	}

	public Map<String, FileSystemDetails> getAllFileSystems() {
		return fileSystems;
	}
	
	public Map<String, DatasourceDetails> getAllConnections() {
		return datasources;
	}
	
	public Table<String, String, Resource> getAllResources() {
		return resources;
	}
	
	public Resource createResource(String name, Resource resource) {
		return createResource(name, resource, RestflowModel.DEFAULT_VERSION);
	}

	public Resource createResource(String name, Resource resource, String version) {
		Validate.notEmpty(name);
		Validate.notEmpty(version);
		String lName = name.toLowerCase();
		String lVersion = version.toLowerCase();
		if(resources.contains(lName, lVersion)) {
			throw new RestflowDuplicatedRefException("Resource ["+lName+lVersion+"] already exists!");
		}
		return resources.put(name, version, resource);
	}
		
	public Restflow loadModels() {
		String modelsPath = environment.getProperty(RestflowEnvironment.MODELS_PATH_PROPERTY);
		List<URL> urls = getUrlsOrElse(modelsPath, DEFAULT_MODELS_PATH);
		if(!urls.isEmpty()) {
			loadModels(urls);
		} else if(logger.isDebugEnabled()) {
			logger.debug("No models additional configuration found");
		}
		return this;
	}  
	
	public Restflow loadModels(List<URL> modelDirUrls) {
		Validate.notEmpty(modelDirUrls);
		logger.info(modelDirUrls.toString());
		modelDirUrls.stream()
					.forEach(modelDirUrl -> {
						List<File> candidates = getCandidateFiles(modelDirUrl);
						if(!candidates.isEmpty()) {
							candidates.stream()
										.forEach(candidate -> loadFile(candidate));
						}
					});
		if(logger.isInfoEnabled()) {
			logger.info("Models loaded.");
		}
		return this;
	}
	
	public Restflow loadModels(RestflowModel ... models) {
		Validate.notEmpty(models);
		Stream.of(models).forEach(model -> {
			String version = model.getVersion().toLowerCase(); 
			loadDatasources(model.getDatasources());
			loadFileSystems(model.getFileSystems());
			loadResources(model.getResources(), version);
		});
		return this;
	}
	
	public Restflow loadFile(File file) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(RestflowModel.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			RestflowModel model = (RestflowModel) 
									jaxbUnmarshaller.unmarshal(file);
			loadModels(model);
			if(logger.isInfoEnabled()) {
				logger.info("Model loaded from xml file ["+file.getName()+"].");
			}
		} catch (Throwable e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Ignoring xml ["+file.getName()+"].", e);
			}
		}
		return this;		
	}
		
	public Restflow loadToEnvironment(Properties properties) {
		Validate.notNull(properties);
		environment.setProperties(properties);	
		return this;
	}

	public Restflow loadToEnvironment(String propertiesFileName) {
		Validate.notNull(propertiesFileName);
		try {
			InputStream is = ResourceUtils.getInputStream(propertiesFileName);
			if(is != null) {
				environment.load(is);	
			} else if(logger.isWarnEnabled()) {
				logger.debug("File ["+propertiesFileName+"] does not exists.");
			}
		} catch (IOException e) {
			if(logger.isDebugEnabled()) {
				logger.debug("Could not open properties file "+propertiesFileName+".", e);
			}
		}
		return this;
	}
	
	public RestflowEnvironment getEnvironment() {
		return environment;
	}
	
	public RestflowContext getContext() {
		return context;
	}
	
	public Restflow loadMessages() {
		String messagesPath = environment.getProperty(RestflowEnvironment.MESSAGES_PATH_PROPERTY);
		String messagesBaseName = environment.getProperty(RestflowEnvironment.MESSAGES_NAME_PROPERTY);
		String messagesDefaultLocale = environment.getProperty(RestflowEnvironment.MESSAGES_DEFAULT_LOCALE_PROPERTY);
		List<URL> urls = getUrlsOrElse(messagesPath, DEFAULT_MESSAGES_PATH);
		ClassLoader loader = null;
		Locale locale = null;
		if(!urls.isEmpty()) {
			 loader = ClassUtils.getClassLoaderByUrl(urls.toArray(new URL[urls.size()]));
		} else if(logger.isDebugEnabled()) {
			logger.debug("No messages resource additional configuration found.");
		}
		if(!StringUtils.isEmpty(messagesDefaultLocale)) {
			locale = LocaleUtils.toLocale(messagesDefaultLocale);
		}
		context.configMessageProvider(messagesBaseName, locale, loader);
		if(logger.isInfoEnabled()) {
			logger.info("Messages resources configuration loaded");
		}
		return this;
	}
	
	public void resolveResourceMethods(Resource resource) {
		List<ResourceMethod> methods = resource.getMethods();
		if(methods != null && !methods.isEmpty()) {
			methods.stream().forEach(method -> {
				String query = method.getQuery();
				if(StringUtils.isNotEmpty(query)) {
					Matcher matcher = paramPattern.matcher(query);
					List<String> params = new ArrayList<>();
			        while (matcher.find()) {
			        	String param = matcher.group();
			        	params.add(param.substring(1));
			        }		
			        if(!params.isEmpty()) {
			        	method.setParams(params.toArray(new String[params.size()]));
			        }
				}
			});
		}
	}
	
	private void loadDatasources(List<DatasourceDetails> datasourcesToLoad) {
		if(datasourcesToLoad == null || datasourcesToLoad.isEmpty()) {
			return;
		}
		datasourcesToLoad.stream()
						.forEach(newDatasource -> {
							String key = newDatasource.getName();
							if(datasources.containsKey(key)) {
								throw new RestflowDuplicatedRefException("Datasource ["+key+"] already exists");
							} else {
								datasources.put(key, newDatasource);
							}
							if(logger.isInfoEnabled()) {
								logger.info("Datasource ["+key+"] loaded.");
							}
						});
	}
	
	private void loadFileSystems(List<FileSystemDetails> fileSystemsToLoad) {
		if(fileSystemsToLoad == null || fileSystemsToLoad.isEmpty()) {
			return;
		}
		fileSystemsToLoad.stream()
						.forEach(newFs -> {
							String key = newFs.getName();
							if(fileSystems.containsKey(key)) {
								throw new RestflowDuplicatedRefException("Filesystem ["+key+"] already exists");
							} else {
								fileSystems.put(key, newFs);
							}
							if(logger.isInfoEnabled()) {
								logger.info("Filesystem ["+key+"] loaded.");
							}
						});
	}
	
	private void loadResources(List<Resource> resourcesToLoad, String version) {
		if(resourcesToLoad == null || resourcesToLoad.isEmpty()) {
			return;
		}
		resourcesToLoad.stream()
						.forEach(newResource -> {
							String key  = newResource.getName();
							if(StringUtils.isEmpty(key)) {
								throw new RestflowException("Resource with empty name.");
							}
							key = key.toLowerCase();
							if(resources.contains(key, version)) {
								throw new RestflowDuplicatedRefException("Resource ["+key+"] already exists");
							} else {
								resolveResourceMethods(newResource);
								resources.put(key, version, newResource);
							}
							if(logger.isInfoEnabled()) {
								logger.info("Resource ["+key+"] with version "+version+" loaded.");
							}
						});
	}
	
	private void configLog() {
		String location = environment.getProperty("LOG_CONFIG_PROPERTY");
		InputStream in = null;
		try {
			if(location == null) {
				location = System.getProperty(logPathSystemProperty);
				in = ResourceUtils.getInputStream(location);
			}
			if(location == null || in == null) {	
				for(String l : ALTERNATIVE_LOG_LOCATIONS) {
					for(String ext : LOG_EXT) {
						in = ResourceUtils.getInputStream(l+ext);
					}
					if(in != null) {
						break;
					}
				}
			}
			if(in != null) {
				LogManager.getLogManager().readConfiguration(in);
				if(logger.isInfoEnabled()) {
					logger.info("Log configuration loaded");
				}
			} else if(logger.isDebugEnabled()) {
				logger.debug("No log configuration file found");
			}
		} catch(Throwable e) {
			if(logger.isDebugEnabled()) {
				logger.info("Unable to load log configuration.", e);
			}
		}
	}
	
	private boolean isCandidateFile(File file) {
		String ext = FilenameUtils.getExtension(file.getName());
		return (ext != null && XML_EXTS.contains(ext));
	}
	
	private List<File> getCandidateFiles(URL dirUrl) {
		List<File> candidates = new ArrayList<>();
		try {
			File dir = new File(dirUrl.getFile());
			if(dir.isDirectory()) {
				File[] files = dir.listFiles();
				if(files != null) {
					Stream.of(files)
							.filter(file -> !file.isDirectory() && isCandidateFile(file))
							.forEach(file -> {
								candidates.add(file);
							});
				}
			}				
		} catch(Throwable e) {
			logger.warn("Unable to open model directory ["+dirUrl+"] ", e);
		}
		return candidates;
	}
	
	private List<URL> getUrlsOrElse(String urlToLoad, List<String> urlsToLoadIfNotExists) {
		List<URL> urls = new ArrayList<>();
		if(!StringUtils.isEmpty(urlToLoad)) {
			URL url = ResourceUtils.getURL(urlToLoad);
			if(url != null) {
				urls.add(url);
			}
		}
		if(urls.isEmpty()) {
			urlsToLoadIfNotExists.stream()
									.forEach(candidatePath -> {
										URL candidateUrl = ResourceUtils.getURL(candidatePath);
										if(candidateUrl != null) {
											urls.add(candidateUrl);
										}
									});
		} 
		return urls;
	}

	private String resolvePathName(String path) {
		Validate.notEmpty(path);
		if(!path.endsWith(File.separator)) {
			path += File.separator;
		}
		return path;
	}

}
