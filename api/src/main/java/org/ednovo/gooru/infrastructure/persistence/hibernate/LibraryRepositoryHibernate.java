package org.ednovo.gooru.infrastructure.persistence.hibernate;

import java.util.List;
import java.util.Map;

import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

@Repository
public class LibraryRepositoryHibernate extends BaseRepositoryHibernate implements LibraryRepository, ConstantProperties, ParameterProperties {

	private static final String GET_COLLECTIONS = "select cc.title, ci.collection_item_id as collectionItemId, cr.gooru_oid as gooruOid, co.gooru_oid as parentGooruOid, cc.image_path as imagePath, u.username, u.gooru_uid as gooruUId, u.firstname, u.lastname  from collection c inner join collection_item ci on ci.collection_content_id = c.content_id inner join collection cc on cc.content_id = ci.resource_content_id inner join content cr on cr.content_id = cc.content_id inner join content co on co.content_id = c.content_id inner join user u on u.gooru_uid = co.user_uid ";

	private static final String GET_USER_UID = "select gooru_uid  from user where username=:username";

	private static final String GET_COLLECTION_ITEMS = "select cc.title, ci.collection_item_id as collectionItemId, cr.gooru_oid as gooruOid, co.gooru_oid as parentGooruOid, cc.image_path as imagePath, cc.collection_type as collectionType  from collection c inner join collection_item ci on ci.collection_content_id = c.content_id inner join collection cc on cc.content_id = ci.resource_content_id inner join content cr on cr.content_id = cc.content_id inner join content co on co.content_id = c.content_id ";

	private static final String GET_COLLECTION_RESOURCE_ITEMS = "select cc.title, cc.type_name as resourceType, cc.folder, cc.thumbnail, ct.value, ct.display_name as displayName  from collection c inner join collection_item ci on ci.collection_content_id = c.content_id inner join resource cc on cc.content_id = ci.resource_content_id inner join content cr on cr.content_id = cc.content_id inner join content co on co.content_id = c.content_id left join custom_table_value ct on ct.custom_table_value_id = resource_format_id where cr.gooru_oid =:gooruOid order by  ci.item_sequence";

	private static final String GET_LIBRARIES = "select library_user_uid as libraryId, name as displayName, username as name, type_id as typeId, image_path as imagePath  from library l inner join user u on u.gooru_uid = l.library_user_uid where category_id =:categoryId  order by sequence";

	private static final String CATEGORY_ID = "categoryId";

	@Override
	public List<Map<String, Object>> getCollections(String userId, String collectionType, int limit, int offset) {
		StringBuilder sql = new StringBuilder(GET_COLLECTIONS);
		sql.append("where co.user_uid =:userUid and cc.collection_type =:collectionType and cc.distinguish = 1 order by  ci.item_sequence");
		Query query = getSession().createSQLQuery(sql.toString());
		query.setParameter(USER_UID, userId);
		query.setParameter(COLLECTION_TYPE, collectionType);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		query.setFirstResult(offset);
		query.setMaxResults(limit > MAX_LIMIT ? MAX_LIMIT : limit);
		return list(query);
	}

	@Override
	public List<Map<String, Object>> getCollectionItems(String gooruOid, String collectionType, int limit, int offset) {
		StringBuilder sql = new StringBuilder(GET_COLLECTIONS);
		sql.append("where co.gooru_oid =:gooruOid and cc.collection_type  =:collectionType order by  ci.item_sequence");
		Query query = getSession().createSQLQuery(sql.toString());
		query.setParameter(GOORU_OID, gooruOid);
		query.setParameter(COLLECTION_TYPE, collectionType);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		query.setFirstResult(offset);
		query.setMaxResults(limit > MAX_LIMIT ? MAX_LIMIT : limit);
		return list(query);
	}

	@Override
	public List<Map<String, Object>> getCollectionItems(String gooruOid, String[] collectionType, int limit, int offset) {
		StringBuilder sql = new StringBuilder(GET_COLLECTION_ITEMS);
		sql.append("where co.gooru_oid =:gooruOid and cc.collection_type in (:collectionType) order by  ci.item_sequence");
		Query query = getSession().createSQLQuery(sql.toString());
		query.setParameter(GOORU_OID, gooruOid);
		query.setParameterList(COLLECTION_TYPE, collectionType);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		query.setFirstResult(offset);
		query.setMaxResults(limit > MAX_LIMIT ? MAX_LIMIT : limit);
		return list(query);
	}

	@Override
	public String getUserIdByUsername(String username) {
		Query query = getSession().createSQLQuery(GET_USER_UID);
		query.setParameter(USER_NAME, username);
		List<String> results = list(query);
		return results.size() > 0 ? results.get(0) : null;
	}

	@Override
	public List<Map<String, Object>> getLibraries(int categoryId) {
		Query query = getSession().createSQLQuery(GET_LIBRARIES);
		query.setParameter(CATEGORY_ID, categoryId);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		return list(query);
	}

	@Override
	public List<Map<String, Object>> getCollectionResourceItems(String gooruOid, int limit, int offset) {
		Query query = getSession().createSQLQuery(GET_COLLECTION_RESOURCE_ITEMS);
		query.setParameter(GOORU_OID, gooruOid);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		query.setFirstResult(offset);
		query.setMaxResults(limit > MAX_LIMIT ? MAX_LIMIT : limit);
		return list(query);
	}
}
