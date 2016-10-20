package com.platum.restflow.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.junit.Assert;

public class ResourceUtils {

	public static final String CLASSPATH_PREFIX = "classpath:";
	
	public static final String FILE_PREFIX = "file:";
	
	public static final String URL_PREFIX = "url:";
	
	private static final String envVarRegex = "\\$\\{(.+?)\\}";
		
	public static URL getURL(String path) {
		Assert.assertNotNull(path, "Resource path must not be null");
		URL url = null;
		if(path.startsWith(CLASSPATH_PREFIX)) {
			url = getUrlFromClassPath(removePrefix(path));
		} else if(path.startsWith(URL_PREFIX)) {
			url = getUrlFromUrlPath(removePrefix(path));
		} else if(path.startsWith(FILE_PREFIX)) {
			url = getUrlFromFilePath(removePrefix(path));
		} else {
			url = getUrlFromFilePath(path);
		}
		return url;
	}
	
	public static InputStream getInputStream(String path) throws IOException  { 
		if(StringUtils.isEmpty(path)) {
			return null;
		}
		URL url = getURL(path);
		if(url != null) {
			return url.openStream();
		}
        return null;
    }
	
	public static List<URL> getUrlsFromClassPath(String path) {
		List<URL> urls = new ArrayList<>();
		try {
			Enumeration<URL> e = ClassLoader.getSystemResources(path);
			while(e.hasMoreElements()) {
				urls.add(e.nextElement());
			}
		} catch (IOException e) {
		}
		return urls;
	}
	
	private static URL getUrlFromClassPath(String path) {
		return getUrlFromClassPath(ClassUtils.getDefaultClassLoader(), path, true);
	}
		
	private static URL getUrlFromClassPath(ClassLoader loader, String path, boolean recursive) {
		URL url = loader.getResource(path);
		if(url == null && recursive) {
			ClassLoader parent = loader.getParent();
			if(parent != null) {
				return getUrlFromClassPath(parent, path, recursive);
			}
		}
		return url;
	}
		
	private static URL getUrlFromUrlPath(String path) {
		try {
			return new URL(path);
		} catch (MalformedURLException e) {
			return null;
		}		
	}
	
	private static URL getUrlFromFilePath(String path) {
		String resourcePath = resolveFilePath(path);
		try {
			return new URL(resourcePath);
		} catch (MalformedURLException e) {
			try {
				File file = new File(resourcePath);
				if(file.exists()) {
					return file.toURI().toURL();	
				}
			}
			catch (MalformedURLException ex2) {}
		}	
		return null;
	}
	
    private static String removePrefix(String path) {
        return path.substring(path.indexOf(":") + 1);
    }
    
    private static String resolveFilePath(String path) {
    	Validate.notEmpty(path);
    	final Pattern envVarPatter = Pattern.compile(envVarRegex);
		final Matcher matcher = envVarPatter.matcher(path);
		if(!matcher.find()) {
			return path;
		}
		String envVal = System.getenv(matcher.group(1));
		if(StringUtils.isEmpty(envVal)) {
			return path;
		}
		return path.replaceAll(envVarRegex, envVal);
    }


}
