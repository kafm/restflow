package com.platum.restflow.resource.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.platum.restflow.resource.property.ResourcePropertyType;

public class PropertyTypeEnumAdapter extends XmlAdapter<String, ResourcePropertyType> {

    @Override
    public ResourcePropertyType unmarshal(String typeString) throws Exception {
        return ResourcePropertyType.fromValue(typeString);
    }

    @Override
    public String marshal(ResourcePropertyType type) throws Exception {
        return type.name();
    }
}
