package com.platum.restflow.utils;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.lang3.Validate;

public class ClassUtils {

	public static ClassLoader getDefaultClassLoader() {
		ClassLoader classLoader = getThreadClassLoader();
		if(classLoader == null) {
			classLoader = ResourceUtils.class.getClassLoader();
			if(classLoader == null) {
				classLoader = ClassLoader.getSystemClassLoader();
			}
		}
		return classLoader;
	}
	
	public static ClassLoader getThreadClassLoader() {
		try {
			return Thread.currentThread().getContextClassLoader();
		}
		catch (Throwable ex) {
			return null;
		}
	}
	
	public static ClassLoader getClassLoaderByUrl(URL... url) {
		return new URLClassLoader(url);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getClassByName(String className) {
		Validate.notEmpty(className, "Class name cannot be null.");
		try {
			return (Class<T>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Invalid class ["+className+"] provided.");
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Class<T> castClass(Class<?> clazz) {
		return (Class<T>) clazz;
	}
		
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(Class<?> clazz, Object... args) {
		try {
			if(args == null || args.length == 0) {
				return (T) clazz.newInstance();
			}
			Class<?>[] params = new Class<?>[args.length];
			for(int i = 0; i < args.length; i++) {
				Object arg = args[i];
				params[i] = arg == null ? null : arg.getClass();
			}
			Constructor<?> ctor = clazz.getConstructor(params);
			return (T) ctor.newInstance(args);
		} catch(Throwable e) {
			throw new RuntimeException("Unable to create instance of ["+clazz.getName()+"]", e);
		}

	}
	
}
