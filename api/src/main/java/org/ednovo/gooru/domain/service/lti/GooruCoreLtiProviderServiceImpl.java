package org.ednovo.gooru.domain.service.lti;

import java.util.Date;

import org.ednovo.gooru.core.api.model.ActionResponseDTO;
import org.ednovo.gooru.core.api.model.LtiService;
import org.ednovo.gooru.core.api.model.OAuthClient;
import org.ednovo.gooru.core.application.util.ServerValidationUtils;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.infrastructure.persistence.hibernate.auth.OAuthRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.ltiService.LtiServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

@Service
public class GooruCoreLtiProviderServiceImpl extends ServerValidationUtils implements GooruCoreLtiProviderService,ParameterProperties, ConstantProperties {

	@Autowired
	private OAuthRepository oAuthRepository;
	
	@Autowired
	private LtiServiceRepository ltiServiceRepository;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public ActionResponseDTO<LtiService> createLtiService(String oauthKey,LtiService ltiService){
		final Errors errors = validateLtiservice(ltiService);
		
		if(!errors.hasErrors()){
			OAuthClient oAuthClient = (OAuthClient) oAuthRepository.findOAuthClientByOAuthKey(oauthKey);
			rejectIfNull(oAuthClient, GL0056, OAUTH_CLIENT);
			ltiService.setCreatedAt(new Date(System.currentTimeMillis()));
			ltiService.setOAuthClient(oAuthClient);
			ltiService.setOauthContentId(oAuthClient.getContentId());
			ltiServiceRepository.save(ltiService);
		}
		return new ActionResponseDTO<LtiService>(ltiService, errors);
	}
	
	private Errors validateLtiservice(LtiService ltiService) {
		final Errors errors = new BindException(ltiService, LTI_SERVICE);
		if (ltiService != null) {
			rejectIfNullOrEmpty(errors, ltiService.getServiceKey(), SERVICE_KEY, GL0006, generateErrorMessage(GL0006, SERVICE_KEY));			
		}
		return errors;
	}

	@Override
	public LtiService getLtiServiceByOAuthKey(String oauthKey) {
		rejectIfNull(oauthKey, GL0006, LTI_OAUTH_KEY);
		OAuthClient oAuthClient = oAuthRepository.findOAuthClientByOAuthKey(oauthKey);
		rejectIfNull(oAuthClient, GL0056, OAUTH_CLIENT);
		LtiService ltiService = oAuthRepository.getLtiServiceByOAuthContentId(oAuthClient);		
		ltiService.setOAuthClient(oAuthClient);
		return ltiService;
	}

	@Override
	public LtiService getLtiServiceByServiceUrlAndSourceId(String outcomeServiceUrl, String resultSourceId) {
		
		return ltiServiceRepository.getLtiServiceByServiceUrlAndSourceId(outcomeServiceUrl, resultSourceId);
	}

	@Override
	public LtiService getLtiServiceByServiceId(String serviceId) {
		rejectIfNull(serviceId, GL0006, LTI_SERVICE_ID);
		LtiService ltiService = ltiServiceRepository.getLtiServiceByServiceId(serviceId);
		rejectIfNull(ltiService, GL0056, LTI_SERVICE_ID);
		
		rejectIfNull(serviceId, GL0006, LTI_OAUTH_KEY);
		OAuthClient oAuthClient = oAuthRepository.getOauthClientByContentId(ltiService.getOauthContentId());
		rejectIfNull(oAuthClient, GL0056, OAUTH_CLIENT);
		ltiService.setOAuthClient(oAuthClient);
		return ltiService;
	}
	
}
