package org.ednovo.gooru.application.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ClearCache {

	public String[] key() default "";
	
	public String id();
	
	public boolean deleteSessionUserCache() default true;
}
