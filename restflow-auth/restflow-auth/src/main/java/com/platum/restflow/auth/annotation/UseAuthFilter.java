package com.platum.restflow.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
public @interface UseAuthFilter {
	
	boolean  primary() default false;
	
}
