package com.platum.restflow.resource.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.platum.restflow.AuthMetadata;
import com.platum.restflow.RestflowHttpMethod;
import com.platum.restflow.exceptions.RestflowInvalidRequestException;
import com.platum.restflow.resource.ObjectContext;
import com.platum.restflow.resource.Params;
import com.platum.restflow.resource.ResourceFactory;
import com.platum.restflow.resource.ResourceMetadata;
import com.platum.restflow.resource.ResourceMethod;
import com.platum.restflow.resource.ResourceObject;
import com.platum.restflow.resource.ResourceRepository;
import com.platum.restflow.resource.ResourceService;
import com.platum.restflow.resource.annotation.HookInterceptor;
import com.platum.restflow.resource.annotation.HookType;
import com.platum.restflow.resource.property.ResourcePropertyValidator;
import com.platum.restflow.resource.query.QueryBuilder;
import com.platum.restflow.resource.query.QueryFilter;
import com.platum.restflow.resource.query.QueryModifier;
import com.platum.restflow.resource.transaction.RepositoryTransaction;
import com.platum.restflow.utils.promise.Promise;
import com.platum.restflow.utils.promise.PromiseFactory;

import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

public class ResourceServiceImpl<T> extends AbstractResourceComponent<T> implements ResourceService<T> {

	private final  Logger logger = LoggerFactory.getLogger(getClass());
	
	private static final String WORKER_POOL_NAME ="DB_QUERY_POOL";
	
	private ResourceRepository<T> repository;

	private Vertx vertx;
	
	private AuthMetadata auth;
	
	private Map<HookType, HookInterceptor> hooks;
	
	private boolean hookContext;
	
	private RepositoryTransaction<?> transaction;
	
	private String lang;
	
	public ResourceServiceImpl(ResourceMetadata<T> resourceMetadata) {
		super(resourceMetadata);
		this.repository = ResourceFactory.getRespositoryInstance(resourceMetadata);
		this.vertx = resourceMetadata.restflow().vertx();
	}

