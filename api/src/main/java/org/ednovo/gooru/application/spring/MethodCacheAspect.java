package org.ednovo.gooru.application.spring;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.ednovo.gooru.application.util.SerializerUtil;
import org.ednovo.gooru.core.api.model.Content;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.Constants;
import org.ednovo.gooru.domain.service.redis.RedisService;
import org.ednovo.gooru.infrastructure.persistence.hibernate.CollectionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import scala.reflect.generic.Constants.Constant;

@Aspect
public class MethodCacheAspect extends SerializerUtil implements ConstantProperties{

	@Autowired
	private RedisService redisService;

	@Pointcut("execution(* org.ednovo.gooru.controllers.*.*RestController.*(..)) || " + "execution(* org.ednovo.gooru.controllers.*.*.*Rest*Controller.*(..))) ")
	public void cacheCheckPointcut() {
	}

	@AfterReturning(pointcut = "cacheCheckPointcut() && @annotation(redisCache)", returning="model")
	public void cache(JoinPoint jointPoint, RedisCache redisCache,  Object model) throws Throwable {
		HttpServletRequest request = getRequest();
		String redisKey = generateKey(redisCache.key(), request, null);
		if(getRedisService().getValue(redisKey) == null){
			Map<String, Object> data = ((ModelAndView) model).getModel();
			Object json = data.get(MODEL);
			if(redisCache.ttl() != 0 && !json.toString().isEmpty()){
				getRedisService().putValue(redisKey, (String)json, redisCache.ttl());
			}
			else if(!data.get(MODEL).toString().isEmpty()){
				getRedisService().putValue(redisKey, (String)json);
			}
		}
	}
	
	@Around(value = "cacheCheckPointcut() && @annotation(redisCache)")
	public Object cache(ProceedingJoinPoint pjp, RedisCache redisCache) throws Throwable{
		HttpServletRequest request = getRequest();
		if(request.getParameter(CLEAR_CACHE)!=null && request.getParameter(CLEAR_CACHE).equalsIgnoreCase(FALSE)){
			String redisKey = generateKey(redisCache.key(), request, null);
			String data =getRedisService().getValue(redisKey);
			if(data != null){
				return toModelAndView(data);
			} 
		}
		return pjp.proceed();
	}
	
	@AfterReturning(pointcut = "@annotation(clearCache)")
	public void clearCache(JoinPoint joinntPoint, ClearCache clearCache){
		HttpServletRequest request = getRequest();
		String redisKey = generateKey(clearCache.key(), request, null)+"*";
		getRedisService().bulkKeyDelete(redisKey);
	}
	
	
	private HttpServletRequest getRequest(){
		HttpServletRequest request = null;
		if (RequestContextHolder.getRequestAttributes() != null) {
			request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		}
		return request;
	}
	
	private String generateKey(String prefixKey, HttpServletRequest request, String id){
		StringBuilder redisKey = new StringBuilder(prefixKey);
		User user = (User) request.getAttribute(Constants.USER);
		redisKey.append(HYPHEN).append((id != null)? id:user.getGooruUId());
		//to get the path variable
		Map<?, ?> pathVariables = (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		Iterator<?> entries = pathVariables.entrySet().iterator();
		while (entries.hasNext()) {
		    Map.Entry entry = (Map.Entry) entries.next();
		    redisKey.append(HYPHEN).append(entry.getValue());
		}
		//get param value
		Map<String, String> parameters  = request.getParameterMap();
		for (String key : parameters.keySet()) {
		    if(!key.equalsIgnoreCase(SESSION_TOKEN) && !key.equalsIgnoreCase(CLEAR_CACHE)){
		    	redisKey.append(HYPHEN).append(request.getParameter(key));
		    }
		}
		return redisKey.toString();
	}

	public RedisService getRedisService() {
		return redisService;
	}
	
}
