package org.ednovo.gooru.application.util;

import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.api.model.UserToken;
import org.ednovo.gooru.core.constant.Constants;
import org.ednovo.gooru.core.security.AuthenticationDo;
import org.ednovo.gooru.domain.service.redis.RedisService;
import org.ednovo.gooru.domain.service.user.UserService;
import org.ednovo.goorucore.application.serializer.ExcludeNullTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import flexjson.JSONSerializer;

@Component
public class AccountUtil {

	@Autowired
	private UserService userService;

	@Autowired
	private RedisService redisService;

	private static final String SESSION_TOKEN_KEY = "authenticate1_";

	private static final String[] INCLUDE_USER_DETAILS = { "*.operationAuthorities", "*.userRoleSet", "*.partyOperations", "*.subOrganizationUids", "*.orgPermits", "*.partyPermits", "*.identities", "*.partyPermissions.*" };

	private static final String[] EXCLUDE_USER_DETAILS = { "*.class", "*.idp", "*.school", "*.customFields", "*.contentType", "*.schoolDistrict", "*.status", "*.meta", "*.resourceInfo" };

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountUtil.class);

	public AuthenticationDo storeAccountLoginDetailsInRedis(String key, UserToken userToken, User user) {
		final AuthenticationDo authentication = new AuthenticationDo();
		try {
			if (userToken != null) {
				authentication.setUserToken(userToken);
				authentication.setUserCredential(getUserService().getUserCredential(user, userToken.getToken(), null, null));
				getRedisService().put(SESSION_TOKEN_KEY + key, new JSONSerializer().transform(new ExcludeNullTransformer(), void.class).include(INCLUDE_USER_DETAILS).exclude(EXCLUDE_USER_DETAILS).serialize(authentication), Constants.AUTHENTICATION_CACHE_EXPIRY_TIME_IN_SEC);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to  put  value in redis server", e);
		}
		return authentication;
	}

	public UserService getUserService() {
		return userService;
	}

	public RedisService getRedisService() {
		return redisService;
	}
}
