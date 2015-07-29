package org.ednovo.gooru.infrastructure.persistence.hibernate.ltiService;

import org.ednovo.gooru.core.api.model.LtiService;
import org.ednovo.gooru.infrastructure.persistence.hibernate.BaseRepository;

public interface LtiServiceRepository extends BaseRepository {
	
	LtiService getLtiServiceByServiceUrlAndSourceId(String outcomeServiceUrl, String resultSourceId);
	
	LtiService getLtiServiceByServiceId(String serviceId);
}
