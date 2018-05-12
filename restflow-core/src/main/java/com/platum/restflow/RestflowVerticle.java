package com.platum.restflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Table;
import com.platum.restflow.components.ComponentFactory;
import com.platum.restflow.components.Controller;
import com.platum.restflow.resource.DownloadMethod;
import com.platum.restflow.resource.Resource;
import com.platum.restflow.resource.ResourceController;
import com.platum.restflow.resource.ResourceFactory;
import com.platum.restflow.resource.ResourceMetadata;
import com.platum.restflow.resource.ResourceMethod;
import com.platum.restflow.resource.UploadMethod;
import com.platum.restflow.resource.query.QueryBuilder;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class RestflowVerticle extends MicroserviceVerticle {
	
	private static final List<RestflowHttpMethod> standardHttpMethods = Arrays.asList(
			RestflowHttpMethod.GET, RestflowHttpMethod.GET_WITH_ID,
			RestflowHttpMethod.POST, RestflowHttpMethod.PUT_WITH_ID,
			RestflowHttpMethod.DELETE_WITH_ID
	);
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Restflow restflow;	
	
	private String staticAssetsPath;
	
	private String staticAssetsRoute;
	
	private String baseUrl;
	
	private Router router;
	
	public RestflowVerticle(Restflow restflow) {
		this.restflow = restflow;
	}
	
	@Override
	public void start() {
		vertx.createHttpServer(RestflowVertxStarter.getHttpOptions(restflow.getEnvironment()))
			  .requestHandler(resolveRouter()::accept)
			  .listen(
				      result -> {
				           if (result.succeeded()) {
				        	 if(logger.isInfoEnabled()) {
				        		 logger.info("Http server started with success");
				        	 }
				           } else {
				        	 logger.error("Could not start http server",result.cause());  
				           }
				         }
				     );
	}
	
	@Override
	public void stop() {
		try {
			super.stop();
			if(logger.isDebugEnabled()) {
				logger.debug("Restflow verticle stopped");
			}
		} catch(Throwable e) {
			if(logger.isWarnEnabled()) {
				logger.warn("Exception when stopping restflow verticle.", e);
			}
		}
	}
	
	public RestflowVerticle staticAssets(String staticAssetsRoute, String staticAssetsPath) {
		this.staticAssetsRoute = staticAssetsRoute;
		this.staticAssetsPath = staticAssetsPath;
		return this;
	}
	
	public RestflowVerticle baseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		return this;
	}
			
	protected void publishEndpoints() {
		//TODO microservices
	}
	
	protected Router resolveRouter() {
		router = Router.router(vertx);
		router.route().handler(CorsHandler.create("*")
			      .allowedMethod(HttpMethod.GET)
			      .allowedMethod(HttpMethod.POST)
			      .allowedMethod(HttpMethod.PUT)
			      .allowedMethod(HttpMethod.PATCH)
			      .allowedMethod(HttpMethod.DELETE)
			      .allowedMethod(HttpMethod.OPTIONS)
			      .allowedHeader("Access-Control-Allow-Method")
			      .allowedHeader("Access-Control-Allow-Origin")
			      .allowedHeader("Access-Control-Allow-Credentials")
			      .allowedHeader("Access-Control-Allow-Headers")
			      .allowedHeader("X-Requested-With")
			      .allowedHeader("Content-Type")
			      .allowedHeader("Accept")
			      .allowedHeader("enctype")
			      .allowedHeader("Authorization"));
		router.route()
				.pathRegex(".*(?<!(upload))$")
				.handler(BodyHandler.create());
		router.route().failureHandler(ResourceFactory.getResourceFailureHandler(restflow.getContext()));
		Table<String, String, Resource> resources = restflow.getAllResources();
		if(!resources.isEmpty()) {
			Set<String> resourceRowNames = resources.rowKeySet();
			resourceRowNames.stream().forEach(resourceName -> {
				Set<Entry<String, Resource>> versions = resources.row(resourceName).entrySet();
				versions.stream().forEach(resourceVersion -> {
					deployResourceRoutes(router, resourceVersion.getValue() , resourceVersion.getKey());			
				});
			});	
		}
		if(StringUtils.isNotEmpty(staticAssetsPath)) {
			if(StringUtils.isEmpty(staticAssetsRoute)) {
				staticAssetsRoute = "/assets/*";
			}
			router.route(staticAssetsRoute).handler(StaticHandler.create(staticAssetsPath));
		}
		publishControllerServices(router);
		return router;
	}

	
	protected void publishControllerServices(Router router) {
		List<Controller> controllers = ComponentFactory.getControllerServices();
		if(controllers != null && !controllers.isEmpty()) {
			controllers.stream().forEach(controller -> {
				controller.setRouter(router);
				controller.setRestflow(restflow);
				controller.createRoutes();
			});
		} else if(logger.isInfoEnabled()) {
			logger.info("No controller services found.");
		}
	}
		
	protected void deployResourceRoutes(Router router, Resource resource, String version) {
		if(resource.isInternal()) {
			return;
		}
		List<ResourceMethod> methods = resource.getMethods();
		ResourceMetadata<?> metadata = ResourceFactory.getResourceMetadataInstance(restflow, resource);
		if(resource.isGenerateCrud()){
			methods = generateResourceMethods(metadata);
			resource.setMethods(methods);
		}
		else if(methods == null || methods.isEmpty()) {
			return;
		}
		ResourceController<?>  resourceHandler =  ResourceFactory.getControllerInstance(metadata);
		methods.stream().forEach(method -> {
			if(!method.isInternal()) {
				if(StringUtils.isEmpty(method.getUrl())) {
					if(logger.isWarnEnabled()) {
						logger.warn("Public method ["+method.getName()+"] of resource ["+resource.getName()+
										"] has no url defined and will not be published.");
					}
				} else {
					RestflowRoute route = new RestflowRoute()
							.basicUrl(getBaseUrl(resource.getName(), version))
							.router(router)
							.method(method);
					deployMethodRoute(route, method, resourceHandler);	
				}
			}
		});
		DownloadMethod download = resource.getDownload();
		UploadMethod upload = resource.getUpload();
		boolean downloadActive = download != null && download.isActive();
		boolean uploadActive = upload != null && upload.isActive();
		if(downloadActive || uploadActive) {
			try {
				assertFileSystem(resource.getFileSystem());
				if(downloadActive) {
					RestflowRoute route = new RestflowRoute()
							.basicUrl(getBaseUrl(resource.getName(), version))
							.router(router)
							.download(download);
					resourceHandler.download(route, download);
					route.deploy();
				}
				if(uploadActive) {
					RestflowRoute route = new RestflowRoute()
							.basicUrl(getBaseUrl(resource.getName(), version))
							.router(router)
							.upload(upload);
					resourceHandler.upload(route, upload);
					route.deploy();		
				}					
			} catch(Throwable e) {
				if(logger.isWarnEnabled()) {
					logger.warn("Resource has download or upload enabled but routes where not deployed due to exception. ", e);
				}
			}		
		}
	}
		
	protected List<ResourceMethod> generateResourceMethods(ResourceMetadata<?> metadata) {
		final List<ResourceMethod> methods = metadata.resource().getMethods() == null
											 ? new ArrayList<>() 
											 : metadata.resource().getMethods();		
		standardHttpMethods.stream().forEach(httpMethod -> {
			ResourceMethod method = methods.stream()
									.filter(m -> m != null && httpMethod.equalValue(m.getUrl()))
									.findAny()
									.orElse(null);
			if(method == null) {
				QueryBuilder builder = metadata.getQueryBuilderInstance();	
				method = builder.generate(httpMethod).method();
				if(method != null) {
					methods.add(method);
				}
			}
		});
		return methods;
		
	}
	
	protected void deployMethodRoute(RestflowRoute route, ResourceMethod method,
			ResourceController<?>  resourceHandler) {
		String methodUrl = method.getUrl();
		if(StringUtils.isNotEmpty(methodUrl)) {
			if(RestflowHttpMethod.GET.urlIsHttpMethod(methodUrl)) {
				resourceHandler.get(route,method);
			} else if(RestflowHttpMethod.POST.urlIsHttpMethod(methodUrl)) {
				resourceHandler.post(route, method);
			} else if(RestflowHttpMethod.PUT.urlIsHttpMethod(methodUrl)) {
				resourceHandler.put(route, method);
			} else if(RestflowHttpMethod.PATCH.urlIsHttpMethod(methodUrl)) {
				resourceHandler.patch(route, method);
			} else if(RestflowHttpMethod.DELETE.urlIsHttpMethod(methodUrl)) {
				resourceHandler.delete(route, method);
			} else {
				if(logger.isDebugEnabled()) {
					logger.debug("Http method for "+methodUrl+" not supported.");
				}
				return;
			}
			route.deploy();
			if(logger.isInfoEnabled()) {
				logger.info("Method ["+method.getName()+"] deployed with url "+route.url());
			}
		} else {
			logger.info("Method url empty?"+methodUrl);
		}
	}

	protected String getBaseUrl(String resource, String version) {
		StringBuilder urlBuilder = new StringBuilder();
		if(StringUtils.isNotEmpty(baseUrl)) {
			if(!baseUrl.startsWith("/")) {
				urlBuilder.append("/"+baseUrl);
			} else {
				urlBuilder.append(baseUrl);
			}
		}
		return urlBuilder.append("/"+version)
				  .append("/"+resource)
				  .toString();
	}
	
	protected void assertFileSystem(String fileSystemName) {
		Validate.notEmpty(fileSystemName, "Resource filesystem not provided.");
		restflow.getFileSystem(fileSystemName);
	}
	
}