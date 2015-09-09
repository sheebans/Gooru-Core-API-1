package org.ednovo.gooru.application.util;

import java.util.HashMap;
import java.util.Map;

import org.ednovo.gooru.core.api.model.Identity;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.api.model.UserCredential;
import org.ednovo.gooru.core.constant.Constants;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.domain.service.redis.RedisService;
import org.ednovo.gooru.domain.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import flexjson.JSONSerializer;

@Component
public class AccountUtil implements ParameterProperties {

	@Autowired
	private UserService userService;

	@Autowired
	private RedisService redisService;

	private static final String SESSION_TOKEN_KEY = "authenticate_";

	private static final String USER_ROLE_SET = "userRoleSetString";

	private static final String PARTY_PERMISSIONS = "partyPermissions";

	public UserCredential storeAccountLoginDetailsInRedis(String sessionToken, User user) {
		UserCredential userCredential = getUserService().getUserCredential(user, sessionToken, null, null);
		String key = SESSION_TOKEN_KEY + sessionToken;
		String data = getRedisService().get(key);
		if (sessionToken != null && data == null) {
			getRedisService().put(key, new JSONSerializer().serialize(initializeModel(userCredential, user)), Constants.AUTHENTICATION_CACHE_EXPIRY_TIME_IN_SEC);
		}
		return userCredential;
	}

	private Map<String, Object> initializeModel(UserCredential userCredential, User user) {
		Map<String, Object> userCredentials = new HashMap<String, Object>();
		userCredentials.put(USER_ROLE_SET, user.getUserRoleSetString());
		userCredentials.put(USER_UID, user.getPartyUid());
		userCredentials.put(USER_NAME, user.getUsername());
		userCredentials.put(FIRST_NAME, user.getFirstName());
		userCredentials.put(LAST_NAME, user.getLastName());
		if (user.getIdentities() != null) {
			Identity identity = user.getIdentities().iterator().next();
			if (identity != null) {
				userCredentials.put(EXTERNAL_ID, identity.getExternalId());
			}
		}
		userCredentials.put(PARTY_PERMISSIONS, userCredential.getPartyPermissions());
		return userCredentials;
	}

	public UserService getUserService() {
		return userService;
	}

	public RedisService getRedisService() {
		return redisService;
	}
}
