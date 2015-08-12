package org.ednovo.gooru.infrastructure.persistence.hibernate;

import java.util.List;

public interface RoleRepository extends BaseRepository {
	List<Integer> getUserRoles(String userUid);

	List<String> getEntityRoleAuthority(Integer roleId);
}
