package org.ednovo.gooru.application.spring;

import java.util.Arrays;
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
public class MethodCacheAspect extends SerializerUtil implements ConstantProperties {

	@Autowired
	private RedisService redisService;

	final String TILD = "~";

	@Pointcut("execution(* org.ednovo.gooru.controllers.*.*RestController.*(..)) || " + "execution(* org.ednovo.gooru.controllers.*.*.*Rest*Controller.*(..))) ")
	public void cacheCheckPointcut() {
	}

	@AfterReturning(pointcut = "cacheCheckPointcut() && @annotation(redisCache)", returning = "model")
	public void cache(JoinPoint jointPoint, RedisCache redisCache, Object model) throws Throwable {
		HttpServletRequest request = getRequest();
		String redisKey = generateKey(redisCache.key(), request, null);
		if (getRedisService().getValue(redisKey) == null) {
			Map<String, Object> data = ((ModelAndView) model).getModel();
			if (redisCache.ttl() != 0 ) {
				getRedisService().putValue(redisKey, (String) data.get(MODEL), redisCache.ttl());
			} else if (!data.get(MODEL).toString().isEmpty()) {
				getRedisService().putValue(redisKey, (String) data.get(MODEL));
			}
		}
	}

	@Around(value = "cacheCheckPointcut() && @annotation(redisCache)")
	public Object cache(ProceedingJoinPoint pjp, RedisCache redisCache) throws Throwable {
		HttpServletRequest request = getRequest();
		if (request.getParameter(CLEAR_CACHE) != null && request.getParameter(CLEAR_CACHE).equalsIgnoreCase(FALSE)) {
			String redisKey = generateKey(redisCache.key(), request, null);
			String data = getRedisService().getValue(redisKey);
			if (data != null) {
				return toModelAndView(data);
			}
		}
		return pjp.proceed();
	}

	@AfterReturning(pointcut = "@annotation(clearCache)")
	public void clearCache(JoinPoint joinntPoint, ClearCache clearCache) {
		HttpServletRequest request = getRequest();
		String prefixKey = getPrefixKey(clearCache.key());
		StringBuilder redisKey = new StringBuilder(prefixKey);
		if (clearCache.id() != null) {
			Map<?, ?> pathVariables = (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
			redisKey.append("*").append(pathVariables.get(clearCache.id())).append("*");
		}
		getRedisService().bulkDelete(redisKey.toString());
		if (clearCache.deleteSessionUserCache()) {
			User user = (User) request.getAttribute(Constants.USER);
			redisKey.setLength(0);
			redisKey.append("*").append(clearCache.key()).append("*").append(user.getGooruUId());
			getRedisService().bulkKeyDelete(redisKey.toString());
		}

	}

	private HttpServletRequest getRequest() {
		HttpServletRequest request = null;
		if (RequestContextHolder.getRequestAttributes() != null) {
			request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		}
		return request;
	}

	private StringBuilder getPathVariable(HttpServletRequest request) {
		StringBuilder redisKey = new StringBuilder();
		Map<?, ?> pathVariables = (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		Iterator<?> entries = pathVariables.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			redisKey.append(TILD).append(entry.getValue());
		}
		return redisKey;
	}

	private String getPrefixKey(String[] prefixKeys){
		StringBuilder prefixKey= new StringBuilder();
		for(String key: prefixKeys){
			if(prefixKey != null && !prefixKey.toString().equals("")){
				prefixKey.append(HYPHEN);
			}
			prefixKey.append(key);
		}
		return prefixKey.toString();
	}
	
	private String generateKey(String[] prefixKeys, HttpServletRequest request, String id) {
		StringBuilder redisKey = new StringBuilder(getPrefixKey(prefixKeys));
		User user = (User) request.getAttribute(Constants.USER);
		redisKey.append(TILD).append((id != null) ? id : user.getGooruUId());
		// to get the path variable
		StringBuilder pathVariable = getPathVariable(request);
		if (pathVariable != null) {
			redisKey.append(pathVariable);
		}
		// get param value
		Map<String, String> parameters = request.getParameterMap();
		for (String key : parameters.keySet()) {
			if (!key.equalsIgnoreCase(SESSION_TOKEN) && !key.equalsIgnoreCase(CLEAR_CACHE)) {
				redisKey.append(TILD).append(request.getParameter(key));
			}
		}
		return redisKey.toString();
	}

	public RedisService getRedisService() {
		return redisService;
	}

}
