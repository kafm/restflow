package com.platum.restflow.resource.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.bind.annotation.adapters.XmlAdapter;



public class DatasourcePropertiesAdapter extends XmlAdapter<DatasourcePropertiesAdapter.PropertyMap, Properties> {
	
	 static class PropertyMap {  
	    public List<DatasourceProperty> property = new ArrayList<>();
	 }

	 @Override
	 public PropertyMap marshal(Properties properties) throws Exception {
	        PropertyMap adaptedMap = new PropertyMap();
	        for(Entry<Object, Object> entry : properties.entrySet()) {
	            adaptedMap.property.add(new DatasourceProperty()
	            							.setName((String) entry.getKey())
	            							.setValue((String) entry.getValue()));
	        }
	        return adaptedMap;
	 }

	    @Override
	    public Properties unmarshal(PropertyMap adaptedMap) throws Exception {
	    	Properties properties = new Properties();
	        for(DatasourceProperty element : adaptedMap.property) {
	            properties.put(element.getName(), element.getValue());
	        }
	        return properties;
	    }

}
