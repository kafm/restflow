package com.platum.restflow;

import org.apache.commons.lang3.Validate;

public enum RestflowHttpMethod {
    GET("GET"),
    GET_WITH_ID("GET /:"+RestflowDefaultConfig.DEFAULT_ID_PARAM),
    DOWNLOAD("GET /:"+RestflowDefaultConfig.DEFAULT_ID_PARAM+"/download"),
    UPLOAD("POST /:"+RestflowDefaultConfig.DEFAULT_ID_PARAM+"/upload"),
    POST("POST"),
    PUT("PUT"),
    PUT_WITH_ID("PUT /:"+RestflowDefaultConfig.DEFAULT_ID_PARAM),
    PATCH("PATCH"),
    PATCH_WITH_ID("PATCH /:"+RestflowDefaultConfig.DEFAULT_ID_PARAM),
    DELETE("DELETE"),
    DELETE_WITH_ID("DELETE /:"+RestflowDefaultConfig.DEFAULT_ID_PARAM),
    ;
	
    private String value;

    RestflowHttpMethod(String value){
        this.value = value;
    }
    
    public String value() {
    	return value;
    }
    
    public boolean equalValue(String passedValue){
        return passedValue != null && this.value.equalsIgnoreCase(passedValue);
    }
    
    public boolean equalValue(RestflowHttpMethod method) {
    	return value.equals(method.toString());
    }
    
    public boolean urlIsHttpMethod(String url) {
    	Validate.notEmpty(url, "Url cannot be empty");
    	return url.toUpperCase().startsWith(value.toUpperCase());
    }
        
    public String parseUrl(String url) {
    	Validate.notEmpty(url, "Url cannot be empty");
    	int index = url.indexOf(" ");
    	return index >= 0 ? url.substring(index+1, url.length()) : "";
    }
    
    public boolean isPost() {
    	return value.startsWith(POST.value);
    }
    
    public boolean isPut() {
    	return value.startsWith(PUT.value);
    }
    
    public boolean isPatch() {
    	return value.startsWith(PATCH.value);
    }
    
    public boolean isDelete() {
    	return value.startsWith(DELETE.value);
    }
    
    public boolean isGet() {
    	return value.startsWith(GET.value);
    }
}
