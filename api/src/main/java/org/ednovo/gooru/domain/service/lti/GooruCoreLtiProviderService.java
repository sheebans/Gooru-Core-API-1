package org.ednovo.gooru.domain.service.lti;

import org.ednovo.gooru.core.api.model.ActionResponseDTO;
import org.ednovo.gooru.core.api.model.LtiService;

public interface GooruCoreLtiProviderService {

	ActionResponseDTO<LtiService> createLtiService(String oauthKey,LtiService ltiService);
	
	LtiService getLtiServiceByOAuthKey(String oauthKey) throws Exception;
	
	LtiService getLtiServiceByServiceUrlAndSourceId(String outcomeServiceUrl, String resultSourceId) throws Exception;
	
	LtiService getLtiServiceByServiceId(String serviceId) throws Exception;
}
