package org.ednovo.gooru.infrastructure.persistence.hibernate.ltiService;

import java.util.List;

import org.ednovo.gooru.core.api.model.LtiService;
import org.ednovo.gooru.infrastructure.persistence.hibernate.BaseRepositoryHibernate;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

@Repository
public class LtiServiceRepositoryHibernate extends BaseRepositoryHibernate implements LtiServiceRepository {

	@Override
	public LtiService getLtiServiceByServiceUrlAndSourceId(String outcomeServiceUrl, String resultSourceId) throws Exception {
		
		String hql = " FROM LtiService ltiService WHERE ltiService.serviceKey=:outcomeServiceUrl AND ltiService.resultSourceId=:resultSourceId";
		Query query = getSession().createQuery(hql);
		query.setParameter("outcomeServiceUrl", outcomeServiceUrl);
		query.setParameter("resultSourceId", resultSourceId);
		List<LtiService> results = list(query);
		if(results.size() > 0){
			return results.get(0);
		}
		return null;
	}

	@Override
	public LtiService getLtiServiceByServiceId(String serviceId) throws Exception {
		
		String hql = " FROM LtiService ltiService WHERE ltiService.ltiServiceId=:serviceId";
		Query query = getSession().createQuery(hql);
		query.setParameter("serviceId", serviceId);
		List<LtiService> results = list(query);
		if(results.size() > 0){
			return results.get(0);
		}
		return null;
	}

	

}
