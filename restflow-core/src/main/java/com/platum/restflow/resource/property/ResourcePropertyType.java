package com.platum.restflow.resource.property;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum ResourcePropertyType {
	INTEGER("integer"),
	LONG("long"),
    DECIMAL("decimal"),
    STRING("string"),
    BOOLEAN("boolean"),
    DATE("date"),
    EMAIL("email"),
    IPV4("ipv4"),
    IPV6("ipv6"),
    URL("url"),
    CREDITCARD("creditcard")
    ;
	
    private String key;
 
    private ResourcePropertyType(final String key) {
        this.key = key;
    }
 
    public static ResourcePropertyType fromValue(String key) {
        return fromString(key);
    }

    
    public static ResourcePropertyType fromString(String key) {
        return key == null
                ? null
                : ResourcePropertyType.valueOf(key.toUpperCase());
    }

    public String getKey() {
        return key;
    }
}
