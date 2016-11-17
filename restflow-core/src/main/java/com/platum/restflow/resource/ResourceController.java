package com.platum.restflow.resource;

import java.io.File;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.platum.restflow.RestflowDefaultConfig;
import com.platum.restflow.RestflowEnvironment;
import com.platum.restflow.RestflowHttpMethod;
import com.platum.restflow.RestflowRoute;
import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.resource.impl.AbstractResourceComponent;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;

/**
 * TODO refactor to use RestflowController
 * */
public class ResourceController<T> extends AbstractResourceComponent<T>{
	
	private final Logger logger = LoggerFactory.getLogger(getClass()); 
	
	private Resource resource;
	
	private ResourceFileSystem fileSystem;
	
	private String tmpPath;
	
	public ResourceController(ResourceMetadata<T> metadata) {
		super(metadata);
		resource = metadata.resource();
		if(StringUtils.isNotEmpty(resource.getFileSystem())) {
			fileSystem = ResourceFactory.getFileSystemInstance(metadata);
		}
		resolveTempPath();
	}
				
	public ResourceController<T> get(RestflowRoute route, ResourceMethod method) {
		if(RestflowHttpMethod.GET.equalValue(method.getUrl())) {
			route
			.httpMethod(RestflowHttpMethod.GET, routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);
				helper.logRequest(method)
				.service()
				.find(method, helper.getFilterFromRequest(), helper.getModifierFromRequest())
				.success(data -> { 
					helper.end(HttpResponseStatus.OK, data);
				})
				.error(error -> {
					helper.fail(error);
				});
			});			
		} else if(RestflowHttpMethod.GET_WITH_ID.equalValue(method.getUrl())) {
			route.httpMethod(RestflowHttpMethod.GET_WITH_ID, routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);
				helper.logRequest(method)
				.service()
				.get(method, new Params().addParam(metadata.idPropertyName()
								, helper.getRequestIdParam()))
				.success(data -> { 
					helper.end(HttpResponseStatus.OK, data);
				})
				.error(error -> {
					helper.fail(error);
				});
			});			
		} else {
			route.httpMethod(RestflowHttpMethod.GET, routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);
				helper.logRequest(method)
				.service()
				.find(method, helper.getFilterFromRequest(), helper.getModifierFromRequest())
				.success(data -> { 
					helper.end(HttpResponseStatus.OK, data);
				})
				.error(error -> {
					helper.fail(error);
				});
			});
		}
		return this;
	}

	public ResourceController<T> post(RestflowRoute route, ResourceMethod method) {
		if(RestflowHttpMethod.POST.equalValue(method.getUrl())) {
			route.httpMethod(RestflowHttpMethod.POST, routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);			
				helper.logRequest(method)
				.service()
				.insert(method, helper.getRequestResourceObject())
				.success(data -> { 
					helper.end(HttpResponseStatus.CREATED, data);
				})
				.error(error -> {
					helper.fail(error);
				});
			});
		} else {
			route.httpMethod(RestflowHttpMethod.POST, routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);									   
				helper.logRequest(method)
				.service()
				.insert(method, helper.getRequestResourceObject(), helper.getParamsFromRequest())
				.success(data -> { 
					helper.end(HttpResponseStatus.CREATED, data);
				})
				.error(error -> {
					helper.fail(error);
				});
			});
		}
			
		return this;
	}

	public ResourceController<T> put(RestflowRoute route, ResourceMethod method) {
		if(RestflowHttpMethod.PUT_WITH_ID.equalValue(method.getUrl())) {
			route.httpMethod(RestflowHttpMethod.PUT_WITH_ID, routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);		
				helper.logRequest(method)
				.service()
				.update(method, metadata.setObjectId(
									helper.getRequestResourceObject(), helper.getRequestIdParam()))
				.success(data -> { 
					helper.end(HttpResponseStatus.OK, data);
				})
				.error(error -> {
					helper.fail(error);
				});
			})	;		
		} else {
			route.httpMethod(RestflowHttpMethod.PUT,routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);		
				helper.logRequest(method)
				.service()
				.update(method, metadata.setObjectId(helper.getRequestResourceObject(), 
									helper.getRequestIdParam()), helper.getParamsFromRequest())
				.success(data -> { 
					helper.end(HttpResponseStatus.OK, data);
				})
				.error(error -> {
					helper.fail(error);
				});
			});
		}
		return this;
	}

	public ResourceController<T> patch(RestflowRoute route, ResourceMethod method) {
		route
		.httpMethod(RestflowHttpMethod.PATCH, routingContext -> {
			ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);		
			helper.logRequest(method)
			.service()
			.partialUpdate(method, metadata.setObjectId(helper.getRequestResourceObject(), 
															helper.getRequestIdParam()),
							helper.getParamsFromRequest())
			.success(data -> { 
				helper.end(HttpResponseStatus.OK, data);
			})
			.error(error -> {
				helper.fail(error);
			});
		});
		return this;
	}

	public ResourceController<T> delete(RestflowRoute route, ResourceMethod method) {
		if(RestflowHttpMethod.DELETE_WITH_ID.equalValue(method.getUrl())) {
			route.httpMethod(RestflowHttpMethod.DELETE_WITH_ID, routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);	
				final ResourceService<T> service = helper.logRequest(method).service();
				service.get(helper.getRequestIdParam())
				.success(object -> {
					service.delete(helper.getRequestIdParam())
					.success(v -> {
						helper.end(HttpResponseStatus.OK);
					})
					.error(error -> {
						helper.fail(error);
					});
				})
				.error(error -> {
					helper.fail(error);
				});
			});			
		} else {
			route.httpMethod(RestflowHttpMethod.DELETE, routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);		
				helper.logRequest(method)
				.service()
				.delete(method, helper.getParamsFromRequest())
				.success(v -> { 
					helper.end(HttpResponseStatus.OK);
				})
				.error(error -> {
					helper.fail(error);
				});
			});
		} 
		return this;
	}
	
	public ResourceController<T> upload(RestflowRoute route, UploadMethod uploadMethod) {
		RestflowHttpMethod httpMethod = RestflowHttpMethod.UPLOAD;
		route.httpMethod(httpMethod, routingContext -> {
			if(logger.isInfoEnabled()) {
				logger.info("Upload request for resource [" +
							resource.getName() + 
							"] with path " + 
							httpMethod.value());
			}
			HttpServerRequest req = routingContext.request();
			HttpServerResponse res = routingContext.response();
			String id = req.getParam(RestflowDefaultConfig.DEFAULT_ID_PARAM);
			if(fileSystem == null) {
				routingContext.fail(new RestflowException("Filesystem not found."));
			} else if(StringUtils.isEmpty(id)) {
				routingContext.fail(new RestflowException("Invalid id provided."));
			} else {
				req.setExpectMultipart(true)				
			       .uploadHandler(upload -> {
					  String uploadedFileName = new File(tmpPath, UUID.randomUUID().toString()).getPath();
					  upload.streamToFileSystem(uploadedFileName);
			          upload.exceptionHandler(routingContext::fail)
			          		.endHandler(v -> {
			          		fileSystem.save(new ResourceFile()
											.id(id)
											.uploaded(true)
											.path(uploadedFileName)
											.resourceName(uploadMethod.getUseResource())
											.fileName(upload.filename()))
			          		.success(s -> {
					        	  res.setChunked(true)
					        	  	 .setStatusCode(HttpResponseStatus.OK.code())
					        	  	 .end();			          			
			          		}).error(err -> {
					        	  routingContext.fail(err);
					        });
			          });
			        });
			}
		});
		return this;
	}
	
	public ResourceController<T> download(RestflowRoute route, DownloadMethod download) {
		RestflowHttpMethod httpMethod =RestflowHttpMethod.DOWNLOAD;
		route.httpMethod(httpMethod, routingContext -> {
			HttpServerRequest req = routingContext.request();
			HttpServerResponse res = routingContext.response();
			if(logger.isInfoEnabled()) {
				logger.info("Download request for resource [" +
							resource.getName() + 
							"] with path " + 
							httpMethod.value());
			}
			String id = req.getParam(RestflowDefaultConfig.DEFAULT_ID_PARAM);
			if(fileSystem == null) {
				routingContext.fail(new RestflowException("Filesystem not found."));
			} else if(StringUtils.isEmpty(id)) {
				routingContext.fail(new RestflowException("Invalid id provided."));
			} else {
				fileSystem.get(id)
				.success(file -> {
					ReadStream<Buffer> readStream = file.stream();
					readStream.endHandler(s -> {
						res.putHeader("Content-Type", "application/octet-stream")
						.putHeader("content-disposition", "attachment; filename=\"" + file.fileName() +"\"")
   						.setStatusCode(HttpResponseStatus.OK.code())
   						.end();
					}).exceptionHandler(routingContext::fail);
					res.setChunked(true);
					Pump p = Pump.pump(file.stream(), res);
					p.start();					
				})
				.error(routingContext::fail);		
			}
		});
		return this;
	}
			
	@Override
	public void close() {
		//TODO log yourself
	}

	private void resolveTempPath() {
		tmpPath = metadata.restflow()
				  .getEnvironment()
				  .getProperty(RestflowEnvironment.TMP_PATH_PROPERTY);
		if(StringUtils.isEmpty(tmpPath)) {
			tmpPath = "./tmp/";
		} else if(!tmpPath.endsWith("/")) {
			tmpPath += "/";
		}
		File file = new File(tmpPath);
		if(!file.exists()) {
			file.mkdirs();
		}
	}
	
}