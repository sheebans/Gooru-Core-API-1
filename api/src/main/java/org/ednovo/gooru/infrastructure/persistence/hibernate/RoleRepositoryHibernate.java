package org.ednovo.gooru.infrastructure.persistence.hibernate;

import java.util.List;

import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.hibernate.Query;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class RoleRepositoryHibernate extends BaseRepositoryHibernate implements RoleRepository, ConstantProperties, ParameterProperties {

	private final static String USER_ROLES = "select role_id from user_role_assoc where user_uid=:userUid ";

	private final static String ENTITY_AUTHORITY = "select concat(entity_name,'___', operation_name) as authority  from entity_operation eop  inner join role_entity_operation reop on eop.entity_operation_id = reop.entity_operation_id where role_id =:roleId";

	@Override
	@Cacheable("persistent")
	public List<Integer> getUserRoles(String userUid) {
		Query query = getSession().createSQLQuery(USER_ROLES);
		query.setParameter(USER_UID, userUid);
		return list(query);
	}


	@Override
	@Cacheable("persistent")
	public List<String> getEntityRoleAuthority(Integer roleId) {
		Query query = getSession().createSQLQuery(ENTITY_AUTHORITY);
		query.setParameter(ROLE_ID, roleId);
		return list(query);
	}

}
