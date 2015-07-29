package org.ednovo.gooru.infrastructure.persistence.hibernate.ltiService;

import java.util.List;

import org.ednovo.gooru.core.api.model.LtiService;
import org.ednovo.gooru.infrastructure.persistence.hibernate.BaseRepositoryHibernate;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

@Repository
public class LtiServiceRepositoryHibernate extends BaseRepositoryHibernate implements LtiServiceRepository {
	
	private static final String LTISERVICE_BY_SERVICE_URL_SOURCEID = " FROM LtiService ltiService WHERE ltiService.serviceKey=:outcomeServiceUrl AND ltiService.resultSourceId=:resultSourceId";
	
	private static final String LTISERVICE_BY_SERVICE_ID = "FROM LtiService ltiService WHERE ltiService.ltiServiceId=:serviceId";

	@Override
	public LtiService getLtiServiceByServiceUrlAndSourceId(String outcomeServiceUrl, String resultSourceId) {
		
		Query query = getSession().createQuery(LTISERVICE_BY_SERVICE_URL_SOURCEID);
		query.setParameter("outcomeServiceUrl", outcomeServiceUrl);
		query.setParameter("resultSourceId", resultSourceId);
		List<LtiService> results = list(query);
		return results.size() > 0 ? results.get(0) : null;
	}

	@Override
	public LtiService getLtiServiceByServiceId(String serviceId) {
		
		Query query = getSession().createQuery(LTISERVICE_BY_SERVICE_ID);
		query.setParameter("serviceId", serviceId);
		List<LtiService> results = list(query);
		return results.size() > 0 ? results.get(0) : null;
	}

	

}
