package com.platum.restflow.components;

import java.util.ArrayList;
import java.util.List;

import com.platum.restflow.FactoryLoader;
import com.platum.restflow.utils.ClassUtils;

public class ComponentFactory {

	private static List<Filter> filters;
	
	private static List<Controller> controllers;
	
	public static List<Controller> getControllerServices() {
		if(controllers == null) {
			controllers = getComponent(Controller.class);
		}
		return controllers;						
	}
	
	public static List<Filter> getFilterServices() {
		if(filters == null) {
			filters = getComponent(Filter.class);
		}
		return filters;					
	}

	public static <T> List<T> getComponent(Class<T> clazz) {
		List<String> impls = FactoryLoader.getImpls(clazz.getName());
		if(impls != null) {
			List<T> components = new ArrayList<>();
			impls.stream().forEach(className -> {
				T component = ClassUtils.newInstance(ClassUtils.getClassByName(className));
				components.add(component);
			});
			return components;
		}
		return null;
	}
}
