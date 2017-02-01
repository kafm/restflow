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

public class ResourceServiceImpl<T> extends AbstractResourceComponent<T> implements ResourceService<T> {

	private final  Logger logger = LoggerFactory.getLogger(getClass());
	
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
		vertx.executeBlocking(future -> {
			if(id == null) {
				throw new RestflowInvalidRequestException("Get object with null id it's not allowed");
			}
			ResourceMethod method = metadata.getQueryBuilderInstance()
					 .method(metadata.resource().getMethodByUrl(
							 	RestflowHttpMethod.GET_WITH_ID.value()))
					 .generateGetByIdIfNotFound()
					 .method();
			future.complete(method);
		}, res -> {
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
		vertx.executeBlocking(future -> {
			assertValidMethod(method);
			T object  = repository.get(method, params);
			HookInterceptor hook = getHook(HookType.GET, object);
			if(hook == null) {
				future.complete(object);
			} else {
				hook.invoke(new ObjectContextImpl<T>(
						metadata.resource(), this, object)
						.lang(lang))
				.success(prunnedObj -> future.complete(prunnedObj))
				.error(error -> future.fail(error));
			}
		}, result -> {
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
		vertx.executeBlocking(future -> {
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
		}, result -> {
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
		vertx.executeBlocking(future -> {
			assertValidMethod(method);
			List<ResourceObject> res = 
					 repository.find(method, params);
			future.complete(res);
		}, result -> {
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
		vertx.executeBlocking(future -> {
			assertValidMethod(method);
			QueryBuilder builder = metadata.getQueryBuilderInstance();
			builder.method(method)
			 	   .wrapIfAllowed()
				   .completeQueryWithFilter(filter);
			future.complete(repository.count(builder.method(), builder.params()));
		}, result -> {
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
		assertValidMethod(method);
		return save(method, new ObjectContextImpl<T>(
								 	metadata.resource(), this, object, params)
									.contextMethod(method)
									.lang(lang)
									.isNew(true));
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
		assertValidMethod(method);
		return save(method, new ObjectContextImpl<T>(
								 	metadata.resource(), this, object, params)
									.contextMethod(method)
									.lang(lang)
									.isNew(false));
	}
	
	@Override
	public Promise<T> partialUpdate(ResourceMethod method, T object) {
		return partialUpdate(method, object, null);
	}
	
	@Override
	public Promise<T> partialUpdate(ResourceMethod method, T object, Params params) {
		assertValidMethod(method);
		return save(method, new ObjectContextImpl<T>(
			 	metadata.resource(), this, object, params)
				.contextMethod(method)
				.lang(lang)
				.isNew(false)
				.partial(true), true);
	}

	@Override
	public <I> Promise<Void> delete(I id) {
		Promise<Void> promise = PromiseFactory.getPromiseInstance();
		ResourceMethod method = metadata.getQueryBuilderInstance()
				 .method(metadata.resource().getMethodByUrl(
						 	RestflowHttpMethod.DELETE_WITH_ID.value()))
				 .generateDeleteIfNotFound()
				 .method();
		get(id).success(object -> {
			delete(method, object)
			.success(v -> {promise.resolve();})
			.error(error -> {promise.reject(error);});
		}).error(error -> {promise.reject(error);});
		return promise;
	}
	
	@Override
	public Promise<Void> delete(ResourceMethod method, Params params) {
		Promise<Void> promise = PromiseFactory.getPromiseInstance();
		assertValidMethod(method);
		vertx.executeBlocking(future -> {
			if(transaction != null) {
				repository.withTransaction(transaction);
			}
			repository.delete(method, params);
			future.complete();
		}, result -> {
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
		vertx.executeBlocking(future -> {
			assertValidMethod(method);
			if(objects != null && !objects.isEmpty()) {
				repository.batchUpdate(method, objects);
			}
			future.complete();
		}, result -> {
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
	public void close() {
		repository.close();
		if(logger.isDebugEnabled()) {
			logger.debug("Repository closed.");
		}
	}
	
	private Promise<Void> destroy(ResourceMethod method, ObjectContext<T> objContext) {
		Promise<Void> promise = PromiseFactory.getPromiseInstance();
		vertx.executeBlocking(future -> {
			T object = objContext.object();
			HookInterceptor hook = getHook(HookType.DESTROY, object);
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
				hookContext = true;
				hook.invoke(objContext)
				.allways(res -> {
					hookContext = false;
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
			if(result.succeeded()) {
				promise.resolve();
			} else {
				promise.reject(result.cause());
			}
		});
		return promise;
	}

	private Promise<T> save(ResourceMethod method, ObjectContext<T> objContext) {
		return save(method, objContext, false);
	}
	
	@SuppressWarnings("unchecked")
	private Promise<T> save(ResourceMethod method, ObjectContext<T> objContext, boolean partial) {
		Promise<T> promise = PromiseFactory.getPromiseInstance();
		final boolean isNew = objContext.isNew();
		Params extParams = objContext.params();
		vertx.executeBlocking(future -> {
			final T object = objContext.object();
			ResourcePropertyValidator.validate(metadata.resource().getProperties(), 
					object, metadata.resourceClass(), partial);
			HookInterceptor hook = getHook(HookType.SAVE, object);
			if(hook == null) {
				T resObj = save(method, object, isNew, extParams);
				future.complete(resObj);
			} else {
				boolean selfTransaction = this.transaction == null;
				final RepositoryTransaction<?> transaction
						= hook.transactional() && repository.hasTransationSupport()
						? this.transaction != null? this.transaction : repository.newTransaction() 
						: null;
				if(transaction != null) {
					repository.withTransaction(transaction);
				}
				hookContext = true;
				hook.invoke(objContext)
				.allways(res -> {
					T resObj = null;
					Throwable error = null;
					hookContext = false;
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
	
	private void assertValidMethod(ResourceMethod method) {
		if(method == null) {
			throw new RestflowInvalidRequestException("Method cannot be null for request.");
		}
	}
	
	private HookInterceptor getHook(HookType hookType, T object) {
		return hooks == null || hookContext ? null : hooks.get(hookType);
	}

}
