package com.platum.restflow.resource.impl;

import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.platum.restflow.FileSystemDetails;
import com.platum.restflow.exceptions.ResflowNotExistsException;
import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.resource.ResourceFile;
import com.platum.restflow.resource.ResourceFileSystem;
import com.platum.restflow.resource.ResourceMetadata;
import com.platum.restflow.utils.promise.Promise;
import com.platum.restflow.utils.promise.PromiseFactory;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.Json;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;

public class SimpleResourceFileSystem<T> extends AbstractResourceComponent<T>  implements ResourceFileSystem {
	
	private static final String FILE_PATH_PROPERTY = "path";
	
	private static final String FILE_NAME = "_file";
	
	private static final String FILE_METADATA = "_metadata";
	
	private final  Logger logger = LoggerFactory.getLogger(getClass());

	private Vertx vertx;
	
	private String fileSystemName;
	
	private String filePath;
	
	public SimpleResourceFileSystem(ResourceMetadata<T> metadata) {
		super(metadata);
		this.vertx = metadata.restflow().vertx();
		config(metadata.fileSystem());
	}

	@Override
	public Promise<Void> create(ResourceFile file) {
		return save(file);
	}
	
	@Override
	public Promise<Void> save(ResourceFile file) {
		assertValidFile(file);
		Promise<Void> promise = PromiseFactory.getPromiseInstance();
		String path = getObjectPath(file.resourceName(), file.id()); 
		resolveDirectories(path)
		.success(dir -> {
			saveFileMetadata(file, path)
			.success(success -> {
				FileSystem fs = vertx.fileSystem();
				String fileName = path+FILE_NAME;
				if(file.uploaded() && StringUtils.isNotEmpty(file.path())) {
					try {
						if(fs.existsBlocking(fileName)) {
							fs.deleteBlocking(fileName);
						}
						fs.moveBlocking(file.path(), fileName);
						promise.resolve();
					} catch(Throwable e) {
						promise.reject(new RestflowException(e));
					}			
				} else {
					writeToFile(fileName, file.stream())
					.success(s -> {
						promise.resolve();
					}).error(err -> {
						promise.reject(err);
					});
				}
			}).error(err -> {
				promise.reject(new RestflowException(err));
			});			
		}).error( err -> {
			promise.reject(err);
		});			
		return promise;
	}
	
	@Override
	public Promise<Void> destroy(Object id) {
		Promise<Void> promise = PromiseFactory.getPromiseInstance();
		assertValidId(id);
		FileSystem fs = vertx.fileSystem();
		String path = getObjectPath(metadata.resource().getName(), id);
		fs.exists(path, exists -> {
			if(exists.succeeded()) {
				if(exists.result()) {
					fs.delete(path, deleted -> {
						if(deleted.succeeded()) {
							promise.resolve();
						} else {
							promise.reject(deleted.cause());
						}
					});
				} else {
					promise.resolve();
				}
			} else {
				promise.reject(exists.cause());;
			}
		});
		return promise;		
	}

	@Override
	public Promise<ResourceFile> get(Object id) {
		assertValidId(id);
		Promise<ResourceFile> promise = PromiseFactory.getPromiseInstance();
		FileSystem fs = vertx.fileSystem();
		String path = getObjectPath(metadata.resource().getName(), id.toString());
		try {
			if(fs.existsBlocking(path)) {
				ResourceFile file = metadataToFileObject(fs.readFileBlocking(path+FILE_METADATA));
				vertx.fileSystem().open(path+FILE_NAME, new OpenOptions(), ar -> {
					if(ar.succeeded()) {
						file.stream(ar.result());
						file.path(path+FILE_NAME);
						promise.resolve(file);
					} else {
						promise.reject(new RestflowException(ar.cause()));
					}
				});
				
			} else {
				promise.reject(new ResflowNotExistsException(
						"File ["+metadata.resource().getName() +
									","+id+"] does not exists."));
			}
		} catch(Throwable e) {
			promise.reject(new RestflowException(e));
		}
		return promise;		
	}

