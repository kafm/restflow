package com.platum.restflow.utils;

import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.lang3.StringUtils;
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

	public static URL getClassLocation(Class<?> c) {
		URL url = c.getResource(org.apache.commons.lang3.ClassUtils.getShortClassName(c) + ".class");
		if(url == null) return null;
		String s = url.toExternalForm();
		String end = "/" + c.getName().replaceAll("\\.", "/") + ".class";
		if(s.endsWith(end)) {
			s = s.substring(0, s.length() - end.length());
		}
		else
		{
			end = end.replaceAll("/", "\\");
			if(s.endsWith(end)){
				s = s.substring(0, s.length() - end.length());
			} else {
            return null;
			}
		}
		if(StringUtils.startsWith(s, "jar:") && s.endsWith("!")) {
			s = s.substring(4, s.length() - 1);
		}
	    try {
	        return new URL(s);
	    }
	    catch (MalformedURLException e) {
	        return null;
	    }
	}
}
