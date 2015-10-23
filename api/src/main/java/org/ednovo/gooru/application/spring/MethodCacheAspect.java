package org.ednovo.gooru.application.spring;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.web.servlet.ModelAndView;

@Aspect
public class MethodCacheAspect {
	
	@Pointcut("execution(* org.ednovo.gooru.controllers.*.*RestController.*(..)) || " + "execution(* org.ednovo.gooru.controllers.*.*.*Rest*Controller.*(..))) ")
	public void cacheCheckPointcut() {
	}

	@AfterReturning(pointcut = "cacheCheckPointcut() && @annotation(redisCache)", returning="model")
	public void cache(JoinPoint jointPoint, RedisCache redisCache,  Object model) throws Throwable {
		// To-Do
		((ModelAndView)model).getModel();
	}
	
}