	@Override
	public <I> Promise<T> get(I id) {
		Promise<T> promise = PromiseFactory.getPromiseInstance();
		WorkerExecutor executor = vertx.createSharedWorkerExecutor(WORKER_POOL_NAME);
		executor.executeBlocking(future -> {
			if(id == null) {
				throw new RestflowInvalidRequestException("Get object with null id it's not allowed");
			}
			ResourceMethod method = metadata.getQueryBuilderInstance()
					 .method(metadata.resource().getMethodByUrl(
							 	RestflowHttpMethod.GET_WITH_ID.value()))
					 .generateGetByIdIfNotFound()
					 .method();
			future.complete(method);
		}, false, res -> {
			executor.close();
			if(res.succeeded()) {
				promise.wrap(get((ResourceMethod) res.result(), 
						new Params().addParam("id", id)));				
			} else {
				promise.reject(res.cause());
			}
		});
		return promise;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Promise<T> get(ResourceMethod method, Params params) {
		Promise<T> promise = PromiseFactory.getPromiseInstance();
		WorkerExecutor executor = vertx.createSharedWorkerExecutor(WORKER_POOL_NAME);
		executor.executeBlocking(future -> {
			assertValidMethod(method);
			T object  = repository.get(method, params);
			HookInterceptor hook = getHook(HookType.GET);
			if(hook == null) {
				future.complete(object);
			} else {
				hook.invoke(new ObjectContextImpl<T>(
						metadata.resource(), this, object)
						.lang(lang))
				.success(prunnedObj -> future.complete(prunnedObj))
				.error(error -> future.fail(error));
			}
		}, false, result -> {
			executor.close();
			if(result.succeeded()) {
				promise.resolve((T) result.result());
			} else {
				promise.reject(result.cause());
			}
		});
		return promise;
	}
	
	@Override
	public Promise<List<ResourceObject>> find(QueryFilter filter) {
		return find(filter, null);
	}
	
	@Override
	public Promise<List<ResourceObject>> find(QueryFilter filter, QueryModifier modifer) {
		ResourceMethod method = metadata.getQueryBuilderInstance()
				 .method(metadata.resource().getMethodByUrl(
							RestflowHttpMethod.GET.value()))
				 .generateGetIfNotFound()
				 .method();
		return find(method, filter, modifer);
	}
	
	@Override
	public Promise<List<ResourceObject>> find(ResourceMethod method, QueryFilter filter) {
		return find(method, filter, null);
	}
	
	@Override
	public Promise<List<ResourceObject>> find(ResourceMethod method, QueryFilter filter, QueryModifier modifier) {
		return find(method, filter, modifier, null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Promise<List<ResourceObject>> find(ResourceMethod method, QueryFilter filter, QueryModifier modifier, Params params) {
		Promise<List<ResourceObject>> promise = PromiseFactory.getPromiseInstance();
		WorkerExecutor executor = vertx.createSharedWorkerExecutor(WORKER_POOL_NAME);
		executor.executeBlocking(future -> {
			assertValidMethod(method);
			QueryBuilder builder = metadata.getQueryBuilderInstance();
			builder.method(method)
			 	   .wrapIfAllowed();
			if(params != null && !params.isEmpty()) {
				builder.params(params);
			}
			builder.completeQueryWithFilter(filter)
				   .completeQueryWithModifier(modifier);
			List<ResourceObject> res = null;
			if(modifier != null) {
				res = repository.find(builder.method(), builder.params(), modifier.fields());
			} else {
				res = repository.find(builder.method(), builder.params());
			}
			future.complete(res);
		}, false , result -> {
			executor.close();
			if(result.succeeded()) {
				promise.resolve((List<ResourceObject>) result.result());
			} else {
				promise.reject(result.cause());
			}
		});
		return promise;		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Promise<List<ResourceObject>> find(ResourceMethod method, Params params) {
		Promise<List<ResourceObject>> promise = PromiseFactory.getPromiseInstance();
		WorkerExecutor executor = vertx.createSharedWorkerExecutor(WORKER_POOL_NAME);
		executor.executeBlocking(future -> {
			assertValidMethod(method);
			List<ResourceObject> res = 
					 repository.find(method, params);
			future.complete(res);
		},false, result -> {
			executor.close();
			if(result.succeeded()) {
				promise.resolve((List<ResourceObject>) result.result());
			} else {
				promise.reject(result.cause());
			}
		});
		return promise;
	}

	@Override
	public Promise<Long> count(QueryFilter filter) {
		ResourceMethod method = metadata.getQueryBuilderInstance()
				 .method(metadata.resource().getMethod("count"))
				 .generateCountIfNotFound()
				 .method();
		return count(method, filter);
	}

	@Override
	public Promise<Long> count(ResourceMethod method, QueryFilter filter) {
		Promise<Long> promise = PromiseFactory.getPromiseInstance();
		WorkerExecutor executor = vertx.createSharedWorkerExecutor(WORKER_POOL_NAME);
		executor.executeBlocking(future -> {
			assertValidMethod(method);
			QueryBuilder builder = metadata.getQueryBuilderInstance();
			builder.method(method)
			 	   .wrapIfAllowed()
				   .completeQueryWithFilter(filter);
			future.complete(repository.count(builder.method(), builder.params()));
		}, false, result -> {
			executor.close();
			if(result.succeeded()) {
				promise.resolve((Long) result.result());
			} else {
				promise.reject(result.cause());
			}
		});
		return promise;
	}

	@Override
	public Promise<T> insert(T object) {
		ResourceMethod method = metadata.getQueryBuilderInstance()
				 .method(metadata.resource().getMethodByUrl(
							RestflowHttpMethod.POST.value()))
				 .generateInsertIfNotFound()
				 .method();
		return insert(method, object);
	}

	@Override
	public Promise<T> insert(ResourceMethod method, T object) {
		return insert(method, object, null);
	}
	
	@Override
	public Promise<T> insert(ResourceMethod method, T object, Params params) {
		return insert(method, object, params, false);
	}
	
	@Override
	public Promise<T> insert(ResourceMethod method, T object, Params params, boolean ignoreValidation) {
		assertValidMethod(method);
		return save(method, new ObjectContextImpl<T>(
								 	metadata.resource(), this, object, params)
									.contextMethod(method)
									.lang(lang)
									.isNew(true), ignoreValidation);
	}

	@Override
	public Promise<T> update(T object) {
		ResourceMethod method = metadata.getQueryBuilderInstance()
				 .method(metadata.resource().getMethodByUrl(
							RestflowHttpMethod.PUT_WITH_ID.value()))
				 .generateUpdateIfNotFound()
				 .method();
		return update(method, object);
	}

	@Override
	public Promise<T> update(ResourceMethod method, T object) {
		return update(method, object, null);
	}
	
	@Override
	public Promise<T> update(ResourceMethod method, T object, Params params) {
		return update(method, object, params, false);
	}
	
	@Override
	public Promise<T> update(ResourceMethod method, T object, Params params, boolean ignoreValidation) {
		assertValidMethod(method);
		return save(method, new ObjectContextImpl<T>(
								 	metadata.resource(), this, object, params)
									.contextMethod(method)
									.lang(lang)
									.isNew(false), ignoreValidation);
	}
	
	@Override
	public Promise<Void> update(ResourceMethod method, Params params) {
		Promise<Void> promise = PromiseFactory.getPromiseInstance();
		WorkerExecutor executor = vertx.createSharedWorkerExecutor(WORKER_POOL_NAME);
		executor.executeBlocking(future -> {
			assertValidMethod(method);
			if(transaction != null) {
				repository.withTransaction(transaction);
			}
			repository.update(method, params);
			future.complete();
		}, result -> {
			executor.close();
			if(result.succeeded()) {
				promise.resolve();
			} else {
				promise.reject(result.cause());
			}
		});		
		return promise;
	}
	
	@Override
	public Promise<T> partialUpdate(ResourceMethod method, T object) {
		return partialUpdate(method, object, null);
	}
	
	@Override
	public Promise<T> partialUpdate(ResourceMethod method, T object, Params params) {
		return partialUpdate(method, object, params, false);
	}
	
	@Override
	public Promise<T> partialUpdate(ResourceMethod method, T object, Params params, boolean ignoreValidation) {
		assertValidMethod(method);
		return save(method, new ObjectContextImpl<T>(
			 	metadata.resource(), this, object, params)
				.contextMethod(method)
				.lang(lang)
				.isNew(false)
				.partial(true), ignoreValidation);
	}

	@Override
	public <I> Promise<Void> delete(I id) {
		Promise<Void> promise = PromiseFactory.getPromiseInstance();
		ResourceMethod method = metadata.getQueryBuilderInstance()
				 .method(metadata.resource().getMethodByUrl(
						 	RestflowHttpMethod.DELETE_WITH_ID.value()))
				 .generateDeleteIfNotFound()
				 .method();
		if(id == null) {
			throw new RestflowInvalidRequestException("Delete object by id with null id it's not allowed");
		}
		get(id).success(object -> {
			delete(method, object)
			.success(promise::resolve)
			.error(promise::reject);
		}).error(err -> {
			delete(method, new Params().addParam("id", id))
			.success(promise::resolve)
			.error(promise::reject);
		});
		return promise;
	}
	
	@Override
	public Promise<Void> delete(ResourceMethod method, Params params) {
		Promise<Void> promise = PromiseFactory.getPromiseInstance();
		WorkerExecutor executor = vertx.createSharedWorkerExecutor(WORKER_POOL_NAME);
		executor.executeBlocking(future -> {
			assertValidMethod(method);
			if(transaction != null) {
				repository.withTransaction(transaction);
			}
			repository.delete(method, params);
			future.complete();
		}, result -> {
			executor.close();
			if(result.succeeded()) {
				promise.resolve();
			} else {
				promise.reject(result.cause());
			}
		});		
		return promise;
	}

	@Override
	public Promise<Void> delete(ResourceMethod method, T object) {
		assertValidMethod(method);
		return destroy(method, new ObjectContextImpl<T>(
								 	metadata.resource(), this, object)
									.lang(lang));
	}

	@Override
	public Promise<Void> batchUpdate(ResourceMethod method, List<T> objects) {
		Promise<Void> promise = PromiseFactory.getPromiseInstance();
		WorkerExecutor executor = vertx.createSharedWorkerExecutor(WORKER_POOL_NAME);
		executor.executeBlocking(future -> {
			try {
				assertValidMethod(method);
				if(objects != null && !objects.isEmpty()) {
					repository.batchUpdate(method, objects);
				}
				future.complete();		
			} catch(Throwable e) {
				logger.error("Batch operation failed", e);
				future.fail(e);
			}
		}, result -> {
			executor.close();
			if(result.succeeded()) {
				promise.resolve();
			} else {
				promise.reject(result.cause());
			}
		});
		return promise;
	}
	
	@Override
	public ResourceService<T> withTransation(RepositoryTransaction<?> transaction) {
		this.transaction = transaction;
		return this;
	}
	
	@Override
	public RepositoryTransaction<?> transaction() {
		return transaction;
	}
	
	@Override
	public ResourceService<T> withLang(String lang) {
		this.lang = lang;
		return this;
	}
	
	@Override
	public String lang() {
		return lang;
	}

	@Override
	public ResourceService<T> authorization(AuthMetadata auth) {
		this.auth = auth;
		repository.withAuthorization(auth);
		return this;
	}
	
	@Override
	public AuthMetadata authorization() {
		return auth;
	}

	@Override
	public ResourceService<T> hooks(Map<HookType, HookInterceptor> hooks) {
		this.hooks = hooks;
		return this;
	}

	@Override
	public Map<HookType, HookInterceptor> hooks() {
		return hooks;
	}
	
	@Override
	public boolean inHookContext() {
		return hookContext;
	}

	@Override
	public ResourceService<T> inHookContext(boolean inContext) {
		hookContext = inContext;
		return this;
	}
	
	@Override
	public void close() {
		repository.close();
		if(logger.isDebugEnabled()) {
			logger.debug("Repository closed.");
		}
	}
	
	private Promise<Void> destroy(ResourceMethod method, ObjectContext<T> objContext) {
		Promise<Void> promise = PromiseFactory.getPromiseInstance();
		WorkerExecutor executor = vertx.createSharedWorkerExecutor(WORKER_POOL_NAME);
		executor.executeBlocking(future -> {
			T object = objContext.object();
			HookInterceptor hook = getHook(HookType.DESTROY);
			if(hook == null) {
				repository.delete(method, object);
				future.complete();
			} else {
				boolean selfTransaction = this.transaction == null;
				final RepositoryTransaction<?> transaction
						= hook.transactional() && repository.hasTransationSupport()
						? this.transaction != null ? this.transaction : repository.newTransaction() 
						: null;
				if(transaction != null) {
						repository.withTransaction(transaction);
				}
				hook.invoke(objContext)
				.allways(res -> {
					if(res.succeeded()) {
						if(!objContext.ignore()) {
							repository.delete(method, object);
						} 
						if(transaction != null && selfTransaction) {
							transaction.commit();
						}
						future.complete();
					} else {
						if(transaction != null && selfTransaction) {
							transaction.rollback();
						}
						future.fail(res.cause());
					}
				});
			}
		}, result -> {
			executor.close();
			if(result.succeeded()) {
				promise.resolve();
			} else {
				promise.reject(result.cause());
			}
		});
		return promise;
	}
	
	@SuppressWarnings("unchecked")
	private Promise<T> save(ResourceMethod method, ObjectContext<T> objContext, boolean ignoreValidation) {
		Promise<T> promise = PromiseFactory.getPromiseInstance();
		final boolean isNew = objContext.isNew();
		Params extParams = objContext.params();
		WorkerExecutor executor = vertx.createSharedWorkerExecutor(WORKER_POOL_NAME);
		executor.executeBlocking(future -> {
			final T object = objContext.object();
			if(!ignoreValidation) {
				ResourcePropertyValidator.validate(metadata.resource().getProperties(), 
						object, metadata.resourceClass(), objContext.partial());	
			}
			HookInterceptor hook = getHook(HookType.SAVE);
			boolean selfTransaction = this.transaction == null;
			resolveTransation(hook);
			if(hook == null) {
				T resObj = save(method, object, isNew, extParams);
				future.complete(resObj);
			} else {
				logger.info("Going to invoke hook on "+metadata.resource().getName());
				hook.invoke(objContext)
				.allways(res -> {
					T resObj = null;
					Throwable error = null;
					try {
						if(res.succeeded()) {
							if(!objContext.ignore()) {
								resObj = save(method, object, objContext.isNew(), extParams);
							} else {
								resObj = res.result();
							}
						} else {
							error = res.cause();
						}						
					} catch(Throwable e) {
						error = e;
					} finally {
						if(error == null) {
							if(transaction != null && selfTransaction) {
								transaction.commit();
							}
							future.complete(resObj);
						} else {
							if(transaction != null && selfTransaction) {
								transaction.rollback();
							}
							future.fail(error);
						}
					}
				});
			}
		}, result -> {
			executor.close();
			if(result.succeeded()) {
				promise.resolve((T) result.result());
			} else {
				promise.reject(result.cause());
			}
		});
		return promise;
	}

	private T save(ResourceMethod method, T object, boolean isNew, Params params) {
		if(isNew) {
			return repository.insert(method, object, params);	
		} else {
			return repository.update(method, object, params);
		}
	}
	
	private void resolveTransation(HookInterceptor hook) {
		if(transaction != null) {
			repository.withTransaction(transaction);
		} else if(hook != null &&
					hook.transactional() && 
						repository.hasTransationSupport()) {
			transaction = repository.newTransaction();
			repository.withTransaction(transaction);
		}
	}
	
	private void assertValidMethod(ResourceMethod method) {
		if(method == null) {
			throw new RestflowInvalidRequestException("Method cannot be null for request.");
		}
	}
		
	private HookInterceptor getHook(HookType hookType) {
		return hooks == null || hookContext ? null : hooks.get(hookType);
	}

}