	@Override
	public void close() {
		if(logger.isDebugEnabled()) {
			logger.debug("Filesystem ["+fileSystemName+"] closed.");
		}
	}
	
	private Promise<Void> writeToFile(String path, ReadStream<Buffer> readStream) {
		Promise<Void> promise = PromiseFactory.getPromiseInstance();
		if(readStream != null) {
			vertx.fileSystem().open(path, new OpenOptions(), res -> {
				if(res.succeeded()) {
					AsyncFile file = res.result();
					file.endHandler(end -> {
						promise.resolve();
					}).exceptionHandler(err -> {
						promise.reject(new RestflowException(err));
			        });
					Pump.pump(readStream, file).start();
				} else {
					promise.reject(new RestflowException(res.cause()));
				}
			});
		} else {
			vertx.fileSystem().createFileBlocking(path);
			promise.resolve();
		}	
		return promise;
	}
		
	private Promise<Void> saveFileMetadata(ResourceFile file, String path) {
		Promise<Void> promise = PromiseFactory.getPromiseInstance();
		vertx.fileSystem().open(path+FILE_METADATA, new OpenOptions(), ar -> {
			if(ar.succeeded()) {
				String json = Json.encode(new ResourceFile()
						.fileName(file.fileName())
						.setNew(false)
						.resourceName(file.resourceName())
						.id(file.id()));
				ar.result().end(Buffer.buffer(json));	
				promise.resolve();
			} else {
				promise.reject(ar.cause());
			}
		});
		return promise;
	}

	private void assertValidFile(ResourceFile file) {
		Validate.notNull(file);
		assertValidId(file.id());
		if(StringUtils.isEmpty(file.fileName())) {
			file.fileName(UUID.randomUUID().toString());
		}
		if(StringUtils.isEmpty(file.resourceName())) {
			file.resourceName(metadata.resource().getName());
		}/* else if(!file.resourceName()
					.equalsIgnoreCase(metadata.resource().getName())) {
			throw new RestflowException("Expecting resource "+metadata.resource().getName()+
							" but got "+file.resourceName());
		}*/
		

	}

	private void assertValidId(Object id) {
		Validate.notNull(id);
		Validate.notEmpty(id.toString());
		Validate.notNull(metadata.resource());
		Validate.notEmpty(metadata.resource().getName());
	}
	
	private String getObjectPath(String resourceName, Object objectId) {
		return filePath +
					  resourceName + 
					  "/" +
					  objectId.toString()+
					  "/"; 
	}
	
	private Promise<Void> resolveDirectories(String path) {
		Promise<Void> promise = PromiseFactory.getPromiseInstance();
		vertx.fileSystem().exists(path, res -> {
			if(res.succeeded()) {
				vertx.fileSystem().mkdirs(path, h -> {
					if(h.succeeded()) {
						promise.resolve();
					} else {
						promise.reject(new RestflowException(h.cause()));
					}
				});
			} else {
				promise.reject(new RestflowException(res.cause()));
			}
		});
		return promise;
	}
	
	private ResourceFile metadataToFileObject(Buffer metadataFile) {
		Validate.notNull(metadataFile);
		return Json.decodeValue(metadataFile.toString(), ResourceFile.class);
	}
	
	private void config(FileSystemDetails fileSystem) {
		Validate.notNull(fileSystem);
		fileSystemName = fileSystem.getName();
		Properties properties = fileSystem.getProperties();
		filePath = "./";
		if(properties != null) {
			String path = properties.getProperty(FILE_PATH_PROPERTY);
			if(StringUtils.isNotEmpty(path)) {
				filePath = path;
				if(!filePath.endsWith("/")) {
					filePath += "/";
				}
			}
		}
		if(logger.isDebugEnabled()) {
			logger.debug("Filesystem ["+fileSystemName+"] configured with path "+filePath);
		}
	}
}
