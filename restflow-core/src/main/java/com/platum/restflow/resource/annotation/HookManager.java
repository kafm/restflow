package com.platum.restflow.resource.annotation;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.resource.ObjectContext;
import com.platum.restflow.utils.promise.Promise;
import com.platum.restflow.utils.promise.PromiseFactory;

public class HookManager {
	
	private Table<String, HookType, HookInterceptor> hooks;
	
	private String basePackage;

	@SuppressWarnings("unchecked")
	public static <T> Promise<T> executeHook(HookInterceptor hook, ObjectContext<T> context) {
		Promise<T> promise = PromiseFactory.getPromiseInstance();
		if(hook == null) {
			promise.resolve(context.object());
		} else {
			try {
				Object hookInstance = hook.getClass().newInstance();
				return (Promise<T>) hook.method().invoke(hookInstance, context);				
			} catch(Throwable e) {
				   
			}
		}
		return promise;
	}

	
	
	
	public static HookManager newInstance(String basePackage) {
		HookManager instance = newInstance();
		instance.basePackage = basePackage;
		return instance;
	}
	
	public static HookManager newInstance() {
		return new HookManager();
	}	

	public HookManager populate() {
		hooks = HashBasedTable.create();
		if(!StringUtils.isEmpty(basePackage)) {
			Reflections reflections = new Reflections((Object[]) basePackage.split(","), new MethodAnnotationsScanner());
			Set<Method> methods = reflections.getMethodsAnnotatedWith(ResourceHook.class);
			if(methods != null && !methods.isEmpty()) {
				methods.stream().forEach(method -> {
					if(!method.getReturnType().equals(Promise.class)) {
						throw new RestflowException("Hook method ["+method.toString()+"] should return a "+Promise.class.getName());
					}
					int numParam = method.getParameterCount();
					if(numParam == 0 || numParam > 1) {
						throw new RestflowException("Hook method ["+method.toString()+"] should have one parameter instead found "+numParam);
					}
					if(!method.getParameters()[0].getType().equals(ObjectContext.class)) {
						throw new RestflowException("Hook method ["+method.toString()+"] should have parameter type of " +
									ObjectContext.class+" instead of "+method.getParameters()[0].getName());
					}
					ResourceHook hookMetadata = method.getAnnotation(ResourceHook.class);
					String resource = hookMetadata.resource();
					HookType type = hookMetadata.on();
					boolean transactional = hookMetadata.transactional();		
					hooks.put(resource, type, new HookInterceptor(method, transactional));
				});
			}			
		}
		return this;
	}
	
	public boolean isEmpty() {
		return hooks.isEmpty();
	}
	
	public Map<HookType, HookInterceptor> getInterceptor(String resource) {
		assertHookPopulated();
		return hooks.row(resource);
	}
	
	public HookInterceptor getInterceptor(String resource, HookType type) {
		assertHookPopulated();
		return hooks.get(resource, type);
	}
	
	private void assertHookPopulated() {
		Validate.notNull(hooks, "Hooks it's not populated yet");
	}

}

